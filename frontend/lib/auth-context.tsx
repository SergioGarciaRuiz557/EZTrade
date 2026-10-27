"use client"

import { createContext, useCallback, useContext, useEffect, useState, ReactNode } from "react"
import { authApi } from "@/features/auth/api"

// Minimal user data the frontend needs to display identity and menus.
interface User {
  email: string
  firstname: string
  lastname: string
  username: string
}

// Public contract for the authentication context used by screens.
interface AuthContextType {
  user: User | null
  token: string | null
  isLoading: boolean
  login: (identifier: string, password: string) => Promise<void>
  register: (data: {
    firstname: string
    lastname: string
    username: string
    email: string
    password: string
  }) => Promise<void>
  logout: () => void
}

// The context starts undefined to detect usage outside AuthProvider.
const AuthContext = createContext<AuthContextType | undefined>(undefined)

// JWT fields read on the client to recover the user and validate expiration.
interface JwtPayload {
  sub?: string
  exp?: number
}

// Decodes the JWT payload without validating the signature; used only for UX and session loading.
function decodeJwtPayload(token: string): JwtPayload | null {
  const parts = token.split(".")
  if (parts.length !== 3) return null

  try {
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/")
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=")
    const json = atob(padded)
    return JSON.parse(json) as JwtPayload
  } catch {
    return null
  }
}

// Checks the token expiration date when the backend includes it.
function isTokenExpired(payload: JwtPayload): boolean {
  if (typeof payload.exp !== "number") return false
  return Date.now() >= payload.exp * 1000
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  // Clears local persistence so no future request reuses an invalid session.
  const clearAuthStorage = useCallback(() => {
    localStorage.removeItem("token")
    localStorage.removeItem("user")
  }, [])

  // Clears storage and React state on logout or when a 401 is detected.
  const clearAuthState = useCallback(() => {
    clearAuthStorage()
    setToken(null)
    setUser(null)
  }, [clearAuthStorage])

  useEffect(() => {
    let isActive = true

    // Restores the saved session at app startup and revalidates the user against the backend.
    const loadSession = async () => {
      const storedToken = localStorage.getItem("token")
      const storedUserRaw = localStorage.getItem("user")

      if (!storedToken) {
        if (isActive) setIsLoading(false)
        return
      }

      // Used as a fallback if the backend does not respond but the token still appears valid.
      let storedUser: User | null = null
      if (storedUserRaw) {
        try {
          storedUser = JSON.parse(storedUserRaw) as User
        } catch {
          storedUser = null
        }
      }

      const payload = decodeJwtPayload(storedToken)
      if (!payload || !payload.sub || isTokenExpired(payload)) {
        clearAuthStorage()
        if (isActive) {
          setToken(null)
          setUser(null)
          setIsLoading(false)
        }
        return
      }

      // Avoids overwriting state if another action changed the token while this request was in flight.
      const isStoredTokenCurrent = () => localStorage.getItem("token") === storedToken

      try {
        const userInfo = await authApi.getUser(payload.sub)
        if (!isStoredTokenCurrent()) return

        localStorage.setItem("user", JSON.stringify(userInfo))
        if (isActive) {
          setToken(storedToken)
          setUser(userInfo)
        }
      } catch (error) {
        const status =
          typeof error === "object" && error !== null && "status" in error
            ? (error as { status?: number }).status
            : undefined

        if (!isStoredTokenCurrent()) return

        if (status === 401 || status === 403) {
          clearAuthStorage()
          if (isActive) {
            setToken(null)
            setUser(null)
          }
        } else if (isActive) {
          setToken(storedToken)
          setUser(storedUser)
        }
      } finally {
        if (isActive) setIsLoading(false)
      }
    }

    loadSession()

    return () => {
      isActive = false
    }
  }, [clearAuthStorage])

  useEffect(() => {
    // The API layer emits this event when a request receives 401 with the current token.
    const handleUnauthorized = () => {
      clearAuthState()
    }

    window.addEventListener("auth:unauthorized", handleUnauthorized)
    return () => {
      window.removeEventListener("auth:unauthorized", handleUnauthorized)
    }
  }, [clearAuthState])

  // Logs in, stores the JWT, and downloads the complete user data.
  const login = async (identifier: string, password: string) => {
    const response = await authApi.login(identifier, password)
    const jwtToken = response.token

    const payload = decodeJwtPayload(jwtToken)
    if (!payload?.sub || isTokenExpired(payload)) {
      throw new Error("Invalid token")
    }

    localStorage.setItem("token", jwtToken)

    try {
      const userInfo = await authApi.getUser(payload.sub, { token: jwtToken })
      localStorage.setItem("user", JSON.stringify(userInfo))
      setToken(jwtToken)
      setUser(userInfo)
    } catch (error) {
      if (localStorage.getItem("token") === jwtToken) {
        clearAuthStorage()
      }
      throw error
    }
  }

  // Registers a new account. The app redirects to login after signup.
  const register = async (data: {
    firstname: string
    lastname: string
    username: string
    email: string
    password: string
  }) => {
    await authApi.register(data)
  }

  // Local logout: does not depend on a server response.
  const logout = () => {
    clearAuthState()
  }

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// Single hook for consuming authentication in components and protected layouts.
export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}

"use client"

import { createContext, useCallback, useContext, useEffect, useState, ReactNode } from "react"
import { authApi } from "./api"

interface User {
  email: string
  firstname: string
  lastname: string
  username: string
}

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

const AuthContext = createContext<AuthContextType | undefined>(undefined)

interface JwtPayload {
  sub?: string
  exp?: number
}

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

function isTokenExpired(payload: JwtPayload): boolean {
  if (typeof payload.exp !== "number") return false
  return Date.now() >= payload.exp * 1000
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const clearAuthStorage = useCallback(() => {
    localStorage.removeItem("token")
    localStorage.removeItem("user")
  }, [])

  const clearAuthState = useCallback(() => {
    clearAuthStorage()
    setToken(null)
    setUser(null)
  }, [clearAuthStorage])

  useEffect(() => {
    let isActive = true

    const loadSession = async () => {
      const storedToken = localStorage.getItem("token")
      const storedUserRaw = localStorage.getItem("user")

      if (!storedToken) {
        if (isActive) setIsLoading(false)
        return
      }

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

      try {
        const userInfo = await authApi.getUser(payload.sub)
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
    const handleUnauthorized = () => {
      clearAuthState()
    }

    window.addEventListener("auth:unauthorized", handleUnauthorized)
    return () => {
      window.removeEventListener("auth:unauthorized", handleUnauthorized)
    }
  }, [clearAuthState])

  const login = async (identifier: string, password: string) => {
    const response = await authApi.login(identifier, password)
    const jwtToken = response.token
    localStorage.setItem("token", jwtToken)
    setToken(jwtToken)

    // Decode JWT to get user info (basic decode, no verification)
    const payload = JSON.parse(atob(jwtToken.split(".")[1]))
    const userInfo = await authApi.getUser(payload.sub)
    localStorage.setItem("user", JSON.stringify(userInfo))
    setUser(userInfo)
  }

  const register = async (data: {
    firstname: string
    lastname: string
    username: string
    email: string
    password: string
  }) => {
    await authApi.register(data)
  }

  const logout = () => {
    clearAuthState()
  }

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}

"use client"

import { createContext, useCallback, useContext, useEffect, useState, ReactNode } from "react"
import { authApi } from "@/features/auth/api"

// Datos minimos del usuario que el frontend necesita para mostrar identidad y menu.
interface User {
  email: string
  firstname: string
  lastname: string
  username: string
}

// Contrato publico del contexto de autenticacion usado por las pantallas.
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

// El contexto empieza indefinido para detectar usos fuera del AuthProvider.
const AuthContext = createContext<AuthContextType | undefined>(undefined)

// Campos del JWT que se leen en cliente para recuperar usuario y validar expiracion.
interface JwtPayload {
  sub?: string
  exp?: number
}

// Decodifica el payload del JWT sin validar firma; solo se usa para UX y carga de sesion.
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

// Comprueba la fecha de expiracion del token cuando el backend la incluye.
function isTokenExpired(payload: JwtPayload): boolean {
  if (typeof payload.exp !== "number") return false
  return Date.now() >= payload.exp * 1000
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  // Limpia persistencia local para que ninguna peticion futura reutilice una sesion invalida.
  const clearAuthStorage = useCallback(() => {
    localStorage.removeItem("token")
    localStorage.removeItem("user")
  }, [])

  // Limpia almacenamiento y estado React al cerrar sesion o detectar un 401.
  const clearAuthState = useCallback(() => {
    clearAuthStorage()
    setToken(null)
    setUser(null)
  }, [clearAuthStorage])

  useEffect(() => {
    let isActive = true

    // Restaura la sesion guardada al arrancar la app y revalida el usuario contra el backend.
    const loadSession = async () => {
      const storedToken = localStorage.getItem("token")
      const storedUserRaw = localStorage.getItem("user")

      if (!storedToken) {
        if (isActive) setIsLoading(false)
        return
      }

      // Se usa como respaldo si el backend no responde pero el token todavia parece valido.
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

      // Evita pisar el estado si otra accion cambio el token mientras esta peticion estaba en vuelo.
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
    // La capa API emite este evento cuando una peticion recibe 401 con el token vigente.
    const handleUnauthorized = () => {
      clearAuthState()
    }

    window.addEventListener("auth:unauthorized", handleUnauthorized)
    return () => {
      window.removeEventListener("auth:unauthorized", handleUnauthorized)
    }
  }, [clearAuthState])

  // Inicia sesion, guarda el JWT y descarga los datos completos del usuario.
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

  // Registra una cuenta nueva. La app redirige al login despues del alta.
  const register = async (data: {
    firstname: string
    lastname: string
    username: string
    email: string
    password: string
  }) => {
    await authApi.register(data)
  }

  // Cierre de sesion local: no depende de respuesta del servidor.
  const logout = () => {
    clearAuthState()
  }

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// Hook unico para consumir autenticacion en componentes y layouts protegidos.
export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}

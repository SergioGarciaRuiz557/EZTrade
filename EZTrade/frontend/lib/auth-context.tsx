"use client"

import { createContext, useContext, useEffect, useState, ReactNode } from "react"
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

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const storedToken = localStorage.getItem("token")
    const storedUser = localStorage.getItem("user")
    if (storedToken && storedUser) {
      setToken(storedToken)
      setUser(JSON.parse(storedUser))
    }
    setIsLoading(false)
  }, [])

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
    localStorage.removeItem("token")
    localStorage.removeItem("user")
    setToken(null)
    setUser(null)
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

"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  const { token, isLoading } = useAuth()
  const router = useRouter()

  // Las paginas de login/registro solo son visibles para usuarios sin sesion activa.
  useEffect(() => {
    if (!isLoading && token) {
      router.push("/")
    }
  }, [token, isLoading, router])

  // Mientras se restaura la sesion se muestra un indicador para evitar parpadeos de la pagina auth.
  if (isLoading && token) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  // Si ya hay token, el efecto redirige y este layout no pinta el formulario.
  if (token) {
    return null
  }

  return <>{children}</>
}

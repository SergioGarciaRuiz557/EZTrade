"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { Sidebar } from "@/components/sidebar"

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const { token, isLoading } = useAuth()
  const router = useRouter()

  // Cualquier ruta del grupo dashboard requiere una sesion valida.
  useEffect(() => {
    if (!isLoading && !token) {
      router.push("/login")
    }
  }, [token, isLoading, router])

  // Se espera a que AuthProvider termine de restaurar localStorage antes de decidir.
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  // El efecto redirige a login; devolver null evita mostrar contenido protegido.
  if (!token) {
    return null
  }

  return (
    <div className="min-h-screen">
      <Sidebar />
      <main className="md:pl-64">
        <div className="px-4 pb-24 pt-20 sm:px-6 md:p-8">{children}</div>
      </main>
    </div>
  )
}

"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { Sidebar } from "@/components/sidebar"

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const { token, isLoading } = useAuth()
  const router = useRouter()

  // Every route in the dashboard group requires a valid session.
  useEffect(() => {
    if (!isLoading && !token) {
      router.push("/login")
    }
  }, [token, isLoading, router])

  // Wait for AuthProvider to finish restoring localStorage before deciding.
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  // The effect redirects to login; returning null avoids showing protected content.
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

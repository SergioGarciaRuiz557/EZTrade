"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  const { token, isLoading } = useAuth()
  const router = useRouter()

  // Login/register pages are only visible to users without an active session.
  useEffect(() => {
    if (!isLoading && token) {
      router.push("/")
    }
  }, [token, isLoading, router])

  // While the session is restored, show an indicator to avoid auth page flicker.
  if (isLoading && token) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  // If a token already exists, the effect redirects and this layout does not render the form.
  if (token) {
    return null
  }

  return <>{children}</>
}

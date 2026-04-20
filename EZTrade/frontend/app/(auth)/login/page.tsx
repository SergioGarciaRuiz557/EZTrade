"use client"

import { useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { toast } from "@/components/ui/toaster"
import { TrendingUp, Loader2 } from "lucide-react"

export default function LoginPage() {
  const [identifier, setIdentifier] = useState("")
  const [password, setPassword] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth()
  const router = useRouter()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)

    try {
      await login(identifier, password)
      toast({ title: "Bienvenido", description: "Has iniciado sesion correctamente" })
      router.push("/")
    } catch {
      toast({ title: "Error", description: "Credenciales incorrectas", variant: "destructive" })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-12">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1 text-center">
          <div className="flex justify-center mb-4">
            <div className="flex items-center gap-2">
              <div className="h-10 w-10 rounded-lg bg-primary flex items-center justify-center">
                <TrendingUp className="h-6 w-6 text-primary-foreground" />
              </div>
              <span className="text-2xl font-bold">EZTrade</span>
            </div>
          </div>
          <CardTitle className="text-2xl">Iniciar sesion</CardTitle>
          <CardDescription>Introduce tus credenciales para acceder a tu cuenta</CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="identifier">Email o nombre de usuario</Label>
              <Input
                id="identifier"
                type="text"
                placeholder="tu@email.com"
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Contrasena</Label>
              <Input
                id="password"
                type="password"
                placeholder="Tu contrasena"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Iniciando sesion...
                </>
              ) : (
                "Iniciar sesion"
              )}
            </Button>
            <p className="text-sm text-muted-foreground text-center">
              No tienes cuenta?{" "}
              <Link href="/register" className="text-primary hover:underline">
                Registrarse
              </Link>
            </p>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}

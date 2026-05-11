import type { Metadata, Viewport } from "next"
import { Inter, JetBrains_Mono } from "next/font/google"
import "./globals.css"
import { AuthProvider } from "@/lib/auth-context"
import { Toaster } from "@/components/ui/toaster"
import { NotificationsWebSocket } from "@/components/notifications/notifications-ws"

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
})

// Fuente monoespaciada para cifras, simbolos y cualquier dato que convenga alinear visualmente.
const jetbrainsMono = JetBrains_Mono({
  subsets: ["latin"],
  variable: "--font-jetbrains-mono",
})

export const metadata: Metadata = {
  title: "EZTrade - Plataforma de Trading",
  description: "Opera en los mercados financieros de forma sencilla y segura con EZTrade",
}

// Configuracion de viewport y color del navegador en dispositivos moviles.
export const viewport: Viewport = {
  themeColor: "#FBBF24",
  width: "device-width",
  initialScale: 1,
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="es" className={`${inter.variable} ${jetbrainsMono.variable} dark bg-background`}>
      <body className="font-sans antialiased">
        {/* AuthProvider envuelve toda la aplicacion para compartir usuario, token y acciones de sesion. */}
        <AuthProvider>
          {children}
          {/* Conexion global para notificaciones privadas y contenedor unico de toasts. */}
          <NotificationsWebSocket />
          <Toaster />
        </AuthProvider>
      </body>
    </html>
  )
}

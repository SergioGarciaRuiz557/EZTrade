"use client"

import { useState } from "react"
import Link from "next/link"
import { useAuth } from "@/lib/auth-context"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { 
  TrendingUp, 
  TrendingDown, 
  BarChart3, 
  LineChart, 
  ArrowRight,
  Lock,
  Wallet,
  PieChart
} from "lucide-react"

// Datos de ejemplo para los gráficos
const marketData = [
  { symbol: "AAPL", name: "Apple Inc.", price: 178.52, change: 2.34, changePercent: 1.33 },
  { symbol: "GOOGL", name: "Alphabet Inc.", price: 141.80, change: -1.23, changePercent: -0.86 },
  { symbol: "MSFT", name: "Microsoft Corp.", price: 378.91, change: 5.67, changePercent: 1.52 },
  { symbol: "AMZN", name: "Amazon.com Inc.", price: 178.25, change: 3.12, changePercent: 1.78 },
  { symbol: "NVDA", name: "NVIDIA Corp.", price: 875.28, change: -12.45, changePercent: -1.40 },
  { symbol: "TSLA", name: "Tesla Inc.", price: 248.50, change: 8.90, changePercent: 3.71 },
]

const sectorPerformance = [
  { name: "Tecnologia", change: 2.4 },
  { name: "Salud", change: 1.2 },
  { name: "Finanzas", change: -0.8 },
  { name: "Energia", change: 3.1 },
  { name: "Consumo", change: 0.5 },
]

const marketIndices = [
  { name: "S&P 500", value: "4,783.45", change: 1.23 },
  { name: "NASDAQ", value: "15,055.65", change: 1.87 },
  { name: "DOW JONES", value: "37,656.52", change: 0.56 },
]

// Componente de gráfico de barras simple con SVG
function SimpleBarChart({ data }: { data: { name: string; change: number }[] }) {
  const maxValue = Math.max(...data.map(d => Math.abs(d.change)))
  
  return (
    <div className="space-y-3">
      {data.map((item, index) => (
        <div key={index} className="flex items-center gap-3">
          <span className="text-sm text-muted-foreground w-24 truncate">{item.name}</span>
          <div className="flex-1 h-6 bg-secondary rounded-full overflow-hidden relative">
            <div 
              className={`h-full rounded-full transition-all ${item.change >= 0 ? 'bg-success' : 'bg-destructive'}`}
              style={{ width: `${(Math.abs(item.change) / maxValue) * 100}%` }}
            />
          </div>
          <span className={`text-sm font-medium w-16 text-right ${item.change >= 0 ? 'text-success' : 'text-destructive'}`}>
            {item.change >= 0 ? '+' : ''}{item.change}%
          </span>
        </div>
      ))}
    </div>
  )
}

// Componente de gráfico de líneas simple con SVG
function SimpleLineChart() {
  const points = [40, 65, 45, 70, 55, 80, 60, 90, 75, 95, 85, 100]
  const width = 300
  const height = 100
  const padding = 10
  
  const maxVal = Math.max(...points)
  const minVal = Math.min(...points)
  
  const getX = (index: number) => padding + (index / (points.length - 1)) * (width - 2 * padding)
  const getY = (value: number) => height - padding - ((value - minVal) / (maxVal - minVal)) * (height - 2 * padding)
  
  const pathD = points.map((point, index) => 
    `${index === 0 ? 'M' : 'L'} ${getX(index)} ${getY(point)}`
  ).join(' ')
  
  const areaD = `${pathD} L ${getX(points.length - 1)} ${height - padding} L ${getX(0)} ${height - padding} Z`
  
  return (
    <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-32">
      <defs>
        <linearGradient id="gradient" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stopColor="hsl(var(--primary))" stopOpacity="0.3" />
          <stop offset="100%" stopColor="hsl(var(--primary))" stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={areaD} fill="url(#gradient)" />
      <path d={pathD} fill="none" stroke="hsl(var(--primary))" strokeWidth="2" />
      {points.map((point, index) => (
        <circle 
          key={index}
          cx={getX(index)} 
          cy={getY(point)} 
          r="3" 
          fill="hsl(var(--primary))"
          className="opacity-0 hover:opacity-100 transition-opacity"
        />
      ))}
    </svg>
  )
}

// Modal de login requerido
function LoginRequiredModal({ onClose }: { onClose: () => void }) {
  return (
    <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="mx-auto w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mb-4">
            <Lock className="w-6 h-6 text-primary" />
          </div>
          <CardTitle>Acceso requerido</CardTitle>
          <CardDescription>
            Para interactuar con los datos del mercado y gestionar tu portfolio, necesitas iniciar sesion.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <Link href="/login" className="block">
            <Button className="w-full">Iniciar sesion</Button>
          </Link>
          <Link href="/register" className="block">
            <Button variant="outline" className="w-full">Crear cuenta</Button>
          </Link>
          <Button variant="ghost" className="w-full" onClick={onClose}>
            Seguir explorando
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}

export default function HomePage() {
  const { token } = useAuth()
  const [showLoginModal, setShowLoginModal] = useState(false)

  const handleInteraction = () => {
    if (!token) {
      setShowLoginModal(true)
    }
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border sticky top-0 bg-background/95 backdrop-blur z-40">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <BarChart3 className="w-8 h-8 text-primary" />
            <span className="text-xl font-bold">EZTrade</span>
          </div>
          <nav className="flex items-center gap-4">
            {token ? (
              <Link href="/dashboard">
                <Button>
                  Ir al Dashboard
                  <ArrowRight className="w-4 h-4 ml-2" />
                </Button>
              </Link>
            ) : (
              <>
                <Link href="/login">
                  <Button variant="ghost">Iniciar sesion</Button>
                </Link>
                <Link href="/register">
                  <Button>Registrarse</Button>
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <section className="container mx-auto px-4 py-12">
        <div className="text-center max-w-3xl mx-auto mb-12">
          <h1 className="text-4xl md:text-5xl font-bold mb-4 text-balance">
            Invierte de forma <span className="text-primary">inteligente</span>
          </h1>
          <p className="text-lg text-muted-foreground text-pretty">
            Explora el mercado, analiza tendencias y gestiona tu portfolio con EZTrade. 
            La plataforma de trading que hace facil invertir.
          </p>
        </div>

        {/* Market Indices */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
          {marketIndices.map((index) => (
            <Card key={index.name} className="cursor-pointer hover:border-primary/50 transition-colors" onClick={handleInteraction}>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-muted-foreground">{index.name}</p>
                    <p className="text-2xl font-bold">{index.value}</p>
                  </div>
                  <div className={`flex items-center gap-1 ${index.change >= 0 ? 'text-success' : 'text-destructive'}`}>
                    {index.change >= 0 ? <TrendingUp className="w-5 h-5" /> : <TrendingDown className="w-5 h-5" />}
                    <span className="font-medium">{index.change >= 0 ? '+' : ''}{index.change}%</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Market Overview */}
      <section className="container mx-auto px-4 pb-12">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Stock List */}
          <Card className="lg:col-span-2">
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle className="flex items-center gap-2">
                    <LineChart className="w-5 h-5 text-primary" />
                    Acciones populares
                  </CardTitle>
                  <CardDescription>Principales movimientos del mercado</CardDescription>
                </div>
                <Button variant="outline" size="sm" onClick={handleInteraction}>
                  Ver todas
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {marketData.map((stock) => (
                  <div 
                    key={stock.symbol} 
                    className="flex items-center justify-between p-3 rounded-lg bg-secondary/50 hover:bg-secondary cursor-pointer transition-colors"
                    onClick={handleInteraction}
                  >
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                        <span className="text-sm font-bold text-primary">{stock.symbol.slice(0, 2)}</span>
                      </div>
                      <div>
                        <p className="font-medium">{stock.symbol}</p>
                        <p className="text-sm text-muted-foreground">{stock.name}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="font-medium">${stock.price.toFixed(2)}</p>
                      <p className={`text-sm ${stock.change >= 0 ? 'text-success' : 'text-destructive'}`}>
                        {stock.change >= 0 ? '+' : ''}{stock.change.toFixed(2)} ({stock.changePercent >= 0 ? '+' : ''}{stock.changePercent.toFixed(2)}%)
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Sector Performance */}
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <PieChart className="w-5 h-5 text-primary" />
                  Rendimiento por sector
                </CardTitle>
                <CardDescription>Ultimas 24 horas</CardDescription>
              </CardHeader>
              <CardContent>
                <SimpleBarChart data={sectorPerformance} />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="w-5 h-5 text-primary" />
                  Tendencia S&P 500
                </CardTitle>
                <CardDescription>Ultimos 12 meses</CardDescription>
              </CardHeader>
              <CardContent>
                <SimpleLineChart />
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="border-t border-border bg-secondary/30">
        <div className="container mx-auto px-4 py-16">
          <h2 className="text-2xl font-bold text-center mb-8">Por que elegir EZTrade</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card className="text-center">
              <CardContent className="pt-6">
                <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mx-auto mb-4">
                  <LineChart className="w-6 h-6 text-primary" />
                </div>
                <h3 className="font-semibold mb-2">Analisis en tiempo real</h3>
                <p className="text-sm text-muted-foreground">
                  Accede a datos actualizados del mercado y toma decisiones informadas.
                </p>
              </CardContent>
            </Card>
            <Card className="text-center">
              <CardContent className="pt-6">
                <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mx-auto mb-4">
                  <Wallet className="w-6 h-6 text-primary" />
                </div>
                <h3 className="font-semibold mb-2">Gestion de portfolio</h3>
                <p className="text-sm text-muted-foreground">
                  Controla tus inversiones y visualiza el rendimiento de tu cartera.
                </p>
              </CardContent>
            </Card>
            <Card className="text-center">
              <CardContent className="pt-6">
                <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mx-auto mb-4">
                  <BarChart3 className="w-6 h-6 text-primary" />
                </div>
                <h3 className="font-semibold mb-2">Trading simplificado</h3>
                <p className="text-sm text-muted-foreground">
                  Compra y vende acciones de forma facil con nuestra interfaz intuitiva.
                </p>
              </CardContent>
            </Card>
          </div>
          
          {!token && (
            <div className="text-center mt-12">
              <Link href="/register">
                <Button size="lg" className="px-8">
                  Empieza a invertir gratis
                  <ArrowRight className="w-4 h-4 ml-2" />
                </Button>
              </Link>
            </div>
          )}
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border py-8">
        <div className="container mx-auto px-4 text-center text-sm text-muted-foreground">
          <p>&copy; 2024 EZTrade. Todos los derechos reservados.</p>
        </div>
      </footer>

      {/* Login Required Modal */}
      {showLoginModal && <LoginRequiredModal onClose={() => setShowLoginModal(false)} />}
    </div>
  )
}

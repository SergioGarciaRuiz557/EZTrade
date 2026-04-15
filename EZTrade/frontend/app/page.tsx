"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { useAuth } from "@/lib/auth-context"
import { marketApi, type MarketPrice, type InstrumentOverview } from "@/lib/api"
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
  PieChart,
  Loader2,
  RefreshCw
} from "lucide-react"

// Simbolos populares para mostrar en la pagina principal
const POPULAR_SYMBOLS = ["AAPL", "GOOGL", "MSFT", "AMZN", "NVDA", "TSLA"]

// Simbolos para indices (ETFs que replican indices)
const INDEX_SYMBOLS = [
  { symbol: "SPY", name: "S&P 500" },
  { symbol: "QQQ", name: "NASDAQ" },
  { symbol: "DIA", name: "DOW JONES" },
]

// Datos de ejemplo para usuarios no logueados
const MOCK_STOCKS = [
  { symbol: "AAPL", name: "Apple Inc.", price: 178.52, sector: "Technology" },
  { symbol: "GOOGL", name: "Alphabet Inc.", price: 141.80, sector: "Technology" },
  { symbol: "MSFT", name: "Microsoft Corp.", price: 378.91, sector: "Technology" },
  { symbol: "AMZN", name: "Amazon.com Inc.", price: 178.25, sector: "Consumer Cyclical" },
  { symbol: "NVDA", name: "NVIDIA Corp.", price: 875.28, sector: "Technology" },
  { symbol: "TSLA", name: "Tesla Inc.", price: 177.48, sector: "Consumer Cyclical" },
]

const MOCK_INDICES = [
  { symbol: "SPY", name: "S&P 500", price: 5234.18 },
  { symbol: "QQQ", name: "NASDAQ", price: 18439.17 },
  { symbol: "DIA", name: "DOW JONES", price: 39087.38 },
]

const MOCK_SECTORS = [
  { name: "Technology", count: 4 },
  { name: "Consumer Cyclical", count: 2 },
]

interface StockData {
  symbol: string
  name: string
  price: number
  sector?: string
}

// Componente de grafico de barras para sectores
function SectorBarChart({ data, blurred }: { data: { name: string; count: number }[]; blurred?: boolean }) {
  const maxValue = Math.max(...data.map(d => d.count), 1)
  
  return (
    <div className={`space-y-3 ${blurred ? "blur-sm select-none pointer-events-none" : ""}`}>
      {data.map((item, index) => (
        <div key={index} className="flex items-center gap-3">
          <span className="text-sm text-muted-foreground w-28 truncate">{item.name}</span>
          <div className="flex-1 h-6 bg-secondary rounded-full overflow-hidden relative">
            <div 
              className="h-full rounded-full transition-all bg-primary"
              style={{ width: `${(item.count / maxValue) * 100}%` }}
            />
          </div>
          <span className="text-sm font-medium w-8 text-right text-muted-foreground">
            {item.count}
          </span>
        </div>
      ))}
    </div>
  )
}

// Componente de grafico de lineas simple
function SimpleLineChart({ blurred }: { blurred?: boolean }) {
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
    <svg viewBox={`0 0 ${width} ${height}`} className={`w-full h-32 ${blurred ? "blur-sm" : ""}`}>
      <defs>
        <linearGradient id="gradient" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stopColor="hsl(var(--primary))" stopOpacity="0.3" />
          <stop offset="100%" stopColor="hsl(var(--primary))" stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={areaD} fill="url(#gradient)" />
      <path d={pathD} fill="none" stroke="hsl(var(--primary))" strokeWidth="2" />
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
            Para ver los datos reales del mercado y gestionar tu portfolio, necesitas iniciar sesion.
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

// Overlay para datos borrosos
function BlurOverlay({ onClick }: { onClick: () => void }) {
  return (
    <div 
      className="absolute inset-0 flex items-center justify-center bg-background/30 backdrop-blur-[2px] cursor-pointer z-10 rounded-lg"
      onClick={onClick}
    >
      <div className="flex flex-col items-center gap-2 text-center p-4">
        <Lock className="w-6 h-6 text-primary" />
        <span className="text-sm font-medium">Inicia sesion para ver datos reales</span>
      </div>
    </div>
  )
}

export default function HomePage() {
  const { token } = useAuth()
  const [showLoginModal, setShowLoginModal] = useState(false)
  const [stocks, setStocks] = useState<StockData[]>([])
  const [indices, setIndices] = useState<{ symbol: string; name: string; price: number }[]>([])
  const [sectors, setSectors] = useState<{ name: string; count: number }[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [dataLoaded, setDataLoaded] = useState(false)

  const isLoggedIn = !!token

  const loadMarketData = async () => {
    if (!token) return
    
    setLoading(true)
    setError(null)
    
    try {
      // Cargar precios de acciones populares
      const stockPromises = POPULAR_SYMBOLS.map(async (symbol) => {
        try {
          const [priceData, overviewData] = await Promise.all([
            marketApi.getPrice(symbol),
            marketApi.getOverview(symbol).catch(() => null)
          ])
          return {
            symbol,
            name: overviewData?.name || symbol,
            price: priceData.price,
            sector: overviewData?.sector
          }
        } catch {
          return null
        }
      })

      // Cargar precios de indices
      const indexPromises = INDEX_SYMBOLS.map(async (index) => {
        try {
          const priceData = await marketApi.getPrice(index.symbol)
          return {
            symbol: index.symbol,
            name: index.name,
            price: priceData.price
          }
        } catch {
          return null
        }
      })

      const [stockResults, indexResults] = await Promise.all([
        Promise.all(stockPromises),
        Promise.all(indexPromises)
      ])

      const validStocks = stockResults.filter((s): s is StockData => s !== null)
      const validIndices = indexResults.filter((i): i is { symbol: string; name: string; price: number } => i !== null)

      setStocks(validStocks)
      setIndices(validIndices)

      // Calcular distribucion por sector
      const sectorMap = new Map<string, number>()
      validStocks.forEach(stock => {
        if (stock.sector) {
          sectorMap.set(stock.sector, (sectorMap.get(stock.sector) || 0) + 1)
        }
      })
      setSectors(Array.from(sectorMap.entries()).map(([name, count]) => ({ name, count })))
      setDataLoaded(true)

    } catch (err) {
      setError("No se pudieron cargar los datos del mercado. Asegurate de que el backend este corriendo.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (token && !dataLoaded) {
      loadMarketData()
    }
  }, [token, dataLoaded])

  const handleInteraction = () => {
    if (!token) {
      setShowLoginModal(true)
    }
  }

  // Datos a mostrar (reales si logueado, mock si no)
  const displayStocks = isLoggedIn && dataLoaded ? stocks : MOCK_STOCKS
  const displayIndices = isLoggedIn && dataLoaded ? indices : MOCK_INDICES
  const displaySectors = isLoggedIn && dataLoaded ? sectors : MOCK_SECTORS

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
          {!isLoggedIn && (
            <p className="text-sm text-muted-foreground mt-4">
              Los datos mostrados son de ejemplo. Inicia sesion para ver datos reales del mercado.
            </p>
          )}
        </div>

        {/* Loading state solo si esta logueado y cargando */}
        {isLoggedIn && loading ? (
          <div className="flex items-center justify-center py-8">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
            <span className="ml-2 text-muted-foreground">Cargando datos del mercado...</span>
          </div>
        ) : isLoggedIn && error ? (
          <Card className="mb-8">
            <CardContent className="py-8 text-center">
              <p className="text-muted-foreground mb-4">{error}</p>
              <Button variant="outline" onClick={loadMarketData}>
                <RefreshCw className="w-4 h-4 mr-2" />
                Reintentar
              </Button>
            </CardContent>
          </Card>
        ) : (
          <>
            {/* Indices */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
              {displayIndices.map((index) => (
                <Card 
                  key={index.symbol} 
                  className={`relative cursor-pointer hover:border-primary/50 transition-colors overflow-hidden ${!isLoggedIn ? "select-none" : ""}`}
                  onClick={handleInteraction}
                >
                  {!isLoggedIn && <BlurOverlay onClick={handleInteraction} />}
                  <CardContent className={`p-4 ${!isLoggedIn ? "blur-sm" : ""}`}>
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm text-muted-foreground">{index.name}</p>
                        <p className="text-2xl font-bold">${index.price.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
                      </div>
                      <div className="flex items-center gap-1 text-primary">
                        <TrendingUp className="w-5 h-5" />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </>
        )}
      </section>

      {/* Market Overview */}
      {!(isLoggedIn && loading) && !(isLoggedIn && error) && (
        <section className="container mx-auto px-4 pb-12">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Stock List */}
            <Card className="lg:col-span-2 relative overflow-hidden">
              {!isLoggedIn && <BlurOverlay onClick={handleInteraction} />}
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <LineChart className="w-5 h-5 text-primary" />
                      Acciones populares
                    </CardTitle>
                    <CardDescription>
                      {isLoggedIn ? "Datos en tiempo real del backend" : "Datos de ejemplo - Inicia sesion para ver datos reales"}
                    </CardDescription>
                  </div>
                  {isLoggedIn && (
                    <Button variant="outline" size="sm" onClick={loadMarketData}>
                      <RefreshCw className="w-4 h-4 mr-2" />
                      Actualizar
                    </Button>
                  )}
                </div>
              </CardHeader>
              <CardContent className={!isLoggedIn ? "blur-sm select-none" : ""}>
                <div className="space-y-4">
                  {displayStocks.map((stock) => (
                    <div 
                      key={stock.symbol} 
                      className={`flex items-center justify-between p-3 rounded-lg bg-secondary/50 transition-colors ${isLoggedIn ? "hover:bg-secondary cursor-pointer" : ""}`}
                      onClick={isLoggedIn ? undefined : handleInteraction}
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
                        {stock.sector && (
                          <p className="text-xs text-muted-foreground">{stock.sector}</p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Sector Distribution */}
            <div className="space-y-6">
              <Card className="relative overflow-hidden">
                {!isLoggedIn && <BlurOverlay onClick={handleInteraction} />}
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <PieChart className="w-5 h-5 text-primary" />
                    Distribucion por sector
                  </CardTitle>
                  <CardDescription>
                    {isLoggedIn ? "Acciones mostradas" : "Datos de ejemplo"}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <SectorBarChart data={displaySectors} blurred={!isLoggedIn} />
                </CardContent>
              </Card>

              <Card className="relative overflow-hidden">
                {!isLoggedIn && <BlurOverlay onClick={handleInteraction} />}
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <TrendingUp className="w-5 h-5 text-primary" />
                    Tendencia general
                  </CardTitle>
                  <CardDescription>
                    {isLoggedIn ? "Grafico ilustrativo" : "Datos de ejemplo"}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <SimpleLineChart blurred={!isLoggedIn} />
                </CardContent>
              </Card>
            </div>
          </div>
        </section>
      )}

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

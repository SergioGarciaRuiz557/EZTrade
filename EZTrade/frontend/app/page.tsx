"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { marketApi, tradingApi, type Candle } from "@/lib/api"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { toast } from "@/components/ui/toaster"
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts"
import { 
  TrendingUp, 
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

const MOCK_CANDLES: Candle[] = [
  { time: "2026-04-04T00:00:00", open: 170.1, high: 172.8, low: 169.4, close: 171.9, volume: 9520000 },
  { time: "2026-04-05T00:00:00", open: 171.9, high: 173.2, low: 170.5, close: 172.6, volume: 10840000 },
  { time: "2026-04-06T00:00:00", open: 172.6, high: 175.4, low: 172.1, close: 174.8, volume: 12530000 },
  { time: "2026-04-07T00:00:00", open: 174.8, high: 176.2, low: 173.6, close: 174.2, volume: 9980000 },
  { time: "2026-04-08T00:00:00", open: 174.2, high: 177.1, low: 173.8, close: 176.5, volume: 14020000 },
  { time: "2026-04-09T00:00:00", open: 176.5, high: 178.4, low: 175.7, close: 177.9, volume: 13350000 },
  { time: "2026-04-10T00:00:00", open: 177.9, high: 179.1, low: 176.2, close: 178.2, volume: 12190000 },
  { time: "2026-04-11T00:00:00", open: 178.2, high: 180.3, low: 177.8, close: 179.7, volume: 11680000 },
  { time: "2026-04-12T00:00:00", open: 179.7, high: 181.6, low: 178.9, close: 180.9, volume: 14230000 },
  { time: "2026-04-13T00:00:00", open: 180.9, high: 182.4, low: 180.3, close: 181.8, volume: 13100000 },
  { time: "2026-04-14T00:00:00", open: 181.8, high: 183.1, low: 180.6, close: 182.5, volume: 12040000 },
  { time: "2026-04-15T00:00:00", open: 182.5, high: 184.2, low: 181.7, close: 183.8, volume: 14920000 },
]

interface StockData {
  symbol: string
  name: string
  price: number
  sector?: string
}

interface SelectedAsset {
  symbol: string
  name: string
  price: number
  sector?: string
  type: "accion" | "indice"
}

interface CandleChartPoint {
  time: string
  label: string
  open: number
  high: number
  low: number
  close: number
  volume: number
}

// Componente de grafico de barras para sectores
function SectorBarChart({ data, blurred }: { data: { name: string; count: number }[]; blurred?: boolean }) {
  if (data.length === 0) {
    return (
      <div className="text-sm text-muted-foreground">
        No hay datos de sectores disponibles por ahora.
      </div>
    )
  }

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

function DailyCandleChart({ data, blurred }: { data: Candle[]; blurred?: boolean }) {
  const chartData: CandleChartPoint[] = data
    .slice(-30)
    .map((candle) => {
      const date = new Date(candle.time)
      const isValidDate = !Number.isNaN(date.getTime())
      const label = isValidDate
        ? date.toLocaleDateString("es-ES", { day: "2-digit", month: "2-digit" })
        : candle.time.slice(0, 10)
      return {
        ...candle,
        label,
      }
    })

  if (chartData.length === 0) {
    return <p className="text-sm text-muted-foreground">No hay velas diarias disponibles para este simbolo.</p>
  }

  const latest = chartData[chartData.length - 1]
  const first = chartData[0]
  const change = latest.close - first.open
  const changePct = first.open > 0 ? (change / first.open) * 100 : 0

  return (
    <div className={blurred ? "blur-sm select-none pointer-events-none" : ""}>
      <div className="mb-4 grid grid-cols-2 gap-3 text-sm">
        <div className="rounded-md border p-3">
          <p className="text-muted-foreground">Cierre actual</p>
          <p className="font-semibold">${latest.close.toFixed(2)}</p>
        </div>
        <div className="rounded-md border p-3">
          <p className="text-muted-foreground">Variacion periodo</p>
          <p className={`font-semibold ${change >= 0 ? "text-success" : "text-destructive"}`}>
            {change >= 0 ? "+" : ""}{change.toFixed(2)} ({changePct.toFixed(2)}%)
          </p>
        </div>
      </div>

      <div className="h-44 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={chartData} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="home-candle-close" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="hsl(var(--primary))" stopOpacity={0.35} />
                <stop offset="95%" stopColor="hsl(var(--primary))" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
            <XAxis dataKey="label" tick={{ fontSize: 11 }} minTickGap={24} />
            <YAxis domain={["dataMin - 1", "dataMax + 1"]} width={44} tick={{ fontSize: 11 }} />
            <Tooltip
              contentStyle={{
                border: "1px solid hsl(var(--border))",
                borderRadius: "0.5rem",
                background: "hsl(var(--background))",
              }}
              formatter={(value: number, name: string) => {
                if (["open", "high", "low", "close"].includes(name)) {
                  return [`$${Number(value).toFixed(2)}`, name.toUpperCase()]
                }
                return [Number(value).toLocaleString("es-ES"), "Volumen"]
              }}
            />
            <Area
              type="monotone"
              dataKey="close"
              stroke="hsl(var(--primary))"
              strokeWidth={2}
              fill="url(#home-candle-close)"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
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
  const router = useRouter()
  const [showLoginModal, setShowLoginModal] = useState(false)
  const [stocks, setStocks] = useState<StockData[]>([])
  const [indices, setIndices] = useState<{ symbol: string; name: string; price: number }[]>([])
  const [sectors, setSectors] = useState<{ name: string; count: number }[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [dataLoaded, setDataLoaded] = useState(false)
  const [selectedAsset, setSelectedAsset] = useState<SelectedAsset | null>(null)
  const [buyQuantity, setBuyQuantity] = useState("1")
  const [buyPrice, setBuyPrice] = useState("")
  const [isBuying, setIsBuying] = useState(false)
  const [selectedChartSymbol, setSelectedChartSymbol] = useState(POPULAR_SYMBOLS[0])
  const [candlesBySymbol, setCandlesBySymbol] = useState<Record<string, Candle[]>>({})
  const [candlesLoading, setCandlesLoading] = useState(false)
  const [candlesError, setCandlesError] = useState<string | null>(null)

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
          const stock: StockData = {
            symbol,
            name: overviewData?.name || symbol,
            price: priceData.price,
            ...(overviewData?.sector ? { sector: overviewData.sector } : {})
          }
          return stock
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
      const computedSectors = Array.from(sectorMap.entries()).map(([name, count]) => ({ name, count }))
      setSectors(
        computedSectors.length > 0
          ? computedSectors
          : validStocks.length > 0
            ? [{ name: "Sin clasificar", count: validStocks.length }]
            : []
      )
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

  useEffect(() => {
    if (!isLoggedIn) return
    if (candlesBySymbol[selectedChartSymbol]) return

    const loadCandles = async () => {
      setCandlesLoading(true)
      setCandlesError(null)
      try {
        const candles = await marketApi.getDailyCandles(selectedChartSymbol)
        setCandlesBySymbol((prev) => ({
          ...prev,
          [selectedChartSymbol]: candles,
        }))
      } catch {
        setCandlesError("No se pudieron cargar las velas diarias en este momento.")
      } finally {
        setCandlesLoading(false)
      }
    }

    loadCandles()
  }, [isLoggedIn, selectedChartSymbol, candlesBySymbol])

  const handleInteraction = () => {
    if (!token) {
      setShowLoginModal(true)
    }
  }

  const openAssetDialog = (asset: SelectedAsset) => {
    setSelectedAsset(asset)
    setBuyQuantity("1")
    setBuyPrice(asset.price.toFixed(2))
  }

  const handleBuyFromHome = async () => {
    if (!selectedAsset) return

    const parsedQuantity = parseFloat(buyQuantity)
    const parsedPrice = parseFloat(buyPrice)

    if (!Number.isFinite(parsedQuantity) || parsedQuantity <= 0) {
      toast({ title: "Cantidad invalida", description: "Introduce una cantidad mayor que 0", variant: "destructive" })
      return
    }

    if (!Number.isFinite(parsedPrice) || parsedPrice <= 0) {
      toast({ title: "Precio invalido", description: "Introduce un precio mayor que 0", variant: "destructive" })
      return
    }

    setIsBuying(true)
    try {
      await tradingApi.placeOrder({
        symbol: selectedAsset.symbol,
        side: "BUY",
        quantity: parsedQuantity,
        price: parsedPrice,
      })

      toast({
        title: "Orden de compra creada",
        description: `${selectedAsset.symbol} se ha enviado correctamente`,
      })
      setSelectedAsset(null)
      router.push("/dashboard")
    } catch {
      toast({
        title: "No se pudo crear la orden",
        description: "Revisa tus datos e intentalo de nuevo",
        variant: "destructive",
      })
    } finally {
      setIsBuying(false)
    }
  }

  // Datos a mostrar (reales si logueado, mock si no)
  const displayStocks = isLoggedIn && dataLoaded ? stocks : MOCK_STOCKS
  const displayIndices = isLoggedIn && dataLoaded ? indices : MOCK_INDICES
  const displaySectors = isLoggedIn && dataLoaded ? sectors : MOCK_SECTORS
  const displayCandles = isLoggedIn ? candlesBySymbol[selectedChartSymbol] || [] : MOCK_CANDLES

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
                  onClick={() => {
                    if (!isLoggedIn) {
                      handleInteraction()
                      return
                    }
                    openAssetDialog({
                      symbol: index.symbol,
                      name: index.name,
                      price: index.price,
                      type: "indice",
                    })
                  }}
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
                      onClick={() => {
                        if (!isLoggedIn) {
                          handleInteraction()
                          return
                        }
                        openAssetDialog({
                          symbol: stock.symbol,
                          name: stock.name,
                          price: stock.price,
                          sector: stock.sector,
                          type: "accion",
                        })
                      }}
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
                  <div className="flex items-center justify-between gap-2">
                    <div>
                      <CardTitle className="flex items-center gap-2">
                        <TrendingUp className="w-5 h-5 text-primary" />
                        Velas diarias
                      </CardTitle>
                      <CardDescription>
                        {isLoggedIn
                          ? `Datos OHLC de ${selectedChartSymbol}`
                          : "Vista de ejemplo - inicia sesion para datos reales"}
                      </CardDescription>
                    </div>
                  </div>
                  <div className="flex flex-wrap gap-2 pt-2">
                    {POPULAR_SYMBOLS.slice(0, 4).map((symbol) => (
                      <Button
                        key={symbol}
                        size="sm"
                        variant={selectedChartSymbol === symbol ? "default" : "outline"}
                        onClick={() => {
                          if (!isLoggedIn) {
                            handleInteraction()
                            return
                          }
                          setSelectedChartSymbol(symbol)
                        }}
                      >
                        {symbol}
                      </Button>
                    ))}
                  </div>
                </CardHeader>
                <CardContent>
                  {isLoggedIn && candlesLoading && !displayCandles.length ? (
                    <div className="flex items-center justify-center py-8">
                      <Loader2 className="h-5 w-5 animate-spin text-primary" />
                      <span className="ml-2 text-sm text-muted-foreground">Cargando velas...</span>
                    </div>
                  ) : isLoggedIn && candlesError && !displayCandles.length ? (
                    <p className="text-sm text-muted-foreground">{candlesError}</p>
                  ) : (
                    <DailyCandleChart data={displayCandles} blurred={!isLoggedIn} />
                  )}
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

      {/* Asset Buy Modal */}
      <Dialog
        open={!!selectedAsset}
        onOpenChange={(open) => {
          if (!open) {
            setSelectedAsset(null)
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{selectedAsset?.symbol} - {selectedAsset?.name}</DialogTitle>
            <DialogDescription>
              {selectedAsset?.type === "indice" ? "Indice de mercado" : "Accion"}
              {selectedAsset?.sector ? ` | Sector: ${selectedAsset.sector}` : ""}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="rounded-lg bg-muted p-4">
              <p className="text-sm text-muted-foreground">Precio de referencia</p>
              <p className="text-2xl font-bold">
                ${selectedAsset?.price.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="home-buy-quantity">Cantidad</Label>
                <Input
                  id="home-buy-quantity"
                  type="number"
                  min="0"
                  step="0.01"
                  value={buyQuantity}
                  onChange={(e) => setBuyQuantity(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="home-buy-price">Precio limite</Label>
                <Input
                  id="home-buy-price"
                  type="number"
                  min="0"
                  step="0.01"
                  value={buyPrice}
                  onChange={(e) => setBuyPrice(e.target.value)}
                />
              </div>
            </div>

            <div className="rounded-lg border p-3 text-sm text-muted-foreground">
              Total estimado: ${((parseFloat(buyQuantity) || 0) * (parseFloat(buyPrice) || 0)).toFixed(2)}
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setSelectedAsset(null)} disabled={isBuying}>
              Cancelar
            </Button>
            <Button className="bg-success hover:bg-success/90" onClick={handleBuyFromHome} disabled={isBuying || !selectedAsset}>
              {isBuying ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Comprando...
                </>
              ) : (
                "Comprar y ver dashboard"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

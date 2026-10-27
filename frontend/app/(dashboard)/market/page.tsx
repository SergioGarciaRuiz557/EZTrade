"use client"

import { useEffect, useState } from "react"
import { marketApi } from "@/features/market/api"
import type { Instrument, InstrumentOverview, MarketPrice } from "@/features/market/types"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { formatCurrency, cn } from "@/lib/utils"
import { Search, Loader2, TrendingUp, Building2, Globe, DollarSign, BarChart3, Store } from "lucide-react"
import Link from "next/link"

function InstrumentDetails({ symbol }: { symbol: string }) {
  // Loads detail and price for the selected instrument inside the dialog.
  const [overview, setOverview] = useState<InstrumentOverview | null>(null)
  const [price, setPrice] = useState<MarketPrice | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // The cancelled flag avoids setState if the user closes the dialog before the call finishes.
    let cancelled = false

    const fetchData = async () => {
      const [overviewResult, priceResult] = await Promise.allSettled([
        marketApi.getOverview(symbol),
        marketApi.getPrice(symbol),
      ])

      if (!cancelled) {
        if (overviewResult.status === "fulfilled") {
          setOverview(overviewResult.value)
        } else if (priceResult.status === "fulfilled") {
          setOverview({
            symbol,
            name: symbol,
            sector: "",
            industry: "",
            marketCap: 0,
            peRatio: 0,
          })
        }

        if (priceResult.status === "fulfilled") {
          setPrice(priceResult.value)
        }

        setLoading(false)
      }
    }

    setLoading(true)
    setOverview(null)
    setPrice(null)
    fetchData()

    return () => {
      cancelled = true
    }
  }, [symbol])

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  if (!overview) {
    return (
      <div className="text-center py-12 text-muted-foreground">
        <p>No se pudo cargar la informacion del instrumento</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">{overview.symbol}</h2>
          <p className="text-muted-foreground">{overview.name}</p>
        </div>
        {price && (
          <div className="text-right">
            <p className="text-3xl font-bold">{formatCurrency(price.price)}</p>
            <p className="text-sm text-muted-foreground">
              Precio actual
            </p>
          </div>
        )}
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 gap-4">
        <div className="p-4 bg-muted rounded-lg">
          <div className="flex items-center gap-2 text-muted-foreground mb-1">
            <Building2 className="h-4 w-4" />
            <span className="text-sm">Sector</span>
          </div>
          <p className="font-semibold">{overview.sector || "N/A"}</p>
        </div>
        <div className="p-4 bg-muted rounded-lg">
          <div className="flex items-center gap-2 text-muted-foreground mb-1">
            <BarChart3 className="h-4 w-4" />
            <span className="text-sm">Industria</span>
          </div>
          <p className="font-semibold">{overview.industry || "N/A"}</p>
        </div>
        <div className="p-4 bg-muted rounded-lg">
          <div className="flex items-center gap-2 text-muted-foreground mb-1">
            <DollarSign className="h-4 w-4" />
            <span className="text-sm">Cap. de mercado</span>
          </div>
          <p className="font-semibold">
            {overview.marketCap ? formatCurrency(overview.marketCap) : "N/A"}
          </p>
        </div>
        <div className="p-4 bg-muted rounded-lg">
          <div className="flex items-center gap-2 text-muted-foreground mb-1">
            <TrendingUp className="h-4 w-4" />
            <span className="text-sm">PER</span>
          </div>
          <p className="font-semibold">{overview.peRatio?.toFixed(2) || "N/A"}</p>
        </div>
      </div>

      {/* Action */}
      <div className="grid gap-3 sm:grid-cols-3">
        <Button asChild className="bg-success hover:bg-success/90">
          <Link href={`/trading?symbol=${symbol}&side=BUY`}>Comprar</Link>
        </Button>
        <Button asChild variant="outline">
          <Link href={`/trading?symbol=${symbol}&tab=marketplace`}>
            <Store className="mr-2 h-4 w-4" />
            Ofertas
          </Link>
        </Button>
        <Button asChild variant="outline" className="border-destructive text-destructive hover:bg-destructive hover:text-destructive-foreground">
          <Link href={`/trading?symbol=${symbol}&side=SELL`}>Vender</Link>
        </Button>
      </div>
    </div>
  )
}

export default function MarketPage() {
  // Search state, results, and selected symbol for the detail dialog.
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<Instrument[]>([])
  const [loading, setLoading] = useState(false)
  const [selectedSymbol, setSelectedSymbol] = useState<string | null>(null)
  const [hasSearched, setHasSearched] = useState(false)

  // Executes the search against the backend and marks that a query has been performed.
  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!query.trim()) return

    setLoading(true)
    setHasSearched(true)
    try {
      const data = await marketApi.search(query)
      setResults(data)
    } catch {
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  // Quick shortcuts for trying common searches without typing in the form.
  const popularSymbols = ["AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META"]

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Mercado</h1>
        <p className="text-muted-foreground">Busca y explora instrumentos financieros</p>
      </div>

      {/* Search */}
      <Card>
        <CardContent className="pt-6">
          <form onSubmit={handleSearch} className="flex gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Buscar por simbolo o nombre (ej: AAPL, Apple...)"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                className="pl-10"
              />
            </div>
            <Button type="submit" disabled={loading || !query.trim()}>
              {loading ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                "Buscar"
              )}
            </Button>
          </form>

          {/* Popular Symbols */}
          <div className="mt-4 flex items-center gap-2 flex-wrap">
            <span className="text-sm text-muted-foreground">Populares:</span>
            {popularSymbols.map((symbol) => (
              <Button
                key={symbol}
                variant="outline"
                size="sm"
                onClick={() => {
                  setQuery(symbol)
                  setHasSearched(true)
                  setLoading(true)
                  marketApi.search(symbol).then(setResults).finally(() => setLoading(false))
                }}
              >
                {symbol}
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Results */}
      {loading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      ) : hasSearched ? (
        results.length > 0 ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {results.map((instrument) => (
              <Card
                key={instrument.symbol}
                className="cursor-pointer hover:border-primary transition-colors"
                onClick={() => setSelectedSymbol(instrument.symbol)}
              >
                <CardHeader className="pb-2">
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">{instrument.symbol}</CardTitle>
                    <div className="flex items-center gap-1 text-xs text-muted-foreground">
                      <Globe className="h-3 w-3" />
                      {instrument.region}
                    </div>
                  </div>
                  <CardDescription className="line-clamp-1">{instrument.name}</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">Moneda</span>
                    <span className="font-medium">{instrument.currency}</span>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          <Card>
            <CardContent className="py-12 text-center text-muted-foreground">
              <Search className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>No se encontraron resultados para &quot;{query}&quot;</p>
              <p className="text-sm">Intenta con otro simbolo o nombre</p>
            </CardContent>
          </Card>
        )
      ) : (
        <Card>
          <CardContent className="py-12 text-center text-muted-foreground">
            <Search className="h-12 w-12 mx-auto mb-4 opacity-50" />
            <p>Busca instrumentos por simbolo o nombre</p>
            <p className="text-sm">Ejemplo: AAPL, Microsoft, Tesla...</p>
          </CardContent>
        </Card>
      )}

      {/* Detail Dialog */}
      <Dialog
        open={!!selectedSymbol}
        onOpenChange={(open) => {
          if (!open) setSelectedSymbol(null)
        }}
      >
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>Detalle del instrumento</DialogTitle>
          </DialogHeader>
          {selectedSymbol && (
            <InstrumentDetails symbol={selectedSymbol} />
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}

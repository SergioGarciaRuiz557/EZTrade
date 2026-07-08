"use client"

import { useEffect, useState } from "react"
import useSWR, { mutate } from "swr"
import { marketApi } from "@/features/market/api"
import { portfolioApi } from "@/features/portfolio/api"
import { tradingApi } from "@/features/trading/api"
import type { MarketPrice } from "@/features/market/types"
import type { Portfolio } from "@/features/portfolio/types"
import type { TradeOrder } from "@/features/trading/types"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { toast } from "@/components/ui/toaster"
import { formatCurrency, formatNumber, formatDate, cn } from "@/lib/utils"
import {
  AlertTriangle,
  DollarSign,
  LineChart,
  Loader2,
  Play,
  Search,
  ShoppingCart,
  Store,
  Tags,
  X,
} from "lucide-react"

type TradingTab = "buy" | "sell" | "marketplace" | "orders"

interface PositionAccumulator {
  symbol: string
  quantity: number
  averageCost: number
  realizedPnl: number
  updatedAt: string
}

const MARKET_DATA_UNAVAILABLE_MESSAGE =
  "Alpha Vantage no esta disponible. No se puede consultar el precio real de mercado ahora mismo."

// Normalizes symbols so searches and orders use the format expected by the backend.
function normalizeSymbol(value: string) {
  return value.trim().toUpperCase()
}

// Returns 0 if the input is not a positive number, simplifying form validations.
function parsePositiveNumber(value: string) {
  const parsed = Number.parseFloat(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : 0
}

function toFiniteNumber(value: number | null | undefined) {
  return typeof value === "number" && Number.isFinite(value) ? value : 0
}

function getOrderTimestamp(order: TradeOrder) {
  const timestamp = new Date(order.executedAt || order.createdAt).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

function buildPortfolioFromOrders(orders: TradeOrder[] | undefined): Portfolio | null {
  if (!orders) return null

  const positionsBySymbol = new Map<string, PositionAccumulator>()
  const executedOrders = orders
    .filter((order) => order.status === "EXECUTED")
    .slice()
    .sort((a, b) => getOrderTimestamp(a) - getOrderTimestamp(b))

  executedOrders.forEach((order) => {
    const symbol = normalizeSymbol(order.symbol)
    const quantity = toFiniteNumber(order.quantity)
    const price = toFiniteNumber(order.price)
    if (!symbol || quantity <= 0 || price <= 0) return

    const current = positionsBySymbol.get(symbol) || {
      symbol,
      quantity: 0,
      averageCost: 0,
      realizedPnl: 0,
      updatedAt: order.executedAt || order.createdAt,
    }

    if (order.side === "BUY") {
      const nextQuantity = current.quantity + quantity
      const nextCost = current.quantity * current.averageCost + quantity * price
      current.quantity = nextQuantity
      current.averageCost = nextQuantity > 0 ? nextCost / nextQuantity : 0
    } else {
      const soldQuantity = Math.min(quantity, current.quantity)
      current.realizedPnl += (price - current.averageCost) * soldQuantity
      current.quantity = Math.max(0, current.quantity - soldQuantity)
      if (current.quantity === 0) current.averageCost = 0
    }

    current.updatedAt = order.executedAt || order.createdAt
    positionsBySymbol.set(symbol, current)
  })

  const positionStates = Array.from(positionsBySymbol.values())
  const positions = positionStates
    .filter((position) => position.quantity > 0)
    .map((position) => ({
      symbol: position.symbol,
      quantity: position.quantity,
      averageCost: position.averageCost,
      realizedPnl: position.realizedPnl,
      updatedAt: position.updatedAt,
    }))
    .sort((a, b) => a.symbol.localeCompare(b.symbol))

  return {
    owner: orders[0]?.owner || "",
    cashAvailable: 0,
    totalCostBasis: positions.reduce((total, position) => total + position.quantity * position.averageCost, 0),
    totalRealizedPnl: positionStates.reduce((total, position) => total + position.realizedPnl, 0),
    positions,
  }
}

function getErrorText(error: unknown) {
  if (error && typeof error === "object" && "message" in error) {
    const message = (error as { message?: unknown }).message
    if (typeof message === "string") return message
  }
  return ""
}

function isMarketDataError(error: unknown) {
  const message = getErrorText(error).toLowerCase()
  return (
    message.includes("alpha vantage") ||
    message.includes("alphavantage") ||
    message.includes("api key") ||
    message.includes("api call frequency") ||
    message.includes("demo") ||
    message.includes("premium") ||
    (message.includes("no se puede validar") && message.includes("precio actual")) ||
    (message.includes("no se pudo consultar") && message.includes("precio"))
  )
}

// Extracts messages from errors thrown by the API layer and uses safe text when there is no detail.
function getErrorMessage(error: unknown, fallback: string) {
  if (isMarketDataError(error)) return MARKET_DATA_UNAVAILABLE_MESSAGE

  const message = getErrorText(error)
  if (message.trim()) return message
  return fallback
}

function MarketDataNotice({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex items-start gap-3 rounded-md border border-warning/40 bg-warning/10 p-3 text-sm text-muted-foreground">
      <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-warning" />
      <p>{children}</p>
    </div>
  )
}

// Refreshes related caches after creating, executing, or cancelling trading actions.
function refreshTradingData() {
  mutate("orders")
  mutate("portfolio")
  mutate("wallet")
}

// Form for creating buy orders using the current market price.
function BuyOrderForm({ initialSymbol, onOrderCreated }: { initialSymbol: string; onOrderCreated: () => void }) {
  const [symbol, setSymbol] = useState("")
  const [quantity, setQuantity] = useState("")
  const [marketPrice, setMarketPrice] = useState<MarketPrice | null>(null)
  const [marketNotice, setMarketNotice] = useState("")
  const [isPriceLoading, setIsPriceLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    // If reached from Market or the home page, preloads symbol and price automatically.
    const nextSymbol = normalizeSymbol(initialSymbol)
    if (!nextSymbol) return

    setSymbol(nextSymbol)
    setMarketPrice(null)
    setMarketNotice("")

    let cancelled = false
    setIsPriceLoading(true)
    marketApi
      .getPrice(nextSymbol)
      .then((data) => {
        if (!cancelled) {
          setMarketPrice(data)
          setMarketNotice("")
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setMarketPrice(null)
          setMarketNotice(getErrorMessage(error, MARKET_DATA_UNAVAILABLE_MESSAGE))
        }
      })
      .finally(() => {
        if (!cancelled) setIsPriceLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [initialSymbol])

  // Manual price lookup for the typed symbol.
  const handleLookup = async () => {
    const nextSymbol = normalizeSymbol(symbol)
    if (!nextSymbol) return

    setSymbol(nextSymbol)
    setMarketNotice("")
    setIsPriceLoading(true)
    try {
      const price = await marketApi.getPrice(nextSymbol)
      setMarketPrice(price)
      setMarketNotice("")
    } catch (error) {
      setMarketPrice(null)
      setMarketNotice(getErrorMessage(error, MARKET_DATA_UNAVAILABLE_MESSAGE))
    } finally {
      setIsPriceLoading(false)
    }
  }

  // Creates a pending BUY order after validating symbol and quantity.
  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    const nextSymbol = normalizeSymbol(symbol)
    const nextQuantity = parsePositiveNumber(quantity)
    if (!nextSymbol || nextQuantity <= 0) return

    setIsSubmitting(true)
    let priceResolved = Boolean(marketPrice)
    try {
      const price = marketPrice ?? await marketApi.getPrice(nextSymbol)
      priceResolved = true
      setMarketPrice(price)
      setMarketNotice("")
      await tradingApi.placeOrder({
        symbol: nextSymbol,
        side: "BUY",
        quantity: nextQuantity,
        price: price.price,
      })
      setQuantity("")
      refreshTradingData()
      toast({
        title: "Orden creada",
        description: `${nextSymbol} queda pendiente con fondos reservados. Ejecutala o cancelala desde Ordenes.`,
        variant: "success",
      })
      onOrderCreated()
    } catch (error) {
      const marketDataFailure = !priceResolved || isMarketDataError(error)
      const description = marketDataFailure
        ? MARKET_DATA_UNAVAILABLE_MESSAGE
        : getErrorMessage(error, "El backend rechazo la orden")
      if (marketDataFailure) setMarketNotice(description)
      toast({
        title: marketDataFailure ? "Precio no disponible" : "No se pudo crear la orden",
        description,
        variant: marketDataFailure ? "warning" : "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  // Derived values that feed the visual summary and button state.
  const quantityValue = parsePositiveNumber(quantity)
  const estimatedTotal = quantityValue * (marketPrice?.price || 0)
  const canSubmit = Boolean(normalizeSymbol(symbol)) && quantityValue > 0

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <ShoppingCart className="h-5 w-5 text-success" />
          Preparar compra
        </CardTitle>
        <CardDescription>Crea una orden pendiente usando el precio actual</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="buy-symbol">Simbolo</Label>
            <div className="flex gap-2">
              <Input
                id="buy-symbol"
                placeholder="AAPL, GOOGL, MSFT..."
                value={symbol}
                onChange={(event) => {
                  setSymbol(event.target.value.toUpperCase())
                  setMarketPrice(null)
                  setMarketNotice("")
                }}
                required
              />
              <Button type="button" variant="outline" onClick={handleLookup} disabled={!symbol.trim() || isPriceLoading}>
                {isPriceLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
              </Button>
            </div>
          </div>

          {marketNotice && <MarketDataNotice>{marketNotice}</MarketDataNotice>}

          <div className="space-y-2">
            <Label htmlFor="buy-quantity">Cantidad</Label>
            <Input
              id="buy-quantity"
              type="number"
              step="0.01"
              min="0"
              placeholder="0.00"
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
              required
            />
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="rounded-lg bg-muted p-4">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <DollarSign className="h-4 w-4" />
                Precio actual
              </div>
              <p className="mt-1 text-xl font-bold">
                {marketPrice ? formatCurrency(marketPrice.price) : "-"}
              </p>
            </div>
            <div className="rounded-lg bg-muted p-4">
              <p className="text-sm text-muted-foreground">Total estimado</p>
              <p className="mt-1 text-xl font-bold">{marketPrice ? formatCurrency(estimatedTotal) : "-"}</p>
            </div>
          </div>

          <Button type="submit" className="w-full bg-success hover:bg-success/90" disabled={isSubmitting || !canSubmit}>
            {isSubmitting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Creando...
              </>
            ) : (
              `Crear orden ${normalizeSymbol(symbol) || "..."}`
            )}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}

// Form for publishing sell offers from existing portfolio positions.
function SellOfferForm({ initialSymbol }: { initialSymbol: string }) {
  const {
    data: portfolio,
    error: portfolioError,
    isLoading: portfolioLoading,
  } = useSWR<Portfolio>("portfolio", () => portfolioApi.getPortfolio(), {
    shouldRetryOnError: false,
  })
  const { data: orders, isLoading: ordersLoading } = useSWR<TradeOrder[]>("orders", () => tradingApi.getOrders())
  const [selectedSymbol, setSelectedSymbol] = useState("")
  const [quantity, setQuantity] = useState("")
  const [price, setPrice] = useState("")
  const [marketPrice, setMarketPrice] = useState<MarketPrice | null>(null)
  const [marketNotice, setMarketNotice] = useState("")
  const [isPriceLoading, setIsPriceLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  // Only positions with available quantity can be sold.
  const localPortfolio = buildPortfolioFromOrders(orders)
  const displayedPortfolio = portfolio || localPortfolio
  const usingLocalPortfolio = !portfolio && Boolean(localPortfolio)
  const positions = displayedPortfolio?.positions?.filter((position) => position.quantity > 0) || []
  const selectedPosition = positions.find((position) => position.symbol === selectedSymbol)
  const positionsLoading = !displayedPortfolio && (portfolioLoading || ordersLoading)
  const portfolioMarketUnavailable = Boolean(portfolioError) || (portfolioLoading && usingLocalPortfolio)

  useEffect(() => {
    // Preselects the symbol if navigation provides it through the query string.
    const nextSymbol = normalizeSymbol(initialSymbol)
    if (nextSymbol) setSelectedSymbol(nextSymbol)
  }, [initialSymbol])

  useEffect(() => {
    // When a position is selected, the market price is queried to limit the offer price.
    if (!selectedSymbol) {
      setMarketPrice(null)
      setMarketNotice("")
      return
    }

    let cancelled = false
    setIsPriceLoading(true)
    setMarketPrice(null)
    setMarketNotice("")
    marketApi
      .getPrice(selectedSymbol)
      .then((data) => {
        if (cancelled) return
        setMarketPrice(data)
        setMarketNotice("")
        setPrice((current) => current || data.price.toString())
      })
      .catch((error) => {
        if (!cancelled) {
          setMarketPrice(null)
          setMarketNotice(getErrorMessage(error, MARKET_DATA_UNAVAILABLE_MESSAGE))
        }
      })
      .finally(() => {
        if (!cancelled) setIsPriceLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [selectedSymbol])

  // Publishes a SELL offer in the local marketplace.
  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!selectedPosition) return

    const quantityValue = parsePositiveNumber(quantity)
    const priceValue = parsePositiveNumber(price)
    if (quantityValue <= 0 || priceValue <= 0) return

    setIsSubmitting(true)
    try {
      await tradingApi.placeSellOffer({
        symbol: selectedSymbol,
        quantity: quantityValue,
        price: priceValue,
      })
      setQuantity("")
      refreshTradingData()
      toast({
        title: "Oferta publicada",
        description: `${selectedSymbol} ya esta disponible en el marketplace`,
        variant: "success",
      })
    } catch (error) {
      const description = getErrorMessage(error, "El backend rechazo la venta")
      if (isMarketDataError(error)) setMarketNotice(description)
      toast({
        title: isMarketDataError(error) ? "Precio no disponible" : "No se pudo publicar la oferta",
        description,
        variant: isMarketDataError(error) ? "warning" : "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  // Derived validations: do not sell more shares or above the market price.
  const quantityValue = parsePositiveNumber(quantity)
  const priceValue = parsePositiveNumber(price)
  const maxPrice = marketPrice?.price
  const exceedsPosition = Boolean(selectedPosition && quantityValue > selectedPosition.quantity)
  const exceedsMarket = Boolean(maxPrice && priceValue > maxPrice)
  const canSubmit =
    Boolean(selectedPosition) &&
    Boolean(marketPrice) &&
    quantityValue > 0 &&
    priceValue > 0 &&
    !exceedsPosition &&
    !exceedsMarket

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Tags className="h-5 w-5 text-destructive" />
          Publicar venta
        </CardTitle>
        <CardDescription>Ofrece acciones de tu portfolio a otros usuarios</CardDescription>
      </CardHeader>
      <CardContent>
        {positionsLoading ? (
          <div className="flex h-64 items-center justify-center">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
          </div>
        ) : positions.length > 0 ? (
          <form onSubmit={handleSubmit} className="space-y-6">
            {portfolioMarketUnavailable && (
              <MarketDataNotice>
                Alpha Vantage no esta disponible. Se muestran tus posiciones guardadas, pero la publicacion de ventas
                necesita validar el precio real de mercado.
              </MarketDataNotice>
            )}
            {marketNotice && <MarketDataNotice>{marketNotice}</MarketDataNotice>}

            <div className="space-y-2">
              <Label htmlFor="sell-symbol">Posicion</Label>
              <Select
                value={selectedSymbol}
                onValueChange={(value) => {
                  setSelectedSymbol(value)
                  setQuantity("")
                  setPrice("")
                }}
              >
                <SelectTrigger id="sell-symbol">
                  <SelectValue placeholder="Selecciona una posicion" />
                </SelectTrigger>
                <SelectContent>
                  {positions.map((position) => (
                    <SelectItem key={position.symbol} value={position.symbol}>
                      {position.symbol} - {formatNumber(position.quantity)} acciones
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {selectedSymbol && !selectedPosition && (
                <p className="text-sm text-destructive">No tienes acciones disponibles de {selectedSymbol}</p>
              )}
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="sell-quantity">Cantidad</Label>
                <Input
                  id="sell-quantity"
                  type="number"
                  step="0.01"
                  min="0"
                  max={selectedPosition?.quantity}
                  placeholder="0.00"
                  value={quantity}
                  onChange={(event) => setQuantity(event.target.value)}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="sell-price">Precio unitario</Label>
                <Input
                  id="sell-price"
                  type="number"
                  step="0.01"
                  min="0"
                  max={maxPrice}
                  placeholder="0.00"
                  value={price}
                  onChange={(event) => setPrice(event.target.value)}
                  disabled={!selectedPosition || isPriceLoading || !marketPrice}
                  required
                />
              </div>
            </div>

            <div className="grid gap-3 sm:grid-cols-3">
              <div className="rounded-lg bg-muted p-4">
                <p className="text-sm text-muted-foreground">Disponibles</p>
                <p className="mt-1 text-lg font-bold">
                  {selectedPosition ? formatNumber(selectedPosition.quantity) : "-"}
                </p>
              </div>
              <div className="rounded-lg bg-muted p-4">
                <p className="text-sm text-muted-foreground">Maximo</p>
                <p className="mt-1 text-lg font-bold">
                  {isPriceLoading ? <Loader2 className="h-5 w-5 animate-spin" /> : maxPrice ? formatCurrency(maxPrice) : "-"}
                </p>
              </div>
              <div className="rounded-lg bg-muted p-4">
                <p className="text-sm text-muted-foreground">Total</p>
                <p className="mt-1 text-lg font-bold">{formatCurrency(quantityValue * priceValue)}</p>
              </div>
            </div>

            {exceedsPosition && (
              <p className="text-sm text-destructive">La cantidad supera tus acciones disponibles.</p>
            )}
            {exceedsMarket && (
              <p className="text-sm text-destructive">El precio no puede superar el precio actual de mercado.</p>
            )}
            {selectedPosition && !isPriceLoading && !marketPrice && (
              <p className="text-sm text-muted-foreground">
                No se puede publicar la venta hasta recuperar el precio real de mercado.
              </p>
            )}

            <Button
              type="submit"
              className="w-full bg-destructive hover:bg-destructive/90"
              disabled={isSubmitting || !canSubmit}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Publicando...
                </>
              ) : (
                `Publicar ${selectedSymbol || "..."}`
              )}
            </Button>
          </form>
        ) : (
          <div className="py-12 text-center text-muted-foreground">
            <Tags className="mx-auto mb-4 h-12 w-12 opacity-50" />
            <p>No tienes posiciones disponibles para vender</p>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

// Lists and buys sell offers published by other users.
function MarketplaceOffers({ initialSymbol, onOrderCreated }: { initialSymbol: string; onOrderCreated: () => void }) {
  const [query, setQuery] = useState("")
  const [filter, setFilter] = useState("")
  const [actionLoading, setActionLoading] = useState<number | null>(null)

  useEffect(() => {
    // If an initial symbol arrives, it is also used as the marketplace filter.
    const nextSymbol = normalizeSymbol(initialSymbol)
    if (!nextSymbol) return
    setQuery(nextSymbol)
    setFilter(nextSymbol)
  }, [initialSymbol])

  // The key includes the filter so SWR caches results by symbol.
  const { data: offers, isLoading } = useSWR<TradeOrder[]>(
    ["sell-offers", filter],
    () => tradingApi.getSellOffers(filter || undefined)
  )

  // Applies the filter typed by the user.
  const handleFilter = (event: React.FormEvent) => {
    event.preventDefault()
    setFilter(normalizeSymbol(query))
  }

  // Creates a pending BUY order at the selected offer price.
  const handleBuyOffer = async (offer: TradeOrder) => {
    setActionLoading(offer.id)
    try {
      await tradingApi.placeOrder({
        symbol: offer.symbol,
        side: "BUY",
        quantity: offer.quantity,
        price: offer.price,
      })
      refreshTradingData()
      toast({
        title: "Orden creada",
        description: `${offer.symbol} queda pendiente con fondos reservados. Ejecutala o cancelala desde Ordenes.`,
        variant: "success",
      })
      onOrderCreated()
    } catch (error) {
      toast({
        title: "No se pudo crear la orden",
        description: getErrorMessage(error, "El backend rechazo la orden"),
        variant: "destructive",
      })
    } finally {
      setActionLoading(null)
    }
  }

  const visibleOffers = offers || []

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Store className="h-5 w-5 text-primary" />
          Marketplace local
        </CardTitle>
        <CardDescription>Ofertas de venta publicadas por otros usuarios</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <form onSubmit={handleFilter} className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Filtrar por simbolo"
              value={query}
              onChange={(event) => setQuery(event.target.value.toUpperCase())}
              className="pl-10"
            />
          </div>
          <Button type="submit" variant="outline">Filtrar</Button>
          {filter && (
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                setQuery("")
                setFilter("")
              }}
            >
              Limpiar
            </Button>
          )}
        </form>

        {isLoading ? (
          <div className="flex h-64 items-center justify-center">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
          </div>
        ) : visibleOffers.length > 0 ? (
          <div className="space-y-4">
            {visibleOffers.map((offer) => (
              <div key={offer.id} className="rounded-lg border bg-card p-4">
                <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                  <div className="flex items-center gap-4">
                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/20 font-bold text-primary">
                      {offer.symbol.slice(0, 2)}
                    </div>
                    <div>
                      <p className="font-semibold">{offer.symbol}</p>
                      <p className="text-sm text-muted-foreground">
                        {formatNumber(offer.quantity)} acciones de {offer.owner}
                      </p>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4 md:min-w-72">
                    <div>
                      <p className="text-xs text-muted-foreground">Precio unitario</p>
                      <p className="font-bold">{formatCurrency(offer.price)}</p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Total</p>
                      <p className="font-bold">{formatCurrency(offer.total)}</p>
                    </div>
                  </div>

                  <Button
                    className="bg-success hover:bg-success/90 md:w-32"
                    onClick={() => handleBuyOffer(offer)}
                    disabled={actionLoading === offer.id}
                  >
                    {actionLoading === offer.id ? (
                      <Loader2 className="h-4 w-4 animate-spin" />
                    ) : (
                      <>
                        <ShoppingCart className="mr-2 h-4 w-4" />
                        Crear orden
                      </>
                    )}
                  </Button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="py-12 text-center text-muted-foreground">
            <Store className="mx-auto mb-4 h-12 w-12 opacity-50" />
            <p>No hay ofertas disponibles{filter ? ` para ${filter}` : ""}</p>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

// Shows open orders and history, with execute or cancel actions.
function OrdersList() {
  const { data: orders, isLoading } = useSWR<TradeOrder[]>("orders", () => tradingApi.getOrders())
  const [actionLoading, setActionLoading] = useState<number | null>(null)

  // Manually executes a pending buy order.
  const handleExecute = async (orderId: number) => {
    setActionLoading(orderId)
    try {
      await tradingApi.executeOrder(orderId)
      refreshTradingData()
      toast({
        title: "Orden ejecutada",
        description: `La orden #${orderId} se ejecuto correctamente`,
        variant: "success",
      })
    } catch (error) {
      toast({
        title: "No se pudo ejecutar la orden",
        description: getErrorMessage(error, "El backend rechazo la ejecucion"),
        variant: "destructive",
      })
    } finally {
      setActionLoading(null)
    }
  }

  // Cancels a pending order and releases reserved resources in the backend.
  const handleCancel = async (orderId: number) => {
    setActionLoading(orderId)
    try {
      await tradingApi.cancelOrder(orderId)
      refreshTradingData()
      toast({
        title: "Orden cancelada",
        description: `La orden #${orderId} se cancelo correctamente`,
        variant: "success",
      })
    } catch (error) {
      toast({
        title: "No se pudo cancelar la orden",
        description: getErrorMessage(error, "El backend rechazo la cancelacion"),
        variant: "destructive",
      })
    } finally {
      setActionLoading(null)
    }
  }

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-b-2 border-primary" />
      </div>
    )
  }

  // Pending orders and history are separated to show each group in its tab.
  const pendingOrders = orders?.filter((order) => order.status === "PENDING") || []
  const historyOrders = orders?.filter((order) => order.status !== "PENDING") || []

  return (
    <Tabs defaultValue="pending" className="space-y-4">
      <TabsList>
        <TabsTrigger value="pending">Abiertas ({pendingOrders.length})</TabsTrigger>
        <TabsTrigger value="history">Historial</TabsTrigger>
      </TabsList>

      <TabsContent value="pending">
        <Card>
          <CardContent className="pt-6">
            {pendingOrders.length > 0 ? (
              <div className="space-y-4">
                {pendingOrders.map((order) => (
                  <div key={order.id} className="flex flex-col gap-4 rounded-lg border bg-card p-4 md:flex-row md:items-center md:justify-between">
                    <div className="flex items-center gap-4">
                      <div
                        className={cn(
                          "flex h-10 w-10 items-center justify-center rounded-full font-bold",
                          order.side === "BUY" ? "bg-success/20 text-success" : "bg-destructive/20 text-destructive"
                        )}
                      >
                        {order.side === "BUY" ? "C" : "V"}
                      </div>
                      <div>
                        <p className="font-semibold">{order.symbol}</p>
                        <p className="text-sm text-muted-foreground">
                          {formatNumber(order.quantity)} acciones @ {formatCurrency(order.price)}
                        </p>
                        {order.side === "SELL" && (
                          <p className="text-xs text-muted-foreground">Oferta publicada en marketplace</p>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center justify-between gap-4 md:justify-end">
                      <div className="text-right">
                        <p className="font-bold">{formatCurrency(order.total)}</p>
                        <p className="text-xs text-muted-foreground">{formatDate(order.createdAt)}</p>
                      </div>
                      <div className="flex gap-2">
                        {order.side === "BUY" && (
                          <Button
                            size="sm"
                            variant="outline"
                            className="border-success text-success hover:bg-success hover:text-success-foreground"
                            onClick={() => handleExecute(order.id)}
                            disabled={actionLoading === order.id}
                          >
                            {actionLoading === order.id ? (
                              <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                              <Play className="h-4 w-4" />
                            )}
                          </Button>
                        )}
                        <Button
                          size="sm"
                          variant="outline"
                          className="border-destructive text-destructive hover:bg-destructive hover:text-destructive-foreground"
                          onClick={() => handleCancel(order.id)}
                          disabled={actionLoading === order.id}
                        >
                          {actionLoading === order.id ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                          ) : (
                            <X className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-12 text-center text-muted-foreground">
                <LineChart className="mx-auto mb-4 h-12 w-12 opacity-50" />
                <p>No tienes ordenes abiertas</p>
              </div>
            )}
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="history">
        <Card>
          <CardContent className="pt-6">
            {historyOrders.length > 0 ? (
              <div className="space-y-4">
                {historyOrders.map((order) => (
                  <div key={order.id} className="flex items-center justify-between rounded-lg border bg-card p-4">
                    <div className="flex items-center gap-4">
                      <div
                        className={cn(
                          "flex h-10 w-10 items-center justify-center rounded-full font-bold",
                          order.side === "BUY" ? "bg-success/20 text-success" : "bg-destructive/20 text-destructive"
                        )}
                      >
                        {order.side === "BUY" ? "C" : "V"}
                      </div>
                      <div>
                        <p className="font-semibold">{order.symbol}</p>
                        <p className="text-sm text-muted-foreground">
                          {formatNumber(order.quantity)} acciones @ {formatCurrency(order.price)}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="font-bold">{formatCurrency(order.total)}</p>
                      <p
                        className={cn(
                          "text-xs font-medium",
                          order.status === "EXECUTED" && "text-success",
                          order.status === "CANCELLED" && "text-muted-foreground"
                        )}
                      >
                        {order.status === "EXECUTED" && "Ejecutada"}
                        {order.status === "CANCELLED" && "Cancelada"}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-12 text-center text-muted-foreground">
                <LineChart className="mx-auto mb-4 h-12 w-12 opacity-50" />
                <p>No hay historial de ordenes</p>
              </div>
            )}
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>
  )
}

export default function TradingPage() {
  const [activeTab, setActiveTab] = useState<TradingTab>("buy")
  const [initialSymbol, setInitialSymbol] = useState("")

  useEffect(() => {
    // Reads query params generated from Market or the home page to open the correct tab.
    const params = new URLSearchParams(window.location.search)
    const symbol = normalizeSymbol(params.get("symbol") || "")
    const side = params.get("side")
    const tab = params.get("tab")

    if (symbol) setInitialSymbol(symbol)
    if (side === "SELL") setActiveTab("sell")
    else if (side === "BUY") setActiveTab("buy")
    else if (tab === "marketplace" || tab === "orders" || tab === "sell" || tab === "buy") setActiveTab(tab)
  }, [])

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Trading</h1>
        <p className="text-muted-foreground">Compra acciones, publica ofertas y opera con otros usuarios</p>
      </div>

      <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as TradingTab)} className="space-y-6">
        <TabsList className="grid w-full grid-cols-2 lg:w-auto lg:grid-cols-4">
          <TabsTrigger value="buy">Comprar</TabsTrigger>
          <TabsTrigger value="sell">Vender</TabsTrigger>
          <TabsTrigger value="marketplace">Marketplace</TabsTrigger>
          <TabsTrigger value="orders">Ordenes</TabsTrigger>
        </TabsList>

        <TabsContent value="buy">
          <div className="max-w-2xl">
            <BuyOrderForm initialSymbol={initialSymbol} onOrderCreated={() => setActiveTab("orders")} />
          </div>
        </TabsContent>

        <TabsContent value="sell">
          <div className="max-w-2xl">
            <SellOfferForm initialSymbol={initialSymbol} />
          </div>
        </TabsContent>

        <TabsContent value="marketplace">
          <MarketplaceOffers initialSymbol={initialSymbol} onOrderCreated={() => setActiveTab("orders")} />
        </TabsContent>

        <TabsContent value="orders">
          <OrdersList />
        </TabsContent>
      </Tabs>
    </div>
  )
}

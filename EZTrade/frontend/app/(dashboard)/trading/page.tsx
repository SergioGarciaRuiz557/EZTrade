"use client"

import { useEffect, useState } from "react"
import useSWR, { mutate } from "swr"
import {
  marketApi,
  portfolioApi,
  tradingApi,
  MarketPrice,
  Portfolio,
  TradeOrder,
} from "@/lib/api"
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
import { DollarSign, LineChart, Loader2, Play, Search, ShoppingCart, Store, Tags, X } from "lucide-react"

type TradingTab = "buy" | "sell" | "marketplace" | "orders"

// Normaliza simbolos para que busquedas y ordenes usen el formato esperado por el backend.
function normalizeSymbol(value: string) {
  return value.trim().toUpperCase()
}

// Devuelve 0 si el input no es un numero positivo, facilitando validaciones de formularios.
function parsePositiveNumber(value: string) {
  const parsed = Number.parseFloat(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : 0
}

// Extrae mensajes de errores lanzados por la capa API y usa un texto seguro si no hay detalle.
function getErrorMessage(error: unknown, fallback: string) {
  if (error && typeof error === "object" && "message" in error) {
    const message = (error as { message?: unknown }).message
    if (typeof message === "string" && message.trim()) return message
  }
  return fallback
}

// Refresca caches relacionadas tras ejecutar acciones de trading.
function refreshTradingData() {
  mutate("orders")
  mutate("portfolio")
  mutate("wallet")
}

// Formulario para crear ordenes de compra usando el precio actual del mercado.
function BuyOrderForm({ initialSymbol }: { initialSymbol: string }) {
  const [symbol, setSymbol] = useState("")
  const [quantity, setQuantity] = useState("")
  const [marketPrice, setMarketPrice] = useState<MarketPrice | null>(null)
  const [isPriceLoading, setIsPriceLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    // Si se llega desde Mercado o la portada, precarga simbolo y precio automaticamente.
    const nextSymbol = normalizeSymbol(initialSymbol)
    if (!nextSymbol) return

    setSymbol(nextSymbol)
    setMarketPrice(null)

    let cancelled = false
    setIsPriceLoading(true)
    marketApi
      .getPrice(nextSymbol)
      .then((data) => {
        if (!cancelled) setMarketPrice(data)
      })
      .catch(() => {
        if (!cancelled) setMarketPrice(null)
      })
      .finally(() => {
        if (!cancelled) setIsPriceLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [initialSymbol])

  // Consulta manual del precio para el simbolo escrito.
  const handleLookup = async () => {
    const nextSymbol = normalizeSymbol(symbol)
    if (!nextSymbol) return

    setSymbol(nextSymbol)
    setIsPriceLoading(true)
    try {
      const price = await marketApi.getPrice(nextSymbol)
      setMarketPrice(price)
    } catch (error) {
      setMarketPrice(null)
      toast({
        title: "Precio no disponible",
        description: getErrorMessage(error, "No se pudo consultar el precio actual"),
        variant: "destructive",
      })
    } finally {
      setIsPriceLoading(false)
    }
  }

  // Crea la orden BUY tras validar simbolo y cantidad.
  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    const nextSymbol = normalizeSymbol(symbol)
    const nextQuantity = parsePositiveNumber(quantity)
    if (!nextSymbol || nextQuantity <= 0) return

    setIsSubmitting(true)
    try {
      const price = marketPrice ?? await marketApi.getPrice(nextSymbol)
      setMarketPrice(price)

      await tradingApi.placeOrder({
        symbol: nextSymbol,
        side: "BUY",
        quantity: nextQuantity,
        price: price.price,
      })
      setQuantity("")
      mutate("orders")
    } catch {
      // Las notificaciones de ordenes las emite el backend por WebSocket.
    } finally {
      setIsSubmitting(false)
    }
  }

  // Valores derivados que alimentan resumen visual y estado del boton.
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
        <CardDescription>Crea una orden pendiente con el precio actual de mercado</CardDescription>
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
                }}
                required
              />
              <Button type="button" variant="outline" onClick={handleLookup} disabled={!symbol.trim() || isPriceLoading}>
                {isPriceLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
              </Button>
            </div>
          </div>

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
                Procesando...
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

// Formulario para publicar ofertas de venta desde posiciones existentes del portfolio.
function SellOfferForm({ initialSymbol }: { initialSymbol: string }) {
  const { data: portfolio, isLoading } = useSWR<Portfolio>("portfolio", () => portfolioApi.getPortfolio())
  const [selectedSymbol, setSelectedSymbol] = useState("")
  const [quantity, setQuantity] = useState("")
  const [price, setPrice] = useState("")
  const [marketPrice, setMarketPrice] = useState<MarketPrice | null>(null)
  const [isPriceLoading, setIsPriceLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  // Solo se pueden vender posiciones con cantidad disponible.
  const positions = portfolio?.positions?.filter((position) => position.quantity > 0) || []
  const selectedPosition = positions.find((position) => position.symbol === selectedSymbol)

  useEffect(() => {
    // Preselecciona el simbolo si la navegacion lo trae por query string.
    const nextSymbol = normalizeSymbol(initialSymbol)
    if (nextSymbol) setSelectedSymbol(nextSymbol)
  }, [initialSymbol])

  useEffect(() => {
    // Al elegir posicion se consulta el precio de mercado para limitar el precio de la oferta.
    if (!selectedSymbol) {
      setMarketPrice(null)
      return
    }

    let cancelled = false
    setIsPriceLoading(true)
    setMarketPrice(null)
    marketApi
      .getPrice(selectedSymbol)
      .then((data) => {
        if (cancelled) return
        setMarketPrice(data)
        setPrice((current) => current || data.price.toString())
      })
      .catch(() => {
        if (!cancelled) setMarketPrice(null)
      })
      .finally(() => {
        if (!cancelled) setIsPriceLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [selectedSymbol])

  // Publica una oferta SELL en el marketplace local.
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
    } catch {
      // Las notificaciones de ordenes las emite el backend por WebSocket.
    } finally {
      setIsSubmitting(false)
    }
  }

  // Validaciones derivadas: no vender mas acciones ni por encima del precio de mercado.
  const quantityValue = parsePositiveNumber(quantity)
  const priceValue = parsePositiveNumber(price)
  const maxPrice = marketPrice?.price
  const exceedsPosition = Boolean(selectedPosition && quantityValue > selectedPosition.quantity)
  const exceedsMarket = Boolean(maxPrice && priceValue > maxPrice)
  const canSubmit = Boolean(selectedPosition) && quantityValue > 0 && priceValue > 0 && !exceedsPosition && !exceedsMarket

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
        {isLoading ? (
          <div className="flex h-64 items-center justify-center">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
          </div>
        ) : positions.length > 0 ? (
          <form onSubmit={handleSubmit} className="space-y-6">
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

// Lista y compra ofertas de venta publicadas por otros usuarios.
function MarketplaceOffers({ initialSymbol }: { initialSymbol: string }) {
  const [query, setQuery] = useState("")
  const [filter, setFilter] = useState("")
  const [actionLoading, setActionLoading] = useState<number | null>(null)

  useEffect(() => {
    // Si llega un simbolo inicial, se usa tambien como filtro del marketplace.
    const nextSymbol = normalizeSymbol(initialSymbol)
    if (!nextSymbol) return
    setQuery(nextSymbol)
    setFilter(nextSymbol)
  }, [initialSymbol])

  // La key incluye el filtro para que SWR cachee resultados por simbolo.
  const { data: offers, isLoading, mutate: refreshOffers } = useSWR<TradeOrder[]>(
    ["sell-offers", filter],
    () => tradingApi.getSellOffers(filter || undefined)
  )

  // Aplica el filtro escrito por el usuario.
  const handleFilter = (event: React.FormEvent) => {
    event.preventDefault()
    setFilter(normalizeSymbol(query))
  }

  // Compra una oferta concreta y refresca wallet, portfolio, ordenes y marketplace.
  const handleBuyOffer = async (offer: TradeOrder) => {
    setActionLoading(offer.id)
    try {
      await tradingApi.buySellOffer(offer.id)
      refreshOffers()
      refreshTradingData()
    } catch {
      // Las notificaciones de ordenes las emite el backend por WebSocket.
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
                        Comprar
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

// Muestra ordenes abiertas e historial, con acciones de ejecutar o cancelar.
function OrdersList() {
  const { data: orders, isLoading } = useSWR<TradeOrder[]>("orders", () => tradingApi.getOrders())
  const [actionLoading, setActionLoading] = useState<number | null>(null)

  // Ejecuta manualmente una orden de compra pendiente.
  const handleExecute = async (orderId: number) => {
    setActionLoading(orderId)
    try {
      await tradingApi.executeOrder(orderId)
      refreshTradingData()
    } catch {
      // Las notificaciones de ordenes las emite el backend por WebSocket.
    } finally {
      setActionLoading(null)
    }
  }

  // Cancela una orden pendiente y libera recursos reservados en backend.
  const handleCancel = async (orderId: number) => {
    setActionLoading(orderId)
    try {
      await tradingApi.cancelOrder(orderId)
      refreshTradingData()
    } catch {
      // Las notificaciones de ordenes las emite el backend por WebSocket.
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

  // Se separan pendientes e historial para mostrar cada grupo en su tab.
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
    // Lee query params generados desde Mercado o la portada para abrir la pestana correcta.
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
            <BuyOrderForm initialSymbol={initialSymbol} />
          </div>
        </TabsContent>

        <TabsContent value="sell">
          <div className="max-w-2xl">
            <SellOfferForm initialSymbol={initialSymbol} />
          </div>
        </TabsContent>

        <TabsContent value="marketplace">
          <MarketplaceOffers initialSymbol={initialSymbol} />
        </TabsContent>

        <TabsContent value="orders">
          <OrdersList />
        </TabsContent>
      </Tabs>
    </div>
  )
}

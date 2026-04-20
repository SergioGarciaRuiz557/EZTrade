"use client"

import { useState } from "react"
import useSWR, { mutate } from "swr"
import { tradingApi, TradeOrder, PlaceOrderRequest } from "@/lib/api"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { toast } from "@/components/ui/toaster"
import { formatCurrency, formatNumber, formatDate, cn } from "@/lib/utils"
import { LineChart, Loader2, X, Play } from "lucide-react"

function OrderForm() {
  const [side, setSide] = useState<"BUY" | "SELL">("BUY")
  const [symbol, setSymbol] = useState("")
  const [quantity, setQuantity] = useState("")
  const [price, setPrice] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)

    try {
      const order: PlaceOrderRequest = {
        symbol: symbol.toUpperCase(),
        side,
        quantity: parseFloat(quantity),
        price: parseFloat(price),
      }
      await tradingApi.placeOrder(order)
      toast({
        title: "Orden creada",
        description: `Orden de ${side === "BUY" ? "compra" : "venta"} de ${symbol.toUpperCase()} creada correctamente`,
      })
      setSymbol("")
      setQuantity("")
      setPrice("")
      mutate("orders")
    } catch {
      toast({ title: "Error", description: "No se pudo crear la orden", variant: "destructive" })
    } finally {
      setIsSubmitting(false)
    }
  }

  const total = (parseFloat(quantity) || 0) * (parseFloat(price) || 0)

  return (
    <Card>
      <CardHeader>
        <CardTitle>Nueva Orden</CardTitle>
        <CardDescription>Crea una orden de compra o venta</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Buy/Sell Toggle */}
          <div className="grid grid-cols-2 gap-2 p-1 bg-muted rounded-lg">
            <Button
              type="button"
              variant={side === "BUY" ? "default" : "ghost"}
              className={cn(side === "BUY" && "bg-success hover:bg-success/90")}
              onClick={() => setSide("BUY")}
            >
              Comprar
            </Button>
            <Button
              type="button"
              variant={side === "SELL" ? "default" : "ghost"}
              className={cn(side === "SELL" && "bg-destructive hover:bg-destructive/90")}
              onClick={() => setSide("SELL")}
            >
              Vender
            </Button>
          </div>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="symbol">Simbolo</Label>
              <Input
                id="symbol"
                placeholder="AAPL, GOOGL, MSFT..."
                value={symbol}
                onChange={(e) => setSymbol(e.target.value)}
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="quantity">Cantidad</Label>
                <Input
                  id="quantity"
                  type="number"
                  step="0.01"
                  min="0"
                  placeholder="0.00"
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="price">Precio</Label>
                <Input
                  id="price"
                  type="number"
                  step="0.01"
                  min="0"
                  placeholder="0.00"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  required
                />
              </div>
            </div>

            {/* Total */}
            <div className="p-4 bg-muted rounded-lg">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Total estimado</span>
                <span className="font-bold text-lg">{formatCurrency(total)}</span>
              </div>
            </div>
          </div>

          <Button
            type="submit"
            className={cn("w-full", side === "BUY" ? "bg-success hover:bg-success/90" : "bg-destructive hover:bg-destructive/90")}
            disabled={isSubmitting || !symbol || !quantity || !price}
          >
            {isSubmitting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Procesando...
              </>
            ) : (
              `${side === "BUY" ? "Comprar" : "Vender"} ${symbol.toUpperCase() || "..."}`
            )}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}

function OrdersList() {
  const { data: orders, isLoading } = useSWR<TradeOrder[]>("orders", () => tradingApi.getOrders())
  const [actionLoading, setActionLoading] = useState<number | null>(null)

  const handleExecute = async (orderId: number) => {
    setActionLoading(orderId)
    try {
      await tradingApi.executeOrder(orderId)
      toast({ title: "Orden ejecutada", description: "La orden se ha ejecutado correctamente" })
      mutate("orders")
      mutate("portfolio")
      mutate("wallet")
    } catch {
      toast({ title: "Error", description: "No se pudo ejecutar la orden", variant: "destructive" })
    } finally {
      setActionLoading(null)
    }
  }

  const handleCancel = async (orderId: number) => {
    setActionLoading(orderId)
    try {
      await tradingApi.cancelOrder(orderId)
      toast({ title: "Orden cancelada", description: "La orden ha sido cancelada" })
      mutate("orders")
      mutate("wallet")
    } catch {
      toast({ title: "Error", description: "No se pudo cancelar la orden", variant: "destructive" })
    } finally {
      setActionLoading(null)
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  const pendingOrders = orders?.filter((o) => o.status === "PENDING") || []
  const historyOrders = orders?.filter((o) => o.status !== "PENDING") || []

  return (
    <Tabs defaultValue="pending" className="space-y-4">
      <TabsList>
        <TabsTrigger value="pending">
          Pendientes ({pendingOrders.length})
        </TabsTrigger>
        <TabsTrigger value="history">Historial</TabsTrigger>
      </TabsList>

      <TabsContent value="pending">
        <Card>
          <CardContent className="pt-6">
            {pendingOrders.length > 0 ? (
              <div className="space-y-4">
                {pendingOrders.map((order) => (
                  <div
                    key={order.id}
                    className="flex items-center justify-between p-4 rounded-lg border bg-card"
                  >
                    <div className="flex items-center gap-4">
                      <div
                        className={cn(
                          "h-10 w-10 rounded-full flex items-center justify-center font-bold",
                          order.side === "BUY" ? "bg-success/20 text-success" : "bg-destructive/20 text-destructive"
                        )}
                      >
                        {order.side === "BUY" ? "C" : "V"}
                      </div>
                      <div>
                        <p className="font-semibold">{order.symbol}</p>
                        <p className="text-sm text-muted-foreground">
                          {formatNumber(order.quantity)} unidades @ {formatCurrency(order.price)}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p className="font-bold">{formatCurrency(order.total)}</p>
                        <p className="text-xs text-muted-foreground">{formatDate(order.createdAt)}</p>
                      </div>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          className="text-success border-success hover:bg-success hover:text-success-foreground"
                          onClick={() => handleExecute(order.id)}
                          disabled={actionLoading === order.id}
                        >
                          {actionLoading === order.id ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                          ) : (
                            <Play className="h-4 w-4" />
                          )}
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          className="text-destructive border-destructive hover:bg-destructive hover:text-destructive-foreground"
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
              <div className="text-center py-12 text-muted-foreground">
                <LineChart className="h-12 w-12 mx-auto mb-4 opacity-50" />
                <p>No tienes ordenes pendientes</p>
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
                  <div
                    key={order.id}
                    className="flex items-center justify-between p-4 rounded-lg border bg-card"
                  >
                    <div className="flex items-center gap-4">
                      <div
                        className={cn(
                          "h-10 w-10 rounded-full flex items-center justify-center font-bold",
                          order.side === "BUY" ? "bg-success/20 text-success" : "bg-destructive/20 text-destructive"
                        )}
                      >
                        {order.side === "BUY" ? "C" : "V"}
                      </div>
                      <div>
                        <p className="font-semibold">{order.symbol}</p>
                        <p className="text-sm text-muted-foreground">
                          {formatNumber(order.quantity)} unidades @ {formatCurrency(order.price)}
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
              <div className="text-center py-12 text-muted-foreground">
                <LineChart className="h-12 w-12 mx-auto mb-4 opacity-50" />
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
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Trading</h1>
        <p className="text-muted-foreground">Crea y gestiona tus ordenes de compra y venta</p>
      </div>

      <div className="grid gap-8 lg:grid-cols-3">
        <div className="lg:col-span-1">
          <OrderForm />
        </div>
        <div className="lg:col-span-2">
          <OrdersList />
        </div>
      </div>
    </div>
  )
}

"use client"

import useSWR from "swr"
import { portfolioApi } from "@/features/portfolio/api"
import { tradingApi } from "@/features/trading/api"
import { walletApi } from "@/features/wallet/api"
import type { Portfolio, Position } from "@/features/portfolio/types"
import type { TradeOrder } from "@/features/trading/types"
import type { WalletBalance } from "@/features/wallet/types"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { formatCurrency, formatNumber, formatDate } from "@/lib/utils"
import { TrendingUp, TrendingDown, Wallet, Briefcase, Activity, Clock, AlertTriangle, Loader2 } from "lucide-react"
import { cn } from "@/lib/utils"

interface PositionAccumulator {
  symbol: string
  quantity: number
  averageCost: number
  realizedPnl: number
  updatedAt: string
}

function toFiniteNumber(value: number | null | undefined) {
  return typeof value === "number" && Number.isFinite(value) ? value : 0
}

function getOrderTimestamp(order: TradeOrder) {
  const timestamp = new Date(order.executedAt || order.createdAt).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

function buildPortfolioFromOrders(orders: TradeOrder[] | undefined, owner?: string): Portfolio | null {
  if (!orders) return null

  const positionsBySymbol = new Map<string, PositionAccumulator>()
  const executedOrders = orders
    .filter((order) => order.status === "EXECUTED")
    .slice()
    .sort((a, b) => getOrderTimestamp(a) - getOrderTimestamp(b))

  executedOrders.forEach((order) => {
    const symbol = order.symbol.trim().toUpperCase()
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
  const positions: Position[] = positionStates
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
    owner: owner || orders[0]?.owner || "",
    cashAvailable: 0,
    totalCostBasis: positions.reduce((total, position) => total + position.quantity * position.averageCost, 0),
    totalRealizedPnl: positionStates.reduce((total, position) => total + position.realizedPnl, 0),
    positions,
  }
}

function SectionLoader() {
  return (
    <div className="flex h-40 items-center justify-center">
      <Loader2 className="h-7 w-7 animate-spin text-primary" />
    </div>
  )
}

function StatCard({
  title,
  value,
  description,
  icon: Icon,
  trend,
}: {
  title: string
  value: string
  description?: string
  icon: React.ElementType
  trend?: "up" | "down" | "neutral"
}) {
  // Tarjeta reutilizable para metricas principales del dashboard.
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
        <Icon className={cn("h-4 w-4", trend === "up" && "text-success", trend === "down" && "text-destructive")} />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {description && <p className="text-xs text-muted-foreground mt-1">{description}</p>}
      </CardContent>
    </Card>
  )
}

export default function DashboardPage() {
  // SWR mantiene cache local y refresco automatico, pero cada fuente se pinta de forma independiente.
  const {
    data: portfolio,
    error: portfolioError,
    isLoading: portfolioLoading,
  } = useSWR<Portfolio>("portfolio", () => portfolioApi.getPortfolio(), {
    shouldRetryOnError: false,
  })
  const { data: wallet, isLoading: walletLoading } = useSWR<WalletBalance>("wallet", () => walletApi.getBalance())
  const { data: orders, isLoading: ordersLoading } = useSWR<TradeOrder[]>("orders", () =>
    tradingApi.getOrders()
  )

  // Calcula metricas derivadas combinando wallet, portfolio y ordenes.
  const localPortfolio = buildPortfolioFromOrders(orders, wallet?.owner || portfolio?.owner)
  const displayedPortfolio = portfolio || localPortfolio
  const usingLocalPortfolio = !portfolio && Boolean(localPortfolio)
  const portfolioMarketUnavailable = Boolean(portfolioError) || (portfolioLoading && usingLocalPortfolio)
  const positions = displayedPortfolio?.positions || []
  const totalBalance = wallet
    ? toFiniteNumber(wallet.availableBalance) + toFiniteNumber(wallet.reservedBalance)
    : toFiniteNumber(portfolio?.cashAvailable)
  const totalCostBasis = toFiniteNumber(displayedPortfolio?.totalCostBasis)
  const totalRealizedPnl = toFiniteNumber(displayedPortfolio?.totalRealizedPnl)
  const portfolioValue = totalCostBasis + totalBalance
  const pendingOrders = orders?.filter((o) => o.status === "PENDING") || []
  const recentOrders = orders?.slice(0, 5) || []
  const positionsLoading = !displayedPortfolio && (portfolioLoading || ordersLoading)
  const portfolioValueReady = Boolean(displayedPortfolio) && (Boolean(wallet) || Boolean(portfolio))

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">Resumen de tu cuenta de trading</p>
      </div>

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Valor del Portfolio"
          value={portfolioValueReady ? formatCurrency(portfolioValue) : "-"}
          description={
            displayedPortfolio
              ? `${positions.length} posiciones activas${usingLocalPortfolio ? " (datos locales)" : ""}`
              : "Cargando posiciones"
          }
          icon={Briefcase}
          trend="neutral"
        />
        <StatCard
          title="Efectivo Disponible"
          value={
            wallet
              ? formatCurrency(wallet.availableBalance)
              : portfolio
                ? formatCurrency(portfolio.cashAvailable)
                : walletLoading
                  ? "-"
                  : formatCurrency(0)
          }
          description={wallet?.reservedBalance ? `${formatCurrency(wallet.reservedBalance)} reservados` : undefined}
          icon={Wallet}
          trend="neutral"
        />
        <StatCard
          title="P&L Realizado"
          value={displayedPortfolio ? formatCurrency(totalRealizedPnl) : "-"}
          description={usingLocalPortfolio ? "Calculado desde ordenes ejecutadas" : undefined}
          icon={totalRealizedPnl >= 0 ? TrendingUp : TrendingDown}
          trend={displayedPortfolio ? (totalRealizedPnl >= 0 ? "up" : "down") : "neutral"}
        />
        <StatCard
          title="Ordenes Abiertas"
          value={orders ? pendingOrders.length.toString() : ordersLoading ? "-" : "0"}
          description="Ordenes y ofertas activas"
          icon={Clock}
          trend="neutral"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Positions */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5" />
              Posiciones
            </CardTitle>
            <CardDescription>Tus posiciones actuales en el mercado</CardDescription>
          </CardHeader>
          <CardContent>
            {portfolioMarketUnavailable && (
              <div className="mb-4 flex items-start gap-3 rounded-md border border-warning/40 bg-warning/10 p-3 text-sm text-muted-foreground">
                <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-warning" />
                <p>
                  {portfolioError
                    ? "Alpha Vantage no esta disponible. Se muestran datos guardados; el P&L no realizado queda oculto hasta que vuelva el precio de mercado."
                    : "Actualizando datos de mercado. Mientras tanto se muestran datos guardados y el P&L no realizado queda oculto."}
                </p>
              </div>
            )}
            {positionsLoading ? (
              <SectionLoader />
            ) : positions.length > 0 ? (
              <div className="space-y-4">
                {positions.map((position) => {
                  const quantity = toFiniteNumber(position.quantity)
                  const averageCost = toFiniteNumber(position.averageCost)
                  const realizedPnl = toFiniteNumber(position.realizedPnl)

                  return (
                    <div key={position.symbol} className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
                      <div>
                        <p className="font-semibold">{position.symbol}</p>
                        <p className="text-sm text-muted-foreground">
                          {formatNumber(quantity)} unidades @ {formatCurrency(averageCost)}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="font-medium">
                          {formatCurrency(quantity * averageCost)}
                        </p>
                        <p className="text-xs text-muted-foreground">P&L realizado</p>
                        <p
                          className={cn(
                            "text-sm",
                            realizedPnl >= 0 ? "text-success" : "text-destructive"
                          )}
                        >
                          {realizedPnl >= 0 ? "+" : ""}
                          {formatCurrency(realizedPnl)}
                        </p>
                      </div>
                    </div>
                  )
                })}
              </div>
            ) : (
              <div className="text-center py-8 text-muted-foreground">
                <Activity className="h-12 w-12 mx-auto mb-4 opacity-50" />
                <p>No tienes posiciones abiertas</p>
                <p className="text-sm">Empieza a operar para ver tus posiciones aqui</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Recent Orders */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              Ordenes Recientes
            </CardTitle>
            <CardDescription>Ultimas ordenes realizadas</CardDescription>
          </CardHeader>
          <CardContent>
            {ordersLoading && !orders ? (
              <SectionLoader />
            ) : recentOrders.length > 0 ? (
              <div className="space-y-4">
                {recentOrders.map((order) => (
                  <div key={order.id} className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
                    <div className="flex items-center gap-3">
                      <div
                        className={cn(
                          "h-8 w-8 rounded-full flex items-center justify-center text-xs font-bold",
                          order.side === "BUY" ? "bg-success/20 text-success" : "bg-destructive/20 text-destructive"
                        )}
                      >
                        {order.side === "BUY" ? "C" : "V"}
                      </div>
                      <div>
                        <p className="font-semibold">{order.symbol}</p>
                        <p className="text-sm text-muted-foreground">
                          {formatNumber(order.quantity)} @ {formatCurrency(order.price)}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p
                        className={cn(
                          "text-xs font-medium px-2 py-1 rounded",
                          order.status === "EXECUTED" && "bg-success/20 text-success",
                          order.status === "PENDING" && "bg-primary/20 text-primary",
                          order.status === "CANCELLED" && "bg-muted text-muted-foreground"
                        )}
                      >
                        {order.status === "EXECUTED" && "Ejecutada"}
                        {order.status === "PENDING" && "Pendiente"}
                        {order.status === "CANCELLED" && "Cancelada"}
                      </p>
                      <p className="text-xs text-muted-foreground mt-1">{formatDate(order.createdAt)}</p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-muted-foreground">
                <Clock className="h-12 w-12 mx-auto mb-4 opacity-50" />
                <p>No hay ordenes recientes</p>
                <p className="text-sm">Tus ordenes apareceran aqui</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

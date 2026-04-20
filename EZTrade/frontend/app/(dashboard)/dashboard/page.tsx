"use client"

import useSWR from "swr"
import { portfolioApi, walletApi, tradingApi, Portfolio, WalletBalance, TradeOrder } from "@/lib/api"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { formatCurrency, formatNumber, formatDate } from "@/lib/utils"
import { TrendingUp, TrendingDown, Wallet, Briefcase, Activity, Clock } from "lucide-react"
import { cn } from "@/lib/utils"

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
  const { data: portfolio, isLoading: portfolioLoading } = useSWR<Portfolio>("portfolio", () =>
    portfolioApi.getPortfolio()
  )
  const { data: wallet, isLoading: walletLoading } = useSWR<WalletBalance>("wallet", () => walletApi.getBalance())
  const { data: orders, isLoading: ordersLoading } = useSWR<TradeOrder[]>("orders", () => tradingApi.getOrders())

  const isLoading = portfolioLoading || walletLoading || ordersLoading

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  const totalBalance = (wallet?.availableBalance || 0) + (wallet?.reservedBalance || 0)
  const portfolioValue = (portfolio?.totalCostBasis || 0) + totalBalance
  const pendingOrders = orders?.filter((o) => o.status === "PENDING") || []
  const recentOrders = orders?.slice(0, 5) || []

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
          value={formatCurrency(portfolioValue)}
          description={`${portfolio?.positions?.length || 0} posiciones activas`}
          icon={Briefcase}
          trend="neutral"
        />
        <StatCard
          title="Efectivo Disponible"
          value={formatCurrency(wallet?.availableBalance || 0)}
          description={wallet?.reservedBalance ? `${formatCurrency(wallet.reservedBalance)} reservados` : undefined}
          icon={Wallet}
          trend="neutral"
        />
        <StatCard
          title="P&L Realizado"
          value={formatCurrency(portfolio?.totalRealizedPnl || 0)}
          icon={portfolio?.totalRealizedPnl && portfolio.totalRealizedPnl >= 0 ? TrendingUp : TrendingDown}
          trend={
            portfolio?.totalRealizedPnl ? (portfolio.totalRealizedPnl >= 0 ? "up" : "down") : "neutral"
          }
        />
        <StatCard
          title="Ordenes Pendientes"
          value={pendingOrders.length.toString()}
          description="En espera de ejecucion"
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
            {portfolio?.positions && portfolio.positions.length > 0 ? (
              <div className="space-y-4">
                {portfolio.positions.map((position) => (
                  <div key={position.symbol} className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
                    <div>
                      <p className="font-semibold">{position.symbol}</p>
                      <p className="text-sm text-muted-foreground">
                        {formatNumber(position.quantity)} unidades @ {formatCurrency(position.averageCost)}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="font-medium">
                        {formatCurrency(position.quantity * position.averageCost)}
                      </p>
                      <p
                        className={cn(
                          "text-sm",
                          position.realizedPnl >= 0 ? "text-success" : "text-destructive"
                        )}
                      >
                        {position.realizedPnl >= 0 ? "+" : ""}
                        {formatCurrency(position.realizedPnl)}
                      </p>
                    </div>
                  </div>
                ))}
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
            {recentOrders.length > 0 ? (
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

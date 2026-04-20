"use client"

import { useState } from "react"
import useSWR, { mutate } from "swr"
import { walletApi, WalletBalance } from "@/lib/api"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { toast } from "@/components/ui/toaster"
import { formatCurrency } from "@/lib/utils"
import { Wallet, Plus, Lock, Loader2, TrendingUp, PiggyBank } from "lucide-react"

function BalanceCard({ title, amount, icon: Icon, description, variant = "default" }: {
  title: string
  amount: number
  icon: React.ElementType
  description?: string
  variant?: "default" | "primary" | "muted"
}) {
  return (
    <Card className={variant === "primary" ? "border-primary" : ""}>
      <CardContent className="pt-6">
        <div className="flex items-center gap-4">
          <div className={`h-12 w-12 rounded-full flex items-center justify-center ${
            variant === "primary" ? "bg-primary/20 text-primary" : 
            variant === "muted" ? "bg-muted text-muted-foreground" : "bg-secondary text-secondary-foreground"
          }`}>
            <Icon className="h-6 w-6" />
          </div>
          <div>
            <p className="text-sm text-muted-foreground">{title}</p>
            <p className="text-2xl font-bold">{formatCurrency(amount)}</p>
            {description && <p className="text-xs text-muted-foreground">{description}</p>}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

function DepositDialog() {
  const [open, setOpen] = useState(false)
  const [amount, setAmount] = useState("")
  const [description, setDescription] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)

    try {
      await walletApi.deposit(parseFloat(amount), description || undefined)
      toast({ title: "Deposito realizado", description: `Se han anadido ${formatCurrency(parseFloat(amount))} a tu cuenta` })
      setAmount("")
      setDescription("")
      setOpen(false)
      mutate("wallet")
    } catch {
      toast({ title: "Error", description: "No se pudo realizar el deposito", variant: "destructive" })
    } finally {
      setIsSubmitting(false)
    }
  }

  const presetAmounts = [100, 500, 1000, 5000]

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Depositar fondos
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Depositar fondos</DialogTitle>
          <DialogDescription>
            Anade fondos a tu wallet para empezar a operar
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-6 py-4">
            <div className="space-y-2">
              <Label htmlFor="amount">Cantidad</Label>
              <Input
                id="amount"
                type="number"
                step="0.01"
                min="1"
                placeholder="0.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                required
              />
              <div className="flex gap-2 mt-2">
                {presetAmounts.map((preset) => (
                  <Button
                    key={preset}
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => setAmount(preset.toString())}
                  >
                    {formatCurrency(preset)}
                  </Button>
                ))}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Descripcion (opcional)</Label>
              <Input
                id="description"
                placeholder="Ej: Deposito inicial"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            {amount && (
              <div className="p-4 bg-muted rounded-lg">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Total a depositar</span>
                  <span className="font-bold text-lg">{formatCurrency(parseFloat(amount) || 0)}</span>
                </div>
              </div>
            )}
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isSubmitting || !amount}>
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Procesando...
                </>
              ) : (
                "Confirmar deposito"
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default function WalletPage() {
  const { data: wallet, isLoading } = useSWR<WalletBalance>("wallet", () => walletApi.getBalance())

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  const totalBalance = (wallet?.availableBalance || 0) + (wallet?.reservedBalance || 0)

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Wallet</h1>
          <p className="text-muted-foreground">Gestiona tus fondos y depositos</p>
        </div>
        <DepositDialog />
      </div>

      {/* Balance Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <BalanceCard
          title="Balance Total"
          amount={totalBalance}
          icon={PiggyBank}
          description="Fondos totales en tu cuenta"
          variant="primary"
        />
        <BalanceCard
          title="Disponible"
          amount={wallet?.availableBalance || 0}
          icon={Wallet}
          description="Listo para operar"
        />
        <BalanceCard
          title="Reservado"
          amount={wallet?.reservedBalance || 0}
          icon={Lock}
          description="En ordenes pendientes"
          variant="muted"
        />
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <TrendingUp className="h-5 w-5" />
            Acciones rapidas
          </CardTitle>
          <CardDescription>Gestiona tus fondos facilmente</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Button variant="outline" className="h-24 flex-col gap-2" onClick={() => document.querySelector<HTMLButtonElement>('[data-deposit-trigger]')?.click()}>
              <Plus className="h-6 w-6 text-success" />
              <span>Depositar</span>
            </Button>
            <Button variant="outline" className="h-24 flex-col gap-2" disabled>
              <Wallet className="h-6 w-6 text-muted-foreground" />
              <span className="text-muted-foreground">Retirar (proximamente)</span>
            </Button>
            <Button variant="outline" className="h-24 flex-col gap-2" disabled>
              <TrendingUp className="h-6 w-6 text-muted-foreground" />
              <span className="text-muted-foreground">Transferir (proximamente)</span>
            </Button>
            <Button variant="outline" className="h-24 flex-col gap-2" disabled>
              <Lock className="h-6 w-6 text-muted-foreground" />
              <span className="text-muted-foreground">Historial (proximamente)</span>
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Info Card */}
      <Card className="bg-primary/5 border-primary/20">
        <CardContent className="pt-6">
          <div className="flex items-start gap-4">
            <div className="h-10 w-10 rounded-full bg-primary/20 flex items-center justify-center flex-shrink-0">
              <Wallet className="h-5 w-5 text-primary" />
            </div>
            <div>
              <h3 className="font-semibold mb-1">Tu dinero esta seguro</h3>
              <p className="text-sm text-muted-foreground">
                Los fondos reservados estan bloqueados mientras tengas ordenes pendientes. 
                Una vez ejecutadas o canceladas, los fondos volveran a estar disponibles.
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

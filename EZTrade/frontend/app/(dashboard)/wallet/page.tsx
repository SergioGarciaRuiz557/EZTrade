"use client"

import { useState, type ElementType, type ReactElement } from "react"
import useSWR, { mutate } from "swr"
import { walletApi, type WalletBalance, type WalletTransaction } from "@/lib/api"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { toast } from "@/components/ui/toaster"
import { cn, formatCurrency, formatDate } from "@/lib/utils"
import {
  ArrowDownToLine,
  ArrowUpFromLine,
  History,
  Loader2,
  Lock,
  PiggyBank,
  Plus,
  ReceiptText,
  RefreshCw,
  Send,
  TrendingUp,
  Wallet,
} from "lucide-react"

function BalanceCard({ title, amount, icon: Icon, description, variant = "default" }: {
  title: string
  amount: number
  icon: ElementType
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

function parsePositiveAmount(value: string) {
  const parsed = Number.parseFloat(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : 0
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error && typeof error === "object" && "message" in error) {
    const message = (error as { message?: unknown }).message
    if (typeof message === "string" && message.trim()) return message
  }
  return fallback
}

function refreshWalletData() {
  mutate("wallet")
  mutate("portfolio")
  mutate("wallet-transactions")
}

function DepositDialog({ trigger }: { trigger?: ReactElement }) {
  const [open, setOpen] = useState(false)
  const [amount, setAmount] = useState("")
  const [description, setDescription] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const amountValue = parsePositiveAmount(amount)
    if (amountValue <= 0) return

    setIsSubmitting(true)
    try {
      await walletApi.deposit(amountValue, description || undefined)
      toast({
        title: "Deposito realizado",
        description: `Se han anadido ${formatCurrency(amountValue)} a tu cuenta`,
        variant: "success",
      })
      setAmount("")
      setDescription("")
      setOpen(false)
      refreshWalletData()
    } catch (error) {
      toast({
        title: "Error",
        description: getErrorMessage(error, "No se pudo realizar el deposito"),
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const presetAmounts = [100, 500, 1000, 5000]
  const amountValue = parsePositiveAmount(amount)

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {trigger ?? (
          <Button data-deposit-trigger>
            <Plus className="mr-2 h-4 w-4" />
            Depositar fondos
          </Button>
        )}
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Depositar fondos</DialogTitle>
          <DialogDescription>Anade fondos a tu wallet para empezar a operar</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-6 py-4">
            <div className="space-y-2">
              <Label htmlFor="deposit-amount">Cantidad</Label>
              <Input
                id="deposit-amount"
                type="number"
                step="0.01"
                min="1"
                placeholder="0.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                required
              />
              <div className="flex flex-wrap gap-2 pt-2">
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
              <Label htmlFor="deposit-description">Descripcion (opcional)</Label>
              <Input
                id="deposit-description"
                placeholder="Ej: Deposito inicial"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            {amountValue > 0 && (
              <div className="rounded-lg bg-muted p-4">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Total a depositar</span>
                  <span className="text-lg font-bold">{formatCurrency(amountValue)}</span>
                </div>
              </div>
            )}
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isSubmitting || amountValue <= 0}>
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

function WithdrawDialog({ availableBalance, trigger }: { availableBalance: number; trigger: ReactElement }) {
  const [open, setOpen] = useState(false)
  const [amount, setAmount] = useState("")
  const [description, setDescription] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  const amountValue = parsePositiveAmount(amount)
  const canSubmit = amountValue > 0 && amountValue <= availableBalance

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!canSubmit) return

    setIsSubmitting(true)
    try {
      await walletApi.withdraw(amountValue, description || undefined)
      toast({
        title: "Retirada realizada",
        description: `Se han retirado ${formatCurrency(amountValue)} de tu wallet`,
        variant: "success",
      })
      setAmount("")
      setDescription("")
      setOpen(false)
      refreshWalletData()
    } catch (error) {
      toast({
        title: "Error",
        description: getErrorMessage(error, "No se pudo realizar la retirada"),
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Retirar fondos</DialogTitle>
          <DialogDescription>Retira efectivo disponible de tu wallet</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-6 py-4">
            <div className="rounded-lg bg-muted p-4">
              <p className="text-sm text-muted-foreground">Disponible</p>
              <p className="mt-1 text-xl font-bold">{formatCurrency(availableBalance)}</p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="withdraw-amount">Cantidad</Label>
              <Input
                id="withdraw-amount"
                type="number"
                step="0.01"
                min="1"
                max={availableBalance}
                placeholder="0.00"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                required
              />
              {amountValue > availableBalance && (
                <p className="text-sm text-destructive">La cantidad supera tu saldo disponible.</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="withdraw-description">Descripcion (opcional)</Label>
              <Input
                id="withdraw-description"
                placeholder="Ej: Retirada bancaria"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
              />
            </div>

            {amountValue > 0 && amountValue <= availableBalance && (
              <div className="rounded-lg bg-muted p-4">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Disponible restante</span>
                  <span className="font-bold">{formatCurrency(availableBalance - amountValue)}</span>
                </div>
              </div>
            )}
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isSubmitting || !canSubmit}>
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Procesando...
                </>
              ) : (
                "Confirmar retirada"
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function TransferDialog({ availableBalance, owner, trigger }: {
  availableBalance: number
  owner?: string
  trigger: ReactElement
}) {
  const [open, setOpen] = useState(false)
  const [recipient, setRecipient] = useState("")
  const [amount, setAmount] = useState("")
  const [description, setDescription] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  const amountValue = parsePositiveAmount(amount)
  const normalizedRecipient = recipient.trim()
  const isSelfTransfer = Boolean(owner && normalizedRecipient && owner.toLowerCase() === normalizedRecipient.toLowerCase())
  const canSubmit = Boolean(normalizedRecipient) && amountValue > 0 && amountValue <= availableBalance && !isSelfTransfer

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!canSubmit) return

    setIsSubmitting(true)
    try {
      await walletApi.transfer(normalizedRecipient, amountValue, description || undefined)
      toast({
        title: "Transferencia enviada",
        description: `${formatCurrency(amountValue)} para ${normalizedRecipient}`,
        variant: "success",
      })
      setRecipient("")
      setAmount("")
      setDescription("")
      setOpen(false)
      refreshWalletData()
    } catch (error) {
      toast({
        title: "Error",
        description: getErrorMessage(error, "No se pudo realizar la transferencia"),
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Transferir fondos</DialogTitle>
          <DialogDescription>Envia efectivo disponible a otro usuario de EZTrade</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-6 py-4">
            <div className="space-y-2">
              <Label htmlFor="transfer-recipient">Destinatario</Label>
              <Input
                id="transfer-recipient"
                placeholder="email o username"
                value={recipient}
                onChange={(event) => setRecipient(event.target.value)}
                required
              />
              {isSelfTransfer && (
                <p className="text-sm text-destructive">El destinatario debe ser distinto a tu usuario.</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="transfer-amount">Cantidad</Label>
              <Input
                id="transfer-amount"
                type="number"
                step="0.01"
                min="1"
                max={availableBalance}
                placeholder="0.00"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                required
              />
              {amountValue > availableBalance && (
                <p className="text-sm text-destructive">La cantidad supera tu saldo disponible.</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="transfer-description">Descripcion (opcional)</Label>
              <Input
                id="transfer-description"
                placeholder="Ej: Pago compartido"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
              />
            </div>

            <div className="rounded-lg bg-muted p-4">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Disponible</span>
                <span className="font-bold">{formatCurrency(availableBalance)}</span>
              </div>
              {amountValue > 0 && amountValue <= availableBalance && (
                <div className="mt-2 flex justify-between">
                  <span className="text-muted-foreground">Restante</span>
                  <span className="font-bold">{formatCurrency(availableBalance - amountValue)}</span>
                </div>
              )}
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isSubmitting || !canSubmit}>
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Enviando...
                </>
              ) : (
                "Confirmar transferencia"
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function movementLabel(type: WalletTransaction["movementType"]) {
  switch (type) {
    case "DEPOSIT":
      return "Deposito"
    case "WITHDRAWAL":
      return "Retirada"
    case "TRANSFER_OUT":
      return "Transferencia enviada"
    case "TRANSFER_IN":
      return "Transferencia recibida"
    case "RESERVE":
      return "Reserva"
    case "RELEASE":
      return "Liberacion"
    case "SETTLEMENT_DEBIT":
      return "Compra liquidada"
    case "SETTLEMENT_CREDIT":
      return "Venta liquidada"
    case "FEE":
      return "Comision"
  }
}

function formatDelta(value: number) {
  if (value === 0) return formatCurrency(0)
  return `${value > 0 ? "+" : "-"}${formatCurrency(Math.abs(value))}`
}

function transactionTone(transaction: WalletTransaction) {
  if (transaction.availableDelta > 0 || transaction.movementType === "TRANSFER_IN") {
    return "text-success"
  }
  if (transaction.availableDelta < 0 || transaction.reservedDelta < 0 || transaction.movementType === "TRANSFER_OUT") {
    return "text-destructive"
  }
  return "text-foreground"
}

function WalletHistoryDialog({ trigger }: { trigger: ReactElement }) {
  const [open, setOpen] = useState(false)
  const { data: transactions, isLoading, mutate: refreshTransactions } = useSWR<WalletTransaction[]>(
    open ? "wallet-transactions" : null,
    () => walletApi.getTransactions()
  )

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent className="max-h-[82vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Historial de wallet</DialogTitle>
          <DialogDescription>Movimientos y balances resultantes de tu cuenta</DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-2">
          <div className="flex justify-end">
            <Button type="button" variant="outline" size="sm" onClick={() => refreshTransactions()}>
              <RefreshCw className="mr-2 h-4 w-4" />
              Actualizar
            </Button>
          </div>

          {isLoading ? (
            <div className="flex h-48 items-center justify-center">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>
          ) : transactions && transactions.length > 0 ? (
            <div className="space-y-3">
              {transactions.map((transaction) => (
                <div key={transaction.id} className="rounded-lg border bg-card p-4">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div className="flex items-start gap-3">
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-muted">
                        <ReceiptText className="h-5 w-5 text-muted-foreground" />
                      </div>
                      <div>
                        <p className="font-semibold">{movementLabel(transaction.movementType)}</p>
                        <p className="text-sm text-muted-foreground">
                          {transaction.description || transaction.referenceId}
                        </p>
                        <p className="text-xs text-muted-foreground">{formatDate(transaction.occurredAt)}</p>
                      </div>
                    </div>

                    <div className="text-left sm:text-right">
                      <p className={cn("font-bold", transactionTone(transaction))}>
                        {formatCurrency(transaction.amount)}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Disponible {formatDelta(transaction.availableDelta)}
                      </p>
                      {transaction.reservedDelta !== 0 && (
                        <p className="text-xs text-muted-foreground">
                          Reservado {formatDelta(transaction.reservedDelta)}
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="mt-3 grid gap-2 border-t pt-3 text-xs text-muted-foreground sm:grid-cols-2">
                    <span>Balance disponible: {formatCurrency(transaction.availableBalanceAfter)}</span>
                    <span>Balance reservado: {formatCurrency(transaction.reservedBalanceAfter)}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="py-12 text-center text-muted-foreground">
              <History className="mx-auto mb-4 h-12 w-12 opacity-50" />
              <p>No hay movimientos registrados</p>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}

export default function WalletPage() {
  const { data: wallet, isLoading } = useSWR<WalletBalance>("wallet", () => walletApi.getBalance())

  if (isLoading) {
    return (
      <div className="flex h-96 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-b-2 border-primary" />
      </div>
    )
  }

  const availableBalance = wallet?.availableBalance || 0
  const reservedBalance = wallet?.reservedBalance || 0
  const totalBalance = availableBalance + reservedBalance

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Wallet</h1>
          <p className="text-muted-foreground">Gestiona tus fondos y depositos</p>
        </div>
        <DepositDialog />
      </div>

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
          amount={availableBalance}
          icon={Wallet}
          description="Listo para operar"
        />
        <BalanceCard
          title="Reservado"
          amount={reservedBalance}
          icon={Lock}
          description="En ordenes pendientes"
          variant="muted"
        />
      </div>

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
            <DepositDialog
              trigger={(
                <Button variant="outline" className="h-24 flex-col gap-2">
                  <ArrowDownToLine className="h-6 w-6 text-success" />
                  <span>Depositar</span>
                </Button>
              )}
            />
            <WithdrawDialog
              availableBalance={availableBalance}
              trigger={(
                <Button variant="outline" className="h-24 flex-col gap-2">
                  <ArrowUpFromLine className="h-6 w-6 text-destructive" />
                  <span>Retirar</span>
                </Button>
              )}
            />
            <TransferDialog
              availableBalance={availableBalance}
              owner={wallet?.owner}
              trigger={(
                <Button variant="outline" className="h-24 flex-col gap-2">
                  <Send className="h-6 w-6 text-primary" />
                  <span>Transferir</span>
                </Button>
              )}
            />
            <WalletHistoryDialog
              trigger={(
                <Button variant="outline" className="h-24 flex-col gap-2">
                  <History className="h-6 w-6 text-muted-foreground" />
                  <span>Historial</span>
                </Button>
              )}
            />
          </div>
        </CardContent>
      </Card>

      <Card className="bg-primary/5 border-primary/20">
        <CardContent className="pt-6">
          <div className="flex items-start gap-4">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/20">
              <Wallet className="h-5 w-5 text-primary" />
            </div>
            <div>
              <h3 className="mb-1 font-semibold">Tu dinero esta seguro</h3>
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

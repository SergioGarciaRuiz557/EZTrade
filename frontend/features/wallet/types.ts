export interface WalletBalance {
  owner: string
  availableBalance: number
  reservedBalance: number
}

export interface WalletTransaction {
  id: number
  movementType:
    | "DEPOSIT"
    | "WITHDRAWAL"
    | "TRANSFER_OUT"
    | "TRANSFER_IN"
    | "RESERVE"
    | "RELEASE"
    | "SETTLEMENT_DEBIT"
    | "SETTLEMENT_CREDIT"
    | "FEE"
  amount: number
  availableDelta: number
  reservedDelta: number
  availableBalanceAfter: number
  reservedBalanceAfter: number
  referenceType: "ORDER" | "MANUAL"
  referenceId: string
  description: string | null
  occurredAt: string
}

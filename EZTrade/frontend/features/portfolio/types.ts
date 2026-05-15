export interface Portfolio {
  owner: string
  cashAvailable: number
  totalCostBasis: number
  totalRealizedPnl: number
  positions: Position[]
}

export interface Position {
  symbol: string
  quantity: number
  averageCost: number
  realizedPnl: number
  updatedAt: string
}

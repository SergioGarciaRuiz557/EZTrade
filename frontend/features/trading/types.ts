export interface TradeOrder {
  id: number
  owner: string
  symbol: string
  side: "BUY" | "SELL"
  quantity: number
  price: number
  total: number
  status: "PENDING" | "EXECUTED" | "CANCELLED"
  createdAt: string
  executedAt: string | null
}

export interface PlaceOrderRequest {
  symbol: string
  side: "BUY" | "SELL"
  quantity: number
  price: number
}

export interface BuyFromMarketRequest {
  symbol: string
  quantity: number
}

export interface PlaceSellOfferRequest {
  symbol: string
  quantity: number
  price: number
}

export interface MarketplaceTrade {
  buyerOrder: TradeOrder
  sellerOrder: TradeOrder
}

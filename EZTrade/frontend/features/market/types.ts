export interface MarketPrice {
  symbol: string
  price: number
  timestamp: string
}

export interface Instrument {
  symbol: string
  name: string
  region: string
  currency: string
}

export interface InstrumentOverview {
  symbol: string
  name: string
  sector: string
  industry: string
  marketCap: number
  peRatio: number
}

export interface Candle {
  time: string
  open: number
  high: number
  low: number
  close: number
  volume: number
}

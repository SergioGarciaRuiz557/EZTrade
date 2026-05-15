import { API_BASE_URL, type ApiRequestOptions, fetchWithAuth } from "@/lib/api-client"
import type { Candle, Instrument, InstrumentOverview, MarketPrice } from "./types"

export const marketApi = {
  getPrice: async (symbol: string, options?: ApiRequestOptions) => {
    return fetchWithAuth<MarketPrice>(
      `${API_BASE_URL}/api/v1/market/get-price?symbol=${encodeURIComponent(symbol)}`,
      {},
      options
    )
  },

  search: async (input: string, options?: ApiRequestOptions) => {
    return fetchWithAuth<Instrument[]>(
      `${API_BASE_URL}/api/v1/market/search?input=${encodeURIComponent(input)}`,
      {},
      options
    )
  },

  getOverview: async (symbol: string, options?: ApiRequestOptions) => {
    return fetchWithAuth<InstrumentOverview>(
      `${API_BASE_URL}/api/v1/market/get-overview?symbol=${encodeURIComponent(symbol)}`,
      {},
      options
    )
  },

  getDailyCandles: async (symbol: string, options?: ApiRequestOptions) => {
    return fetchWithAuth<Candle[]>(
      `${API_BASE_URL}/api/v1/market/get-daily-candles?symbol=${encodeURIComponent(symbol)}`,
      {},
      options
    )
  },
}

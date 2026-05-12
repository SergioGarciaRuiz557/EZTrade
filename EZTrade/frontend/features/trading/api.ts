import { API_BASE_URL, fetchWithAuth } from "@/lib/api-client"
import type { BuyFromMarketRequest, MarketplaceTrade, PlaceOrderRequest, PlaceSellOfferRequest, TradeOrder } from "./types"

export const tradingApi = {
  getOrders: async () => {
    return fetchWithAuth<TradeOrder[]>(`${API_BASE_URL}/api/v1/trading/orders`)
  },

  placeOrder: async (order: PlaceOrderRequest) => {
    return fetchWithAuth<TradeOrder>(`${API_BASE_URL}/api/v1/trading/orders`, {
      method: "POST",
      body: JSON.stringify(order),
    })
  },

  executeOrder: async (orderId: number) => {
    return fetchWithAuth<TradeOrder>(`${API_BASE_URL}/api/v1/trading/orders/${orderId}/execute`, {
      method: "POST",
    })
  },

  cancelOrder: async (orderId: number) => {
    return fetchWithAuth<TradeOrder>(`${API_BASE_URL}/api/v1/trading/orders/${orderId}/cancel`, {
      method: "POST",
    })
  },

  buyFromMarket: async (order: BuyFromMarketRequest) => {
    return fetchWithAuth<TradeOrder>(`${API_BASE_URL}/api/v1/trading/market/buy`, {
      method: "POST",
      body: JSON.stringify(order),
    })
  },

  placeSellOffer: async (offer: PlaceSellOfferRequest) => {
    return fetchWithAuth<TradeOrder>(`${API_BASE_URL}/api/v1/trading/offers`, {
      method: "POST",
      body: JSON.stringify(offer),
    })
  },

  getSellOffers: async (symbol?: string) => {
    const query = symbol ? `?symbol=${encodeURIComponent(symbol)}` : ""
    return fetchWithAuth<TradeOrder[]>(`${API_BASE_URL}/api/v1/trading/offers${query}`)
  },

  buySellOffer: async (offerId: number) => {
    return fetchWithAuth<MarketplaceTrade>(`${API_BASE_URL}/api/v1/trading/offers/${offerId}/buy`, {
      method: "POST",
    })
  },
}

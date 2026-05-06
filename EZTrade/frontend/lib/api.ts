const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8088"

interface ApiError {
  message: string
  status: number
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      if (typeof window !== "undefined") {
        localStorage.removeItem("token")
        localStorage.removeItem("user")
        window.dispatchEvent(new Event("auth:unauthorized"))
      }
    }
    let message = response.statusText
    try {
      const payload = await response.clone().json()
      if (payload && typeof payload === "object") {
        if ("error" in payload && typeof payload.error === "string") {
          message = payload.error
        } else if ("message" in payload && typeof payload.message === "string") {
          message = payload.message
        }
      }
    } catch {
      try {
        const text = await response.text()
        if (text) message = text
      } catch {
        // Keep the HTTP status text if the body cannot be read.
      }
    }
    const error: ApiError = {
      message,
      status: response.status,
    }
    throw error
  }
  if (response.status === 204) {
    return undefined as T
  }
  return response.json()
}

function getAuthHeaders(): HeadersInit {
  const token = typeof window !== "undefined" ? localStorage.getItem("token") : null
  return {
    "Content-Type": "application/json",
    ...(token && { Authorization: `Bearer ${token}` }),
  }
}

// Auth API
export const authApi = {
  login: async (identifier: string, password: string) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: identifier, password }),
    })
    return handleResponse<{ token: string }>(response)
  },

  register: async (data: {
    firstname: string
    lastname: string
    username: string
    email: string
    password: string
  }) => {
    const response = await fetch(`${API_BASE_URL}/api/user/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    })
    return handleResponse<{ firstname: string; lastname: string; username: string; email: string }>(response)
  },

  getUser: async (email: string) => {
    const response = await fetch(`${API_BASE_URL}/api/user?email=${encodeURIComponent(email)}`, {
      headers: getAuthHeaders(),
    })
    return handleResponse<{ firstname: string; lastname: string; username: string; email: string }>(response)
  },
}

// Trading API
export const tradingApi = {
  getOrders: async () => {
    const response = await fetch(`${API_BASE_URL}/api/v1/trading/orders`, {
      headers: getAuthHeaders(),
    })
    return handleResponse<TradeOrder[]>(response)
  },

  placeOrder: async (order: PlaceOrderRequest) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/trading/orders`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify(order),
    })
    return handleResponse<TradeOrder>(response)
  },

  executeOrder: async (orderId: number) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/trading/orders/${orderId}/execute`, {
      method: "POST",
      headers: getAuthHeaders(),
    })
    return handleResponse<TradeOrder>(response)
  },

  cancelOrder: async (orderId: number) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/trading/orders/${orderId}/cancel`, {
      method: "POST",
      headers: getAuthHeaders(),
    })
    return handleResponse<TradeOrder>(response)
  },

  buyFromMarket: async (order: BuyFromMarketRequest) => {
    const priceResponse = await fetch(`${API_BASE_URL}/api/v1/market/get-price?symbol=${encodeURIComponent(order.symbol)}`, {
      headers: getAuthHeaders(),
    })
    const marketPrice = await handleResponse<MarketPrice>(priceResponse)

    const response = await fetch(`${API_BASE_URL}/api/v1/trading/orders`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({
        symbol: order.symbol,
        side: "BUY",
        quantity: order.quantity,
        price: marketPrice.price,
      }),
    })
    return handleResponse<TradeOrder>(response)
  },

  placeSellOffer: async (offer: PlaceSellOfferRequest) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/trading/offers`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify(offer),
    })
    return handleResponse<TradeOrder>(response)
  },

  getSellOffers: async (symbol?: string) => {
    const query = symbol ? `?symbol=${encodeURIComponent(symbol)}` : ""
    const response = await fetch(`${API_BASE_URL}/api/v1/trading/offers${query}`, {
      headers: getAuthHeaders(),
    })
    return handleResponse<TradeOrder[]>(response)
  },

  buySellOffer: async (offerId: number) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/trading/offers/${offerId}/buy`, {
      method: "POST",
      headers: getAuthHeaders(),
    })
    return handleResponse<MarketplaceTrade>(response)
  },
}

// Portfolio API
export const portfolioApi = {
  getPortfolio: async () => {
    const response = await fetch(`${API_BASE_URL}/api/portfolio`, {
      headers: getAuthHeaders(),
    })
    return handleResponse<Portfolio>(response)
  },
}

// Wallet API
export const walletApi = {
  getBalance: async () => {
    const response = await fetch(`${API_BASE_URL}/api/v1/wallet/balance`, {
      headers: getAuthHeaders(),
    })
    return handleResponse<WalletBalance>(response)
  },

  deposit: async (amount: number, description?: string) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/wallet/deposit`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({ amount, description }),
    })
    return handleResponse<WalletBalance>(response)
  },
}

// Market API (requieren autenticacion)
export const marketApi = {
  getPrice: async (symbol: string) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/market/get-price?symbol=${encodeURIComponent(symbol)}`, {
      headers: getAuthHeaders(),
    })
    return handleResponse<MarketPrice>(response)
  },

  search: async (input: string) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/market/search?input=${encodeURIComponent(input)}`, {
      headers: getAuthHeaders(),
    })
    return handleResponse<Instrument[]>(response)
  },

  getOverview: async (symbol: string) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/market/get-overview?symbol=${encodeURIComponent(symbol)}`, {
      headers: getAuthHeaders(),
    })
    return handleResponse<InstrumentOverview>(response)
  },

  getDailyCandles: async (symbol: string) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/market/get-daily-candles?symbol=${encodeURIComponent(symbol)}`, {
      headers: getAuthHeaders(),
    })
    return handleResponse<Candle[]>(response)
  },
}

// Types
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

export interface WalletBalance {
  owner: string
  availableBalance: number
  reservedBalance: number
}

export interface MarketPrice {
  symbol: { value: string }
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

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8088"

interface ApiError {
  message: string
  status: number
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error: ApiError = {
      message: response.statusText,
      status: response.status,
    }
    throw error
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

// Market API (endpoints publicos, no requieren autenticacion)
export const marketApi = {
  getPrice: async (symbol: string) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/market/get-price?symbol=${encodeURIComponent(symbol)}`, {
      headers: { "Content-Type": "application/json" },
    })
    return handleResponse<MarketPrice>(response)
  },

  search: async (input: string) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/market/search?input=${encodeURIComponent(input)}`, {
      headers: { "Content-Type": "application/json" },
    })
    return handleResponse<Instrument[]>(response)
  },

  getOverview: async (symbol: string) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/market/get-overview?symbol=${encodeURIComponent(symbol)}`, {
      headers: { "Content-Type": "application/json" },
    })
    return handleResponse<InstrumentOverview>(response)
  },

  // Obtener precios de multiples simbolos
  getPrices: async (symbols: string[]) => {
    const prices = await Promise.all(
      symbols.map(async (symbol) => {
        try {
          const response = await fetch(`${API_BASE_URL}/api/v1/market/get-price?symbol=${encodeURIComponent(symbol)}`, {
            headers: { "Content-Type": "application/json" },
          })
          if (!response.ok) return null
          return response.json() as Promise<MarketPrice>
        } catch {
          return null
        }
      })
    )
    return prices.filter((p): p is MarketPrice => p !== null)
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

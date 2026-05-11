const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8088"

interface ApiError {
  message: string
  status: number
}

interface ApiRequestOptions {
  signal?: AbortSignal
  token?: string | null
}

interface HandleResponseOptions {
  requestToken?: string | null
}

function getStoredToken(): string | null {
  return typeof window !== "undefined" ? localStorage.getItem("token") : null
}

async function handleResponse<T>(response: Response, options: HandleResponseOptions = {}): Promise<T> {
  if (!response.ok) {
    if (response.status === 401) {
      if (typeof window !== "undefined") {
        const currentToken = getStoredToken()
        if (options.requestToken && currentToken === options.requestToken) {
          localStorage.removeItem("token")
          localStorage.removeItem("user")
          window.dispatchEvent(new Event("auth:unauthorized"))
        }
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

function getAuthHeaders(token = getStoredToken()): HeadersInit {
  return {
    "Content-Type": "application/json",
    ...(token && { Authorization: `Bearer ${token}` }),
  }
}

async function fetchWithAuth<T>(
  url: string,
  init: RequestInit = {},
  options: ApiRequestOptions = {}
): Promise<T> {
  const requestToken = options.token ?? getStoredToken()
  const response = await fetch(url, {
    ...init,
    headers: {
      ...getAuthHeaders(requestToken),
      ...init.headers,
    },
    signal: options.signal,
  })
  return handleResponse<T>(response, { requestToken })
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

  getUser: async (email: string, options?: ApiRequestOptions) => {
    return fetchWithAuth<{ firstname: string; lastname: string; username: string; email: string }>(
      `${API_BASE_URL}/api/user?email=${encodeURIComponent(email)}`,
      {},
      options
    )
  },
}

// Trading API
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
    const marketPrice = await fetchWithAuth<MarketPrice>(
      `${API_BASE_URL}/api/v1/market/get-price?symbol=${encodeURIComponent(order.symbol)}`
    )

    return fetchWithAuth<TradeOrder>(`${API_BASE_URL}/api/v1/trading/orders`, {
      method: "POST",
      body: JSON.stringify({
        symbol: order.symbol,
        side: "BUY",
        quantity: order.quantity,
        price: marketPrice.price,
      }),
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

// Portfolio API
export const portfolioApi = {
  getPortfolio: async () => {
    return fetchWithAuth<Portfolio>(`${API_BASE_URL}/api/portfolio`)
  },
}

// Wallet API
export const walletApi = {
  getBalance: async () => {
    return fetchWithAuth<WalletBalance>(`${API_BASE_URL}/api/v1/wallet/balance`)
  },

  deposit: async (amount: number, description?: string) => {
    return fetchWithAuth<WalletBalance>(`${API_BASE_URL}/api/v1/wallet/deposit`, {
      method: "POST",
      body: JSON.stringify({ amount, description }),
    })
  },

  withdraw: async (amount: number, description?: string) => {
    return fetchWithAuth<WalletBalance>(`${API_BASE_URL}/api/v1/wallet/withdraw`, {
      method: "POST",
      body: JSON.stringify({ amount, description }),
    })
  },

  transfer: async (recipient: string, amount: number, description?: string) => {
    return fetchWithAuth<WalletBalance>(`${API_BASE_URL}/api/v1/wallet/transfer`, {
      method: "POST",
      body: JSON.stringify({ recipient, amount, description }),
    })
  },

  getTransactions: async () => {
    return fetchWithAuth<WalletTransaction[]>(`${API_BASE_URL}/api/v1/wallet/transactions`)
  },
}

// Market API (requieren autenticacion)
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

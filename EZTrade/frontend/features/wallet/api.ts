import { API_BASE_URL, fetchWithAuth } from "@/lib/api-client"
import type { WalletBalance, WalletTransaction } from "./types"

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

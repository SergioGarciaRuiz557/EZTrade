import { API_BASE_URL, type ApiRequestOptions, fetchWithAuth } from "@/lib/api-client"
import type { Portfolio } from "./types"

export const portfolioApi = {
  getPortfolio: async (options?: ApiRequestOptions) => {
    return fetchWithAuth<Portfolio>(`${API_BASE_URL}/api/portfolio`, {}, options)
  },
}

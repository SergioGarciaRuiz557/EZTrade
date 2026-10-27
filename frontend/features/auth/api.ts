import { API_BASE_URL, type ApiRequestOptions, fetchWithAuth, handleResponse } from "@/lib/api-client"
import type { LoginResponse, RegisterRequest, UserProfile } from "./types"

export const authApi = {
  login: async (identifier: string, password: string) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: identifier, password }),
    })
    return handleResponse<LoginResponse>(response)
  },

  register: async (data: RegisterRequest) => {
    const response = await fetch(`${API_BASE_URL}/api/user/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    })
    return handleResponse<UserProfile>(response)
  },

  getUser: async (email: string, options?: ApiRequestOptions) => {
    return fetchWithAuth<UserProfile>(
      `${API_BASE_URL}/api/user?email=${encodeURIComponent(email)}`,
      {},
      options
    )
  },
}

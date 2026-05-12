export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8088"

export interface ApiError {
  message: string
  status: number
}

export interface ApiRequestOptions {
  signal?: AbortSignal
  token?: string | null
}

interface HandleResponseOptions {
  requestToken?: string | null
}

function getStoredToken(): string | null {
  return typeof window !== "undefined" ? localStorage.getItem("token") : null
}

export async function handleResponse<T>(response: Response, options: HandleResponseOptions = {}): Promise<T> {
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

    throw { message, status: response.status } satisfies ApiError
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

export async function fetchWithAuth<T>(
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

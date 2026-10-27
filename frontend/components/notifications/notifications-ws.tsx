"use client"

import { useEffect, useRef } from "react"
import { Client, type IMessage } from "@stomp/stompjs"
import { useAuth } from "@/lib/auth-context"
import { toast } from "@/components/ui/toaster"

type NotificationPayload = {
  type?: string
  title?: string
  body?: string
  occurredAt?: string
  severity?: string
  level?: string
  status?: string
  variant?: string
}

type ToastVariant = "default" | "destructive" | "success" | "warning"

// Default values for connecting to the backend STOMP endpoint.
const DEFAULT_WS_PATH = "/ws"
const USER_QUEUE_DESTINATION = "/user/queue/notifications"
const RECONNECT_DELAY_MS = 5000

// Converts the backend HTTP URL to its WebSocket equivalent when needed.
function normalizeWsUrl(url: string): string {
  if (url.startsWith("https://")) return `wss://${url.slice("https://".length)}`
  if (url.startsWith("http://")) return `ws://${url.slice("http://".length)}`
  return url
}

// Resolves the final WebSocket URL from NEXT_PUBLIC_WS_URL or from the API URL.
function getWsUrl(): string {
  const explicit = process.env.NEXT_PUBLIC_WS_URL
  if (explicit) return normalizeWsUrl(explicit)

  const apiBaseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8088"
  const wsBaseUrl = normalizeWsUrl(apiBaseUrl)
  return `${wsBaseUrl}${DEFAULT_WS_PATH}`
}

// Translates backend severity/type into the toast visual tone.
function chooseVariant(payload: NotificationPayload): ToastVariant {
  const explicitTone = `${payload.variant ?? payload.severity ?? payload.level ?? payload.status ?? ""}`.toLowerCase()

  if (explicitTone.includes("warn")) return "warning"
  if (explicitTone.includes("error") || explicitTone.includes("danger") || explicitTone.includes("destructive")) {
    return "destructive"
  }
  if (explicitTone.includes("success")) return "success"

  const content = `${payload.type ?? ""} ${payload.title ?? ""} ${payload.body ?? ""}`.toLowerCase()

  if (content.includes("warn") || content.includes("warning") || content.includes("advert")) {
    return "warning"
  }
  if (
    content.includes("error") ||
    content.includes("failed") ||
    content.includes("insufficient") ||
    content.includes("cancel") ||
    content.includes("deleted") ||
    content.includes("removed") ||
    content.includes("elimin") ||
    content.includes("reject")
  ) {
    return "destructive"
  }
  if (
    content.includes("success") ||
    content.includes("placed") ||
    content.includes("registrad") ||
    content.includes("created") ||
    content.includes("cread") ||
    content.includes("executed") ||
    content.includes("ejecutad") ||
    content.includes("updated") ||
    content.includes("actualizad") ||
    content.includes("completed")
  ) {
    return "success"
  }

  return "default"
}

// Accepts JSON messages and plain bodies so the toast does not fail on simple formats.
function parseMessage(message: IMessage): NotificationPayload {
  try {
    return JSON.parse(message.body) as NotificationPayload
  } catch {
    return { title: "Notificacion", body: message.body }
  }
}

// Maintains one WebSocket connection per authenticated user and shows each message as a toast.
export function NotificationsWebSocket() {
  const { token } = useAuth()
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    // If there is no token, close any previous connection to avoid receiving events from another session.
    if (!token) {
      if (clientRef.current) {
        clientRef.current.deactivate()
        clientRef.current = null
      }
      return
    }

    const client = new Client({
      brokerURL: getWsUrl(),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: RECONNECT_DELAY_MS,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        // The backend publishes private notifications to the authenticated user's queue.
        client.subscribe(USER_QUEUE_DESTINATION, (message) => {
          const payload = parseMessage(message)
          toast({
            title: payload.title || payload.type || "Notificacion",
            description: payload.body || payload.occurredAt,
            variant: chooseVariant(payload),
          })
        })
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
    }
  }, [token])

  return null
}

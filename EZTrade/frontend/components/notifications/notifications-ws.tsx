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
}

const DEFAULT_WS_PATH = "/ws"
const USER_QUEUE_DESTINATION = "/user/queue/notifications"
const RECONNECT_DELAY_MS = 5000

function normalizeWsUrl(url: string): string {
  if (url.startsWith("https://")) return `wss://${url.slice("https://".length)}`
  if (url.startsWith("http://")) return `ws://${url.slice("http://".length)}`
  return url
}

function getWsUrl(): string {
  const explicit = process.env.NEXT_PUBLIC_WS_URL
  if (explicit) return normalizeWsUrl(explicit)

  const apiBaseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8088"
  const wsBaseUrl = normalizeWsUrl(apiBaseUrl)
  return `${wsBaseUrl}${DEFAULT_WS_PATH}`
}

function chooseVariant(type?: string) {
  const normalized = type?.toLowerCase() || ""
  if (normalized.includes("error") || normalized.includes("failed") || normalized.includes("cancel")) {
    return "destructive" as const
  }
  if (normalized.includes("success") || normalized.includes("executed") || normalized.includes("completed")) {
    return "success" as const
  }
  return "default" as const
}

function parseMessage(message: IMessage): NotificationPayload {
  try {
    return JSON.parse(message.body) as NotificationPayload
  } catch {
    return { title: "Notificacion", body: message.body }
  }
}

export function NotificationsWebSocket() {
  const { token } = useAuth()
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
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
        client.subscribe(USER_QUEUE_DESTINATION, (message) => {
          const payload = parseMessage(message)
          toast({
            title: payload.title || payload.type || "Notificacion",
            description: payload.body || payload.occurredAt,
            variant: chooseVariant(payload.type),
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

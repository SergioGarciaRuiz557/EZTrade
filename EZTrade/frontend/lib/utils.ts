import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

// Une clases condicionales y resuelve conflictos de Tailwind, por ejemplo varios paddings.
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

// Formatea importes monetarios en el locale espanol usado por la interfaz.
export function formatCurrency(value: number, currency = "USD"): string {
  return new Intl.NumberFormat("es-ES", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value)
}

// Formatea cantidades de acciones u otros valores numericos con dos decimales.
export function formatNumber(value: number): string {
  return new Intl.NumberFormat("es-ES", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value)
}

// Convierte un numero porcentual, por ejemplo 12.5, a una cadena localizada.
export function formatPercentage(value: number): string {
  return new Intl.NumberFormat("es-ES", {
    style: "percent",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value / 100)
}

// Presenta fechas del backend en formato corto con hora para listados y movimientos.
export function formatDate(date: string | Date): string {
  return new Intl.DateTimeFormat("es-ES", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(date))
}

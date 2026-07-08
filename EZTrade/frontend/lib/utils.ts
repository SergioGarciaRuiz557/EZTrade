import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

// Merges conditional classes and resolves Tailwind conflicts, such as multiple paddings.
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

// Formats monetary amounts using the Spanish locale currently used by the interface.
export function formatCurrency(value: number, currency = "USD"): string {
  return new Intl.NumberFormat("es-ES", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value)
}

// Formats share quantities or other numeric values with two decimals.
export function formatNumber(value: number): string {
  return new Intl.NumberFormat("es-ES", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value)
}

// Converts a percentage number, for example 12.5, into a localized string.
export function formatPercentage(value: number): string {
  return new Intl.NumberFormat("es-ES", {
    style: "percent",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value / 100)
}

// Presents backend dates in a short date-time format for lists and movements.
export function formatDate(date: string | Date): string {
  return new Intl.DateTimeFormat("es-ES", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(date))
}

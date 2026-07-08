"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"
import {
  TrendingUp,
  LayoutDashboard,
  LineChart,
  Wallet,
  Search,
  LogOut,
  User,
  Home,
} from "lucide-react"
import { useAuth } from "@/lib/auth-context"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

// Main entries for the authenticated area with route and icon.
const navigation = [
  { name: "Inicio", href: "/", icon: Home },
  { name: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
  { name: "Trading", href: "/trading", icon: LineChart },
  { name: "Wallet", href: "/wallet", icon: Wallet },
  { name: "Mercado", href: "/market", icon: Search },
]

export function Sidebar() {
  // usePathname highlights the active route without maintaining manual state.
  const pathname = usePathname()
  const { user, logout } = useAuth()

  return (
    <>
      <aside className="fixed inset-y-0 left-0 z-50 hidden w-64 flex-col border-r bg-card md:flex">
        {/* Logo - clickable to return home. */}
        <Link href="/" className="flex h-16 items-center gap-2 border-b px-6 hover:bg-muted transition-colors">
          <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
            <TrendingUp className="h-5 w-5 text-primary-foreground" />
          </div>
          <span className="text-xl font-bold">EZTrade</span>
        </Link>

        {/* Navigation */}
        <nav className="flex-1 space-y-1 px-3 py-4">
          {navigation.map((item) => {
            const isActive = pathname === item.href
            return (
              <Link
                key={item.name}
                href={item.href}
                className={cn(
                  "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
                  isActive
                    ? "bg-primary text-primary-foreground"
                    : "text-muted-foreground hover:bg-muted hover:text-foreground"
                )}
              >
                <item.icon className="h-5 w-5" />
                {item.name}
              </Link>
            )
          })}
        </nav>

        {/* User Menu */}
        <div className="border-t p-4">
          <UserMenu user={user} logout={logout} />
        </div>
      </aside>

      <header className="fixed inset-x-0 top-0 z-50 flex h-16 items-center justify-between border-b bg-card px-4 md:hidden">
        <Link href="/" className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
            <TrendingUp className="h-5 w-5 text-primary-foreground" />
          </div>
          <span className="text-lg font-bold">EZTrade</span>
        </Link>
        <UserMenu user={user} logout={logout} compact />
      </header>

      <nav className="fixed inset-x-0 bottom-0 z-50 grid grid-cols-5 border-t bg-card md:hidden">
        {navigation.map((item) => {
          const isActive = pathname === item.href
          return (
            <Link
              key={item.name}
              href={item.href}
              className={cn(
                "flex min-h-16 flex-col items-center justify-center gap-1 px-1 text-[11px] font-medium transition-colors",
                isActive ? "text-primary" : "text-muted-foreground"
              )}
            >
              <item.icon className="h-5 w-5" />
              <span className="max-w-full truncate">{item.name}</span>
            </Link>
          )
        })}
      </nav>
    </>
  )
}

function UserMenu({
  user,
  logout,
  compact = false,
}: {
  user: ReturnType<typeof useAuth>["user"]
  logout: ReturnType<typeof useAuth>["logout"]
  compact?: boolean
}) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className={cn("gap-3 px-3", compact ? "h-10 w-10 p-0" : "w-full justify-start")}>
          <Avatar className="h-8 w-8">
            <AvatarFallback className="bg-primary text-primary-foreground">
              {user?.firstname?.[0]}
              {user?.lastname?.[0]}
            </AvatarFallback>
          </Avatar>
          {!compact && (
            <div className="flex min-w-0 flex-col items-start text-sm">
              <span className="max-w-full truncate font-medium">
                {user?.firstname} {user?.lastname}
              </span>
              <span className="max-w-full truncate text-xs text-muted-foreground">{user?.email}</span>
            </div>
          )}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-56">
        <DropdownMenuLabel>Mi cuenta</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem>
          <User className="mr-2 h-4 w-4" />
          Perfil
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={logout} className="text-destructive">
          <LogOut className="mr-2 h-4 w-4" />
          Cerrar sesion
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

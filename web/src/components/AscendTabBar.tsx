import { BookOpen, CalendarCheck, ListChecks, Lock, MoreHorizontal } from "lucide-react"
import type { LucideIcon } from "lucide-react"

export type TabId = "today" | "trackers" | "learn" | "vault" | "more"

const TABS: { id: TabId; label: string; Icon: LucideIcon }[] = [
  { id: "today", label: "Today", Icon: CalendarCheck },
  { id: "trackers", label: "Trackers", Icon: ListChecks },
  { id: "learn", label: "Learn", Icon: BookOpen },
  { id: "vault", label: "Vault", Icon: Lock },
  { id: "more", label: "More", Icon: MoreHorizontal },
]

interface AscendTabBarProps {
  value: TabId
  onChange: (tab: TabId) => void
}

export function AscendTabBar({ value, onChange }: AscendTabBarProps) {
  return (
    <nav
      aria-label="Primary"
      className="fixed inset-x-0 bottom-0 z-40 border-t border-border bg-[#0a0e17]/95 pb-[env(safe-area-inset-bottom)] backdrop-blur-md"
    >
      <div className="mx-auto flex h-16 max-w-3xl">
        {TABS.map(({ id, label, Icon }) => {
          const isActive = id === value
          return (
            <button
              key={id}
              type="button"
              onClick={() => onChange(id)}
              aria-current={isActive ? "page" : undefined}
              className={`flex min-h-11 flex-1 flex-col items-center justify-center gap-1 text-[11px] font-medium transition-colors ${
                isActive ? "text-primary" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <Icon className="h-5 w-5" />
              {label}
            </button>
          )
        })}
      </div>
    </nav>
  )
}

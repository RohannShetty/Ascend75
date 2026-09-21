import { useState } from "react"
import { Bell, Flame, Moon, Plus, Search, Settings, User } from "lucide-react"

import { AscendTabBar, type TabId } from "@/components/AscendTabBar"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet"
import { CHALLENGE_LENGTH_DAYS, HABIT_CATEGORIES, INITIAL_HABITS, type Habit, type HabitCategory } from "@/lib/protocol"
import { LearnView } from "@/views/LearnView"
import { MoreView } from "@/views/MoreView"
import { TodayView, type TodayMetrics } from "@/views/TodayView"
import { TrackersView } from "@/views/TrackersView"
import { VaultView } from "@/views/VaultView"

const CURRENT_DAY = 18

function milestoneRankFor(day: number): { rank: string; next: number } {
  if (day >= 60) return { rank: "Legend Tier", next: 75 }
  if (day >= 30) return { rank: "Titan Tier", next: 45 }
  return { rank: "Iron Tier", next: 30 }
}

export default function App() {
  const [tab, setTab] = useState<TabId>("today")
  const [searchQuery, setSearchQuery] = useState("")
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [newHabitTitle, setNewHabitTitle] = useState("")
  const [newHabitCategory, setNewHabitCategory] = useState<HabitCategory>("Workout")
  const [sleepCutoffHour, setSleepCutoffHour] = useState(3)
  const [habits, setHabits] = useState<Habit[]>(INITIAL_HABITS)

  const toggleHabit = (id: string) => {
    setHabits((previous) =>
      previous.map((habit) => (habit.id === id ? { ...habit, completed: !habit.completed } : habit))
    )
  }

  const handleAddHabit = (event: React.FormEvent) => {
    event.preventDefault()
    if (!newHabitTitle.trim()) return

    setHabits((previous) => [
      {
        id: `custom-${Date.now()}`,
        title: newHabitTitle.trim(),
        category: newHabitCategory,
        time: "Flexible",
        completed: false,
        streak: 0,
      },
      ...previous,
    ])
    setNewHabitTitle("")
    setIsDialogOpen(false)
  }

  const wipeAllData = () => {
    setHabits([])
    setSearchQuery("")
    setTab("today")
  }

  const completedCount = habits.filter((habit) => habit.completed).length
  const completionPercentage = habits.length > 0 ? Math.round((completedCount / habits.length) * 100) : 0
  const waterHabit = habits.find((habit) => habit.id === "water")
  const hydrationCurrent = waterHabit?.value ?? 0
  const hydrationTarget = waterHabit?.target ?? 3800
  const rank = milestoneRankFor(CURRENT_DAY)

  const metrics: TodayMetrics = {
    completionPercentage,
    completedCount,
    totalCount: habits.length,
    streakDays: habits.reduce((longest, habit) => Math.max(longest, habit.streak), 0),
    hydrationCurrent,
    hydrationTarget,
    hydrationPercentage: Math.min(100, Math.round((hydrationCurrent / hydrationTarget) * 100)),
    milestoneRank: rank.rank,
    nextMilestone: rank.next,
    daysToNextTier: rank.next - CURRENT_DAY,
  }

  const needle = searchQuery.trim().toLowerCase()
  const filteredHabits = habits.filter(
    (habit) =>
      needle === "" ||
      habit.title.toLowerCase().includes(needle) ||
      habit.category.toLowerCase().includes(needle)
  )

  return (
    <div className="flex min-h-screen flex-col bg-background text-foreground selection:bg-primary/20">
      <header className="sticky top-0 z-40 border-b border-border bg-background/80 backdrop-blur-md">
        <div className="mx-auto flex h-16 max-w-3xl items-center justify-between px-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary text-primary-foreground font-bold">
              <Flame className="h-5 w-5" />
            </div>
            <div className="flex items-center gap-2">
              <span className="text-lg font-bold tracking-tight">Ascend 75</span>
              <span className="rounded-full border border-border bg-secondary px-2 py-0.5 text-xs font-medium text-secondary-foreground">
                Day {CURRENT_DAY} of {CHALLENGE_LENGTH_DAYS}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="relative" aria-label="Activity updates">
                  <Bell className="h-4 w-4" />
                  <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-primary" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-72">
                <DropdownMenuLabel>Activity Updates</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem className="flex flex-col items-start gap-1 p-3">
                  <div className="flex items-center gap-2">
                    <Badge variant="outline" className="text-xs">
                      Workout
                    </Badge>
                    <span className="text-xs font-semibold">Morning Workout Logged</span>
                  </div>
                  <span className="text-xs text-muted-foreground">45 min session synced 2 hours ago.</span>
                </DropdownMenuItem>
                <DropdownMenuItem className="flex flex-col items-start gap-1 p-3">
                  <div className="flex items-center gap-2">
                    <Badge variant="outline" className="text-xs">
                      Reminder
                    </Badge>
                    <span className="text-xs font-semibold">Hydration Check</span>
                  </div>
                  <span className="text-xs text-muted-foreground">
                    {(hydrationTarget - hydrationCurrent) / 1000} L remaining to hit today&apos;s goal.
                  </span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="h-9 w-9 rounded-full p-0" aria-label="Account menu">
                  <Avatar className="h-9 w-9 border border-border">
                    <AvatarFallback>AS</AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuLabel>
                  <div className="flex flex-col space-y-1">
                    <p className="text-sm font-medium leading-none">Ascend Athlete</p>
                    <p className="text-xs leading-none text-muted-foreground">Local profile — no account</p>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => setTab("more")}>
                  <Settings className="mr-2 h-4 w-4" /> Protocol Preferences
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setTab("today")}>
                  <User className="mr-2 h-4 w-4" /> Profile &amp; Stats
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
      </header>

      <main className="mx-auto w-full max-w-3xl flex-1 space-y-6 px-4 py-6 pb-28">
        {tab === "today" && (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="Filter habits..."
                  value={searchQuery}
                  onChange={(event) => setSearchQuery(event.target.value)}
                  className="pl-9"
                />
              </div>

              <Sheet>
                <SheetTrigger asChild>
                  <Button variant="outline">Protocol Rules</Button>
                </SheetTrigger>
                <SheetContent>
                  <SheetHeader>
                    <SheetTitle>Ascend 75 Constitution</SheetTitle>
                    <SheetDescription>Non-negotiable daily rules for 75 continuous days.</SheetDescription>
                  </SheetHeader>
                  <div className="mt-6 space-y-4 text-sm">
                    <div className="rounded-lg border bg-card p-3">
                      <p className="font-semibold">1. Two 45-min Workouts</p>
                      <p className="mt-1 text-xs text-muted-foreground">One must be outdoors regardless of weather.</p>
                    </div>
                    <div className="rounded-lg border bg-card p-3">
                      <p className="font-semibold">2. Clean Nutrition</p>
                      <p className="mt-1 text-xs text-muted-foreground">Zero cheat meals, zero alcohol.</p>
                    </div>
                    <div className="rounded-lg border bg-card p-3">
                      <p className="font-semibold">3. 3.8 L Water</p>
                      <p className="mt-1 text-xs text-muted-foreground">Spread evenly throughout waking hours.</p>
                    </div>
                    <div className="rounded-lg border bg-card p-3">
                      <p className="font-semibold">4. 10 Pages Reading</p>
                      <p className="mt-1 text-xs text-muted-foreground">Non-fiction or personal growth books.</p>
                    </div>
                    <div className="rounded-lg border bg-card p-3">
                      <p className="font-semibold">5. Daily Progress Photo</p>
                      <p className="mt-1 text-xs text-muted-foreground">Visual accountability inside the vault.</p>
                    </div>
                  </div>
                </SheetContent>
              </Sheet>

              <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogTrigger asChild>
                  <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Objective
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Add Custom Daily Objective</DialogTitle>
                    <DialogDescription>
                      Introduce an additional targeted habit to your daily discipline flow.
                    </DialogDescription>
                  </DialogHeader>
                  <form onSubmit={handleAddHabit} className="space-y-4 py-2">
                    <div className="space-y-2">
                      <Label htmlFor="title">Objective Title</Label>
                      <Input
                        id="title"
                        placeholder="e.g., 15-min Cold Immersion / Mobility"
                        value={newHabitTitle}
                        onChange={(event) => setNewHabitTitle(event.target.value)}
                        required
                      />
                    </div>
                    <div className="space-y-2">
                      <Label>Category</Label>
                      <div className="flex flex-wrap gap-2">
                        {HABIT_CATEGORIES.map((option) => (
                          <Button
                            key={option}
                            type="button"
                            size="sm"
                            variant={option === newHabitCategory ? "default" : "outline"}
                            onClick={() => setNewHabitCategory(option)}
                          >
                            {option}
                          </Button>
                        ))}
                      </div>
                    </div>
                    <DialogFooter className="pt-4">
                      <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                        Cancel
                      </Button>
                      <Button type="submit">Save Objective</Button>
                    </DialogFooter>
                  </form>
                </DialogContent>
              </Dialog>
            </div>

            <TodayView
              habits={habits}
              filteredHabits={filteredHabits}
              metrics={metrics}
              onToggleHabit={toggleHabit}
            />
          </div>
        )}

        {tab === "trackers" && (
          <TrackersView habits={habits} onToggleHabit={toggleHabit} onOpenVault={() => setTab("vault")} />
        )}
        {tab === "learn" && <LearnView />}
        {tab === "vault" && <VaultView />}
        {tab === "more" && (
          <MoreView
            habits={habits}
            sleepCutoffHour={sleepCutoffHour}
            onSleepCutoffChange={setSleepCutoffHour}
            onWipe={wipeAllData}
          />
        )}
      </main>

      <footer className="border-t border-border px-4 py-6 pb-24 text-center text-xs text-muted-foreground">
        <span className="inline-flex items-center gap-1">
          <Moon className="h-3 w-3" />
          Obsidian theme only • Prototype reference for the Android build
        </span>
      </footer>

      <AscendTabBar value={tab} onChange={setTab} />
    </div>
  )
}

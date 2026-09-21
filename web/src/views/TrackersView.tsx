import { Camera, CheckCircle2, Dumbbell, Droplets, Lock, BookOpen } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import type { Habit } from "@/lib/protocol"

interface TrackersViewProps {
  habits: Habit[]
  onToggleHabit: (id: string) => void
  onOpenVault: () => void
}

function progressPercent(habit: Habit): number {
  if (!habit.target || habit.target <= 0) return habit.completed ? 100 : 0
  return Math.min(100, Math.round(((habit.value ?? 0) / habit.target) * 100))
}

function valueLabel(habit: Habit): string {
  if (habit.unit === "check") return habit.completed ? "Complete" : "Pending"
  if (habit.unit === "photo") return habit.completed ? "1 / 1 captured" : "No photo today"
  return `${habit.value ?? 0} / ${habit.target ?? 0} ${habit.unit ?? ""}`
}

export function TrackersView({ habits, onToggleHabit, onOpenVault }: TrackersViewProps) {
  const workouts = habits.filter((habit) => habit.category === "Workout")
  const water = habits.find((habit) => habit.id === "water")
  const reading = habits.find((habit) => habit.id === "reading")
  const photo = habits.find((habit) => habit.id === "photo")

  return (
    <div className="space-y-6">
      <div>
        <p className="text-[10px] font-semibold uppercase tracking-wider text-primary">Dedicated Engines</p>
        <h1 className="text-2xl font-bold tracking-tight">Trackers</h1>
        <p className="text-xs text-muted-foreground mt-1">Day 18 of 75</p>
      </div>

      {workouts.map((habit) => (
        <Card key={habit.id}>
          <CardHeader className="pb-2">
            <CardTitle className="text-base flex items-center justify-between">
              <span className="flex items-center gap-2">
                <Dumbbell className="h-4 w-4 text-primary" />
                {habit.title}
              </span>
              <Badge variant={habit.completed ? "default" : "outline"} className="text-[10px]">
                {habit.completed ? "Complete" : "Open"}
              </Badge>
            </CardTitle>
            <CardDescription>Foreground timer • {habit.time} • rest separation advisory</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex justify-between text-xs font-medium">
              <span>{valueLabel(habit)}</span>
              <span className="text-muted-foreground">{progressPercent(habit)}%</span>
            </div>
            <Progress value={progressPercent(habit)} className="h-2" />
            <Button
              variant={habit.completed ? "outline" : "default"}
              size="sm"
              onClick={() => onToggleHabit(habit.id)}
            >
              <CheckCircle2 className="mr-2 h-4 w-4" />
              {habit.completed ? "Mark incomplete" : "Mark complete"}
            </Button>
          </CardContent>
        </Card>
      ))}

      {water && (
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-base flex items-center gap-2">
              <Droplets className="h-4 w-4 text-secondary" />
              Hydration
            </CardTitle>
            <CardDescription>Paced across waking hours • hyponatremia guard at 1.2 L per hour</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="text-2xl font-bold">{valueLabel(water)}</div>
            <Progress value={progressPercent(water)} className="h-2" />
            <Button variant="outline" size="sm" onClick={() => onToggleHabit(water.id)}>
              <CheckCircle2 className="mr-2 h-4 w-4" />
              {water.completed ? "Mark incomplete" : "Mark complete"}
            </Button>
          </CardContent>
        </Card>
      )}

      {reading && (
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-base flex items-center gap-2">
              <BookOpen className="h-4 w-4 text-primary" />
              Reading
            </CardTitle>
            <CardDescription>10-page minimum session • key takeaway reflection</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="text-2xl font-bold">{valueLabel(reading)}</div>
            <Progress value={progressPercent(reading)} className="h-2" />
            <Button variant="outline" size="sm" onClick={() => onToggleHabit(reading.id)}>
              <CheckCircle2 className="mr-2 h-4 w-4" />
              {reading.completed ? "Mark incomplete" : "Mark complete"}
            </Button>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-base flex items-center gap-2">
            <Lock className="h-4 w-4 text-primary" />
            Progress Photo
          </CardTitle>
          <CardDescription>Hardware-encrypted vault • never exported to a public gallery</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="text-2xl font-bold">{photo ? valueLabel(photo) : "No photo today"}</div>
          <Button size="sm" onClick={onOpenVault}>
            <Camera className="mr-2 h-4 w-4" />
            Open vault
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}

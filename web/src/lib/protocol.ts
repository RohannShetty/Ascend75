export type HabitCategory = "Workout" | "Nutrition" | "Mindset" | "Hydration"

export interface Habit {
  id: string
  title: string
  category: HabitCategory
  time: string
  completed: boolean
  streak: number
  /** Current progress against the habit target, when the habit tracks a value. */
  value?: number
  target?: number
  unit?: string
}

export const HABIT_CATEGORIES: HabitCategory[] = ["Workout", "Nutrition", "Hydration", "Mindset"]

export const CHALLENGE_LENGTH_DAYS = 75

export const MILESTONE_DAYS = [1, 7, 14, 21, 30, 45, 60, 75]

/** In-memory prototype state; the Android build is the authoritative implementation. */
export const INITIAL_HABITS: Habit[] = [
  {
    id: "workout-1",
    title: "45-min Outdoor Endurance Session",
    category: "Workout",
    time: "07:00 AM",
    completed: true,
    streak: 18,
    value: 45,
    target: 45,
    unit: "min",
  },
  {
    id: "workout-2",
    title: "45-min Strength & Resistance Training",
    category: "Workout",
    time: "05:30 PM",
    completed: true,
    streak: 18,
    value: 45,
    target: 45,
    unit: "min",
  },
  {
    id: "diet",
    title: "Strict Caloric & Macro Plan Compliance",
    category: "Nutrition",
    time: "All Day",
    completed: true,
    streak: 18,
    value: 1,
    target: 1,
    unit: "check",
  },
  {
    id: "water",
    title: "3.8 L Pure Water Intake",
    category: "Hydration",
    time: "Continuous",
    completed: false,
    streak: 17,
    value: 2400,
    target: 3800,
    unit: "ml",
  },
  {
    id: "reading",
    title: "10 Pages Non-Fiction Reading",
    category: "Mindset",
    time: "09:30 PM",
    completed: false,
    streak: 22,
    value: 7,
    target: 10,
    unit: "pages",
  },
  {
    id: "photo",
    title: "Progress Photo (Encrypted Vault)",
    category: "Mindset",
    time: "Flexible",
    completed: false,
    streak: 18,
    value: 0,
    target: 1,
    unit: "photo",
  },
]

export function formatHours(hour: number): string {
  const normalized = ((hour % 24) + 24) % 24
  const display = normalized === 0 ? 12 : normalized > 12 ? normalized - 12 : normalized
  return `${display.toString().padStart(2, "0")}:00 ${normalized < 12 ? "AM" : "PM"}`
}

import { useState } from "react"
import { Download, ShieldCheck, Trash2 } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { RangeField } from "@/components/RangeField"
import { CHALLENGE_LENGTH_DAYS, formatHours, type Habit } from "@/lib/protocol"

const MEDICAL_DISCLAIMER =
  "Ascend 75 is an educational discipline and habit development tool. It is not a medical device. " +
  "Always consult a qualified physician prior to initiating strenuous fitness regimens or major hydration shifts."

const PROTOCOL_RULES = [
  "Two 45-minute workouts daily, at least one outdoors.",
  "Clean nutrition: no cheat meals, no alcohol.",
  "3.8 L of water paced across waking hours.",
  "10 pages of non-fiction reading.",
  "One progress photo into the encrypted vault.",
]

interface MoreViewProps {
  habits: Habit[]
  sleepCutoffHour: number
  onSleepCutoffChange: (hour: number) => void
  onWipe: () => void
}

export function MoreView({ habits, sleepCutoffHour, onSleepCutoffChange, onWipe }: MoreViewProps) {
  const [archiveReady, setArchiveReady] = useState(false)
  const [confirmingWipe, setConfirmingWipe] = useState(false)

  const exportArchive = () => {
    const archive = {
      schemaVersion: 1,
      exportTimestamp: new Date().toISOString(),
      challenge: { mode: "STRICT_75", dayNumber: 18, lengthDays: CHALLENGE_LENGTH_DAYS },
      sleepCutoffHour,
      habits,
    }
    const blob = new Blob([JSON.stringify(archive, null, 2)], { type: "application/json" })
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement("a")
    anchor.href = url
    anchor.download = `ascend75-export-${Date.now()}.json`
    anchor.click()
    URL.revokeObjectURL(url)
    setArchiveReady(true)
  }

  return (
    <div className="space-y-4">
      <div>
        <p className="text-[10px] font-semibold uppercase tracking-wider text-primary">Vault &amp; Preferences</p>
        <h1 className="text-2xl font-bold tracking-tight">More</h1>
      </div>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-xs uppercase tracking-wider text-secondary">Day Boundary</CardTitle>
          <CardDescription>
            Sleep Cutoff: {formatHours(sleepCutoffHour)} — habits completed before this timestamp credit to the previous day.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <RangeField
            label="Sleep cutoff hour"
            value={sleepCutoffHour}
            min={0}
            max={6}
            valueLabel={formatHours(sleepCutoffHour)}
            onValueChange={onSleepCutoffChange}
          />
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-xs uppercase tracking-wider text-primary">Protocol Rules</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-2 text-sm text-muted-foreground">
            {PROTOCOL_RULES.map((rule) => (
              <li key={rule} className="flex gap-2">
                <ShieldCheck className="mt-0.5 h-4 w-4 shrink-0 text-primary" />
                {rule}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-xs uppercase tracking-wider text-primary">Data Sovereignty &amp; Export</CardTitle>
          <CardDescription>Export the full session as portable JSON.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button variant="outline" className="w-full" onClick={exportArchive}>
            <Download className="mr-2 h-4 w-4" />
            Export JSON Archive
          </Button>
          {archiveReady && <p className="text-xs text-primary">Archive generated ({habits.length} objectives).</p>}
        </CardContent>
      </Card>

      <Card className="border-destructive/40 bg-destructive/5">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs uppercase tracking-wider text-destructive">Danger Zone</CardTitle>
          <CardDescription>
            Permanently zero-fill and delete encrypted photos, truncate every store, and reset the app.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {confirmingWipe ? (
            <>
              <p className="text-sm text-destructive">This action is irreversible.</p>
              <div className="flex gap-2">
                <Button
                  variant="destructive"
                  className="flex-1"
                  onClick={() => {
                    onWipe()
                    setConfirmingWipe(false)
                  }}
                >
                  Permanently wipe everything
                </Button>
                <Button variant="outline" className="flex-1" onClick={() => setConfirmingWipe(false)}>
                  Cancel
                </Button>
              </div>
            </>
          ) : (
            <Button variant="outline" className="w-full" onClick={() => setConfirmingWipe(true)}>
              <Trash2 className="mr-2 h-4 w-4" />
              Cryptographic Data Wipe
            </Button>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-xs uppercase tracking-wider text-secondary">About</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm text-muted-foreground">
          <p>Ascend 75 — offline-first, no accounts, no telemetry, no network calls.</p>
          <p className="text-xs">{MEDICAL_DISCLAIMER}</p>
        </CardContent>
      </Card>
    </div>
  )
}

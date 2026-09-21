import { useState } from "react"
import { Camera, Lock } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { RangeField } from "@/components/RangeField"

const CAPTURED_DAYS = [18, 14, 11, 7, 4, 2, 1]

export function VaultView() {
  const [isLocked, setIsLocked] = useState(true)
  const [split, setSplit] = useState(50)

  if (isLocked) {
    return (
      <div className="flex min-h-[70vh] flex-col items-center justify-center gap-4 text-center">
        <div className="flex h-20 w-20 items-center justify-center rounded-full bg-accent">
          <Lock className="h-9 w-9 text-primary" />
        </div>
        <h1 className="text-2xl font-bold tracking-tight">Encrypted Photo Vault</h1>
        <p className="max-w-sm text-sm text-muted-foreground">
          Progress photos are protected by hardware-backed AES-256-GCM encryption and never leave the device.
        </p>
        <Button onClick={() => setIsLocked(false)}>Unlock vault</Button>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-wider text-primary">Biometric Vault</p>
          <h1 className="text-2xl font-bold tracking-tight">Transformation Timeline</h1>
        </div>
        <Button variant="outline" size="sm" onClick={() => setIsLocked(true)}>
          Lock
        </Button>
      </div>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-xs uppercase tracking-wider text-secondary">Before &amp; After Comparison</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="relative h-56 overflow-hidden rounded-xl border border-border bg-[#0a0e17]">
            <div className="absolute inset-0 flex items-center justify-center bg-[#262a34] text-xs text-muted-foreground">
              Day 1 Baseline
            </div>
            <div
              className="absolute inset-y-0 left-0 flex items-center justify-center overflow-hidden bg-[#1c1f29] text-xs text-primary"
              style={{ width: `${split}%` }}
            >
              <span className="whitespace-nowrap px-2">Day {CAPTURED_DAYS[0]} Current</span>
            </div>
          </div>
          <RangeField
            label="Split position"
            value={split}
            min={0}
            max={100}
            valueLabel={`${split}%`}
            onValueChange={setSplit}
          />
        </CardContent>
      </Card>

      <Button className="w-full">
        <Camera className="mr-2 h-4 w-4" />
        Capture Day 18 Photo
      </Button>

      <div>
        <p className="mb-3 text-sm font-medium">Historical Photo Log</p>
        <div className="grid grid-cols-3 gap-2">
          {CAPTURED_DAYS.map((day) => (
            <div
              key={day}
              className="flex aspect-square items-center justify-center rounded-lg border border-border bg-accent text-xs text-muted-foreground"
            >
              Day {day}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

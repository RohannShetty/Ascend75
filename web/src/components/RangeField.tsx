interface RangeFieldProps {
  label: string
  value: number
  min: number
  max: number
  step?: number
  valueLabel?: string
  onValueChange: (value: number) => void
}

/**
 * Native range input — the prototype deliberately adds no new dependency for this control.
 */
export function RangeField({ label, value, min, max, step = 1, valueLabel, onValueChange }: RangeFieldProps) {
  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between text-xs">
        <span className="text-muted-foreground">{label}</span>
        {valueLabel ? <span className="font-medium text-foreground">{valueLabel}</span> : null}
      </div>
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        aria-label={label}
        style={{ accentColor: "var(--primary)" }}
        onChange={(event) => onValueChange(Number(event.target.value))}
        className="h-2 w-full cursor-pointer appearance-none rounded-full bg-muted"
      />
    </div>
  )
}

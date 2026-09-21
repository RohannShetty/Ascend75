// Mirrors the Android science-card asset into the web prototype's data module.
// Usage: node tools/sync_web_science_cards.mjs
import fs from "node:fs"

const source = JSON.parse(fs.readFileSync("core/database/src/main/assets/science_cards.json", "utf8"))

const cards = source.map((card) => ({
  day: card.dayNumber,
  title: card.title.replace(/^Day \d+:\s*/, ""),
  category: card.category,
  summary: card.summary,
  action: card.actionItem.replace(/^Day \d+ Protocol:\s*/, "")
}))

const output = `// Generated from core/database/src/main/assets/science_cards.json so the prototype mirrors the app.
// Regenerate with: node tools/sync_web_science_cards.mjs

export interface ScienceCard {
  day: number
  title: string
  category: string
  summary: string
  action: string
}

export const studyCategories = ["ALL", "CIRCADIAN", "FOCUS", "DOPAMINE", "RECOVERY", "HABITS"]

export const scienceCards: ScienceCard[] = ${JSON.stringify(cards, null, 2)}
`

fs.writeFileSync("web/src/data/scienceCards.ts", output)
console.log(`wrote ${cards.length} cards to web/src/data/scienceCards.ts`)

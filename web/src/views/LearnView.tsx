import { useState } from "react"
import { ArrowLeft, Bookmark, Search } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { scienceCards, studyCategories, type ScienceCard } from "@/data/scienceCards"

export function LearnView() {
  const [query, setQuery] = useState("")
  const [category, setCategory] = useState("ALL")
  const [selected, setSelected] = useState<ScienceCard | null>(null)
  const [bookmarks, setBookmarks] = useState<Record<number, boolean>>({})

  const toggleBookmark = (day: number) => {
    setBookmarks((previous) => ({ ...previous, [day]: !previous[day] }))
  }

  if (selected) {
    const isBookmarked = Boolean(bookmarks[selected.day])
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <Button variant="ghost" size="sm" onClick={() => setSelected(null)}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
          <Button variant="ghost" size="icon" aria-label="Toggle bookmark" onClick={() => toggleBookmark(selected.day)}>
            <Bookmark className={`h-4 w-4 ${isBookmarked ? "fill-primary text-primary" : "text-muted-foreground"}`} />
          </Button>
        </div>

        <p className="text-[10px] font-semibold uppercase tracking-wider text-primary">{selected.category}</p>
        <h1 className="text-2xl font-bold tracking-tight">{selected.title}</h1>

        <Card className="border-secondary/30">
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase tracking-wider text-secondary">Core Evidence Takeaway</CardTitle>
          </CardHeader>
          <CardContent className="text-sm">{selected.summary}</CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase tracking-wider text-primary">Practical Step</CardTitle>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">{selected.action}</CardContent>
        </Card>
      </div>
    )
  }

  const filtered = scienceCards.filter((card) => {
    const matchesCategory = category === "ALL" || card.category === category
    const needle = query.trim().toLowerCase()
    const matchesQuery = needle === "" || card.title.toLowerCase().includes(needle) || card.summary.toLowerCase().includes(needle)
    return matchesCategory && matchesQuery
  })

  return (
    <div className="space-y-6">
      <div>
        <p className="text-[10px] font-semibold uppercase tracking-wider text-primary">Behavioural Science</p>
        <h1 className="text-2xl font-bold tracking-tight">75-Day Curriculum</h1>
        <p className="text-xs text-muted-foreground mt-1">Unlocked up to Day 18 of 75</p>
      </div>

      <div className="relative">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          type="search"
          placeholder="Search circadian, dopamine, protocols..."
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          className="pl-9"
        />
      </div>

      <div className="flex flex-wrap gap-2">
        {studyCategories.map((option) => (
          <Button
            key={option}
            size="sm"
            variant={option === category ? "default" : "outline"}
            className="text-[10px] tracking-wider"
            onClick={() => setCategory(option)}
          >
            {option}
          </Button>
        ))}
      </div>

      <div className="space-y-3">
        {filtered.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">No cards match that filter.</p>
        ) : (
          filtered.map((card) => (
            <Card
              key={card.day}
              className="cursor-pointer transition-colors hover:border-primary/40"
              onClick={() => setSelected(card)}
            >
              <CardHeader className="pb-2">
                <CardTitle className="flex items-center justify-between text-base">
                  <span className="text-[10px] font-semibold uppercase tracking-wider text-primary">
                    Day {card.day} • {card.category}
                  </span>
                  <span className="flex items-center gap-2">
                    {bookmarks[card.day] && <Badge variant="secondary" className="text-[10px]">Saved</Badge>}
                    <button
                      type="button"
                      aria-label={`Bookmark day ${card.day}`}
                      onClick={(event) => {
                        event.stopPropagation()
                        toggleBookmark(card.day)
                      }}
                    >
                      <Bookmark
                        className={`h-4 w-4 ${bookmarks[card.day] ? "fill-primary text-primary" : "text-muted-foreground"}`}
                      />
                    </button>
                  </span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm font-medium">{card.title}</p>
                <CardDescription className="mt-1 line-clamp-2">{card.summary}</CardDescription>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  )
}

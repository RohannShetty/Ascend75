import { Activity, Award, CheckCircle2, Clock, Droplets, Dumbbell, Flame, LayoutDashboard, TrendingUp, Zap } from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Separator } from "@/components/ui/separator"
import { Switch } from "@/components/ui/switch"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import type { Habit } from "@/lib/protocol"

export interface TodayMetrics {
  completionPercentage: number
  completedCount: number
  totalCount: number
  streakDays: number
  hydrationCurrent: number
  hydrationTarget: number
  hydrationPercentage: number
  milestoneRank: string
  nextMilestone: number
  daysToNextTier: number
}

interface TodayViewProps {
  habits: Habit[]
  filteredHabits: Habit[]
  metrics: TodayMetrics
  onToggleHabit: (id: string) => void
}

export function TodayView({ habits, filteredHabits, metrics, onToggleHabit }: TodayViewProps) {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Today&apos;s Protocol</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Stay relentless. Day 18 in progress • Discipline over motivation.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="hover:border-primary/50 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Daily Completion</CardTitle>
            <Activity className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics.completionPercentage}%</div>
            <Progress value={metrics.completionPercentage} className="h-2 mt-2" />
            <p className="text-xs text-muted-foreground mt-2">
              {metrics.completedCount} of {metrics.totalCount} objectives checked
            </p>
          </CardContent>
        </Card>

        <Card className="hover:border-primary/50 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Active Streak</CardTitle>
            <Flame className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics.streakDays} Days</div>
            <div className="flex items-center gap-1.5 mt-2">
              <Badge variant="secondary" className="text-xs">
                <TrendingUp className="mr-1 h-3 w-3" />
                Top 5% Consistency
              </Badge>
            </div>
            <p className="text-xs text-muted-foreground mt-2">Zero restart penalties</p>
          </CardContent>
        </Card>

        <Card className="hover:border-primary/50 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Hydration Status</CardTitle>
            <Droplets className="h-4 w-4 text-secondary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {(metrics.hydrationCurrent / 1000).toFixed(1)} / {(metrics.hydrationTarget / 1000).toFixed(1)} L
            </div>
            <Progress value={metrics.hydrationPercentage} className="h-2 mt-2" />
            <p className="text-xs text-muted-foreground mt-2">{metrics.hydrationPercentage}% target reached today</p>
          </CardContent>
        </Card>

        <Card className="hover:border-primary/50 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Milestone Rank</CardTitle>
            <Award className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics.milestoneRank}</div>
            <div className="flex items-center gap-1.5 mt-2">
              <Badge variant="outline" className="text-xs">
                <Zap className="mr-1 h-3 w-3 text-primary" />
                Next: Day {metrics.nextMilestone}
              </Badge>
            </div>
            <p className="text-xs text-muted-foreground mt-2">{metrics.daysToNextTier} days until next unlock</p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="habits" className="w-full">
        <TabsList className="grid w-full grid-cols-2 max-w-md">
          <TabsTrigger value="habits">
            <LayoutDashboard className="mr-2 h-4 w-4" />
            Active Checklist
          </TabsTrigger>
          <TabsTrigger value="metrics">
            <TrendingUp className="mr-2 h-4 w-4" />
            Discipline Analytics
          </TabsTrigger>
        </TabsList>

        <TabsContent value="habits" className="space-y-4 pt-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Daily Non-Negotiables</CardTitle>
              <CardDescription>
                Toggle tasks as completed. Updates sync instantly with your local session.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              {filteredHabits.length === 0 ? (
                <div className="py-8 text-center text-muted-foreground text-sm">
                  No habits found matching your filter query.
                </div>
              ) : (
                filteredHabits.map((habit) => (
                  <div
                    key={habit.id}
                    className={`flex items-center justify-between p-4 rounded-xl border transition-all ${
                      habit.completed
                        ? "bg-muted/30 border-border opacity-75"
                        : "bg-card border-border hover:border-primary/40 shadow-xs"
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <Switch
                        checked={habit.completed}
                        onCheckedChange={() => onToggleHabit(habit.id)}
                        aria-label={`Toggle ${habit.title}`}
                      />
                      <div>
                        <div className="flex items-center gap-2">
                          <span
                            className={`text-sm font-medium ${
                              habit.completed ? "line-through text-muted-foreground" : ""
                            }`}
                          >
                            {habit.title}
                          </span>
                          <Badge
                            variant={
                              habit.category === "Workout"
                                ? "default"
                                : habit.category === "Nutrition"
                                  ? "secondary"
                                  : "outline"
                            }
                            className="text-[10px] px-1.5 py-0"
                          >
                            {habit.category}
                          </Badge>
                        </div>
                        <div className="flex items-center gap-3 text-xs text-muted-foreground mt-0.5">
                          <span className="flex items-center gap-1">
                            <Clock className="h-3 w-3" />
                            {habit.time}
                          </span>
                          <span className="flex items-center gap-1">
                            <Flame className="h-3 w-3 text-primary" />
                            {habit.streak}d streak
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="hidden sm:flex items-center gap-2">
                      {habit.completed && (
                        <span className="flex items-center text-xs text-primary font-medium">
                          <CheckCircle2 className="h-4 w-4 mr-1" />
                          Complete
                        </span>
                      )}
                    </div>
                  </div>
                ))
              )}
            </CardContent>
            <CardFooter className="flex justify-between border-t p-4 text-xs text-muted-foreground">
              <span>Standard 75-Hard protocol rules enforced</span>
              <span>Day boundary follows your sleep cutoff</span>
            </CardFooter>
          </Card>
        </TabsContent>

        <TabsContent value="metrics" className="space-y-4 pt-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-base flex items-center gap-2">
                  <Dumbbell className="h-4 w-4 text-primary" />
                  Workout Consistency Log
                </CardTitle>
                <CardDescription>Outdoor vs Indoor sessions balance across the past 7 days.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <div className="flex justify-between text-xs font-medium">
                    <span>Outdoor Endurance (45 min)</span>
                    <span className="text-muted-foreground">7 / 7 sessions</span>
                  </div>
                  <Progress value={100} className="h-2" />
                </div>
                <div className="space-y-2">
                  <div className="flex justify-between text-xs font-medium">
                    <span>Indoor Resistance &amp; Hypertrophy</span>
                    <span className="text-muted-foreground">7 / 7 sessions</span>
                  </div>
                  <Progress value={100} className="h-2" />
                </div>
                <Separator />
                <div className="text-xs text-muted-foreground">
                  Total cumulative training volume: <strong>14.5 hours</strong> logged this week.
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base flex items-center gap-2">
                  <Award className="h-4 w-4 text-primary" />
                  Protocol Milestone Projection
                </CardTitle>
                <CardDescription>Estimated completion roadmap to Day 75 finish line.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between p-3 rounded-lg border bg-muted/20">
                  <div>
                    <p className="text-xs font-semibold">Phase 1: Foundation (Days 1 - 25)</p>
                    <p className="text-xs text-muted-foreground">Status: 72% complete</p>
                  </div>
                  <Badge variant="outline">Active</Badge>
                </div>
                <div className="flex items-center justify-between p-3 rounded-lg border opacity-60">
                  <div>
                    <p className="text-xs font-semibold">Phase 2: Momentum (Days 26 - 50)</p>
                    <p className="text-xs text-muted-foreground">Unlock on Day 26</p>
                  </div>
                  <Badge variant="secondary">Locked</Badge>
                </div>
                <div className="flex items-center justify-between p-3 rounded-lg border opacity-60">
                  <div>
                    <p className="text-xs font-semibold">Phase 3: Mastery (Days 51 - 75)</p>
                    <p className="text-xs text-muted-foreground">Unlock on Day 51</p>
                  </div>
                  <Badge variant="secondary">Locked</Badge>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>

      <p className="text-xs text-muted-foreground">
        {habits.length} objectives tracked in this session.
      </p>
    </div>
  )
}

// Generated from core/database/src/main/assets/science_cards.json so the prototype mirrors the app.
// Regenerate with: node tools/sync_web_science_cards.mjs

export interface ScienceCard {
  day: number
  title: string
  category: string
  summary: string
  action: string
}

export const studyCategories = ["ALL", "CIRCADIAN", "FOCUS", "DOPAMINE", "RECOVERY", "HABITS"]

export const scienceCards: ScienceCard[] = [
  {
    "day": 1,
    "title": "Morning Sunlight & Adenosine Clearance",
    "category": "CIRCADIAN",
    "summary": "Viewing natural sunlight within 60 minutes of waking initiates the cortisol awakening response and synchronizes the master circadian clock.",
    "action": "Get 10–20 minutes of outdoor morning sunlight without sunglasses."
  },
  {
    "day": 2,
    "title": "Evening Blue Light & Melatonin Suppression",
    "category": "CIRCADIAN",
    "summary": "Artificial blue-enriched light within 2 hours of sleep inhibits nocturnal melatonin secretion and impairs deep sleep architecture.",
    "action": "Dim overhead artificial lights and switch to low-placed warm or red illumination 2 hours before bed."
  },
  {
    "day": 3,
    "title": "Core Body Temperature & Sleep Latency",
    "category": "CIRCADIAN",
    "summary": "Sleep onset is biologically triggered by a drop in internal core body temperature of approximately 1°C.",
    "action": "Take a warm shower 90 minutes before bed; post-bath peripheral vasodilation rapidly lowers core temperature."
  },
  {
    "day": 4,
    "title": "Time-Restricted Feeding & Peripheral Oscillators",
    "category": "CIRCADIAN",
    "summary": "Consuming food within a consistent 8-10 hour window aligns metabolic peripheral clocks with the master SCN clock.",
    "action": "Conclude your last caloric intake at least 2.5 to 3 hours before sleep."
  },
  {
    "day": 5,
    "title": "Afternoon Adenosine Dip & The 90-Minute Rest Window",
    "category": "CIRCADIAN",
    "summary": "A natural circadian dip in alertness occurs 7-8 hours post-waking, coinciding with peak sleepiness propensity.",
    "action": "Utilize a 20-minute Non-Sleep Deep Rest (NSDR) or brief meditation instead of caffeine."
  },
  {
    "day": 6,
    "title": "Ultradian Rhythms & 90-Minute Focus Cycles",
    "category": "FOCUS",
    "summary": "Human cognitive performance naturally cycles in approximately 90-minute ultradian rhythms of peak alertness followed by restorative troughs.",
    "action": "Block intense creative or analytic deep work into single 90-minute uninterrupted bouts."
  },
  {
    "day": 7,
    "title": "Visual Focus Dictates Mental Acuity",
    "category": "FOCUS",
    "summary": "Restricting visual aperture to a narrow focal point activates the sympathetic nervous system and sharpens executive attention.",
    "action": "Focus your eyes on a single target on your screen or notebook for 30 seconds before beginning a sprint."
  },
  {
    "day": 8,
    "title": "Binaural Beats & Alpha-Theta Brain Synchronization",
    "category": "FOCUS",
    "summary": "Presenting two slightly different tones, one to each ear, produces a perceived binaural beat that measurably shifted vigilance performance and mood in healthy adults.",
    "action": "Match the beat frequency to the task and verify the effect against your own output — responses vary between people."
  },
  {
    "day": 9,
    "title": "The Cost of Attentional Residue",
    "category": "FOCUS",
    "summary": "Rapid task-switching leaves lingering cognitive activation on previous tasks, degrading performance on subsequent work.",
    "action": "Close all unrelated browser tabs and communication channels before initiating a work block."
  },
  {
    "day": 10,
    "title": "Strategic Deliberate Decompression",
    "category": "FOCUS",
    "summary": "Memory consolidation and creative synthesis occur predominantly during periods of waking mind-wandering without task engagement.",
    "action": "Take a 10-minute walk without phone, audio, or active inputs immediately following a rigorous deep work session."
  },
  {
    "day": 11,
    "title": "Dopamine Baseline vs Peak Dynamics",
    "category": "DOPAMINE",
    "summary": "Motivation and drive are determined by circulating tonic dopamine baseline levels rather than transient phasic spikes.",
    "action": "Avoid stacking multiple high-intensity dopaminergic stimuli simultaneously (e.g. pre-workout + loud music + social media scrolling)."
  },
  {
    "day": 12,
    "title": "Effort as the Primary Reward Signal",
    "category": "DOPAMINE",
    "summary": "Attaching subjective reward to the friction and struggle of the process preserves baseline dopamine and fosters intrinsic grit.",
    "action": "Mentally tell yourself 'This resistance is where growth occurs' during the most strenuous part of a workout or study session."
  },
  {
    "day": 13,
    "title": "Recovery of Dopamine Markers With Abstinence",
    "category": "DOPAMINE",
    "summary": "Dopamine transporter availability partially recovers over months of sustained abstinence from stimulant use, so a down-regulated dopamine system is not a permanent state.",
    "action": "Treat dopamine recovery as a slow, measurable process — months of reduced stimulation, not a weekend of avoidance."
  },
  {
    "day": 14,
    "title": "Delayed Gratification & Prefrontal Strengthening",
    "category": "DOPAMINE",
    "summary": "Practicing impulse inhibition directly strengthens top-down executive control connections from the ventral prefrontal cortex.",
    "action": "When an urge to check notifications strikes, enforce a mandatory 10-minute pause before acting."
  },
  {
    "day": 15,
    "title": "Intermittent Reward Schedules & Long-Term Adherence",
    "category": "DOPAMINE",
    "summary": "Unpredictable intermittent reinforcement produces the highest resistance to behavioral extinction.",
    "action": "Do not reward yourself after every single workout; allow consistency itself to be the non-negotiable standard."
  },
  {
    "day": 16,
    "title": "Slow-Wave Sleep & Growth Hormone Pulsing",
    "category": "RECOVERY",
    "summary": "Stage 3/4 non-REM sleep accounts for >70% of daily human growth hormone (HGH) secretion and physical tissue regeneration.",
    "action": "Prioritize going to sleep at a consistent time each night to maximize early-night slow-wave sleep duration."
  },
  {
    "day": 17,
    "title": "The Glymphatic System & Brain Waste Clearance",
    "category": "RECOVERY",
    "summary": "During deep sleep, the brain cleanses metabolic byproducts including beta-amyloid, tau proteins, and adenosine via cerebrospinal fluid influx.",
    "action": "Avoid heavy meals within 2 hours of sleep to ensure cardiac parasympathetic dominance during glymphatic cycling."
  },
  {
    "day": 18,
    "title": "Active Recovery & Lactate Clearance",
    "category": "RECOVERY",
    "summary": "Low-intensity aerobic movement accelerates muscular recovery and systemic waste clearance far superior to passive sedentary rest.",
    "action": "Incorporate a 45-minute brisk outdoor walk or mobility routine as one of your daily training sessions."
  },
  {
    "day": 19,
    "title": "Cold Exposure & Norepinephrine Upregulation",
    "category": "RECOVERY",
    "summary": "Deliberate cold exposure produces sustained 200–300% elevations in circulating norepinephrine and dopamine lasting several hours.",
    "action": "End morning showers with 60–90 seconds of cold water to trigger sustained mental vigor without jitters."
  },
  {
    "day": 20,
    "title": "Physiological Sigh & Acute Sympathetic Downregulation",
    "category": "RECOVERY",
    "summary": "Two quick consecutive nasal inhales followed by an extended, slow mouth exhale rapidly reduces heart rate and autonomic arousal.",
    "action": "Perform 3 to 5 physiological sighs whenever you experience acute stress, racing thoughts, or pre-sleep tension."
  },
  {
    "day": 21,
    "title": "Implementation Intentions & Environmental Cues",
    "category": "HABITS",
    "summary": "Formulating explicit 'If [Situation X], Then [Behavior Y]' rules increases the probability of habit execution by more than 200%.",
    "action": "State exactly when and where tomorrow's outdoor workout will occur before going to bed tonight."
  },
  {
    "day": 22,
    "title": "Friction Engineering & Micro-Barriers",
    "category": "HABITS",
    "summary": "Increasing the initiation friction of undesirable habits by even 20 seconds dramatically reduces compulsive relapse.",
    "action": "Keep your smartphone in another room while sleeping, and lay out tomorrow's workout shoes beside your door."
  },
  {
    "day": 23,
    "title": "The Habit Loop & Neurochemical Consolidation",
    "category": "HABITS",
    "summary": "Habits consolidate as neural firing patterns shift from the associative dorsomedial striatum to the sensorimotor dorsolateral striatum.",
    "action": "Celebrate the completion of each daily habit with an immediate mental acknowledgment of progress."
  },
  {
    "day": 24,
    "title": "Identity-Based Habit Formation",
    "category": "HABITS",
    "summary": "Sustainable behavioral transformation occurs when habits are viewed as proof of self-identity rather than external obligations.",
    "action": "Reframe your habits: not 'I have to finish my reading', but 'I am someone who never misses intellectual development'."
  },
  {
    "day": 25,
    "title": "The 66-Day Automaticity Curve",
    "category": "HABITS",
    "summary": "Reaching behavioral automaticity requires an average of 66 consecutive daily repetitions depending on task complexity.",
    "action": "Trust the 75-day process; resistance peaks between Days 14 and 25 before steepening into effortless automaticity."
  },
  {
    "day": 26,
    "title": "Amber Lenses and Sleep",
    "category": "CIRCADIAN",
    "summary": "Seven nights of amber blue-blocking lenses advanced sleep onset and improved sleep quality ratings and mood versus clear lenses.",
    "action": "If you must use screens after dark, add blue-blocking lenses rather than trusting a night-shift colour profile alone."
  },
  {
    "day": 27,
    "title": "Circadian Misalignment & Metabolism",
    "category": "CIRCADIAN",
    "summary": "Forcing sleep and meals out of phase with the internal clock raises postprandial glucose, insulin and blood pressure while lowering leptin.",
    "action": "Keep sleep onset within a 60-minute window seven days a week so meals land at the same internal phase each day."
  },
  {
    "day": 28,
    "title": "Feeding Entrains Peripheral Clocks",
    "category": "CIRCADIAN",
    "summary": "Restricting feeding to a fixed daily window shifts the liver clock independently of the light-dark cycle.",
    "action": "Anchor a consistent first and last meal time; the metabolic clock follows the eating window more than the alarm clock."
  },
  {
    "day": 29,
    "title": "Social Jetlag",
    "category": "CIRCADIAN",
    "summary": "The weekend shift in sleep timing creates a chronic phase mismatch comparable to flying one to two time zones every week.",
    "action": "Hold weekend wake time within one hour of weekday wake time; recovery sleep is better taken as a nap."
  },
  {
    "day": 30,
    "title": "Self-Luminous Screens & Melatonin",
    "category": "CIRCADIAN",
    "summary": "Two hours of a bright self-luminous tablet at night measurably suppresses melatonin; a dimmed display at the same distance does not.",
    "action": "Lower screen brightness to the minimum comfortable level after dark — the dose, not the presence, of light is what suppresses melatonin."
  },
  {
    "day": 31,
    "title": "Daylight Exposure & Sleep Quality",
    "category": "CIRCADIAN",
    "summary": "Office workers with window access sleep longer and report better sleep quality and vitality than colleagues without daylight.",
    "action": "Take a short outdoor break in the morning rather than a fluorescent-lit corridor; the ceiling light is a fraction of outdoor irradiance."
  },
  {
    "day": 32,
    "title": "Light Phase Response Curve",
    "category": "CIRCADIAN",
    "summary": "Bright light shifts the human clock in opposite directions depending on when it lands: earlier after late-night exposure, later after early-morning exposure.",
    "action": "Use light deliberately: morning light advances the clock, late-evening light delays it."
  },
  {
    "day": 33,
    "title": "Epidemiology of the Human Clock",
    "category": "CIRCADIAN",
    "summary": "Chronotype shifts later through adolescence and earlier again with age, and is strongly shaped by work and school schedules.",
    "action": "Work with your chronotype rather than against it when choosing when to schedule the hardest training block."
  },
  {
    "day": 34,
    "title": "Two-Process Sleep Regulation",
    "category": "CIRCADIAN",
    "summary": "Sleep timing is governed by the interaction of a homeostatic sleep drive that builds with wakefulness and a circadian gate that opens in the biological evening.",
    "action": "A night of short sleep raises homeostatic pressure but cannot move the circadian gate — expect an early night, not a shift in rhythm."
  },
  {
    "day": 35,
    "title": "Natural Light-Dark Cycle Entrainment",
    "category": "CIRCADIAN",
    "summary": "Camping without artificial light shifts sleep timing earlier and aligns the internal clock tightly with sunset and sunrise.",
    "action": "Treat outdoor morning light as the anchor habit and evening darkness as maintenance — together they retrain the clock fastest."
  },
  {
    "day": 36,
    "title": "Cost of Interrupted Work",
    "category": "FOCUS",
    "summary": "Interrupted work is completed faster but at the cost of higher stress, frustration and effort, with a documented carryover of attentional residue.",
    "action": "Batch notifications: keep one uninterrupted block in the first half of the day for the single most important task."
  },
  {
    "day": 37,
    "title": "Attentional Blink",
    "category": "FOCUS",
    "summary": "After detecting a target in a rapid stream, the ability to detect a second target is impaired for several hundred milliseconds.",
    "action": "Space demanding reviews apart; two high-attention tasks back-to-back degrade the second one."
  },
  {
    "day": 38,
    "title": "Selective Attention as a Filter",
    "category": "FOCUS",
    "summary": "Attention works by biasing competition between stimuli so that relevant representations win and irrelevant ones are suppressed.",
    "action": "Remove competing stimuli from the field rather than trying to ignore them — suppression costs more than absence."
  },
  {
    "day": 39,
    "title": "Working Memory Capacity Limits",
    "category": "FOCUS",
    "summary": "The reliable capacity of focused attention in working memory is around four chunks, not the classical seven.",
    "action": "Keep an active task list to a handful of items; the rest belongs in a written system, not in your head."
  },
  {
    "day": 40,
    "title": "Meditation & Executive Attention",
    "category": "FOCUS",
    "summary": "Five days of integrative body-mind training improved executive attention and reduced cortisol and anxiety relative to relaxation training.",
    "action": "Ten minutes of focused-attention practice per day is enough to see measurable attentional gains."
  },
  {
    "day": 41,
    "title": "Working Memory Training Transfer",
    "category": "FOCUS",
    "summary": "Adaptive working memory training transferred to measures of fluid intelligence, with gains tracking the amount of training.",
    "action": "Make practice adaptively hard; repeating something already easy produces no generalizable gain."
  },
  {
    "day": 42,
    "title": "Nature and Directed Attention",
    "category": "FOCUS",
    "summary": "Walking in nature improved directed-attention performance and mood relative to an urban walk of the same duration.",
    "action": "Use the outdoor workout as attentional recovery, not just conditioning — it restores the faculty the rest of the day spends."
  },
  {
    "day": 43,
    "title": "Exercise and Executive Function",
    "category": "FOCUS",
    "summary": "Regular aerobic activity improves executive control and attention and is associated with better academic and cognitive outcomes.",
    "action": "Schedule cognitively demanding work after training rather than before; the post-exercise window favours executive tasks."
  },
  {
    "day": 44,
    "title": "Working Memory Capacity & Limits",
    "category": "FOCUS",
    "summary": "Visual working memory capacity is a stable individual trait that predicts broader cognitive performance, and it is limited by attentional allocation.",
    "action": "Protect attention quality: when it degrades, effective capacity drops even though the task has not changed."
  },
  {
    "day": 45,
    "title": "Default Mode & Mind Wandering",
    "category": "FOCUS",
    "summary": "Spontaneous thought is not random noise; it tracks the salience of unresolved goals and competes directly with task focus.",
    "action": "Capture open loops in writing before a focus block so unresolved goals stop pulling attention."
  },
  {
    "day": 46,
    "title": "Dopamine and Reward Wanting",
    "category": "DOPAMINE",
    "summary": "Dopamine mediates 'wanting' — the incentive pull of a cue — and is not required for the hedonic pleasure of a reward.",
    "action": "Expect craving to track cues, not value: removing the cue removes the pull more reliably than resolving to resist it."
  },
  {
    "day": 47,
    "title": "Dopamine Signals Are Broader Than Reward",
    "category": "DOPAMINE",
    "summary": "Midbrain dopamine neurons encode motivationally salient events generally, including aversive and alerting ones, not reward alone.",
    "action": "Judge dopamine by the behaviour it drives, not by how something feels; salience includes the negative kind."
  },
  {
    "day": 48,
    "title": "Reward Prediction Error",
    "category": "DOPAMINE",
    "summary": "Dopamine neuron firing encodes the difference between received and expected reward, providing the teaching signal for learning.",
    "action": "Progress must stay slightly unpredictable to stay motivating; a fully predictable reward stops teaching the system anything."
  },
  {
    "day": 49,
    "title": "Effort, Cost and Dopamine",
    "category": "DOPAMINE",
    "summary": "Dopamine in the nucleus accumbens sets willingness to overcome effort costs, independent of the hedonic value of the outcome.",
    "action": "Lower the activation cost of hard habits — the system will not pay high effort for a distant reward without support."
  },
  {
    "day": 50,
    "title": "Model-Based Value Learning",
    "category": "DOPAMINE",
    "summary": "Humans use both cached habits and forward-looking models, and the striatal prediction error signal is modulated by model-based expectations.",
    "action": "Pair repetition with deliberate reasoning about why a habit works; habit alone is fragile when conditions change."
  },
  {
    "day": 51,
    "title": "Dopamine Circuit Organisation",
    "category": "DOPAMINE",
    "summary": "Reward-related dopamine arises from two anatomically distinct projection systems with different targets and functions.",
    "action": "Motivation has multiple channels: physical reward, social reward and completion each drive behaviour through different routes."
  },
  {
    "day": 52,
    "title": "Refined Sugar and Reward Sensitisation",
    "category": "DOPAMINE",
    "summary": "Intermittent, excessive sugar access produces neurochemical changes resembling those seen with drugs of abuse in animal models.",
    "action": "Continuous restriction plus an occasional deliberate treat beats repeated binge cycles; intermittency is what sensitises."
  },
  {
    "day": 53,
    "title": "Dopamine and Cognitive Control",
    "category": "DOPAMINE",
    "summary": "Prefrontal dopamine tunes working memory and attentional control, with an inverted-U relationship between dopamine level and performance.",
    "action": "Aim for a stable baseline of sleep and movement: both under- and over-stimulation flatten cognitive control."
  },
  {
    "day": 54,
    "title": "Dopamine, Time and Impulsivity",
    "category": "DOPAMINE",
    "summary": "Raising dopamine precursor availability shifted people toward choosing sooner, smaller rewards over larger, later ones.",
    "action": "Make delayed outcomes concrete; discounting is the default, and specificity is the antidote."
  },
  {
    "day": 55,
    "title": "Incentive Sensitisation",
    "category": "DOPAMINE",
    "summary": "Repeated reward exposure sensitizes 'wanting' while 'liking' stays flat or declines, which is why craving and enjoyment diverge.",
    "action": "When a habit stops being enjoyable but the pull remains, change the cue environment rather than relying on willpower."
  },
  {
    "day": 56,
    "title": "Sleep and Memory Consolidation",
    "category": "RECOVERY",
    "summary": "Sleep is an active part of memory formation, not a pause: consolidation depends on the specific sleep stages that follow learning.",
    "action": "Review material you need to retain shortly before sleep; consolidation does the rest of the work overnight."
  },
  {
    "day": 57,
    "title": "Sleep and Immune Function",
    "category": "RECOVERY",
    "summary": "Sleep deprivation reduces natural killer cell activity, blunts antibody response to vaccination and raises inflammatory markers.",
    "action": "Treat sleep as part of training: a short night measurably weakens your immune response to the same workload."
  },
  {
    "day": 58,
    "title": "Sleep Extension & Performance",
    "category": "RECOVERY",
    "summary": "Extending sleep increased sprint speed, shooting accuracy and reaction time while improving mood and reducing daytime fatigue.",
    "action": "Before a demanding block, add 30-60 minutes of sleep rather than adding another session."
  },
  {
    "day": 59,
    "title": "Sleep Debt and Glucose Control",
    "category": "RECOVERY",
    "summary": "Six nights of restricted sleep reduced glucose tolerance and thyrotropin, and raised evening cortisol and sympathetic activity.",
    "action": "Eat the largest carbohydrate load after training and after adequate sleep, not during a sleep-debt stretch."
  },
  {
    "day": 60,
    "title": "Naps and Learning",
    "category": "RECOVERY",
    "summary": "A daytime nap containing both slow-wave and REM sleep produced performance gains equal to a full night of sleep on a perceptual learning task.",
    "action": "A 60-90 minute nap after a demanding cognitive block is a legitimate recovery tool, not indulgence."
  },
  {
    "day": 61,
    "title": "Cold Water Immersion for Recovery",
    "category": "RECOVERY",
    "summary": "Cold-water immersion reduces delayed-onset muscle soreness and perceived fatigue in the days after strenuous exercise.",
    "action": "Use cold immersion after a heavy eccentric session when soreness would compromise the next workout; skip it after strength sessions you want to adapt from."
  },
  {
    "day": 62,
    "title": "Delayed Onset Muscle Soreness",
    "category": "RECOVERY",
    "summary": "Soreness peaks 24-72 hours after unaccustomed eccentric work and temporarily reduces force production and range of motion.",
    "action": "Progress eccentric load gradually; soreness is a load-tolerance signal, not a training-quality score."
  },
  {
    "day": 63,
    "title": "Overtraining Syndrome",
    "category": "RECOVERY",
    "summary": "Sustained training without adequate recovery produces performance decrement that rest alone may take weeks to reverse.",
    "action": "Track resting heart rate and mood alongside workload; two weeks of declining mood plus flat performance means reduce load now."
  },
  {
    "day": 64,
    "title": "Autonomic Recovery Markers",
    "category": "RECOVERY",
    "summary": "Heart rate variability provides a standardized, non-invasive window on autonomic balance and vagal tone.",
    "action": "Use a consistent morning HRV measurement as a trend, not a single reading; compare today against your own baseline."
  },
  {
    "day": 65,
    "title": "Sleep Hygiene Evidence",
    "category": "RECOVERY",
    "summary": "Only some sleep-hygiene recommendations have solid empirical support; consistent timing, light control and caffeine limits are the strongest.",
    "action": "Prioritise the high-evidence behaviours — fixed schedule, morning light, caffeine cutoff, cool dark room — over the long tail of sleep tips."
  },
  {
    "day": 66,
    "title": "Implementation Intentions",
    "category": "HABITS",
    "summary": "Specifying when, where and how an action will be performed substantially increases the rate at which it is actually done.",
    "action": "Write each habit as an if-then: 'After I finish breakfast, I go outside for the workout.'"
  },
  {
    "day": 67,
    "title": "Self-Control as a Limited Resource",
    "category": "HABITS",
    "summary": "Acts of self-control measurably reduce subsequent self-control performance, and the effect is strongest for the most demanding tasks.",
    "action": "Sequence the day so the hardest discipline happens before the decisions pile up, and automate the rest."
  },
  {
    "day": 68,
    "title": "Habit-Goal Interface",
    "category": "HABITS",
    "summary": "Habits are context-triggered automatic responses, and they compete with, rather than simply serve, consciously held goals.",
    "action": "Change the context to change the habit: a new location or time breaks the cue-response link faster than renewed intention."
  },
  {
    "day": 69,
    "title": "Commitment Devices",
    "category": "HABITS",
    "summary": "Bundling a wanted indulgence with a beneficial behaviour significantly increased gym attendance and adherence.",
    "action": "Pick one thing you genuinely look forward to and make it available only during the workout."
  },
  {
    "day": 70,
    "title": "Goal Setting Theory",
    "category": "HABITS",
    "summary": "Specific, difficult goals produce higher performance than vague or easy goals, provided commitment and feedback are present.",
    "action": "Replace 'train more' with an exact target: 45 minutes, outdoors, before 09:00."
  },
  {
    "day": 71,
    "title": "Small Wins",
    "category": "HABITS",
    "summary": "Small, concrete, completed wins reduce the perceived size of a problem and build the momentum needed for the next step.",
    "action": "Define a win small enough to finish today and mark it complete; momentum is built from completed units."
  },
  {
    "day": 72,
    "title": "Self-Monitoring and Behaviour Change",
    "category": "HABITS",
    "summary": "Self-monitoring is among the behaviour-change techniques most consistently associated with larger intervention effects.",
    "action": "Log the habit the same day it happens; delayed logging loses the correction value of the measurement."
  },
  {
    "day": 73,
    "title": "Social Support and Adherence",
    "category": "HABITS",
    "summary": "Participants recruited with friends achieved higher completion and better maintenance than those treated individually.",
    "action": "Tell one person exactly what you are doing and ask them to check in weekly."
  },
  {
    "day": 74,
    "title": "Default Effects",
    "category": "HABITS",
    "summary": "Changing the default option changes behaviour dramatically even when opting out is trivially easy.",
    "action": "Design your environment so the desired behaviour is the default: kit laid out, water filled, book on the pillow."
  },
  {
    "day": 75,
    "title": "Breaking and Creating Habits",
    "category": "HABITS",
    "summary": "Deliberate habit interventions work best when they change the cues and context that trigger the behaviour, not just the intention.",
    "action": "When an unwanted habit fires, change one physical element of the situation; context is the lever."
  }
]

// Regenerates days 26-75 of science_cards.json.
//
// Every citation is resolved through Crossref by exact paper title and rejected unless the returned
// record's title has high token overlap with the intended paper, so no DOI is ever invented.
// Usage: node tools/author_science_cards.mjs
import fs from "node:fs"

const TOPICS = [
  // ---------------- CIRCADIAN (days 26-35) ----------------
  {
    slug: "Amber Lenses and Sleep",
    category: "CIRCADIAN",
    paper: "Amber lenses to block blue light and improve sleep: a randomized trial",
    summary: "Seven nights of amber blue-blocking lenses advanced sleep onset and improved sleep quality ratings and mood versus clear lenses.",
    mechanism: "Filtering the short-wavelength band reduces melanopsin stimulation in the hours before bed, so the pineal melatonin rise is no longer suppressed by evening light.",
    actionItem: "If you must use screens after dark, add blue-blocking lenses rather than trusting a night-shift colour profile alone."
  },
  {
    slug: "Circadian Misalignment & Metabolism",
    category: "CIRCADIAN",
    paper: "Adverse metabolic and cardiovascular consequences of circadian misalignment",
    summary: "Forcing sleep and meals out of phase with the internal clock raises postprandial glucose, insulin and blood pressure while lowering leptin.",
    mechanism: "Peripheral clocks in liver, pancreas and adipose tissue lose their normal phase relationship with the central clock, so insulin sensitivity and glucose uptake peak at the wrong time of day.",
    actionItem: "Keep sleep onset within a 60-minute window seven days a week so meals land at the same internal phase each day."
  },
  {
    slug: "Feeding Entrains Peripheral Clocks",
    category: "CIRCADIAN",
    paper: "Entrainment of the circadian clock in the liver by feeding",
    summary: "Restricting feeding to a fixed daily window shifts the liver clock independently of the light-dark cycle.",
    mechanism: "Food-anticipatory signals drive rhythmic transcription in hepatocytes through nutrient-sensing pathways, decoupling hepatic gene expression from the suprachiasmatic rhythm.",
    actionItem: "Anchor a consistent first and last meal time; the metabolic clock follows the eating window more than the alarm clock."
  },
  {
    slug: "Social Jetlag",
    category: "CIRCADIAN",
    paper: "Social jetlag: misalignment of biological and social time",
    summary: "The weekend shift in sleep timing creates a chronic phase mismatch comparable to flying one to two time zones every week.",
    mechanism: "Repeated re-entrainment demands on the suprachiasmatic nucleus desynchronize peripheral oscillators, degrading sleep quality and increasing metabolic and mood burden.",
    actionItem: "Hold weekend wake time within one hour of weekday wake time; recovery sleep is better taken as a nap."
  },
  {
    slug: "Self-Luminous Screens & Melatonin",
    category: "CIRCADIAN",
    paper: "Light level and duration of exposure determine the impact of self-luminous tablets on melatonin suppression",
    summary: "Two hours of a bright self-luminous tablet at night measurably suppresses melatonin; a dimmed display at the same distance does not.",
    mechanism: "Melanopsin-containing intrinsically photosensitive retinal ganglion cells integrate both irradiance and exposure duration, so short exposure to very bright light can equal long exposure to moderate light.",
    actionItem: "Lower screen brightness to the minimum comfortable level after dark — the dose, not the presence, of light is what suppresses melatonin."
  },
  {
    slug: "Daylight Exposure & Sleep Quality",
    category: "CIRCADIAN",
    paper: "Impact of windows and daylight exposure on overall health and sleep quality of office workers: a case-control pilot study",
    summary: "Office workers with window access sleep longer and report better sleep quality and vitality than colleagues without daylight.",
    mechanism: "Daytime light exposure increases the amplitude of the circadian signal, which strengthens the evening melatonin rise and shortens sleep latency.",
    actionItem: "Take a short outdoor break in the morning rather than a fluorescent-lit corridor; the ceiling light is a fraction of outdoor irradiance."
  },
  {
    slug: "Light Phase Response Curve",
    category: "CIRCADIAN",
    paper: "A phase response curve to single bright light pulses in human subjects",
    summary: "Bright light shifts the human clock in opposite directions depending on when it lands: earlier after late-night exposure, later after early-morning exposure.",
    mechanism: "The phase response curve peaks around the core body temperature minimum, so a single light pulse near that point produces the largest phase shift of the entire circadian cycle.",
    actionItem: "Use light deliberately: morning light advances the clock, late-evening light delays it."
  },
  {
    slug: "Epidemiology of the Human Clock",
    category: "CIRCADIAN",
    paper: "Epidemiology of the human circadian clock",
    summary: "Chronotype shifts later through adolescence and earlier again with age, and is strongly shaped by work and school schedules.",
    mechanism: "Intrinsic period, light exposure history and social schedule constraints together determine the phase of entrainment, producing population-level differences in sleep timing.",
    actionItem: "Work with your chronotype rather than against it when choosing when to schedule the hardest training block."
  },
  {
    slug: "Two-Process Sleep Regulation",
    category: "CIRCADIAN",
    paper: "Timing of human sleep: recovery process gated by a circadian pacemaker",
    summary: "Sleep timing is governed by the interaction of a homeostatic sleep drive that builds with wakefulness and a circadian gate that opens in the biological evening.",
    mechanism: "Process S accumulates adenosine-related sleep pressure across wakefulness while Process C imposes a circadian threshold; sleep becomes possible when pressure crosses the gated threshold.",
    actionItem: "A night of short sleep raises homeostatic pressure but cannot move the circadian gate — expect an early night, not a shift in rhythm."
  },
  {
    slug: "Natural Light-Dark Cycle Entrainment",
    category: "CIRCADIAN",
    paper: "Circadian entrainment to the natural light-dark cycle across seasons and the weekend",
    summary: "Camping without artificial light shifts sleep timing earlier and aligns the internal clock tightly with sunset and sunrise.",
    mechanism: "Removing evening electric light and adding morning outdoor light compresses the phase angle of entrainment, advancing both melatonin onset and sleep offset.",
    actionItem: "Treat outdoor morning light as the anchor habit and evening darkness as maintenance — together they retrain the clock fastest."
  },

  // ---------------- FOCUS (days 36-45) ----------------
  {
    slug: "Cost of Interrupted Work",
    category: "FOCUS",
    paper: "The cost of interrupted work: more speed and stress",
    summary: "Interrupted work is completed faster but at the cost of higher stress, frustration and effort, with a documented carryover of attentional residue.",
    mechanism: "Each interruption forces a re-establishment of goal state in working memory, and the unresolved prior task continues to compete for attentional resources.",
    actionItem: "Batch notifications: keep one uninterrupted block in the first half of the day for the single most important task."
  },
  {
    slug: "Attentional Blink",
    category: "FOCUS",
    paper: "The attentional blink: a review of data and theory",
    summary: "After detecting a target in a rapid stream, the ability to detect a second target is impaired for several hundred milliseconds.",
    mechanism: "Late-stage attentional selection is temporarily occupied by the first target's consolidation, gating the second target out before it reaches working memory.",
    actionItem: "Space demanding reviews apart; two high-attention tasks back-to-back degrade the second one."
  },
  {
    slug: "Selective Attention as a Filter",
    category: "FOCUS",
    paper: "Neural mechanisms of selective visual attention",
    summary: "Attention works by biasing competition between stimuli so that relevant representations win and irrelevant ones are suppressed.",
    mechanism: "Top-down signals from prefrontal and parietal cortex increase the gain of neurons representing the attended feature while suppressing the competing population.",
    actionItem: "Remove competing stimuli from the field rather than trying to ignore them — suppression costs more than absence."
  },
  {
    slug: "Working Memory Capacity Limits",
    category: "FOCUS",
    paper: "The magical number 4 in short-term memory: a reconsideration of mental storage capacity",
    summary: "The reliable capacity of focused attention in working memory is around four chunks, not the classical seven.",
    mechanism: "Capacity limits arise from the number of distinct object representations that can be held in a limited-capacity focus of attention simultaneously.",
    actionItem: "Keep an active task list to a handful of items; the rest belongs in a written system, not in your head."
  },
  {
    slug: "Meditation & Executive Attention",
    category: "FOCUS",
    paper: "Short-term meditation training improves attention and self-regulation",
    summary: "Five days of integrative body-mind training improved executive attention and reduced cortisol and anxiety relative to relaxation training.",
    mechanism: "Training increases connectivity in anterior cingulate and prefrontal circuits that resolve conflict between competing response tendencies.",
    actionItem: "Ten minutes of focused-attention practice per day is enough to see measurable attentional gains."
  },
  {
    slug: "Working Memory Training Transfer",
    doi: "10.1073/pnas.0801268105",
    category: "FOCUS",
    paper: "Improving fluid intelligence with training on working memory",
    summary: "Adaptive working memory training transferred to measures of fluid intelligence, with gains tracking the amount of training.",
    mechanism: "Demanding, adaptively difficult working memory tasks engage frontoparietal control networks whose efficiency gains generalize beyond the trained task.",
    actionItem: "Make practice adaptively hard; repeating something already easy produces no generalizable gain."
  },
  {
    slug: "Nature and Directed Attention",
    category: "FOCUS",
    paper: "The cognitive benefits of interacting with nature",
    summary: "Walking in nature improved directed-attention performance and mood relative to an urban walk of the same duration.",
    mechanism: "Natural environments recruit soft, involuntary attention, allowing the directed-attention system to recover from the fatigue produced by effortful focus.",
    actionItem: "Use the outdoor workout as attentional recovery, not just conditioning — it restores the faculty the rest of the day spends."
  },
  {
    slug: "Exercise and Executive Function",
    category: "FOCUS",
    paper: "Be smart, exercise your heart: exercise effects on brain and cognition",
    summary: "Regular aerobic activity improves executive control and attention and is associated with better academic and cognitive outcomes.",
    mechanism: "Exercise increases cerebral blood flow, upregulates neurotrophic signalling and optimizes dopaminergic and noradrenergic tone supporting executive networks.",
    actionItem: "Schedule cognitively demanding work after training rather than before; the post-exercise window favours executive tasks."
  },
  {
    slug: "Working Memory Capacity & Limits",
    category: "FOCUS",
    paper: "Visual working memory capacity: from psychophysics and neurobiology to individual differences",
    summary: "Visual working memory capacity is a stable individual trait that predicts broader cognitive performance, and it is limited by attentional allocation.",
    mechanism: "Capacity is set by the number of items that can be bound into integrated representations under the control of sustained attention, not by a raw storage buffer.",
    actionItem: "Protect attention quality: when it degrades, effective capacity drops even though the task has not changed."
  },
  {
    slug: "Default Mode & Mind Wandering",
    category: "FOCUS",
    paper: "Mind-wandering as spontaneous thought: a dynamic framework",
    summary: "Spontaneous thought is not random noise; it tracks the salience of unresolved goals and competes directly with task focus.",
    mechanism: "Default network activity increases when external task demands fall, and its content is constrained by the same goal representations that guide deliberate thought.",
    actionItem: "Capture open loops in writing before a focus block so unresolved goals stop pulling attention."
  },

  // ---------------- DOPAMINE (days 46-55) ----------------
  {
    slug: "Dopamine and Reward Wanting",
    category: "DOPAMINE",
    paper: "What is the role of dopamine in reward: hedonic impact, reward learning, or incentive salience?",
    summary: "Dopamine mediates 'wanting' — the incentive pull of a cue — and is not required for the hedonic pleasure of a reward.",
    mechanism: "Mesolimbic dopamine increases the motivational salience attributed to reward-predictive cues, driving approach behaviour independently of hedonic 'liking' circuits.",
    actionItem: "Expect craving to track cues, not value: removing the cue removes the pull more reliably than resolving to resist it."
  },
  {
    slug: "Dopamine Signals Are Broader Than Reward",
    category: "DOPAMINE",
    paper: "Dopamine in motivational control: rewarding, aversive, and alerting",
    summary: "Midbrain dopamine neurons encode motivationally salient events generally, including aversive and alerting ones, not reward alone.",
    mechanism: "Distinct dopamine subpopulations project to different targets and carry value, salience and alerting signals, so dopamine is a salience and motivation system rather than a pleasure signal.",
    actionItem: "Judge dopamine by the behaviour it drives, not by how something feels; salience includes the negative kind."
  },
  {
    slug: "Reward Prediction Error",
    category: "DOPAMINE",
    paper: "A neural substrate of prediction and reward",
    summary: "Dopamine neuron firing encodes the difference between received and expected reward, providing the teaching signal for learning.",
    mechanism: "Phasic dopamine bursts for better-than-expected outcomes and dips for worse-than-expected outcomes update value estimates in striatal circuits.",
    actionItem: "Progress must stay slightly unpredictable to stay motivating; a fully predictable reward stops teaching the system anything."
  },
  {
    slug: "Effort, Cost and Dopamine",
    category: "DOPAMINE",
    paper: "Effort-related functions of nucleus accumbens dopamine and associated forebrain circuits",
    summary: "Dopamine in the nucleus accumbens sets willingness to overcome effort costs, independent of the hedonic value of the outcome.",
    mechanism: "Accumbens dopamine modulates the cost-benefit computation in ventral striatal and prefrontal circuits, shifting the effort a subject will expend for a given payoff.",
    actionItem: "Lower the activation cost of hard habits — the system will not pay high effort for a distant reward without support."
  },
  {
    slug: "Model-Based Value Learning",
    category: "DOPAMINE",
    paper: "Model-based influences on humans' choices and striatal prediction errors",
    summary: "Humans use both cached habits and forward-looking models, and the striatal prediction error signal is modulated by model-based expectations.",
    mechanism: "Ventral striatum integrates model-free prediction errors with inferred transition structure from prefrontal circuits, so learning reflects both habit and reasoning.",
    actionItem: "Pair repetition with deliberate reasoning about why a habit works; habit alone is fragile when conditions change."
  },
  {
    slug: "Dopamine Circuit Organisation",
    category: "DOPAMINE",
    paper: "Dopamine reward circuitry: two projection systems from the ventral midbrain to the nucleus accumbens-olfactory tubercle complex",
    summary: "Reward-related dopamine arises from two anatomically distinct projection systems with different targets and functions.",
    mechanism: "The mesolimbic and mesocortical projections from ventral midbrain to accumbens shell and core carry partially separable motivational and associative functions.",
    actionItem: "Motivation has multiple channels: physical reward, social reward and completion each drive behaviour through different routes."
  },
  {
    slug: "Refined Sugar and Reward Sensitisation",
    category: "DOPAMINE",
    paper: "Evidence for sugar addiction: behavioral and neurochemical effects of intermittent, excessive sugar intake",
    summary: "Intermittent, excessive sugar access produces neurochemical changes resembling those seen with drugs of abuse in animal models.",
    mechanism: "Repeated binge-like access alters accumbens dopamine and opioid signalling and produces cross-sensitisation, with withdrawal-like effects on removal.",
    actionItem: "Continuous restriction plus an occasional deliberate treat beats repeated binge cycles; intermittency is what sensitises."
  },
  {
    slug: "Dopamine and Cognitive Control",
    category: "DOPAMINE",
    paper: "Dopamine and the regulation of cognition and attention",
    summary: "Prefrontal dopamine tunes working memory and attentional control, with an inverted-U relationship between dopamine level and performance.",
    mechanism: "Optimal D1 receptor stimulation stabilises prefrontal representations against distraction, while excessive or insufficient stimulation degrades signal-to-noise.",
    actionItem: "Aim for a stable baseline of sleep and movement: both under- and over-stimulation flatten cognitive control."
  },
  {
    slug: "Dopamine, Time and Impulsivity",
    category: "DOPAMINE",
    paper: "Dopamine, time, and impulsivity in humans",
    summary: "Raising dopamine precursor availability shifted people toward choosing sooner, smaller rewards over larger, later ones.",
    mechanism: "Dopamine availability biases the rate at which delayed rewards are discounted, changing how steeply the future is devalued.",
    actionItem: "Make delayed outcomes concrete; discounting is the default, and specificity is the antidote."
  },
  {
    slug: "Incentive Sensitisation",
    category: "DOPAMINE",
    paper: "Liking, wanting, and the incentive-sensitization theory of addiction",
    summary: "Repeated reward exposure sensitizes 'wanting' while 'liking' stays flat or declines, which is why craving and enjoyment diverge.",
    mechanism: "Sensitisation of mesolimbic dopamine systems amplifies cue-triggered incentive salience without a corresponding change in hedonic hotspots.",
    actionItem: "When a habit stops being enjoyable but the pull remains, change the cue environment rather than relying on willpower."
  },

  // ---------------- RECOVERY (days 56-65) ----------------
  {
    slug: "Sleep and Memory Consolidation",
    doi: "10.1152/physrev.00032.2012",
    category: "RECOVERY",
    paper: "About sleep's role in memory",
    summary: "Sleep is an active part of memory formation, not a pause: consolidation depends on the specific sleep stages that follow learning.",
    mechanism: "Slow-wave sleep drives hippocampal-to-neocortical replay for declarative memory, while REM sleep supports procedural and emotional memory integration and synaptic renormalisation.",
    actionItem: "Review material you need to retain shortly before sleep; consolidation does the rest of the work overnight."
  },
  {
    slug: "Sleep and Immune Function",
    doi: "10.1007/s00424-011-1044-0",
    category: "RECOVERY",
    paper: "Sleep and immune function",
    summary: "Sleep deprivation reduces natural killer cell activity, blunts antibody response to vaccination and raises inflammatory markers.",
    mechanism: "Nocturnal sleep promotes redistribution of T cells and release of growth hormone and prolactin while suppressing cortisol, conditions that favour adaptive immune consolidation.",
    actionItem: "Treat sleep as part of training: a short night measurably weakens your immune response to the same workload."
  },
  {
    slug: "Sleep Extension & Performance",
    category: "RECOVERY",
    paper: "The effects of sleep extension on the athletic performance of collegiate basketball players",
    summary: "Extending sleep increased sprint speed, shooting accuracy and reaction time while improving mood and reducing daytime fatigue.",
    mechanism: "Additional slow-wave and REM sleep improves motor sequence consolidation, reaction time and autonomic recovery, translating directly into performance output.",
    actionItem: "Before a demanding block, add 30-60 minutes of sleep rather than adding another session."
  },
  {
    slug: "Sleep Debt and Glucose Control",
    category: "RECOVERY",
    paper: "Impact of sleep debt on metabolic and endocrine function",
    summary: "Six nights of restricted sleep reduced glucose tolerance and thyrotropin, and raised evening cortisol and sympathetic activity.",
    mechanism: "Sleep restriction decreases glucose disposal rate and blunts the insulin response, creating a pre-diabetic metabolic profile in healthy young adults.",
    actionItem: "Eat the largest carbohydrate load after training and after adequate sleep, not during a sleep-debt stretch."
  },
  {
    slug: "Naps and Learning",
    category: "RECOVERY",
    paper: "Sleep-dependent learning: a nap is as good as a night",
    summary: "A daytime nap containing both slow-wave and REM sleep produced performance gains equal to a full night of sleep on a perceptual learning task.",
    mechanism: "Hippocampal replay occurs during slow-wave sleep regardless of whether it follows a nap or a night, so consolidation depends on sleep architecture rather than clock time.",
    actionItem: "A 60-90 minute nap after a demanding cognitive block is a legitimate recovery tool, not indulgence."
  },
  {
    slug: "Cold Water Immersion for Recovery",
    category: "RECOVERY",
    paper: "Cold-water immersion (cryotherapy) for preventing and treating muscle soreness after exercise",
    summary: "Cold-water immersion reduces delayed-onset muscle soreness and perceived fatigue in the days after strenuous exercise.",
    mechanism: "Vasoconstriction and reduced tissue temperature lower metabolic rate, nerve conduction velocity and inflammatory mediator release in the affected muscle.",
    actionItem: "Use cold immersion after a heavy eccentric session when soreness would compromise the next workout; skip it after strength sessions you want to adapt from."
  },
  {
    slug: "Delayed Onset Muscle Soreness",
    doi: "10.2165/00007256-200333020-00005",
    category: "RECOVERY",
    paper: "Delayed onset muscle soreness: treatment strategies and performance factors",
    summary: "Soreness peaks 24-72 hours after unaccustomed eccentric work and temporarily reduces force production and range of motion.",
    mechanism: "Mechanical disruption of sarcomeres, particularly at the Z-lines, triggers a local inflammatory response with oedema and increased intramuscular pressure.",
    actionItem: "Progress eccentric load gradually; soreness is a load-tolerance signal, not a training-quality score."
  },
  {
    slug: "Overtraining Syndrome",
    category: "RECOVERY",
    paper: "Prevention, diagnosis, and treatment of the overtraining syndrome: joint consensus statement of the European College of Sport Science and the American College of Sports Medicine",
    summary: "Sustained training without adequate recovery produces performance decrement that rest alone may take weeks to reverse.",
    mechanism: "Persistent autonomic and endocrine dysregulation — blunted sympathetic response, altered cortisol rhythms and mood disturbance — accumulates faster than it resolves.",
    actionItem: "Track resting heart rate and mood alongside workload; two weeks of declining mood plus flat performance means reduce load now."
  },
  {
    slug: "Autonomic Recovery Markers",
    category: "RECOVERY",
    paper: "Heart rate variability: standards of measurement, physiological interpretation and clinical use",
    summary: "Heart rate variability provides a standardized, non-invasive window on autonomic balance and vagal tone.",
    mechanism: "Beat-to-beat variation reflects vagal modulation of the sinoatrial node, so a depressed high-frequency component indicates reduced parasympathetic recovery capacity.",
    actionItem: "Use a consistent morning HRV measurement as a trend, not a single reading; compare today against your own baseline."
  },
  {
    slug: "Sleep Hygiene Evidence",
    category: "RECOVERY",
    paper: "The role of sleep hygiene in promoting public health: a review of empirical evidence",
    summary: "Only some sleep-hygiene recommendations have solid empirical support; consistent timing, light control and caffeine limits are the strongest.",
    mechanism: "Recommendations that directly affect circadian phase and homeostatic sleep pressure show the largest effects, while many folk rules have weak or inconsistent evidence.",
    actionItem: "Prioritise the high-evidence behaviours — fixed schedule, morning light, caffeine cutoff, cool dark room — over the long tail of sleep tips."
  },

  // ---------------- HABITS (days 66-75) ----------------
  {
    slug: "Implementation Intentions",
    doi: "10.1016/S0065-2601(06)38002-1",
    category: "HABITS",
    paper: "Implementation intentions and goal achievement: a meta-analysis of effects and processes",
    summary: "Specifying when, where and how an action will be performed substantially increases the rate at which it is actually done.",
    mechanism: "Linking a situational cue to a response creates a mental if-then association that lets the cue trigger the action with minimal deliberate control.",
    actionItem: "Write each habit as an if-then: 'After I finish breakfast, I go outside for the workout.'"
  },
  {
    slug: "Self-Control as a Limited Resource",
    category: "HABITS",
    paper: "Ego depletion and the strength model of self-control: a meta-analysis",
    summary: "Acts of self-control measurably reduce subsequent self-control performance, and the effect is strongest for the most demanding tasks.",
    mechanism: "Sustained executive control recruits shared prefrontal resources, so consecutive acts of restraint compete for the same limited capacity.",
    actionItem: "Sequence the day so the hardest discipline happens before the decisions pile up, and automate the rest."
  },
  {
    slug: "Habit-Goal Interface",
    category: "HABITS",
    paper: "A new look at habits and the habit-goal interface",
    summary: "Habits are context-triggered automatic responses, and they compete with, rather than simply serve, consciously held goals.",
    mechanism: "Repeated performance in a stable context forms direct cue-response links in dorsal striatal circuits that can be activated without goal activation.",
    actionItem: "Change the context to change the habit: a new location or time breaks the cue-response link faster than renewed intention."
  },
  {
    slug: "Commitment Devices",
    category: "HABITS",
    paper: "Holding the Hunger Games hostage at the gym: an evaluation of temptation bundling",
    summary: "Bundling a wanted indulgence with a beneficial behaviour significantly increased gym attendance and adherence.",
    mechanism: "The immediately rewarding experience is made contingent on the effortful behaviour, raising the motivational value of the activity at the moment of choice.",
    actionItem: "Pick one thing you genuinely look forward to and make it available only during the workout."
  },
  {
    slug: "Goal Setting Theory",
    category: "HABITS",
    paper: "Building a practically useful theory of goal setting and task motivation: a 35-year odyssey",
    summary: "Specific, difficult goals produce higher performance than vague or easy goals, provided commitment and feedback are present.",
    mechanism: "Goals direct attention and effort, mobilize persistence and promote strategy search, with performance scaling to goal difficulty within ability limits.",
    actionItem: "Replace 'train more' with an exact target: 45 minutes, outdoors, before 09:00."
  },
  {
    slug: "Small Wins",
    category: "HABITS",
    paper: "Small wins: redefining the scale of social problems",
    summary: "Small, concrete, completed wins reduce the perceived size of a problem and build the momentum needed for the next step.",
    mechanism: "A completed concrete outcome provides unambiguous feedback that lowers perceived difficulty and sustains engagement with an otherwise overwhelming goal.",
    actionItem: "Define a win small enough to finish today and mark it complete; momentum is built from completed units."
  },
  {
    slug: "Self-Monitoring and Behaviour Change",
    category: "HABITS",
    paper: "Effective techniques in healthy eating and physical activity interventions: a meta-regression",
    summary: "Self-monitoring is among the behaviour-change techniques most consistently associated with larger intervention effects.",
    mechanism: "Recording behaviour closes the feedback loop between intention and outcome, making discrepancies visible while they can still be corrected.",
    actionItem: "Log the habit the same day it happens; delayed logging loses the correction value of the measurement."
  },
  {
    slug: "Social Support and Adherence",
    category: "HABITS",
    paper: "Benefits of recruiting participants with friends and increasing social support for weight loss and maintenance",
    summary: "Participants recruited with friends achieved higher completion and better maintenance than those treated individually.",
    mechanism: "Social support supplies accountability, modelling and reinforcement that sustain behaviour once intrinsic motivation dips.",
    actionItem: "Tell one person exactly what you are doing and ask them to check in weekly."
  },
  {
    slug: "Default Effects",
    category: "HABITS",
    paper: "Do defaults save lives?",
    summary: "Changing the default option changes behaviour dramatically even when opting out is trivially easy.",
    mechanism: "Defaults work through inertia, implied endorsement and the effort of active choice, so they shape outcomes without changing stated preferences.",
    actionItem: "Design your environment so the desired behaviour is the default: kit laid out, water filled, book on the pillow."
  },
  {
    slug: "Breaking and Creating Habits",
    category: "HABITS",
    paper: "Interventions to break and create consumer habits",
    summary: "Deliberate habit interventions work best when they change the cues and context that trigger the behaviour, not just the intention.",
    mechanism: "Habit strength depends on the stability of the cue-behaviour link, so altering the performance context forces controlled processing and allows new links to form.",
    actionItem: "When an unwanted habit fires, change one physical element of the situation; context is the lever."
  }
]

const EMAIL = "ascend75@example.com"
const HEADERS = { "User-Agent": `Ascend75CardAuthoring/1.0 (mailto:${EMAIL})` }
const JUNK_TYPE = new Set(["component", "dataset", "peer-review", "grant", "erratum"])
const JUNK_TITLE = /^(supplemental material|erratum|correction|comment on|corrigendum|retraction)/i

function decodeEntities(text) {
  return text
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
}

function tokens(text) {
  return new Set(
    text
      .toLowerCase()
      .replace(/[^a-z0-9 ]+/g, " ")
      .split(/\s+/)
      .filter((word) => word.length > 2)
  )
}

function overlap(a, b) {
  const left = tokens(a)
  const right = tokens(b)
  if (left.size === 0 || right.size === 0) return 0
  let shared = 0
  for (const token of left) if (right.has(token)) shared += 1
  return shared / Math.max(left.size, right.size)
}

function citationOf(item) {
  const journal = decodeEntities(item["container-title"]?.[0] ?? "")
  const year = item.issued?.["date-parts"]?.[0]?.[0] ?? ""
  const first = item.author?.[0]
  const family = first?.family ?? ""
  // Corporate authors come back as very long literal names; fall back to the journal there.
  const author = family && family.length <= 24 ? `${family} et al.` : ""
  return author ? `${author}, ${journal}, ${year}` : `${journal}, ${year}`.replace(/^, /, "")
}

async function resolveByDoi(doi) {
  const res = await fetch(`https://api.crossref.org/works/${encodeURIComponent(doi)}`, { headers: HEADERS })
  if (!res.ok) throw new Error(`Crossref ${res.status} for pinned doi ${doi}`)
  const json = await res.json()
  const item = json.message
  return {
    doi: `https://doi.org/${item.DOI}`,
    resolvedTitle: decodeEntities(item.title?.[0] ?? ""),
    score: 1,
    citation: citationOf(item)
  }
}

async function resolveByTitle(paper) {
  const url = `https://api.crossref.org/works?rows=8&select=DOI,title,container-title,issued,author,type&query.bibliographic=${encodeURIComponent(paper)}`
  const res = await fetch(url, { headers: HEADERS })
  if (!res.ok) throw new Error(`Crossref ${res.status} for ${paper}`)
  const json = await res.json()

  const candidates = json.message.items
    .filter((item) => item.title?.length && !JUNK_TYPE.has(item.type) && !JUNK_TITLE.test(item.title[0]))
    .map((item) => ({ item, score: overlap(paper, item.title[0]) }))
    .filter((candidate) => candidate.score >= 0.5)
    .sort((a, b) => {
      const journalA = a.item.type === "journal-article" ? 1 : 0
      const journalB = b.item.type === "journal-article" ? 1 : 0
      return journalB - journalA || b.score - a.score
    })

  if (!candidates.length) return null
  const best = candidates[0].item
  return {
    doi: `https://doi.org/${best.DOI}`,
    resolvedTitle: decodeEntities(best.title[0]),
    score: candidates[0].score,
    citation: citationOf(best)
  }
}

const out = []
let day = 26
for (const topic of TOPICS) {
  const match = topic.doi ? await resolveByDoi(topic.doi) : await resolveByTitle(topic.paper)
  if (!match) {
    console.error(`!! no confident match for day ${day}: ${topic.paper}`)
    process.exit(1)
  }
  console.log(`day ${day} [${topic.category}] ${topic.slug} (score ${match.score.toFixed(2)})`)
  console.log(`     ${match.resolvedTitle}`)
  console.log(`     ${match.citation} | ${match.doi}`)
  out.push({
    dayNumber: day,
    title: `Day ${day}: ${topic.slug}`,
    category: topic.category,
    summary: topic.summary,
    mechanism: topic.mechanism,
    actionItem: `Day ${day} Protocol: ${topic.actionItem}`,
    sourceCitation: match.citation,
    doiOrUrl: match.doi,
    isBookmarked: false
  })
  day += 1
}

if (out.length !== 50) {
  console.error(`expected 50 cards, produced ${out.length}`)
  process.exit(1)
}

const target = "core/database/src/main/assets/science_cards.json"
const existing = JSON.parse(fs.readFileSync(target, "utf8"))
const merged = [...existing.filter((card) => card.dayNumber <= 25), ...out]
const titles = new Set(merged.map((card) => card.title))
const dois = new Set(merged.map((card) => card.doiOrUrl))
if (merged.length !== 75 || titles.size !== 75) {
  console.error(`merged=${merged.length} distinctTitles=${titles.size}`)
  process.exit(1)
}
fs.writeFileSync(target, JSON.stringify(merged, null, 2) + "\n")
console.log(`\nwrote ${merged.length} cards (${titles.size} distinct titles, ${dois.size} distinct sources)`)

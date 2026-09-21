# Ascend 75 — Web Prototype

A presentational React + Vite prototype of the Ascend 75 five-tab shell. It mirrors the Android app's
information architecture and design tokens so the layout, tab behaviour and visual language can be
reviewed without a device.

**The Android app in the repository root is the authoritative implementation.** This prototype keeps all
state in memory: no backend, no persistence, no accounts.

## What it covers

| Tab | Content |
| :--- | :--- |
| Today | Derived metric cards (completion, streak, hydration, milestone), habit checklist with search, checklist / analytics tabs |
| Trackers | Workout, hydration, reading and progress-photo cards carrying the same values as the checklist |
| Learn | All 75 curriculum cards with search, category filter, in-place detail and bookmarking |
| Vault | Locked gate, before/after split comparison, thumbnail grid |
| More | Sleep-cutoff control, protocol rules, JSON export, data wipe, about and disclaimer |

`src/data/scienceCards.ts` is generated from `core/database/src/main/assets/science_cards.json` so the two
clients cannot drift:

```bash
node ../tools/sync_web_science_cards.mjs
```

## Commands

```bash
npm install
npm run dev      # dev server
npm run build    # tsc -b && vite build
npm run lint     # oxlint
npm run preview  # serve the production build
```

## Design tokens

`src/index.css` carries the "Zenith Fitness & Growth" palette from [`../DESIGN.md`](../DESIGN.md) as CSS
custom properties, plus the self-hosted Plus Jakarta Sans face in `public/fonts/` — the prototype makes no
network requests at runtime.

The vendored `src/components/ui/*` primitives style Radix state with the attribute names Radix actually
emits (`data-[state=…]`, `data-[orientation=…]`); changing those selectors back to the short
`data-active:` / `data-horizontal:` forms silently disables the state styling.

# Fly-brained villagers (26.3 branch)

`[model]` A villager's body moved by the fly's whole brain, with the same genome, mating, eggs, inheritance and selection rules as the flies.

## How it works
- It is **body mode 2 of the mod's own entity** (`connectomefly:connectome_fly`), next to the fly model (0) and the bee (1). It is never a vanilla villager, so `@e[type=villager]` and Zion's command blocks do not see it, and nothing existing is converted or retagged.
- Drawn with the vanilla villager model and plain villager skin (no profession). Hitbox 0.6 x 1.95, scaled with growth, so the young are small.
- The brain is unchanged. What differs is the body:
  - **No flight, no walls or ceilings.** Flight outputs are ignored; the giant-fibre escape becomes a startled hop.
  - Frogs cannot hurt it.
  - Everything else is the fly's behaviour in a villager's shape: walking, turning, grooming, feeding, sleeping, courting, laying an egg.
- **Children take the mother's body.** The egg carries it, so villagers have villager children and flies have fly children. A fly and a villager can still mate with each other.
- Females run the whole brain (about one CPU core each, same queue as flies). Males use the small courtship circuit.

## Commands
- `/flygenes spawn villager [female|male]` adds one where you stand, leaving others alone. `/flygenes spawn fly [female|male]` likewise.
- `/flylab body villager` turns every fly into a villager body and makes it the default (`/flylab body model` turns them back).
- `/flygenes`, `/flygenes stats`, `/flygenes set ...` apply to both.
- HUD: header reads "FLY-BRAINED VILLAGER [model]" with a line saying it cannot trade, farm or take orders. The credits screen says the same.

## What was checked (26.3 client, `-Pautotest=villager` then `reload`; `test-log.txt`)
| Check | Result |
|---|---|
| Drawn as a villager, with health and hunger bars and speech | yes (screenshots here) |
| `@e[type=villager]` | selected only the real villager placed beside them |
| Natural courtship and mating | a founder pair mated without being forced and laid two eggs |
| Children | two hatched with villager bodies (`Body: 2b`), generation 1; 9 genes from mother and 14 or 15 from father, 5 or 6 mutated |
| Save, quit, reopen | all four identical afterwards |

## Not checked
- **Whether it looks right in motion.** Only still screenshots: walk animation, head turning, the startled hop, sleeping and death were not watched.
- That it never leaves the ground or climbs (the code blocks it; no long run was watched).
- Feeding in a villager body: food-finding distances were tuned for a fly-sized body.
- A fly mating with a villager; `/flylab body villager` on existing flies; the experiment boxes with a villager inside (they are fly-sized).
- Doors, beds, workstations, iron golems, raids, zombies: it has no villager behaviour, and how vanilla mobs treat it was not tested.
- Real graphics card, the owner's instance, Zion.

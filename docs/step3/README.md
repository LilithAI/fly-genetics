# Step 3: selection and readout

All rules here are a `[model]` and are labelled so in game.

## What was built
**Selection** (on by default; `/flygenes set selection 0` restores the mod's original no-death behaviour):
- **Old age.** An adult lives `adult_life_minutes` (default 120) times its own factor of 0.8 to 1.2, fixed at birth and not inherited. Time as a young fly does not count. Then it dies.
- **Starvation kills.** The mod stopped starvation damage at 1 health; with selection on it goes to zero. A female with an empty stomach dies about 80 s after reaching zero hunger from full health.
- **Fed-only breeding.** A female whose hunger is below `breed_hunger` (default 12 of 20) turns the male down. Laying an egg costs her `egg_cost` (3) hunger.
- **Population cap.** No egg is laid while `cap` (default 12) flies and eggs are within `cap_radius` (48) blocks of the mother.
- **Predators.** The fly is added to the vanilla `frog_food` tag, so frogs hunt it with their normal AI. No existing entity is changed or retagged. The fly's brain already sensed frogs as predators.
- **Stored mate.** A female keeps her last mate's genome, so later eggs her brain decides to lay have a father too (before this fix they had none).
- `growth_speed` (default 1) can shorten the 90 minutes the mod takes from egg to adult.

**Settings and statistics are saved with the world** (`data/connectomefly_genetics.dat`), including the inherited-memory toggle. `/flygenes set` lists them; `/flygenes set <key> <value>` changes one.

**Readout:**
- HUD (look at a fly): four green lines near the top: Generation `[model]`, Parents, Genes `[model]` (the three furthest from the author's values), Age `[model]`.
- `/flygenes stats`: flies alive by generation and sex, born per generation, eggs laid, eggs refused by the cap, matings refused for hunger, deaths by cause with mean adult age, and the six genes whose mean moved most between the first and latest generation and between the first generation and the flies alive now. It ends with a reminder that with few flies this is mostly chance.
- Every death and refusal is also written to the game log.

## What was checked (in game, `runAutoTest -Pautotest=select` and `-Pautotest=cap`; log in `select-test-log.txt`)
| Rule | Result |
|---|---|
| Old age | Life set to 0.3 min: a female and a male both died of "old age" 19 s after spawning |
| Starvation | Female with hunger 0 and 2 health died of "starvation" in 16 s |
| Fed-only breeding | Female held at hunger 5 turned the male down twice in 44 s; no egg |
| Cap | Fed pair with cap 2: mated, "egg refused by cap: 2 of 2" twice, no egg |
| Breeding still works | Fresh fed pair, cap 12: egg laid, hatched, generation 1 with both parents |
| Predator | A frog ate a fly that could not move within 14 s; recorded as "predator (frog)" |
| Free flies among frogs | Four frogs beside a free pair for 80 s: none caught, and the pair bred |
| Statistics saved | Counts carried over between separate game sessions on the same world |
| HUD | Lines visible for female and male (`hud-female.png`, `hud-male-with-frogs.png`) |
| Step 2 checks | `check-genome.sh`, breeding test and reload test all still pass |

## Not checked
- **No evolution has been observed.** No population has been run for several generations, so nothing shows gene means moving for a reason. The statistics command has only been exercised with a handful of flies.
- **Frogs may be too weak a predator.** They caught nothing that could move. Whether they ever catch a free fly, and whether escape-related genes change the odds, is unknown.
- **Males cannot starve.** The mod gives males no hunger, so only old age and predators kill them.
- Thirst was not touched and has not been tested as a cause of death.
- Natural lifespan (120 min), natural starvation from a full stomach (about 10 min without food) and the 90-minute growth were not waited out; the tests used shortened values or preset hunger.
- Whether a female in a vivarium finds enough food to stay above the breeding threshold. That depends on the vivarium (step 5).
- `selection 0`, `growth_speed`, `egg_cost` and `cap_radius` settings were not exercised.
- Inherited memory: still never run.
- The HUD panel is taller than the window at 854x480 for females (it already was in the original); the new lines were moved up so they stay visible, but the bottom lines are cut off at that size.

# Population runs (between step 3 and step 4)

Unattended runs on a 1.20.1 dev server in a throwaway world (`headless/poprun.sh [minutes] [frogs]`).
A 21 x 21 x 6 glass pen with 10 sweet berry bushes, 4 melons and a water cauldron; 3 founder females and 3 founder males; 3 whole brains at once on a 4-core machine. Speeded up: `growth_speed 60` (90 s from hatching to adult) and `adult_life_minutes 8`; cap 12. Permanent noon.

## Run 1: with two frogs (`run1-with-frogs.log`), stopped after 9 minutes
- The frogs ate all three founder males within 32 seconds, and the first hatchling (a male) the moment it appeared.
- One egg was laid in total. One female starved, one died of old age. The population was finished.
- In the open-field test of step 3, four frogs caught no free fly in 80 s. In a closed pen it is the opposite, at least for males.

## Run 2: no frogs (`run2-no-frogs.log`), 40 minutes
| | |
|---|---|
| Born per generation | gen 0: 6, gen 1: 6, gen 2: 10, gen 3: 6, gen 4: 4, gen 5: 5 (37 flies) |
| Deaths | old age 35, starvation 2, nothing else |
| Eggs | 31 laid, 23 refused by the cap |
| Matings refused because she was hungry | 76 |
| End | **extinct at minute 40**: the last generation was 4 females and 1 male; the last two females outlived every male |
| Server load | no "can't keep up" warnings with 3 brains on 4 cores |

What it shows:
- The whole loop works unattended for five generations: feeding, courtship, refusal when hungry, eggs, hatching, growing up, ageing, the cap, statistics.
- Flies do feed in the pen, and they differ: most females sat at 15 to 19 hunger while one founder never fed, was turned down again and again and starved.
- **Selection was weak.** 35 of 37 deaths were old age. The only pressure was hungry females missing matings.
- **The gene shifts are not evidence of selection.** For example vision 1.07 to 0.93 and smell 1.01 to 0.87 between generation 0 and 5, but with 4 to 10 flies per generation and one run, chance alone produces shifts of this size. Telling the two apart needs several runs, or a control with selection off.
- **A cap of 12 can die out by bad luck.** Sex is a coin flip per hatchling; with five born, 4 to 1 is not rare.

## Not checked
- Only one run of each kind. No repeat, no control run with selection off.
- Speeded-up timings only. At the real pace (90 min growth, 120 min adult life) nothing has been run.
- Permanent noon: sleep, the clock and night behaviour played no part.
- Whether thirst mattered (not logged).
- Why the one founder female never fed.

# Step 1: wiring format, how gains apply, and what is safe to vary

All of this is from running the brain **outside Minecraft** (`headless/`). Nothing here has been seen in a fly's behaviour in game yet.

## How it was measured
- `headless/src/ParamBrain.java` is a copy of the mod's step and learning rules with every constant made a per-brain field, plus a per-brain multiplier for each circuit-gain group. It uses the original shared wiring from the unmodified jar.
- At baseline it reproduces the original: rest 0.23 Hz (original 0.25), food 1.38 (1.37), threat 4.32 (4.29), front-leg motor 0.33 (0.33).
- Each test brain runs: 3 s rest, 3 s food smell and sugar, 3 s visual + touch + aversive, 1.5 s recovery, 2 s pheromone and song, then odour with punishment and odour with reward.
- Verdicts are our own rules (see `headless/analyze.py`): SILENT = firing below half of baseline or leg response below 30%; SEIZING = any phase above 3x baseline, or activity still above 2x resting level 1.5 s after the threat, or legs driven with no input.
- Raw data: `seeds.csv`, `single.csv`, `random20.csv`, `bands.csv`. Tables: `single-report.md`, `gain-report.md`.

## Wiring file
`fullbrain.bin.gz`, little-endian: `int n`, `int nEdges`, `int[n+1] indptr`, `int[nEdges] indices`, `float[nEdges] weight` (row = source neuron; negative weight = inhibitory), `byte[n] sensory`, padding to 4 bytes, `byte[n] dnclass` (1 = left descending, 2 = right).

## How circuit gains apply
`brain_circuit_gains.json` has 7 groups, each a `src` list and a `dst` list. Every synapse from a src neuron to a dst neuron is multiplied by the gain, once, into the shared weights when the wiring loads. No synapse is in two groups. For per-fly gains the multiplier has to be applied when a spike is delivered; `ParamBrain` does this with one byte per synapse (3 MB, shared) and it made no measurable speed difference.

## Result 1: plus or minus 20 percent is too wide
One constant at a time, four of them break inside 20 percent:

| Constant | Widest tested range that stayed ok | What happens outside |
|---|---|---|
| TAU_SYN | 0.8 to 1.1 | at 1.2 resting activity jumps to 2.3x and stays up |
| SYN_MULT | 0.7 to 1.1 | at 1.2 resting activity jumps to 2.1x |
| ADAPT_INC | 0.9 to 2 | at 0.8 resting activity 2x |
| ADAPT_TAU | 0.9 to 2 | at 0.8 resting activity 2x |
| V_TH | 0.8 to 1.3 | 0.7 overactive, 1.5 silent |
| TAU_M | 0.7 to 1.5 | 0.5 overactive, 2 silent |
| DRIVE_AMP | 0.7 to 2 | 0.5 silent |
| DRIVE_RATE, DN_DRIVE_RATE, SENS_MULT, INH_MULT, BUS_RATE | 0.5 to 2 | nothing broke |
| ETA, FORGET | 0.5 to 2 | no effect on activity; ETA changes how much is learned; FORGET shows nothing in a 3 s test |

The jump in resting activity between 1.1 and 1.2 is sudden, not gradual: the network has a second, more active resting state.

With **all** constants drawn at once within plus or minus 20 percent (60 brains): 38 ok, 17 seizing, 5 silent. So about a third of children would be broken.

## Result 2: proposed per-gene bands
`headless/src/Bands.java`. Tight where the brain is fragile, wide where it is not:

TAU_M 0.85 to 1.25; V_TH 0.90 to 1.15; TAU_SYN 0.93 to 1.05; SYN_MULT 0.90 to 1.05; ADAPT_INC and ADAPT_TAU 0.95 to 1.40; DRIVE_AMP 0.85 to 1.30; DRIVE_RATE, DN_DRIVE_RATE, SENS_MULT, INH_MULT, BUS_RATE 0.70 to 1.40; ETA and FORGET 0.5 to 2.

60 brains with every gene random inside these bands: **51 ok, 0 seizing, 9 silent** (sluggish: leg response to threat under 30% of baseline). The bands lean quiet (median leg response 0.63 of baseline), so they still need re-centring. Sluggish flies are acceptable material for selection; seizing ones are not, and none occurred.

## Result 3: which circuit gains are worth making genes
Each group tested with an input that drives it, multipliers 0 to 4 on top of the author's gain:

| Group | Effect | Use as gene? |
|---|---|---|
| `aggr_out` | Aggression output scales smoothly: 0.033 at x0.5, 0.067 at x1, 0.20 at x2. The mod's trigger level is 0.035, so x0.5 is a fly that barely fights | Yes, 0.5 to 2 |
| `kc_mbon` | Mushroom-body output to odour scales smoothly, 0 at x0 to 6x at x4 | Yes, 0.5 to 2 |
| `compass_out` | Steering cells scale up from x1.25; no instability | Yes, 0.5 to 2 |
| `compass_loop` | Fine up to x1.25. At x2 the compass bump smears (0.90 to 0.34); at x4 it runs away | Narrow only, 0.5 to 1.25 |
| `mbon_out`, `lc_out`, `pc1_recurrent` | No measurable effect on any output we read | Not worth a gene yet |

## What this means for the genome (step 2)
- Use per-gene bands, not a flat 20 percent.
- Only 3 or 4 of the mod's circuit groups make useful genes. Genes for escape, feeding, courtship or sleep do not exist in the mod's data. Two honest ways to get them: per-channel sensory sensitivity (39 channels; `BUS_RATE` per channel, which the sweep shows is safe from 0.5 to 2), and new src/dst groups built from `brain_outputs.json`.
- The behaviour layer (`FlyMind`) mostly compares each output with that fly's own running baseline (for example legs "active" = above 8x baseline and above 0.05). So a gene that raises everything evenly changes behaviour less than the raw rates suggest. This can only be settled in game.

## Not checked
- Nothing in game: no fly has run with altered constants, so the link from these rates to visible behaviour is untested.
- One random seed per altered brain. Baseline seed-to-seed spread is about 5% on resting rate and under 1% when driven, so small differences in the tables (under about 10%, and all proboscis figures, which rest on a handful of spikes) are noise.
- Runs are seconds long. Slow effects are untested: sleep, the internal clock, hunger and thirst models, memory fading (FORGET), anything over minutes.
- `T_REFRAC`, `V_RESET`, `DT` and the learning gate floors were not varied.
- `mbon_out`, `lc_out` and `pc1_recurrent` may matter for outputs or situations we did not test.
- Male flies use a separate small circuit (`Circuit`, `LifNetwork`); it was not examined.

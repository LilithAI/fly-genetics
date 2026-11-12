# Fly Genetics — project brief and findings (written 2026-11-12)

A separate project from Zion Civ. Nothing has been built yet. This file is everything learned so far and the plan.

## What the owner wants
The owner has a hardcore Minecraft world whose capital is **Zion** (Minecraft 26.3, Fabric, CurseForge instance `~/Documents/curseforge/minecraft/Instances/green`, world `saves/first`). They want to **grow a population of brain-driven fruit flies in Zion**: flies that mate, lay eggs, and produce offspring that genuinely differ from each other and resemble their parents, so the population changes over generations. They also asked about putting the fly brain into villagers; see "Villagers" below.

Hard rule carried over from Zion Civ: `saves/first` is HARDCORE. Never open or test in it. Test only in a fresh world or a copy (`saves/first-test`).

## The source mod
- File: `~/Downloads/connectome-fly-1.0.0.jar` (19,631,064 bytes). SHA-256 `cb9665cc61cbd174dfccfdcb529aec59236313b084005d0c2f6f5f8895e707ce`, which matches the checksum published at https://flyproject.io/minecraft.
- "Connectome Fly" / "FruitFly Brain Mod" v1.0.0 by ro0oney (flyproject.io). Mod id `connectomefly`. Licence in `fabric.mod.json`: **CC-BY-4.0** (the web page says CC BY in one place and CC BY-SA in another). Building on it is allowed with credit; keep the credit to the fly project and to the connectome data's authors (BANC v888 via Codex/FlyWire; male courtship circuit from HHMI Janelia and Cambridge).
- Built for **Minecraft 1.20.1**, Fabric Loader >= 0.15, Java 17. It will not load in the owner's 26.3 instance.
- It has been **inspected only, never run**: file listing, `javap` signatures and constants, embedded strings.

## What the mod claims (from its page)
One creature, a fruit fly whose whole brain (158,262 neurons, about 3.1 million synapses) runs live as a spiking network, stepped 1000 times a second. World events drive receptor neurons (smell, taste, touch, hearing, vision, heat, wind, internal hunger/thirst). Motor and descending neuron activity moves the body: walk, turn, groom, escape jump, fly, feed, court, lay eggs. It states plainly that the wiring and synapse counts are real, the time constants, thresholds and gains are the author's calibration, and hunger, thirst, wind and "what a block smells like" are hand-written models. Each brain uses about one CPU core; at most half the machine's cores run brains at once; extra females wait with "brain queued". Males use a small circuit and cost almost nothing. Tested in single player only.

## What is actually inside (findings)
50 classes, package `io.flyproject.connectomefly`:
- `neural/WholeBrain` — the full-brain simulator. `neural/LifNetwork`, `Circuit`, `EscapeBrain`, `EscapeCircuit`, `SteeringCircuit`, `LoomingMath`, `NeuronJitter` — smaller circuits and helpers.
- `entity/ConnectomeFlyEntity` (51 KB), `entity/FlyMind` (88 KB, the behaviour layer), `entity/FlyEggEntity`.
- `client/*` renderer, rig, HUD, intro and credits screens. `lab/*` the `/flylab` command and 33 experiment boxes.

Data files under `data/connectomefly/`:
- `fullbrain.bin.gz` 9.5 MB — the wiring (format not yet decoded).
- `brain_circuit_gains.json` 1.1 MB — per-circuit gains. This is the natural place for a genome to act.
- `brain_channels.json` 195 KB (sensory channels), `brain_outputs.json` 21 KB (output neurons), `brain_compass.json` 31 KB, `circuit.json` 222 KB, `steering_circuit.json` 1.5 MB.

The key facts for genetics:
1. **Every fly shares one wiring map.** `WholeBrain` holds `private static volatile WholeBrain$Data shared`, loaded once under a lock via `static Data data()`. Each fly's `WholeBrain` instance has only its own *state* arrays: membrane voltage `v`, synaptic current `isyn`, adaptation `a`, spike buffers.
2. **Every fly uses the same tuning constants.** They are `private static final` in `WholeBrain`: `TAU_M=20`, `V_TH=1.0`, `V_RESET=0`, `T_REFRAC=2`, `TAU_SYN=5`, `DRIVE_RATE=0.001`, `DRIVE_AMP=10`, `DN_DRIVE_RATE=0.004`, `SYN_MULT=2.2`, `SENS_MULT=4.0`, `INH_MULT=1.1`, `ADAPT_INC=0.4`, `ADAPT_TAU=250`, `BUS_RATE=0.05`, `DT=1`, `K=0.05`; learning: `ETA=0.06`, `FORGET=2e-5`, `PAM_FLOOR=0.4`, `PAM_SCALE=2.0`, `PPL1_FLOOR=0.04`, `PPL1_SCALE=0.12`.
3. **The only per-fly brain difference today is learned memory**: `float[] plastScale` (scaled synapses in the mushroom body), `kcTrace`, `kcList`, updated by `plasticity(int,int)`, exposed via `memory()` and `dopamine()`. The page says it fades in about 15 minutes.
4. **`NeuronJitter.of(String)`** returns a value from a neuron id: a fixed per-neuron variation, the same in every fly. Not individual variation.
5. **An egg carries nothing from its parents.** `FlyEggEntity` has only `AGE`, `HATCH_TICKS` and `boolean fertile`. The mother has `mated`, `mate`, `eggDueT`, `acceptMate(ConnectomeFlyEntity)`, `layEgg(String)` / `layEgg(String, boolean)`. The hatchling therefore gets the same shared brain with fresh state: a behavioural clone.
6. Brain threading: `WholeBrain.start()/stop()/run()`, a static `RUNNING` counter, `ConnectomeFlyEntity.maxBrains`. Inputs arrive through `setBus(Map<String, Float>)` and `setCompassInput(float, float)`; outputs through `loco()`.
7. The 1.20.1 classes reference Minecraft through intermediary names (`net.minecraft.class_1297` and so on), so decompiled code needs remapping. This is harder than Zion Civ, whose 26.3 base was unobfuscated.

## The honest limit
In a real fly, genes shape the brain by directing how it grows. Nobody can simulate that. What can be built is **inheritance of tuning on top of real wiring**: invented rules, and they must be labelled as a model in game, the way the mod already tags its own rules `[model]`.

## Design: making it genetic
**Genome (per fly, a few hundred floats):**
- Global genes: per-fly versions of the constants above, each allowed to vary within a safe band around the author's value (for example plus or minus 20 percent), since they were calibrated together and wide changes will produce silent or seizing brains.
- Circuit genes: one multiplier per circuit or cell-type group from `brain_circuit_gains.json` (escape, feeding, courtship, locomotion, sleep, aggression, learning rate and so on).
- Optional body genes for visible variety: size, tint, wing-beat rate.

**Inheritance:** when the female accepts a mate, record both genomes. At `layEgg`, build the child genome by picking each gene from mother or father at random (crossover), then mutating a small fraction by a small amount. Store the genome in the egg's saved data and hand it to the hatchling. Males currently run a small circuit; they need a stored genome even if their own brain does not use all of it.

**Optional inherited memory:** copy a fraction of the mother's `plastScale` into the child, so offspring are born with a faint version of what she learned. Not real biology; make it a toggle and label it.

**Selection, so the population actually changes:** flies need a reason to differ in success. Suggested: a lifespan (the mod has none: "She does not age"), breeding only when fed, and death by starvation or predators. Then show per-generation statistics so drift is visible.

**Readout:** extend the mod's HUD to show generation number, parents, and the genes that differ most from the baseline. Tag all of it `[model]`.

**Why not a full copy of the wiring per fly:** 3.1 million individually varied synapses per fly is possible in memory for a few flies, but random changes at that level mostly produce noise, and it would end the shared-data design that keeps the mod affordable.

## Growing them in Zion
- **Cost is the main constraint.** About one CPU core per whole brain. Zion already runs 568 command blocks and many entities. Plan for 2 to 4 live brains at once, with the rest queued or asleep. Check the owner's core count first.
- **Containment.** Flies roam far under open sky. Build a roofed vivarium (the mod's "Home" box idea) and keep the population inside; consider a hard cap and a homing rule.
- **Do not combine with Zion Civ citizens' AI.** Keep the two mods independent; if both run in the same world, make sure the fly mod's commands, entities and tick cost do not interfere with command-block selectors (`@e[type=...]` selectors in Zion target vanilla villagers and tagged entities; a new entity type is safe, converting existing ones is not).
- **Test path:** fresh 26.3 test world, then a copy of `saves/first`, and only then the real world.

## Villagers
The owner also asked about putting the fly brain in a villager and breeding villagers. Assessment given: technically possible by mapping the brain's outputs (walk, turn, flee, groom, feed, sleep) onto a villager body, but the result behaves like a fly in a villager costume. It cannot farm, guard or follow orders, and it costs a core each. If wanted, treat it as a later novelty: a few "wild-minded" villagers in one district, separate from Zion Civ citizens.

## Build plan
0. **Set up and baseline.** New folder, for example `~/dev/fly-genetics`. Copy the jar into `original/`. Decompile with Vineflower. Because the 1.20.1 jar uses intermediary names, create a Fabric Loom project **for 1.20.1 first** (Loom remaps intermediary to named mappings), get it building unchanged, and run it in a fresh 1.20.1 dev world to see the real behaviour. Commit as baseline.
1. **Decode and document.** Work out the `fullbrain.bin.gz` format and how `brain_circuit_gains.json` is applied in `WholeBrain.step`. List which constants and gains are safe to vary and by how much (run brains headless with altered values and watch firing rates and behaviours).
2. **Genome.** Make the constants and circuit gains per-instance; add the genome class, save/load on fly and egg, crossover and mutation, generation counter and parent ids.
3. **Selection and readout.** Lifespan, fed-only breeding, HUD and a `/flylab genome` view, population statistics.
4. **Port to 26.3.** Update to current Loom, Fabric API and the 26.3 entity, rendering, save-data and networking APIs (they changed a great deal; see the notes in the Zion Civ project: check every signature with `javap`). The renderer and custom model code will be the largest part.
5. **Vivarium for Zion.** Containment, caps, performance check alongside Zion Civ, then the test-world, copy-of-world, real-world sequence.
6. Optional: inherited memory; villager bodies.

Testing approach that worked well on Zion Civ: Fabric client game tests (`./gradlew runClientGameTest`) that drive a real client on a throwaway world and take screenshots, plus headless server runs.

## Open questions for the owner
- How many CPU cores does the machine have?
- Should flies die (old age, hunger, predators)? Without death there is no selection.
- Where in Zion should the vivarium go, and how many flies at most?
- Is inherited memory wanted, or only inherited temperament?
- Flies only, or the villager-body idea as well?

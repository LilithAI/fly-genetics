# Step 2: the genome

Everything here is a `[model]`: inheritance of tuning on real wiring, not real genetics.

## What was built
- **Source tree.** The mod's code is now compilable source in `src/main/java/io/flyproject/connectomefly` (Loom-remapped jar, decompiled with Vineflower, three small decompiler errors fixed by hand). The original jar is no longer a dependency. Mod id stays `connectomefly`, so entity ids are unchanged; the name is "Connectome Fly: Genetics" and the original author and data credits are kept.
- **`genetics/Genome`**: 29 genes, each a multiplier (1.0 = the author's value) with its own band:
  - 14 brain constants, on the bands from step 1;
  - 4 circuit gains: `aggr_out`, `kc_mbon`, `compass_out` (0.5 to 2), `compass_loop` (0.5 to 1.25);
  - 11 sense genes (0.6 to 1.6): smell, taste, touch, hearing, vision, pheromone, pain, reward, hunger_drive, sleep_need, climate. Each scales how strongly that group of sensory channels drives its receptor neurons.
- **`neural/WholeBrain`**: tuning is per fly. Circuit genes are applied when a spike is delivered, using one byte per synapse in the shared wiring. A fly with no genome, or a baseline genome, runs exactly the author's values.
- **Inheritance.** When a female accepts a male she stores his genome. When she lays, the child's genome is made: each gene from mother or father at random, then each gene has a 20% chance of a small mutation (about 5.8 genes per child), clamped to its band. Generation = older parent's + 1; mother and father ids recorded. The genome is saved in the egg and given to the hatchling.
- **Founders.** Any fly that was spawned rather than hatched (spawn egg, `/summon`, `/flylab spawn`, flies from older saves) gets a generation-0 genome near the author's values, derived from its UUID so it is stable.
- **Males** carry and pass on a genome, but their own small circuit does not use it. `/flygenes` says so.
- **Inherited memory** (off by default): `/flygenes memory true|false`. When on, the egg carries 25% of the mother's learned mushroom-body weights. The setting is not saved; it is off again after a restart.
- **`/flygenes`** and **`/flygenes full`** (operators): generation, parents, mutation count and genes of flies and eggs within 64 blocks, with the `[model]` label.
- Tunables as JVM properties: `connectomefly.genetics.mutationRate`, `.mutationSize`, `.founderSpread`, `.inheritMemory`, `.inheritMemoryFraction`.

## What was checked
1. `./headless/check-genome.sh` (the mod's own classes, outside Minecraft), all passing:
   - 4,000 children: 40% of genes from mother, 40% from father, 20% mutated; none outside its band; generation and parents correct; egg with no father handled.
   - The modified brain with no genome and with a baseline genome matches the original jar measured the same way, and still runs 1000 steps a second.
   - Vision and touch genes at 1.6 raise the threat response (4.28 to 6.57 Hz; legs 0.30 to 0.50); at 0.6 they lower it (2.71 Hz; legs 0.16).
   - Aggression gene at 2 triples the aggression output (0.060 to 0.197); at 0.5 it falls to 0.033, just under the mod's 0.035 trigger; the rest of the brain is unchanged.
2. In game (`runAutoTest -Pautotest=breed`, then `-Pautotest=reload`; log in `breed-test-log.txt`, checked by `headless/verify_breed_log.py`):
   - two founders with different genomes; forced courtship; she laid two eggs, each generation 1 naming both parents;
   - first child: 11 genes from mother, 10 from father, 8 changed (9 mutations recorded; one landed on a band edge);
   - the hatchling carried the egg's genome;
   - after saving, quitting and reopening the world, all three flies had identical genomes, and the second egg hatched after the reload with the genome it was saved with.

## Not checked
- **Visible behaviour.** No test yet shows two flies with different genes behaving differently in the world. Only the brain's firing rates were compared, outside Minecraft.
- **Inherited memory** has not been run at all: not the copy, not the save, not its effect.
- Mating in the test was forced with `/flylab mate`. Natural courtship between flies with altered genes was not observed.
- The female in the test ran with her founder genome and reported healthy, but no long run (minutes to hours) of a gene-altered fly has been done.
- A second and later generation (children breeding) was not run.
- Founder spread, mutation rate and mutation size are first guesses, not tuned.
- The fly's body is still not identifiable in a screenshot.
- Decompiled code is behaviourally equal as far as the tests go (client test, brain rates); it was not compared class by class.

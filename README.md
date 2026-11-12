# Fly Genetics

Inherited tuning for the brain-driven fruit flies of **Connectome Fly**, to be grown in a vivarium in Zion.

Built on "Connectome Fly" v1.0.0 by **ro0oney (flyproject.io)**, licence CC-BY-4.0.
Connectome data: BANC v888 via Codex/FlyWire; male courtship circuit from HHMI Janelia and Cambridge.
Everything this project adds is a `[model]`: inheritance of tuning on top of real wiring, not real genetics.

Full brief: `FLY-PROJECT.md`.

## Owner decisions (2026-11-12)
- Flies die of old age, hunger and predators.
- Vivarium cap: 12 flies. Location to be given at step 5.
- Inherited memory: built as a toggle, off by default.
- CPU core count: unknown. Plan for 2 live brains until checked.

## Layout
- `original/connectome-fly-1.0.0.jar` — the unmodified mod (SHA-256 `cb9665cc…e707ce`, matches flyproject.io).
- `decompiled-intermediary/` — Vineflower 1.11.1 output. Minecraft names are still intermediary (`class_1297`); reference only, does not compile.
- `headless/` — runs the original whole brain outside Minecraft. `./headless/run.sh [seconds per phase]`. Last output in `headless/baseline-run.txt`.
- `build.gradle` etc. — Loom project for 1.20.1 that loads the original jar unchanged (`./gradlew runClient`).

## Step 0 status
Checked:
- Jar checksum.
- Decompile: 27 source files (50 classes).
- The unmodified `WholeBrain` loads and runs headless at 1000 steps a second on one core:
  158,262 neurons, 3,126,254 synapses, 39 sensory channels, 56 output groups, 4,085 plastic synapses.
  Mean firing rate about 0.25 Hz at rest, 1.4 Hz with food smell and sugar, 4.3 Hz with visual, touch and aversive input; leg motor neurons respond strongly to the last.

Not checked:
- **The Loom build has never been resolved or run.** The machine this was written on could not reach `maven.fabricmc.net`, `meta.fabricmc.net`, `piston-meta.mojang.com`, `libraries.minecraft.net` or `resources.download.minecraft.net`. The versions in `gradle.properties` are from memory and may need adjusting.
- The mod has not been run inside Minecraft. No world, no client test, no screenshots.
- No remap to named mappings yet, so there is no compilable source tree.

## Findings that change the brief
- `fullbrain.bin.gz` format is plain from the loader: little-endian `int n`, `int nEdges`, `int[n+1] indptr`, `int[nEdges] indices`, `float[nEdges] weight` (CSR, row = source neuron), `byte[n] sensory`, padding to 4 bytes, `byte[n] dnclass`.
- `brain_circuit_gains.json` has only **7** groups, not one per behaviour: `compass_loop` x2, `compass_out` x1, `kc_mbon` x20, `mbon_out` x1, `lc_out` x1, `aggr_out` x25, `pc1_recurrent` x1. Each is a `src` and `dst` neuron list; every synapse from src to dst is multiplied by the gain, **once, into the shared weights at load**. Per-fly circuit genes therefore need a per-fly multiplier applied in `step`, and behaviour genes such as escape, feeding or sleep need new groups built from the output and channel lists in `brain_outputs.json` and `brain_channels.json`.
- Gains can already be overridden for the whole process with `-Dconnectomefly.gain.<name>=<value>`, which is handy for step 1 sweeps.

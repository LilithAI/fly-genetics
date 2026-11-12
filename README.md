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
- `build.gradle` etc. — Loom project for 1.20.1 that loads the original jar unchanged.
- `src/main` — our mod `flygenetics`; so far only `test/AutoTest`, the automated client test (active only with `-Dflygenetics.autotest=true`).
- `docs/baseline/` — screenshots from the baseline client test.

## Step 0 status (done)
Toolchain that works: Loom 1.17.20, Gradle 9.7.1 (wrapper), JDK 21 compiling to Java 17, Yarn 1.20.1+build.10, loader 0.20.1, Fabric API 0.92.12+1.20.1. Loom 1.11 refuses the jar ("built with a newer version of Loom").

Checked:
- Jar checksum.
- Decompile: 27 source files (50 classes).
- Headless brain (`./headless/run.sh`): 158,262 neurons, 3,126,254 synapses, 1000 steps a second on one core. Mean rate about 0.25 Hz at rest, 1.4 Hz with food smell and sugar, 4.3 Hz with visual, touch and aversive input.
- `./gradlew build` succeeds with the original jar loaded unchanged.
- Dev server (`./gradlew runServer`, flat throwaway world): mod loads, two flies summoned, `/flylab brains` reports 2 whole brains running, `/flylab state` reports both "good", both flies moved from where they were summoned.
- Automated client test (`xvfb-run ./gradlew runAutoTest`, software rendering): joins `run/saves/devworld`, spawns two females and a male, takes four screenshots (`docs/baseline/`), exits cleanly. The mod's HUD shows live whole-brain readout for a female and the courtship circuit for a male ("approaching her").

Not checked:
- **The fly's body is not identifiable in any screenshot.** The HUD proves the entity is in view and tracked, but the model itself is tiny and the camera was not close enough. The renderer is therefore unconfirmed.
- Mating, egg laying and hatching have not been observed.
- `/flylab arena` and the 33 experiment boxes have not been built or tried.
- Sound (no audio device here) and real GPU rendering.
- No remap to named mappings yet, so the mod's own code is not yet a compilable source tree; only our small test helper in `src/main` compiles.

## Step 1 status (done, headless only)
See `docs/step1/README.md`. Short version: a flat plus or minus 20 percent breaks about a third of brains; per-gene bands (`headless/src/Bands.java`) gave 51 of 60 working and none seizing; only `aggr_out`, `kc_mbon`, `compass_out` and (narrowly) `compass_loop` are useful circuit genes. Nothing from step 1 has been checked in game.

## Findings that change the brief
- `fullbrain.bin.gz` format is plain from the loader: little-endian `int n`, `int nEdges`, `int[n+1] indptr`, `int[nEdges] indices`, `float[nEdges] weight` (CSR, row = source neuron), `byte[n] sensory`, padding to 4 bytes, `byte[n] dnclass`.
- `brain_circuit_gains.json` has only **7** groups, not one per behaviour: `compass_loop` x2, `compass_out` x1, `kc_mbon` x20, `mbon_out` x1, `lc_out` x1, `aggr_out` x25, `pc1_recurrent` x1. Each is a `src` and `dst` neuron list; every synapse from src to dst is multiplied by the gain, **once, into the shared weights at load**. Per-fly circuit genes therefore need a per-fly multiplier applied in `step`, and behaviour genes such as escape, feeding or sleep need new groups built from the output and channel lists in `brain_outputs.json` and `brain_channels.json`.
- Gains can already be overridden for the whole process with `-Dconnectomefly.gain.<name>=<value>`, which is handy for step 1 sweeps.

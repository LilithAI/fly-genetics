# Step 4: port to Minecraft 26.3

Branch `port-26.3`. The `main` branch stays on 1.20.1 as the reference and for the headless research tools.

Toolchain: Minecraft 26.3 (unobfuscated, official names), Java 25, Fabric Loader 0.20.1, Fabric API 0.166.2+26.3, Loom 1.19.4 (`net.fabricmc.fabric-loom`), Gradle 9.9.1.
Build: `JAVA_HOME=<jdk 25> ./gradlew build` gives `build/libs/fly-genetics-1.0.0+genetics.0.jar`.

## How it was done
1. **Names.** Loom's `migrateMappings` converted the source from Yarn to official Mojang names while still on 1.20.1. It missed three overridden methods (found by comparing, class by class, how many methods override a Minecraft method before and after) and created one accidental override (`isFlapping`, renamed `isWingFlapping`).
2. **Server side** (about 220 compile errors): entity registration, synced data, save data (`ValueInput`/`ValueOutput`), `hurtServer`, spawn reasons, day and night (world clocks), saved world data (codec-based), signs, written books, display entities, colour blocks, command permissions, moved animal classes. Small shims are in `compat/Compat.java`.
3. **Client side** (about 160 errors): HUD, the two screens, commands, and the renderers.

## What changed in behaviour (not just names)
- **The fly renderer was rewritten.** The original uploaded its own vertex buffers and drew them with direct OpenGL calls; 26.3 has no such path (it can run on Vulkan). The same mesh, CPU-skinned legs and wings included, is now sent through the standard entity pipeline. Consequences: the fly is lit by the world's light map and face normals instead of one hand-computed brightness; the hurt tint is the standard red overlay.
- **Detail levels.** Sending the full mesh (102,717 triangles) costs about 13 ms per fly per frame on the test machine. Two simplified copies are built at load (23,463 and 4,376 triangles). Full detail is used within 3 blocks, the middle one to 12 blocks, the coarse one beyond.
- **The egg** is a small cream box with the mod's egg texture, not a tinted turtle-egg block model (block models can no longer be drawn that way from an entity renderer).
- **The fallback box model was removed.** If the fly model fails to load, the bee body is drawn.
- **Leads** are drawn by vanilla now, not by the mod's own code.
- **Save format.** Parent ids in genomes are stored as text, not as integer arrays. Worlds from the 1.20.1 build are not meant to be carried over.
- **Frog tag** moved to `tags/entity_type/` (the folder was renamed). With the old path frogs silently ignored flies.
- Flowers as hand food use the `bee_food` item tag (`flowers` is gone).
- The in-game credits have a new section saying what the genetics fork added and that it is a `[model]`.

## What was checked on 26.3 (log: `26.3-test-log.txt`)
| Check | Result |
|---|---|
| Build | succeeds |
| Headless `check-genome.sh` on the 26.3 classes | all 11 pass |
| Dedicated server | loads; whole brains run; mating, egg, hatch, inheritance; old age |
| Client start | runs on software Vulkan (the container's OpenGL was rejected by 26.3) |
| Fly model | visible: body, head, legs, wings, shadow (`26.3-flies-and-egg.png`). This is the first time the fly's body has been seen in any test, on either version |
| Bee body, HUD with genetics lines | visible (`26.3-bee-body-and-hud.png`) |
| Health and hunger bars over the fly, egg | visible |
| Breeding test | child: 14 genes from mother, 9 from father, 6 mutated; second egg likewise |
| Save, quit, reopen | all flies identical; an egg saved mid-incubation hatched with its genome |
| Old age, starvation, hungry female refusing, breeding | each triggered as on 1.20.1 |
| Population cap | "egg refused by cap: 2 of 2" twice |
| Frog | ate a fly that could not move in 3 s, recorded as "predator (frog)" |
| `/flylab arena all`, book, loom shape, drum | run without errors; boxes visible (`26.3-boxes-and-hud.png`) |
| Credits screen | renders (`26.3-credits.png`) |

## Not checked
- **A real graphics card.** Everything was drawn by a software renderer at 3 to 26 frames a second, so real frame rates are unknown. The 13 ms per fly figure is CPU time on a cloud machine; a dozen flies within 3 blocks of the camera could still be slow.
- **The owner's instance.** Not tried in the CurseForge `green` instance, with its other mods, or anywhere near Zion.
- **Wing blur in flight, death animation, flies on walls and ceilings, leads, name tags, the spawn egg item's icon, the intro screen, `/flyhud` sizes**: code ported, not looked at.
- **Looks of the simplified meshes** up close: only seen at 3 to 6 blocks in one screenshot.
- **The 33 boxes** were built and one screenshot taken; no box's experiment was run, no sign or book was read in game.
- **The 1.20.1 and 26.3 fly side by side.** No 1.20.1 screenshot ever showed the fly, so whether it looks the same cannot be said.
- Sound (no audio device), multiplayer, long runs, the population script on 26.3.
- Inherited memory: still never run on either version.

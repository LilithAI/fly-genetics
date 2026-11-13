# Fly brain in any mob, the menu, and the new readout (26.3 branch)

## Any mob body `[model]`
- Body mode 3 of the mod's own entity: the fly's whole brain wearing the body of any living mob (cow, zombie, creeper, iron golem, axolotl...). It is still `connectomefly:connectome_fly`, so `@e[type=cow]` never selects it and it has none of that mob's behaviour or abilities (a fly-brained creeper does not explode, a fly-brained zombie does not attack).
- Drawn by that mob's own vanilla renderer, using a stand-in entity that is never added to the world. If a mob's renderer cannot cope, the bee body is drawn instead and a line is written to the log.
- Hitbox is the mob's. Ground mobs cannot fly or climb; bee, parrot, bat, phantom, allay, vex, ghast, happy ghast, blaze, wither, ender dragon and breeze bodies keep the fly's flight.
- Children take the mother's body. Different bodies can mate with each other.
- Items, projectiles, vehicles and players are refused.

## Menu (key G) and readout (key H)
- **G** opens the Fly Lab menu: choose a body (fly, villager, cow, or search any mob), sex, Spawn here, give the chosen body to the nearest creature, readout mode and size, how many whole brains run at once, adult life, growth speed, population cap, the death and hunger rules, inherited memory, statistics, gene list, field guide, the experiment boxes, credits. Each button sends the matching command, so the world needs cheats on. Value buttons set that value; the game answers in chat. The menu does not show the current values.
- **H** cycles the readout: compact, full, off. The choice is remembered. Both keys can be changed in Controls.
- **Compact readout:** what it is doing, generation, parents, genes, age, senses firing, brain outputs, bars for health, food and thirst, and a live map of the whole brain.
- **Brain map:** 512 cells, each the mean firing rate of about 309 neurons, updated five times a second from the running brain (dark = silent, brighter = faster, up to 13 Hz and above). The neurons are in the data's index order, **not laid out by anatomy**; the readout says so. Males have no map (they run the small courtship circuit).
- The card shrinks to fit if it would run off the bottom of the window.

## Checked (26.3 client under software rendering)
| Check | Result |
|---|---|
| Cow, zombie, creeper, chicken, iron golem, parrot, axolotl bodies | spawned and drawn (`fly-brained-mobs.png`) |
| `minecraft:arrow`, a made-up id | refused with a clear message |
| Breeding with mob bodies | eggs laid; children hatched with the mother's body type and both parents' genes |
| `@e[type=cow]` with fly-brained cows present | matched nothing |
| Changing the nearest creature's body | worked |
| Menu | opens, fits a 427 x 240 GUI (`menu.png`); a spawn and a setting sent the way the buttons send them both worked for an operator player |
| Readout | compact and full both fit (`hud-compact-with-brain-map.png`, `hud-full.png`); the brain map changes with activity (354 to 400 of 512 groups firing at rest) |
| Earlier tests | breeding, reload and headless checks still pass |

## Not checked
- **No button was clicked with a mouse.** The test opened the menu and sent the same commands the buttons send; the search box was not typed into.
- Only seven mob bodies were drawn. Others (ender dragon, wither, guardian, slime sizes, mobs with layers such as sheep wool or horse armour) may look wrong or fall back to the bee.
- Whether each body moves sensibly: speeds and food-finding distances are the fly's; a big body may look odd or get stuck.
- Flying bodies in flight; water mobs in water (an axolotl body still lives on land like the fly).
- How real mobs treat the borrowed bodies (golems, zombies, wolves).
- The H and G keys themselves (key presses were not simulated), and whether G or H clash with other mods' keys.
- A real graphics card: you have now seen the fly on your machine, but none of this build has run there yet.

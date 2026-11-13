package io.flyproject.flygenetics.test;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.MinecraftServer;

/**
 * Automated client test, active only with -Dflygenetics.autotest=true.
 * Joins the throwaway dev world (quick play), spawns flies in front of the player,
 * saves screenshots to run/screenshots/ and closes the game.
 */
public class AutoTest implements ClientModInitializer {
   private int ticks = -1;

   @Override
   public void onInitializeClient() {
      if (System.getProperty("flygenetics.autotest") == null) return;
      ClientTickEvents.END_CLIENT_TICK.register(this::tick);
   }

   private void tick(Minecraft c) {
      if (c.level == null || c.player == null || c.getSingleplayerServer() == null) return;
      c.options.pauseOnLostFocus = false;
      if (c.gui.screen() != null && !(c.gui.screen() instanceof io.flyproject.connectomefly.client.CreditsScreen) && !(c.gui.screen() instanceof io.flyproject.connectomefly.client.FlyMenuScreen)) c.gui.setScreen(null);
      ticks++;
      String mode = System.getProperty("flygenetics.autotest");
      if (mode.equals("breed")) {
         breed(c);
      } else if (mode.equals("look")) {
         // port check: flies that cannot move, right in front of the camera, at three distances
         if (ticks == 20) run(c, CLEAR, "kill @e[type=connectomefly:fly_egg]", "tp @a 0.5 -60 0.5 0 25");
         if (ticks == 39 || ticks == 139 || ticks == 229 || ticks == 399) {
            int fps = Math.max(1, c.getFps());
            System.out.println("[flygenetics autotest] tick " + ticks + " fps " + fps + "; fly vertex sending over the last ~5 s: "
               + io.flyproject.connectomefly.client.FlyRig.emitNanos / 1000000L + " ms for " + io.flyproject.connectomefly.client.FlyRig.emitTris + " triangles");
         }
         if (ticks == 40 || ticks == 130 || ticks == 300) {
            io.flyproject.connectomefly.client.FlyRig.emitNanos = 0L;
            io.flyproject.connectomefly.client.FlyRig.emitTris = 0L;
         }
         if (ticks == 40) {
            io.flyproject.connectomefly.client.FlyHud.enabled = false;
            run(c, "flygenes set adult_life_minutes 120", "time set noon", "weather clear", "gamemode creative @a", CLEAR, "kill @e[type=connectomefly:fly_egg]", "tp @a 0.5 -60 0.5 0 25",
               "summon connectomefly:connectome_fly 0.5 -60 2.2 {NoAI:1b}", "summon connectomefly:connectome_fly -1.0 -60 3.5 {NoAI:1b,Male:1b}",
               "summon connectomefly:connectome_fly 2.0 -60 6.0 {NoAI:1b}", "summon connectomefly:fly_egg 1.3 -60 2.2");
         }
         if (ticks == 140) {
            shot(c, "look-1.png");
         }
         if (ticks == 150) {
            io.flyproject.connectomefly.client.FlyHud.enabled = true;
            run(c, "tp @a 0.5 -60 1.2 0 40", "flylab body bee");
         }
         if (ticks == 230) shot(c, "look-2-bee.png");
         if (ticks == 240) run(c, "flylab body model", "tp @a 0.5 -60 0.5 0 25");
         if (ticks == 400) shot(c, "look-3-free.png");
         if (ticks == 420) {
            System.out.println("[flygenetics autotest] done");
            c.stop();
         }
      } else if (mode.equals("keys")) {
         // press G and H the way the game does when the keys are hit, and report what happened
         int t = ticks;
         com.mojang.blaze3d.platform.InputConstants.Key g = com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(com.mojang.blaze3d.platform.InputConstants.KEY_G);
         com.mojang.blaze3d.platform.InputConstants.Key h = com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(com.mojang.blaze3d.platform.InputConstants.KEY_H);
         if (t == 40) { io.flyproject.connectomefly.client.FlyHud.setEnabled(true); io.flyproject.connectomefly.client.FlyHud.setDetail(false); }
         if (t == 50) net.minecraft.client.KeyMapping.click(g);
         if (t == 54) System.out.println("[flygenetics autotest] after G the screen is " + (c.gui.screen() == null ? "none" : c.gui.screen().getClass().getSimpleName()));
         if (t == 56) c.gui.setScreen(null);
         if (t == 60 || t == 64 || t == 68) net.minecraft.client.KeyMapping.click(h);
         if (t == 62 || t == 66 || t == 70) System.out.println("[flygenetics autotest] after H the readout is " + io.flyproject.connectomefly.client.FlyHud.modeName());
         if (t == 80) {
            System.out.println("[flygenetics autotest] done");
            c.stop();
         }
      } else if (mode.equals("menu")) {
         // the new readout (compact and full) and the menu screen; one menu button is pressed for real
         int t = ticks;
         if (t == 40) {
            io.flyproject.connectomefly.client.FlyHud.setEnabled(true);
            io.flyproject.connectomefly.client.FlyHud.setDetail(false);
            run(c, "flygenes set adult_life_minutes 120", "time set noon", "weather clear", "gamemode creative @a", CLEAR, "kill @e[type=connectomefly:fly_egg]", "tp @a 0.5 -60 0.5 0 20",
               "summon connectomefly:connectome_fly 0.5 -60 3.0");
            // stand in for "cheats on": make the test player an operator
            MinecraftServer srv = c.getSingleplayerServer();
            srv.execute(() -> srv.getPlayerList().getPlayers().forEach(pl -> srv.getPlayerList().op(pl.nameAndId())));
         }
         if (t == 120) run(c, "tp @a 0.5 -60 1.5 0 30");
         if (t == 200) shot(c, "hud-compact.png");
         if (t == 204) shot(c, "hud-compact-b.png");
         if (t == 210) io.flyproject.connectomefly.client.FlyHud.setDetail(true);
         if (t == 260) shot(c, "hud-full.png");
         if (t == 270) { io.flyproject.connectomefly.client.FlyHud.setDetail(false); c.gui.setScreen(new io.flyproject.connectomefly.client.FlyMenuScreen()); }
         if (t == 310) shot(c, "menu.png");
         if (t == 320) { c.gui.setScreen(null); c.player.connection.sendCommand("flygenes spawn mob minecraft:sheep female"); c.player.connection.sendCommand("flygenes set cap 24"); }
         if (t == 380) { run(c, "flygenes", "flygenes set"); shot(c, "menu-after.png"); }
         if (t == 400) {
            System.out.println("[flygenetics autotest] done");
            c.stop();
         }
      } else if (mode.equals("mobs")) {
         // fly brain in other mobs' bodies: several types side by side, a pair of cows left to breed
         int t = ticks;
         if (t == 30) io.flyproject.connectomefly.client.FlyHud.enabled = false;
         if (t == 40) {
            run(c, "flygenes set adult_life_minutes 120", "flygenes set cap 20", "time set noon", "weather clear", "gamemode creative @a", CLEAR, "kill @e[type=connectomefly:fly_egg]",
               "tp @a 0.5 -60 0.5 0 12",
               "execute positioned -4.0 -60 6.0 run flygenes spawn mob minecraft:cow female", "execute positioned -2.5 -60 6.5 run flygenes spawn mob minecraft:cow male",
               "execute positioned -0.5 -60 6.0 run flygenes spawn mob minecraft:zombie", "execute positioned 1.0 -60 6.0 run flygenes spawn mob minecraft:creeper",
               "execute positioned 2.5 -60 6.0 run flygenes spawn mob minecraft:chicken", "execute positioned 4.0 -60 6.0 run flygenes spawn mob minecraft:iron_golem male",
               "execute positioned 0.5 -60 9.0 run flygenes spawn mob minecraft:parrot male", "execute positioned 5.5 -60 7.0 run flygenes spawn mob minecraft:axolotl male",
               "flygenes spawn mob minecraft:arrow", "flygenes spawn mob minecraft:not_a_mob");
         }
         if (t == 60) run(c, "data merge entity @e[type=connectomefly:connectome_fly,limit=1,sort=nearest] {}", "flygenes full");
         if (t == 100) { shot(c, "mobs-1.png"); io.flyproject.connectomefly.client.FlyHud.enabled = true; }
         if (t == 160) shot(c, "mobs-2-hud.png");
         if (t == 170) io.flyproject.connectomefly.client.FlyHud.enabled = false;
         if (t > 220 && t % 300 == 0) run(c, "flygenes");
         if (t == 2100) {
            run(c, "flygenes full", "execute as @e[type=connectomefly:connectome_fly] run data get entity @s BodyType", "execute if entity @e[type=cow] run say REAL COW SELECTED",
               "execute unless entity @e[type=cow] run say no entity matches type=cow", "execute as @e[type=connectomefly:connectome_fly,limit=1,sort=nearest] at @s run flygenes body minecraft:pig");
         }
         if (t == 2160) shot(c, "mobs-3-after.png");
         if (t == 2180) {
            System.out.println("[flygenetics autotest] done");
            c.stop();
         }
      } else if (mode.equals("villager")) {
         villager(c);
      } else if (mode.equals("arena")) {
         // the experiment boxes: signs, books, display panels, the intro and credits screens
         if (ticks == 40) run(c, CLEAR, "gamemode creative @a", "tp @a 0.5 -60 0.5 0 10", "execute as @a at @s run flylab arena all");
         if (ticks == 120) run(c, "execute as @a at @s run flylab book", "execute as @a at @s run flylab spawn female", "execute as @a at @s run flylab loom", "execute as @a at @s run flylab drum left");
         if (ticks == 200) shot(c, "arena.png");
         if (ticks == 210) c.gui.setScreen(new io.flyproject.connectomefly.client.CreditsScreen());
         if (ticks == 230) shot(c, "credits.png");
         if (ticks == 240) {
            System.out.println("[flygenetics autotest] done");
            c.stop();
         }
      } else if (mode.equals("frog")) {
         // is a fly frog food? A fly that cannot move, a frog beside it.
         if (ticks == 40) run(c, "flygenes set adult_life_minutes 120", CLEAR, "kill @e[type=connectomefly:fly_egg]", "kill @e[type=frog]", "tp @a 0.5 -60 0.5 0 35");
         if (ticks == 60) run(c, "say PHASE F1 frog and a fly that cannot move", "summon connectomefly:connectome_fly 6.5 -60 6.5 {NoAI:1b,Male:1b}", "summon frog 8.5 -60 6.5",
            "execute as @e[type=connectomefly:connectome_fly] if entity @s[type=#minecraft:frog_food] run say fly is tagged frog_food");
         if (ticks == 900) run(c, "flygenes stats", "kill @e[type=frog]");
         if (ticks == 940) {
            System.out.println("[flygenetics autotest] done");
            c.stop();
         }
      } else if (mode.equals("cap")) {
         // a fresh, fed pair with the cap (2) already reached by the pair itself: they mate, no egg
         if (ticks == 40) run(c, "kill @e[type=connectomefly:fly_egg]", CLEAR, "kill @e[type=frog]", "tp @a 0.5 -60 0.5 0 35");
         if (ticks == 60) run(c, "say PHASE D cap", "flygenes set cap 2", "summon connectomefly:connectome_fly 0.5 -60 2.5", "execute positioned 0.5 -60 2.5 run flylab spawn male");
         if (ticks == 1300) run(c, "flygenes stats", "flygenes", "flygenes set cap 12");
         if (ticks == 1340) {
            System.out.println("[flygenetics autotest] done");
            c.stop();
         }
      } else if (mode.equals("select")) {
         select(c);
      } else if (mode.equals("reload")) {
         if (ticks == 100) run(c, "flygenes full");
         if (ticks == 160) {
            System.out.println("[flygenetics autotest] done");
            c.stop();
         }
      } else {
         baseline(c);
      }
   }

   /** One female and one male: forced courtship, mating, egg, hatch. Genomes are printed to the log at each stage. */
   private void breed(Minecraft c) {
      if (ticks == 40) {
         run(c, "flygenes set adult_life_minutes 120", "time set noon", "weather clear", "gamemode creative @a", "kill @e[type=connectomefly:connectome_fly]", "kill @e[type=connectomefly:fly_egg]",
            "tp @a 0.5 -60 0.5 0 35", "summon connectomefly:connectome_fly 0.5 -60 2.5",
            "summon connectomefly:connectome_fly 1.5 -60 2.5 {Male:1b}");
      }
      if (ticks == 120) run(c, "flygenes full");
      if (ticks > 120 && ticks % 100 == 0) run(c, "flygenes", "execute as @e[type=connectomefly:fly_egg] run data get entity @s Genome");
      if (ticks == 300 || ticks == 1000 || ticks == 1790) shot(c, "breed-t" + ticks + ".png");
      if (ticks == 1800) run(c, "flygenes full", "execute as @e[type=connectomefly:connectome_fly] run data get entity @s Genome");
      if (ticks == 1860) {
         System.out.println("[flygenetics autotest] done");
         c.stop();
      }
   }

   /** Fly-brained villagers: look, breed, check the child is a villager with both parents' genes, check vanilla selectors ignore them. */
   private void villager(Minecraft c) {
      int t = ticks;
      if (t == 30) io.flyproject.connectomefly.client.FlyHud.enabled = false;
      if (t == 40) {
         run(c, "flygenes set adult_life_minutes 120", "flygenes set cap 12", "time set noon", "weather clear", "gamemode creative @a", CLEAR, "kill @e[type=connectomefly:fly_egg]", "kill @e[type=frog]",
            "kill @e[type=villager]", "tp @a 0.5 -60 0.5 0 15",
            "execute positioned 0.5 -60 4.0 run flygenes spawn villager female", "execute positioned 2.0 -60 4.5 run flygenes spawn villager male",
            "summon villager -2.5 -60 4.5 {NoAI:1b}");
      }
      if (t == 80) run(c, "flygenes full", "execute if entity @e[type=villager] run say COUNT vanilla selector matches a real villager",
         "say CHECK how many entities @e[type=villager] selects:", "execute as @e[type=villager] run say I am selected by type=villager",
         "execute as @e[type=connectomefly:connectome_fly] run say I am selected by type=connectomefly:connectome_fly");
      if (t == 140) shot(c, "villager-1.png");
      if (t == 150) io.flyproject.connectomefly.client.FlyHud.enabled = true;
      if (t == 220) shot(c, "villager-2-hud.png");
      if (t > 220 && t % 200 == 0) run(c, "flygenes", "flylab state");
      if (t == 2300) {
         run(c, "flygenes full", "flygenes stats", "execute as @e[type=connectomefly:connectome_fly] run data get entity @s Body");
         run(c, "execute as @a at @s run tp @s @e[type=connectomefly:connectome_fly,limit=1,sort=nearest]", "execute as @a at @s run tp @s ~ ~ ~-3 0 10");
         io.flyproject.connectomefly.client.FlyHud.enabled = false;
      }
      if (t == 2360) shot(c, "villager-3-after.png");
      if (t == 2380) {
         System.out.println("[flygenetics autotest] done");
         c.stop();
      }
   }

   private static final String F = "@e[type=connectomefly:connectome_fly,nbt={Male:0b}]";
   private static final String CLEAR = "kill @e[type=connectomefly:connectome_fly]";

   /** Selection rules, one phase each: old age, starvation, fed-only breeding, population cap, breeding, predator. */
   private void select(Minecraft c) {
      int t = ticks;
      if (t == 40) {
         run(c, "time set noon", "weather clear", "gamemode creative @a", CLEAR, "kill @e[type=connectomefly:fly_egg]", "kill @e[type=frog]",
            "tp @a 0.5 -60 0.5 0 35", "flygenes set");
         // A: old age. Adult life 0.3 min, so both die 14 to 22 s after spawning.
         run(c, "say PHASE A old age", "flygenes set adult_life_minutes 0.3", "summon connectomefly:connectome_fly 0.5 -60 2.5", "execute positioned 0.5 -60 2.5 run flylab spawn male");
      }
      if (t == 640) run(c, "flygenes stats");
      if (t == 660) {
         // B: starvation. Long life, empty stomach, 2 health: 0.5 damage every 4 s.
         run(c, CLEAR, "say PHASE B starvation", "flygenes set adult_life_minutes 120", "summon connectomefly:connectome_fly 0.5 -60 2.5 {Hunger:0f,Health:2f}");
      }
      if (t == 1160) run(c, "flygenes stats");
      if (t == 1180) {
         // C: a hungry female must refuse. Hunger is held at 5 (below breed_hunger 12).
         run(c, CLEAR, "say PHASE C hungry female", "summon connectomefly:connectome_fly 0.5 -60 2.5 {Hunger:5f}", "execute positioned 0.5 -60 2.5 run flylab mate");
      }
      if (t > 1180 && t < 2060 && t % 40 == 0) run(c, "data merge entity " + F.replace("]", ",limit=1]") + " {Hunger:5f}");
      if (t == 2060) run(c, "flygenes stats", "flygenes");
      if (t == 2900) {
         run(c, "execute as @a at @s run tp @s " + F.replace("]", ",limit=1,sort=nearest]"), "execute as @a at @s run tp @s ~ ~ ~-1.2 0 40");
      }
      if (t == 2960) shot(c, "select-hud.png");
      if (t == 2980) {
         // E: cap raised, fresh pair: she lays, and the egg hatches.
         run(c, CLEAR, "say PHASE E breeding allowed", "flygenes set cap 12", "tp @a 0.5 -60 0.5 0 35", "summon connectomefly:connectome_fly 0.5 -60 2.5", "execute positioned 0.5 -60 2.5 run flylab mate");
      }
      if (t == 4100) run(c, "flygenes stats", "flygenes full");
      if (t == 4120) {
         // F1: is a fly frog food at all? A fly that cannot move (NoAI), a frog beside it.
         run(c, CLEAR, "kill @e[type=connectomefly:fly_egg]", "say PHASE F1 frog and a fly that cannot move", "summon connectomefly:connectome_fly 6.5 -60 6.5 {NoAI:1b,Male:1b}", "summon frog 8.5 -60 6.5",
            "execute as @e[type=connectomefly:connectome_fly] if entity @s[type=#minecraft:frog_food] run say fly is tagged frog_food");
      }
      if (t == 4700) run(c, "flygenes stats");
      if (t == 4720) {
         // F2: free flies among frogs. They may well escape; this is an observation, not a pass or fail.
         run(c, CLEAR, "kill @e[type=frog]", "say PHASE F2 frogs and free flies", "summon connectomefly:connectome_fly 0.5 -60 2.5", "execute positioned 0.5 -60 2.5 run flylab spawn male",
            "summon frog 2.5 -60 2.5", "summon frog -1.5 -60 2.5", "summon frog 0.5 -60 4.5", "summon frog 3.5 -60 4.5");
      }
      if (t == 5000) shot(c, "select-frogs.png");
      if (t == 6300) run(c, "flygenes stats", "flygenes", "flygenes set");
      if (t == 6360) {
         System.out.println("[flygenetics autotest] done");
         c.stop();
      }
   }

   private void baseline(Minecraft c) {
      if (ticks == 40) {
         run(c, "time set noon", "weather clear", "gamemode creative @a", "kill @e[type=connectomefly:connectome_fly]",
            "tp @a 0.5 -60 0.5 0 35",
            "summon connectomefly:connectome_fly 0.5 -60 2.0",
            "summon connectomefly:connectome_fly 1.2 -60 2.6",
            "execute as @a at @s run flylab spawn male");
      }
      if (ticks == 100 || ticks == 300 || ticks == 700) {
         run(c, "flylab state", "flylab brains");
         shot(c, "autotest-t" + ticks + ".png");
      }
      if (ticks == 320) run(c, "execute as @a at @s run tp @s @e[type=connectomefly:connectome_fly,limit=1,sort=nearest]", "execute as @a at @s run tp @s ~ ~ ~-1.2 0 40");
      if (ticks == 400) shot(c, "autotest-t400-close.png");
      if (ticks == 720) {
         System.out.println("[flygenetics autotest] done");
         c.stop();
      }
   }

   private static void run(Minecraft c, String... cmds) {
      MinecraftServer s = c.getSingleplayerServer();
      s.execute(() -> {
         for (String cmd : cmds) {
            s.getCommands().performPrefixedCommand(s.createCommandSourceStack(), cmd);
            int r = 0;
            System.out.println("[flygenetics autotest] /" + cmd + " -> " + r);
         }
      });
   }

   private static void shot(Minecraft c, String name) {
      Screenshot.grab(c.gameDirectory, name, c.gameRenderer.mainRenderTarget(), 1,
         t -> System.out.println("[flygenetics autotest] screenshot " + name + ": " + t.getString()));
   }
}

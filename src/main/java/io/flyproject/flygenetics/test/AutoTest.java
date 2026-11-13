package io.flyproject.flygenetics.test;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
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

   private void tick(MinecraftClient c) {
      if (c.world == null || c.player == null || c.getServer() == null) return;
      c.options.pauseOnLostFocus = false;
      if (c.currentScreen != null) c.setScreen(null);
      ticks++;
      String mode = System.getProperty("flygenetics.autotest");
      if (mode.equals("breed")) {
         breed(c);
      } else if (mode.equals("cap")) {
         // a fresh, fed pair with the cap (2) already reached by the pair itself: they mate, no egg
         if (ticks == 40) run(c, "kill @e[type=connectomefly:fly_egg]", CLEAR, "kill @e[type=frog]", "tp @a 0.5 -60 0.5 0 35");
         if (ticks == 60) run(c, "say PHASE D cap", "flygenes set cap 2", "summon connectomefly:connectome_fly 0.5 -60 2.5", "execute positioned 0.5 -60 2.5 run flylab spawn male");
         if (ticks == 1300) run(c, "flygenes stats", "flygenes", "flygenes set cap 12");
         if (ticks == 1340) {
            System.out.println("[flygenetics autotest] done");
            c.scheduleStop();
         }
      } else if (mode.equals("select")) {
         select(c);
      } else if (mode.equals("reload")) {
         if (ticks == 100) run(c, "flygenes full");
         if (ticks == 160) {
            System.out.println("[flygenetics autotest] done");
            c.scheduleStop();
         }
      } else {
         baseline(c);
      }
   }

   /** One female and one male: forced courtship, mating, egg, hatch. Genomes are printed to the log at each stage. */
   private void breed(MinecraftClient c) {
      if (ticks == 40) {
         run(c, "time set noon", "weather clear", "gamemode creative @a", "kill @e[type=connectomefly:connectome_fly]", "kill @e[type=connectomefly:fly_egg]",
            "tp @a 0.5 -60 0.5 0 35", "summon connectomefly:connectome_fly 0.5 -60 2.5",
            "execute positioned 0.5 -60 2.5 run flylab mate");
      }
      if (ticks == 120) run(c, "flygenes full");
      if (ticks > 120 && ticks % 100 == 0) run(c, "flygenes", "execute as @e[type=connectomefly:fly_egg] run data get entity @s Genome");
      if (ticks == 300 || ticks == 1000 || ticks == 1790) shot(c, "breed-t" + ticks + ".png");
      if (ticks == 1800) run(c, "flygenes full", "execute as @e[type=connectomefly:connectome_fly] run data get entity @s Genome");
      if (ticks == 1860) {
         System.out.println("[flygenetics autotest] done");
         c.scheduleStop();
      }
   }

   private static final String F = "@e[type=connectomefly:connectome_fly,nbt={Male:0b}]";
   private static final String CLEAR = "kill @e[type=connectomefly:connectome_fly]";

   /** Selection rules, one phase each: old age, starvation, fed-only breeding, population cap, breeding, predator. */
   private void select(MinecraftClient c) {
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
            "execute if entity @e[type=#minecraft:frog_food] run say fly is tagged frog_food");
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
         c.scheduleStop();
      }
   }

   private void baseline(MinecraftClient c) {
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
         c.scheduleStop();
      }
   }

   private static void run(MinecraftClient c, String... cmds) {
      MinecraftServer s = c.getServer();
      s.execute(() -> {
         for (String cmd : cmds) {
            int r = s.getCommandManager().executeWithPrefix(s.getCommandSource(), cmd);
            System.out.println("[flygenetics autotest] /" + cmd + " -> " + r);
         }
      });
   }

   private static void shot(MinecraftClient c, String name) {
      ScreenshotRecorder.saveScreenshot(c.runDirectory, name, c.getFramebuffer(),
         t -> System.out.println("[flygenetics autotest] screenshot " + name + ": " + t.getString()));
   }
}

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

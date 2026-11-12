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

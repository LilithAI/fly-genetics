package io.flyproject.connectomefly.genetics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import io.flyproject.connectomefly.ConnectomeFlyMod;
import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import io.flyproject.connectomefly.entity.FlyEggEntity;
import java.util.List;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;

/** /flygenes: read the [model] genomes of nearby flies and eggs, and switch inherited memory. */
public final class GeneticsCommand {
   private static final String MODEL = "[model] inherited tuning on real wiring, not real genetics. ";

   private GeneticsCommand() {
   }

   public static void register(CommandDispatcher<ServerCommandSource> d) {
      d.register(
         CommandManager.literal("flygenes")
            .requires(s -> s.hasPermissionLevel(2))
            .executes(ctx -> list(ctx.getSource(), false))
            .then(CommandManager.literal("full").executes(ctx -> list(ctx.getSource(), true)))
            .then(CommandManager.literal("stats").executes(ctx -> stats(ctx.getSource())))
            .then(
               CommandManager.literal("set")
                  .executes(ctx -> set(ctx.getSource(), null, 0.0F))
                  .then(
                     CommandManager.argument("key", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .then(
                           CommandManager.argument("value", com.mojang.brigadier.arguments.FloatArgumentType.floatArg())
                              .executes(
                                 ctx -> set(
                                    ctx.getSource(),
                                    com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "key"),
                                    com.mojang.brigadier.arguments.FloatArgumentType.getFloat(ctx, "value")
                                 )
                              )
                        )
                  )
            )
            .then(
               CommandManager.literal("memory")
                  .executes(ctx -> memory(ctx.getSource(), null))
                  .then(CommandManager.argument("on", BoolArgumentType.bool()).executes(ctx -> memory(ctx.getSource(), BoolArgumentType.getBool(ctx, "on"))))
            )
      );
   }

   private static int memory(ServerCommandSource src, Boolean on) {
      GeneticsState st = GeneticsState.get(src.getServer());
      if (on != null) {
         st.set("inherit_memory", on ? 1.0F : 0.0F);
      }

      src.sendFeedback(
         () -> Text.literal(
               "[model] inherited memory is "
                  + (Genome.inheritMemory ? "ON" : "OFF")
                  + ": a child starts with "
                  + Math.round(Genome.inheritMemoryFraction * 100.0F)
                  + "% of what its mother had learned. Not biology. Saved with the world."
            )
            .formatted(Formatting.AQUA),
         false
      );
      return 1;
   }

   private static void say(ServerCommandSource src, String line, Formatting f) {
      src.sendFeedback(() -> Text.literal(line).formatted(f), false);
   }

   private static int set(ServerCommandSource src, String key, float value) {
      GeneticsState st = GeneticsState.get(src.getServer());
      String r = key == null ? st.settings() : st.set(key, value);
      say(src, r == null ? "unknown setting. " + st.settings() : "[model] settings (saved with the world; 1 = on, 0 = off): " + r, r == null ? Formatting.RED : Formatting.AQUA);
      return r == null ? 0 : 1;
   }

   private static int stats(ServerCommandSource src) {
      GeneticsState st = GeneticsState.get(src.getServer());
      say(src, MODEL + "Population statistics for this world.", Formatting.AQUA);
      java.util.TreeMap<Integer, int[]> living = new java.util.TreeMap<>();
      float[] liveSum = new float[Genome.N];
      int alive = 0;

      for (ServerWorld w : src.getServer().getWorlds()) {
         for (ConnectomeFlyEntity f : w.getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, e -> e.isAlive())) {
            int[] c = living.computeIfAbsent(f.genome().generation, k -> new int[2]);
            c[f.isMale() ? 1 : 0]++;
            alive++;

            for (int i = 0; i < Genome.N; i++) {
               liveSum[i] += f.genome().g[i];
            }
         }
      }

      StringBuilder lv = new StringBuilder("alive in loaded chunks: " + alive);

      for (java.util.Map.Entry<Integer, int[]> e : living.entrySet()) {
         lv.append("; gen ").append(e.getKey()).append(": ").append(e.getValue()[0]).append(" female ").append(e.getValue()[1]).append(" male");
      }

      say(src, lv.toString(), Formatting.GRAY);
      StringBuilder bn = new StringBuilder("born:");

      for (java.util.Map.Entry<Integer, Integer> e : st.born.entrySet()) {
         bn.append(" gen ").append(e.getKey()).append(": ").append(e.getValue()).append(';');
      }

      say(src, bn + " eggs laid " + st.eggsLaid + ", eggs refused by cap " + st.eggsRefusedCap + ", matings refused (hungry) " + st.matingsRefusedHungry + ", hatchlings whose sex was set by the balance rule " + st.sexBalanced, Formatting.GRAY);
      StringBuilder dd = new StringBuilder("deaths:");

      for (String c : GeneticsState.CAUSES) {
         int n = st.deaths.getOrDefault(c, 0);
         dd.append(' ').append(c).append(' ').append(n);
         if (n > 0) {
            dd.append(String.format(java.util.Locale.ROOT, " (mean adult age %.1f min)", st.deathAdultMinutes.getOrDefault(c, 0.0) / n));
         }

         dd.append(';');
      }

      say(src, dd.toString(), Formatting.GRAY);
      if (!st.born.isEmpty()) {
         int first = st.born.firstKey();
         int last = st.born.lastKey();
         if (last != first) {
            say(src, "gene means, generation " + last + " (" + st.born.get(last) + " born) against generation " + first + " (" + st.born.get(first) + " born), largest shifts:", Formatting.GRAY);
            say(src, shifts(i -> st.mean(last, i), i -> st.mean(first, i)), Formatting.GRAY);
         }

         if (alive > 0) {
            int n = alive;
            say(src, "gene means, flies alive now against generation " + first + ", largest shifts:", Formatting.GRAY);
            say(src, shifts(i -> liveSum[i] / n, i -> st.mean(first, i)), Formatting.GRAY);
         }
      }

      say(src, "With few flies these shifts are mostly chance (drift), not selection.", Formatting.DARK_GRAY);
      return 1;
   }

   private static String shifts(java.util.function.IntFunction<Float> now, java.util.function.IntFunction<Float> then) {
      Integer[] order = new Integer[Genome.N];

      for (int i = 0; i < Genome.N; i++) {
         order[i] = i;
      }

      java.util.Arrays.sort(order, (a, b) -> Double.compare(Math.abs(Math.log(now.apply(b) / then.apply(b))), Math.abs(Math.log(now.apply(a) / then.apply(a)))));
      StringBuilder sb = new StringBuilder();

      for (int k = 0; k < 6; k++) {
         int i = order[k];
         sb.append(k > 0 ? ", " : "  ").append(Genome.GENES[i].name()).append(String.format(java.util.Locale.ROOT, " %.3f -> %.3f", then.apply(i), now.apply(i)));
      }

      return sb.toString();
   }

   private static int list(ServerCommandSource src, boolean full) {
      ServerWorld world = src.getWorld();
      Box box = new Box(BlockPos.ofFloored(src.getPosition())).expand(64.0);
      List<ConnectomeFlyEntity> flies = world.getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, box, e -> true);
      List<FlyEggEntity> eggs = world.getEntitiesByType(ConnectomeFlyMod.FLY_EGG, box, e -> true);
      src.sendFeedback(() -> Text.literal(MODEL + flies.size() + " flies, " + eggs.size() + " eggs within 64 blocks").formatted(Formatting.AQUA), false);

      for (ConnectomeFlyEntity f : flies) {
         Genome g = f.genome();
         String line = "fly "
            + Genome.shortId(f.getUuid())
            + (f.isMale() ? " male" : " female")
            + (f.isBabyFly() ? " (young)" : "")
            + " gen "
            + g.generation
            + " mother "
            + Genome.shortId(g.mother)
            + " father "
            + Genome.shortId(g.father)
            + " mutations "
            + g.mutations
            + (f.isMale() ? " [his small circuit does not use these genes; he only passes them on]" : "")
            + ": "
            + (full ? g.all() : g.top(5));
         src.sendFeedback(() -> Text.literal(line).formatted(Formatting.GRAY), false);
      }

      for (FlyEggEntity e : eggs) {
         Genome g = e.genome();
         String line = "egg "
            + Genome.shortId(e.getUuid())
            + (g == null ? " no genome (unfertilized or laid before genetics)" : " gen " + g.generation + " mother " + Genome.shortId(g.mother) + " father " + Genome.shortId(g.father) + " mutations " + g.mutations + ": " + (full ? g.all() : g.top(5)));
         src.sendFeedback(() -> Text.literal(line).formatted(Formatting.GRAY), false);
      }

      return flies.size() + eggs.size();
   }
}

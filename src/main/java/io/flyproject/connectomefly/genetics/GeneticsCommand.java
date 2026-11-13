package io.flyproject.connectomefly.genetics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import io.flyproject.connectomefly.ConnectomeFlyMod;
import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import io.flyproject.connectomefly.entity.FlyEggEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

/** /flygenes: read the [model] genomes of nearby flies and eggs, and switch inherited memory. */
public final class GeneticsCommand {
   private static final String MODEL = "[model] inherited tuning on real wiring, not real genetics. ";

   private GeneticsCommand() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> d) {
      d.register(
         Commands.literal("flygenes")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .executes(ctx -> list(ctx.getSource(), false))
            .then(Commands.literal("full").executes(ctx -> list(ctx.getSource(), true)))
            .then(Commands.literal("stats").executes(ctx -> stats(ctx.getSource())))
            .then(
               Commands.literal("body")
                  .then(Commands.literal("fly").executes(ctx -> rebody(ctx.getSource(), null, (byte)0)))
                  .then(Commands.literal("villager").executes(ctx -> rebody(ctx.getSource(), null, ConnectomeFlyEntity.BODY_VILLAGER)))
                  .then(
                     Commands.argument("type", net.minecraft.commands.arguments.IdentifierArgument.id())
                        .suggests(net.minecraft.commands.synchronization.SuggestionProviders.cast(net.minecraft.commands.synchronization.SuggestionProviders.SUMMONABLE_ENTITIES))
                        .executes(ctx -> rebody(ctx.getSource(), net.minecraft.commands.arguments.IdentifierArgument.getId(ctx, "type"), ConnectomeFlyEntity.BODY_MOB))
                  )
            )
            .then(
               Commands.literal("spawn")
                  .then(
                     Commands.literal("villager")
                        .executes(ctx -> spawn(ctx.getSource(), ConnectomeFlyEntity.BODY_VILLAGER, false))
                        .then(Commands.literal("male").executes(ctx -> spawn(ctx.getSource(), ConnectomeFlyEntity.BODY_VILLAGER, true)))
                        .then(Commands.literal("female").executes(ctx -> spawn(ctx.getSource(), ConnectomeFlyEntity.BODY_VILLAGER, false)))
                  )
                  .then(
                     Commands.literal("mob")
                        .then(
                           Commands.argument("type", net.minecraft.commands.arguments.IdentifierArgument.id())
                              .suggests(net.minecraft.commands.synchronization.SuggestionProviders.cast(net.minecraft.commands.synchronization.SuggestionProviders.SUMMONABLE_ENTITIES))
                              .executes(ctx -> spawnMob(ctx.getSource(), net.minecraft.commands.arguments.IdentifierArgument.getId(ctx, "type"), false))
                              .then(Commands.literal("male").executes(ctx -> spawnMob(ctx.getSource(), net.minecraft.commands.arguments.IdentifierArgument.getId(ctx, "type"), true)))
                              .then(Commands.literal("female").executes(ctx -> spawnMob(ctx.getSource(), net.minecraft.commands.arguments.IdentifierArgument.getId(ctx, "type"), false)))
                        )
                  )
                  .then(
                     Commands.literal("fly")
                        .executes(ctx -> spawn(ctx.getSource(), (byte)0, false))
                        .then(Commands.literal("male").executes(ctx -> spawn(ctx.getSource(), (byte)0, true)))
                        .then(Commands.literal("female").executes(ctx -> spawn(ctx.getSource(), (byte)0, false)))
                  )
            )
            .then(
               Commands.literal("set")
                  .executes(ctx -> set(ctx.getSource(), null, 0.0F))
                  .then(
                     Commands.argument("key", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .then(
                           Commands.argument("value", com.mojang.brigadier.arguments.FloatArgumentType.floatArg())
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
               Commands.literal("memory")
                  .executes(ctx -> memory(ctx.getSource(), null))
                  .then(Commands.argument("on", BoolArgumentType.bool()).executes(ctx -> memory(ctx.getSource(), BoolArgumentType.getBool(ctx, "on"))))
            )
      );
   }

   private static int memory(CommandSourceStack src, Boolean on) {
      GeneticsState st = GeneticsState.get(src.getServer());
      if (on != null) {
         st.set("inherit_memory", on ? 1.0F : 0.0F);
      }

      src.sendSuccess(
         () -> Component.literal(
               "[model] inherited memory is "
                  + (Genome.inheritMemory ? "ON" : "OFF")
                  + ": a child starts with "
                  + Math.round(Genome.inheritMemoryFraction * 100.0F)
                  + "% of what its mother had learned. Not biology. Saved with the world."
            )
            .withStyle(ChatFormatting.AQUA),
         false
      );
      return 1;
   }

   /** Adds one creature at the caller's position without touching the ones already there. */
   private static int spawn(CommandSourceStack src, byte body, boolean male) {
      ServerLevel world = src.getLevel();
      ConnectomeFlyEntity f = ConnectomeFlyMod.CONNECTOME_FLY.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      if (f == null) {
         return 0;
      }

      f.setMale(male);
      f.setBodyMode(body);
      net.minecraft.world.phys.Vec3 at = src.getPosition();
      f.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
      world.addFreshEntity(f);
      boolean villager = body == ConnectomeFlyEntity.BODY_VILLAGER;
      say(
         src,
         villager
            ? "[model] a fly-brained villager (" + (male ? "male" : "female") + "): a villager's body moved by the fly's whole brain. It is not a real villager: it cannot trade, farm, guard or follow orders, and @e[type=villager] does not select it."
            : "a " + (male ? "male" : "female") + " fly",
         ChatFormatting.AQUA
      );
      return 1;
   }

   /** Is this entity type a living mob whose body can be borrowed? Returns its name, or null with a message sent. */
   private static String mobName(CommandSourceStack src, net.minecraft.resources.Identifier id) {
      net.minecraft.world.entity.EntityType<?> t = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
      if (t == null) {
         say(src, "there is no entity type " + id, ChatFormatting.RED);
         return null;
      } else if (t == ConnectomeFlyMod.CONNECTOME_FLY) {
         say(src, "that is the fly itself: use /flygenes spawn fly", ChatFormatting.RED);
         return null;
      }

      net.minecraft.world.entity.Entity probe = null;

      try {
         probe = t.create(src.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      } catch (Exception var5) {
      }

      boolean living = probe instanceof net.minecraft.world.entity.LivingEntity && !(probe instanceof net.minecraft.world.entity.player.Player);
      if (probe != null) {
         probe.discard();
      }

      if (!living) {
         say(src, id + " is not a living mob (it may be an item, a projectile, a vehicle or a player), so the fly's brain cannot wear it", ChatFormatting.RED);
         return null;
      }

      return t.getDescription().getString();
   }

   private static int spawnMob(CommandSourceStack src, net.minecraft.resources.Identifier id, boolean male) {
      String name = mobName(src, id);
      if (name == null) {
         return 0;
      }

      ServerLevel world = src.getLevel();
      ConnectomeFlyEntity f = ConnectomeFlyMod.CONNECTOME_FLY.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
      if (f == null) {
         return 0;
      }

      f.setMale(male);
      f.setMobBody(id.toString());
      net.minecraft.world.phys.Vec3 at = src.getPosition();
      f.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
      world.addFreshEntity(f);
      say(
         src,
         "[model] a fly-brained " + name.toLowerCase(java.util.Locale.ROOT) + " (" + (male ? "male" : "female") + "): that mob's body moved by the fly's whole brain. It has none of the real mob's behaviour or abilities, and @e[type=" + id + "] does not select it.",
         ChatFormatting.AQUA
      );
      return 1;
   }

   /** Changes the body of the nearest fly-brained creature within 16 blocks. */
   private static int rebody(CommandSourceStack src, net.minecraft.resources.Identifier id, byte mode) {
      String name = mode == ConnectomeFlyEntity.BODY_MOB ? mobName(src, id) : (mode == ConnectomeFlyEntity.BODY_VILLAGER ? "villager" : "fly");
      if (name == null) {
         return 0;
      }

      net.minecraft.world.phys.Vec3 at = src.getPosition();
      ConnectomeFlyEntity best = null;

      for (ConnectomeFlyEntity f : src.getLevel().getEntities(ConnectomeFlyMod.CONNECTOME_FLY, new AABB(BlockPos.containing(at)).inflate(16.0), e -> e.isAlive())) {
         if (best == null || f.distanceToSqr(at) < best.distanceToSqr(at)) {
            best = f;
         }
      }

      if (best == null) {
         say(src, "no fly-brained creature within 16 blocks", ChatFormatting.RED);
         return 0;
      }

      if (mode == ConnectomeFlyEntity.BODY_MOB) {
         best.setMobBody(id.toString());
      } else {
         best.setBodyMode(mode);
      }

      say(src, "[model] the nearest one now wears the body of a " + name.toLowerCase(java.util.Locale.ROOT) + ". Same brain, same genes.", ChatFormatting.AQUA);
      return 1;
   }

   private static void say(CommandSourceStack src, String line, ChatFormatting f) {
      src.sendSuccess(() -> Component.literal(line).withStyle(f), false);
   }

   private static int set(CommandSourceStack src, String key, float value) {
      GeneticsState st = GeneticsState.get(src.getServer());
      String r = key == null ? st.settings() : st.set(key, value);
      say(src, r == null ? "unknown setting. " + st.settings() : "[model] settings (saved with the world; 1 = on, 0 = off): " + r, r == null ? ChatFormatting.RED : ChatFormatting.AQUA);
      return r == null ? 0 : 1;
   }

   private static int stats(CommandSourceStack src) {
      GeneticsState st = GeneticsState.get(src.getServer());
      say(src, MODEL + "Population statistics for this world.", ChatFormatting.AQUA);
      java.util.TreeMap<Integer, int[]> living = new java.util.TreeMap<>();
      float[] liveSum = new float[Genome.N];
      int alive = 0;

      for (ServerLevel w : src.getServer().getAllLevels()) {
         for (ConnectomeFlyEntity f : w.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, e -> e.isAlive())) {
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

      say(src, lv.toString(), ChatFormatting.GRAY);
      StringBuilder bn = new StringBuilder("born:");

      for (java.util.Map.Entry<Integer, Integer> e : st.born.entrySet()) {
         bn.append(" gen ").append(e.getKey()).append(": ").append(e.getValue()).append(';');
      }

      say(src, bn + " eggs laid " + st.eggsLaid + ", eggs refused by cap " + st.eggsRefusedCap + ", matings refused (hungry) " + st.matingsRefusedHungry + ", hatchlings whose sex was set by the balance rule " + st.sexBalanced, ChatFormatting.GRAY);
      StringBuilder dd = new StringBuilder("deaths:");

      for (String c : GeneticsState.CAUSES) {
         int n = st.deaths.getOrDefault(c, 0);
         dd.append(' ').append(c).append(' ').append(n);
         if (n > 0) {
            dd.append(String.format(java.util.Locale.ROOT, " (mean adult age %.1f min)", st.deathAdultMinutes.getOrDefault(c, 0.0) / n));
         }

         dd.append(';');
      }

      say(src, dd.toString(), ChatFormatting.GRAY);
      if (!st.born.isEmpty()) {
         int first = st.born.firstKey();
         int last = st.born.lastKey();
         if (last != first) {
            say(src, "gene means, generation " + last + " (" + st.born.get(last) + " born) against generation " + first + " (" + st.born.get(first) + " born), largest shifts:", ChatFormatting.GRAY);
            say(src, shifts(i -> st.mean(last, i), i -> st.mean(first, i)), ChatFormatting.GRAY);
         }

         if (alive > 0) {
            int n = alive;
            say(src, "gene means, flies alive now against generation " + first + ", largest shifts:", ChatFormatting.GRAY);
            say(src, shifts(i -> liveSum[i] / n, i -> st.mean(first, i)), ChatFormatting.GRAY);
         }
      }

      say(src, "With few flies these shifts are mostly chance (drift), not selection.", ChatFormatting.DARK_GRAY);
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

   private static int list(CommandSourceStack src, boolean full) {
      ServerLevel world = src.getLevel();
      AABB box = new AABB(BlockPos.containing(src.getPosition())).inflate(64.0);
      List<ConnectomeFlyEntity> flies = world.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, box, e -> true);
      List<FlyEggEntity> eggs = world.getEntities(ConnectomeFlyMod.FLY_EGG, box, e -> true);
      src.sendSuccess(() -> Component.literal(MODEL + flies.size() + " flies, " + eggs.size() + " eggs within 64 blocks").withStyle(ChatFormatting.AQUA), false);

      for (ConnectomeFlyEntity f : flies) {
         Genome g = f.genome();
         String line = "fly "
            + Genome.shortId(f.getUUID())
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
         src.sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.GRAY), false);
      }

      for (FlyEggEntity e : eggs) {
         Genome g = e.genome();
         String line = "egg "
            + Genome.shortId(e.getUUID())
            + (g == null ? " no genome (unfertilized or laid before genetics)" : " gen " + g.generation + " mother " + Genome.shortId(g.mother) + " father " + Genome.shortId(g.father) + " mutations " + g.mutations + ": " + (full ? g.all() : g.top(5)));
         src.sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.GRAY), false);
      }

      return flies.size() + eggs.size();
   }
}

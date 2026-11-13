package io.flyproject.connectomefly.lab;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.flyproject.connectomefly.ConnectomeFlyMod;
import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import io.flyproject.connectomefly.neural.WholeBrain;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FlyLabCommand {
   private static final SuggestionProvider<CommandSourceStack> ARENAS = (ctx, b) -> {
      List<String> ids = new ArrayList<>(ArenaBuilder.ids());
      ids.add("all");
      ids.add("list");
      return SharedSuggestionProvider.suggest(ids, b);
   };
   public static volatile boolean stacked = false;
   public static volatile int scanRadius = 160;

   private FlyLabCommand() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> d) {
      LiteralArgumentBuilder<CommandSourceStack> flylab = (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                      "flylab"
                                                   )
                                                   .executes(ctx -> help((CommandSourceStack)ctx.getSource())))
                                                .then(
                                                   ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spawn")
                                                            .executes(ctx -> spawn((CommandSourceStack)ctx.getSource(), false)))
                                                         .then(
                                                            Commands.literal("female")
                                                               .executes(ctx -> spawn((CommandSourceStack)ctx.getSource(), false))
                                                         ))
                                                      .then(Commands.literal("male").executes(ctx -> spawn((CommandSourceStack)ctx.getSource(), true)))
                                                ))
                                             .then(
                                                ((LiteralArgumentBuilder)Commands.literal("body")
                                                      .then(
                                                         Commands.literal("model").executes(ctx -> body((CommandSourceStack)ctx.getSource(), (byte)0))
                                                      ))
                                                   .then(Commands.literal("bee").executes(ctx -> body((CommandSourceStack)ctx.getSource(), (byte)1)))
                                             ))
                                          .then(Commands.literal("book").executes(ctx -> book((CommandSourceStack)ctx.getSource()))))
                                       .then(Commands.literal("hungry").executes(ctx -> need((CommandSourceStack)ctx.getSource(), true))))
                                    .then(Commands.literal("thirsty").executes(ctx -> need((CommandSourceStack)ctx.getSource(), false))))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("stacked")
                                             .executes(ctx -> stacked((CommandSourceStack)ctx.getSource(), stacked, 0)))
                                          .then(Commands.literal("on").executes(ctx -> stacked((CommandSourceStack)ctx.getSource(), true, 0))))
                                       .then(
                                          ((LiteralArgumentBuilder)Commands.literal("off")
                                                .executes(ctx -> stacked((CommandSourceStack)ctx.getSource(), false, 0)))
                                             .then(
                                                Commands.argument("blocks", IntegerArgumentType.integer(8, 512))
                                                   .executes(
                                                      ctx -> stacked((CommandSourceStack)ctx.getSource(), false, IntegerArgumentType.getInteger(ctx, "blocks"))
                                                   )
                                             )
                                       )
                                 ))
                              .then(
                                 ((LiteralArgumentBuilder)Commands.literal("brains").executes(ctx -> brains((CommandSourceStack)ctx.getSource(), 0)))
                                    .then(
                                       Commands.argument("n", IntegerArgumentType.integer(1, 64))
                                          .executes(ctx -> brains((CommandSourceStack)ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))
                                    )
                              ))
                           .then(Commands.literal("state").executes(ctx -> status((CommandSourceStack)ctx.getSource()))))
                        .then(Commands.literal("fly").executes(ctx -> fly((CommandSourceStack)ctx.getSource()))))
                     .then(Commands.literal("lay").executes(ctx -> lay((CommandSourceStack)ctx.getSource()))))
                  .then(Commands.literal("mate").executes(ctx -> mate((CommandSourceStack)ctx.getSource()))))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("loom").executes(ctx -> loom((CommandSourceStack)ctx.getSource(), Double.NaN)))
                     .then(
                        Commands.argument("degrees", DoubleArgumentType.doubleArg(-180.0, 360.0))
                           .executes(ctx -> loom((CommandSourceStack)ctx.getSource(), DoubleArgumentType.getDouble(ctx, "degrees")))
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("drum")
                           .then(Commands.literal("left").executes(ctx -> drum((CommandSourceStack)ctx.getSource(), -40.0))))
                        .then(Commands.literal("right").executes(ctx -> drum((CommandSourceStack)ctx.getSource(), 40.0))))
                     .then(Commands.literal("fast").executes(ctx -> drum((CommandSourceStack)ctx.getSource(), 90.0))))
                  .then(Commands.literal("stop").executes(ctx -> drum((CommandSourceStack)ctx.getSource(), 0.0)))
            ))
         .then(
            Commands.literal("arena")
               .then(
                  Commands.argument("sense", StringArgumentType.word())
                     .suggests(ARENAS)
                     .executes(ctx -> arena((CommandSourceStack)ctx.getSource(), StringArgumentType.getString(ctx, "sense")))
               )
         );
      if (Boolean.getBoolean("connectomefly.dev")) {
         flylab = (LiteralArgumentBuilder<CommandSourceStack>)flylab.then(
            ((LiteralArgumentBuilder)Commands.literal("debug").requires(src -> Commands.hasPermission(Commands.LEVEL_OWNERS).test(src) && !src.isPlayer()))
               .executes(ctx -> state((CommandSourceStack)ctx.getSource()))
         );
      }

      d.register(flylab);
   }

   private static int clearFlies(ServerLevel world, Vec3 at, Boolean maleOnly) {
      int n = 0;
      double r2 = (double)scanRadius * scanRadius;

      for (ConnectomeFlyEntity f : world.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, e -> e.isAlive())) {
         if (!(f.distanceToSqr(at) > r2) && (maleOnly == null || f.isMale() == maleOnly && !f.isBabyFly())) {
            f.dropLeash();
            f.discard();
            n++;
         }
      }

      return n;
   }

   private static ConnectomeFlyEntity place(ServerLevel world, double x, double y, double z, boolean male) {
      ConnectomeFlyEntity fly = (ConnectomeFlyEntity)ConnectomeFlyMod.CONNECTOME_FLY.create(world, EntitySpawnReason.COMMAND);
      if (fly != null) {
         fly.setMale(male);
         fly.setBodyMode(ConnectomeFlyEntity.defaultBody);
         fly.snapTo(x, y, z, 0.0F, 0.0F);
         fly.finalizeSpawn(world, world.getCurrentDifficultyAt(fly.blockPosition()), EntitySpawnReason.COMMAND, null);
         world.addFreshEntity(fly);
      }

      return fly;
   }

   public static int populate(ServerLevel world, String id, BlockPos spawn) {
      int gone = stacked ? 0 : clearFlies(world, Vec3.atBottomCenterOf(spawn), null);
      double x = spawn.getX() + 0.5;
      double y = spawn.getY();
      double z = spawn.getZ() + 0.5;
      ConnectomeFlyEntity fly = place(world, x, y, z, false);
      if (fly == null) {
         return gone;
      } else {
         if (id.equals("lure")) {
            fly.setHunger(9.0F);
         } else if (id.equals("memory")
            || id.equals("taste")
            || id.equals("hunger")
            || id.equals("bitter")
            || id.equals("pain")
            || id.equals("mould")
            || id.equals("co2")
            || id.equals("smell")) {
            fly.setHunger(6.0F);
         } else if (id.equals("damp")) {
            fly.setThirst(17.0F);
         }

         if (id.equals("courtship")) {
            place(world, x + 4.0, y, z + 4.0, true);
         } else if (id.equals("social")) {
            fly.setHunger(10.0F);

            for (int k = 0; k < 2; k++) {
               ConnectomeFlyEntity other = place(world, x + (k == 0 ? 6 : 3), y, z + (k == 0 ? 2 : 6), false);
               if (other != null) {
                  other.setHunger(10.0F);
               }
            }
         } else if (id.equals("aggression")) {
            fly.setHunger(7.0F);
            ConnectomeFlyEntity rival = place(world, x + 2.0, y, z, false);
            if (rival != null) {
               rival.setHunger(7.0F);
            }
         }

         return gone;
      }
   }

   private static int need(CommandSourceStack src, boolean hunger) {
      int n = 0;

      for (ConnectomeFlyEntity f : src.getLevel()
         .getEntities(ConnectomeFlyMod.CONNECTOME_FLY, new AABB(BlockPos.containing(src.getPosition())).inflate(24.0), e -> e.isAlive() && !e.isMale())) {
         if (hunger) {
            f.setHunger(5.0F);
         } else {
            f.setThirst(17.0F);
         }

         n++;
      }

      int count = n;
      src.sendSuccess(
         () -> Component.literal(
               count == 0
                  ? "no female within 24 blocks"
                  : (
                     hunger
                        ? "She is hungry now. Hunger turns up her food smell and taste, and her insulin cells (IPCs) go quiet"
                        : "She is thirsty now. Her humidity sensors turn attractive, so she walks to water or damp ground and drinks"
                  )
            )
            .withStyle(ChatFormatting.AQUA),
         false
      );
      return 1;
   }

   private static int stacked(CommandSourceStack src, boolean on, int radius) {
      stacked = on;
      if (radius > 0) {
         scanRadius = radius;
      }

      src.sendSuccess(
         () -> Component.literal(
               on
                  ? "stacked ON. Nothing is removed, so every spawn and every box button adds flies. Brains run for the first "
                     + ConnectomeFlyEntity.maxBrains
                     + " females (/flylab brains <n>); the rest rest until one frees up."
                  : "stacked OFF. One experiment at a time. A spawn or a box button first removes the flies within "
                     + scanRadius
                     + " blocks, then spawns fresh ones (no teleporting)."
            )
            .withStyle(ChatFormatting.AQUA),
         false
      );
      return 1;
   }

   private static int brains(CommandSourceStack src, int n) {
      if (n > 0) {
         ConnectomeFlyEntity.maxBrains = n;
      }

      src.sendSuccess(
         () -> Component.literal(
               "whole brains: "
                  + WholeBrain.running()
                  + " running, up to "
                  + ConnectomeFlyEntity.maxBrains
                  + " at once (this machine has "
                  + Runtime.getRuntime().availableProcessors()
                  + " cores and each brain keeps about one busy; leave a few for the game)"
            )
            .withStyle(ChatFormatting.AQUA),
         false
      );
      return 1;
   }

   private static int help(CommandSourceStack src) {
      src.sendSuccess(
         () -> Component.literal(
               "/flylab spawn [female|male] | body <model|bee> | arena <sense|all|list> | stacked <on|off [blocks]> | brains [n] | hungry | thirsty | fly | lay | mate | loom [degrees] | drum <left|right|fast|stop> | state | book. and /flycredits for what is real vs calibrated"
            )
            .withStyle(ChatFormatting.AQUA),
         false
      );
      return 1;
   }

   private static int spawn(CommandSourceStack src, boolean male) {
      ServerLevel world = src.getLevel();
      ServerPlayer p = src.getPlayer();
      Vec3 at = src.getPosition();
      int gone = stacked ? 0 : clearFlies(world, at, male);
      ConnectomeFlyEntity fly = (ConnectomeFlyEntity)ConnectomeFlyMod.CONNECTOME_FLY.create(world, EntitySpawnReason.COMMAND);
      if (fly == null) {
         return 0;
      } else {
         fly.setMale(male);
         fly.setBodyMode(ConnectomeFlyEntity.defaultBody);
         fly.snapTo(at.x + (p != null ? 2 : 0), at.y, at.z + (p != null ? 2 : 0), p != null ? p.getYRot() : 0.0F, 0.0F);
         fly.finalizeSpawn(world, world.getCurrentDifficultyAt(fly.blockPosition()), EntitySpawnReason.COMMAND, null);
         world.addFreshEntity(fly);
         src.sendSuccess(
            () -> Component.literal(
                  male
                     ? "a MALE fly spawned: he courts the nearest female with his wing song."
                     : "fly spawned: her whole brain (158,262 neurons) is waking, the escape circuit is ticking. Sprint at her, or build a box: /flylab arena <sense>"
                        + (gone > 0 ? "  (" + gone + " earlier " + (gone == 1 ? "one" : "ones") + " removed: /flylab stacked on keeps them)" : "")
               )
               .withStyle(ChatFormatting.AQUA),
            false
         );
         return 1;
      }
   }

   private static int body(CommandSourceStack src, byte mode) {
      ConnectomeFlyEntity.defaultBody = mode;
      int n = 0;

      for (ServerLevel w : src.getServer().getAllLevels()) {
         for (ConnectomeFlyEntity f : w.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, e -> true)) {
            f.setBodyMode(mode);
            n++;
         }
      }

      int count = n;
      src.sendSuccess(
         () -> Component.literal(
               (mode == 1 ? "body: plain bee" : "body: the real fly model") + " (" + count + (count == 1 ? " fly" : " flies") + " updated; new ones follow)"
            )
            .withStyle(ChatFormatting.AQUA),
         false
      );
      return 1;
   }

   private static int arena(CommandSourceStack src, String id) {
      if (id.equals("list")) {
         src.sendSuccess(() -> Component.literal("senses: " + String.join(", ", ArenaBuilder.ids()) + ", all").withStyle(ChatFormatting.AQUA), false);
         return 1;
      } else {
         ServerLevel world = src.getLevel();
         BlockPos here = BlockPos.containing(src.getPosition());
         if (id.equals("all")) {
            BlockPos spawn = ArenaBuilder.buildAll(world, here.offset(0, 0, 10));
            src.sendSuccess(
               () -> Component.literal(
                     "all "
                        + ArenaBuilder.SENSES.size()
                        + " sense boxes built north of you, six per row, all empty. Press the wooden button on the LIME post by a box's door: its fly appears inside (and, with /flylab stacked off, the fly of the last box is removed)."
                  )
                  .withStyle(ChatFormatting.AQUA),
               false
            );
            return 1;
         } else if (!ArenaBuilder.SENSES.containsKey(id)) {
            src.sendFailure(Component.literal("unknown sense '" + id + "': /flylab arena list"));
            return 0;
         } else {
            BlockPos spawn = ArenaBuilder.build(world, here.offset(0, 0, 9), id);
            int gone = populate(world, id, spawn);
            String title = ArenaBuilder.SENSES.get(id)[0];
            String how = ArenaBuilder.instructions(id);
            src.sendSuccess(
               () -> Component.literal(
                     "box "
                        + title
                        + " built ahead of you: she is inside"
                        + (gone > 0 ? " (" + gone + " earlier " + (gone == 1 ? "fly" : "flies") + " removed; /flylab stacked on keeps them)." : ".")
                        + (id.equals("home") ? " Every sense is live in this one." : " Inside it only this experiment's senses are live.")
                  )
                  .withStyle(ChatFormatting.AQUA),
               false
            );
            src.sendSuccess(() -> Component.literal(how).withStyle(ChatFormatting.GRAY), false);
            return 1;
         }
      }
   }

   private static int fly(CommandSourceStack src) {
      ServerLevel world = src.getLevel();
      BlockPos here = BlockPos.containing(src.getPosition());
      int n = 0;

      for (ConnectomeFlyEntity f : world.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, new AABB(here).inflate(48.0), e -> !e.isMale())) {
         f.takeOffNow();
         n++;
      }

      int count = n;
      src.sendSuccess(
         () -> Component.literal(count == 0 ? "no fly within 48 blocks" : "take-off: crouch, wings up, push. Then she hovers and roams from her brain")
            .withStyle(ChatFormatting.AQUA),
         false
      );
      return 1;
   }

   private static int mate(CommandSourceStack src) {
      ServerLevel world = src.getLevel();
      BlockPos here = BlockPos.containing(src.getPosition());
      boolean female = false;
      boolean male = false;

      for (ConnectomeFlyEntity f : world.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, new AABB(here).inflate(48.0), e -> !e.isBabyFly())) {
         f.resetCourtship();
         if (f.isMale()) {
            male = true;
         } else {
            female = true;
         }
      }

      if (!female) {
         spawn(src, false);
      }

      if (!male) {
         spawn(src, true);
      }

      src.sendSuccess(
         () -> Component.literal("courtship reset: he sings, after 10 s she accepts, 10 s of mating, then the egg (hatches in 30 s)").withStyle(ChatFormatting.AQUA),
         false
      );
      return 1;
   }

   private static int loom(CommandSourceStack src, double relDeg) {
      ServerLevel world = src.getLevel();
      BlockPos here = BlockPos.containing(src.getPosition());
      ConnectomeFlyEntity best = null;
      double bd = 1.0E9;

      for (ConnectomeFlyEntity f : world.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, new AABB(here).inflate(48.0), e -> e.isAlive())) {
         double d = f.distanceToSqr(here.getX() + 0.5, here.getY(), here.getZ() + 0.5);
         if (d < bd) {
            bd = d;
            best = f;
         }
      }

      if (best == null) {
         src.sendFailure(Component.literal("no fly within 48 blocks"));
         return 0;
      } else {
         LoomStimulus.spawn(world, best, relDeg);
         src.sendSuccess(
            () -> Component.literal(
                  "a shape looms in"
                     + (Double.isNaN(relDeg) ? "" : String.format(Locale.ROOT, " at %.0f degrees off her heading", relDeg))
                     + ": LC4/LPLC2 -> Giant Fiber -> TTMn if she can see it (~30 degrees behind her, she cannot)"
               )
               .withStyle(ChatFormatting.AQUA),
            false
         );
         return 1;
      }
   }

   private static int drum(CommandSourceStack src, double omega) {
      if (!OptomotorDrum.active()) {
         src.sendFailure(Component.literal("no drum here. Build the optomotor box first with /flylab arena optomotor"));
         return 0;
      } else {
         OptomotorDrum.spin(omega);
         src.sendSuccess(
            () -> Component.literal(
                  omega == 0.0
                     ? "the drum stops"
                     : "the drum turns " + (omega > 0.0 ? "clockwise" : "anticlockwise") + " at " + Math.abs(omega) + " deg/s: watch her HS cells and her turn"
               )
               .withStyle(ChatFormatting.AQUA),
            false
         );
         return 1;
      }
   }

   private static int lay(CommandSourceStack src) {
      ServerLevel world = src.getLevel();
      BlockPos here = BlockPos.containing(src.getPosition());
      int n = 0;

      for (ConnectomeFlyEntity f : world.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, new AABB(here).inflate(48.0), e -> !e.isMale() && !e.isBabyFly())) {
         f.layEgg("lays an egg (forced): it hatches in 30 s");
         n++;
      }

      int count = n;
      src.sendSuccess(
         () -> Component.literal(count == 0 ? "no adult female within 48 blocks" : "egg laid: it cracks in 30 s into a hatchling with its own brain")
            .withStyle(ChatFormatting.AQUA),
         false
      );
      return 1;
   }

   private static int status(CommandSourceStack src) {
      ServerLevel world = src.getLevel();
      BlockPos here = BlockPos.containing(src.getPosition());
      int n = 0;

      for (ConnectomeFlyEntity f : world.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, new AABB(here).inflate(64.0), e -> true)) {
         boolean good = f.isAlive() && f.getHealth() > 0.0F;
         String line = (f.isMale() ? "male fly: " : "female fly: ") + (good ? "good" : "bad (she is dead)");
         src.sendSuccess(() -> Component.literal(line).withStyle(good ? ChatFormatting.GREEN : ChatFormatting.RED), false);
         n++;
      }

      if (n == 0) {
         src.sendSuccess(() -> Component.literal("bad: there is no fly within 64 blocks").withStyle(ChatFormatting.RED), false);
      }

      return 1;
   }

   private static int state(CommandSourceStack src) {
      ServerLevel world = src.getLevel();
      BlockPos here = BlockPos.containing(src.getPosition());
      int n = 0;

      for (ConnectomeFlyEntity f : world.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, new AABB(here).inflate(64.0), e -> true)) {
         String line = (f.isMale() ? "male " : "female ") + f.debugState();
         src.sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.GRAY), false);
         n++;
      }

      if (n == 0) {
         src.sendSuccess(() -> Component.literal("no fly within 64 blocks").withStyle(ChatFormatting.GRAY), false);
      }

      return 1;
   }

   private static int book(CommandSourceStack src) {
      ServerPlayer p = src.getPlayer();
      if (p == null) {
         return 0;
      } else {
         String[] pageTexts = new String[]{
            "THE CONNECTOME FLY\n\nOne creature. Her whole brain, all 158,262 neurons of a real fruit fly's brain map (BANC v888), runs live inside her.\n\nNothing is scripted. If she moves, neurons fired.",
            "SENSES\n\nWhat she meets in the world drives her REAL receptor neurons: heat, cold, wet, dry, sugar, bitter, water, food smell, CO2, touch, sound, pheromone, light, wind.",
            "ACTIONS\n\nHer own descending and motor neurons move her: walking, turning, the jump, take off, grooming, the proboscis, egg laying.\n\nThe HUD shows them firing.",
            "THE BOXES\n\n/flylab arena all builds 33 glass boxes, one per experiment.\n\nEach has a book on a lectern that explains it, and a wooden button that puts the fly inside.",
            "TRY THIS\n\n1. /flylab arena escape\n2. Press the wooden button.\n3. Sprint straight at her.\n\nHer looming detectors fire, the Giant Fiber fires, TTMn fires, she jumps.",
            "BODIES\n\n/flylab body model\nthe real fly model, with wings that spread and beat in flight.\n\n/flylab body bee\na plain bee.",
            "COURTSHIP\n\n/flylab spawn male gives a courting male. He follows her and sings with one wing. After about 10 seconds of song she accepts, and later lays an egg.",
            "A PET\n\nFeed her from your hand, give her water from a bottle, walk her on a lead.\n\nIndoors she mostly walks. Under open sky she also flies on her own.",
            "CREDITS\n\n/flycredits lists what is real (the wiring), what is calibrated (speeds and gains) and what is a labeled model (hunger, wind, speech).\n\nNo full consciousness is claimed."
         };

         ItemStack stack = io.flyproject.connectomefly.compat.Compat.book("The Connectome Fly", "the fly project", pageTexts);
         p.addItem(stack);
         src.sendSuccess(() -> Component.literal("field guide added to your inventory").withStyle(ChatFormatting.AQUA), false);
         return 1;
      }
   }
}

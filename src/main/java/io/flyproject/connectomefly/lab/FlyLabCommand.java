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
import net.minecraft.command.CommandSource;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serializer;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class FlyLabCommand {
   private static final SuggestionProvider<ServerCommandSource> ARENAS = (ctx, b) -> {
      List<String> ids = new ArrayList<>(ArenaBuilder.ids());
      ids.add("all");
      ids.add("list");
      return CommandSource.suggestMatching(ids, b);
   };
   public static volatile boolean stacked = false;
   public static volatile int scanRadius = 160;

   private FlyLabCommand() {
   }

   public static void register(CommandDispatcher<ServerCommandSource> d) {
      LiteralArgumentBuilder<ServerCommandSource> flylab = (LiteralArgumentBuilder<ServerCommandSource>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                                                      "flylab"
                                                   )
                                                   .executes(ctx -> help((ServerCommandSource)ctx.getSource())))
                                                .then(
                                                   ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spawn")
                                                            .executes(ctx -> spawn((ServerCommandSource)ctx.getSource(), false)))
                                                         .then(
                                                            CommandManager.literal("female")
                                                               .executes(ctx -> spawn((ServerCommandSource)ctx.getSource(), false))
                                                         ))
                                                      .then(CommandManager.literal("male").executes(ctx -> spawn((ServerCommandSource)ctx.getSource(), true)))
                                                ))
                                             .then(
                                                ((LiteralArgumentBuilder)CommandManager.literal("body")
                                                      .then(
                                                         CommandManager.literal("model").executes(ctx -> body((ServerCommandSource)ctx.getSource(), (byte)0))
                                                      ))
                                                   .then(CommandManager.literal("bee").executes(ctx -> body((ServerCommandSource)ctx.getSource(), (byte)1)))
                                             ))
                                          .then(CommandManager.literal("book").executes(ctx -> book((ServerCommandSource)ctx.getSource()))))
                                       .then(CommandManager.literal("hungry").executes(ctx -> need((ServerCommandSource)ctx.getSource(), true))))
                                    .then(CommandManager.literal("thirsty").executes(ctx -> need((ServerCommandSource)ctx.getSource(), false))))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("stacked")
                                             .executes(ctx -> stacked((ServerCommandSource)ctx.getSource(), stacked, 0)))
                                          .then(CommandManager.literal("on").executes(ctx -> stacked((ServerCommandSource)ctx.getSource(), true, 0))))
                                       .then(
                                          ((LiteralArgumentBuilder)CommandManager.literal("off")
                                                .executes(ctx -> stacked((ServerCommandSource)ctx.getSource(), false, 0)))
                                             .then(
                                                CommandManager.argument("blocks", IntegerArgumentType.integer(8, 512))
                                                   .executes(
                                                      ctx -> stacked((ServerCommandSource)ctx.getSource(), false, IntegerArgumentType.getInteger(ctx, "blocks"))
                                                   )
                                             )
                                       )
                                 ))
                              .then(
                                 ((LiteralArgumentBuilder)CommandManager.literal("brains").executes(ctx -> brains((ServerCommandSource)ctx.getSource(), 0)))
                                    .then(
                                       CommandManager.argument("n", IntegerArgumentType.integer(1, 64))
                                          .executes(ctx -> brains((ServerCommandSource)ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))
                                    )
                              ))
                           .then(CommandManager.literal("state").executes(ctx -> status((ServerCommandSource)ctx.getSource()))))
                        .then(CommandManager.literal("fly").executes(ctx -> fly((ServerCommandSource)ctx.getSource()))))
                     .then(CommandManager.literal("lay").executes(ctx -> lay((ServerCommandSource)ctx.getSource()))))
                  .then(CommandManager.literal("mate").executes(ctx -> mate((ServerCommandSource)ctx.getSource()))))
               .then(
                  ((LiteralArgumentBuilder)CommandManager.literal("loom").executes(ctx -> loom((ServerCommandSource)ctx.getSource(), Double.NaN)))
                     .then(
                        CommandManager.argument("degrees", DoubleArgumentType.doubleArg(-180.0, 360.0))
                           .executes(ctx -> loom((ServerCommandSource)ctx.getSource(), DoubleArgumentType.getDouble(ctx, "degrees")))
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("drum")
                           .then(CommandManager.literal("left").executes(ctx -> drum((ServerCommandSource)ctx.getSource(), -40.0))))
                        .then(CommandManager.literal("right").executes(ctx -> drum((ServerCommandSource)ctx.getSource(), 40.0))))
                     .then(CommandManager.literal("fast").executes(ctx -> drum((ServerCommandSource)ctx.getSource(), 90.0))))
                  .then(CommandManager.literal("stop").executes(ctx -> drum((ServerCommandSource)ctx.getSource(), 0.0)))
            ))
         .then(
            CommandManager.literal("arena")
               .then(
                  CommandManager.argument("sense", StringArgumentType.word())
                     .suggests(ARENAS)
                     .executes(ctx -> arena((ServerCommandSource)ctx.getSource(), StringArgumentType.getString(ctx, "sense")))
               )
         );
      if (Boolean.getBoolean("connectomefly.dev")) {
         flylab = (LiteralArgumentBuilder<ServerCommandSource>)flylab.then(
            ((LiteralArgumentBuilder)CommandManager.literal("debug").requires(src -> src.hasPermissionLevel(4) && !src.isExecutedByPlayer()))
               .executes(ctx -> state((ServerCommandSource)ctx.getSource()))
         );
      }

      d.register(flylab);
   }

   private static int clearFlies(ServerWorld world, Vec3d at, Boolean maleOnly) {
      int n = 0;
      double r2 = (double)scanRadius * scanRadius;

      for (ConnectomeFlyEntity f : world.getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, e -> e.isAlive())) {
         if (!(f.squaredDistanceTo(at) > r2) && (maleOnly == null || f.isMale() == maleOnly && !f.isBabyFly())) {
            f.detachLeash(true, false);
            f.discard();
            n++;
         }
      }

      return n;
   }

   private static ConnectomeFlyEntity place(ServerWorld world, double x, double y, double z, boolean male) {
      ConnectomeFlyEntity fly = (ConnectomeFlyEntity)ConnectomeFlyMod.CONNECTOME_FLY.create(world);
      if (fly != null) {
         fly.setMale(male);
         fly.setBodyMode(ConnectomeFlyEntity.defaultBody);
         fly.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);
         fly.initialize(world, world.getLocalDifficulty(fly.getBlockPos()), SpawnReason.COMMAND, null, null);
         world.spawnEntity(fly);
      }

      return fly;
   }

   public static int populate(ServerWorld world, String id, BlockPos spawn) {
      int gone = stacked ? 0 : clearFlies(world, Vec3d.ofBottomCenter(spawn), null);
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

   private static int need(ServerCommandSource src, boolean hunger) {
      int n = 0;

      for (ConnectomeFlyEntity f : src.getWorld()
         .getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, new Box(BlockPos.ofFloored(src.getPosition())).expand(24.0), e -> e.isAlive() && !e.isMale())) {
         if (hunger) {
            f.setHunger(5.0F);
         } else {
            f.setThirst(17.0F);
         }

         n++;
      }

      int count = n;
      src.sendFeedback(
         () -> Text.literal(
               count == 0
                  ? "no female within 24 blocks"
                  : (
                     hunger
                        ? "She is hungry now. Hunger turns up her food smell and taste, and her insulin cells (IPCs) go quiet"
                        : "She is thirsty now. Her humidity sensors turn attractive, so she walks to water or damp ground and drinks"
                  )
            )
            .formatted(Formatting.AQUA),
         false
      );
      return 1;
   }

   private static int stacked(ServerCommandSource src, boolean on, int radius) {
      stacked = on;
      if (radius > 0) {
         scanRadius = radius;
      }

      src.sendFeedback(
         () -> Text.literal(
               on
                  ? "stacked ON. Nothing is removed, so every spawn and every box button adds flies. Brains run for the first "
                     + ConnectomeFlyEntity.maxBrains
                     + " females (/flylab brains <n>); the rest rest until one frees up."
                  : "stacked OFF. One experiment at a time. A spawn or a box button first removes the flies within "
                     + scanRadius
                     + " blocks, then spawns fresh ones (no teleporting)."
            )
            .formatted(Formatting.AQUA),
         false
      );
      return 1;
   }

   private static int brains(ServerCommandSource src, int n) {
      if (n > 0) {
         ConnectomeFlyEntity.maxBrains = n;
      }

      src.sendFeedback(
         () -> Text.literal(
               "whole brains: "
                  + WholeBrain.running()
                  + " running, up to "
                  + ConnectomeFlyEntity.maxBrains
                  + " at once (this machine has "
                  + Runtime.getRuntime().availableProcessors()
                  + " cores and each brain keeps about one busy; leave a few for the game)"
            )
            .formatted(Formatting.AQUA),
         false
      );
      return 1;
   }

   private static int help(ServerCommandSource src) {
      src.sendFeedback(
         () -> Text.literal(
               "/flylab spawn [female|male] | body <model|bee> | arena <sense|all|list> | stacked <on|off [blocks]> | brains [n] | hungry | thirsty | fly | lay | mate | loom [degrees] | drum <left|right|fast|stop> | state | book. and /flycredits for what is real vs calibrated"
            )
            .formatted(Formatting.AQUA),
         false
      );
      return 1;
   }

   private static int spawn(ServerCommandSource src, boolean male) {
      ServerWorld world = src.getWorld();
      ServerPlayerEntity p = src.getPlayer();
      Vec3d at = src.getPosition();
      int gone = stacked ? 0 : clearFlies(world, at, male);
      ConnectomeFlyEntity fly = (ConnectomeFlyEntity)ConnectomeFlyMod.CONNECTOME_FLY.create(world);
      if (fly == null) {
         return 0;
      } else {
         fly.setMale(male);
         fly.setBodyMode(ConnectomeFlyEntity.defaultBody);
         fly.refreshPositionAndAngles(at.x + (p != null ? 2 : 0), at.y, at.z + (p != null ? 2 : 0), p != null ? p.getYaw() : 0.0F, 0.0F);
         fly.initialize(world, world.getLocalDifficulty(fly.getBlockPos()), SpawnReason.COMMAND, null, null);
         world.spawnEntity(fly);
         src.sendFeedback(
            () -> Text.literal(
                  male
                     ? "a MALE fly spawned: he courts the nearest female with his wing song."
                     : "fly spawned: her whole brain (158,262 neurons) is waking, the escape circuit is ticking. Sprint at her, or build a box: /flylab arena <sense>"
                        + (gone > 0 ? "  (" + gone + " earlier " + (gone == 1 ? "one" : "ones") + " removed: /flylab stacked on keeps them)" : "")
               )
               .formatted(Formatting.AQUA),
            false
         );
         return 1;
      }
   }

   private static int body(ServerCommandSource src, byte mode) {
      ConnectomeFlyEntity.defaultBody = mode;
      int n = 0;

      for (ServerWorld w : src.getServer().getWorlds()) {
         for (ConnectomeFlyEntity f : w.getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, e -> true)) {
            f.setBodyMode(mode);
            n++;
         }
      }

      int count = n;
      src.sendFeedback(
         () -> Text.literal(
               (mode == 1 ? "body: plain bee" : "body: the real fly model") + " (" + count + (count == 1 ? " fly" : " flies") + " updated; new ones follow)"
            )
            .formatted(Formatting.AQUA),
         false
      );
      return 1;
   }

   private static int arena(ServerCommandSource src, String id) {
      if (id.equals("list")) {
         src.sendFeedback(() -> Text.literal("senses: " + String.join(", ", ArenaBuilder.ids()) + ", all").formatted(Formatting.AQUA), false);
         return 1;
      } else {
         ServerWorld world = src.getWorld();
         BlockPos here = BlockPos.ofFloored(src.getPosition());
         if (id.equals("all")) {
            BlockPos spawn = ArenaBuilder.buildAll(world, here.add(0, 0, 10));
            src.sendFeedback(
               () -> Text.literal(
                     "all "
                        + ArenaBuilder.SENSES.size()
                        + " sense boxes built north of you, six per row, all empty. Press the wooden button on the LIME post by a box's door: its fly appears inside (and, with /flylab stacked off, the fly of the last box is removed)."
                  )
                  .formatted(Formatting.AQUA),
               false
            );
            return 1;
         } else if (!ArenaBuilder.SENSES.containsKey(id)) {
            src.sendError(Text.literal("unknown sense '" + id + "': /flylab arena list"));
            return 0;
         } else {
            BlockPos spawn = ArenaBuilder.build(world, here.add(0, 0, 9), id);
            int gone = populate(world, id, spawn);
            String title = ArenaBuilder.SENSES.get(id)[0];
            String how = ArenaBuilder.instructions(id);
            src.sendFeedback(
               () -> Text.literal(
                     "box "
                        + title
                        + " built ahead of you: she is inside"
                        + (gone > 0 ? " (" + gone + " earlier " + (gone == 1 ? "fly" : "flies") + " removed; /flylab stacked on keeps them)." : ".")
                        + (id.equals("home") ? " Every sense is live in this one." : " Inside it only this experiment's senses are live.")
                  )
                  .formatted(Formatting.AQUA),
               false
            );
            src.sendFeedback(() -> Text.literal(how).formatted(Formatting.GRAY), false);
            return 1;
         }
      }
   }

   private static int fly(ServerCommandSource src) {
      ServerWorld world = src.getWorld();
      BlockPos here = BlockPos.ofFloored(src.getPosition());
      int n = 0;

      for (ConnectomeFlyEntity f : world.getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, new Box(here).expand(48.0), e -> !e.isMale())) {
         f.takeOffNow();
         n++;
      }

      int count = n;
      src.sendFeedback(
         () -> Text.literal(count == 0 ? "no fly within 48 blocks" : "take-off: crouch, wings up, push. Then she hovers and roams from her brain")
            .formatted(Formatting.AQUA),
         false
      );
      return 1;
   }

   private static int mate(ServerCommandSource src) {
      ServerWorld world = src.getWorld();
      BlockPos here = BlockPos.ofFloored(src.getPosition());
      boolean female = false;
      boolean male = false;

      for (ConnectomeFlyEntity f : world.getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, new Box(here).expand(48.0), e -> !e.isBabyFly())) {
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

      src.sendFeedback(
         () -> Text.literal("courtship reset: he sings, after 10 s she accepts, 10 s of mating, then the egg (hatches in 30 s)").formatted(Formatting.AQUA),
         false
      );
      return 1;
   }

   private static int loom(ServerCommandSource src, double relDeg) {
      ServerWorld world = src.getWorld();
      BlockPos here = BlockPos.ofFloored(src.getPosition());
      ConnectomeFlyEntity best = null;
      double bd = 1.0E9;

      for (ConnectomeFlyEntity f : world.getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, new Box(here).expand(48.0), e -> e.isAlive())) {
         double d = f.squaredDistanceTo(here.getX() + 0.5, here.getY(), here.getZ() + 0.5);
         if (d < bd) {
            bd = d;
            best = f;
         }
      }

      if (best == null) {
         src.sendError(Text.literal("no fly within 48 blocks"));
         return 0;
      } else {
         LoomStimulus.spawn(world, best, relDeg);
         src.sendFeedback(
            () -> Text.literal(
                  "a shape looms in"
                     + (Double.isNaN(relDeg) ? "" : String.format(Locale.ROOT, " at %.0f degrees off her heading", relDeg))
                     + ": LC4/LPLC2 -> Giant Fiber -> TTMn if she can see it (~30 degrees behind her, she cannot)"
               )
               .formatted(Formatting.AQUA),
            false
         );
         return 1;
      }
   }

   private static int drum(ServerCommandSource src, double omega) {
      if (!OptomotorDrum.active()) {
         src.sendError(Text.literal("no drum here. Build the optomotor box first with /flylab arena optomotor"));
         return 0;
      } else {
         OptomotorDrum.spin(omega);
         src.sendFeedback(
            () -> Text.literal(
                  omega == 0.0
                     ? "the drum stops"
                     : "the drum turns " + (omega > 0.0 ? "clockwise" : "anticlockwise") + " at " + Math.abs(omega) + " deg/s: watch her HS cells and her turn"
               )
               .formatted(Formatting.AQUA),
            false
         );
         return 1;
      }
   }

   private static int lay(ServerCommandSource src) {
      ServerWorld world = src.getWorld();
      BlockPos here = BlockPos.ofFloored(src.getPosition());
      int n = 0;

      for (ConnectomeFlyEntity f : world.getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, new Box(here).expand(48.0), e -> !e.isMale() && !e.isBabyFly())) {
         f.layEgg("lays an egg (forced): it hatches in 30 s");
         n++;
      }

      int count = n;
      src.sendFeedback(
         () -> Text.literal(count == 0 ? "no adult female within 48 blocks" : "egg laid: it cracks in 30 s into a hatchling with its own brain")
            .formatted(Formatting.AQUA),
         false
      );
      return 1;
   }

   private static int status(ServerCommandSource src) {
      ServerWorld world = src.getWorld();
      BlockPos here = BlockPos.ofFloored(src.getPosition());
      int n = 0;

      for (ConnectomeFlyEntity f : world.getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, new Box(here).expand(64.0), e -> true)) {
         boolean good = f.isAlive() && f.getHealth() > 0.0F;
         String line = (f.isMale() ? "male fly: " : "female fly: ") + (good ? "good" : "bad (she is dead)");
         src.sendFeedback(() -> Text.literal(line).formatted(good ? Formatting.GREEN : Formatting.RED), false);
         n++;
      }

      if (n == 0) {
         src.sendFeedback(() -> Text.literal("bad: there is no fly within 64 blocks").formatted(Formatting.RED), false);
      }

      return 1;
   }

   private static int state(ServerCommandSource src) {
      ServerWorld world = src.getWorld();
      BlockPos here = BlockPos.ofFloored(src.getPosition());
      int n = 0;

      for (ConnectomeFlyEntity f : world.getEntitiesByType(ConnectomeFlyMod.CONNECTOME_FLY, new Box(here).expand(64.0), e -> true)) {
         String line = (f.isMale() ? "male " : "female ") + f.debugState();
         src.sendFeedback(() -> Text.literal(line).formatted(Formatting.GRAY), false);
         n++;
      }

      if (n == 0) {
         src.sendFeedback(() -> Text.literal("no fly within 64 blocks").formatted(Formatting.GRAY), false);
      }

      return 1;
   }

   private static int book(ServerCommandSource src) {
      ServerPlayerEntity p = src.getPlayer();
      if (p == null) {
         return 0;
      } else {
         ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
         NbtCompound nbt = stack.getOrCreateNbt();
         nbt.putString("title", "The Connectome Fly");
         nbt.putString("author", "the fly project");
         NbtList pages = new NbtList();
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

         for (String t : pageTexts) {
            pages.add(NbtString.of(Serializer.toJson(Text.literal(t))));
         }

         nbt.put("pages", pages);
         p.giveItemStack(stack);
         src.sendFeedback(() -> Text.literal("field guide added to your inventory").formatted(Formatting.AQUA), false);
         return 1;
      }
   }
}

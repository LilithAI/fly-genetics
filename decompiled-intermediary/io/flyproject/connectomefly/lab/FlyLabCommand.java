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
import net.minecraft.class_124;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3730;
import net.minecraft.class_2561.class_2562;

public final class FlyLabCommand {
   private static final SuggestionProvider<class_2168> ARENAS = (ctx, b) -> {
      List<String> ids = new ArrayList<>(ArenaBuilder.ids());
      ids.add("all");
      ids.add("list");
      return class_2172.method_9265(ids, b);
   };
   public static volatile boolean stacked = false;
   public static volatile int scanRadius = 160;

   private FlyLabCommand() {
   }

   public static void register(CommandDispatcher<class_2168> d) {
      LiteralArgumentBuilder<class_2168> flylab = (LiteralArgumentBuilder<class_2168>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247(
                                                      "flylab"
                                                   )
                                                   .executes(ctx -> help((class_2168)ctx.getSource())))
                                                .then(
                                                   ((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("spawn")
                                                            .executes(ctx -> spawn((class_2168)ctx.getSource(), false)))
                                                         .then(class_2170.method_9247("female").executes(ctx -> spawn((class_2168)ctx.getSource(), false))))
                                                      .then(class_2170.method_9247("male").executes(ctx -> spawn((class_2168)ctx.getSource(), true)))
                                                ))
                                             .then(
                                                ((LiteralArgumentBuilder)class_2170.method_9247("body")
                                                      .then(class_2170.method_9247("model").executes(ctx -> body((class_2168)ctx.getSource(), (byte)0))))
                                                   .then(class_2170.method_9247("bee").executes(ctx -> body((class_2168)ctx.getSource(), (byte)1)))
                                             ))
                                          .then(class_2170.method_9247("book").executes(ctx -> book((class_2168)ctx.getSource()))))
                                       .then(class_2170.method_9247("hungry").executes(ctx -> need((class_2168)ctx.getSource(), true))))
                                    .then(class_2170.method_9247("thirsty").executes(ctx -> need((class_2168)ctx.getSource(), false))))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("stacked")
                                             .executes(ctx -> stacked((class_2168)ctx.getSource(), stacked, 0)))
                                          .then(class_2170.method_9247("on").executes(ctx -> stacked((class_2168)ctx.getSource(), true, 0))))
                                       .then(
                                          ((LiteralArgumentBuilder)class_2170.method_9247("off")
                                                .executes(ctx -> stacked((class_2168)ctx.getSource(), false, 0)))
                                             .then(
                                                class_2170.method_9244("blocks", IntegerArgumentType.integer(8, 512))
                                                   .executes(ctx -> stacked((class_2168)ctx.getSource(), false, IntegerArgumentType.getInteger(ctx, "blocks")))
                                             )
                                       )
                                 ))
                              .then(
                                 ((LiteralArgumentBuilder)class_2170.method_9247("brains").executes(ctx -> brains((class_2168)ctx.getSource(), 0)))
                                    .then(
                                       class_2170.method_9244("n", IntegerArgumentType.integer(1, 64))
                                          .executes(ctx -> brains((class_2168)ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))
                                    )
                              ))
                           .then(class_2170.method_9247("state").executes(ctx -> status((class_2168)ctx.getSource()))))
                        .then(class_2170.method_9247("fly").executes(ctx -> fly((class_2168)ctx.getSource()))))
                     .then(class_2170.method_9247("lay").executes(ctx -> lay((class_2168)ctx.getSource()))))
                  .then(class_2170.method_9247("mate").executes(ctx -> mate((class_2168)ctx.getSource()))))
               .then(
                  ((LiteralArgumentBuilder)class_2170.method_9247("loom").executes(ctx -> loom((class_2168)ctx.getSource(), Double.NaN)))
                     .then(
                        class_2170.method_9244("degrees", DoubleArgumentType.doubleArg(-180.0, 360.0))
                           .executes(ctx -> loom((class_2168)ctx.getSource(), DoubleArgumentType.getDouble(ctx, "degrees")))
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("drum")
                           .then(class_2170.method_9247("left").executes(ctx -> drum((class_2168)ctx.getSource(), -40.0))))
                        .then(class_2170.method_9247("right").executes(ctx -> drum((class_2168)ctx.getSource(), 40.0))))
                     .then(class_2170.method_9247("fast").executes(ctx -> drum((class_2168)ctx.getSource(), 90.0))))
                  .then(class_2170.method_9247("stop").executes(ctx -> drum((class_2168)ctx.getSource(), 0.0)))
            ))
         .then(
            class_2170.method_9247("arena")
               .then(
                  class_2170.method_9244("sense", StringArgumentType.word())
                     .suggests(ARENAS)
                     .executes(ctx -> arena((class_2168)ctx.getSource(), StringArgumentType.getString(ctx, "sense")))
               )
         );
      if (Boolean.getBoolean("connectomefly.dev")) {
         flylab = (LiteralArgumentBuilder<class_2168>)flylab.then(
            ((LiteralArgumentBuilder)class_2170.method_9247("debug").requires(src -> src.method_9259(4) && !src.method_43737()))
               .executes(ctx -> state((class_2168)ctx.getSource()))
         );
      }

      d.register(flylab);
   }

   private static int clearFlies(class_3218 world, class_243 at, Boolean maleOnly) {
      int n = 0;
      double r2 = (double)scanRadius * scanRadius;

      for (ConnectomeFlyEntity f : world.method_18198(ConnectomeFlyMod.CONNECTOME_FLY, e -> e.method_5805())) {
         if (!(f.method_5707(at) > r2) && (maleOnly == null || f.isMale() == maleOnly && !f.isBabyFly())) {
            f.method_5932(true, false);
            f.method_31472();
            n++;
         }
      }

      return n;
   }

   private static ConnectomeFlyEntity place(class_3218 world, double x, double y, double z, boolean male) {
      ConnectomeFlyEntity fly = (ConnectomeFlyEntity)ConnectomeFlyMod.CONNECTOME_FLY.method_5883(world);
      if (fly != null) {
         fly.setMale(male);
         fly.setBodyMode(ConnectomeFlyEntity.defaultBody);
         fly.method_5808(x, y, z, 0.0F, 0.0F);
         fly.method_5943(world, world.method_8404(fly.method_24515()), class_3730.field_16462, null, null);
         world.method_8649(fly);
      }

      return fly;
   }

   public static int populate(class_3218 world, String id, class_2338 spawn) {
      int gone = stacked ? 0 : clearFlies(world, class_243.method_24955(spawn), null);
      double x = spawn.method_10263() + 0.5;
      double y = spawn.method_10264();
      double z = spawn.method_10260() + 0.5;
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

   private static int need(class_2168 src, boolean hunger) {
      int n = 0;

      for (ConnectomeFlyEntity f : src.method_9225()
         .method_18023(
            ConnectomeFlyMod.CONNECTOME_FLY, new class_238(class_2338.method_49638(src.method_9222())).method_1014(24.0), e -> e.method_5805() && !e.isMale()
         )) {
         if (hunger) {
            f.setHunger(5.0F);
         } else {
            f.setThirst(17.0F);
         }

         n++;
      }

      int count = n;
      src.method_9226(
         () -> class_2561.method_43470(
               count == 0
                  ? "no female within 24 blocks"
                  : (
                     hunger
                        ? "She is hungry now. Hunger turns up her food smell and taste, and her insulin cells (IPCs) go quiet"
                        : "She is thirsty now. Her humidity sensors turn attractive, so she walks to water or damp ground and drinks"
                  )
            )
            .method_27692(class_124.field_1075),
         false
      );
      return 1;
   }

   private static int stacked(class_2168 src, boolean on, int radius) {
      stacked = on;
      if (radius > 0) {
         scanRadius = radius;
      }

      src.method_9226(
         () -> class_2561.method_43470(
               on
                  ? "stacked ON. Nothing is removed, so every spawn and every box button adds flies. Brains run for the first "
                     + ConnectomeFlyEntity.maxBrains
                     + " females (/flylab brains <n>); the rest rest until one frees up."
                  : "stacked OFF. One experiment at a time. A spawn or a box button first removes the flies within "
                     + scanRadius
                     + " blocks, then spawns fresh ones (no teleporting)."
            )
            .method_27692(class_124.field_1075),
         false
      );
      return 1;
   }

   private static int brains(class_2168 src, int n) {
      if (n > 0) {
         ConnectomeFlyEntity.maxBrains = n;
      }

      src.method_9226(
         () -> class_2561.method_43470(
               "whole brains: "
                  + WholeBrain.running()
                  + " running, up to "
                  + ConnectomeFlyEntity.maxBrains
                  + " at once (this machine has "
                  + Runtime.getRuntime().availableProcessors()
                  + " cores and each brain keeps about one busy; leave a few for the game)"
            )
            .method_27692(class_124.field_1075),
         false
      );
      return 1;
   }

   private static int help(class_2168 src) {
      src.method_9226(
         () -> class_2561.method_43470(
               "/flylab spawn [female|male] | body <model|bee> | arena <sense|all|list> | stacked <on|off [blocks]> | brains [n] | hungry | thirsty | fly | lay | mate | loom [degrees] | drum <left|right|fast|stop> | state | book. and /flycredits for what is real vs calibrated"
            )
            .method_27692(class_124.field_1075),
         false
      );
      return 1;
   }

   private static int spawn(class_2168 src, boolean male) {
      class_3218 world = src.method_9225();
      class_3222 p = src.method_44023();
      class_243 at = src.method_9222();
      int gone = stacked ? 0 : clearFlies(world, at, male);
      ConnectomeFlyEntity fly = (ConnectomeFlyEntity)ConnectomeFlyMod.CONNECTOME_FLY.method_5883(world);
      if (fly == null) {
         return 0;
      } else {
         fly.setMale(male);
         fly.setBodyMode(ConnectomeFlyEntity.defaultBody);
         fly.method_5808(at.field_1352 + (p != null ? 2 : 0), at.field_1351, at.field_1350 + (p != null ? 2 : 0), p != null ? p.method_36454() : 0.0F, 0.0F);
         fly.method_5943(world, world.method_8404(fly.method_24515()), class_3730.field_16462, null, null);
         world.method_8649(fly);
         src.method_9226(
            () -> class_2561.method_43470(
                  male
                     ? "a MALE fly spawned: he courts the nearest female with his wing song."
                     : "fly spawned: her whole brain (158,262 neurons) is waking, the escape circuit is ticking. Sprint at her, or build a box: /flylab arena <sense>"
                        + (gone > 0 ? "  (" + gone + " earlier " + (gone == 1 ? "one" : "ones") + " removed: /flylab stacked on keeps them)" : "")
               )
               .method_27692(class_124.field_1075),
            false
         );
         return 1;
      }
   }

   private static int body(class_2168 src, byte mode) {
      ConnectomeFlyEntity.defaultBody = mode;
      int n = 0;

      for (class_3218 w : src.method_9211().method_3738()) {
         for (ConnectomeFlyEntity f : w.method_18198(ConnectomeFlyMod.CONNECTOME_FLY, e -> true)) {
            f.setBodyMode(mode);
            n++;
         }
      }

      int count = n;
      src.method_9226(
         () -> class_2561.method_43470(
               (mode == 1 ? "body: plain bee" : "body: the real fly model") + " (" + count + (count == 1 ? " fly" : " flies") + " updated; new ones follow)"
            )
            .method_27692(class_124.field_1075),
         false
      );
      return 1;
   }

   private static int arena(class_2168 src, String id) {
      if (id.equals("list")) {
         src.method_9226(() -> class_2561.method_43470("senses: " + String.join(", ", ArenaBuilder.ids()) + ", all").method_27692(class_124.field_1075), false);
         return 1;
      } else {
         class_3218 world = src.method_9225();
         class_2338 here = class_2338.method_49638(src.method_9222());
         if (id.equals("all")) {
            class_2338 spawn = ArenaBuilder.buildAll(world, here.method_10069(0, 0, 10));
            src.method_9226(
               () -> class_2561.method_43470(
                     "all "
                        + ArenaBuilder.SENSES.size()
                        + " sense boxes built north of you, six per row, all empty. Press the wooden button on the LIME post by a box's door: its fly appears inside (and, with /flylab stacked off, the fly of the last box is removed)."
                  )
                  .method_27692(class_124.field_1075),
               false
            );
            return 1;
         } else if (!ArenaBuilder.SENSES.containsKey(id)) {
            src.method_9213(class_2561.method_43470("unknown sense '" + id + "': /flylab arena list"));
            return 0;
         } else {
            class_2338 spawn = ArenaBuilder.build(world, here.method_10069(0, 0, 9), id);
            int gone = populate(world, id, spawn);
            String title = ArenaBuilder.SENSES.get(id)[0];
            String how = ArenaBuilder.instructions(id);
            src.method_9226(
               () -> class_2561.method_43470(
                     "box "
                        + title
                        + " built ahead of you: she is inside"
                        + (gone > 0 ? " (" + gone + " earlier " + (gone == 1 ? "fly" : "flies") + " removed; /flylab stacked on keeps them)." : ".")
                        + (id.equals("home") ? " Every sense is live in this one." : " Inside it only this experiment's senses are live.")
                  )
                  .method_27692(class_124.field_1075),
               false
            );
            src.method_9226(() -> class_2561.method_43470(how).method_27692(class_124.field_1080), false);
            return 1;
         }
      }
   }

   private static int fly(class_2168 src) {
      class_3218 world = src.method_9225();
      class_2338 here = class_2338.method_49638(src.method_9222());
      int n = 0;

      for (ConnectomeFlyEntity f : world.method_18023(ConnectomeFlyMod.CONNECTOME_FLY, new class_238(here).method_1014(48.0), e -> !e.isMale())) {
         f.takeOffNow();
         n++;
      }

      int count = n;
      src.method_9226(
         () -> class_2561.method_43470(count == 0 ? "no fly within 48 blocks" : "take-off: crouch, wings up, push. Then she hovers and roams from her brain")
            .method_27692(class_124.field_1075),
         false
      );
      return 1;
   }

   private static int mate(class_2168 src) {
      class_3218 world = src.method_9225();
      class_2338 here = class_2338.method_49638(src.method_9222());
      boolean female = false;
      boolean male = false;

      for (ConnectomeFlyEntity f : world.method_18023(ConnectomeFlyMod.CONNECTOME_FLY, new class_238(here).method_1014(48.0), e -> !e.isBabyFly())) {
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

      src.method_9226(
         () -> class_2561.method_43470("courtship reset: he sings, after 10 s she accepts, 10 s of mating, then the egg (hatches in 30 s)")
            .method_27692(class_124.field_1075),
         false
      );
      return 1;
   }

   private static int loom(class_2168 src, double relDeg) {
      class_3218 world = src.method_9225();
      class_2338 here = class_2338.method_49638(src.method_9222());
      ConnectomeFlyEntity best = null;
      double bd = 1.0E9;

      for (ConnectomeFlyEntity f : world.method_18023(ConnectomeFlyMod.CONNECTOME_FLY, new class_238(here).method_1014(48.0), e -> e.method_5805())) {
         double d = f.method_5649(here.method_10263() + 0.5, here.method_10264(), here.method_10260() + 0.5);
         if (d < bd) {
            bd = d;
            best = f;
         }
      }

      if (best == null) {
         src.method_9213(class_2561.method_43470("no fly within 48 blocks"));
         return 0;
      } else {
         LoomStimulus.spawn(world, best, relDeg);
         src.method_9226(
            () -> class_2561.method_43470(
                  "a shape looms in"
                     + (Double.isNaN(relDeg) ? "" : String.format(Locale.ROOT, " at %.0f degrees off her heading", relDeg))
                     + ": LC4/LPLC2 -> Giant Fiber -> TTMn if she can see it (~30 degrees behind her, she cannot)"
               )
               .method_27692(class_124.field_1075),
            false
         );
         return 1;
      }
   }

   private static int drum(class_2168 src, double omega) {
      if (!OptomotorDrum.active()) {
         src.method_9213(class_2561.method_43470("no drum here. Build the optomotor box first with /flylab arena optomotor"));
         return 0;
      } else {
         OptomotorDrum.spin(omega);
         src.method_9226(
            () -> class_2561.method_43470(
                  omega == 0.0
                     ? "the drum stops"
                     : "the drum turns " + (omega > 0.0 ? "clockwise" : "anticlockwise") + " at " + Math.abs(omega) + " deg/s: watch her HS cells and her turn"
               )
               .method_27692(class_124.field_1075),
            false
         );
         return 1;
      }
   }

   private static int lay(class_2168 src) {
      class_3218 world = src.method_9225();
      class_2338 here = class_2338.method_49638(src.method_9222());
      int n = 0;

      for (ConnectomeFlyEntity f : world.method_18023(
         ConnectomeFlyMod.CONNECTOME_FLY, new class_238(here).method_1014(48.0), e -> !e.isMale() && !e.isBabyFly()
      )) {
         f.layEgg("lays an egg (forced): it hatches in 30 s");
         n++;
      }

      int count = n;
      src.method_9226(
         () -> class_2561.method_43470(count == 0 ? "no adult female within 48 blocks" : "egg laid: it cracks in 30 s into a hatchling with its own brain")
            .method_27692(class_124.field_1075),
         false
      );
      return 1;
   }

   private static int status(class_2168 src) {
      class_3218 world = src.method_9225();
      class_2338 here = class_2338.method_49638(src.method_9222());
      int n = 0;

      for (ConnectomeFlyEntity f : world.method_18023(ConnectomeFlyMod.CONNECTOME_FLY, new class_238(here).method_1014(64.0), e -> true)) {
         boolean good = f.method_5805() && f.method_6032() > 0.0F;
         String line = (f.isMale() ? "male fly: " : "female fly: ") + (good ? "good" : "bad (she is dead)");
         src.method_9226(() -> class_2561.method_43470(line).method_27692(good ? class_124.field_1060 : class_124.field_1061), false);
         n++;
      }

      if (n == 0) {
         src.method_9226(() -> class_2561.method_43470("bad: there is no fly within 64 blocks").method_27692(class_124.field_1061), false);
      }

      return 1;
   }

   private static int state(class_2168 src) {
      class_3218 world = src.method_9225();
      class_2338 here = class_2338.method_49638(src.method_9222());
      int n = 0;

      for (ConnectomeFlyEntity f : world.method_18023(ConnectomeFlyMod.CONNECTOME_FLY, new class_238(here).method_1014(64.0), e -> true)) {
         String line = (f.isMale() ? "male " : "female ") + f.debugState();
         src.method_9226(() -> class_2561.method_43470(line).method_27692(class_124.field_1080), false);
         n++;
      }

      if (n == 0) {
         src.method_9226(() -> class_2561.method_43470("no fly within 64 blocks").method_27692(class_124.field_1080), false);
      }

      return 1;
   }

   private static int book(class_2168 src) {
      class_3222 p = src.method_44023();
      if (p == null) {
         return 0;
      } else {
         class_1799 stack = new class_1799(class_1802.field_8360);
         class_2487 nbt = stack.method_7948();
         nbt.method_10582("title", "The Connectome Fly");
         nbt.method_10582("author", "the fly project");
         class_2499 pages = new class_2499();
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
            pages.add(class_2519.method_23256(class_2562.method_10867(class_2561.method_43470(t))));
         }

         nbt.method_10566("pages", pages);
         p.method_7270(stack);
         src.method_9226(() -> class_2561.method_43470("field guide added to your inventory").method_27692(class_124.field_1075), false);
         return 1;
      }
   }
}

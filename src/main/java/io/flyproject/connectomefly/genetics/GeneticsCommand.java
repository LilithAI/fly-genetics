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
            .then(
               CommandManager.literal("memory")
                  .executes(ctx -> memory(ctx.getSource(), null))
                  .then(CommandManager.argument("on", BoolArgumentType.bool()).executes(ctx -> memory(ctx.getSource(), BoolArgumentType.getBool(ctx, "on"))))
            )
      );
   }

   private static int memory(ServerCommandSource src, Boolean on) {
      if (on != null) {
         Genome.inheritMemory = on;
      }

      src.sendFeedback(
         () -> Text.literal(
               "[model] inherited memory is "
                  + (Genome.inheritMemory ? "ON" : "OFF")
                  + ": a child starts with "
                  + Math.round(Genome.inheritMemoryFraction * 100.0F)
                  + "% of what its mother had learned. Not biology. Resets to OFF when the game restarts."
            )
            .formatted(Formatting.AQUA),
         false
      );
      return 1;
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

package io.flyproject.connectomefly.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Small shims for Minecraft calls that changed between 1.20.1 and 26.3, so the ported code keeps its shape. */
public final class Compat {
   private Compat() {
   }

   public static boolean isDay(Level level) {
      return level.isBrightOutside();
   }

   public static boolean isNight(Level level) {
      return !level.dimensionType().hasFixedTime() && !level.isBrightOutside();
   }

   /** Ticks of the dimension's default clock: what Level.getDayTime() was. */
   public static long dayTime(Level level) {
      return level.getDefaultClockTime();
   }

   public static final Block BLACK_CONCRETE = Blocks.CONCRETE.pick(DyeColor.BLACK);
   public static final Block WHITE_CONCRETE = Blocks.CONCRETE.pick(DyeColor.WHITE);
   public static final Block LIME_CONCRETE = Blocks.CONCRETE.pick(DyeColor.LIME);

   /** A finished written book (1.20.1 wrote "title", "author" and "pages" NBT by hand). */
   public static ItemStack book(String title, String author, String... pages) {
      ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
      List<Filterable<Component>> ps = new ArrayList<>();

      for (String p : pages) {
         ps.add(Filterable.passThrough(Component.literal(p)));
      }

      stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(title.length() > 32 ? title.substring(0, 32) : title), author, 0, ps, true));
      return stack;
   }

   /** Save an entity to NBT, let the caller change the NBT, load it back (what saveWithoutId + load did in 1.20.1). */
   public static void editNbt(Entity e, Consumer<CompoundTag> change) {
      TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, e.registryAccess());
      e.saveWithoutId(out);
      CompoundTag nbt = out.buildResult();
      change.accept(nbt);
      e.load(TagValueInput.create(ProblemReporter.DISCARDING, e.registryAccess(), nbt));
   }

   /** Front text of a sign: up to four lines. */
   public static SignText signText(List<String> lines) {
      List<Component> msgs = new ArrayList<>();

      for (int i = 0; i < 4; i++) {
         msgs.add(Component.literal(i < lines.size() ? lines.get(i) : ""));
      }

      return new SignText(msgs, msgs, DyeColor.BLACK, false);
   }
}

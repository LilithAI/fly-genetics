package io.flyproject.connectomefly.lab;

import io.flyproject.connectomefly.entity.FlyMind;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

public final class OptomotorDrum {
   private static final int PANELS = 16;
   private static final double RADIUS = 4.6;
   private static ServerLevel world;
   private static final List<BlockDisplay> panels = new ArrayList<>();
   private static double cx;
   private static double cz;
   private static double cy;
   private static double angle;
   private static double omega;

   public static void build(ServerLevel w, BlockPos centre) {
      clear();
      world = w;
      cx = centre.getX() + 0.5;
      cz = centre.getZ() + 0.5;
      cy = centre.getY();

      for (int i = 0; i < 16; i++) {
         BlockDisplay d = (BlockDisplay)EntityType.BLOCK_DISPLAY.create(w);
         if (d != null) {
            d.setPos(cx, cy, cz);
            CompoundTag nbt = new CompoundTag();
            d.saveWithoutId(nbt);
            nbt.putString("id", "minecraft:block_display");
            CompoundTag bs = new CompoundTag();
            bs.putString("Name", (i % 2 == 0 ? Blocks.BLACK_CONCRETE : Blocks.WHITE_CONCRETE).getDescriptionId().replace("block.minecraft.", "minecraft:"));
            nbt.put("block_state", bs);
            nbt.putInt("interpolation_duration", 2);
            nbt.putInt("start_interpolation", 0);
            nbt.put("transformation", transform(i));
            d.load(nbt);
            w.addFreshEntity(d);
            panels.add(d);
         }
      }

      omega = 0.0;
      FlyMind.drum = new double[]{cx, cz, 0.0, 4.6};
   }

   private static ListTag transform(int i) {
      double a = Math.toRadians(angle + i * 360.0 / 16.0);
      double px = Math.cos(a) * 4.6;
      double pz = Math.sin(a) * 4.6;
      double yaw = -a;
      double c = Math.cos(yaw);
      double s = Math.sin(yaw);
      double sx = 0.9;
      double sy = 3.0;
      double sz = 0.1;
      double[] m = new double[]{
         c * sx,
         0.0,
         -s * sx,
         0.0,
         0.0,
         sy,
         0.0,
         0.0,
         s * sz,
         0.0,
         c * sz,
         0.0,
         px - (c * sx * 0.5 + s * sz * 0.5),
         0.0,
         pz - (-s * sx * 0.5 + c * sz * 0.5),
         1.0
      };
      OptomotorDrum.NtbHolder h = new OptomotorDrum.NtbHolder(m);
      return h.list();
   }

   public static void spin(double degPerSec) {
      omega = degPerSec;
      if (FlyMind.drum != null) {
         FlyMind.drum = new double[]{cx, cz, omega, 4.6};
      }
   }

   public static boolean active() {
      return world != null && !panels.isEmpty();
   }

   public static double omega() {
      return omega;
   }

   public static void tick() {
      if (world != null && !panels.isEmpty() && omega != 0.0) {
         angle = angle + omega * 0.05;

         for (int i = 0; i < panels.size(); i++) {
            BlockDisplay d = panels.get(i);
            if (!d.isRemoved()) {
               CompoundTag nbt = new CompoundTag();
               d.saveWithoutId(nbt);
               nbt.put("transformation", transform(i));
               nbt.putInt("start_interpolation", 0);
               nbt.putInt("interpolation_duration", 2);
               d.load(nbt);
            }
         }
      }
   }

   public static void clear() {
      for (BlockDisplay d : panels) {
         d.discard();
      }

      panels.clear();
      world = null;
      omega = 0.0;
      FlyMind.drum = null;
   }

   private record NtbHolder(double[] m) {
      ListTag list() {
         ListTag l = new ListTag();

         for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
               l.add(FloatTag.valueOf((float)this.m[c * 4 + r]));
            }
         }

         return l;
      }
   }
}

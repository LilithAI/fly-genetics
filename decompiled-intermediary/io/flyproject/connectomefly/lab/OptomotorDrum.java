package io.flyproject.connectomefly.lab;

import io.flyproject.connectomefly.entity.FlyMind;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1299;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2494;
import net.minecraft.class_2499;
import net.minecraft.class_3218;
import net.minecraft.class_8113.class_8115;

public final class OptomotorDrum {
   private static final int PANELS = 16;
   private static final double RADIUS = 4.6;
   private static class_3218 world;
   private static final List<class_8115> panels = new ArrayList<>();
   private static double cx;
   private static double cz;
   private static double cy;
   private static double angle;
   private static double omega;

   public static void build(class_3218 w, class_2338 centre) {
      clear();
      world = w;
      cx = centre.method_10263() + 0.5;
      cz = centre.method_10260() + 0.5;
      cy = centre.method_10264();

      for (int i = 0; i < 16; i++) {
         class_8115 d = (class_8115)class_1299.field_42460.method_5883(w);
         if (d != null) {
            d.method_5814(cx, cy, cz);
            class_2487 nbt = new class_2487();
            d.method_5647(nbt);
            nbt.method_10582("id", "minecraft:block_display");
            class_2487 bs = new class_2487();
            bs.method_10582("Name", (i % 2 == 0 ? class_2246.field_10458 : class_2246.field_10107).method_9539().replace("block.minecraft.", "minecraft:"));
            nbt.method_10566("block_state", bs);
            nbt.method_10569("interpolation_duration", 2);
            nbt.method_10569("start_interpolation", 0);
            nbt.method_10566("transformation", transform(i));
            d.method_5651(nbt);
            w.method_8649(d);
            panels.add(d);
         }
      }

      omega = 0.0;
      FlyMind.drum = new double[]{cx, cz, 0.0, 4.6};
   }

   private static class_2499 transform(int i) {
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
            class_8115 d = panels.get(i);
            if (!d.method_31481()) {
               class_2487 nbt = new class_2487();
               d.method_5647(nbt);
               nbt.method_10566("transformation", transform(i));
               nbt.method_10569("start_interpolation", 0);
               nbt.method_10569("interpolation_duration", 2);
               d.method_5651(nbt);
            }
         }
      }
   }

   public static void clear() {
      for (class_8115 d : panels) {
         d.method_31472();
      }

      panels.clear();
      world = null;
      omega = 0.0;
      FlyMind.drum = null;
   }

   private record NtbHolder(double[] m) {
      class_2499 list() {
         class_2499 l = new class_2499();

         for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
               l.add(class_2494.method_23244((float)this.m[c * 4 + r]));
            }
         }

         return l;
      }
   }
}

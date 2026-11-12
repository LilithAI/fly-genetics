package io.flyproject.connectomefly.lab;

import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_1299;
import net.minecraft.class_2246;
import net.minecraft.class_2487;
import net.minecraft.class_2494;
import net.minecraft.class_2499;
import net.minecraft.class_3218;
import net.minecraft.class_8113.class_8115;

public final class LoomStimulus {
   public static final double LOOM_EXPAND = 0.3;
   public static final double START_RADIUS = 0.12;
   public static final double DONE_THETA = 1.0;
   private static final double DISTANCE = 4.5;
   private static final List<LoomStimulus.Loom> LOOMS = new ArrayList<>();
   private static int nextId = 1000000;

   public static LoomStimulus.Loom spawn(class_3218 world, ConnectomeFlyEntity fly) {
      return spawn(world, fly, Double.NaN);
   }

   public static LoomStimulus.Loom spawn(class_3218 world, ConnectomeFlyEntity fly, double relDeg) {
      double bearing;
      if (Double.isNaN(relDeg)) {
         bearing = world.field_9229.method_43058() * Math.PI * 2.0;
      } else {
         double yaw = Math.toRadians(fly.method_36454() + relDeg);
         bearing = Math.atan2(Math.cos(yaw), -Math.sin(yaw));
      }

      double x = fly.method_23317() + Math.cos(bearing) * 4.5;
      double z = fly.method_23321() + Math.sin(bearing) * 4.5;
      LoomStimulus.Loom l = new LoomStimulus.Loom(world, fly, x, fly.method_23320(), z, nextId++);
      synchronized (LOOMS) {
         LOOMS.add(l);
         return l;
      }
   }

   public static List<LoomStimulus.Loom> activeFor(ConnectomeFlyEntity fly) {
      List<LoomStimulus.Loom> out = new ArrayList<>();
      synchronized (LOOMS) {
         for (LoomStimulus.Loom l : LOOMS) {
            if (l.target == fly && !l.done) {
               out.add(l);
            }
         }

         return out;
      }
   }

   public static void tick() {
      synchronized (LOOMS) {
         Iterator<LoomStimulus.Loom> it = LOOMS.iterator();

         while (it.hasNext()) {
            LoomStimulus.Loom l = it.next();
            l.tick();
            if (l.expired()) {
               it.remove();
            }
         }
      }
   }

   public static final class Loom {
      public final class_3218 world;
      public final ConnectomeFlyEntity target;
      public final double x;
      public final double y;
      public final double z;
      public double radius = 0.12;
      public boolean done;
      public double fade;
      public final int id;
      private final class_8115 panel;

      Loom(class_3218 world, ConnectomeFlyEntity target, double x, double y, double z, int id) {
         this.world = world;
         this.target = target;
         this.x = x;
         this.y = y;
         this.z = z;
         this.id = id;
         class_8115 d = (class_8115)class_1299.field_42460.method_5883(world);
         this.panel = d;
         if (d != null) {
            d.method_5814(x, y, z);
            class_2487 nbt = new class_2487();
            d.method_5647(nbt);
            nbt.method_10582("id", "minecraft:block_display");
            class_2487 bs = new class_2487();
            bs.method_10582("Name", class_2246.field_10458.method_9539().replace("block.minecraft.", "minecraft:"));
            nbt.method_10566("block_state", bs);
            nbt.method_10569("interpolation_duration", 1);
            nbt.method_10569("start_interpolation", 0);
            nbt.method_10566("transformation", this.transform());
            d.method_5651(nbt);
            world.method_8649(d);
         }
      }

      private class_2499 transform() {
         double dx = this.target.method_23317() - this.x;
         double dz = this.target.method_23321() - this.z;
         double yaw = Math.atan2(dx, dz);
         double c = Math.cos(yaw);
         double s = Math.sin(yaw);
         double side = this.radius * 2.0;
         double thin = 0.05;
         double[] m = new double[]{
            c * side,
            0.0,
            -s * side,
            0.0,
            0.0,
            side,
            0.0,
            0.0,
            s * thin,
            0.0,
            c * thin,
            0.0,
            -(c * side * 0.5) - s * thin * 0.5,
            -side * 0.5,
            s * side * 0.5 - c * thin * 0.5,
            1.0
         };
         class_2499 l = new class_2499();

         for (int r = 0; r < 4; r++) {
            for (int cc = 0; cc < 4; cc++) {
               l.add(class_2494.method_23244((float)m[cc * 4 + r]));
            }
         }

         return l;
      }

      void tick() {
         if (!this.done) {
            double dist = Math.max(0.3, Math.hypot(this.target.method_23317() - this.x, this.target.method_23321() - this.z));
            this.radius += 0.3 * dist * 0.05;
            if (2.0 * Math.atan(this.radius / dist) > 1.0) {
               this.done = true;
            }
         } else {
            this.fade += 0.05;
         }

         if (this.panel != null && !this.panel.method_31481()) {
            if (this.fade > 0.7) {
               this.panel.method_31472();
               return;
            }

            class_2487 nbt = new class_2487();
            this.panel.method_5647(nbt);
            nbt.method_10566("transformation", this.transform());
            nbt.method_10569("start_interpolation", 0);
            nbt.method_10569("interpolation_duration", 1);
            if (this.done) {
               class_2487 bs = new class_2487();
               bs.method_10582("Name", "minecraft:gray_concrete");
               nbt.method_10566("block_state", bs);
            }

            this.panel.method_5651(nbt);
         }
      }

      boolean expired() {
         return this.done && this.fade > 0.7 || !this.target.method_5805();
      }
   }
}

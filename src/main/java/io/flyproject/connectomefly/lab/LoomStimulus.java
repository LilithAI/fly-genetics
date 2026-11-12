package io.flyproject.connectomefly.lab;

import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;

public final class LoomStimulus {
   public static final double LOOM_EXPAND = 0.3;
   public static final double START_RADIUS = 0.12;
   public static final double DONE_THETA = 1.0;
   private static final double DISTANCE = 4.5;
   private static final List<LoomStimulus.Loom> LOOMS = new ArrayList<>();
   private static int nextId = 1000000;

   public static LoomStimulus.Loom spawn(ServerWorld world, ConnectomeFlyEntity fly) {
      return spawn(world, fly, Double.NaN);
   }

   public static LoomStimulus.Loom spawn(ServerWorld world, ConnectomeFlyEntity fly, double relDeg) {
      double bearing;
      if (Double.isNaN(relDeg)) {
         bearing = world.random.nextDouble() * Math.PI * 2.0;
      } else {
         double yaw = Math.toRadians(fly.getYaw() + relDeg);
         bearing = Math.atan2(Math.cos(yaw), -Math.sin(yaw));
      }

      double x = fly.getX() + Math.cos(bearing) * 4.5;
      double z = fly.getZ() + Math.sin(bearing) * 4.5;
      LoomStimulus.Loom l = new LoomStimulus.Loom(world, fly, x, fly.getEyeY(), z, nextId++);
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
      public final ServerWorld world;
      public final ConnectomeFlyEntity target;
      public final double x;
      public final double y;
      public final double z;
      public double radius = 0.12;
      public boolean done;
      public double fade;
      public final int id;
      private final BlockDisplayEntity panel;

      Loom(ServerWorld world, ConnectomeFlyEntity target, double x, double y, double z, int id) {
         this.world = world;
         this.target = target;
         this.x = x;
         this.y = y;
         this.z = z;
         this.id = id;
         BlockDisplayEntity d = (BlockDisplayEntity)EntityType.BLOCK_DISPLAY.create(world);
         this.panel = d;
         if (d != null) {
            d.setPosition(x, y, z);
            NbtCompound nbt = new NbtCompound();
            d.writeNbt(nbt);
            nbt.putString("id", "minecraft:block_display");
            NbtCompound bs = new NbtCompound();
            bs.putString("Name", Blocks.BLACK_CONCRETE.getTranslationKey().replace("block.minecraft.", "minecraft:"));
            nbt.put("block_state", bs);
            nbt.putInt("interpolation_duration", 1);
            nbt.putInt("start_interpolation", 0);
            nbt.put("transformation", this.transform());
            d.readNbt(nbt);
            world.spawnEntity(d);
         }
      }

      private NbtList transform() {
         double dx = this.target.getX() - this.x;
         double dz = this.target.getZ() - this.z;
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
         NbtList l = new NbtList();

         for (int r = 0; r < 4; r++) {
            for (int cc = 0; cc < 4; cc++) {
               l.add(NbtFloat.of((float)m[cc * 4 + r]));
            }
         }

         return l;
      }

      void tick() {
         if (!this.done) {
            double dist = Math.max(0.3, Math.hypot(this.target.getX() - this.x, this.target.getZ() - this.z));
            this.radius += 0.3 * dist * 0.05;
            if (2.0 * Math.atan(this.radius / dist) > 1.0) {
               this.done = true;
            }
         } else {
            this.fade += 0.05;
         }

         if (this.panel != null && !this.panel.isRemoved()) {
            if (this.fade > 0.7) {
               this.panel.discard();
               return;
            }

            NbtCompound nbt = new NbtCompound();
            this.panel.writeNbt(nbt);
            nbt.put("transformation", this.transform());
            nbt.putInt("start_interpolation", 0);
            nbt.putInt("interpolation_duration", 1);
            if (this.done) {
               NbtCompound bs = new NbtCompound();
               bs.putString("Name", "minecraft:gray_concrete");
               nbt.put("block_state", bs);
            }

            this.panel.readNbt(nbt);
         }
      }

      boolean expired() {
         return this.done && this.fade > 0.7 || !this.target.isAlive();
      }
   }
}

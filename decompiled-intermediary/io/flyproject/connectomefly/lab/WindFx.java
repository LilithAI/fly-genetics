package io.flyproject.connectomefly.lab;

import net.minecraft.class_2398;
import net.minecraft.class_3218;
import net.minecraft.server.MinecraftServer;

public final class WindFx {
   private WindFx() {
   }

   public static void puff(class_3218 world, double x, double y, double z, double dx, double dz) {
      double oy = (world.field_9229.method_43058() - 0.5) * 0.9;
      double os = (world.field_9229.method_43058() - 0.5) * 0.9;
      world.method_14199(class_2398.field_11204, x - dz * os, y + oy, z + dx * os, 0, dx, 0.0, dz, 0.32);
   }

   public static void tick(MinecraftServer server) {
      for (class_3218 world : server.method_3738()) {
         if (world.method_8510() % 2L == 0L && !world.method_18456().isEmpty()) {
            for (ArenaBuilder.Arena a : ArenaBuilder.all(world)) {
               if (a.id().equals("wind")) {
                  double fx = a.x0() + 2.2;
                  double fy = a.y0() + 1.5;
                  double fz = (a.z0() + a.z1()) / 2.0 + 0.5;
                  if (world.method_18459(fx, fy, fz, 48.0, false) != null) {
                     puff(world, fx, fy, fz, 1.0, 0.0);
                  }
               }
            }
         }
      }
   }
}

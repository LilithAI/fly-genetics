package io.flyproject.connectomefly.lab;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public final class WindFx {
   private WindFx() {
   }

   public static void puff(ServerWorld world, double x, double y, double z, double dx, double dz) {
      double oy = (world.random.nextDouble() - 0.5) * 0.9;
      double os = (world.random.nextDouble() - 0.5) * 0.9;
      world.spawnParticles(ParticleTypes.CLOUD, x - dz * os, y + oy, z + dx * os, 0, dx, 0.0, dz, 0.32);
   }

   public static void tick(MinecraftServer server) {
      for (ServerWorld world : server.getWorlds()) {
         if (world.getTime() % 2L == 0L && !world.getPlayers().isEmpty()) {
            for (ArenaBuilder.Arena a : ArenaBuilder.all(world)) {
               if (a.id().equals("wind")) {
                  double fx = a.x0() + 2.2;
                  double fy = a.y0() + 1.5;
                  double fz = (a.z0() + a.z1()) / 2.0 + 0.5;
                  if (world.getClosestPlayer(fx, fy, fz, 48.0, false) != null) {
                     puff(world, fx, fy, fz, 1.0, 0.0);
                  }
               }
            }
         }
      }
   }
}

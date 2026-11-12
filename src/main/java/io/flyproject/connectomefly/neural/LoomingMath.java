package io.flyproject.connectomefly.neural;

public final class LoomingMath {
   public static final double MIN_DISTANCE = 0.05;
   public static final double VISION_FULL_DEG = 140.0;
   public static final double VISION_BLIND_DEG = 165.0;
   public static final double WALL_APPARENT_RADIUS = 0.5;

   private LoomingMath() {
   }

   public static double angularSize(double distanceBlocks, double radius) {
      double d = Math.max(distanceBlocks, 0.05);
      return 2.0 * Math.atan(radius / d);
   }

   public static double translateLooming(double thetaPrev, double thetaNow, double dt) {
      return dt <= 0.0 ? 0.0 : Math.max(0.0, (thetaNow - thetaPrev) / dt);
   }

   public static double visualFieldGain(double bearingDeg) {
      double b = Math.abs(((bearingDeg + 180.0) % 360.0 + 360.0) % 360.0 - 180.0);
      if (b <= 140.0) {
         return 1.0;
      } else {
         return b >= 165.0 ? 0.0 : (165.0 - b) / 25.0;
      }
   }

   public static double elevationDeg(double dy, double horizontalDist) {
      return Math.toDegrees(Math.atan2(dy, Math.max(horizontalDist, 1.0E-6)));
   }

   public static double visualFieldGain3d(double bearingDeg, double elevDeg) {
      double w = Math.max(0.0, Math.min(1.0, Math.abs(elevDeg) / 90.0));
      return (1.0 - w) * visualFieldGain(bearingDeg) + w;
   }
}

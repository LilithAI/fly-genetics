package io.flyproject.connectomefly.neural;

public final class EscapeBrain {
   public static final int JUMP_COOLDOWN_TICKS = 10;
   public static final double FLEE_LOOM = 0.12;
   public static final double FLEE_DISTANCE = 6.0;
   public static final int FLEE_TURN_TICKS = 9;
   public static final double FLEE_RUN_TURN_CAP = 0.6666666666666666;
   public static final double TURN_GAIN_PER_SPIKE = 0.03;
   public static final double MAX_TURN_PER_TICK = 5.0;
   public static final double WALK_GAIN_PER_SPIKE = 8.0E-4;
   public static final double MAX_STEP_PER_TICK = 0.16666666666666666;
   private final EscapeCircuit escape;
   private final SteeringCircuit steering;
   private final EscapeBrain.Intent intent = new EscapeBrain.Intent();
   private long lastJumpTick = -1000000L;
   private long fleeCommittedTick = -1L;
   private double fleeSide;

   public EscapeBrain(Circuit escapeCircuit, Circuit steeringCircuit) {
      this.escape = new EscapeCircuit(escapeCircuit);
      this.steering = new SteeringCircuit(steeringCircuit);
   }

   public EscapeBrain.Intent tick(long gameTick, EscapeBrain.Sensed sensed) {
      this.intent.jump = false;
      this.intent.fleeing = false;
      this.intent.yawDeltaDeg = 0.0;
      this.intent.stepBlocks = 0.0;
      this.intent.speechCategory = null;
      this.intent.speechThreatName = sensed.threatName;
      double loom = sensed.threatLoomDrive;
      double size = sensed.threatThetaDrive;
      EscapeCircuit.TickResult r = this.escape.tick(loom, size);
      this.intent.dnSpikes = r.dnSpikes;
      this.intent.sensorySpikes = r.sensorySpikes;
      if (r.ttmnFired && gameTick - this.lastJumpTick >= 10L) {
         this.intent.jump = true;
         this.lastJumpTick = gameTick;
         if (sensed.threatFound && sensed.threatLoom > 0.0) {
            this.intent.speechCategory = "jump_threat";
         }
      }

      boolean gateOpen = sensed.threatFound && (sensed.threatLoomDrive > 0.12 || sensed.threatDist < 6.0 && !"the looming shape".equals(sensed.threatName));
      if (gateOpen) {
         if (this.fleeCommittedTick < 0L) {
            this.fleeCommittedTick = gameTick;
            this.fleeSide = sensed.threatSide >= 0.0 ? 1.0 : -1.0;
            if (this.intent.speechCategory == null) {
               this.intent.speechCategory = "flee";
            }
         }

         this.steering.drive(this.fleeSide);
         double yaw = (this.steering.lastRightMotor - this.steering.lastLeftMotor) * 0.03;
         yaw = Math.max(-5.0, Math.min(5.0, yaw));
         double step = Math.min(0.16666666666666666, (this.steering.lastLeftMotor + this.steering.lastRightMotor) * 8.0E-4);
         if (gameTick - this.fleeCommittedTick > 9L) {
            yaw = Math.max(-0.6666666666666666, Math.min(0.6666666666666666, yaw));
         }

         this.intent.fleeing = true;
         this.intent.yawDeltaDeg = yaw;
         this.intent.stepBlocks = step;
      } else {
         this.fleeCommittedTick = -1L;
         this.steering.drive(0.0);
      }

      return this.intent;
   }

   public static final class Intent {
      public boolean jump;
      public boolean fleeing;
      public double yawDeltaDeg;
      public double stepBlocks;
      public String speechCategory;
      public String speechThreatName;
      public int dnSpikes;
      public int sensorySpikes;
   }

   public static final class Sensed {
      public boolean threatFound;
      public double threatDist = Double.POSITIVE_INFINITY;
      public double threatLoom;
      public double threatLoomDrive;
      public double threatThetaDrive;
      public double threatSide;
      public double wallLoom;
      public String threatName = "";
   }
}

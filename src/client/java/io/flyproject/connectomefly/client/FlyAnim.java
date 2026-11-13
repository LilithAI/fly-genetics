package io.flyproject.connectomefly.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class FlyAnim {
   private static final Quaternionf _qa = new Quaternionf();
   private static final Quaternionf _qb = new Quaternionf();
   private static final Quaternionf _qc = new Quaternionf();
   private static final float[][][] FLIGHT_LEGS = new float[][][]{
      {{2.15F, 1.5F, -1.4F}, {-2.2F, -1.5F, -1.4F}}, {{-1.15F, -0.15F, 0.7F}, {1.1F, 0.15F, 0.7F}}, {{-1.45F, 0.3F, 0.7F}, {-2.5F, 0.9F, 0.7F}}
   };
   private static final float[][][] LANDING_LEGS = new float[][][]{
      {{1.4F, 0.9F, -0.5F}, {0.5F, 1.05F, -0.65F}}, {{0.5F, -0.3F, -0.65F}, {-0.55F, 0.3F, -0.65F}}, {{0.65F, -0.75F, -0.35F}, {-0.7F, 0.75F, -0.35F}}
   };
   private static final float[][] STROKE = new float[][]{
      {-0.15F, 1.15F},
      {-0.051F, 1.075F},
      {0.041F, 1.005F},
      {0.126F, 0.94F},
      {0.201F, 0.88F},
      {0.271F, 0.824F},
      {0.342F, 0.765F},
      {0.403F, 0.713F},
      {0.469F, 0.657F},
      {0.528F, 0.605F},
      {0.581F, 0.557F},
      {0.634F, 0.508F},
      {0.689F, 0.455F},
      {0.735F, 0.41F},
      {0.784F, 0.359F},
      {0.829F, 0.311F},
      {0.872F, 0.263F},
      {0.913F, 0.215F},
      {0.952F, 0.166F},
      {0.988F, 0.117F},
      {1.021F, 0.069F},
      {1.05F, 0.021F},
      {1.078F, -0.032F},
      {1.1F, -0.085F},
      {1.118F, -0.145F},
      {1.126F, -0.203F},
      {1.122F, -0.261F},
      {1.105F, -0.313F},
      {1.078F, -0.355F},
      {1.044F, -0.391F},
      {1.002F, -0.422F},
      {0.95F, -0.45F}
   };
   private static final float BEAT_HZ = 8.0F;
   private final float[] wzx = new float[2];
   public final Quaternionf[] pose;
   public final Quaternionf[] poseGhostA;
   public final Quaternionf[] poseGhostB;
   public boolean inFlight;
   public boolean ghostsReady;
   public VertexBuffer legsVb;
   public VertexBuffer wingLVb;
   public VertexBuffer wingRVb;
   public VertexBuffer wingLGhostA;
   public VertexBuffer wingLGhostB;
   public VertexBuffer wingRGhostA;
   public VertexBuffer wingRGhostB;
   public float[] legsOut;
   public float[] wingLOut;
   public float[] wingROut;
   public BufferBuilder legsBB;
   public BufferBuilder wingLBB;
   public BufferBuilder wingRBB;
   private final FlyRig rig;
   private double lastT;
   private double lastYaw;
   private Vec3 lastPos;
   private double vx;
   private double vy;
   private double vz;
   private double yawRate;
   private float gaitT;
   private float groomT;
   private float pitch;
   private float roll;
   private float bob;
   private float prepT;
   private float landT;
   private float jumpT = 1.0F;
   private final float[] legDrive = new float[6];
   private float steerL;
   private float steerR;
   private float halL;
   private float halR;
   private float resp;
   private float neckL;
   private float neckR;
   private float headYaw;
   private float headNod;
   private float breathPhase;
   private final float[] twitch = new float[6];
   private boolean wasAirborne;
   private boolean wasGrooming;
   public int groomPhase = -1;

   private static void wingAt(float p, float[] out) {
      float c = (1.0F - (float)Math.cos(p * 2.0F * Math.PI)) / 2.0F;
      float f = c * (STROKE.length - 1);
      int i = Math.min(STROKE.length - 2, (int)f);
      float t = f - i;
      out[0] = STROKE[i][0] + (STROKE[i + 1][0] - STROKE[i][0]) * t;
      out[1] = STROKE[i][1] + (STROKE[i + 1][1] - STROKE[i][1]) * t;
   }

   public float headYaw() {
      return this.headYaw;
   }

   public float headNod() {
      return this.headNod;
   }

   private static float q(int packed, int slot) {
      return (packed >>> slot * 4 & 15) / 4.0F;
   }

   public FlyAnim(FlyRig rig) {
      this.rig = rig;
      this.pose = new Quaternionf[rig.bones.length + 1];
      this.poseGhostA = new Quaternionf[rig.bones.length + 1];
      this.poseGhostB = new Quaternionf[rig.bones.length + 1];

      for (int i = 0; i < rig.bones.length; i++) {
         this.pose[i + 1] = new Quaternionf(rig.bones[i].rest);
         this.poseGhostA[i + 1] = this.pose[i + 1];
         this.poseGhostB[i + 1] = this.pose[i + 1];
      }

      for (FlyRig.Wing W : rig.wings) {
         this.poseGhostA[W.root] = new Quaternionf(rig.bones[W.root - 1].rest);
         this.poseGhostB[W.root] = new Quaternionf(rig.bones[W.root - 1].rest);
      }

      this.legsVb = new VertexBuffer(Usage.DYNAMIC);
      this.wingLVb = new VertexBuffer(Usage.DYNAMIC);
      this.wingRVb = new VertexBuffer(Usage.DYNAMIC);
      this.wingLGhostA = new VertexBuffer(Usage.DYNAMIC);
      this.wingLGhostB = new VertexBuffer(Usage.DYNAMIC);
      this.wingRGhostA = new VertexBuffer(Usage.DYNAMIC);
      this.wingRGhostB = new VertexBuffer(Usage.DYNAMIC);
      this.legsOut = new float[rig.legsG.nVerts * 3];
      this.wingLOut = new float[rig.wingLG.nVerts * 3];
      this.wingROut = new float[rig.wingRG.nVerts * 3];
      this.legsBB = new BufferBuilder(rig.legsG.nIdx * 24 + 1024);
      this.wingLBB = new BufferBuilder(rig.wingLG.nIdx * 24 + 1024);
      this.wingRBB = new BufferBuilder(rig.wingRG.nIdx * 24 + 1024);
   }

   public void close() {
      this.legsVb.close();
      this.wingLVb.close();
      this.wingRVb.close();
      this.wingLGhostA.close();
      this.wingLGhostB.close();
      this.wingRGhostA.close();
      this.wingRGhostB.close();
   }

   public float pitch() {
      return this.pitch;
   }

   public float roll() {
      return this.roll;
   }

   public float bob() {
      return this.bob;
   }

   public void update(ConnectomeFlyEntity e, double t, Vec3 p, float yaw) {
      double dt = this.lastT > 0.0 ? Math.min(0.1, t - this.lastT) : 0.016;
      if (this.lastPos != null && dt > 0.0) {
         Vec3 v = p.subtract(this.lastPos).scale(1.0 / dt);
         this.vx = this.vx + (v.x - this.vx) * 0.25;
         this.vy = this.vy + (v.y - this.vy) * 0.25;
         this.vz = this.vz + (v.z - this.vz) * 0.25;
         double dyaw = Mth.wrapDegrees((float)(yaw - this.lastYaw));
         this.yawRate = this.yawRate + (dyaw / dt - this.yawRate) * 0.15;
      }

      this.lastPos = p;
      this.lastYaw = yaw;
      this.lastT = t;
      float fdt = (float)dt;
      double ts = t * 1000.0;
      float BLOCKS_PER_UNIT = 0.95F;
      float speed = (float)(Math.hypot(this.vx, this.vz) / 0.95F);
      float vyU = (float)(this.vy / 0.95F);
      int flags = e.animFlags();
      boolean flapping = (flags & 1) != 0;
      boolean grooming = (flags & 2) != 0;
      boolean sleeping = (flags & 4) != 0;
      boolean prep = (flags & 16) != 0;
      boolean landingFlag = (flags & 32) != 0;
      boolean singing = (flags & 64) != 0;
      boolean approach = (flags & 128) != 0;
      boolean perched = e.isPerched();
      boolean airborne = !e.onGround() && !perched;
      if (perched) {
         speed = (float)(Math.sqrt(this.vx * this.vx + this.vy * this.vy + this.vz * this.vz) / 0.95F);
      }

      boolean moving = speed > 0.14 && !airborne;
      this.prepT = prep ? 0.09F : 0.0F;
      if (airborne && !this.wasAirborne) {
         this.jumpT = 0.0F;
      }

      this.jumpT += fdt;
      if (!airborne && this.wasAirborne && vyU < -0.8F) {
         this.landT = 0.22F;
      }

      if (landingFlag && this.landT <= 0.0F) {
         this.landT = 0.22F;
      }

      this.landT = Math.max(0.0F, this.landT - fdt);
      this.wasAirborne = airborne;
      boolean crouch = this.prepT > 0.0F || !airborne && this.landT > 0.0F;
      int nl = e.getNeuroLegs();
      int nf = e.getNeuroFlight();

      for (int i = 0; i < 6; i++) {
         float target = nl == 0 ? 1.0F : q(nl, i);
         this.legDrive[i] = this.legDrive[i] + (target - this.legDrive[i]) * Math.min(1.0F, fdt * 6.0F);
         if (!moving && !airborne && target >= 2.2F && this.twitch[i] <= 0.0F) {
            this.twitch[i] = 0.35F;
         }

         this.twitch[i] = Math.max(0.0F, this.twitch[i] - fdt);
      }

      float k5 = Math.min(1.0F, fdt * 5.0F);
      this.steerL = this.steerL + ((nf == 0 ? 1.0F : q(nf, 0)) - this.steerL) * k5;
      this.steerR = this.steerR + ((nf == 0 ? 1.0F : q(nf, 1)) - this.steerR) * k5;
      this.halL = this.halL + ((nf == 0 ? 1.0F : q(nf, 2)) - this.halL) * k5;
      this.halR = this.halR + ((nf == 0 ? 1.0F : q(nf, 3)) - this.halR) * k5;
      this.resp = this.resp + ((nf == 0 ? 1.0F : q(nf, 4)) - this.resp) * k5;
      this.neckL = this.neckL + ((nf == 0 ? 1.0F : q(nf, 5)) - this.neckL) * k5;
      this.neckR = this.neckR + ((nf == 0 ? 1.0F : q(nf, 6)) - this.neckR) * k5;
      float tgtHeadYaw = Math.max(-0.6F, Math.min(0.6F, (this.neckL - this.neckR) * 0.45F));
      this.headYaw = this.headYaw + (tgtHeadYaw - this.headYaw) * Math.min(1.0F, fdt * 4.0F);
      this.breathPhase = this.breathPhase + fdt * (float) (Math.PI * 2) * (0.6F + 0.5F * Math.min(3.0F, this.resp));
      this.gaitT += fdt * (6.0F + speed * 16.0F);
      float tBob = airborne
         ? 0.02F + (speed < 0.4F ? 0.035F * (float)Math.sin(t * 3.1) : 0.012F * (float)Math.sin(t * 9.0))
         : (
            crouch
               ? -0.06F
               : (
                  sleeping
                     ? -0.1F + 0.006F * (float)Math.sin(t * 1.3)
                     : (
                        moving
                           ? (float)Math.sin(this.gaitT * 2.0F) * 0.03F
                           : (float)Math.sin(this.breathPhase) * 0.006F * (0.5F + 0.5F * Math.min(3.0F, this.resp))
                     )
               )
         );
      this.bob = tBob;
      float tgtPitch = airborne ? Math.max(-0.12F, Math.min(0.38F, vyU * 0.07F + speed * 0.08F)) : (crouch ? -0.1F : -Math.min(0.12F, speed * 0.1F));
      float tgtRoll = airborne
         ? (float)Math.max(-0.5, Math.min(0.5, -this.yawRate * 0.0025 + (this.steerR - this.steerL) * 0.1 + (this.halR - this.halL) * 0.06))
         : 0.0F;
      if (grooming && !this.wasGrooming) {
         this.groomT = 0.0F;
      }

      this.wasGrooming = grooming;
      this.groomT = grooming && !moving && !airborne ? this.groomT + fdt : 0.0F;
      float gph = grooming && !moving && !airborne ? this.groomT % 4.6F : -1.0F;
      int phase = gph < 0.0F ? -1 : (gph < 1.3F ? 0 : (gph < 3.6F ? 1 : 2));
      this.groomPhase = phase;
      float rub = (float)Math.sin(ts * 0.045);
      if (phase >= 0) {
         tgtPitch = phase == 0 ? -0.14F : 0.1F;
      }

      float tgtNod = phase == 0 ? 0.25F + rub * 0.05F : (sleeping ? 0.38F : 0.0F);
      this.headNod = this.headNod + (tgtNod - this.headNod) * Math.min(1.0F, fdt * 5.0F);
      this.pitch = this.pitch + (tgtPitch - this.pitch) * Math.min(1.0F, fdt * 7.0F);
      this.roll = this.roll + (tgtRoll - this.roll) * Math.min(1.0F, fdt * 6.0F);
      float kL = Math.min(1.0F, fdt * 14.0F);

      for (FlyRig.Leg L : this.rig.legs) {
         float fold = 0.0F;
         float rollA = 0.0F;
         float swing;
         float lift;
         if (crouch) {
            swing = L.row == 0 ? 0.25F : (L.row == 1 ? 0.0F : -0.3F);
            lift = 0.18F * L.sign;
            fold = -0.75F;
         } else if (airborne && vyU > 1.2F && this.jumpT < 0.3F) {
            swing = L.row == 0 ? 0.45F : -1.0F;
            lift = 0.12F * L.sign;
            fold = L.row == 0 ? -0.5F : 0.15F;
         } else if (airborne && approach) {
            float[] v = LANDING_LEGS[L.row][L.sign > 0.0F ? 0 : 1];
            swing = v[0];
            lift = v[1];
            fold = v[2];
         } else if (airborne) {
            float[] v = FLIGHT_LEGS[L.row][L.sign > 0.0F ? 0 : 1];
            swing = v[0];
            lift = v[1];
            fold = v[2];
         } else if (moving) {
            float ph = this.gaitT + (L.grp != 0 ? (float) Math.PI : 0.0F);
            float amp = 0.7F + 0.3F * Math.min(2.0F, this.legDrive[L.row * 2 + (L.sign > 0.0F ? 1 : 0)]);
            swing = (float)Math.sin(ph) * 0.28F * amp;
            lift = Math.max(0.0F, (float)Math.sin(ph)) * 0.3F * L.sign * amp;
         } else if (phase == 0 && L.row == 0) {
            swing = 0.55F + rub * 0.16F * L.sign;
            lift = 0.55F * L.sign;
            fold = -0.8F;
         } else if (phase == 1 && L.row == 2) {
            swing = -0.3F + rub * 0.15F;
            lift = 0.0F;
            fold = -1.3F;
            rollA = 1.0F + rub * 0.22F;
         } else if (phase == 2 && L.row == 2) {
            swing = -0.5F;
            lift = 0.0F;
            fold = -1.0F;
            rollA = 0.6F + rub * 0.18F * L.sign;
         } else if (sleeping) {
            swing = L.row == 0 ? 0.12F : (L.row == 2 ? -0.12F : 0.0F);
            lift = 0.1F * L.sign;
            fold = -0.62F;
         } else {
            swing = (float)Math.sin(ts * 0.002 + L.grp * 2) * 0.04F;
            lift = 0.0F;
            float tw = this.twitch[L.row * 2 + (L.sign > 0.0F ? 1 : 0)];
            if (tw > 0.0F) {
               lift = (float)Math.sin(tw / 0.35F * Math.PI) * 0.22F * L.sign;
               swing += (float)Math.sin(tw / 0.35F * Math.PI) * 0.08F;
            }
         }

         _qa.identity().rotateY(swing);
         _qb.identity().rotateZ(lift);
         _qc.identity().rotateX(rollA);
         _qa.mul(_qb).mul(_qc).mul(this.rig.bones[L.root - 1].rest);
         this.pose[L.root].slerp(_qa, kL);
         float fk = fold;

         for (int k : L.kids) {
            _qb.identity().rotateX(fk).mul(this.rig.bones[k - 1].rest);
            this.pose[k].slerp(_qb, kL);
            fk *= 0.7F;
         }
      }

      boolean inFlight = (airborne || flapping) && !crouch;
      this.inFlight = inFlight;
      float groomWing = phase == 1 ? 0.26F + rub * 0.05F : 0.0F;
      float beatP = (float)(t * 8.0 % 1.0);

      for (FlyRig.Wing W : this.rig.wings) {
         if (singing && W.left) {
            _qa.identity().rotateZ((1.3F + (float)Math.sin(ts * 0.05) * 0.15F) * W.sign);
         } else if (singing) {
            _qa.identity().rotateZ(0.05F * W.sign);
         } else if (inFlight) {
            wingAt(beatP, this.wzx);
            _qa.identity().rotateZ(this.wzx[0] * W.sign);
            _qb.identity().rotateX(this.wzx[1]);
            _qa.mul(_qb);
         } else {
            _qa.identity().rotateZ(((float)Math.sin(ts * 0.002) * 0.04F + groomWing) * W.sign);
            if (this.prepT > 0.0F) {
               _qb.identity().rotateX(0.85F);
               _qa.mul(_qb);
            }
         }

         _qa.mul(this.rig.bones[W.root - 1].rest);
         this.pose[W.root].slerp(_qa, phase >= 0 ? Math.min(1.0F, fdt * 6.0F) : 1.0F);

         for (int g = 0; g < 2; g++) {
            Quaternionf[] gp = g == 0 ? this.poseGhostA : this.poseGhostB;
            if (inFlight && !singing) {
               float gphase = (beatP + (g == 0 ? -0.11F : 0.11F) + 1.0F) % 1.0F;
               wingAt(gphase, this.wzx);
               _qa.identity().rotateZ(this.wzx[0] * W.sign);
               _qb.identity().rotateX(this.wzx[1]);
               _qa.mul(_qb).mul(this.rig.bones[W.root - 1].rest);
               gp[W.root].set(_qa);
            } else {
               gp[W.root].set(this.pose[W.root]);
            }
         }
      }
   }
}

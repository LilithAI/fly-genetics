package io.flyproject.connectomefly.entity;

import io.flyproject.connectomefly.ConnectomeFlyMod;
import io.flyproject.connectomefly.lab.ArenaBuilder;
import io.flyproject.connectomefly.lab.LoomStimulus;
import io.flyproject.connectomefly.neural.EscapeBrain;
import io.flyproject.connectomefly.neural.LoomingMath;
import io.flyproject.connectomefly.neural.WholeBrain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1282;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1314;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_1676;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_1847;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_239;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_3218;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_4048;
import net.minecraft.class_4050;
import net.minecraft.class_5134;
import net.minecraft.class_5328;
import net.minecraft.class_8111;
import net.minecraft.class_1297.class_5529;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_2350.class_2351;
import net.minecraft.class_2350.class_2353;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import net.minecraft.class_5132.class_5133;

public class ConnectomeFlyEntity extends class_1314 {
   private static final class_2940<String> SPEECH = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13326);
   private static final class_2940<Byte> BODY = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13319);
   private static final class_2940<Byte> SEX = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13319);
   private static final class_2940<Byte> ANIM = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13319);
   private static final class_2940<String> HUD = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13326);
   private static final class_2940<Byte> SURFACE = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13319);
   private static final class_2940<Float> THIRST = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13320);
   private static final class_2940<Integer> NEURO_LEGS = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13327);
   private static final class_2940<Integer> NEURO_FLIGHT = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13327);
   private static final class_2940<Float> HUNGER = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13320);
   public static final float MAX_HUNGER = 20.0F;
   private static final class_2940<Integer> GROWTH = class_2945.method_12791(ConnectomeFlyEntity.class, class_2943.field_13327);
   public static final int GROW_TICKS = 108000;
   public static volatile int maxBrains = Integer.getInteger(
      "connectomefly.maxBrains", Math.max(2, Math.min(16, Runtime.getRuntime().availableProcessors() / 2))
   );
   public static final byte BODY_MODEL = 0;
   public static final byte BODY_BEE = 1;
   public static volatile byte defaultBody = 0;
   private static final double SENSE_RANGE = 24.0;
   private static final double STEERING_RANGE = 20.0;
   private static final double WALL_MAX_RANGE = 4.0;
   private static final int SPEECH_SHOW_TICKS = 80;
   private static final int SPEECH_GLOBAL_COOLDOWN = 60;
   private static final Map<String, Integer> SPEECH_CATEGORY_COOLDOWN = Map.of("jump_threat", 160, "flee", 280);
   private static final Map<String, String[]> SPEECH_LINES = Map.of(
      "jump_threat",
      new String[]{"a %s! RUN!", "%s incoming: go go go!", "too close! MOVE!"},
      "flee",
      new String[]{"that %s is way too close...", "backing off from the %s"}
   );
   private final Random speechRandom = new Random();
   private EscapeBrain brain;
   private WholeBrain whole;
   private final FlyMind mind = new FlyMind();
   private float[] chanSmooth;
   private boolean mated;
   private double cruiseSmooth;
   private int knockbackTicks;
   private int flapTicks;
   private boolean flying;
   private int flightTicks;
   private int prepTicks = -1;
   private int landTicks;
   private boolean wasAirborne;
   private boolean animFlap;
   private boolean animGroom;
   private boolean animSleep;
   private boolean animFeed;
   private boolean animSing;
   private double hoverPhase;
   private double climb;
   private boolean noGround;
   private double[] perchTarget;
   private int perchCooldown;
   private int prevThreatId = -1;
   private EscapeBrain.Sensed lastSensed;
   private double prevThreatTheta;
   private double prevWallTheta = Double.NaN;
   private int speechTicksLeft;
   private long lastSpeechTick = Long.MIN_VALUE;
   private final HashMap<String, Long> lastCategoryTick = new HashMap<>();
   private static long lastDisclosureTick = Long.MIN_VALUE;
   static final boolean PERCH_DEBUG = Boolean.getBoolean("connectomefly.perchdebug");
   private int eggModelCd;
   private double courtedT;
   private double matingT;
   private double eggDueT;
   private double courtCd;
   private int songTick;
   public ConnectomeFlyEntity mate;
   private ConnectomeFlyEntity suitor;
   private int suitorTick;
   private int starveTick;
   private long lastTtmnTick = Long.MIN_VALUE;
   private int lastSensorySpikes;
   private int lastDnSpikes;
   private boolean descending;
   private boolean dragged;
   private static final double NO_GROUND = -1000000.0;

   public boolean hasMated() {
      return this.mated;
   }

   public boolean noGroundBelow() {
      return this.noGround;
   }

   private double[] nearestFace() {
      double best = this.method_23318() - this.groundBelow();
      double[] out = new double[]{this.method_23317(), this.groundBelow(), this.method_23321(), -1.0};
      if (this.perchCooldown > 0) {
         return out;
      } else {
         class_2339 m = new class_2339();

         for (int up = 1; up <= 1; up++) {
            m.method_10103(
               (int)Math.floor(this.method_23317()), (int)Math.floor(this.method_23318() + this.method_17682()) + up, (int)Math.floor(this.method_23321())
            );
            class_2680 st = this.method_37908().method_8320(m);
            if (!st.method_26215() && !st.method_26220(this.method_37908(), m).method_1110() && st.method_26227().method_15769()) {
               double d = m.method_10264() - (this.method_23318() + this.method_17682());
               if (d < best) {
                  best = d;
                  out = new double[]{
                     this.method_23317(), m.method_10264() - this.method_17682() - 0.02, this.method_23321(), class_2350.field_11036.method_10146()
                  };
               }
               break;
            }
         }

         for (class_2350 d : class_2353.field_11062) {
            for (int k = 1; k <= 1; k++) {
               m.method_10103(
                  (int)Math.floor(this.method_23317() + d.method_10148() * k),
                  (int)Math.floor(this.method_23318() + this.method_17682() / 2.0F),
                  (int)Math.floor(this.method_23321() + d.method_10165() * k)
               );
               class_2680 st = this.method_37908().method_8320(m);
               if (!st.method_26215() && !st.method_26220(this.method_37908(), m).method_1110() && st.method_26227().method_15769()) {
                  if (st.method_26206(this.method_37908(), m, d.method_10153())) {
                     double fx = d == class_2350.field_11034
                        ? m.method_10263() - this.method_17681() / 2.0F - 0.02
                        : (d == class_2350.field_11039 ? m.method_10263() + 1 + this.method_17681() / 2.0F + 0.02 : this.method_23317());
                     double fz = d == class_2350.field_11035
                        ? m.method_10260() - this.method_17681() / 2.0F - 0.02
                        : (d == class_2350.field_11043 ? m.method_10260() + 1 + this.method_17681() / 2.0F + 0.02 : this.method_23321());
                     double dist = Math.hypot(fx - this.method_23317(), fz - this.method_23321());
                     if (dist < best) {
                        best = dist;
                        out = new double[]{fx, this.method_23318(), fz, d.method_10146()};
                     }
                  }
                  break;
               }
            }
         }

         return out;
      }
   }

   public ConnectomeFlyEntity(class_1299<? extends class_1314> entityType, class_1937 world) {
      super(entityType, world);
      this.method_49477(0.5F);
   }

   public static class_5133 createFlyAttributes() {
      return class_1308.method_26828().method_26868(class_5134.field_23716, 10.0).method_26868(class_5134.field_23719, 0.3);
   }

   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(SPEECH, "");
      this.field_6011.method_12784(BODY, defaultBody);
      this.field_6011.method_12784(SEX, (byte)0);
      this.field_6011.method_12784(ANIM, (byte)0);
      this.field_6011.method_12784(HUD, "");
      this.field_6011.method_12784(HUNGER, 20.0F);
      this.field_6011.method_12784(GROWTH, 108000);
      this.field_6011.method_12784(NEURO_LEGS, 0);
      this.field_6011.method_12784(NEURO_FLIGHT, 0);
      this.field_6011.method_12784(SURFACE, (byte)-1);
      this.field_6011.method_12784(THIRST, 0.0F);
   }

   public class_2350 surface() {
      byte s = (Byte)this.field_6011.method_12789(SURFACE);
      return s < 0 ? null : class_2350.method_10143(s);
   }

   public boolean isPerched() {
      return (Byte)this.field_6011.method_12789(SURFACE) >= 0;
   }

   public boolean isStanding() {
      return this.method_24828() || this.isPerched();
   }

   public float getThirst() {
      return (Float)this.field_6011.method_12789(THIRST);
   }

   public void setThirst(float t) {
      this.field_6011.method_12778(THIRST, class_3532.method_15363(t, 0.0F, 20.0F));
   }

   private void setSurface(class_2350 d) {
      this.field_6011.method_12778(SURFACE, (byte)(d == null ? -1 : d.method_10146()));
   }

   private class_2350 faceWithin(double reach, class_2350 only) {
      class_2350[] dirs = only != null
         ? new class_2350[]{only}
         : new class_2350[]{class_2350.field_11036, class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039};
      double cy = this.method_23318() + this.method_17682() / 2.0F;

      for (class_2350 d : dirs) {
         double px = this.method_23317() + d.method_10148() * (this.method_17681() / 2.0F + reach);
         double py = cy + d.method_10164() * (this.method_17682() / 2.0F + reach);
         double pz = this.method_23321() + d.method_10165() * (this.method_17681() / 2.0F + reach);
         class_2338 bp = class_2338.method_49637(px, py, pz);
         class_2680 st = this.method_37908().method_8320(bp);
         if (!st.method_26215()
            && !st.method_26220(this.method_37908(), bp).method_1110()
            && st.method_26227().method_15769()
            && st.method_26206(this.method_37908(), bp, d.method_10153())) {
            return d;
         }
      }

      return null;
   }

   void perch(class_2350 d) {
      if (PERCH_DEBUG) {
         System.out.printf(Locale.ROOT, "[perch] grab %s at y=%.2f (x %.2f z %.2f)%n", d, this.method_23318(), this.method_23317(), this.method_23321());
      }

      this.flying = false;
      this.descending = false;
      this.climb = 0.0;
      this.method_5875(true);
      this.method_18800(0.0, 0.0, 0.0);
      this.field_6037 = true;
      this.setSurface(d);
      this.pressToSurface();
      this.landTicks = 5;
      this.method_5783(class_3417.field_20607, 0.15F, 1.9F);
   }

   private void pressToSurface() {
      class_2350 d = this.surface();
      if (d != null) {
         double cy = this.method_23318() + this.method_17682() / 2.0F;
         class_2338 bp = null;

         for (double reach = 0.05; reach <= 0.75; reach += 0.1) {
            class_2338 cand = class_2338.method_49637(
               this.method_23317() + d.method_10148() * (this.method_17681() / 2.0F + reach),
               cy + d.method_10164() * (this.method_17682() / 2.0F + reach),
               this.method_23321() + d.method_10165() * (this.method_17681() / 2.0F + reach)
            );
            class_2680 st = this.method_37908().method_8320(cand);
            if (!st.method_26215() && !st.method_26220(this.method_37908(), cand).method_1110() && st.method_26227().method_15769()) {
               bp = cand;
               break;
            }
         }

         if (bp != null) {
            double eps = 0.02;
            switch (d) {
               case field_11036:
                  this.method_5814(this.method_23317(), bp.method_10264() - this.method_17682() - eps, this.method_23321());
                  break;
               case field_11043:
                  this.method_5814(this.method_23317(), this.method_23318(), bp.method_10260() + 1 + this.method_17681() / 2.0F + eps);
                  break;
               case field_11035:
                  this.method_5814(this.method_23317(), this.method_23318(), bp.method_10260() - this.method_17681() / 2.0F - eps);
                  break;
               case field_11039:
                  this.method_5814(bp.method_10263() + 1 + this.method_17681() / 2.0F + eps, this.method_23318(), this.method_23321());
                  break;
               case field_11034:
                  this.method_5814(bp.method_10263() - this.method_17681() / 2.0F - eps, this.method_23318(), this.method_23321());
            }
         }
      }
   }

   void unperch(boolean intoFlight) {
      class_2350 d = this.surface();
      this.perchCooldown = 80;
      if (PERCH_DEBUG) {
         System.out.printf(Locale.ROOT, "[perch] let go of %s at y=%.2f intoFlight=%s flying=%s%n", d, this.method_23318(), intoFlight, this.flying);
      }

      this.setSurface(null);
      if (intoFlight) {
         this.flying = true;
         this.descending = false;
         this.flapTicks = 10;
         if (d != null) {
            this.method_18800(-d.method_10148() * 0.12, d == class_2350.field_11036 ? -0.05 : 0.1, -d.method_10165() * 0.12);
            this.field_6037 = true;
         }
      } else {
         this.method_5875(false);
      }
   }

   public class_243 surfaceForward() {
      return surfaceForward(this.surface(), this.method_36454());
   }

   public static class_243 surfaceForward(class_2350 d, float yaw) {
      double yawRad = Math.toRadians(yaw);
      double fx = -Math.sin(yawRad);
      double fz = Math.cos(yawRad);
      if (d != null && d != class_2350.field_11036) {
         class_243 along = new class_243(-d.method_10165(), 0.0, d.method_10148());
         return along.method_1021(fx).method_1031(0.0, fz, 0.0);
      } else {
         return new class_243(fx, 0.0, fz);
      }
   }

   private void surfaceTick() {
      if (this.perchCooldown > 0) {
         this.perchCooldown--;
      }

      class_2350 d = this.surface();
      if (d != null) {
         this.method_5875(true);
         this.field_6017 = 0.0F;
         if (this.flying || this.prepTicks >= 0) {
            this.unperch(true);
         } else if (this.faceWithin(0.45, d) != null) {
            this.pressToSurface();
         } else {
            if (d != class_2350.field_11036 && this.method_23318() - this.groundBelow() < 0.15) {
               this.unperch(false);
            } else if (d != class_2350.field_11036 && this.faceWithin(0.35, class_2350.field_11036) != null) {
               this.setSurface(class_2350.field_11036);
               this.pressToSurface();
            } else {
               this.unperch(true);
            }
         }
      }
   }

   public int getNeuroLegs() {
      return (Integer)this.field_6011.method_12789(NEURO_LEGS);
   }

   public int getNeuroFlight() {
      return (Integer)this.field_6011.method_12789(NEURO_FLIGHT);
   }

   void setNeuro(int legs, int flight) {
      if ((Integer)this.field_6011.method_12789(NEURO_LEGS) != legs) {
         this.field_6011.method_12778(NEURO_LEGS, legs);
      }

      if ((Integer)this.field_6011.method_12789(NEURO_FLIGHT) != flight) {
         this.field_6011.method_12778(NEURO_FLIGHT, flight);
      }
   }

   public FlyMind mind() {
      return this.mind;
   }

   public int getGrowth() {
      return (Integer)this.field_6011.method_12789(GROWTH);
   }

   public void setGrowth(int g) {
      this.field_6011.method_12778(GROWTH, class_3532.method_15340(g, 0, 108000));
      this.method_18382();
   }

   public float method_17825() {
      return 0.5F + 0.5F * (this.getGrowth() / 108000.0F);
   }

   public boolean isBabyFly() {
      return this.getGrowth() < 108000;
   }

   public class_4048 method_18377(class_4050 pose) {
      return super.method_18377(pose).method_18383(this.method_17825());
   }

   public boolean isMating() {
      return this.matingT > 0.0;
   }

   public double courtedSeconds() {
      return this.courtedT;
   }

   public boolean courtRefractory() {
      return this.courtCd > 0.0;
   }

   public void resetCourtship() {
      this.courtCd = 0.0;
      this.courtedT = 0.0;
      this.matingT = 0.0;
      this.eggDueT = 0.0;
      this.mate = null;
   }

   public void sungTo(ConnectomeFlyEntity male, double dt) {
      if (!this.isMale() && !this.isBabyFly() && !male.isBabyFly() && !(this.matingT > 0.0) && !(this.courtCd > 0.0)) {
         this.courtedT += dt * 2.0;
         this.suitor = male;
         this.suitorTick = this.field_6012;
      }
   }

   public ConnectomeFlyEntity suitor() {
      return this.suitor != null && this.suitor.method_5805() && this.field_6012 - this.suitorTick < 10 ? this.suitor : null;
   }

   public void acceptMate(ConnectomeFlyEntity male) {
      if (!this.isMale() && !(this.matingT > 0.0) && !(this.courtCd > 0.0) && male != null && male.method_5805()) {
         this.courtedT = 0.0;
         this.matingT = 10.0;
         male.matingT = 10.0;
         this.mate = male;
         male.mate = this;
         this.sayNow("[brain] her pC1 cells say yes: she accepts him");
         male.sayNow("mating");
      }
   }

   public void songTick() {
      this.songTick = (this.songTick + 1) % 12;
      if (this.songTick < 8 && this.songTick % 2 == 0) {
         float pitch = 1.7F + 0.1F * (this.songTick / 2);
         this.method_37908()
            .method_43128(
               null,
               this.method_23317(),
               this.method_23318(),
               this.method_23321(),
               (class_3414)class_3417.field_15114.comp_349(),
               class_3419.field_15254,
               0.45F,
               pitch
            );
      }
   }

   private void matingTick() {
      if (this.courtCd > 0.0) {
         this.courtCd -= 0.05;
      }

      if (this.courtedT > 0.0) {
         this.courtedT = Math.max(0.0, this.courtedT - 0.05);
      }

      if (this.matingT > 0.0) {
         this.matingT -= 0.05;
         if (this.mate != null && this.mate.method_5805()) {
            if (this.isMale()) {
               double yawRad = Math.toRadians(this.mate.method_36454());
               double tx = this.mate.method_23317() + Math.sin(yawRad) * 0.35;
               double tz = this.mate.method_23321() - Math.cos(yawRad) * 0.35;
               this.method_18800((tx - this.method_23317()) * 0.3, this.method_18798().field_1351, (tz - this.method_23321()) * 0.3);
               this.method_36456(this.mate.method_36454());
               this.field_6283 = this.mate.method_36454();
            } else {
               this.method_18800(0.0, this.method_18798().field_1351, 0.0);
            }

            this.field_6037 = true;
            if (this.field_6012 % 20 == 0) {
               ((class_3218)this.method_37908())
                  .method_14199(class_2398.field_11201, this.method_23317(), this.method_23318() + 0.5, this.method_23321(), 1, 0.15, 0.1, 0.15, 0.0);
            }
         }

         if (this.matingT <= 0.0) {
            if (!this.isMale()) {
               this.eggDueT = 1.5;
               this.mated = true;
               this.courtCd = 25.0;
               this.sayNow("mated: an egg is on its way (oviposition)");
            } else {
               this.courtCd = 12.0;
            }

            this.mate = null;
         }
      }

      if (this.eggDueT > 0.0) {
         this.eggDueT -= 0.05;
         if (this.eggDueT <= 0.0) {
            this.layEgg("[model] lays a fertilized egg: it hatches in 30 s");
         }
      }
   }

   public void layEgg(String why) {
      this.layEgg(why, true);
   }

   public void layEgg(String why, boolean fertile) {
      if (!this.method_37908().field_9236 && !this.isMale() && !this.isBabyFly()) {
         class_3218 world = (class_3218)this.method_37908();
         FlyEggEntity egg = (FlyEggEntity)ConnectomeFlyMod.FLY_EGG.method_5883(world);
         if (egg != null) {
            double yawRad = Math.toRadians(this.method_36454());
            egg.setFertile(fertile);
            egg.method_5808(this.method_23317() + Math.sin(yawRad) * 0.25, this.method_23318(), this.method_23321() - Math.cos(yawRad) * 0.25, 0.0F, 0.0F);
            world.method_8649(egg);
            world.method_14199(class_2398.field_11207, this.method_23317(), this.method_23318() + 0.1, this.method_23321(), 4, 0.1, 0.05, 0.1, 0.005);
            this.method_5783(class_3417.field_14634, 0.5F, 1.7F);
            this.sayNow(why);
         }
      }
   }

   private void growthTick() {
      int g = this.getGrowth();
      if (g < 108000) {
         if (this.field_6012 % 20 == 0) {
            this.setGrowth(g + 20);
         }
      }
   }

   public float getHunger() {
      return (Float)this.field_6011.method_12789(HUNGER);
   }

   public void setHunger(float h) {
      this.field_6011.method_12778(HUNGER, class_3532.method_15363(h, 0.0F, 20.0F));
   }

   public void eat(float hunger, float heal) {
      this.setHunger(this.getHunger() + hunger);
      if (heal > 0.0F) {
         this.method_6025(heal);
      }
   }

   private void thirstTick() {
      if (!this.isMale()) {
         float rate = 0.0016666667F * (this.flying ? 2.0F : (this.mind.onDryGround() ? 1.8F : 1.0F));
         this.setThirst(this.getThirst() + rate);
      }
   }

   public void drink(float amount) {
      this.setThirst(this.getThirst() - amount);
   }

   private void hungerTick() {
      if (!this.isMale()) {
         float h = this.getHunger();
         float drain = 0.0020833334F * (this.flying ? 2.0F : (this.method_18798().method_37267() > 0.02 ? 1.3F : 1.0F));
         h -= drain;
         this.setHunger(h);
         if (h >= 19.0F && this.method_6032() < this.method_6063() && this.field_6012 % 20 == 0) {
            this.method_6025(0.5F);
         } else if (h >= 12.0F && this.method_6032() < this.method_6063() && this.field_6012 % 60 == 0) {
            this.method_6025(0.25F);
         } else if (h <= 0.0F && ++this.starveTick >= 80 && this.method_6032() > 1.0F) {
            this.starveTick = 0;
            this.method_5643(this.method_37908().method_48963().method_48825(), 0.5F);
         }
      }
   }

   protected class_1269 method_5992(class_1657 player, class_1268 hand) {
      class_1799 stack = player.method_5998(hand);
      if (!this.isMale() && FlyMind.isHandFood(stack)) {
         if (!this.method_37908().field_9236) {
            this.eat(6.0F, 1.5F);
            this.method_5783(class_3417.field_20614, 0.6F, 1.7F);
            this.animFeed = true;
            if (!player.method_31549().field_7477) {
               stack.method_7934(1);
            }

            ((class_3218)this.method_37908())
               .method_14199(class_2398.field_11211, this.method_23317(), this.method_23318() + 0.4, this.method_23321(), 6, 0.25, 0.2, 0.25, 0.02);
            this.method_5783(class_3417.field_20614, 0.5F, 1.8F);
            this.sayNow("eats from your hand: proboscis extension (Gr5a)");
         }

         return class_1269.method_29236(this.method_37908().field_9236);
      } else if (!this.isMale() && stack.method_31574(class_1802.field_8574) && class_1844.method_8063(stack) == class_1847.field_8991) {
         if (!this.method_37908().field_9236) {
            this.drink(8.0F);
            this.animFeed = true;
            this.method_5783(class_3417.field_20613, 0.6F, 1.7F);
            if (!player.method_31549().field_7477) {
               player.method_6122(hand, class_5328.method_30012(stack, player, new class_1799(class_1802.field_8469)));
            }

            ((class_3218)this.method_37908())
               .method_14199(class_2398.field_11202, this.method_23317(), this.method_23318() + 0.3, this.method_23321(), 6, 0.2, 0.1, 0.2, 0.02);
            this.sayNow("drinks from your bottle: ppk28 water taste neurons, proboscis out");
         }

         return class_1269.method_29236(this.method_37908().field_9236);
      } else {
         return super.method_5992(player, hand);
      }
   }

   public String getHud() {
      return (String)this.field_6011.method_12789(HUD);
   }

   private void hudTick() {
      if (this.field_6012 % 4 == 0) {
         StringBuilder sb = new StringBuilder(320);
         String body = this.dragged
            ? "on a lead (being walked)"
            : (
               this.matingT > 0.0
                  ? "mating"
                  : (
                     this.isPerched()
                        ? (this.surface() == class_2350.field_11036 ? "hanging from the ceiling" : "on the wall")
                           + (this.method_18798().method_1033() > 0.02 ? ", walking" : "")
                        : (
                           this.prepTicks >= 0
                              ? "take-off crouch"
                              : (
                                 this.landTicks > 0
                                    ? "landing"
                                    : (
                                       this.flying
                                          ? "hovering"
                                          : (
                                             this.animSing
                                                ? "singing"
                                                : (
                                                   this.animSleep
                                                      ? "asleep"
                                                      : (
                                                         this.animGroom
                                                            ? "grooming"
                                                            : (this.animFeed ? "feeding" : (this.method_18798().method_37267() > 0.02 ? "walking" : "standing"))
                                                      )
                                                )
                                          )
                                    )
                              )
                        )
                  )
            );
         sb.append("body=").append(body).append(';');
         String label = this.mind.brainLabelT > 0.0 ? this.mind.brainLabel : this.mind.label;
         sb.append("doing=").append(label == null ? "" : label.replace(';', ',')).append(';');
         ArenaBuilder.Arena box = ArenaBuilder.arenaAt((class_3218)this.method_37908(), this.method_24515());
         if (box != null) {
            String[] spec = ArenaBuilder.SENSES.get(box.id());
            Set<FlyMind.Kind> iso = ArenaBuilder.ISOLATION.get(box.id());
            sb.append("box=")
               .append(spec == null ? box.id() : spec[0])
               .append(" · ")
               .append(
                  iso == null
                     ? "every sense live"
                     : (iso.isEmpty() ? "no props needed" : "live: " + iso.toString().toLowerCase().replace("[", "").replace("]", ""))
               )
               .append(';');
         }

         WholeBrain.Loco l = this.whole == null ? null : this.whole.loco();
         if (this.whole == null && !this.isMale()) {
            sb.append("brain=waiting (").append(maxBrains).append(" brains already running);");
         }

         if (l != null && this.whole.dataOrNull() != null) {
            sb.append(
               String.format(Locale.ROOT, "brain=%.1f;dn=L %.2f  R %.2f;turn=%+.2f;walk=%.1f;", l.activity, l.dnLeft, l.dnRight, l.turnBias, l.walkDrive)
            );
            String[] names = this.whole.dataOrNull().chanNames;
            List<int[]> top = new ArrayList<>();
            int[][] cidx = this.whole.dataOrNull().chanIdx;

            for (int c = 0; c < names.length && c < l.chanSpikes.length && c < cidx.length; c++) {
               float expected = Math.max(0.002F, cidx[c].length * 0.001F);
               if (this.chanSmooth == null || this.chanSmooth.length != l.chanSpikes.length) {
                  this.chanSmooth = (float[])l.chanSpikes.clone();
               }

               this.chanSmooth[c] = this.chanSmooth[c] + (l.chanSpikes[c] - this.chanSmooth[c]) * 0.25F;
               float ratio = this.chanSmooth[c] / expected;
               if (ratio >= 2.5F) {
                  top.add(new int[]{c, Math.round(ratio * 100.0F)});
               }
            }

            top.sort((a, b) -> Integer.compare(b[1], a[1]));
            StringBuilder cs = new StringBuilder();

            for (int i = 0; i < top.size() && i < 4; i++) {
               if (i > 0) {
                  cs.append(" · ");
               }

               cs.append(names[((int[])top.get(i))[0]]).append(" x").append(String.format(Locale.ROOT, "%.0f", top.get(i)[1] / 100.0));
            }

            sb.append("chans=").append((CharSequence)cs).append(';');
         }

         boolean fired = this.lastTtmnTick != Long.MIN_VALUE && this.method_37908().method_8510() - this.lastTtmnTick < 40L;
         sb.append("esc=")
            .append(String.format(Locale.ROOT, "LC4/LPLC %d · DN %d · TTMn %s", this.lastSensorySpikes, this.lastDnSpikes, fired ? "FIRED" : "silent"))
            .append(';');
         sb.append("outs=").append(this.mind.brainOuts.replace(';', ',')).append(';');
         sb.append(this.mind.hudExtra());
         sb.append("hp=").append(String.format(Locale.ROOT, "%.1f", this.method_6032())).append(';');
         sb.append("hunger=").append(String.format(Locale.ROOT, "%.1f", this.getHunger())).append(';');
         sb.append("thirst=").append(String.format(Locale.ROOT, "%.1f", this.getThirst())).append(';');
         String s = sb.toString();
         if (!s.equals(this.field_6011.method_12789(HUD))) {
            this.field_6011.method_12778(HUD, s);
         }
      }
   }

   protected void method_5959() {
   }

   public String getSpeech() {
      return (String)this.field_6011.method_12789(SPEECH);
   }

   public byte getBodyMode() {
      return (Byte)this.field_6011.method_12789(BODY);
   }

   public void setBodyMode(byte mode) {
      this.field_6011.method_12778(BODY, mode);
   }

   public boolean isMale() {
      return (Byte)this.field_6011.method_12789(SEX) != 0;
   }

   public void setMale(boolean male) {
      this.field_6011.method_12778(SEX, (byte)(male ? 1 : 0));
   }

   public int animFlags() {
      return (Byte)this.field_6011.method_12789(ANIM);
   }

   public boolean isFlapping() {
      return (this.animFlags() & 1) != 0;
   }

   public boolean isGrooming() {
      return (this.animFlags() & 2) != 0;
   }

   public boolean method_6113() {
      return (this.animFlags() & 4) != 0;
   }

   void setAnimFlags(boolean flapping, boolean grooming, boolean sleeping, boolean feeding) {
      if (flapping) {
         this.flapTicks = 6;
      }

      this.animFlap = flapping;
      this.animGroom = grooming;
      this.animSleep = sleeping;
      this.animFeed = feeding;
   }

   void setSinging(boolean singing) {
      this.animSing = singing;
   }

   void requestFlight(boolean fly) {
      if (this.matingT > 0.0) {
         fly = false;
      }

      if (fly) {
         this.descending = false;
         this.perchTarget = null;
         if (this.isPerched()) {
            this.unperch(true);
         } else if (!this.flying && this.method_24828() && this.prepTicks < 0) {
            this.prepTicks = 2;
         } else if (!this.flying && !this.method_24828()) {
            this.flying = true;
         }
      } else if (this.flightTicks <= 0 && this.flying) {
         this.descending = true;
      }
   }

   public boolean perchNearby() {
      if (this.flying && this.perchCooldown <= 0) {
         class_2350 d = this.faceWithin(0.45, null);
         if (d != null) {
            this.perch(d);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isFlying() {
      return this.flying;
   }

   public void brainJump() {
      if (this.method_24828() || this.flying) {
         this.method_18799(this.method_18798().method_1031(0.0, 0.55, 0.0));
         this.field_6037 = true;
         this.flapTicks = 12;
         this.flying = true;
         this.flightTicks = 50;
         this.lastTtmnTick = this.method_37908().method_8510();
      }
   }

   public void takeOffNow() {
      this.mind.takeOffNow();
      this.requestFlight(true);
      this.flightTicks = Math.max(this.flightTicks, 60);
   }

   public String debugState() {
      WholeBrain.Loco l = this.whole == null ? null : this.whole.loco();
      String sensedDbg = this.lastSensed == null
         ? ""
         : String.format(
            Locale.ROOT,
            " sensed[found=%s dist=%.2f loom=%.3f loomDrive=%.3f theta=%.3f wall=%.3f]",
            this.lastSensed.threatFound,
            this.lastSensed.threatDist,
            this.lastSensed.threatLoom,
            this.lastSensed.threatLoomDrive,
            this.lastSensed.threatThetaDrive,
            this.lastSensed.wallLoom
         );
      return String.format(
            "flying=%s prep=%d flightTicks=%d onGround=%s noGrav=%s anim=%d vel=%.3f desc=%s surf=%s target=%s" + sensedDbg + " | brain %s | %s",
            this.flying,
            this.prepTicks,
            this.flightTicks,
            this.method_24828(),
            this.method_5740(),
            this.animFlags(),
            this.method_18798().method_37267(),
            this.descending,
            this.surface(),
            this.perchTarget == null
               ? "-"
               : String.format(Locale.ROOT, "%.1f,%.1f,%.1f/%d", this.perchTarget[0], this.perchTarget[1], this.perchTarget[2], (int)this.perchTarget[3]),
            l == null ? "(not ready)" : String.format("walk=%.2f turn=%.2f act=%.1f", l.walkDrive, l.turnBias, l.activity),
            this.mind.state()
         )
         + " | hud: "
         + this.getHud()
         + String.format(Locale.ROOT, "pos=%.2f,%.2f,%.2f;yaw=%.0f;", this.method_23317(), this.method_23318(), this.method_23321(), this.method_36454());
   }

   private void eatSoundTick() {
      if (this.animFeed && this.field_6012 % 12 == 0) {
         this.method_5783(this.mind.isDrinking() ? class_3417.field_20613 : class_3417.field_20614, 0.55F, 1.6F + this.method_6051().method_43057() * 0.3F);
      }
   }

   public boolean isDragged() {
      return this.dragged;
   }

   public boolean method_5931(class_1657 player) {
      return !this.method_5934();
   }

   private void leashTick() {
      this.dragged = false;
      class_1297 holder = this.method_5933();
      if (holder != null && !this.method_37908().field_9236) {
         double dx = holder.method_23317() - this.method_23317();
         double dy = holder.method_23318() - this.method_23318();
         double dz = holder.method_23321() - this.method_23321();
         double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (!(d < 2.5)) {
            this.dragged = true;
            if (this.isPerched()) {
               this.unperch(true);
            }

            double k = Math.min(0.32, (d - 2.0) * 0.07) / d;
            boolean lift = dy > 0.8 || !this.method_24828() && !this.method_5799();
            if (lift) {
               this.flying = true;
               this.descending = false;
               this.method_5875(true);
               this.flapTicks = 4;
               this.method_18800(dx * k, dy * k + 0.015, dz * k);
            } else {
               this.method_18800(dx * k, this.method_18798().field_1351, dz * k);
            }

            this.field_6037 = true;
            float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
            this.method_36456(yaw);
            this.field_6283 = yaw;
            this.field_6241 = yaw;
         }
      }
   }

   private void pushAnimFlags() {
      boolean approach = this.descending || !this.flying && !this.method_24828() && this.prepTicks < 0 && this.wasAirborne;
      byte f = (byte)(
         (!this.animFlap && this.flapTicks <= 0 && !this.flying && this.method_24828() ? 0 : 1)
            | (this.animGroom ? 2 : 0)
            | (this.animSleep ? 4 : 0)
            | (this.animFeed ? 8 : 0)
            | (this.prepTicks >= 0 ? 16 : 0)
            | (this.landTicks > 0 ? 32 : 0)
            | (this.animSing ? 64 : 0)
            | (approach ? -128 : 0)
      );
      if ((Byte)this.field_6011.method_12789(ANIM) != f) {
         this.field_6011.method_12778(ANIM, f);
      }
   }

   private double groundBelow() {
      class_2339 m = new class_2339((int)Math.floor(this.method_23317()), (int)Math.floor(this.method_23318()), (int)Math.floor(this.method_23321()));

      for (int i = 0; i < 25; i++) {
         class_2680 st = this.method_37908().method_8320(m);
         if (!st.method_26227().method_15769()) {
            return -1000000.0;
         }

         if (!st.method_26220(this.method_37908(), m).method_1110()) {
            return m.method_10264() + st.method_26220(this.method_37908(), m).method_1105(class_2351.field_11052);
         }

         m.method_10100(0, -1, 0);
      }

      return -1000000.0;
   }

   private void flightTick() {
      this.noGround = this.groundBelow() == -1000000.0;
      if (this.noGround && !this.flying && this.prepTicks < 0 && !this.isMale()) {
         this.flying = true;
         this.descending = false;
         this.flapTicks = 6;
      }

      if (this.prepTicks >= 0) {
         if (this.prepTicks == 0) {
            this.flying = true;
            this.method_18799(this.method_18798().method_1031(0.0, 0.18, 0.0));
            this.field_6037 = true;
            this.flapTicks = 10;
         }

         this.prepTicks--;
      }

      if (this.flightTicks > 0) {
         this.flightTicks--;
         this.flying = true;
      }

      if (!this.flying && !this.method_24828() && this.prepTicks < 0 && this.field_6017 > 0.6F && !this.method_5799()) {
         this.flying = true;
         this.descending = true;
         this.flapTicks = 10;
      }

      this.method_5875(this.flying);
      this.field_6017 = 0.0F;
      if (!this.flying || !this.method_5799() && !this.method_5771()) {
         if (this.flying) {
            this.hoverPhase += 0.11;
            class_243 v = this.method_18798();
            if (this.descending && this.noGround) {
               this.descending = false;
            }

            double vy;
            if (this.descending) {
               vy = -0.07;
               if (this.field_6012 % 3 == 0 && this.perchNearby()) {
                  return;
               }

               if (this.field_6012 % 5 == 0) {
                  this.perchTarget = this.nearestFace();
               }

               if (this.perchTarget != null && this.perchTarget[3] >= 0.0) {
                  class_2350 pd = class_2350.method_10143((int)this.perchTarget[3]);
                  if (this.faceWithin(0.5, pd) != null) {
                     this.perch(pd);
                     this.perchTarget = null;
                     return;
                  }

                  double tx = this.perchTarget[0] - this.method_23317();
                  double ty = this.perchTarget[1] - this.method_23318();
                  double tz = this.perchTarget[2] - this.method_23321();
                  double dist = Math.sqrt(tx * tx + ty * ty + tz * tz);
                  double k = Math.min(0.12, dist) / Math.max(dist, 1.0E-6);
                  this.method_18800(tx * k, ty * k, tz * k);
                  this.field_6037 = true;
                  if (PERCH_DEBUG && this.field_6012 % 4 == 0) {
                     System.out
                        .printf(
                           Locale.ROOT,
                           "[perch] steer to %s y=%.2f -> %.2f vy=%.3f vel=%s face=%s%n",
                           pd,
                           this.method_23318(),
                           this.perchTarget[1],
                           ty * k,
                           this.method_18798(),
                           this.faceWithin(0.5, pd)
                        );
                  }

                  return;
               }

               if (this.method_23318() - this.groundBelow() < 0.1 || this.method_24828()) {
                  this.flying = false;
                  this.descending = false;
                  this.method_5875(false);
                  this.landTicks = 5;
               }
            } else {
               if (this.field_5976 && !this.mind.fleeing() && this.perchNearby()) {
                  return;
               }

               if (this.field_5976) {
                  this.climb = Math.min(3.5, this.climb + 0.25);
               } else {
                  this.climb = Math.max(0.0, this.climb - 0.015);
               }

               double gb = this.groundBelow();
               if (this.noGround) {
                  vy = -0.035 + 0.01 * Math.sin(this.hoverPhase);
               } else {
                  this.cruiseSmooth = this.cruiseSmooth + (this.mind.cruiseAlt() - this.cruiseSmooth) * 0.03;
                  double target = gb + 1.15 + this.climb + this.cruiseSmooth + 0.12 * Math.sin(this.hoverPhase);
                  vy = class_3532.method_15350((target - this.method_23318()) * 0.18, -0.14, 0.2);
               }
            }

            this.method_18800(v.field_1352, vy, v.field_1350);
            this.field_6037 = true;
            if (this.field_6012 % 24 == 0) {
               this.method_37908()
                  .method_43128(
                     null, this.method_23317(), this.method_23318(), this.method_23321(), class_3417.field_20605, class_3419.field_15254, 0.25F, 1.7F
                  );
            }
         }
      } else {
         this.descending = false;
         class_2339 m = new class_2339((int)Math.floor(this.method_23317()), (int)Math.floor(this.method_23318()), (int)Math.floor(this.method_23321()));

         for (int up = 0; up < 12 && !this.method_37908().method_8316(m).method_15769(); up++) {
            m.method_10100(0, 1, 0);
         }

         class_243 vx = this.method_18798();
         this.method_18800(vx.field_1352 * 0.9, 0.22, vx.field_1350 * 0.9);
         this.field_6037 = true;
         this.flapTicks = 6;
         if (this.field_6012 % 10 == 0) {
            this.method_37908()
               .method_43128(null, this.method_23317(), this.method_23318(), this.method_23321(), class_3417.field_14737, class_3419.field_15254, 0.3F, 1.8F);
         }
      }

      boolean airborne = !this.method_24828();
      if (!airborne && this.wasAirborne && !this.flying) {
         this.landTicks = 5;
      }

      this.wasAirborne = airborne;
      if (this.landTicks > 0) {
         this.landTicks--;
      }
   }

   public void method_5652(class_2487 nbt) {
      super.method_5652(nbt);
      nbt.method_10567("Body", this.getBodyMode());
      nbt.method_10556("Male", this.isMale());
      nbt.method_10548("Hunger", this.getHunger());
      nbt.method_10569("Growth", this.getGrowth());
      nbt.method_10548("Thirst", this.getThirst());
      nbt.method_10556("Mated", this.mated);
   }

   public void method_5749(class_2487 nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("Body")) {
         this.setBodyMode(nbt.method_10571("Body"));
      }

      this.setMale(nbt.method_10577("Male"));
      if (nbt.method_10545("Hunger")) {
         this.setHunger(nbt.method_10583("Hunger"));
      }

      if (nbt.method_10545("Growth")) {
         this.setGrowth(nbt.method_10550("Growth"));
      }

      this.mated = nbt.method_10577("Mated");
      if (nbt.method_10545("Thirst")) {
         this.setThirst(nbt.method_10583("Thirst"));
      }
   }

   public void method_5773() {
      super.method_5773();
      class_1937 world = this.method_37908();
      if (!world.field_9236) {
         if (this.flapTicks > 0) {
            this.flapTicks--;
         }

         if (this.brain == null) {
            this.brain = new EscapeBrain(ConnectomeFlyMod.escapeCircuitData(), ConnectomeFlyMod.steeringCircuitData());
            this.disclose();
         }

         if (this.whole == null && !this.isMale() && WholeBrain.running() < maxBrains) {
            this.whole = new WholeBrain(this.method_5845().substring(0, 8));
            this.whole.start();
         }

         if (this.whole != null) {
            this.whole.keepAlive();
         }

         EscapeBrain.Sensed sensed = this.sense();
         EscapeBrain.Intent intent = this.brain.tick(world.method_8510(), sensed);
         if (intent.jump && this.isPerched()) {
            this.unperch(true);
         }

         if (intent.jump && (this.method_24828() || this.flying)) {
            this.method_18799(this.method_18798().method_1031(0.0, 0.55, 0.0));
            this.field_6037 = true;
            this.flapTicks = 12;
            this.flying = true;
            this.flightTicks = 50;
         }

         if (intent.fleeing) {
            float newYaw = this.method_36454() + (float)intent.yawDeltaDeg;
            this.method_36456(newYaw);
            this.field_6283 = newYaw;
            this.field_6241 = newYaw;
            double yawRad = Math.toRadians(newYaw);
            double vx = -Math.sin(yawRad) * intent.stepBlocks;
            double vz = Math.cos(yawRad) * intent.stepBlocks;
            class_243 vel = this.method_18798();
            this.method_18800(vx, vel.field_1351, vz);
            this.field_6037 = true;
         }

         if (intent.speechCategory != null) {
            this.say(intent.speechCategory, intent.speechThreatName, world.method_8510());
         }

         this.lastSensorySpikes = intent.sensorySpikes;
         this.lastDnSpikes = intent.dnSpikes;
         this.lastSensed = sensed;
         if (intent.jump) {
            this.lastTtmnTick = world.method_8510();
         }

         if (this.knockbackTicks > 0) {
            this.knockbackTicks--;
         }

         this.mind.tick(this, (class_3218)world, this.whole, intent.jump || intent.fleeing || this.knockbackTicks > 0);
         this.flightTick();
         this.surfaceTick();
         this.hungerTick();
         this.thirstTick();
         this.growthTick();
         this.matingTick();
         this.eatSoundTick();
         this.pushAnimFlags();
         this.leashTick();
         this.hudTick();
         if (this.speechTicksLeft > 0 && --this.speechTicksLeft == 0) {
            this.field_6011.method_12778(SPEECH, "");
         }
      }
   }

   public void method_5650(class_5529 reason) {
      super.method_5650(reason);
      if (this.whole != null) {
         this.whole.stop();
      }
   }

   public boolean method_6049(class_1293 effect) {
      return effect.method_5579() != class_1294.field_5920 && super.method_6049(effect);
   }

   public boolean method_5643(class_1282 source, float amount) {
      if (source.method_49708(class_8111.field_42353)) {
         return false;
      } else {
         boolean spines = source.method_49708(class_8111.field_42344);
         if (spines) {
            amount = Math.min(amount, 0.5F);
         }

         boolean r = super.method_5643(source, amount);
         if (r && source.method_5529() != null) {
            this.knockbackTicks = 8;
         }

         if (r && spines) {
            this.mind.pricked();
         }

         boolean bodily = source.method_5529() != null
            || !source.method_49708(class_8111.field_42343)
               && !source.method_49708(class_8111.field_42342)
               && !source.method_49708(class_8111.field_42354)
               && !source.method_49708(class_8111.field_42337)
               && !source.method_49708(class_8111.field_42335)
               && !source.method_49708(class_8111.field_42338)
               && !source.method_49708(class_8111.field_42339)
               && !source.method_49708(class_8111.field_42349)
               && !source.method_49708(class_8111.field_42350)
               && !source.method_49708(class_8111.field_44869);
         if (r && bodily) {
            this.mind.touched();
         }

         if (r && !source.method_49708(class_8111.field_42343)) {
            this.mind.punished();
         }

         return r;
      }
   }

   public WholeBrain wholeBrain() {
      return this.whole;
   }

   private EscapeBrain.Sensed sense() {
      EscapeBrain.Sensed sensed = new EscapeBrain.Sensed();
      double tickDt = 0.05;
      List<class_1297> threats = this.method_37908().method_8333(this, this.method_5829().method_1014(24.0), ConnectomeFlyEntity::isThreat);
      class_1297 nearest = null;
      double nearestSq = Double.POSITIVE_INFINITY;

      for (class_1297 e : threats) {
         double sq = this.method_5858(e);
         if (sq < nearestSq) {
            nearestSq = sq;
            nearest = e;
         }
      }

      LoomStimulus.Loom disc = null;
      double discSq = Double.POSITIVE_INFINITY;

      for (LoomStimulus.Loom l : LoomStimulus.activeFor(this)) {
         double sq = this.method_5649(l.x, l.y, l.z);
         if (sq < discSq) {
            discSq = sq;
            disc = l;
         }
      }

      if (disc != null && discSq < nearestSq) {
         nearest = null;
         nearestSq = discSq;
      }

      if (nearest == null && disc == null) {
         this.prevThreatId = -1;
      } else {
         double dist = Math.sqrt(nearestSq);
         double radius = nearest != null ? nearest.method_17681() / 2.0 : disc.radius;
         int id = nearest != null ? nearest.method_5628() : disc.id;
         double theta = LoomingMath.angularSize(dist, radius);
         double loom = 0.0;
         if (id == this.prevThreatId) {
            loom = LoomingMath.translateLooming(this.prevThreatTheta, theta, tickDt);
         }

         this.prevThreatId = id;
         this.prevThreatTheta = theta;
         double dx = (nearest != null ? nearest.method_23317() : disc.x) - this.method_23317();
         double dz = (nearest != null ? nearest.method_23321() : disc.z) - this.method_23321();
         double dy = (nearest != null ? nearest.method_23320() : disc.y) - this.method_23320();
         double bearingAbs = Math.toDegrees(Math.atan2(-dx, dz));
         double headingDeg = this.method_36454();
         class_2350 sf = this.surface();
         if (sf != null && sf != class_2350.field_11036) {
            headingDeg = Math.toDegrees(Math.atan2(sf.method_10148(), -sf.method_10165()));
         }

         double bearing = class_3532.method_15338(bearingAbs - headingDeg);
         double elev = LoomingMath.elevationDeg(dy, Math.sqrt(dx * dx + dz * dz));
         double gain = LoomingMath.visualFieldGain3d(bearing, elev);
         double proximityGain = Math.max(0.0, 1.0 - dist / 20.0);
         sensed.threatFound = true;
         sensed.threatDist = dist;
         sensed.threatLoom = loom;
         sensed.threatLoomDrive = loom * gain;
         sensed.threatThetaDrive = theta * gain;
         sensed.threatSide = Math.max(-1.0, Math.min(1.0, bearing / 90.0)) * proximityGain;
         sensed.threatName = nearest != null ? threatName(nearest) : "the looming shape";
      }

      class_243 eye = this.method_33571();
      double yawRad = Math.toRadians(this.method_36454());
      class_243 dir = this.isPerched() ? this.surfaceForward() : new class_243(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
      class_239 hit = this.method_37908()
         .method_17742(new class_3959(eye, eye.method_1019(dir.method_1021(4.0)), class_3960.field_17558, class_242.field_1348, this));
      double wallDist = hit.method_17783() == class_240.field_1333 ? 4.0 : eye.method_1022(hit.method_17784());
      double wallTheta = LoomingMath.angularSize(wallDist, 0.5);
      if (!Double.isNaN(this.prevWallTheta)) {
         sensed.wallLoom = LoomingMath.translateLooming(this.prevWallTheta, wallTheta, tickDt);
      }

      this.prevWallTheta = wallTheta;
      return sensed;
   }

   private static boolean isThreat(class_1297 e) {
      if (!e.method_5805()) {
         return false;
      } else {
         return e instanceof class_1657 player ? !player.method_7325() : e instanceof class_1588 || e instanceof class_1676;
      }
   }

   private static String threatName(class_1297 e) {
      return e instanceof class_1657 player ? player.method_7334().getName() : e.method_5864().method_5897().getString();
   }

   private void say(String category, String threatName, long now) {
      if (now - this.lastSpeechTick >= 60L) {
         long catLast = this.lastCategoryTick.getOrDefault(category, Long.MIN_VALUE);
         if (now - catLast >= SPEECH_CATEGORY_COOLDOWN.getOrDefault(category, 160).intValue()) {
            String[] lines = SPEECH_LINES.get(category);
            if (lines != null) {
               String line = lines[this.speechRandom.nextInt(lines.length)];
               if (line.contains("%s")) {
                  line = String.format(line, threatName != null && !threatName.isEmpty() ? threatName : "something");
               }

               this.show(line, now);
               this.lastCategoryTick.put(category, now);
            }
         }
      }
   }

   void sayBehaviour(String text) {
      long now = this.method_37908().method_8510();
      if (!text.isEmpty() && now - this.lastSpeechTick >= 60L) {
         this.show(text, now);
      }
   }

   void sayNow(String text) {
      this.show(text, this.method_37908().method_8510());
   }

   private void show(String line, long now) {
      this.field_6011.method_12778(SPEECH, line);
      this.speechTicksLeft = 80;
      this.lastSpeechTick = now;
   }

   private void disclose() {
      long now = this.method_37908().method_8510();
      if (now - lastDisclosureTick >= 200L) {
         lastDisclosureTick = now;

         for (class_1657 p : this.method_37908().method_18456()) {
            if (p.method_5858(this) < 4096.0) {
               p.method_7353(
                  class_2561.method_43470(
                        "connectome-fly: her whole brain (158,262 BANC neurons) and the giant-fibre escape circuit are computed live. Speech is translated, not decoded. /flycredits for details."
                     )
                     .method_27692(class_124.field_1080),
                  false
               );
            }
         }
      }
   }

   public boolean method_5747(float fallDistance, float damageMultiplier, class_1282 damageSource) {
      return false;
   }

   protected class_3414 method_6011(class_1282 source) {
      return class_3417.field_20603;
   }

   protected class_3414 method_6002() {
      return class_3417.field_20602;
   }

   public float method_6017() {
      return 1.6F;
   }

   protected float method_6107() {
      return 0.6F;
   }

   public boolean method_5974(double distanceSquared) {
      return false;
   }
}

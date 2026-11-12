package io.flyproject.connectomefly.entity;

import io.flyproject.connectomefly.lab.ArenaBuilder;
import io.flyproject.connectomefly.lab.WindFx;
import io.flyproject.connectomefly.neural.WholeBrain;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1408;
import net.minecraft.class_1428;
import net.minecraft.class_1451;
import net.minecraft.class_1453;
import net.minecraft.class_1493;
import net.minecraft.class_1538;
import net.minecraft.class_1542;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1944;
import net.minecraft.class_1959;
import net.minecraft.class_2246;
import net.minecraft.class_2272;
import net.minecraft.class_2282;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2363;
import net.minecraft.class_238;
import net.minecraft.class_2387;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3481;
import net.minecraft.class_3486;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_3701;
import net.minecraft.class_3709;
import net.minecraft.class_3830;
import net.minecraft.class_3959;
import net.minecraft.class_3962;
import net.minecraft.class_4019;
import net.minecraft.class_4481;
import net.minecraft.class_5803;
import net.minecraft.class_7102;
import net.minecraft.class_1959.class_1963;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_2350.class_2353;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2902.class_2903;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public final class FlyMind {
   static final Map<FlyMind.Kind, FlyMind.Info> INFO = new EnumMap<>(FlyMind.Kind.class);
   static final double UNIT = 0.95;
   static final double MAX_HEALTH = 10.0;
   static final double SNOW_DPS = 0.15;
   static final double POND_GRACE = 2.0;
   static final double POND_DPS = 0.4;
   static final double RAIN_DPS = 0.35;
   static final double HEAT_DPS = 0.6;
   static final double EAT_HPS = 0.8;
   static final double EAT_BONUS = 1.5;
   static final double BRAIN_TURN = 2.4;
   static final double BRAIN_WALK = 0.5;
   static final double BRAIN_SPEED_CAP = 3.0;
   private double neuralExcess = 1.0;
   static final double FAN_PUSH = 0.45;
   static final int SCAN_EVERY = 4;
   static final int SCAN_R = 8;
   static final int SCAN_DY = 2;
   private static final int[][] SCAN_OFFSETS;
   private double moveT;
   private double homeT;
   private double pondT;
   private double smTurn;
   private double eaten;
   private boolean resting;
   private boolean grooming;
   private boolean homeReturning;
   private boolean boxRivalsOff;
   private double neuralSmooth = -1.0;
   private double neuralBase = -1.0;
   private double neuralVar = 1.0E-6;
   private int neuralN;
   private double smellFruit;
   private double smellFerment;
   private boolean fruitDominant;
   private double songHeardT;
   private double lungeCd;
   private double retreatT;
   private double painT;
   private boolean painJump;
   private boolean sated;
   private double rejectT;
   private double rejectX;
   private double rejectZ;
   private double refuseT;
   private boolean homeBySight;
   private boolean neuralMoving;
   private double forcedFlyT;
   private double groomHoldT;
   private boolean groomAfterTouch;
   private double outdoorT;
   private boolean outdoors;
   private double lureNearT;
   private double lureHabT;
   private double airRestT;
   private double lastSenseMag;
   private double[] lastWind;
   private boolean spontFly;
   private boolean dispersing;
   private double boutAlt;
   private double cruiseAlt;
   private int farScanCd;
   private class_2338 plumeSrc;
   private boolean plumeFruit;
   private double plumeStrength;
   private double flownT;
   private double groundT;
   private int detourSide;
   private boolean drinking;
   private double waterContactT;
   private double stallT;
   private double stallX;
   private double stallZ;
   private double tornT;
   private int groomPhase;
   private FlyMind.Prop feeding;
   private int scanCooldown;
   private List<FlyMind.Prop> blockProps = new ArrayList<>();
   private int busCooldown;
   private final Map<String, Float> bus = new HashMap<>();
   private double dmgAcc;
   private int dmgTick;
   private double touchT;
   private double stuckT;
   private int wallSide;
   public String label = "";
   private double scrambleT;
   private double backT;
   private double brainJumpCd;
   private double eggCd;
   private double proboscisT;
   private int scrambleSide;
   public String brainOuts = "";
   public String brainLabel = "";
   public double brainLabelT;
   private double cmpHeading = Double.NaN;
   private double cmpAmp;
   private double prevYaw = Double.NaN;
   private double hvx;
   private double hvz;
   private boolean atHome;
   private double homeStayT;
   private double excT;
   private double excYaw;
   private double sleepP;
   private boolean asleep;
   private double sleepScore;
   private double r5Rate;
   private double lnvRate;
   private static final double R5_FULL = 0.42;
   private static final double LNV_FULL = 0.117;
   private double cmpVx;
   private double cmpVz;
   private double rewardT;
   private double punishT;
   private double nutrientT;
   private double mbValence;
   private double mbRaw;
   private double mbRest;
   private double ipcAct;
   private double lcLeft;
   private double lcRight;
   private int lcCand;
   private double lcMaxAng;
   private final Map<Integer, Double> lastBearing = new HashMap<>();
   private int neuroLegs;
   private int neuroFlight;
   private String seeLabel = "";
   private float memAvoid = 1.0F;
   private float memApproach = 1.0F;
   private WholeBrain lastBrain;
   private double receptivity;
   private double joSong;
   private double aggrDrive;
   private double clockM;
   private double clockE;
   private double clockMout;
   private double clockEout;
   private boolean dryGround;
   private double drinkT;
   private double tasteSugar;
   private double tasteBitter;
   private double tasteWater;
   private boolean feedingSuppressed;
   private double socialAx;
   private double socialAz;
   private int socialN;
   private int closeN;
   private String chainDbg = "";
   private double lmBearing = Double.NaN;
   private double lmDist;
   private int lmScanT;
   private double holdYaw = Double.NaN;
   private double saccadeT;
   private double optoZ;
   private double optoZRest = Double.NaN;
   private double optoTurn;
   private double perchWishT;
   private boolean fleeingNow;
   public static volatile double[] drum;
   private float[] outSmooth;
   private WholeBrain.Loco lastLoco;
   private WholeBrain.Loco lastLocoBout;
   private static final List<long[]> RECENT_SOUNDS;
   private double navX = Double.NaN;
   private double navZ = Double.NaN;
   private int navRetry;

   public void shoved() {
      this.retreatT = 6.0;
   }

   public void pricked() {
      if (this.painT <= 0.0) {
         this.painJump = true;
      }

      this.painT = 2.5;
   }

   public double cruiseAlt() {
      return this.cruiseAlt;
   }

   static double[] wind(class_3218 world) {
      long day = world.method_8532() / 24000L;
      double h = Math.sin(day * 12.9898 + 78.233) * 43758.5453;
      double a = (h - Math.floor(h)) * Math.PI * 2.0 + 0.4 * Math.sin(world.method_8532() / 2400.0);
      return new double[]{Math.cos(a), Math.sin(a)};
   }

   private static String compass8(double x, double z) {
      String[] n = new String[]{"E", "SE", "S", "SW", "W", "NW", "N", "NE"};
      return n[(int)Math.round((Math.toDegrees(Math.atan2(z, x)) + 360.0) % 360.0 / 45.0) % 8];
   }

   private void farScan(ConnectomeFlyEntity fly, class_3218 world) {
      this.plumeSrc = null;
      class_2338 c = fly.method_24515();
      class_2339 m = new class_2339();
      long now = world.method_8510();
      double best = 1.0E9;
      double[] w = wind(world);

      for (int dx = -24; dx <= 24; dx++) {
         for (int dz = -24; dz <= 24; dz++) {
            if (Math.abs(dx) > 8 || Math.abs(dz) > 8) {
               double along = -(dx * w[0] + dz * w[1]);
               if (!(along <= 0.0)) {
                  double lat = Math.abs(-dx * w[1] + dz * w[0]);
                  if (!(lat > 1.5 + 0.3 * along)) {
                     for (int dy = -7; dy <= 2; dy++) {
                        m.method_10103(c.method_10263() + dx, c.method_10264() + dy, c.method_10260() + dz);
                        class_2680 st = world.method_8320(m);
                        if (!st.method_26215() && classify(world, st, m, now) == FlyMind.Kind.FOOD) {
                           double d = Math.hypot(dx, dz);
                           if (d < best) {
                              best = d;
                              this.plumeSrc = m.method_10062();
                              this.plumeFruit = st.method_27852(class_2246.field_10545)
                                 || st.method_27852(class_2246.field_16999)
                                 || st.method_27852(class_2246.field_28675)
                                 || st.method_27852(class_2246.field_28676)
                                 || st.method_27852(class_2246.field_10302)
                                 || st.method_27852(class_2246.field_10261);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public boolean isDrinking() {
      return this.drinking;
   }

   private void brainSay(ConnectomeFlyEntity fly, String text) {
      this.brainLabel = text;
      this.brainLabelT = 1.6;
      fly.sayNow(text);
   }

   public void punished() {
      this.punishT = 3.0;
      if (this.asleep) {
         this.asleep = false;
         this.sleepP *= 0.7;
      }
   }

   private float ratio(WholeBrain brain, String name) {
      float[] v = this.out(brain, name);
      return v == null ? 1.0F : v[0] / Math.max(v[1], 0.002F);
   }

   private static int q4(float ratio) {
      return Math.max(0, Math.min(15, Math.round(ratio * 4.0F)));
   }

   public String hudExtra() {
      StringBuilder sb = new StringBuilder();
      if (!Double.isNaN(this.cmpHeading)) {
         sb.append(
            String.format(
               Locale.ROOT,
               "cmp=bump %.0f deg · amp %.2f · home %.1f m%s;",
               (Math.toDegrees(this.cmpHeading) + 360.0) % 360.0,
               this.cmpAmp,
               Math.hypot(this.hvx, this.hvz),
               this.atHome ? " (at the leaf)" : ""
            )
         );
      }

      sb.append(
         String.format(
            Locale.ROOT,
            "mem=valence %+.2f · synapses avoid %.2f / approach %.2f%s;",
            this.mbValence,
            this.memAvoid,
            this.memApproach,
            this.rewardT > 0.0 ? " · PAM reward" : (this.punishT > 0.0 ? " · PPL1 punishment" : "")
         )
      );
      sb.append(
         String.format(
            Locale.ROOT, "slp=pressure %.2f · R5 %.2f vs l-LNv %.2f%s;", this.sleepP, this.r5Rate / 0.42, this.lnvRate / 0.117, this.asleep ? " · ASLEEP" : ""
         )
      );
      double[] w = this.lastWind;
      sb.append(
         String.format(
            Locale.ROOT,
            "air=%s · %.0f%% of the last minutes in the air · wind from the %s%s;",
            this.outdoors ? "open sky" : "under cover",
            100.0 * this.flownT / Math.max(1.0, this.flownT + this.groundT),
            w == null ? "?" : compass8(-w[0], -w[1]),
            this.plumeStrength > 0.0 ? " · food smell on it" : ""
         )
      );
      sb.append(String.format(Locale.ROOT, "sat=IPCs %s;", this.ipcAct > 1.5 ? String.format(Locale.ROOT, "x%.0f (insulin)", this.ipcAct) : "at rest"));
      sb.append("see=").append(this.seeLabel.isEmpty() ? "nothing small moving" : this.seeLabel).append(';');
      sb.append(
         String.format(
            Locale.ROOT, "court=pC1 %.2f%s;", this.receptivity, this.joSong > 0.15 ? String.format(Locale.ROOT, " · JO-B hears song %.1f", this.joSong) : ""
         )
      );
      sb.append(
         String.format(
            Locale.ROOT,
            "clock=M %.0f · E %.0f (s-LNv / LNd x rest)%s;",
            this.clockMout,
            this.clockEout,
            this.clockM > 0.3 ? " · dawn peak" : (this.clockE > 0.3 ? " · dusk peak" : "")
         )
      );
      sb.append(
         String.format(
            Locale.ROOT,
            "taste=%s%s%s%s;",
            this.tasteSugar > 0.0 ? "sugar on the feet " : "",
            this.tasteBitter > 0.0 ? "bitter " : "",
            this.tasteWater > 0.0 ? "water " : "",
            this.tasteSugar + this.tasteBitter + this.tasteWater == 0.0 ? "nothing" : ""
         )
      );
      sb.append(
         String.format(
            Locale.ROOT,
            "opto=HS L/R %.2f%s;",
            this.optoZ - (Double.isNaN(this.optoZRest) ? 0.0 : this.optoZRest),
            this.optoTurn != 0.0 ? String.format(Locale.ROOT, " -> turn %+.2f", this.optoTurn) : ""
         )
      );
      float[] ag = this.lastBrain == null ? null : this.out(this.lastBrain, "dn_aggression");
      sb.append(
         String.format(
            Locale.ROOT,
            "social=%s%s;",
            this.closeN + this.socialN > 0
               ? this.closeN + this.socialN + " other flies near" + (this.closeN > 0 ? " (" + this.closeN + " touching)" : "")
               : "alone",
            this.aggrDrive > 0.0
               ? String.format(
                  Locale.ROOT, " · pC1d/e %.1f · DNa11/DNp37 %.0f Hz/cell (lunge above 9)", this.aggrDrive, ag == null ? 0.0F : ag[0] * 1000.0F / 4.0F
               )
               : ""
         )
      );
      return sb.toString();
   }

   public boolean fleeing() {
      return this.fleeingNow;
   }

   public boolean onDryGround() {
      return this.dryGround;
   }

   private void smoothOutputs(WholeBrain brain) {
      if (brain != null && brain.dataOrNull() != null) {
         WholeBrain.Loco l = brain.loco();
         if (l != null && l != this.lastLoco && l.outRate.length != 0) {
            this.lastLoco = l;
            if (this.outSmooth == null || this.outSmooth.length != l.outRate.length) {
               this.outSmooth = (float[])l.outRate.clone();
            }

            for (int i = 0; i < this.outSmooth.length; i++) {
               this.outSmooth[i] = this.outSmooth[i] + (l.outRate[i] - this.outSmooth[i]) * 0.35F;
            }
         }
      }
   }

   private float[] out(WholeBrain brain, String name) {
      if (brain != null && brain.dataOrNull() != null && this.outSmooth != null) {
         WholeBrain.Loco l = brain.loco();
         String[] names = brain.dataOrNull().outNames;
         if (l != null && l.outBase.length == names.length && this.outSmooth.length == names.length) {
            for (int i = 0; i < names.length; i++) {
               if (names[i].equals(name)) {
                  return new float[]{this.outSmooth[i], l.outBase[i]};
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private boolean aboveRate(WholeBrain brain, String name, float rate) {
      float[] v = this.out(brain, name);
      return v != null && v[0] > rate;
   }

   private boolean active(WholeBrain brain, String name, float ratio, float floor) {
      float[] v = this.out(brain, name);
      return v != null && v[0] > Math.max(v[1] * ratio, floor);
   }

   public static void noteSound(class_2338 pos, long expiryTick) {
      synchronized (RECENT_SOUNDS) {
         RECENT_SOUNDS.removeIf(s -> s[1] < expiryTick - 200L);
         RECENT_SOUNDS.add(new long[]{pos.method_10063(), expiryTick});
      }
   }

   private static boolean soundingAt(class_2338 pos, long now) {
      synchronized (RECENT_SOUNDS) {
         long key = pos.method_10063();

         for (long[] s : RECENT_SOUNDS) {
            if (s[0] == key && s[1] >= now) {
               return true;
            }
         }

         return false;
      }
   }

   public void touched() {
      this.touchT = 0.0;
      this.groomAfterTouch = true;
   }

   public void takeOffNow() {
      this.resting = false;
      this.grooming = false;
      this.forcedFlyT = 16.0;
   }

   public String state() {
      float mx = 0.0F;
      String top = "";
      if (this.outSmooth != null && this.lastLoco != null) {
         for (int i = 0; i < this.outSmooth.length; i++) {
            if (this.outSmooth[i] > mx) {
               mx = this.outSmooth[i];
               top = String.format(Locale.ROOT, "%d:%.3f/base %.3f", i, this.outSmooth[i], i < this.lastLoco.outBase.length ? this.lastLoco.outBase[i] : -1.0F);
            }
         }
      }

      this.label = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.label);
      this.brainOuts = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.brainOuts);
      this.touchT += 0.0;
      String dbg = " top="
         + top
         + " brainOuts='"
         + this.brainOuts
         + "'"
         + String.format(
            Locale.ROOT,
            " cmp=%.0f/%.2f hv=%.1f,%.1f sleepP=%.2f score=%.2f asleep=%s val=%.2f mem=%.2f/%.2f ipc=%.1f lc=%.2f/%.2f cand=%d ang=%.0f",
            Double.isNaN(this.cmpHeading) ? -1.0 : Math.toDegrees(this.cmpHeading),
            this.cmpAmp,
            this.hvx,
            this.hvz,
            this.sleepP,
            this.sleepScore,
            this.asleep,
            this.mbValence,
            this.memAvoid,
            this.memApproach,
            this.ipcAct,
            this.lcLeft,
            this.lcRight,
            this.lcCand,
            this.lcMaxAng
         )
         + " chain["
         + this.chainDbg
         + "]"
         + (
            this.lastBrain == null
               ? ""
               : " da=" + Arrays.toString(this.lastBrain.dopamine()) + String.format(Locale.ROOT, " rewardT=%.1f punishT=%.1f", this.rewardT, this.punishT)
         );
      return String.format(
            "resting=%s grooming=%s neural=%.3f/%.3f stuckT=%.2f label='%s' outSmoothN=%d outMax=%.3f lastLocoOutN=%d touchT=%.2f bus=%s",
            this.resting,
            this.grooming,
            this.neuralSmooth,
            this.neuralBase,
            this.stuckT,
            this.label,
            this.outSmooth == null ? -1 : this.outSmooth.length,
            mx,
            this.lastLoco == null ? -1 : this.lastLoco.outRate.length,
            this.touchT,
            this.bus.toString().replace(';', ',')
         )
         + dbg;
   }

   public void tick(ConnectomeFlyEntity fly, class_3218 world, WholeBrain brain, boolean escapeBusy) {
      double dt = 0.05;
      this.touchT += 0.05;
      if (--this.scanCooldown <= 0) {
         this.scanCooldown = 4;
         this.blockProps = this.scanBlocks(fly, world);
      }

      List<FlyMind.Prop> props = new ArrayList<>(this.blockProps);
      props.addAll(this.scanEntities(fly, world));
      ArenaBuilder.Arena box = ArenaBuilder.arenaAt(world, fly.method_24515());
      boolean inBox = box != null;
      Set<FlyMind.Kind> allowed = box == null ? null : ArenaBuilder.ISOLATION.get(box.id());
      if (allowed != null) {
         props.removeIf(p -> !allowed.contains(p.kind));
      }

      this.boxRivalsOff = box != null && !box.id().equals("aggression") && !box.id().equals("home");
      boolean dryBox = box != null && box.id().equals("dry");
      boolean openSky = !inBox && world.method_8624(class_2903.field_13203, fly.method_31477(), fly.method_31479()) <= fly.method_31478() + 1;
      this.outdoorT = openSky ? this.outdoorT + 0.05 : 0.0;
      this.outdoors = this.outdoorT > 2.0;
      if (fly.isFlying()) {
         this.flownT += 0.05;
      } else {
         this.groundT += 0.05;
      }

      if (this.flownT + this.groundT > 600.0) {
         this.flownT *= 0.5;
         this.groundT *= 0.5;
      }

      this.plumeStrength = 0.0;
      this.lastWind = wind(world);
      this.cruiseAlt = this.spontFly && this.outdoors && this.forcedFlyT > 1.5 && this.lastSenseMag <= 0.35 ? this.boutAlt : 0.0;
      if (!fly.isMale() && this.outdoors && !this.sated) {
         if (--this.farScanCd <= 0) {
            this.farScanCd = 40;
            this.farScan(fly, world);
         }

         if (this.plumeSrc != null) {
            double[] w = wind(world);
            double rx = fly.method_23317() - (this.plumeSrc.method_10263() + 0.5);
            double rz = fly.method_23321() - (this.plumeSrc.method_10260() + 0.5);
            double along = rx * w[0] + rz * w[1];
            double lat = Math.abs(-rx * w[1] + rz * w[0]);
            if (along > 0.0 && along < 26.0 && lat < 1.5 + 0.3 * along) {
               this.plumeStrength = 1.0 - along / 26.0;
            }
         }
      } else {
         this.plumeSrc = null;
      }

      boolean windBox = box != null && box.id().equals("wind");
      this.drinking = false;
      double ux = fly.method_23317();
      double uz = fly.method_23321();
      double uy = fly.method_23318();
      boolean night = world.method_23886();
      boolean raining = world.method_8419();
      class_2338 here = fly.method_24515();
      boolean exposed = raining && world.method_8311(here);
      boolean daylight = world.method_8530() && world.method_8314(class_1944.field_9284, here) >= 12;
      double yawNow = fly.method_36454();
      double dyaw = Double.isNaN(this.prevYaw) ? 0.0 : class_3532.method_15393((float)(yawNow - this.prevYaw)) / 0.05;
      this.prevYaw = yawNow;
      this.lastBrain = brain;
      if (brain != null && !fly.isMale()) {
         double headRad = Math.atan2(Math.cos(Math.toRadians(yawNow)), -Math.sin(Math.toRadians(yawNow)));
         int sky = world.method_8314(class_1944.field_9284, here);
         float landmark = daylight ? 0.6F : (world.method_8530() ? 0.35F : (sky >= 8 ? 0.2F : 0.1F));
         brain.setCompassInput((float)headRad, landmark);
         this.sleepP = Math.max(0.0, Math.min(1.0, this.sleepP + (this.asleep ? -2.0833333333333335E-4 : 5.555555555555556E-5)));
         int block = world.method_8314(class_1944.field_9282, here);
         double light = daylight ? 0.7 : (world.method_8530() ? 0.35 : 0.05);
         light = Math.max(light, block / 15.0 * 0.45);
         WholeBrain.Loco lc0 = brain.loco();
         WholeBrain.Data dd = brain.dataOrNull();
         if (lc0 != null && dd != null) {
            int c5 = dd.channel("sleep_pressure");
            int cl = dd.channel("light_clock");
            if (c5 >= 0 && c5 < lc0.chanSpikes.length) {
               this.r5Rate = this.r5Rate + (lc0.chanSpikes[c5] - this.r5Rate) * Math.min(1.0, 0.07500000000000001);
            }

            if (cl >= 0 && cl < lc0.chanSpikes.length) {
               this.lnvRate = this.lnvRate + (lc0.chanSpikes[cl] - this.lnvRate) * Math.min(1.0, 0.07500000000000001);
            }

            this.sleepScore = this.r5Rate / 0.42 - this.lnvRate / 0.117;
            double kc = Math.min(1.0, 0.15000000000000002);
            if (!Float.isNaN(lc0.compassAngle)) {
               this.cmpVx = this.cmpVx + (Math.cos(lc0.compassAngle) * lc0.compassAmp - this.cmpVx) * kc;
               this.cmpVz = this.cmpVz + (Math.sin(lc0.compassAngle) * lc0.compassAmp - this.cmpVz) * kc;
            } else {
               this.cmpVx = this.cmpVx - this.cmpVx * kc;
               this.cmpVz = this.cmpVz - this.cmpVz * kc;
            }

            this.cmpAmp = Math.hypot(this.cmpVx, this.cmpVz);
            if (this.cmpAmp > 0.2) {
               this.cmpHeading = Math.atan2(this.cmpVz, this.cmpVx);
            }

            this.memAvoid = lc0.memAvoid;
            this.memApproach = lc0.memApproach;
         }

         boolean wasAsleep = this.asleep;
         if (!this.asleep && this.sleepScore > 0.15 && !escapeBusy) {
            this.asleep = true;
         } else if (this.asleep && this.sleepScore < -0.05) {
            this.asleep = false;
         }

         if (this.asleep != wasAsleep) {
            fly.sayNow(this.asleep ? "[brain] her R5 sleep cells out-fire her l-LNv clock cells: she sleeps" : "[brain] l-LNv light input wins: she wakes");
         }

         if (!Double.isNaN(this.cmpHeading) && this.cmpAmp > 0.2 && !fly.isDragged()) {
            double step = fly.method_18798().method_37267();
            this.hvx = this.hvx + step * Math.cos(this.cmpHeading);
            this.hvz = this.hvz + step * Math.sin(this.cmpHeading);
         }

         this.rewardT = Math.max(0.0, this.rewardT - 0.05);
         this.punishT = Math.max(0.0, this.punishT - 0.05);
         this.nutrientT = Math.max(0.0, this.nutrientT - 0.05);
      }

      if (!fly.isMale() && (fly.method_5799() || fly.method_5771())) {
         fly.requestFlight(true);
         class_243 v = fly.method_18798();
         fly.method_18800(v.field_1352, Math.max(v.field_1351, 0.14), v.field_1350);
         fly.field_6037 = true;
      }

      boolean grounded = (fly.isStanding() || fly.isFlying()) && !escapeBusy;
      if (fly.getHunger() >= 19.5F) {
         this.sated = true;
      } else if (fly.getHunger() < 15.0F) {
         this.sated = false;
      }

      double hungerFactor = this.sated ? 0.0 : 0.3 + (1.0F - fly.getHunger() / 20.0F) * 3.1;
      if (fly.isMating()) {
         this.stopNav(fly);
         fly.setAnimFlags(false, false, false, false);
         fly.setSinging(false);
         this.label(fly, fly.isMale() ? "mating" : "mating: she accepted his song");
      } else if (fly.isMale()) {
         this.maleTick(fly, world, props, grounded, 0.05);
         this.hazards(fly, world, props, exposed, grounded, 0.05);
      } else {
         double ax = 0.0;
         double az = 0.0;
         FlyMind.Prop feed = null;
         FlyMind.Prop domRepel = null;
         FlyMind.Prop domAttract = null;
         FlyMind.Prop fanUp = null;
         FlyMind.Prop home = null;
         FlyMind.Prop rival = null;
         double domRInt = 0.0;
         double domAConc = 0.0;
         double fanFeel = 0.0;
         double rivalD = 1.0E9;
         this.fruitDominant = this.smellFruit > this.smellFerment;
         this.smellFruit = this.smellFerment = 0.0;
         boolean flee = false;
         this.bus.clear();

         for (FlyMind.Prop p : props) {
            double dx = (p.x - ux) / 0.95;
            double dz = (p.z - uz) / 0.95;
            double d = Math.max(0.25, Math.hypot(dx, dz));
            FlyMind.Info info = p.info;
            if (p.kind == FlyMind.Kind.HOME) {
               if (home == null || d < Math.hypot((home.x - ux) / 0.95, (home.z - uz) / 0.95)) {
                  home = p;
               }
            } else if (p.kind == FlyMind.Kind.FAN) {
               double vx = ux - p.x;
               double vz = uz - p.z;
               double ddx = Math.max(1.0E-6, Math.hypot(vx, vz));
               double along = (vx * p.fanDx + vz * p.fanDz) / ddx;
               double dU = ddx / 0.95;
               if (!windBox && world.method_8510() % 3L == 0L && dU < info.range) {
                  WindFx.puff(world, p.x + p.fanDx * 0.8, p.y + 0.5, p.z + p.fanDz * 0.8, p.fanDx, p.fanDz);
               }

               if (dU < info.range && along > 0.78) {
                  double feel = (1.0 - dU / info.range) * along;
                  if (feel > fanFeel) {
                     fanFeel = feel;
                     fanUp = p;
                  }

                  if (grounded) {
                     class_243 v = fly.method_18798();
                     fly.method_18799(v.method_1031(p.fanDx * feel * 0.45 * 0.95 * 0.05 * 0.05 * 20.0, 0.0, p.fanDz * feel * 0.45 * 0.95 * 0.05 * 0.05 * 20.0));
                  }
               }
            } else if (p.kind == FlyMind.Kind.MATE) {
               double prox = Math.max(0.0, 1.0 - d / info.range);
               if (prox > 0.0) {
                  this.busMax("pheromone", prox * 0.35);
               }

               if (p.ent instanceof ConnectomeFlyEntity m && (m.animFlags() & 64) != 0 && d < 3.0) {
                  this.busMax("auditory", 0.25);
                  this.songHeardT = 0.8;
                  if (fly.courtedSeconds() > 0.0) {
                     this.label(fly, String.format(Locale.ROOT, "courted: his song on her JO (%.0f s)", fly.courtedSeconds()));
                  }
               }
            } else {
               double proxx = Math.max(0.0, 1.0 - d / info.range);
               if (proxx > 0.0) {
                  switch (p.kind) {
                     case LAVA:
                        this.busMax("heating", d < 1.5 ? 1.0 : proxx);
                        break;
                     case PREDATOR:
                     case CO2:
                        this.busMax("olf_co2", proxx);
                        break;
                     case POND:
                        this.busMax("humid", proxx);
                        break;
                     case FOOD:
                        this.busAdd(p.fruit ? "olf_fruit" : "olf_food", proxx * 0.7);
                        if (p.fruit) {
                           this.smellFruit += proxx;
                        } else {
                           this.smellFerment += proxx;
                        }
                        break;
                     case LURE:
                        this.busAdd("olf_fruit", proxx * 0.4);
                        this.smellFruit += proxx * 0.5;
                     case MATE:
                     case FAN:
                     case HOME:
                     default:
                        break;
                     case BITTER:
                     case GEOSMIN:
                        this.busMax("aversive", proxx);
                        break;
                     case SPEAKER:
                        this.busMax("auditory", proxx);
                        break;
                     case SNOW:
                        this.busMax("cooling", proxx);
                        break;
                     case LIGHT:
                        this.busMax("visual", proxx);
                        break;
                     case PAIN:
                        this.busMax("tactile", d < info.contact + 0.3 ? 1.0 : 0.0);
                        break;
                     case DAMP:
                        this.busMax("humid", d < 1.2 ? 0.5 : proxx * 0.3);
                        break;
                     case DRY:
                        this.busMax("dry", d < 1.2 ? 0.4 : proxx * 0.2);
                  }
               }

               if (p.kind == FlyMind.Kind.DAMP && fly.getThirst() > 12.0F && d < 10.0 && d > 1.0) {
                  double conc = 1.0 / (1.0 + d * d / 6.0) * (0.8 + (fly.getThirst() - 12.0F) / 8.0F * 2.2) * (p.cauldron ? 1.8 : 1.0);
                  ax += dx / d * conc * 1.3;
                  az += dz / d * conc * 1.3;
                  this.busMax("humid", Math.max(0.15, 1.0 - d / 10.0));
                  if (conc > domAConc) {
                     domAConc = conc;
                     domAttract = p;
                  }
               } else if (!(d > info.range) && (p.kind != FlyMind.Kind.LIGHT || !daylight) && (p.kind != FlyMind.Kind.POND || !fly.noGroundBelow())) {
                  if (!info.effect.equals("attract")) {
                     double inten = 1.0 - d / info.range;
                     ax += -dx / d * inten * info.gain;
                     az += -dz / d * inten * info.gain;
                     if (inten > domRInt) {
                        domRInt = inten;
                        domRepel = p;
                     }

                     if (info.flee && inten > 0.3) {
                        flee = true;
                     }
                  } else {
                     if (p.kind == FlyMind.Kind.LURE && p.ent == null && d < 1.7) {
                        this.lureNearT += 0.05;
                     }

                     double conc = 1.0
                        / (1.0 + d * d / 6.0)
                        * (
                           p.kind == FlyMind.Kind.LIGHT
                              ? 1.0
                              : (
                                 p.kind == FlyMind.Kind.LURE
                                    ? (p.ent != null ? Math.max(0.6, hungerFactor) : (this.lureHabT > 0.0 ? 0.0 : 0.35 * hungerFactor))
                                    : hungerFactor
                              )
                        );
                     if (p.kind != FlyMind.Kind.FOOD || !(this.rejectT > 0.0) || !(Math.hypot(p.x - this.rejectX, p.z - this.rejectZ) < 1.6)) {
                        if (p.kind == FlyMind.Kind.FOOD || p.kind == FlyMind.Kind.LURE) {
                           conc /= 1.0 + Math.min(3.0, Math.max(0.0, this.ipcAct - 1.0) * 0.5);
                           if (this.mbValence < -0.12 && p.fruit == this.fruitDominant) {
                              double rep = (0.4 + (-this.mbValence - 0.12) / 0.4) * conc * info.gain;
                              ax -= dx / d * rep;
                              az -= dz / d * rep;
                              continue;
                           }

                           if (this.mbValence > 0.35) {
                              conc *= 1.0 + (this.mbValence - 0.35) * 2.0;
                           }
                        }

                        ax += dx / d * conc * info.gain;
                        az += dz / d * conc * info.gain;
                        if (conc > domAConc) {
                           domAConc = conc;
                           domAttract = p;
                        }

                        double dFace = p.pos == null
                           ? d
                           : Math.hypot(
                                    Math.max(0.0, Math.abs(ux - (p.pos.method_10263() + 0.5)) - 0.5),
                                    Math.max(0.0, Math.abs(uz - (p.pos.method_10260() + 0.5)) - 0.5)
                                 )
                                 / 0.95
                              + 0.3;
                        if (Math.min(d, dFace) < info.contact && info.consume && Math.abs(p.y - uy) < 1.2 && !this.sated) {
                           feed = p;
                        }
                     }
                  }
               }
            }
         }

         for (FlyMind.Prop px : props) {
            if (px.kind == FlyMind.Kind.MATE) {
            }
         }

         this.socialAx = this.socialAz = 0.0;
         this.socialN = 0;
         this.closeN = 0;
         this.aggrDrive = 0.0;
         boolean nearFood = feed != null
            || domAttract != null && domAttract.kind == FlyMind.Kind.FOOD && Math.hypot(domAttract.x - ux, domAttract.z - uz) < 3.5;

         for (class_1297 e : world.method_8333(
            fly, fly.method_5829().method_1014(8.0), o -> o instanceof ConnectomeFlyEntity f && f.method_5805() && !f.isMale() && !f.isBabyFly()
         )) {
            double dx = e.method_23317() - ux;
            double dz = e.method_23321() - uz;
            double d = Math.max(0.3, Math.hypot(dx, dz) / 0.95);
            if (!this.boxRivalsOff && d < rivalD) {
               rivalD = d;
               rival = new FlyMind.Prop(FlyMind.Kind.PREDATOR, e.method_23317(), e.method_23318(), e.method_23321());
               rival.ent = e;
            }

            if (d <= 2.0) {
               this.closeN++;
            }

            if (d > 2.0 && d < 8.5) {
               double pull = (1.0 - d / 8.5) * (nearFood ? 0.5 : 0.25);
               this.socialAx += dx / (d * 0.95) * pull;
               this.socialAz += dz / (d * 0.95) * pull;
               this.socialN++;
               this.busMax("pheromone", Math.min(0.3, 0.1 + 0.05 * this.socialN));
            }

            if (!this.boxRivalsOff && d < 3.5 && (nearFood || fly.getHunger() < 12.0F)) {
               this.aggrDrive = Math.max(this.aggrDrive, 0.4 + 0.6 * (1.0 - d / 3.5));
            }

            if (!this.boxRivalsOff && d < 1.8) {
               this.aggrDrive = Math.max(this.aggrDrive, 0.8);
            }
         }

         this.lungeCd = Math.max(0.0, this.lungeCd - 0.05);
         this.retreatT = Math.max(0.0, this.retreatT - 0.05);
         if (this.retreatT > 0.0) {
            this.aggrDrive = 0.0;
         }

         if (this.aggrDrive > 0.0) {
            this.busMax("aggression_pc1", this.aggrDrive);
         }

         this.tasteSugar = this.tasteBitter = this.tasteWater = 0.0;
         this.dryGround = false;

         for (FlyMind.Prop pxx : props) {
            double dxx = Math.hypot(pxx.x - ux, pxx.z - uz) / 0.95;
            if (pxx.kind == FlyMind.Kind.FOOD && dxx < pxx.info.contact + 0.2 && Math.abs(pxx.y - uy) < 1.2) {
               this.tasteSugar = 1.0;
            }

            if (pxx.kind == FlyMind.Kind.BITTER && dxx < 1.2) {
               this.tasteBitter = Math.max(this.tasteBitter, 1.0 - dxx / 1.2);
            }

            if (pxx.kind == FlyMind.Kind.DAMP && dxx < 1.3) {
               this.tasteWater = Math.max(this.tasteWater, 0.8);
            }

            if (pxx.kind == FlyMind.Kind.DRY && dxx < 1.2) {
               this.dryGround = true;
            }
         }

         if (this.tasteSugar > 0.0) {
            this.busMax("taste_sugar_leg", this.tasteSugar);
         }

         if (feed != null) {
            this.busMax("taste_sugar_labellum", 1.0);
         }

         if (this.tasteBitter > 0.0) {
            this.busMax("taste_bitter_leg", this.tasteBitter);
            if (feed != null) {
               this.busMax("taste_bitter_labellum", this.tasteBitter);
            }
         }

         if (feed != null) {
            for (FlyMind.Prop pxx : props) {
               if (pxx.kind == FlyMind.Kind.BITTER && Math.hypot(pxx.x - feed.x, pxx.z - feed.z) < 1.6) {
                  this.tasteBitter = Math.max(this.tasteBitter, 0.8);
                  this.busMax("taste_bitter_leg", 0.8);
                  this.busMax("taste_bitter_labellum", 0.8);
               }
            }
         }

         this.feedingSuppressed = this.tasteBitter > 0.5;
         this.rejectT = Math.max(0.0, this.rejectT - 0.05);
         this.songHeardT = Math.max(0.0, this.songHeardT - 0.05);
         this.painT = Math.max(0.0, this.painT - 0.05);
         if (this.tasteWater > 0.0 && fly.getThirst() > 6.0F) {
            this.busMax("taste_water", this.tasteWater);
         }

         this.waterContactT = this.tasteWater > 0.0 && fly.getThirst() > 6.0F && !fly.isFlying() ? this.waterContactT + 0.05 : 0.0;
         if (dryBox && this.dryGround) {
            class_2338 feet = fly.method_24515().method_10074();
            double bestD = 1.0E9;
            double bx = 0.0;
            double bz = 0.0;
            class_2339 mx = new class_2339();

            for (int ox = -6; ox <= 6; ox++) {
               for (int oz = -6; oz <= 6; oz++) {
                  mx.method_10103(feet.method_10263() + ox, feet.method_10264(), feet.method_10260() + oz);
                  class_2680 fs = world.method_8320(mx);
                  boolean dry = fs.method_26164(class_3481.field_15466) || fs.method_26164(class_3481.field_36265);
                  if (!dry && fs.method_26212(world, mx) && world.method_8320(mx.method_10084()).method_26215()) {
                     double ddxx = Math.hypot(ox, oz);
                     if (ddxx < bestD) {
                        bestD = ddxx;
                        bx = mx.method_10263() + 0.5 - ux;
                        bz = mx.method_10260() + 0.5 - uz;
                     }
                  }
               }
            }

            if (bestD < 1.0E8) {
               double bl = Math.max(1.0E-6, Math.hypot(bx, bz));
               ax += bx / bl * 1.2;
               az += bz / bl * 1.2;

               for (FlyMind.Prop pxxx : props) {
                  if (pxxx.kind == FlyMind.Kind.DRY && domRepel == null) {
                     domRepel = pxxx;
                  }
               }
            }
         }

         if (domAttract != null) {
            double tx = domAttract.x - ux;
            double tz = domAttract.z - uz;
            double tl = Math.max(1.0E-6, Math.hypot(tx, tz));

            for (FlyMind.Prop pxxxx : props) {
               if (pxxxx.kind == FlyMind.Kind.CO2) {
                  double cx = pxxxx.x - ux;
                  double cz = pxxxx.z - uz;
                  double clx = Math.max(1.0E-6, Math.hypot(cx, cz));
                  double alongPath = (cx * tx + cz * tz) / tl;
                  double cross = (tx * cz - tz * cx) / tl;
                  if (alongPath > -0.5 && alongPath < tl && Math.abs(cross) < 3.0 && clx / 0.95 < pxxxx.info.range + 1.0) {
                     if (Math.abs(cross) > 0.25) {
                        this.detourSide = cross > 0.0 ? -1 : 1;
                     } else if (this.detourSide == 0) {
                        this.detourSide = world.field_9229.method_43056() ? 1 : -1;
                     }

                     double k = (1.0 - Math.abs(cross) / 3.0) * 1.6 * Math.min(1.5, hungerFactor);
                     ax += -tz / tl * this.detourSide * k;
                     az += tx / tl * this.detourSide * k;
                  }
               }
            }
         } else {
            this.detourSide = 0;
         }

         if (this.plumeStrength > 0.0) {
            this.busAdd(this.plumeFruit ? "olf_fruit" : "olf_food", 0.1 + 0.15 * this.plumeStrength);
         }

         ax += this.socialAx;
         az += this.socialAz;
         double senseMag = Math.hypot(ax, az);
         this.lastSenseMag = senseMag;
         this.lureHabT = Math.max(0.0, this.lureHabT - 0.05);
         if (this.lureNearT > 3.0) {
            this.lureNearT = 0.0;
            this.lureHabT = 90.0;
         }

         if (fanUp != null) {
            this.busMax("wind", Math.min(1.0, fanFeel * 1.4));
         }

         if (feed != null) {
            this.busMax("gust_sugar", 1.0);
         }

         if (raining) {
            this.busMax("humid", 0.7);
         }

         class_1959 biome = (class_1959)world.method_23753(here).comp_349();
         float temp = biome.method_8712();
         if (temp >= 1.5F) {
            this.busMax("heating", Math.min(0.6, (temp - 1.5) * 0.6 + 0.3));
            this.busMax("dry", 0.6);
         } else if (temp <= 0.15F) {
            this.busMax("cooling", 0.5);
         }

         if (biome.method_48162(here) == class_1963.field_9383) {
            this.busMax("cooling", 0.5);
         }

         if (world.method_8546()) {
            this.busMax("auditory", 0.25);
         }

         if (this.touchT < 0.6 || fly.field_5976) {
            this.busMax("tactile", this.touchT < 0.6 ? 1.0 : 0.25);
         }

         double tod = world.method_8532() % 24000L / 24000.0;
         this.clockM = Math.exp(-Math.pow(((tod + 0.5) % 1.0 - 0.5) / 0.09, 2.0));
         this.clockE = Math.exp(-Math.pow((tod - 0.5) / 0.09, 2.0));
         this.busMax("clock_morning", this.clockM);
         this.busMax("clock_evening", this.clockE);
         ConnectomeFlyEntity suitor = fly.suitor();
         if (suitor != null) {
            this.busMax("song", 0.6);
         }

         WholeBrain.Data ddxx = brain == null ? null : brain.dataOrNull();
         WholeBrain.Loco lc1 = brain == null ? null : brain.loco();
         if (ddxx != null && lc1 != null) {
            int ca = ddxx.channel("auditory");
            if (ca >= 0 && ca < lc1.chanSpikes.length) {
               double background = ddxx.chanIdx[ca].length * 0.001;
               double jo = (lc1.chanSpikes[ca] - background) / 6.0;
               this.joSong = this.joSong + (Math.max(0.0, Math.min(1.5, jo)) - this.joSong) * Math.min(1.0, 0.15000000000000002);
            }
         }

         if (suitor != null && this.joSong > 0.15) {
            this.busMax("song_pc1", Math.min(1.0, this.joSong) * (this.bus.getOrDefault("pheromone", 0.0F) > 0.05F ? 1.0 : 0.5));
         }

         double[] dr = drum;
         double rel = 0.0;
         if (dr != null && Math.hypot(dr[0] - ux, dr[1] - uz) < dr[3] + 1.5) {
            rel += dr[2];
         }

         double var126 = rel - dyaw * 0.25;
         double mag = Math.min(1.0, Math.abs(var126) / 60.0);
         if (mag > 0.03) {
            if (var126 > 0.0) {
               this.busMax("motion_prog_right", mag);
               this.busMax("motion_reg_left", mag);
            } else {
               this.busMax("motion_prog_left", mag);
               this.busMax("motion_reg_right", mag);
            }
         }

         if (fly.noGroundBelow()) {
            if (--this.lmScanT <= 0) {
               this.lmScanT = 20;
               this.lmBearing = Double.NaN;
               this.lmDist = 1.0E9;
               class_243 eye = fly.method_33571();

               for (int k = 0; k < 16; k++) {
                  double yawK = k * 22.5;
                  double rad = Math.toRadians(yawK);
                  class_243 dir = new class_243(-Math.sin(rad), 0.0, Math.cos(rad));

                  for (double dyy : new double[]{0.5, 3.0}) {
                     class_239 hit = world.method_17742(
                        new class_3959(
                           eye, eye.method_1019(dir.method_1021(64.0)).method_1031(0.0, dyy, 0.0), class_3960.field_17558, class_242.field_1348, fly
                        )
                     );
                     if (hit.method_17783() == class_240.field_1332) {
                        double ddxxx = eye.method_1022(hit.method_17784());
                        if (ddxxx < this.lmDist) {
                           this.lmDist = ddxxx;
                           this.lmBearing = yawK;
                        }
                     }
                  }
               }
            }

            if (!Double.isNaN(this.lmBearing)) {
               tod = class_3532.method_15393((float)(this.lmBearing - fly.method_36454()));
               double magx = 0.35 + 0.35 * (1.0 - Math.min(1.0, this.lmDist / 64.0));
               this.busMax(tod > 0.0 ? "landmark_right" : "landmark_left", magx);
            }
         } else {
            this.lmBearing = Double.NaN;
         }

         if (dyaw > 0.0) {
            this.busMax("pen_right", Math.min(1.0, dyaw / 240.0));
         } else if (dyaw < 0.0) {
            this.busMax("pen_left", Math.min(1.0, -dyaw / 240.0));
         }

         this.busMax("sleep_pressure", this.sleepP);
         int blockx = world.method_8314(class_1944.field_9282, here);
         double lightx = daylight ? 0.7 : (world.method_8530() ? 0.35 : 0.05);
         this.busMax("light_clock", Math.max(lightx, blockx / 15.0 * 0.45));
         if (this.rewardT > 0.0) {
            this.busMax("reward", 1.0);
         }

         if (this.punishT > 0.0) {
            this.busMax("punishment", 1.0);
         }

         if (this.nutrientT > 0.0) {
            this.busMax("nutrient", Math.min(1.0, this.nutrientT / 20.0));
         }

         this.lcLeft = this.lcRight = 0.0;
         this.seeLabel = "";
         tod = -Math.sin(Math.toRadians(fly.method_36454()));
         double hz = Math.cos(Math.toRadians(fly.method_36454()));
         this.lcCand = 0;
         this.lcMaxAng = 0.0;

         for (class_1297 e : world.method_8333(
            fly,
            fly.method_5829().method_1014(10.0),
            o -> o instanceof class_1657
               || o instanceof ConnectomeFlyEntity
               || o instanceof class_1542
               || o instanceof class_1309 le && le.method_17681() < 0.7F
         )) {
            double dxxx = e.method_23317() - ux;
            double dzx = e.method_23321() - uz;
            double dist = Math.hypot(dxxx, dzx);
            this.lcCand++;
            if (!(dist < 0.3)) {
               double fwd = (dxxx * tod + dzx * hz) / dist;
               double side = (dxxx * hz - dzx * tod) / dist;
               double bearing = Math.atan2(side, fwd);
               if (Math.abs(bearing) > Math.toRadians(110.0)) {
                  this.lastBearing.remove(e.method_5628());
               } else {
                  Double prevB = this.lastBearing.get(e.method_5628());
                  this.lastBearing.put(e.method_5628(), bearing);
                  if (prevB != null) {
                     double angSpeed = Math.abs(class_3532.method_15393((float)Math.toDegrees(bearing - prevB))) / 0.05;
                     this.lcMaxAng = Math.max(this.lcMaxAng, angSpeed);
                     double magx = Math.min(1.0, angSpeed / 90.0) * Math.max(0.0, 1.0 - dist / 10.0);
                     if (!(magx < 0.05)) {
                        if (side > 0.0) {
                           this.lcLeft = Math.max(this.lcLeft, magx);
                        } else {
                           this.lcRight = Math.max(this.lcRight, magx);
                        }
                     }
                  }
               }
            }
         }

         if (this.lastBearing.size() > 64) {
            this.lastBearing.clear();
         }

         if (this.lcLeft > 0.0) {
            this.busMax("small_object_left", this.lcLeft);
         }

         if (this.lcRight > 0.0) {
            this.busMax("small_object_right", this.lcRight);
         }

         if (brain != null && --this.busCooldown <= 0) {
            this.busCooldown = 2;
            brain.setBus(this.bus);
         }

         FlyMind.Prop homeReturn = null;
         this.atHome = false;
         if (home != null) {
            if (Math.hypot(home.x - ux, home.z - uz) < 1.2) {
               this.hvx = this.hvz = 0.0;
               this.atHome = true;
            }

            this.homeStayT = this.atHome && !this.homeReturning ? this.homeStayT + 0.05 : 0.0;
            this.excT = Math.max(0.0, this.excT - 0.05);
            if (this.homeStayT > 3.0 && this.excT <= 0.0 && !this.homeReturning) {
               this.excT = 4.0 + world.field_9229.method_43058() * 3.0;
               this.excYaw = world.field_9229.method_43058() * 360.0;
               this.homeStayT = 0.0;
            }

            this.homeT -= 0.05;
            if (this.homeT <= 0.0) {
               this.homeReturning = !this.homeReturning;
               this.homeT = this.homeReturning ? 8.0 : 12.0 + world.field_9229.method_43058() * 8.0;
            }

            this.homeBySight = false;
            if (this.homeReturning && Math.hypot(this.hvx, this.hvz) > 0.8 && !this.atHome && this.cmpAmp > 0.2) {
               homeReturn = new FlyMind.Prop(FlyMind.Kind.HOME, ux - this.hvx, home.y, uz - this.hvz);
            } else if (this.homeReturning && !this.atHome && Math.hypot(home.x - ux, home.z - uz) < 4.5) {
               homeReturn = home;
               this.homeBySight = true;
            }
         } else {
            this.hvx = this.hvz = 0.0;
         }

         class_2338 roof = null;
         if ((!raining || exposed) && raining) {
            roof = nearestRoof(world, here);
         }

         this.fleeingNow = flee;
         this.hazards(fly, world, props, exposed && grounded && !flee, grounded, 0.05);
         fly.setAnimFlags(false, false, false, feed != null);
         if (!fly.method_29504() && grounded) {
            this.brainJumpCd = Math.max(0.0, this.brainJumpCd - 0.05);
            this.brainLabelT = Math.max(0.0, this.brainLabelT - 0.05);
            this.eggCd = Math.max(0.0, this.eggCd - 0.05);
            this.scrambleT = Math.max(0.0, this.scrambleT - 0.05);
            this.backT = Math.max(0.0, this.backT - 0.05);
            this.proboscisT = Math.max(0.0, this.proboscisT - 0.05);
            this.smoothOutputs(brain);
            StringBuilder outs = new StringBuilder();
            if (brain != null && brain.dataOrNull() != null && brain.loco() != null && this.outSmooth != null) {
               WholeBrain.Loco l = brain.loco();
               String[] names = brain.dataOrNull().outNames;

               for (int i = 0; i < names.length && i < this.outSmooth.length && i < l.outBase.length; i++) {
                  float base = Math.max(l.outBase[i], 0.001F);
                  if (!names[i].equals("epg")
                     && !names[i].equals("clock")
                     && !names[i].startsWith("lc_small")
                     && this.outSmooth[i] > Math.max(l.outBase[i] * 1.8F, 0.03F * Math.min(1.0F, brain.dataOrNull().outIdx[i].length / 20.0F))) {
                     if (outs.length() > 0) {
                        outs.append(" · ");
                     }

                     outs.append(names[i].replace("mn_", "").replace("dn_", ""))
                        .append(" x")
                        .append(String.format(Locale.ROOT, "%.0f", this.outSmooth[i] / base));
                  }
               }
            }

            this.brainOuts = outs.toString();
            float[] ap = this.out(brain, "mbon_approach");
            float[] av = this.out(brain, "mbon_avoid");
            if (ap != null && av != null) {
               double v = (ap[0] - av[0]) / (ap[0] + av[0] + 0.004);
               boolean smelling = this.bus.getOrDefault("olf_food", 0.0F) > 0.08F || this.bus.getOrDefault("olf_fruit", 0.0F) > 0.08F;
               this.mbRaw = this.mbRaw + (v - this.mbRaw) * Math.min(1.0, 0.04000000000000001);
               if (!smelling) {
                  this.mbRest = this.mbRest + (this.mbRaw - this.mbRest) * Math.min(1.0, 0.010000000000000002);
               }

               this.mbValence = smelling ? this.mbRaw - this.mbRest : 0.0;
            }

            this.ipcAct = this.ipcAct + (this.ratio(brain, "ipc") - this.ipcAct) * Math.min(1.0, 0.1);
            float[] pc = this.out(brain, "pc1_receptive");
            double rate = pc == null ? 0.0 : pc[0] * 1000.0 / 6.0;
            this.receptivity = this.receptivity + (rate / 18.0 - this.receptivity) * Math.min(1.0, 0.005555555555555556);
            ConnectomeFlyEntity suitorx = fly.suitor();
            if (suitorx != null && this.receptivity > 0.5 && !fly.isMating()) {
               fly.acceptMate(suitorx);
            }

            this.clockMout = this.clockMout + (this.ratio(brain, "clock_morning") - this.clockMout) * Math.min(1.0, 0.05);
            this.clockEout = this.clockEout + (this.ratio(brain, "clock_evening") - this.clockEout) * Math.min(1.0, 0.05);
            double hl = Math.max(0.05, (double)this.ratio(brain, "hs_left"));
            double z = Math.log(hl);
            this.optoZ = this.optoZ + (z - this.optoZ) * Math.min(1.0, 0.15000000000000002);
            boolean motionOn = this.bus.containsKey("motion_prog_left") || this.bus.containsKey("motion_prog_right");
            double o = Math.max(-1.0, Math.min(1.0, -this.optoZ / 1.5));
            this.optoTurn = motionOn ? o : 0.0;
            this.optoZRest = 0.0;
            this.neuroLegs = q4(this.ratio(brain, "mn_leg_front_left"))
               | q4(this.ratio(brain, "mn_leg_front_right")) << 4
               | q4(this.ratio(brain, "mn_leg_middle_left")) << 8
               | q4(this.ratio(brain, "mn_leg_middle_right")) << 12
               | q4(this.ratio(brain, "mn_leg_hind_left")) << 16
               | q4(this.ratio(brain, "mn_leg_hind_right")) << 20;
            this.neuroFlight = q4(this.ratio(brain, "mn_wing_steer_left"))
               | q4(this.ratio(brain, "mn_wing_steer_right")) << 4
               | q4(this.ratio(brain, "mn_haltere_left")) << 8
               | q4(this.ratio(brain, "mn_haltere_right")) << 12
               | q4(this.ratio(brain, "mn_respiratory")) << 16
               | q4(this.ratio(brain, "mn_neck_left")) << 20
               | q4(this.ratio(brain, "mn_neck_right")) << 24;
            fly.setNeuro(this.neuroLegs, this.neuroFlight);
            boolean lcl = this.active(brain, "lc_small_left", 3.0F, 0.05F);
            boolean lcr = this.active(brain, "lc_small_right", 3.0F, 0.05F);
            if (lcl || lcr) {
               this.seeLabel = "[brain] LC11/LC10a fire (" + (lcl && lcr ? "both eyes" : (lcl ? "left eye" : "right eye")) + "): her descending neurons decide";
            }

            if (!(this.mbValence > 0.5) || domAttract == null || domAttract.kind != FlyMind.Kind.FOOD && domAttract.kind != FlyMind.Kind.LURE) {
               if (this.mbValence < -0.12 && (this.bus.getOrDefault("olf_food", 0.0F) > 0.1F || this.bus.getOrDefault("olf_fruit", 0.0F) > 0.1F)) {
                  this.brainLabel = String.format(
                     Locale.ROOT,
                     "[brain] mushroom body: avoid the %s smell (MBON valence %+.2f, learned)",
                     this.fruitDominant ? "fruit" : "cake",
                     this.mbValence
                  );
                  this.brainLabelT = Math.max(this.brainLabelT, 0.3);
               }
            } else {
               this.brainLabel = String.format(Locale.ROOT, "[brain] mushroom body: approach (MBON valence %+.2f, learned)", this.mbValence);
               this.brainLabelT = Math.max(this.brainLabelT, 0.3);
            }

            if (this.painT > 0.0) {
               if (this.painJump) {
                  this.painJump = false;
                  fly.brainJump();
               }

               this.stopNav(fly);
               FlyMind.Prop spine = null;
               double sd = 1.0E9;

               for (FlyMind.Prop pxxxxx : props) {
                  double pd = Math.hypot(pxxxxx.x - ux, pxxxxx.z - uz);
                  if (pxxxxx.kind == FlyMind.Kind.PAIN && pd < sd) {
                     sd = pd;
                     spine = pxxxxx;
                  }
               }

               fly.requestFlight(true);
               if (spine != null) {
                  face(fly, ux - spine.x, uz - spine.z);
               }

               this.drive(fly, 3.4);
               fly.setAnimFlags(true, false, false, false);
               this.label(fly, "recoils from the spines: nociceptors (contact pain)");
            } else {
               boolean legBurst = this.active(brain, "mn_leg_front", 8.0F, 0.05F)
                  || this.active(brain, "mn_leg_middle", 8.0F, 0.05F)
                  || this.active(brain, "mn_leg_hind", 8.0F, 0.05F);
               float gfRatio = fly.courtedSeconds() > 0.0 ? 12.0F : 4.0F;
               float jumpRatio = fly.courtedSeconds() > 0.0 ? 6.0F : 2.0F;
               boolean onlyHisSong = this.songHeardT > 0.0 && this.bus.getOrDefault("auditory", 0.0F) <= 0.3F && this.touchT > 1.5 && !flee;
               if (!onlyHisSong
                  && (this.active(brain, "dn_giant_fiber", gfRatio, 0.03F) || this.active(brain, "mn_jump", jumpRatio, 0.03F))
                  && this.brainJumpCd <= 0.0) {
                  this.brainJumpCd = 1.5;
                  fly.brainJump();
                  this.brainSay(fly, "[brain] Giant Fiber / TTMn fired: escape jump");
               } else {
                  if (this.active(brain, "mn_wing_power", 3.0F, 0.03F) && !fly.isFlying()) {
                     fly.requestFlight(true);
                     this.brainSay(fly, "[brain] wing power motor neurons: take-off");
                  }

                  if (this.active(brain, "dn_backward", 4.0F, 0.05F) && this.backT <= 0.0 && !fly.isFlying()) {
                     this.backT = 0.5;
                  }

                  boolean strongTouch = this.touchT < 1.5 || this.bus.getOrDefault("tactile", 0.0F) >= 0.9F;
                  if (legBurst && strongTouch && this.scrambleT <= 0.0 && !fly.isFlying() && !flee) {
                     this.scrambleT = 0.6;
                     this.scrambleSide = world.field_9229.method_43056() ? 1 : -1;
                  }

                  if (this.active(brain, "mn_proboscis", 3.0F, 0.05F)) {
                     this.proboscisT = 1.0;
                  }

                  if (this.active(brain, "mn_antenna", 3.0F, 0.05F) && !this.resting && !fly.isFlying() && senseMag < 0.12 && !flee) {
                     this.resting = true;
                     this.grooming = true;
                     this.groomHoldT = 3.0;
                     this.moveT = 0.0;
                     this.brainSay(fly, "[brain] antennal motor neurons: grooming bout");
                  }

                  if (this.active(brain, "dn_egg_laying", 4.0F, 0.05F) && this.eggCd <= 0.0 && !fly.isFlying()) {
                     this.eggCd = 30.0;
                     fly.layEgg(
                        fly.hasMated()
                           ? "[brain] oviDN fired: lays a fertilised egg"
                           : "[brain] oviDN fired: lays an unfertilised egg (she has not mated, so it will not hatch)",
                        fly.hasMated()
                     );
                     this.brainLabel = "[brain] oviDN fired: lays an egg";
                     this.brainLabelT = 1.6;
                  }

                  if (this.backT > 0.0) {
                     this.stopNav(fly);
                     double yawRad = Math.toRadians(fly.method_36454());
                     o = 0.056999999999999995;
                     class_243 v = fly.method_18798();
                     fly.method_18800(Math.sin(yawRad) * o, v.field_1351, -Math.cos(yawRad) * o);
                     fly.field_6037 = true;
                     this.brainSay(fly, "[brain] MDN (moonwalker) fired: walks backward");
                  } else if (this.scrambleT > 0.0) {
                     this.stopNav(fly);
                     fly.method_36456(fly.method_36454() + this.scrambleSide * 9.0F);
                     fly.field_6283 = fly.method_36454();
                     this.drive(fly, 3.2);
                     this.brainSay(fly, "[brain] leg motor neurons burst: scramble (bristle reflex)");
                  } else {
                     if (this.proboscisT > 0.0 && feed == null) {
                        fly.setAnimFlags(false, false, false, true);
                        this.brainSay(fly, "[brain] proboscis motor neurons: extension (no food here)");
                     }

                     this.chainDbg = String.format(
                        Locale.ROOT,
                        "rival=%s d=%.2f aggr=%.2f above=%s flee=%s feed=%s sense=%.2f var=%.5f moving=%s",
                        rival != null,
                        rivalD,
                        this.aggrDrive,
                        this.aboveRate(brain, "dn_aggression", 0.035F),
                        flee,
                        feed != null,
                        senseMag,
                        this.neuralVar,
                        this.neuralMoving
                     );
                     if (flee && domRepel != null) {
                        this.stopNav(fly);
                        fly.requestFlight(true);
                        face(fly, ux - domRepel.x, uz - domRepel.z);
                        this.drive(fly, Math.min(4.8, 2.8 + domRInt * 2.2) * (fly.isFlying() ? 1.2 : 1.0));
                        fly.setAnimFlags(true, false, false, false);
                        this.label(fly, domRepel.info.act + ": " + domRepel.info.ch);
                     } else if (this.asleep) {
                        this.stopNav(fly);
                        fly.requestFlight(false);
                        damp(fly, 0.4);
                        fly.setAnimFlags(false, false, true, false);
                        this.label(
                           fly,
                           String.format(
                              Locale.ROOT,
                              "[brain] asleep: R5 sleep cells %.1f vs l-LNv light cells %.1f (pressure %.2f)",
                              this.r5Rate / 0.42,
                              this.lnvRate / 0.117,
                              this.sleepP
                           )
                        );
                     } else if (this.retreatT > 0.0 && rival != null && rivalD < 4.5) {
                        this.stopNav(fly);
                        fly.requestFlight(false);
                        face(fly, ux - rival.x, uz - rival.z);
                        this.drive(fly, 2.0);
                        this.label(fly, "[model] shoved: she gives ground to the winner (loser effect)");
                     } else if (rival != null
                        && rivalD < 3.5
                        && this.aggrDrive > 0.3
                        && !this.aboveRate(brain, "dn_aggression", 0.035F)
                        && rivalD > 1.2
                        && !fly.isFlying()
                        && this.lungeCd <= 0.0) {
                        this.stopNav(fly);
                        fly.requestFlight(false);
                        face(fly, rival.x - ux, rival.z - uz);
                        this.drive(fly, 1.4);
                        this.label(fly, "[brain] pC1d/e firing: squares up to the rival (DNa11/DNp37 not yet)");
                     } else if (rival != null && rivalD < 2.8 && this.lungeCd <= 0.0 && this.aboveRate(brain, "dn_aggression", 0.035F)) {
                        this.stopNav(fly);
                        fly.requestFlight(false);
                        face(fly, rival.x - ux, rival.z - uz);
                        this.drive(fly, Math.min(4.2, 2.2 + (2.8 - rivalD)));
                        if (rivalD < 1.4 && rival.ent != null) {
                           double aw = Math.atan2(rival.z - uz, rival.x - ux);
                           rival.ent.method_5762(Math.cos(aw) * 0.22, 0.05, Math.sin(aw) * 0.22);
                           if (rival.ent instanceof ConnectomeFlyEntity loser) {
                              loser.mind().shoved();
                           }

                           this.lungeCd = 7.0;
                        }

                        this.brainSay(fly, "[brain] pC1d/e -> DNa11/DNp37 fired: she lunges at the rival");
                        this.label(fly, "[brain] lunges at the rival: pC1d/e -> DNa11/DNp37 firing");
                     } else if (!(this.drinkT > 0.0)
                        && (
                           !(this.tasteWater > 0.0)
                              || !(fly.getThirst() > 6.0F)
                              || fly.isFlying()
                              || !this.active(brain, "mn_proboscis", 2.0F, 0.03F) && !(this.waterContactT > 1.2)
                        )) {
                        if (homeReturn != null) {
                           fly.requestFlight(false);
                           this.stopNav(fly);
                           face(fly, homeReturn.x - ux, homeReturn.z - uz);
                           this.drive(fly, 1.9);
                           this.label(
                              fly,
                              this.homeBySight
                                 ? "[model] her count says home is here: she sees the leaf and walks the last steps"
                                 : String.format(
                                    Locale.ROOT,
                                    "[brain] heading home: EPG bump %.0f deg (amp %.2f), home vector %.1f m",
                                    (Math.toDegrees(this.cmpHeading) + 360.0) % 360.0,
                                    this.cmpAmp,
                                    Math.hypot(this.hvx, this.hvz)
                                 )
                           );
                        } else if (home == null || !this.homeReturning || !this.atHome && !(Math.hypot(this.hvx, this.hvz) <= 0.8)) {
                           if (home != null && !this.homeReturning && this.excT > 0.0 && !fly.isFlying()) {
                              this.stopNav(fly);
                              fly.requestFlight(false);
                              fly.method_36456((float)this.excYaw);
                              fly.field_6283 = fly.method_36454();
                              fly.field_6241 = fly.method_36454();
                              this.drive(fly, 1.5);
                              this.label(
                                 fly,
                                 String.format(
                                    Locale.ROOT,
                                    "[model] excursion: her compass counts the steps (bump %.0f deg, %.1f m out)",
                                    Double.isNaN(this.cmpHeading) ? 0.0 : (Math.toDegrees(this.cmpHeading) + 360.0) % 360.0,
                                    Math.hypot(this.hvx, this.hvz)
                                 )
                              );
                           } else if (home != null && this.homeReturning) {
                              this.stopNav(fly);
                              fly.requestFlight(false);
                              damp(fly, 0.5);
                              this.label(fly, "cannot find home: her compass lost the bump");
                           } else if (roof != null) {
                              fly.requestFlight(Math.hypot(roof.method_10263() + 0.5 - ux, roof.method_10260() + 0.5 - uz) > 2.0);
                              if (fly.isFlying() || !this.walkTo(fly, roof.method_10263() + 0.5, roof.method_10264(), roof.method_10260() + 0.5)) {
                                 this.stopNav(fly);
                                 face(fly, roof.method_10263() + 0.5 - ux, roof.method_10260() + 0.5 - uz);
                                 this.drive(fly, fly.isFlying() ? 3.0 : 2.4);
                              }

                              this.label(fly, "rain: seeking shelter (Ir68a hygrosensors)");
                           } else if (raining && !exposed) {
                              this.stopNav(fly);
                              fly.requestFlight(false);
                              damp(fly, 0.6);
                              this.label(fly, "sheltering: waiting out the rain");
                           } else if (feed != null && this.feedingSuppressed) {
                              this.stopNav(fly);
                              fly.requestFlight(false);
                              damp(fly, 0.5);
                              this.label(fly, "refuses it: bitter GRNs (Gr33a/Gr66a) suppress the proboscis");
                              this.refuseT += 0.05;
                              if (this.refuseT > 2.5) {
                                 this.refuseT = 0.0;
                                 this.rejectT = 40.0;
                                 this.rejectX = feed.x;
                                 this.rejectZ = feed.z;
                                 fly.sayNow("bitter: she leaves it");
                              }
                           } else if (feed != null) {
                              fly.requestFlight(false);
                              this.stopNav(fly);
                              damp(fly, 0.4);
                              fly.eat((float)(0.027500000000000004 * (0.6 + 0.4 * Math.min(2.0, this.ipcAct / 3.0))), 0.04F);
                              this.rewardT = 1.0;
                              this.nutrientT = Math.max(this.nutrientT, 30.0);
                              this.label(fly, "she feeds: " + feed.info.ch + (this.ipcAct > 1.5 ? " · [brain] IPCs firing (insulin)" : ""));
                              this.eaten += 0.05;
                              if (this.eaten >= feed.info.eat) {
                                 this.eaten = 0.0;
                                 consume(world, feed);
                                 fly.eat(0.75F, 1.5F);
                                 this.nutrientT = 60.0;
                                 fly.sayNow("ate it: proboscis extension (Gr5a)");
                              }
                           } else if (senseMag > 0.12) {
                              this.eaten = 0.0;
                              double starving = 1.0 - fly.method_6032() / 10.0;
                              o = domAttract != null ? Math.hypot(domAttract.x - ux, domAttract.z - uz) : (domRepel != null ? 4.0 : 0.0);
                              this.stallT += 0.05;
                              if (this.stallT > 3.0) {
                                 if (Math.hypot(ux - this.stallX, uz - this.stallZ) < 0.7 && o > 2.5) {
                                    this.tornT = 12.0;
                                 }

                                 this.stallT = 0.0;
                                 this.stallX = ux;
                                 this.stallZ = uz;
                              }

                              this.tornT = Math.max(0.0, this.tornT - 0.05);
                              if (this.tornT > 0.0) {
                                 this.stopNav(fly);
                                 fly.requestFlight(false);
                                 damp(fly, 0.6);
                                 this.label(
                                    fly,
                                    "[model] torn: the food pulls, "
                                       + (domRepel != null ? domRepel.info.act.replace("flees ", "").replace("avoids ", "") : "something")
                                       + " pushes: she stays put"
                                 );
                                 return;
                              }

                              fly.requestFlight(fly.isFlying() ? o > 2.5 : o > 6.0);
                              if (fly.isFlying() || domAttract == null || !(o > 1.6) || !this.walkTo(fly, domAttract.x, domAttract.y, domAttract.z)) {
                                 this.stopNav(fly);
                                 face(fly, ax, az);
                                 this.drive(fly, Math.min(3.0, 0.9 + starving * 1.3 + senseMag * 1.2) * (fly.isFlying() ? 1.3 : 1.0));
                              }

                              if (domAttract != null) {
                                 this.label(fly, "tracking the " + domAttract.kind.name().toLowerCase() + ": " + domAttract.info.ch);
                              } else if (domRepel != null) {
                                 this.label(fly, domRepel.info.act + ": " + domRepel.info.ch);
                              } else if (this.socialN > 0) {
                                 this.label(fly, "joins the others: aggregation (Or47b/Or67d pheromone cells)");
                              }
                           } else if (this.plumeStrength > 0.0 && this.plumeSrc != null && fly.getHunger() < 15.0F && !raining && !night) {
                              this.stopNav(fly);
                              double[] w = wind(world);
                              double rx = ux - (this.plumeSrc.method_10263() + 0.5);
                              double rz = uz - (this.plumeSrc.method_10260() + 0.5);
                              double alongx = rx * w[0] + rz * w[1];
                              double cx = rx - alongx * w[0];
                              double cz = rz - alongx * w[1];
                              double dxp = -w[0] - cx * 0.35;
                              double dzp = -w[1] - cz * 0.35;
                              fly.requestFlight(true);
                              face(fly, dxp, dzp);
                              this.drive(fly, 2.6);
                              this.spontFly = true;
                              this.boutAlt = Math.max(0.3, Math.min(this.boutAlt, 1.5));
                              this.forcedFlyT = Math.max(this.forcedFlyT, 1.0);
                              this.label(
                                 fly,
                                 String.format(
                                    Locale.ROOT,
                                    "[model] %s smell on the wind: she turns into it and surges upwind (source about %.0f blocks off)",
                                    this.plumeFruit ? "fruit" : "cake",
                                    Math.hypot(rx, rz)
                                 )
                              );
                           } else if (fanUp != null) {
                              this.stopNav(fly);
                              fly.requestFlight(false);
                              face(fly, fanUp.x - ux, fanUp.z - uz);
                              if (Math.hypot(fanUp.x - ux, fanUp.z - uz) > 2.2) {
                                 this.drive(fly, 1.5);
                                 this.label(fly, "walks upwind: anemotaxis (antennal wind sensors)");
                              } else {
                                 damp(fly, 0.75);
                                 this.label(fly, "at the source, facing the wind: anemotaxis (antennal wind sensors)");
                              }
                           } else {
                              this.stopNav(fly);
                              this.eaten = 0.0;
                              WholeBrain.Loco loco = brain == null ? null : brain.loco();
                              this.forcedFlyT = Math.max(0.0, this.forcedFlyT - 0.05);
                              if (!fly.isFlying()) {
                                 this.airRestT = Math.max(0.0, this.airRestT - 0.05);
                              }

                              if (this.spontFly && (this.forcedFlyT <= 0.0 || !this.outdoors && this.outdoorT <= 0.0 && this.forcedFlyT > 1.5)) {
                                 this.forcedFlyT = Math.min(this.forcedFlyT, 1.5);
                                 if (this.forcedFlyT <= 0.0) {
                                    this.airRestT = this.dispersing
                                       ? 8.0 + world.field_9229.method_43058() * 14.0
                                       : 20.0 + world.field_9229.method_43058() * 40.0;
                                    this.spontFly = false;
                                    this.dispersing = false;
                                 }
                              }

                              this.groomHoldT = Math.max(0.0, this.groomHoldT - 0.05);
                              if (this.groomAfterTouch && this.touchT > 2.0 && !fly.isFlying()) {
                                 this.groomAfterTouch = false;
                                 this.resting = true;
                                 this.neuralMoving = false;
                                 this.grooming = true;
                                 this.groomHoldT = 5.0;
                                 this.moveT = 0.0;
                              }

                              if (loco != null && loco != this.lastLocoBout) {
                                 this.lastLocoBout = loco;
                                 double w = loco.walkDrive;
                                 if (this.neuralSmooth < 0.0) {
                                    this.neuralSmooth = w;
                                    this.neuralBase = w;
                                    this.neuralMoving = false;
                                    this.resting = true;
                                    this.grooming = false;
                                    this.moveT = 0.0;
                                 }

                                 this.neuralSmooth = this.neuralSmooth + (w - this.neuralSmooth) * 0.15;
                                 this.neuralBase = this.neuralBase + (w - this.neuralBase) * (this.neuralN < 120 ? 0.08 : 0.005);
                                 double satiety = fly.getHunger() / 20.0F;
                                 double x = this.neuralSmooth / Math.max(this.neuralBase, 1.0E-6);
                                 this.neuralN++;
                                 if (this.neuralN > 120) {
                                    this.neuralVar = this.neuralVar + ((x - 1.0) * (x - 1.0) - this.neuralVar) * (this.neuralN < 320 ? 0.05 : 0.01);
                                 }

                                 double zx = (x - 1.0) / Math.max(1.0E-4, Math.sqrt(this.neuralVar));
                                 double k = -0.65 + 1.0 * satiety;
                                 k -= 0.4 * Math.min(1.0, (this.clockMout + this.clockEout) / 60.0);
                                 boolean mv = this.neuralN > 200 && (this.neuralMoving ? zx > k - 0.3 : zx > k + 0.3);
                                 this.neuralExcess = Math.max(0.3, Math.min(1.0, (zx - (k - 0.3)) / 1.6));
                                 if (mv != this.neuralMoving && this.groomHoldT <= 0.0) {
                                    this.neuralMoving = mv;
                                    this.resting = !mv;
                                    this.moveT = 0.0;
                                    if (mv
                                       && this.outdoors
                                       && !fly.isFlying()
                                       && !fly.isBabyFly()
                                       && fly.method_5933() == null
                                       && !night
                                       && !raining
                                       && !this.asleep
                                       && this.airRestT <= 0.0) {
                                       double hungerN = 1.0F - fly.getHunger() / 20.0F;
                                       boolean nothingToEat = !this.sated
                                          && hungerN > 0.35
                                          && this.bus.getOrDefault("olf_food", 0.0F) < 0.05F
                                          && this.bus.getOrDefault("olf_fruit", 0.0F) < 0.05F;
                                       double pFly = 0.09
                                          + (nothingToEat ? 0.45 * hungerN : 0.05 * hungerN)
                                          + 0.07 * Math.min(1.0, (this.clockMout + this.clockEout) / 60.0);
                                       if (world.field_9229.method_43058() < pFly) {
                                          this.spontFly = true;
                                          this.dispersing = nothingToEat && hungerN > 0.5;
                                          this.forcedFlyT = this.dispersing
                                             ? 25.0 + world.field_9229.method_43058() * 35.0
                                             : 7.0 + world.field_9229.method_43058() * 14.0;
                                          this.boutAlt = (this.dispersing ? 2.0 : 0.4) + world.field_9229.method_43058() * 3.2;
                                          this.holdYaw = Double.NaN;
                                          this.brainSay(
                                             fly,
                                             this.dispersing
                                                ? "[brain] drive up, hungry, no food on the air: she takes off to search somewhere else"
                                                : "[brain] drive up, open sky: she takes off"
                                          );
                                       }
                                    }

                                    if (mv && !fly.isFlying() && !fly.isPerched() && this.forcedFlyT <= 0.0) {
                                       double side = Math.abs(loco.turnBias) > 0.01 ? Math.signum(loco.turnBias) : (world.field_9229.method_43056() ? 1 : -1);
                                       fly.method_36456(fly.method_36454() + (float)(side * (15.0 + world.field_9229.method_43058() * 60.0)));
                                       fly.field_6283 = fly.method_36454();
                                    }

                                    if (this.resting) {
                                       this.grooming = world.field_9229.method_43058() < 0.35;
                                       this.groomPhase = 0;
                                    }
                                 }
                              }

                              if (this.forcedFlyT > 0.0) {
                                 this.resting = false;
                              }

                              if (fly.noGroundBelow()) {
                                 this.resting = false;
                                 this.forcedFlyT = Math.max(this.forcedFlyT, 0.5);
                              }

                              this.moveT += 0.05;
                              if (this.resting) {
                                 fly.requestFlight(false);
                                 damp(fly, 0.6);
                                 if (this.optoTurn != 0.0) {
                                    fly.method_36456(fly.method_36454() + (float)(this.optoTurn * 45.0 * 0.05));
                                    fly.field_6283 = fly.method_36454();
                                    fly.field_6241 = fly.method_36454();
                                 }

                                 if (this.grooming) {
                                    this.groomPhase = (int)(this.moveT * 1.3 % 3.0);
                                    fly.setAnimFlags(false, true, false, false);
                                    this.label(
                                       fly,
                                       "grooming: "
                                          + new String[]{"front legs over the head and eyes", "hind legs sweep the wings", "hind legs rub each other"}[this.groomPhase]
                                          + " (flies clean constantly)"
                                    );
                                 } else {
                                    this.label(fly, "[brain] descending drive below its average: rests");
                                 }
                              } else if (loco != null) {
                                 fly.requestFlight(this.forcedFlyT > 0.0);
                                 this.smTurn = this.smTurn + (loco.turnBias - this.smTurn) * Math.min(1.0, 0.04000000000000001);
                                 motionOn = this.active(brain, "lc_bar_left", 3.0F, 0.05F);
                                 boolean lcR = this.active(brain, "lc_bar_right", 3.0F, 0.05F);
                                 String flightLabel = null;
                                 if (!fly.isFlying() || !fly.noGroundBelow() || Double.isNaN(this.lmBearing) || !motionOn && !lcR) {
                                    if (!fly.isFlying()) {
                                       this.holdYaw = Double.NaN;
                                       fly.method_36456(
                                          fly.method_36454() + (float)Math.toDegrees(this.smTurn * 2.4 * 0.05) + (float)(this.optoTurn * 45.0 * 0.05)
                                       );
                                    } else {
                                       if (Double.isNaN(this.holdYaw)) {
                                          this.holdYaw = fly.method_36454();
                                       }

                                       if (!Double.isNaN(this.cmpHeading) && this.cmpAmp > 0.3) {
                                          this.holdYaw = Math.toDegrees(Math.atan2(-Math.cos(this.cmpHeading), Math.sin(this.cmpHeading)));
                                       }

                                       this.saccadeT -= 0.05;
                                       if (this.saccadeT <= 0.0) {
                                          this.saccadeT = this.dispersing && this.spontFly
                                             ? 2.5 + world.field_9229.method_43058() * 3.5
                                             : (this.spontFly ? 1.2 + world.field_9229.method_43058() * 2.3 : 0.6 + world.field_9229.method_43058() * 0.9);
                                          if (this.spontFly) {
                                             this.boutAlt = Math.max(0.2, Math.min(5.5, this.boutAlt + (world.field_9229.method_43058() - 0.5) * 1.6));
                                          }

                                          if (Math.abs(loco.turnBias) > 0.12) {
                                             this.holdYaw = class_3532.method_15393(
                                                (float)(this.holdYaw + Math.signum(loco.turnBias) * (70.0 + world.field_9229.method_43058() * 40.0))
                                             );
                                             this.brainSay(fly, "[brain] descending L/R imbalance: a saccade");
                                          }
                                       }

                                       fly.method_36456(
                                          class_3532.method_17821((float)Math.min(1.0, 0.6000000000000001), fly.method_36454(), (float)this.holdYaw)
                                       );
                                       if (fly.noGroundBelow()) {
                                          flightLabel = String.format(
                                             Locale.ROOT,
                                             "[brain] straight over the water: heading held by her compass bump (%.0f deg)",
                                             (this.holdYaw + 360.0) % 360.0
                                          );
                                       }
                                    }
                                 } else {
                                    float yaw = class_3532.method_17821((float)Math.min(1.0, 0.125), fly.method_36454(), (float)this.lmBearing);
                                    fly.method_36456(yaw);
                                    this.holdYaw = yaw;
                                    flightLabel = String.format(
                                       Locale.ROOT,
                                       "[brain] LC15/LC17 fixation cells fire (%s): she flies to the land on the horizon (%.0f m)",
                                       motionOn && lcR ? "both eyes" : (motionOn ? "left eye" : "right eye"),
                                       this.lmDist
                                    );
                                 }

                                 fly.field_6283 = fly.method_36454();
                                 fly.field_6241 = fly.method_36454();
                                 double sp = Math.min(3.0, Math.max(0.0, loco.walkDrive * 0.5)) * this.neuralExcess;
                                 if (fly.courtedSeconds() > 0.0) {
                                    sp *= 0.4;
                                 }

                                 this.drive(fly, !fly.isFlying() || !fly.noGroundBelow() && !(this.forcedFlyT > 0.0) ? sp : Math.max(sp, 2.2));
                                 if (flightLabel == null && fly.isFlying()) {
                                    flightLabel = this.spontFly
                                       ? (
                                          this.dispersing
                                             ? "[brain] search flight: long straight legs on her compass bump, turns from her descending balance"
                                             : "[brain] her own flight: heading held by her compass bump, saccades from her descending balance"
                                       )
                                       : "[brain] in flight: heading held by her compass bump";
                                 }

                                 this.label(fly, flightLabel != null ? flightLabel : "[brain] descending drive above its average: walks");
                              } else {
                                 fly.requestFlight(this.forcedFlyT > 0.0);
                                 damp(fly, 0.8);
                                 this.label(
                                    fly,
                                    brain == null
                                       ? "resting: her brain is queued (" + WholeBrain.running() + " brains running)"
                                       : "waking her brain (158k neurons)..."
                                 );
                              }
                           }
                        } else {
                           this.stopNav(fly);
                           fly.requestFlight(false);
                           damp(fly, 0.5);
                           this.label(fly, this.atHome ? "home on the leaf: the path integrator resets" : "home by her own count: rests");
                        }
                     } else {
                        this.stopNav(fly);
                        fly.requestFlight(false);
                        damp(fly, 0.4);
                        if (this.drinkT <= 0.0) {
                           this.drinkT = 3.0;
                        }

                        this.drinkT -= 0.05;
                        fly.drink(0.125F);
                        this.drinking = true;
                        fly.setAnimFlags(false, false, false, true);
                        this.label(fly, "drinks: ppk28 water GRNs on the labellum, proboscis motor neurons");
                     }
                  }
               }
            }
         }
      }
   }

   private void maleTick(ConnectomeFlyEntity fly, class_3218 world, List<FlyMind.Prop> props, boolean grounded, double dt) {
      ConnectomeFlyEntity her = null;
      double best = 1.0E9;

      for (class_1297 e : world.method_8333(
         fly, fly.method_5829().method_1014(14.0), o -> o instanceof ConnectomeFlyEntity f && f.method_5805() && !f.isMale() && !f.isBabyFly()
      )) {
         double d = fly.method_5858(e);
         if (d < best) {
            best = d;
            her = (ConnectomeFlyEntity)e;
         }
      }

      if (grounded) {
         if (her != null && !fly.courtRefractory()) {
            double d = Math.sqrt(best) / 0.95;
            face(fly, her.method_23317() - fly.method_23317(), her.method_23321() - fly.method_23321());
            if (d > 1.1) {
               fly.requestFlight(d > 2.8);
               this.drive(fly, fly.isFlying() ? 2.4 : 1.7);
               fly.setAnimFlags(false, false, false, false);
               fly.setSinging(false);
               this.label(fly, "approaching her: P1 courtship decision (MaleCNS)");
            } else {
               fly.requestFlight(false);
               damp(fly, 0.6);
               fly.setAnimFlags(false, false, false, false);
               fly.setSinging(true);
               fly.songTick();
               if (!her.isBabyFly() && !fly.isBabyFly()) {
                  her.sungTo(fly, dt);
               }

               this.label(fly, "singing: pIP10 -> song CPG -> 52 wing motor neurons");
            }
         } else {
            fly.requestFlight(false);
            fly.setSinging(false);
            damp(fly, 0.8);
            fly.setAnimFlags(false, false, false, false);
            this.label(fly, her == null ? "" : "rests after mating: courtship refractory");
         }
      }
   }

   private void hazards(ConnectomeFlyEntity fly, class_3218 world, List<FlyMind.Prop> props, boolean rainOnHer, boolean grounded, double dt) {
      double heat = 0.0;
      double cold = 0.0;
      boolean inPond = false;

      for (FlyMind.Prop p : props) {
         double d = Math.max(0.25, Math.hypot(p.x - fly.method_23317(), p.z - fly.method_23321()) / 0.95);
         if (p.kind == FlyMind.Kind.SNOW && d < p.info.contact + 0.2) {
            cold = 0.15;
         } else if (p.kind == FlyMind.Kind.LAVA && d >= 1.5 && d < p.info.range) {
            heat = Math.max(heat, 0.6 * (1.0 - d / p.info.range));
         }
      }

      if (fly.method_5799()) {
         inPond = true;
      }

      this.pondT = inPond ? this.pondT + dt : 0.0;
      double dmg = heat + cold + (inPond && this.pondT > 2.0 ? 0.4 : 0.0) + (rainOnHer ? 0.35 : 0.0);
      this.dmgAcc += dmg * dt;
      if (++this.dmgTick >= 12 && this.dmgAcc > 0.0) {
         this.dmgTick = 0;
         float amount = (float)this.dmgAcc;
         this.dmgAcc = 0.0;
         if (cold > 0.0) {
            fly.method_5643(world.method_48963().method_48836(), amount);
         } else if (inPond && this.pondT > 2.0) {
            fly.method_5643(world.method_48963().method_48824(), amount);
         } else if (heat > 0.0) {
            fly.method_5643(world.method_48963().method_48813(), amount);
         } else {
            fly.method_5643(world.method_48963().method_48830(), amount);
         }

         this.punishT = Math.max(this.punishT, 1.0);
         if (fly.method_29504()) {
            fly.sayNow(cold > 0.0 ? "froze on the snow" : (inPond ? "drowned in the pond" : (heat > 0.0 ? "burned by the heat" : "waterlogged by the rain")));
         }
      }
   }

   private List<FlyMind.Prop> scanBlocks(ConnectomeFlyEntity fly, class_3218 world) {
      List<FlyMind.Prop> out = new ArrayList<>();
      class_2338 c = fly.method_24515();
      long now = world.method_8510();
      class_2339 m = new class_2339();
      int[] perKind = new int[FlyMind.Kind.values().length];
      int down = fly.isFlying() ? Math.min(8, 2 + (int)Math.ceil(this.cruiseAlt) + 1) : 2;

      for (int dy = -down; dy <= 2; dy++) {
         for (int[] off : SCAN_OFFSETS) {
            int dx = off[0];
            int dz = off[1];
            m.method_10103(c.method_10263() + dx, c.method_10264() + dy, c.method_10260() + dz);
            class_2680 st = world.method_8320(m);
            if (!st.method_26215()) {
               FlyMind.Kind k = classify(world, st, m, now);
               if (k != null && perKind[k.ordinal()] < 8) {
                  perKind[k.ordinal()]++;
                  FlyMind.Prop p = new FlyMind.Prop(k, m.method_10263() + 0.5, m.method_10264(), m.method_10260() + 0.5).at(m.method_10062());
                  p.cauldron = st.method_27852(class_2246.field_27097);
                  p.fruit = k == FlyMind.Kind.LURE
                     || st.method_27852(class_2246.field_10545)
                     || st.method_27852(class_2246.field_16999)
                     || st.method_27852(class_2246.field_28675)
                     || st.method_27852(class_2246.field_28676)
                     || st.method_27852(class_2246.field_10302)
                     || st.method_27852(class_2246.field_10261);
                  if (k == FlyMind.Kind.FAN) {
                     p.fanDx = 1.0;
                     p.fanDz = 0.0;

                     for (class_2350 dir : class_2353.field_11062) {
                        if (world.method_8320(m.method_10093(dir)).method_27852(class_2246.field_10576)) {
                           p.fanDx = dir.method_10148();
                           p.fanDz = dir.method_10165();
                           break;
                        }
                     }
                  }

                  out.add(p);
               }
            }
         }
      }

      return out;
   }

   private static FlyMind.Kind classify(class_3218 world, class_2680 st, class_2338 pos, long now) {
      if (st.method_26164(class_3481.field_23799)) {
         return FlyMind.Kind.CO2;
      } else if (!st.method_27852(class_2246.field_10029)
         && !st.method_27852(class_2246.field_28048)
         && (!st.method_27852(class_2246.field_16999) || (Integer)st.method_11654(class_3830.field_17000) >= 2)) {
         if (st.method_27852(class_2246.field_10164) || st.method_27852(class_2246.field_10092) || st.method_26164(class_3481.field_21952)) {
            return FlyMind.Kind.LAVA;
         } else if (st.method_26164(class_3481.field_29823) || st.method_26164(class_3481.field_15467) || st.method_27852(class_2246.field_27879)) {
            return FlyMind.Kind.SNOW;
         } else if (st.method_27852(class_2246.field_27097)) {
            return FlyMind.Kind.DAMP;
         } else if (st.method_27852(class_2246.field_10382)
            || st.method_27852(class_2246.field_10422)
            || st.method_26227().method_15767(class_3486.field_15517)) {
            return FlyMind.Kind.POND;
         } else if (st.method_27852(class_2246.field_37576) || st.method_27852(class_2246.field_10562) || st.method_27852(class_2246.field_37547)) {
            return FlyMind.Kind.DAMP;
         } else if (st.method_26164(class_3481.field_15466)
            || st.method_26164(class_3481.field_36265)
            || st.method_27852(class_2246.field_10114)
            || st.method_27852(class_2246.field_22090)
            || st.method_27852(class_2246.field_10342)) {
            return FlyMind.Kind.DRY;
         } else if (st.method_26204() instanceof class_2363 && (Boolean)st.method_11654(class_2363.field_11105)) {
            return FlyMind.Kind.LAVA;
         } else if (st.method_27852(class_2246.field_27098)) {
            return FlyMind.Kind.LAVA;
         } else if (!st.method_27852(class_2246.field_10183)
            && !st.method_26164(class_3481.field_26984)
            && !st.method_27852(class_2246.field_21211)
            && !st.method_27852(class_2246.field_10545)
            && !st.method_27852(class_2246.field_21212)
            && !st.method_27852(class_2246.field_10424)
            && (!st.method_27852(class_2246.field_16999) || (Integer)st.method_11654(class_3830.field_17000) < 2)
            && (!st.method_27852(class_2246.field_28675) || !(Boolean)st.method_11654(class_5803.field_28688))
            && (!st.method_27852(class_2246.field_28676) || !(Boolean)st.method_11654(class_5803.field_28688))
            && (!st.method_27852(class_2246.field_10302) || (Integer)st.method_11654(class_2282.field_10779) < 2)
            && (!st.method_27852(class_2246.field_17563) || (Integer)st.method_11654(class_3962.field_17565) < 1)
            && (!st.method_27852(class_2246.field_20421) && !st.method_27852(class_2246.field_20422) || (Integer)st.method_11654(class_4481.field_20420) < 3)) {
            if (st.method_27852(class_2246.field_10606)) {
               return FlyMind.Kind.BITTER;
            } else if (st.method_26164(class_3481.field_20339) || st.method_27852(class_2246.field_28677)) {
               return FlyMind.Kind.LURE;
            } else if (st.method_27852(class_2246.field_10402)
               || st.method_27852(class_2246.field_10251)
               || st.method_27852(class_2246.field_10559)
               || st.method_27852(class_2246.field_10580)
               || st.method_27852(class_2246.field_10240)
               || st.method_27852(class_2246.field_10556)) {
               return FlyMind.Kind.GEOSMIN;
            } else if (st.method_27852(class_2246.field_10223) && (Boolean)st.method_11654(class_2387.field_11180)) {
               return FlyMind.Kind.SPEAKER;
            } else if ((st.method_27852(class_2246.field_10179) || st.method_26204() instanceof class_3709) && soundingAt(pos, now)) {
               return FlyMind.Kind.SPEAKER;
            } else if (st.method_27852(class_2246.field_28673) || st.method_27852(class_2246.field_28674)) {
               return FlyMind.Kind.HOME;
            } else if (st.method_27852(class_2246.field_23261)) {
               return FlyMind.Kind.FAN;
            } else {
               return st.method_26213() >= 10 && !st.method_27852(class_2246.field_27098) ? FlyMind.Kind.LIGHT : null;
            }
         } else {
            return FlyMind.Kind.FOOD;
         }
      } else {
         return FlyMind.Kind.PAIN;
      }
   }

   private static boolean isFoodItem(class_1799 s) {
      class_1792 i = s.method_7909();
      return i == class_1802.field_17534
         || i == class_1802.field_16998
         || i == class_1802.field_28659
         || i == class_1802.field_8497
         || i == class_1802.field_8279
         || i == class_1802.field_8229
         || i == class_1802.field_8423
         || i == class_1802.field_20417
         || i == class_1802.field_20414
         || i == class_1802.field_8479
         || i == class_1802.field_8741
         || i == class_1802.field_8463;
   }

   private static boolean isBitterItem(class_1799 s) {
      class_1792 i = s.method_7909();
      return i == class_1802.field_8635
         || i == class_1802.field_8680
         || i == class_1802.field_8711
         || i == class_1802.field_8323
         || i == class_1802.field_8511
         || i == class_1802.field_17515;
   }

   private static boolean isLureItem(class_1799 s) {
      return s.method_31573(class_3489.field_20344) || isFoodItem(s) || s.method_7909() == class_1802.field_8179;
   }

   private List<FlyMind.Prop> scanEntities(ConnectomeFlyEntity fly, class_3218 world) {
      List<FlyMind.Prop> out = new ArrayList<>();
      class_238 box = fly.method_5829().method_1009(12.0, 4.0, 12.0);

      for (class_1297 e : world.method_8335(fly, box)) {
         if (e.method_5805()) {
            FlyMind.Kind k = null;
            if (e instanceof class_1542 it) {
               class_1799 s = it.method_6983();
               if (isFoodItem(s)) {
                  k = FlyMind.Kind.FOOD;
               } else if (isBitterItem(s)) {
                  k = FlyMind.Kind.BITTER;
               }
            } else if (e instanceof ConnectomeFlyEntity f) {
               if (f.isMale() && !fly.isMale()) {
                  k = FlyMind.Kind.MATE;
               }
            } else if (e instanceof class_1657 p) {
               if (p.method_7325() || !isLureItem(p.method_6047()) && !isLureItem(p.method_6079())) {
                  if (!p.method_7325() && p.method_5624()) {
                     k = FlyMind.Kind.SPEAKER;
                  }
               } else {
                  k = FlyMind.Kind.LURE;
               }
            } else if (e instanceof class_1538) {
               k = FlyMind.Kind.SPEAKER;
            } else if (e instanceof class_1588
               || e instanceof class_1493
               || e instanceof class_1451
               || e instanceof class_4019
               || e instanceof class_3701
               || e instanceof class_7102
               || e instanceof class_1428
               || e instanceof class_1453) {
               k = FlyMind.Kind.PREDATOR;
            } else if (e instanceof class_1309 le && !(e instanceof ConnectomeFlyEntity) && fly.method_5858(e) < 6.25) {
               k = FlyMind.Kind.CO2;
            }

            if (k != null) {
               FlyMind.Prop px = new FlyMind.Prop(k, e.method_23317(), e.method_23318(), e.method_23321());
               px.ent = e;
               if (e instanceof class_1542 fi) {
                  class_1792 fit = fi.method_6983().method_7909();
                  px.fruit = fit == class_1802.field_16998
                     || fit == class_1802.field_28659
                     || fit == class_1802.field_8497
                     || fit == class_1802.field_8279
                     || fit == class_1802.field_8463;
               } else if (k == FlyMind.Kind.LURE) {
                  px.fruit = true;
               }

               out.add(px);
            }
         }
      }

      return out;
   }

   private static class_2338 nearestRoof(class_3218 world, class_2338 here) {
      class_2338 best = null;
      double bestD = 1.0E9;
      class_2339 m = new class_2339();

      for (int dx = -8; dx <= 8; dx++) {
         for (int dz = -8; dz <= 8; dz++) {
            for (int dy = -1; dy <= 1; dy++) {
               m.method_10103(here.method_10263() + dx, here.method_10264() + dy, here.method_10260() + dz);
               if (!world.method_8311(m) && world.method_8320(m).method_26215() && world.method_8320(m.method_10074()).method_26212(world, m.method_10074())) {
                  double d = dx * dx + dz * dz;
                  if (d < bestD) {
                     bestD = d;
                     best = m.method_10062();
                  }
               }
            }
         }
      }

      return best;
   }

   private static void consume(class_3218 world, FlyMind.Prop feed) {
      if (feed.ent instanceof class_1542 it) {
         it.method_31472();
      } else if (feed.pos != null) {
         class_2680 st = world.method_8320(feed.pos);
         if (st.method_27852(class_2246.field_10183)) {
            int bites = (Integer)st.method_11654(class_2272.field_10739);
            if (bites < 6) {
               world.method_8501(feed.pos, (class_2680)st.method_11657(class_2272.field_10739, bites + 1));
            } else {
               world.method_8650(feed.pos, false);
            }
         } else if (st.method_27852(class_2246.field_16999)) {
            world.method_8501(feed.pos, (class_2680)st.method_11657(class_3830.field_17000, 1));
         }
      }
   }

   private boolean walkTo(ConnectomeFlyEntity fly, double x, double y, double z) {
      class_1408 nav = fly.method_5942();
      boolean same = Math.abs(x - this.navX) < 0.6 && Math.abs(z - this.navZ) < 0.6;
      if (!same || nav.method_6357() && --this.navRetry <= 0) {
         this.navRetry = 20;
         if (!nav.method_6337(x, y, z, 1.0)) {
            return false;
         }

         this.navX = x;
         this.navZ = z;
      }

      return !nav.method_6357();
   }

   private void stopNav(ConnectomeFlyEntity fly) {
      if (!Double.isNaN(this.navX)) {
         fly.method_5942().method_6340();
         this.navX = this.navZ = Double.NaN;
      }
   }

   public static boolean isHandFood(class_1799 s) {
      return isFoodItem(s) || s.method_31573(class_3489.field_20344);
   }

   private static void face(ConnectomeFlyEntity fly, double dx, double dz) {
      float target = (float)Math.toDegrees(Math.atan2(-dx, dz));
      float yaw = class_3532.method_17821(0.35F, fly.method_36454(), target);
      fly.method_36456(yaw);
      fly.field_6283 = yaw;
      fly.field_6241 = yaw;
   }

   private void drive(ConnectomeFlyEntity fly, double unitsPerSec) {
      if (fly.field_5976 && fly.isPerched()) {
         this.stuckT += 0.05;
         if (this.stuckT > 0.35) {
            float yaw = fly.method_36454() + 150.0F + fly.method_6051().method_43057() * 60.0F;
            fly.method_36456(yaw);
            fly.field_6283 = yaw;
            fly.field_6241 = yaw;
            this.stuckT = 0.0;
         }
      } else if (fly.field_5976) {
         this.stuckT += 0.05;
         if (this.stuckT > 0.35) {
            if (this.wallSide == 0) {
               this.wallSide = fly.method_6051().method_43056() ? 1 : -1;
            }

            float yaw = fly.method_36454() + this.wallSide * (80.0F + fly.method_6051().method_43057() * 40.0F);
            fly.method_36456(yaw);
            fly.field_6283 = yaw;
            fly.field_6241 = yaw;
            this.stuckT = 0.0;
         }
      } else {
         this.stuckT = Math.max(0.0, this.stuckT - 0.05);
         if (this.stuckT == 0.0) {
            this.wallSide = 0;
         }
      }

      double sp = unitsPerSec * 0.95 / 20.0;
      if (fly.isPerched()) {
         class_243 f = fly.surfaceForward().method_1021(sp);
         fly.method_18799(f);
         fly.field_6037 = true;
      } else {
         double yawRad = Math.toRadians(fly.method_36454());
         class_243 v = fly.method_18798();
         fly.method_18800(-Math.sin(yawRad) * sp, v.field_1351, Math.cos(yawRad) * sp);
         fly.field_6037 = true;
      }
   }

   private static void damp(ConnectomeFlyEntity fly, double k) {
      class_243 v = fly.method_18798();
      fly.method_18800(v.field_1352 * k, v.field_1351, v.field_1350 * k);
      fly.field_6037 = true;
   }

   private void busMax(String ch, double v) {
      Float cur = this.bus.get(ch);
      if (cur == null || v > cur.floatValue()) {
         this.bus.put(ch, (float)v);
      }
   }

   private void busAdd(String ch, double v) {
      Float cur = this.bus.get(ch);
      this.bus.put(ch, (float)Math.min(1.0, (cur == null ? 0.0F : cur) + v));
   }

   private void label(ConnectomeFlyEntity fly, String text) {
      if (!text.equals(this.label)) {
         this.label = text;
         fly.sayBehaviour(text);
      }
   }

   static {
      INFO.put(FlyMind.Kind.LAVA, new FlyMind.Info("repel", 5.5, 1.6, 1.4, true, false, 0.0, "Gr28a thermosensors (heat)", "recoils from the heat"));
      INFO.put(FlyMind.Kind.PREDATOR, new FlyMind.Info("repel", 8.0, 2.0, 1.3, true, false, 0.0, "Gr21a/Gr63a CO2 receptors", "flees the predator"));
      INFO.put(FlyMind.Kind.POND, new FlyMind.Info("repel", 5.0, 1.5, 1.8, true, false, 0.0, "wetness aversion (hygro/mechano)", "avoids the water"));
      INFO.put(FlyMind.Kind.FOOD, new FlyMind.Info("attract", 9.0, 1.3, 0.8, false, true, 2.0, "Gr5a sweet + ORNs (odor)", "feeds"));
      INFO.put(FlyMind.Kind.LURE, new FlyMind.Info("attract", 6.5, 1.0, 1.0, false, false, 0.0, "ORNs (odor of the offered treat)", "follows the treat"));
      INFO.put(FlyMind.Kind.MATE, new FlyMind.Info("attract", 10.0, 1.4, 1.0, false, false, 0.0, "pheromone_volatile (courtship)", "courts her"));
      INFO.put(FlyMind.Kind.BITTER, new FlyMind.Info("repel", 1.4, 0.6, 0.8, false, false, 0.0, "bitter GRNs Gr33a/Gr66a (contact)", "rejects the bitter"));
      INFO.put(FlyMind.Kind.SPEAKER, new FlyMind.Info("repel", 7.0, 1.7, 1.0, true, false, 0.0, "Johnston's organ (auditory)", "startles from the sound"));
      INFO.put(FlyMind.Kind.SNOW, new FlyMind.Info("repel", 5.5, 1.4, 1.3, false, false, 0.0, "cooling thermosensors (Ir21a)", "avoids the snow"));
      INFO.put(FlyMind.Kind.LIGHT, new FlyMind.Info("attract", 13.0, 1.1, 1.6, false, false, 0.0, "visual system (phototaxis)", "walks toward the light"));
      INFO.put(FlyMind.Kind.GEOSMIN, new FlyMind.Info("repel", 7.0, 1.8, 1.0, true, false, 0.0, "Or56a: geosmin (innate aversion)", "flees the mould smell"));
      INFO.put(FlyMind.Kind.FAN, new FlyMind.Info("special", 8.5, 0.0, 1.0, false, false, 0.0, "antennal wind sensors (anemotaxis)", "orients upwind"));
      INFO.put(FlyMind.Kind.HOME, new FlyMind.Info("special", 0.0, 0.0, 1.0, false, false, 0.0, "ellipsoid-body compass (path integration)", "heads home"));
      INFO.put(FlyMind.Kind.CO2, new FlyMind.Info("repel", 5.0, 1.2, 1.0, false, false, 0.0, "Gr21a/Gr63a CO2 receptors (smoke plume)", "avoids the CO2 plume"));
      INFO.put(FlyMind.Kind.PAIN, new FlyMind.Info("repel", 2.5, 1.6, 0.9, false, false, 0.0, "nociceptors (spines on contact)", "recoils from the spines"));
      INFO.put(FlyMind.Kind.DAMP, new FlyMind.Info("special", 3.0, 0.0, 1.0, false, false, 0.0, "Ir68a/Ir40a hygrosensors (damp ground)", "feels the damp"));
      INFO.put(FlyMind.Kind.DRY, new FlyMind.Info("special", 3.0, 0.0, 1.0, false, false, 0.0, "Ir93a/Ir25a dry sensors (dry ground)", "leaves the dry ground"));
      List<int[]> offs = new ArrayList<>();

      for (int dx = -8; dx <= 8; dx++) {
         for (int dz = -8; dz <= 8; dz++) {
            offs.add(new int[]{dx, dz});
         }
      }

      offs.sort(Comparator.comparingInt(o -> o[0] * o[0] + o[1] * o[1]));
      SCAN_OFFSETS = offs.toArray(new int[0][]);
      RECENT_SOUNDS = new ArrayList<>();
   }

   public record Info(String effect, double range, double gain, double contact, boolean flee, boolean consume, double eat, String ch, String act) {
   }

   public static enum Kind {
      LAVA,
      PREDATOR,
      POND,
      FOOD,
      LURE,
      MATE,
      BITTER,
      SPEAKER,
      SNOW,
      LIGHT,
      GEOSMIN,
      FAN,
      HOME,
      CO2,
      PAIN,
      DAMP,
      DRY;
   }

   static final class Prop {
      FlyMind.Kind kind;
      FlyMind.Info info;
      double x;
      double y;
      double z;
      class_2338 pos;
      class_1297 ent;
      double fanDx;
      double fanDz;
      boolean cauldron;
      boolean fruit;

      Prop(FlyMind.Kind kind, double x, double y, double z) {
         this.kind = kind;
         this.info = FlyMind.INFO.get(kind);
         this.x = x;
         this.y = y;
         this.z = z;
      }

      FlyMind.Prop at(class_2338 p) {
         this.pos = p;
         return this;
      }
   }
}

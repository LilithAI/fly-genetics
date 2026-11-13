package io.flyproject.connectomefly.entity;

import io.flyproject.connectomefly.ConnectomeFlyMod;
import io.flyproject.connectomefly.lab.ArenaBuilder;
import io.flyproject.connectomefly.lab.LoomStimulus;
import io.flyproject.connectomefly.neural.EscapeBrain;
import io.flyproject.connectomefly.neural.LoomingMath;
import io.flyproject.connectomefly.genetics.GeneticsState;
import io.flyproject.connectomefly.genetics.Genome;
import io.flyproject.connectomefly.neural.WholeBrain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ConnectomeFlyEntity extends PathfinderMob {
   private static final EntityDataAccessor<String> SPEECH = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Byte> BODY = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> SEX = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> ANIM = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<String> HUD = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Byte> SURFACE = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Float> THIRST = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> NEURO_LEGS = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> NEURO_FLIGHT = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.FLOAT);
   public static final float MAX_HUNGER = 20.0F;
   private static final EntityDataAccessor<Integer> GROWTH = SynchedEntityData.defineId(ConnectomeFlyEntity.class, EntityDataSerializers.INT);
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
   // [model] genetics
   private Genome genome;
   private Genome sireGenome;
   private java.util.UUID sireId;
   private float[] bornMemory;
   // [model] selection
   private int adultTicks;
   private float lifeFactor = -1.0F;
   private boolean counted;
   private boolean diedOfAge;
   private boolean deathRecorded;

   private GeneticsState gstate() {
      return GeneticsState.get(((ServerLevel)this.level()).getServer());
   }

   /** [model] this fly's own share of the adult lifespan, 0.8 to 1.2, fixed at birth, not inherited */
   private float lifeFactor() {
      if (this.lifeFactor < 0.0F) {
         this.lifeFactor = 0.8F + 0.4F * new java.util.Random(this.getUUID().getMostSignificantBits() * 31L + this.getUUID().getLeastSignificantBits()).nextFloat();
      }

      return this.lifeFactor;
   }

   public double adultMinutes() {
      return this.adultTicks / 1200.0;
   }

   private void selectionTick() {
      GeneticsState st = this.gstate();
      if (!this.counted) {
         this.counted = true;
         st.recordBirth(this.genome());
      }

      if (st.selection && this.isAlive() && !this.isBabyFly()) {
         this.adultTicks++;
         if (this.adultTicks > st.adultLifeMinutes * 1200.0F * this.lifeFactor()) {
            this.diedOfAge = true;
            this.sayNow("[model] dies of old age");
            this.hurt(this.level().damageSources().generic(), 1000.0F);
         }
      }
   }

   public void die(DamageSource source) {
      super.die(source);
      if (!this.level().isClientSide() && !this.deathRecorded) {
         this.deathRecorded = true;
         String cause = this.diedOfAge
            ? "old age"
            : (
               source.is(DamageTypes.STARVE)
                  ? "starvation"
                  : (source.getEntity() != null && !(source.getEntity() instanceof Player) ? "predator" : (source.is(DamageTypes.GENERIC_KILL) ? "removed" : "other"))
            );
         this.gstate().recordDeath(cause, this.adultMinutes());
         System.out.println(
            "[connectomefly genetics] death: "
               + Genome.shortId(this.getUUID())
               + (this.isMale() ? " male" : " female")
               + " gen "
               + this.genome().generation
               + " cause "
               + cause
               + (source.getEntity() != null ? " (" + source.getEntity().getType().toShortString() + ")" : "")
               + String.format(Locale.ROOT, " adult minutes %.1f", this.adultMinutes())
         );
      }
   }

   /** This fly's inherited tuning. Flies that were spawned rather than hatched become founders (generation 0). */
   public Genome genome() {
      if (this.genome == null) {
         this.genome = Genome.founder(new java.util.Random(this.getUUID().getLeastSignificantBits() ^ this.getUUID().getMostSignificantBits()));
      }

      return this.genome;
   }

   public void setGenome(Genome g) {
      this.genome = g;
   }

   public void setBornMemory(float[] m) {
      this.bornMemory = m;
   }
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
      double best = this.getY() - this.groundBelow();
      double[] out = new double[]{this.getX(), this.groundBelow(), this.getZ(), -1.0};
      if (this.perchCooldown > 0) {
         return out;
      } else {
         MutableBlockPos m = new MutableBlockPos();

         for (int up = 1; up <= 1; up++) {
            m.set((int)Math.floor(this.getX()), (int)Math.floor(this.getY() + this.getBbHeight()) + up, (int)Math.floor(this.getZ()));
            BlockState st = this.level().getBlockState(m);
            if (!st.isAir() && !st.getCollisionShape(this.level(), m).isEmpty() && st.getFluidState().isEmpty()) {
               double d = m.getY() - (this.getY() + this.getBbHeight());
               if (d < best) {
                  best = d;
                  out = new double[]{this.getX(), m.getY() - this.getBbHeight() - 0.02, this.getZ(), Direction.UP.get3DDataValue()};
               }
               break;
            }
         }

         for (Direction d : Plane.HORIZONTAL) {
            for (int k = 1; k <= 1; k++) {
               m.set(
                  (int)Math.floor(this.getX() + d.getStepX() * k),
                  (int)Math.floor(this.getY() + this.getBbHeight() / 2.0F),
                  (int)Math.floor(this.getZ() + d.getStepZ() * k)
               );
               BlockState st = this.level().getBlockState(m);
               if (!st.isAir() && !st.getCollisionShape(this.level(), m).isEmpty() && st.getFluidState().isEmpty()) {
                  if (st.isFaceSturdy(this.level(), m, d.getOpposite())) {
                     double fx = d == Direction.EAST
                        ? m.getX() - this.getBbWidth() / 2.0F - 0.02
                        : (d == Direction.WEST ? m.getX() + 1 + this.getBbWidth() / 2.0F + 0.02 : this.getX());
                     double fz = d == Direction.SOUTH
                        ? m.getZ() - this.getBbWidth() / 2.0F - 0.02
                        : (d == Direction.NORTH ? m.getZ() + 1 + this.getBbWidth() / 2.0F + 0.02 : this.getZ());
                     double dist = Math.hypot(fx - this.getX(), fz - this.getZ());
                     if (dist < best) {
                        best = dist;
                        out = new double[]{fx, this.getY(), fz, d.get3DDataValue()};
                     }
                  }
                  break;
               }
            }
         }

         return out;
      }
   }

   public ConnectomeFlyEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
      super(entityType, world);
   }

   public static Builder createFlyAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.STEP_HEIGHT, 0.5);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(SPEECH, "");
      builder.define(BODY, defaultBody);
      builder.define(SEX, (byte)0);
      builder.define(ANIM, (byte)0);
      builder.define(HUD, "");
      builder.define(HUNGER, 20.0F);
      builder.define(GROWTH, 108000);
      builder.define(NEURO_LEGS, 0);
      builder.define(NEURO_FLIGHT, 0);
      builder.define(SURFACE, (byte)-1);
      builder.define(THIRST, 0.0F);
   }

   public Direction surface() {
      byte s = (Byte)this.entityData.get(SURFACE);
      return s < 0 ? null : Direction.from3DDataValue(s);
   }

   public boolean isPerched() {
      return (Byte)this.entityData.get(SURFACE) >= 0;
   }

   public boolean isStanding() {
      return this.onGround() || this.isPerched();
   }

   public float getThirst() {
      return (Float)this.entityData.get(THIRST);
   }

   public void setThirst(float t) {
      this.entityData.set(THIRST, Mth.clamp(t, 0.0F, 20.0F));
   }

   private void setSurface(Direction d) {
      this.entityData.set(SURFACE, (byte)(d == null ? -1 : d.get3DDataValue()));
   }

   private Direction faceWithin(double reach, Direction only) {
      Direction[] dirs = only != null ? new Direction[]{only} : new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
      double cy = this.getY() + this.getBbHeight() / 2.0F;

      for (Direction d : dirs) {
         double px = this.getX() + d.getStepX() * (this.getBbWidth() / 2.0F + reach);
         double py = cy + d.getStepY() * (this.getBbHeight() / 2.0F + reach);
         double pz = this.getZ() + d.getStepZ() * (this.getBbWidth() / 2.0F + reach);
         BlockPos bp = BlockPos.containing(px, py, pz);
         BlockState st = this.level().getBlockState(bp);
         if (!st.isAir()
            && !st.getCollisionShape(this.level(), bp).isEmpty()
            && st.getFluidState().isEmpty()
            && st.isFaceSturdy(this.level(), bp, d.getOpposite())) {
            return d;
         }
      }

      return null;
   }

   void perch(Direction d) {
      if (PERCH_DEBUG) {
         System.out.printf(Locale.ROOT, "[perch] grab %s at y=%.2f (x %.2f z %.2f)%n", d, this.getY(), this.getX(), this.getZ());
      }

      this.flying = false;
      this.descending = false;
      this.climb = 0.0;
      this.setNoGravity(true);
      this.setDeltaMovement(0.0, 0.0, 0.0);
      this.needsSync = true;
      this.setSurface(d);
      this.pressToSurface();
      this.landTicks = 5;
      this.playSound(SoundEvents.BEE_POLLINATE, 0.15F, 1.9F);
   }

   private void pressToSurface() {
      Direction d = this.surface();
      if (d != null) {
         double cy = this.getY() + this.getBbHeight() / 2.0F;
         BlockPos bp = null;

         for (double reach = 0.05; reach <= 0.75; reach += 0.1) {
            BlockPos cand = BlockPos.containing(
               this.getX() + d.getStepX() * (this.getBbWidth() / 2.0F + reach),
               cy + d.getStepY() * (this.getBbHeight() / 2.0F + reach),
               this.getZ() + d.getStepZ() * (this.getBbWidth() / 2.0F + reach)
            );
            BlockState st = this.level().getBlockState(cand);
            if (!st.isAir() && !st.getCollisionShape(this.level(), cand).isEmpty() && st.getFluidState().isEmpty()) {
               bp = cand;
               break;
            }
         }

         if (bp != null) {
            double eps = 0.02;
            switch (d) {
               case UP:
                  this.setPos(this.getX(), bp.getY() - this.getBbHeight() - eps, this.getZ());
                  break;
               case NORTH:
                  this.setPos(this.getX(), this.getY(), bp.getZ() + 1 + this.getBbWidth() / 2.0F + eps);
                  break;
               case SOUTH:
                  this.setPos(this.getX(), this.getY(), bp.getZ() - this.getBbWidth() / 2.0F - eps);
                  break;
               case WEST:
                  this.setPos(bp.getX() + 1 + this.getBbWidth() / 2.0F + eps, this.getY(), this.getZ());
                  break;
               case EAST:
                  this.setPos(bp.getX() - this.getBbWidth() / 2.0F - eps, this.getY(), this.getZ());
            }
         }
      }
   }

   void unperch(boolean intoFlight) {
      Direction d = this.surface();
      this.perchCooldown = 80;
      if (PERCH_DEBUG) {
         System.out.printf(Locale.ROOT, "[perch] let go of %s at y=%.2f intoFlight=%s flying=%s%n", d, this.getY(), intoFlight, this.flying);
      }

      this.setSurface(null);
      if (intoFlight) {
         this.flying = true;
         this.descending = false;
         this.flapTicks = 10;
         if (d != null) {
            this.setDeltaMovement(-d.getStepX() * 0.12, d == Direction.UP ? -0.05 : 0.1, -d.getStepZ() * 0.12);
            this.needsSync = true;
         }
      } else {
         this.setNoGravity(false);
      }
   }

   public Vec3 surfaceForward() {
      return surfaceForward(this.surface(), this.getYRot());
   }

   public static Vec3 surfaceForward(Direction d, float yaw) {
      double yawRad = Math.toRadians(yaw);
      double fx = -Math.sin(yawRad);
      double fz = Math.cos(yawRad);
      if (d != null && d != Direction.UP) {
         Vec3 along = new Vec3(-d.getStepZ(), 0.0, d.getStepX());
         return along.scale(fx).add(0.0, fz, 0.0);
      } else {
         return new Vec3(fx, 0.0, fz);
      }
   }

   private void surfaceTick() {
      if (this.isVillagerBody()) {
         // a villager body does not walk on walls or ceilings
         if (this.surface() != null) {
            this.entityData.set(SURFACE, (byte)-1);
            this.setNoGravity(false);
         }

         return;
      }

      if (this.perchCooldown > 0) {
         this.perchCooldown--;
      }

      Direction d = this.surface();
      if (d != null) {
         this.setNoGravity(true);
         this.fallDistance = 0.0F;
         if (this.flying || this.prepTicks >= 0) {
            this.unperch(true);
         } else if (this.faceWithin(0.45, d) != null) {
            this.pressToSurface();
         } else {
            if (d != Direction.UP && this.getY() - this.groundBelow() < 0.15) {
               this.unperch(false);
            } else if (d != Direction.UP && this.faceWithin(0.35, Direction.UP) != null) {
               this.setSurface(Direction.UP);
               this.pressToSurface();
            } else {
               this.unperch(true);
            }
         }
      }
   }

   public int getNeuroLegs() {
      return (Integer)this.entityData.get(NEURO_LEGS);
   }

   public int getNeuroFlight() {
      return (Integer)this.entityData.get(NEURO_FLIGHT);
   }

   void setNeuro(int legs, int flight) {
      if ((Integer)this.entityData.get(NEURO_LEGS) != legs) {
         this.entityData.set(NEURO_LEGS, legs);
      }

      if ((Integer)this.entityData.get(NEURO_FLIGHT) != flight) {
         this.entityData.set(NEURO_FLIGHT, flight);
      }
   }

   public FlyMind mind() {
      return this.mind;
   }

   public int getGrowth() {
      return (Integer)this.entityData.get(GROWTH);
   }

   public void setGrowth(int g) {
      this.entityData.set(GROWTH, Mth.clamp(g, 0, 108000));
      this.refreshDimensions();
   }

   /** 26.3: LivingEntity.getScale() is final and multiplies this in; it also scales the hitbox. */
   public float getAgeScale() {
      return 0.5F + 0.5F * (this.getGrowth() / 108000.0F);
   }

   public boolean isBabyFly() {
      return this.getGrowth() < 108000;
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
         this.suitorTick = this.tickCount;
      }
   }

   public ConnectomeFlyEntity suitor() {
      return this.suitor != null && this.suitor.isAlive() && this.tickCount - this.suitorTick < 10 ? this.suitor : null;
   }

   public void acceptMate(ConnectomeFlyEntity male) {
      if (!this.isMale() && !(this.matingT > 0.0) && !(this.courtCd > 0.0) && male != null && male.isAlive()) {
         GeneticsState st = this.gstate();
         if (st.selection && this.getHunger() < st.breedHunger) {
            // [model] fed-only breeding: a hungry female turns him down and he has to start again
            this.courtedT = 0.0;
            this.courtCd = 8.0;
            st.matingsRefusedHungry++;
            st.setDirty();
            this.sayNow("[model] too hungry to breed: she turns him down");
            System.out.println("[connectomefly genetics] mating refused: " + Genome.shortId(this.getUUID()) + String.format(Locale.ROOT, " hunger %.1f below %.1f", this.getHunger(), st.breedHunger));
            return;
         }

         this.courtedT = 0.0;
         this.matingT = 10.0;
         male.matingT = 10.0;
         this.mate = male;
         male.mate = this;
         this.sireGenome = male.genome();
         this.sireId = male.getUUID();
         this.sayNow("[brain] her pC1 cells say yes: she accepts him");
         male.sayNow("mating");
      }
   }

   public void songTick() {
      this.songTick = (this.songTick + 1) % 12;
      if (this.songTick < 8 && this.songTick % 2 == 0) {
         float pitch = 1.7F + 0.1F * (this.songTick / 2);
         this.level()
            .playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.NEUTRAL, 0.45F, pitch);
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
         if (this.mate != null && this.mate.isAlive()) {
            if (this.isMale()) {
               double yawRad = Math.toRadians(this.mate.getYRot());
               double tx = this.mate.getX() + Math.sin(yawRad) * 0.35;
               double tz = this.mate.getZ() - Math.cos(yawRad) * 0.35;
               this.setDeltaMovement((tx - this.getX()) * 0.3, this.getDeltaMovement().y, (tz - this.getZ()) * 0.3);
               this.setYRot(this.mate.getYRot());
               this.yBodyRot = this.mate.getYRot();
            } else {
               this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            }

            this.needsSync = true;
            if (this.tickCount % 20 == 0) {
               ((ServerLevel)this.level()).sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.15, 0.1, 0.15, 0.0);
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
      if (!this.level().isClientSide() && !this.isMale() && !this.isBabyFly()) {
         ServerLevel world = (ServerLevel)this.level();
         GeneticsState st = this.gstate();
         if (st.selection && fertile) {
            net.minecraft.world.phys.AABB capBox = this.getBoundingBox().inflate(st.capRadius);
            int here = world.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, capBox, e -> e.isAlive()).size()
               + world.getEntities(ConnectomeFlyMod.FLY_EGG, capBox, e -> e.isAlive()).size();
            if (here >= st.cap) {
               st.eggsRefusedCap++;
               st.setDirty();
               this.sayNow("[model] no room: " + here + " flies and eggs here, the cap is " + st.cap + ". No egg.");
               System.out.println("[connectomefly genetics] egg refused by cap: " + here + " of " + st.cap);
               return;
            }

            this.setHunger(this.getHunger() - st.eggCost);
         }

         if (fertile) {
            st.eggsLaid++;
            st.setDirty();
         }

         FlyEggEntity egg = (FlyEggEntity)ConnectomeFlyMod.FLY_EGG.create(world, EntitySpawnReason.BREEDING);
         if (egg != null) {
            double yawRad = Math.toRadians(this.getYRot());
            egg.setFertile(fertile);
            egg.setBody(this.getBodyMode());
            if (fertile) {
               egg.setGenome(Genome.child(this.genome(), this.sireGenome, this.getUUID(), this.sireGenome == null ? null : this.sireId, new java.util.Random(world.getRandom().nextLong())));
               if (Genome.inheritMemory && this.whole != null) {
                  float[] m = this.whole.memorySnapshot();
                  if (m != null) {
                     for (int i = 0; i < m.length; i++) {
                        m[i] = 1.0F + (m[i] - 1.0F) * Genome.inheritMemoryFraction;
                     }

                     egg.setMemory(m);
                  }
               }
               // [model] the last mate's genome stays with her (flies store sperm), so later eggs
               // that her brain decides to lay have the same father until she mates again
            }

            egg.snapTo(this.getX() + Math.sin(yawRad) * 0.25, this.getY(), this.getZ() - Math.cos(yawRad) * 0.25, 0.0F, 0.0F);
            world.addFreshEntity(egg);
            world.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.1, this.getZ(), 4, 0.1, 0.05, 0.1, 0.005);
            this.playSound(SoundEvents.TURTLE_LAY_EGG, 0.5F, 1.7F);
            this.sayNow(why);
         }
      }
   }

   private void growthTick() {
      int g = this.getGrowth();
      if (g < 108000) {
         if (this.tickCount % 20 == 0) {
            this.setGrowth(g + Math.round(20.0F * this.gstate().growthSpeed));
         }
      }
   }

   public float getHunger() {
      return (Float)this.entityData.get(HUNGER);
   }

   public void setHunger(float h) {
      this.entityData.set(HUNGER, Mth.clamp(h, 0.0F, 20.0F));
   }

   public void eat(float hunger, float heal) {
      this.setHunger(this.getHunger() + hunger);
      if (heal > 0.0F) {
         this.heal(heal);
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
         float drain = 0.0020833334F * (this.flying ? 2.0F : (this.getDeltaMovement().horizontalDistance() > 0.02 ? 1.3F : 1.0F));
         h -= drain;
         this.setHunger(h);
         if (h >= 19.0F && this.getHealth() < this.getMaxHealth() && this.tickCount % 20 == 0) {
            this.heal(0.5F);
         } else if (h >= 12.0F && this.getHealth() < this.getMaxHealth() && this.tickCount % 60 == 0) {
            this.heal(0.25F);
         } else if (h <= 0.0F && ++this.starveTick >= 80 && (this.getHealth() > 1.0F || this.gstate().selection)) {
            this.starveTick = 0;
            this.hurt(this.level().damageSources().starve(), 0.5F);
         }
      }
   }

   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!this.isMale() && FlyMind.isHandFood(stack)) {
         if (!this.level().isClientSide()) {
            this.eat(6.0F, 1.5F);
            this.playSound(SoundEvents.GENERIC_EAT.value(), 0.6F, 1.7F);
            this.animFeed = true;
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }

            ((ServerLevel)this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.4, this.getZ(), 6, 0.25, 0.2, 0.25, 0.02);
            this.playSound(SoundEvents.GENERIC_EAT.value(), 0.5F, 1.8F);
            this.sayNow("eats from your hand: proboscis extension (Gr5a)");
         }

         return InteractionResult.SUCCESS;
      } else if (!this.isMale() && stack.is(Items.POTION) && stack.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS, net.minecraft.world.item.alchemy.PotionContents.EMPTY).is(Potions.WATER)) {
         if (!this.level().isClientSide()) {
            this.drink(8.0F);
            this.animFeed = true;
            this.playSound(SoundEvents.GENERIC_DRINK.value(), 0.6F, 1.7F);
            if (!player.getAbilities().instabuild) {
               player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            }

            ((ServerLevel)this.level()).sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.3, this.getZ(), 6, 0.2, 0.1, 0.2, 0.02);
            this.sayNow("drinks from your bottle: ppk28 water taste neurons, proboscis out");
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract(player, hand);
      }
   }

   public String getHud() {
      return (String)this.entityData.get(HUD);
   }

   private void hudTick() {
      if (this.tickCount % 4 == 0) {
         StringBuilder sb = new StringBuilder(320);
         String body = this.dragged
            ? "on a lead (being walked)"
            : (
               this.matingT > 0.0
                  ? "mating"
                  : (
                     this.isPerched()
                        ? (this.surface() == Direction.UP ? "hanging from the ceiling" : "on the wall")
                           + (this.getDeltaMovement().length() > 0.02 ? ", walking" : "")
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
                                                            : (
                                                               this.animFeed
                                                                  ? "feeding"
                                                                  : (this.getDeltaMovement().horizontalDistance() > 0.02 ? "walking" : "standing")
                                                            )
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
         ArenaBuilder.Arena box = ArenaBuilder.arenaAt((ServerLevel)this.level(), this.blockPosition());
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

         boolean fired = this.lastTtmnTick != Long.MIN_VALUE && this.level().getGameTime() - this.lastTtmnTick < 40L;
         sb.append("esc=")
            .append(String.format(Locale.ROOT, "LC4/LPLC %d · DN %d · TTMn %s", this.lastSensorySpikes, this.lastDnSpikes, fired ? "FIRED" : "silent"))
            .append(';');
         sb.append("outs=").append(this.mind.brainOuts.replace(';', ',')).append(';');
         sb.append(this.mind.hudExtra());
         sb.append("hp=").append(String.format(Locale.ROOT, "%.1f", this.getHealth())).append(';');
         sb.append("hunger=").append(String.format(Locale.ROOT, "%.1f", this.getHunger())).append(';');
         sb.append("thirst=").append(String.format(Locale.ROOT, "%.1f", this.getThirst())).append(';');
         Genome gn = this.genome();
         GeneticsState gs = this.gstate();
         sb.append("bodykind=").append(this.isVillagerBody() ? "villager" : "fly").append(';');
         sb.append("gen=").append(gn.generation).append(';');
         sb.append("par=").append(gn.mother == null ? "founder (spawned)" : Genome.shortId(gn.mother) + " x " + Genome.shortId(gn.father)).append(';');
         sb.append("genes=").append(gn.top(3)).append(';');
         sb.append("life=")
            .append(
               this.isBabyFly()
                  ? "young, " + Math.round(this.getGrowth() / 1080.0F) + "% grown"
                  : (
                     gs.selection
                        ? String.format(Locale.ROOT, "adult %.0f of about %.0f min", this.adultMinutes(), gs.adultLifeMinutes * this.lifeFactor())
                        : String.format(Locale.ROOT, "adult %.0f min, no ageing (selection off)", this.adultMinutes())
                  )
            )
            .append(';');
         String s = sb.toString();
         if (!s.equals(this.entityData.get(HUD))) {
            this.entityData.set(HUD, s);
         }
      }
   }

   protected void registerGoals() {
   }

   public String getSpeech() {
      return (String)this.entityData.get(SPEECH);
   }

   public byte getBodyMode() {
      return (Byte)this.entityData.get(BODY);
   }

   public void setBodyMode(byte mode) {
      this.entityData.set(BODY, mode);
      this.refreshDimensions();
   }

   /** Body 2: [model] a villager's body driven by the fly's brain. Still this mod's own entity, never a real villager. */
   public static final byte BODY_VILLAGER = 2;

   public boolean isVillagerBody() {
      return this.getBodyMode() == BODY_VILLAGER;
   }

   protected net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose) {
      return this.isVillagerBody() ? net.minecraft.world.entity.EntityDimensions.scalable(0.6F, 1.95F).withEyeHeight(1.62F) : super.getDefaultDimensions(pose);
   }

   public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (BODY.equals(key) || GROWTH.equals(key)) {
         this.refreshDimensions();
      }
   }

   public boolean isMale() {
      return (Byte)this.entityData.get(SEX) != 0;
   }

   public void setMale(boolean male) {
      this.entityData.set(SEX, (byte)(male ? 1 : 0));
   }

   public int animFlags() {
      return (Byte)this.entityData.get(ANIM);
   }

   public boolean isWingFlapping() {
      return (this.animFlags() & 1) != 0;
   }

   public boolean isGrooming() {
      return (this.animFlags() & 2) != 0;
   }

   public boolean isSleeping() {
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
         } else if (!this.flying && this.onGround() && this.prepTicks < 0) {
            this.prepTicks = 2;
         } else if (!this.flying && !this.onGround()) {
            this.flying = true;
         }
      } else if (this.flightTicks <= 0 && this.flying) {
         this.descending = true;
      }
   }

   public boolean perchNearby() {
      if (this.flying && this.perchCooldown <= 0) {
         Direction d = this.faceWithin(0.45, null);
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
      if (this.isVillagerBody()) {
         // the giant-fibre escape still fires; in a villager body it is a startled hop, not a take-off
         if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.42, 0.0));
            this.needsSync = true;
            this.lastTtmnTick = this.level().getGameTime();
         }

         return;
      }

      if (this.onGround() || this.flying) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.55, 0.0));
         this.needsSync = true;
         this.flapTicks = 12;
         this.flying = true;
         this.flightTicks = 50;
         this.lastTtmnTick = this.level().getGameTime();
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
            this.onGround(),
            this.isNoGravity(),
            this.animFlags(),
            this.getDeltaMovement().horizontalDistance(),
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
         + String.format(Locale.ROOT, "pos=%.2f,%.2f,%.2f;yaw=%.0f;", this.getX(), this.getY(), this.getZ(), this.getYRot());
   }

   private void eatSoundTick() {
      if (this.animFeed && this.tickCount % 12 == 0) {
         this.playSound(
            this.mind.isDrinking() ? SoundEvents.GENERIC_DRINK.value() : SoundEvents.GENERIC_EAT.value(), 0.55F, 1.6F + this.getRandom().nextFloat() * 0.3F
         );
      }
   }

   public boolean isDragged() {
      return this.dragged;
   }

   public boolean canBeLeashed(Player player) {
      return !this.isLeashed();
   }

   private void leashTick() {
      this.dragged = false;
      Entity holder = this.getLeashHolder();
      if (holder != null && !this.level().isClientSide()) {
         double dx = holder.getX() - this.getX();
         double dy = holder.getY() - this.getY();
         double dz = holder.getZ() - this.getZ();
         double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (!(d < 2.5)) {
            this.dragged = true;
            if (this.isPerched()) {
               this.unperch(true);
            }

            double k = Math.min(0.32, (d - 2.0) * 0.07) / d;
            boolean lift = dy > 0.8 || !this.onGround() && !this.isInWater();
            if (lift) {
               this.flying = true;
               this.descending = false;
               this.setNoGravity(true);
               this.flapTicks = 4;
               this.setDeltaMovement(dx * k, dy * k + 0.015, dz * k);
            } else {
               this.setDeltaMovement(dx * k, this.getDeltaMovement().y, dz * k);
            }

            this.needsSync = true;
            float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
         }
      }
   }

   private void pushAnimFlags() {
      boolean approach = this.descending || !this.flying && !this.onGround() && this.prepTicks < 0 && this.wasAirborne;
      byte f = (byte)(
         (!this.animFlap && this.flapTicks <= 0 && !this.flying && this.onGround() ? 0 : 1)
            | (this.animGroom ? 2 : 0)
            | (this.animSleep ? 4 : 0)
            | (this.animFeed ? 8 : 0)
            | (this.prepTicks >= 0 ? 16 : 0)
            | (this.landTicks > 0 ? 32 : 0)
            | (this.animSing ? 64 : 0)
            | (approach ? -128 : 0)
      );
      if ((Byte)this.entityData.get(ANIM) != f) {
         this.entityData.set(ANIM, f);
      }
   }

   private double groundBelow() {
      MutableBlockPos m = new MutableBlockPos((int)Math.floor(this.getX()), (int)Math.floor(this.getY()), (int)Math.floor(this.getZ()));

      for (int i = 0; i < 25; i++) {
         BlockState st = this.level().getBlockState(m);
         if (!st.getFluidState().isEmpty()) {
            return -1000000.0;
         }

         if (!st.getCollisionShape(this.level(), m).isEmpty()) {
            return m.getY() + st.getCollisionShape(this.level(), m).max(Axis.Y);
         }

         m.move(0, -1, 0);
      }

      return -1000000.0;
   }

   private void flightTick() {
      if (this.isVillagerBody()) {
         // no wings: whatever the brain's flight outputs ask for, the villager body stays on the ground
         this.flying = false;
         this.flightTicks = 0;
         this.prepTicks = -1;
         this.descending = false;
         this.noGround = false;
         this.setNoGravity(false);
         return;
      }

      this.noGround = this.groundBelow() == -1000000.0;
      if (this.noGround && !this.flying && this.prepTicks < 0 && !this.isMale()) {
         this.flying = true;
         this.descending = false;
         this.flapTicks = 6;
      }

      if (this.prepTicks >= 0) {
         if (this.prepTicks == 0) {
            this.flying = true;
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.18, 0.0));
            this.needsSync = true;
            this.flapTicks = 10;
         }

         this.prepTicks--;
      }

      if (this.flightTicks > 0) {
         this.flightTicks--;
         this.flying = true;
      }

      if (!this.flying && !this.onGround() && this.prepTicks < 0 && this.fallDistance > 0.6F && !this.isInWater()) {
         this.flying = true;
         this.descending = true;
         this.flapTicks = 10;
      }

      this.setNoGravity(this.flying);
      this.fallDistance = 0.0F;
      if (!this.flying || !this.isInWater() && !this.isInLava()) {
         if (this.flying) {
            this.hoverPhase += 0.11;
            Vec3 v = this.getDeltaMovement();
            if (this.descending && this.noGround) {
               this.descending = false;
            }

            double vy;
            if (this.descending) {
               vy = -0.07;
               if (this.tickCount % 3 == 0 && this.perchNearby()) {
                  return;
               }

               if (this.tickCount % 5 == 0) {
                  this.perchTarget = this.nearestFace();
               }

               if (this.perchTarget != null && this.perchTarget[3] >= 0.0) {
                  Direction pd = Direction.from3DDataValue((int)this.perchTarget[3]);
                  if (this.faceWithin(0.5, pd) != null) {
                     this.perch(pd);
                     this.perchTarget = null;
                     return;
                  }

                  double tx = this.perchTarget[0] - this.getX();
                  double ty = this.perchTarget[1] - this.getY();
                  double tz = this.perchTarget[2] - this.getZ();
                  double dist = Math.sqrt(tx * tx + ty * ty + tz * tz);
                  double k = Math.min(0.12, dist) / Math.max(dist, 1.0E-6);
                  this.setDeltaMovement(tx * k, ty * k, tz * k);
                  this.needsSync = true;
                  if (PERCH_DEBUG && this.tickCount % 4 == 0) {
                     System.out
                        .printf(
                           Locale.ROOT,
                           "[perch] steer to %s y=%.2f -> %.2f vy=%.3f vel=%s face=%s%n",
                           pd,
                           this.getY(),
                           this.perchTarget[1],
                           ty * k,
                           this.getDeltaMovement(),
                           this.faceWithin(0.5, pd)
                        );
                  }

                  return;
               }

               if (this.getY() - this.groundBelow() < 0.1 || this.onGround()) {
                  this.flying = false;
                  this.descending = false;
                  this.setNoGravity(false);
                  this.landTicks = 5;
               }
            } else {
               if (this.horizontalCollision && !this.mind.fleeing() && this.perchNearby()) {
                  return;
               }

               if (this.horizontalCollision) {
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
                  vy = Mth.clamp((target - this.getY()) * 0.18, -0.14, 0.2);
               }
            }

            this.setDeltaMovement(v.x, vy, v.z);
            this.needsSync = true;
            if (this.tickCount % 24 == 0) {
               this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEE_LOOP, SoundSource.NEUTRAL, 0.25F, 1.7F);
            }
         }
      } else {
         this.descending = false;
         MutableBlockPos m = new MutableBlockPos((int)Math.floor(this.getX()), (int)Math.floor(this.getY()), (int)Math.floor(this.getZ()));

         for (int up = 0; up < 12 && !this.level().getFluidState(m).isEmpty(); up++) {
            m.move(0, 1, 0);
         }

         Vec3 vx = this.getDeltaMovement();
         this.setDeltaMovement(vx.x * 0.9, 0.22, vx.z * 0.9);
         this.needsSync = true;
         this.flapTicks = 6;
         if (this.tickCount % 10 == 0) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.NEUTRAL, 0.3F, 1.8F);
         }
      }

      boolean airborne = !this.onGround();
      if (!airborne && this.wasAirborne && !this.flying) {
         this.landTicks = 5;
      }

      this.wasAirborne = airborne;
      if (this.landTicks > 0) {
         this.landTicks--;
      }
   }

   protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput nbt) {
      super.addAdditionalSaveData(nbt);
      nbt.putByte("Body", this.getBodyMode());
      nbt.putBoolean("Male", this.isMale());
      nbt.putFloat("Hunger", this.getHunger());
      nbt.putInt("Growth", this.getGrowth());
      nbt.putFloat("Thirst", this.getThirst());
      nbt.putBoolean("Mated", this.mated);
      nbt.putInt("AdultTicks", this.adultTicks);
      nbt.putFloat("LifeFactor", this.lifeFactor());
      nbt.putBoolean("GenomeCounted", this.counted);
      nbt.store("Genome", CompoundTag.CODEC, io.flyproject.connectomefly.genetics.GenomeNbt.write(this.genome()));
      if (this.sireGenome != null) {
         nbt.store("SireGenome", CompoundTag.CODEC, io.flyproject.connectomefly.genetics.GenomeNbt.write(this.sireGenome));
         if (this.sireId != null) {
            nbt.putString("SireId", this.sireId.toString());
         }
      }
   }

   protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput nbt) {
      super.readAdditionalSaveData(nbt);
      this.setBodyMode(nbt.getByteOr("Body", this.getBodyMode()));
      this.setMale(nbt.getBooleanOr("Male", false));
      this.setHunger(nbt.getFloatOr("Hunger", this.getHunger()));
      this.setGrowth(nbt.getIntOr("Growth", this.getGrowth()));
      this.mated = nbt.getBooleanOr("Mated", false);
      this.adultTicks = nbt.getIntOr("AdultTicks", 0);
      this.lifeFactor = nbt.getFloatOr("LifeFactor", this.lifeFactor);
      this.counted = nbt.getBooleanOr("GenomeCounted", false);
      nbt.read("Genome", CompoundTag.CODEC).ifPresent(t -> this.genome = io.flyproject.connectomefly.genetics.GenomeNbt.read(t));
      nbt.read("SireGenome", CompoundTag.CODEC).ifPresent(t -> {
         this.sireGenome = io.flyproject.connectomefly.genetics.GenomeNbt.read(t);
         this.sireId = nbt.getString("SireId").map(java.util.UUID::fromString).orElse(null);
      });
      this.setThirst(nbt.getFloatOr("Thirst", this.getThirst()));
   }

   public void tick() {
      super.tick();
      Level world = this.level();
      if (!world.isClientSide()) {
         if (this.flapTicks > 0) {
            this.flapTicks--;
         }

         if (this.brain == null) {
            this.brain = new EscapeBrain(ConnectomeFlyMod.escapeCircuitData(), ConnectomeFlyMod.steeringCircuitData());
            this.disclose();
         }

         if (this.whole == null && !this.isMale() && WholeBrain.running() < maxBrains) {
            this.whole = new WholeBrain(this.getStringUUID().substring(0, 8), this.genome());
            if (this.bornMemory != null) {
               this.whole.setInitialMemory(this.bornMemory);
               this.bornMemory = null;
            }

            this.whole.start();
         }

         if (this.whole != null) {
            this.whole.keepAlive();
         }

         EscapeBrain.Sensed sensed = this.sense();
         EscapeBrain.Intent intent = this.brain.tick(world.getGameTime(), sensed);
         if (intent.jump && this.isPerched()) {
            this.unperch(true);
         }

         if (intent.jump && (this.onGround() || this.flying)) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.55, 0.0));
            this.needsSync = true;
            this.flapTicks = 12;
            this.flying = true;
            this.flightTicks = 50;
         }

         if (intent.fleeing) {
            float newYaw = this.getYRot() + (float)intent.yawDeltaDeg;
            this.setYRot(newYaw);
            this.yBodyRot = newYaw;
            this.yHeadRot = newYaw;
            double yawRad = Math.toRadians(newYaw);
            double vx = -Math.sin(yawRad) * intent.stepBlocks;
            double vz = Math.cos(yawRad) * intent.stepBlocks;
            Vec3 vel = this.getDeltaMovement();
            this.setDeltaMovement(vx, vel.y, vz);
            this.needsSync = true;
         }

         if (intent.speechCategory != null) {
            this.say(intent.speechCategory, intent.speechThreatName, world.getGameTime());
         }

         this.lastSensorySpikes = intent.sensorySpikes;
         this.lastDnSpikes = intent.dnSpikes;
         this.lastSensed = sensed;
         if (intent.jump) {
            this.lastTtmnTick = world.getGameTime();
         }

         if (this.knockbackTicks > 0) {
            this.knockbackTicks--;
         }

         this.mind.tick(this, (ServerLevel)world, this.whole, intent.jump || intent.fleeing || this.knockbackTicks > 0);
         this.flightTick();
         this.surfaceTick();
         this.hungerTick();
         this.selectionTick();
         this.thirstTick();
         this.growthTick();
         this.matingTick();
         this.eatSoundTick();
         this.pushAnimFlags();
         this.leashTick();
         this.hudTick();
         if (this.speechTicksLeft > 0 && --this.speechTicksLeft == 0) {
            this.entityData.set(SPEECH, "");
         }
      }
   }

   public void remove(RemovalReason reason) {
      super.remove(reason);
      if (this.whole != null) {
         this.whole.stop();
      }
   }

   public boolean canBeAffected(MobEffectInstance effect) {
      return effect.getEffect() != MobEffects.WITHER && super.canBeAffected(effect);
   }

   public boolean hurtServer(ServerLevel hurtLevel, DamageSource source, float amount) {
      if (source.is(DamageTypes.SWEET_BERRY_BUSH) || this.isVillagerBody() && source.getEntity() instanceof net.minecraft.world.entity.animal.frog.Frog) {
         return false;
      } else {
         boolean spines = source.is(DamageTypes.CACTUS);
         if (spines) {
            amount = Math.min(amount, 0.5F);
         }

         boolean r = super.hurtServer(hurtLevel, source, amount);
         if (r && source.getEntity() != null) {
            this.knockbackTicks = 8;
         }

         if (r && spines) {
            this.mind.pricked();
         }

         boolean bodily = source.getEntity() != null
            || !source.is(DamageTypes.STARVE)
               && !source.is(DamageTypes.DROWN)
               && !source.is(DamageTypes.FREEZE)
               && !source.is(DamageTypes.ON_FIRE)
               && !source.is(DamageTypes.IN_FIRE)
               && !source.is(DamageTypes.LAVA)
               && !source.is(DamageTypes.HOT_FLOOR)
               && !source.is(DamageTypes.MAGIC)
               && !source.is(DamageTypes.WITHER)
               && !source.is(DamageTypes.GENERIC_KILL);
         if (r && bodily) {
            this.mind.touched();
         }

         if (r && !source.is(DamageTypes.STARVE)) {
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
      List<Entity> threats = this.level().getEntities(this, this.getBoundingBox().inflate(24.0), ConnectomeFlyEntity::isThreat);
      Entity nearest = null;
      double nearestSq = Double.POSITIVE_INFINITY;

      for (Entity e : threats) {
         double sq = this.distanceToSqr(e);
         if (sq < nearestSq) {
            nearestSq = sq;
            nearest = e;
         }
      }

      LoomStimulus.Loom disc = null;
      double discSq = Double.POSITIVE_INFINITY;

      for (LoomStimulus.Loom l : LoomStimulus.activeFor(this)) {
         double sq = this.distanceToSqr(l.x, l.y, l.z);
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
         double radius = nearest != null ? nearest.getBbWidth() / 2.0 : disc.radius;
         int id = nearest != null ? nearest.getId() : disc.id;
         double theta = LoomingMath.angularSize(dist, radius);
         double loom = 0.0;
         if (id == this.prevThreatId) {
            loom = LoomingMath.translateLooming(this.prevThreatTheta, theta, tickDt);
         }

         this.prevThreatId = id;
         this.prevThreatTheta = theta;
         double dx = (nearest != null ? nearest.getX() : disc.x) - this.getX();
         double dz = (nearest != null ? nearest.getZ() : disc.z) - this.getZ();
         double dy = (nearest != null ? nearest.getEyeY() : disc.y) - this.getEyeY();
         double bearingAbs = Math.toDegrees(Math.atan2(-dx, dz));
         double headingDeg = this.getYRot();
         Direction sf = this.surface();
         if (sf != null && sf != Direction.UP) {
            headingDeg = Math.toDegrees(Math.atan2(sf.getStepX(), -sf.getStepZ()));
         }

         double bearing = Mth.wrapDegrees(bearingAbs - headingDeg);
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

      Vec3 eye = this.getEyePosition();
      double yawRad = Math.toRadians(this.getYRot());
      Vec3 dir = this.isPerched() ? this.surfaceForward() : new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
      HitResult hit = this.level().clip(new ClipContext(eye, eye.add(dir.scale(4.0)), Block.COLLIDER, Fluid.NONE, this));
      double wallDist = hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS ? 4.0 : eye.distanceTo(hit.getLocation());
      double wallTheta = LoomingMath.angularSize(wallDist, 0.5);
      if (!Double.isNaN(this.prevWallTheta)) {
         sensed.wallLoom = LoomingMath.translateLooming(this.prevWallTheta, wallTheta, tickDt);
      }

      this.prevWallTheta = wallTheta;
      return sensed;
   }

   private static boolean isThreat(Entity e) {
      if (!e.isAlive()) {
         return false;
      } else {
         return e instanceof Player player ? !player.isSpectator() : e instanceof Monster || e instanceof Projectile;
      }
   }

   private static String threatName(Entity e) {
      return e instanceof Player player ? player.getGameProfile().name() : e.getType().getDescription().getString();
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
      long now = this.level().getGameTime();
      if (!text.isEmpty() && now - this.lastSpeechTick >= 60L) {
         this.show(text, now);
      }
   }

   void sayNow(String text) {
      this.show(text, this.level().getGameTime());
   }

   private void show(String line, long now) {
      this.entityData.set(SPEECH, line);
      this.speechTicksLeft = 80;
      this.lastSpeechTick = now;
   }

   private void disclose() {
      long now = this.level().getGameTime();
      if (now - lastDisclosureTick >= 200L) {
         lastDisclosureTick = now;

         for (Player p : this.level().players()) {
            if (p.distanceToSqr(this) < 4096.0) {
               p.sendSystemMessage(Component.literal(
                        "connectome-fly: her whole brain (158,262 BANC neurons) and the giant-fibre escape circuit are computed live. Speech is translated, not decoded. /flycredits for details."
                     )
                     .withStyle(ChatFormatting.GRAY));
            }
         }
      }
   }

   public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
      return false;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.BEE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.BEE_DEATH;
   }

   public float getVoicePitch() {
      return 1.6F;
   }

   protected float getSoundVolume() {
      return 0.6F;
   }

   public boolean removeWhenFarAway(double distanceSquared) {
      return false;
   }
}

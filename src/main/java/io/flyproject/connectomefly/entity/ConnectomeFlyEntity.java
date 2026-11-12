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
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class ConnectomeFlyEntity extends PathAwareEntity {
   private static final TrackedData<String> SPEECH = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.STRING);
   private static final TrackedData<Byte> BODY = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.BYTE);
   private static final TrackedData<Byte> SEX = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.BYTE);
   private static final TrackedData<Byte> ANIM = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.BYTE);
   private static final TrackedData<String> HUD = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.STRING);
   private static final TrackedData<Byte> SURFACE = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.BYTE);
   private static final TrackedData<Float> THIRST = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.FLOAT);
   private static final TrackedData<Integer> NEURO_LEGS = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Integer> NEURO_FLIGHT = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Float> HUNGER = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.FLOAT);
   public static final float MAX_HUNGER = 20.0F;
   private static final TrackedData<Integer> GROWTH = DataTracker.registerData(ConnectomeFlyEntity.class, TrackedDataHandlerRegistry.INTEGER);
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
      double best = this.getY() - this.groundBelow();
      double[] out = new double[]{this.getX(), this.groundBelow(), this.getZ(), -1.0};
      if (this.perchCooldown > 0) {
         return out;
      } else {
         Mutable m = new Mutable();

         for (int up = 1; up <= 1; up++) {
            m.set((int)Math.floor(this.getX()), (int)Math.floor(this.getY() + this.getHeight()) + up, (int)Math.floor(this.getZ()));
            BlockState st = this.getWorld().getBlockState(m);
            if (!st.isAir() && !st.getCollisionShape(this.getWorld(), m).isEmpty() && st.getFluidState().isEmpty()) {
               double d = m.getY() - (this.getY() + this.getHeight());
               if (d < best) {
                  best = d;
                  out = new double[]{this.getX(), m.getY() - this.getHeight() - 0.02, this.getZ(), Direction.UP.getId()};
               }
               break;
            }
         }

         for (Direction d : Type.HORIZONTAL) {
            for (int k = 1; k <= 1; k++) {
               m.set(
                  (int)Math.floor(this.getX() + d.getOffsetX() * k),
                  (int)Math.floor(this.getY() + this.getHeight() / 2.0F),
                  (int)Math.floor(this.getZ() + d.getOffsetZ() * k)
               );
               BlockState st = this.getWorld().getBlockState(m);
               if (!st.isAir() && !st.getCollisionShape(this.getWorld(), m).isEmpty() && st.getFluidState().isEmpty()) {
                  if (st.isSideSolidFullSquare(this.getWorld(), m, d.getOpposite())) {
                     double fx = d == Direction.EAST
                        ? m.getX() - this.getWidth() / 2.0F - 0.02
                        : (d == Direction.WEST ? m.getX() + 1 + this.getWidth() / 2.0F + 0.02 : this.getX());
                     double fz = d == Direction.SOUTH
                        ? m.getZ() - this.getWidth() / 2.0F - 0.02
                        : (d == Direction.NORTH ? m.getZ() + 1 + this.getWidth() / 2.0F + 0.02 : this.getZ());
                     double dist = Math.hypot(fx - this.getX(), fz - this.getZ());
                     if (dist < best) {
                        best = dist;
                        out = new double[]{fx, this.getY(), fz, d.getId()};
                     }
                  }
                  break;
               }
            }
         }

         return out;
      }
   }

   public ConnectomeFlyEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setStepHeight(0.5F);
   }

   public static Builder createFlyAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SPEECH, "");
      this.dataTracker.startTracking(BODY, defaultBody);
      this.dataTracker.startTracking(SEX, (byte)0);
      this.dataTracker.startTracking(ANIM, (byte)0);
      this.dataTracker.startTracking(HUD, "");
      this.dataTracker.startTracking(HUNGER, 20.0F);
      this.dataTracker.startTracking(GROWTH, 108000);
      this.dataTracker.startTracking(NEURO_LEGS, 0);
      this.dataTracker.startTracking(NEURO_FLIGHT, 0);
      this.dataTracker.startTracking(SURFACE, (byte)-1);
      this.dataTracker.startTracking(THIRST, 0.0F);
   }

   public Direction surface() {
      byte s = (Byte)this.dataTracker.get(SURFACE);
      return s < 0 ? null : Direction.byId(s);
   }

   public boolean isPerched() {
      return (Byte)this.dataTracker.get(SURFACE) >= 0;
   }

   public boolean isStanding() {
      return this.isOnGround() || this.isPerched();
   }

   public float getThirst() {
      return (Float)this.dataTracker.get(THIRST);
   }

   public void setThirst(float t) {
      this.dataTracker.set(THIRST, MathHelper.clamp(t, 0.0F, 20.0F));
   }

   private void setSurface(Direction d) {
      this.dataTracker.set(SURFACE, (byte)(d == null ? -1 : d.getId()));
   }

   private Direction faceWithin(double reach, Direction only) {
      Direction[] dirs = only != null ? new Direction[]{only} : new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
      double cy = this.getY() + this.getHeight() / 2.0F;

      for (Direction d : dirs) {
         double px = this.getX() + d.getOffsetX() * (this.getWidth() / 2.0F + reach);
         double py = cy + d.getOffsetY() * (this.getHeight() / 2.0F + reach);
         double pz = this.getZ() + d.getOffsetZ() * (this.getWidth() / 2.0F + reach);
         BlockPos bp = BlockPos.ofFloored(px, py, pz);
         BlockState st = this.getWorld().getBlockState(bp);
         if (!st.isAir()
            && !st.getCollisionShape(this.getWorld(), bp).isEmpty()
            && st.getFluidState().isEmpty()
            && st.isSideSolidFullSquare(this.getWorld(), bp, d.getOpposite())) {
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
      this.setVelocity(0.0, 0.0, 0.0);
      this.velocityModified = true;
      this.setSurface(d);
      this.pressToSurface();
      this.landTicks = 5;
      this.playSound(SoundEvents.ENTITY_BEE_POLLINATE, 0.15F, 1.9F);
   }

   private void pressToSurface() {
      Direction d = this.surface();
      if (d != null) {
         double cy = this.getY() + this.getHeight() / 2.0F;
         BlockPos bp = null;

         for (double reach = 0.05; reach <= 0.75; reach += 0.1) {
            BlockPos cand = BlockPos.ofFloored(
               this.getX() + d.getOffsetX() * (this.getWidth() / 2.0F + reach),
               cy + d.getOffsetY() * (this.getHeight() / 2.0F + reach),
               this.getZ() + d.getOffsetZ() * (this.getWidth() / 2.0F + reach)
            );
            BlockState st = this.getWorld().getBlockState(cand);
            if (!st.isAir() && !st.getCollisionShape(this.getWorld(), cand).isEmpty() && st.getFluidState().isEmpty()) {
               bp = cand;
               break;
            }
         }

         if (bp != null) {
            double eps = 0.02;
            switch (d) {
               case UP:
                  this.setPosition(this.getX(), bp.getY() - this.getHeight() - eps, this.getZ());
                  break;
               case NORTH:
                  this.setPosition(this.getX(), this.getY(), bp.getZ() + 1 + this.getWidth() / 2.0F + eps);
                  break;
               case SOUTH:
                  this.setPosition(this.getX(), this.getY(), bp.getZ() - this.getWidth() / 2.0F - eps);
                  break;
               case WEST:
                  this.setPosition(bp.getX() + 1 + this.getWidth() / 2.0F + eps, this.getY(), this.getZ());
                  break;
               case EAST:
                  this.setPosition(bp.getX() - this.getWidth() / 2.0F - eps, this.getY(), this.getZ());
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
            this.setVelocity(-d.getOffsetX() * 0.12, d == Direction.UP ? -0.05 : 0.1, -d.getOffsetZ() * 0.12);
            this.velocityModified = true;
         }
      } else {
         this.setNoGravity(false);
      }
   }

   public Vec3d surfaceForward() {
      return surfaceForward(this.surface(), this.getYaw());
   }

   public static Vec3d surfaceForward(Direction d, float yaw) {
      double yawRad = Math.toRadians(yaw);
      double fx = -Math.sin(yawRad);
      double fz = Math.cos(yawRad);
      if (d != null && d != Direction.UP) {
         Vec3d along = new Vec3d(-d.getOffsetZ(), 0.0, d.getOffsetX());
         return along.multiply(fx).add(0.0, fz, 0.0);
      } else {
         return new Vec3d(fx, 0.0, fz);
      }
   }

   private void surfaceTick() {
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
      return (Integer)this.dataTracker.get(NEURO_LEGS);
   }

   public int getNeuroFlight() {
      return (Integer)this.dataTracker.get(NEURO_FLIGHT);
   }

   void setNeuro(int legs, int flight) {
      if ((Integer)this.dataTracker.get(NEURO_LEGS) != legs) {
         this.dataTracker.set(NEURO_LEGS, legs);
      }

      if ((Integer)this.dataTracker.get(NEURO_FLIGHT) != flight) {
         this.dataTracker.set(NEURO_FLIGHT, flight);
      }
   }

   public FlyMind mind() {
      return this.mind;
   }

   public int getGrowth() {
      return (Integer)this.dataTracker.get(GROWTH);
   }

   public void setGrowth(int g) {
      this.dataTracker.set(GROWTH, MathHelper.clamp(g, 0, 108000));
      this.calculateDimensions();
   }

   public float getScaleFactor() {
      return 0.5F + 0.5F * (this.getGrowth() / 108000.0F);
   }

   public boolean isBabyFly() {
      return this.getGrowth() < 108000;
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return super.getDimensions(pose).scaled(this.getScaleFactor());
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
         this.suitorTick = this.age;
      }
   }

   public ConnectomeFlyEntity suitor() {
      return this.suitor != null && this.suitor.isAlive() && this.age - this.suitorTick < 10 ? this.suitor : null;
   }

   public void acceptMate(ConnectomeFlyEntity male) {
      if (!this.isMale() && !(this.matingT > 0.0) && !(this.courtCd > 0.0) && male != null && male.isAlive()) {
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
         this.getWorld()
            .playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.NEUTRAL, 0.45F, pitch);
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
               double yawRad = Math.toRadians(this.mate.getYaw());
               double tx = this.mate.getX() + Math.sin(yawRad) * 0.35;
               double tz = this.mate.getZ() - Math.cos(yawRad) * 0.35;
               this.setVelocity((tx - this.getX()) * 0.3, this.getVelocity().y, (tz - this.getZ()) * 0.3);
               this.setYaw(this.mate.getYaw());
               this.bodyYaw = this.mate.getYaw();
            } else {
               this.setVelocity(0.0, this.getVelocity().y, 0.0);
            }

            this.velocityModified = true;
            if (this.age % 20 == 0) {
               ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.15, 0.1, 0.15, 0.0);
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
      if (!this.getWorld().isClient && !this.isMale() && !this.isBabyFly()) {
         ServerWorld world = (ServerWorld)this.getWorld();
         FlyEggEntity egg = (FlyEggEntity)ConnectomeFlyMod.FLY_EGG.create(world);
         if (egg != null) {
            double yawRad = Math.toRadians(this.getYaw());
            egg.setFertile(fertile);
            egg.refreshPositionAndAngles(this.getX() + Math.sin(yawRad) * 0.25, this.getY(), this.getZ() - Math.cos(yawRad) * 0.25, 0.0F, 0.0F);
            world.spawnEntity(egg);
            world.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.1, this.getZ(), 4, 0.1, 0.05, 0.1, 0.005);
            this.playSound(SoundEvents.ENTITY_TURTLE_LAY_EGG, 0.5F, 1.7F);
            this.sayNow(why);
         }
      }
   }

   private void growthTick() {
      int g = this.getGrowth();
      if (g < 108000) {
         if (this.age % 20 == 0) {
            this.setGrowth(g + 20);
         }
      }
   }

   public float getHunger() {
      return (Float)this.dataTracker.get(HUNGER);
   }

   public void setHunger(float h) {
      this.dataTracker.set(HUNGER, MathHelper.clamp(h, 0.0F, 20.0F));
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
         float drain = 0.0020833334F * (this.flying ? 2.0F : (this.getVelocity().horizontalLength() > 0.02 ? 1.3F : 1.0F));
         h -= drain;
         this.setHunger(h);
         if (h >= 19.0F && this.getHealth() < this.getMaxHealth() && this.age % 20 == 0) {
            this.heal(0.5F);
         } else if (h >= 12.0F && this.getHealth() < this.getMaxHealth() && this.age % 60 == 0) {
            this.heal(0.25F);
         } else if (h <= 0.0F && ++this.starveTick >= 80 && this.getHealth() > 1.0F) {
            this.starveTick = 0;
            this.damage(this.getWorld().getDamageSources().starve(), 0.5F);
         }
      }
   }

   protected ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack stack = player.getStackInHand(hand);
      if (!this.isMale() && FlyMind.isHandFood(stack)) {
         if (!this.getWorld().isClient) {
            this.eat(6.0F, 1.5F);
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.6F, 1.7F);
            this.animFeed = true;
            if (!player.getAbilities().creativeMode) {
               stack.decrement(1);
            }

            ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.4, this.getZ(), 6, 0.25, 0.2, 0.25, 0.02);
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F, 1.8F);
            this.sayNow("eats from your hand: proboscis extension (Gr5a)");
         }

         return ActionResult.success(this.getWorld().isClient);
      } else if (!this.isMale() && stack.isOf(Items.POTION) && PotionUtil.getPotion(stack) == Potions.WATER) {
         if (!this.getWorld().isClient) {
            this.drink(8.0F);
            this.animFeed = true;
            this.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.6F, 1.7F);
            if (!player.getAbilities().creativeMode) {
               player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            }

            ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.3, this.getZ(), 6, 0.2, 0.1, 0.2, 0.02);
            this.sayNow("drinks from your bottle: ppk28 water taste neurons, proboscis out");
         }

         return ActionResult.success(this.getWorld().isClient);
      } else {
         return super.interactMob(player, hand);
      }
   }

   public String getHud() {
      return (String)this.dataTracker.get(HUD);
   }

   private void hudTick() {
      if (this.age % 4 == 0) {
         StringBuilder sb = new StringBuilder(320);
         String body = this.dragged
            ? "on a lead (being walked)"
            : (
               this.matingT > 0.0
                  ? "mating"
                  : (
                     this.isPerched()
                        ? (this.surface() == Direction.UP ? "hanging from the ceiling" : "on the wall")
                           + (this.getVelocity().length() > 0.02 ? ", walking" : "")
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
                                                                  : (this.getVelocity().horizontalLength() > 0.02 ? "walking" : "standing")
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
         ArenaBuilder.Arena box = ArenaBuilder.arenaAt((ServerWorld)this.getWorld(), this.getBlockPos());
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

         boolean fired = this.lastTtmnTick != Long.MIN_VALUE && this.getWorld().getTime() - this.lastTtmnTick < 40L;
         sb.append("esc=")
            .append(String.format(Locale.ROOT, "LC4/LPLC %d · DN %d · TTMn %s", this.lastSensorySpikes, this.lastDnSpikes, fired ? "FIRED" : "silent"))
            .append(';');
         sb.append("outs=").append(this.mind.brainOuts.replace(';', ',')).append(';');
         sb.append(this.mind.hudExtra());
         sb.append("hp=").append(String.format(Locale.ROOT, "%.1f", this.getHealth())).append(';');
         sb.append("hunger=").append(String.format(Locale.ROOT, "%.1f", this.getHunger())).append(';');
         sb.append("thirst=").append(String.format(Locale.ROOT, "%.1f", this.getThirst())).append(';');
         String s = sb.toString();
         if (!s.equals(this.dataTracker.get(HUD))) {
            this.dataTracker.set(HUD, s);
         }
      }
   }

   protected void initGoals() {
   }

   public String getSpeech() {
      return (String)this.dataTracker.get(SPEECH);
   }

   public byte getBodyMode() {
      return (Byte)this.dataTracker.get(BODY);
   }

   public void setBodyMode(byte mode) {
      this.dataTracker.set(BODY, mode);
   }

   public boolean isMale() {
      return (Byte)this.dataTracker.get(SEX) != 0;
   }

   public void setMale(boolean male) {
      this.dataTracker.set(SEX, (byte)(male ? 1 : 0));
   }

   public int animFlags() {
      return (Byte)this.dataTracker.get(ANIM);
   }

   public boolean isFlapping() {
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
         } else if (!this.flying && this.isOnGround() && this.prepTicks < 0) {
            this.prepTicks = 2;
         } else if (!this.flying && !this.isOnGround()) {
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
      if (this.isOnGround() || this.flying) {
         this.setVelocity(this.getVelocity().add(0.0, 0.55, 0.0));
         this.velocityModified = true;
         this.flapTicks = 12;
         this.flying = true;
         this.flightTicks = 50;
         this.lastTtmnTick = this.getWorld().getTime();
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
            this.isOnGround(),
            this.hasNoGravity(),
            this.animFlags(),
            this.getVelocity().horizontalLength(),
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
         + String.format(Locale.ROOT, "pos=%.2f,%.2f,%.2f;yaw=%.0f;", this.getX(), this.getY(), this.getZ(), this.getYaw());
   }

   private void eatSoundTick() {
      if (this.animFeed && this.age % 12 == 0) {
         this.playSound(
            this.mind.isDrinking() ? SoundEvents.ENTITY_GENERIC_DRINK : SoundEvents.ENTITY_GENERIC_EAT, 0.55F, 1.6F + this.getRandom().nextFloat() * 0.3F
         );
      }
   }

   public boolean isDragged() {
      return this.dragged;
   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return !this.isLeashed();
   }

   private void leashTick() {
      this.dragged = false;
      Entity holder = this.getHoldingEntity();
      if (holder != null && !this.getWorld().isClient) {
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
            boolean lift = dy > 0.8 || !this.isOnGround() && !this.isTouchingWater();
            if (lift) {
               this.flying = true;
               this.descending = false;
               this.setNoGravity(true);
               this.flapTicks = 4;
               this.setVelocity(dx * k, dy * k + 0.015, dz * k);
            } else {
               this.setVelocity(dx * k, this.getVelocity().y, dz * k);
            }

            this.velocityModified = true;
            float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
            this.setYaw(yaw);
            this.bodyYaw = yaw;
            this.headYaw = yaw;
         }
      }
   }

   private void pushAnimFlags() {
      boolean approach = this.descending || !this.flying && !this.isOnGround() && this.prepTicks < 0 && this.wasAirborne;
      byte f = (byte)(
         (!this.animFlap && this.flapTicks <= 0 && !this.flying && this.isOnGround() ? 0 : 1)
            | (this.animGroom ? 2 : 0)
            | (this.animSleep ? 4 : 0)
            | (this.animFeed ? 8 : 0)
            | (this.prepTicks >= 0 ? 16 : 0)
            | (this.landTicks > 0 ? 32 : 0)
            | (this.animSing ? 64 : 0)
            | (approach ? -128 : 0)
      );
      if ((Byte)this.dataTracker.get(ANIM) != f) {
         this.dataTracker.set(ANIM, f);
      }
   }

   private double groundBelow() {
      Mutable m = new Mutable((int)Math.floor(this.getX()), (int)Math.floor(this.getY()), (int)Math.floor(this.getZ()));

      for (int i = 0; i < 25; i++) {
         BlockState st = this.getWorld().getBlockState(m);
         if (!st.getFluidState().isEmpty()) {
            return -1000000.0;
         }

         if (!st.getCollisionShape(this.getWorld(), m).isEmpty()) {
            return m.getY() + st.getCollisionShape(this.getWorld(), m).getMax(Axis.Y);
         }

         m.move(0, -1, 0);
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
            this.setVelocity(this.getVelocity().add(0.0, 0.18, 0.0));
            this.velocityModified = true;
            this.flapTicks = 10;
         }

         this.prepTicks--;
      }

      if (this.flightTicks > 0) {
         this.flightTicks--;
         this.flying = true;
      }

      if (!this.flying && !this.isOnGround() && this.prepTicks < 0 && this.fallDistance > 0.6F && !this.isTouchingWater()) {
         this.flying = true;
         this.descending = true;
         this.flapTicks = 10;
      }

      this.setNoGravity(this.flying);
      this.fallDistance = 0.0F;
      if (!this.flying || !this.isTouchingWater() && !this.isInLava()) {
         if (this.flying) {
            this.hoverPhase += 0.11;
            Vec3d v = this.getVelocity();
            if (this.descending && this.noGround) {
               this.descending = false;
            }

            double vy;
            if (this.descending) {
               vy = -0.07;
               if (this.age % 3 == 0 && this.perchNearby()) {
                  return;
               }

               if (this.age % 5 == 0) {
                  this.perchTarget = this.nearestFace();
               }

               if (this.perchTarget != null && this.perchTarget[3] >= 0.0) {
                  Direction pd = Direction.byId((int)this.perchTarget[3]);
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
                  this.setVelocity(tx * k, ty * k, tz * k);
                  this.velocityModified = true;
                  if (PERCH_DEBUG && this.age % 4 == 0) {
                     System.out
                        .printf(
                           Locale.ROOT,
                           "[perch] steer to %s y=%.2f -> %.2f vy=%.3f vel=%s face=%s%n",
                           pd,
                           this.getY(),
                           this.perchTarget[1],
                           ty * k,
                           this.getVelocity(),
                           this.faceWithin(0.5, pd)
                        );
                  }

                  return;
               }

               if (this.getY() - this.groundBelow() < 0.1 || this.isOnGround()) {
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
                  vy = MathHelper.clamp((target - this.getY()) * 0.18, -0.14, 0.2);
               }
            }

            this.setVelocity(v.x, vy, v.z);
            this.velocityModified = true;
            if (this.age % 24 == 0) {
               this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_BEE_LOOP, SoundCategory.NEUTRAL, 0.25F, 1.7F);
            }
         }
      } else {
         this.descending = false;
         Mutable m = new Mutable((int)Math.floor(this.getX()), (int)Math.floor(this.getY()), (int)Math.floor(this.getZ()));

         for (int up = 0; up < 12 && !this.getWorld().getFluidState(m).isEmpty(); up++) {
            m.move(0, 1, 0);
         }

         Vec3d vx = this.getVelocity();
         this.setVelocity(vx.x * 0.9, 0.22, vx.z * 0.9);
         this.velocityModified = true;
         this.flapTicks = 6;
         if (this.age % 10 == 0) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.NEUTRAL, 0.3F, 1.8F);
         }
      }

      boolean airborne = !this.isOnGround();
      if (!airborne && this.wasAirborne && !this.flying) {
         this.landTicks = 5;
      }

      this.wasAirborne = airborne;
      if (this.landTicks > 0) {
         this.landTicks--;
      }
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putByte("Body", this.getBodyMode());
      nbt.putBoolean("Male", this.isMale());
      nbt.putFloat("Hunger", this.getHunger());
      nbt.putInt("Growth", this.getGrowth());
      nbt.putFloat("Thirst", this.getThirst());
      nbt.putBoolean("Mated", this.mated);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("Body")) {
         this.setBodyMode(nbt.getByte("Body"));
      }

      this.setMale(nbt.getBoolean("Male"));
      if (nbt.contains("Hunger")) {
         this.setHunger(nbt.getFloat("Hunger"));
      }

      if (nbt.contains("Growth")) {
         this.setGrowth(nbt.getInt("Growth"));
      }

      this.mated = nbt.getBoolean("Mated");
      if (nbt.contains("Thirst")) {
         this.setThirst(nbt.getFloat("Thirst"));
      }
   }

   public void tick() {
      super.tick();
      World world = this.getWorld();
      if (!world.isClient) {
         if (this.flapTicks > 0) {
            this.flapTicks--;
         }

         if (this.brain == null) {
            this.brain = new EscapeBrain(ConnectomeFlyMod.escapeCircuitData(), ConnectomeFlyMod.steeringCircuitData());
            this.disclose();
         }

         if (this.whole == null && !this.isMale() && WholeBrain.running() < maxBrains) {
            this.whole = new WholeBrain(this.getUuidAsString().substring(0, 8));
            this.whole.start();
         }

         if (this.whole != null) {
            this.whole.keepAlive();
         }

         EscapeBrain.Sensed sensed = this.sense();
         EscapeBrain.Intent intent = this.brain.tick(world.getTime(), sensed);
         if (intent.jump && this.isPerched()) {
            this.unperch(true);
         }

         if (intent.jump && (this.isOnGround() || this.flying)) {
            this.setVelocity(this.getVelocity().add(0.0, 0.55, 0.0));
            this.velocityModified = true;
            this.flapTicks = 12;
            this.flying = true;
            this.flightTicks = 50;
         }

         if (intent.fleeing) {
            float newYaw = this.getYaw() + (float)intent.yawDeltaDeg;
            this.setYaw(newYaw);
            this.bodyYaw = newYaw;
            this.headYaw = newYaw;
            double yawRad = Math.toRadians(newYaw);
            double vx = -Math.sin(yawRad) * intent.stepBlocks;
            double vz = Math.cos(yawRad) * intent.stepBlocks;
            Vec3d vel = this.getVelocity();
            this.setVelocity(vx, vel.y, vz);
            this.velocityModified = true;
         }

         if (intent.speechCategory != null) {
            this.say(intent.speechCategory, intent.speechThreatName, world.getTime());
         }

         this.lastSensorySpikes = intent.sensorySpikes;
         this.lastDnSpikes = intent.dnSpikes;
         this.lastSensed = sensed;
         if (intent.jump) {
            this.lastTtmnTick = world.getTime();
         }

         if (this.knockbackTicks > 0) {
            this.knockbackTicks--;
         }

         this.mind.tick(this, (ServerWorld)world, this.whole, intent.jump || intent.fleeing || this.knockbackTicks > 0);
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
            this.dataTracker.set(SPEECH, "");
         }
      }
   }

   public void remove(RemovalReason reason) {
      super.remove(reason);
      if (this.whole != null) {
         this.whole.stop();
      }
   }

   public boolean canHaveStatusEffect(StatusEffectInstance effect) {
      return effect.getEffectType() != StatusEffects.WITHER && super.canHaveStatusEffect(effect);
   }

   public boolean damage(DamageSource source, float amount) {
      if (source.isOf(DamageTypes.SWEET_BERRY_BUSH)) {
         return false;
      } else {
         boolean spines = source.isOf(DamageTypes.CACTUS);
         if (spines) {
            amount = Math.min(amount, 0.5F);
         }

         boolean r = super.damage(source, amount);
         if (r && source.getAttacker() != null) {
            this.knockbackTicks = 8;
         }

         if (r && spines) {
            this.mind.pricked();
         }

         boolean bodily = source.getAttacker() != null
            || !source.isOf(DamageTypes.STARVE)
               && !source.isOf(DamageTypes.DROWN)
               && !source.isOf(DamageTypes.FREEZE)
               && !source.isOf(DamageTypes.ON_FIRE)
               && !source.isOf(DamageTypes.IN_FIRE)
               && !source.isOf(DamageTypes.LAVA)
               && !source.isOf(DamageTypes.HOT_FLOOR)
               && !source.isOf(DamageTypes.MAGIC)
               && !source.isOf(DamageTypes.WITHER)
               && !source.isOf(DamageTypes.GENERIC_KILL);
         if (r && bodily) {
            this.mind.touched();
         }

         if (r && !source.isOf(DamageTypes.STARVE)) {
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
      List<Entity> threats = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(24.0), ConnectomeFlyEntity::isThreat);
      Entity nearest = null;
      double nearestSq = Double.POSITIVE_INFINITY;

      for (Entity e : threats) {
         double sq = this.squaredDistanceTo(e);
         if (sq < nearestSq) {
            nearestSq = sq;
            nearest = e;
         }
      }

      LoomStimulus.Loom disc = null;
      double discSq = Double.POSITIVE_INFINITY;

      for (LoomStimulus.Loom l : LoomStimulus.activeFor(this)) {
         double sq = this.squaredDistanceTo(l.x, l.y, l.z);
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
         double radius = nearest != null ? nearest.getWidth() / 2.0 : disc.radius;
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
         double headingDeg = this.getYaw();
         Direction sf = this.surface();
         if (sf != null && sf != Direction.UP) {
            headingDeg = Math.toDegrees(Math.atan2(sf.getOffsetX(), -sf.getOffsetZ()));
         }

         double bearing = MathHelper.wrapDegrees(bearingAbs - headingDeg);
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

      Vec3d eye = this.getEyePos();
      double yawRad = Math.toRadians(this.getYaw());
      Vec3d dir = this.isPerched() ? this.surfaceForward() : new Vec3d(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
      HitResult hit = this.getWorld().raycast(new RaycastContext(eye, eye.add(dir.multiply(4.0)), ShapeType.COLLIDER, FluidHandling.NONE, this));
      double wallDist = hit.getType() == net.minecraft.util.hit.HitResult.Type.MISS ? 4.0 : eye.distanceTo(hit.getPos());
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
         return e instanceof PlayerEntity player ? !player.isSpectator() : e instanceof HostileEntity || e instanceof ProjectileEntity;
      }
   }

   private static String threatName(Entity e) {
      return e instanceof PlayerEntity player ? player.getGameProfile().getName() : e.getType().getName().getString();
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
      long now = this.getWorld().getTime();
      if (!text.isEmpty() && now - this.lastSpeechTick >= 60L) {
         this.show(text, now);
      }
   }

   void sayNow(String text) {
      this.show(text, this.getWorld().getTime());
   }

   private void show(String line, long now) {
      this.dataTracker.set(SPEECH, line);
      this.speechTicksLeft = 80;
      this.lastSpeechTick = now;
   }

   private void disclose() {
      long now = this.getWorld().getTime();
      if (now - lastDisclosureTick >= 200L) {
         lastDisclosureTick = now;

         for (PlayerEntity p : this.getWorld().getPlayers()) {
            if (p.squaredDistanceTo(this) < 4096.0) {
               p.sendMessage(
                  Text.literal(
                        "connectome-fly: her whole brain (158,262 BANC neurons) and the giant-fibre escape circuit are computed live. Speech is translated, not decoded. /flycredits for details."
                     )
                     .formatted(Formatting.GRAY),
                  false
               );
            }
         }
      }
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
      return false;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_BEE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_BEE_DEATH;
   }

   public float getSoundPitch() {
      return 1.6F;
   }

   protected float getSoundVolume() {
      return 0.6F;
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return false;
   }
}

package io.flyproject.connectomefly.entity;

import io.flyproject.connectomefly.ConnectomeFlyMod;
import io.flyproject.connectomefly.genetics.Genome;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FlyEggEntity extends Entity {
   private static final TrackedData<Integer> AGE = DataTracker.registerData(FlyEggEntity.class, TrackedDataHandlerRegistry.INTEGER);
   public static final int HATCH_TICKS = 600;
   private boolean fertile = true;
   // [model] genetics: the child's genome is decided when the egg is laid and travels in the egg
   private Genome genome;
   private float[] memory;

   public void setGenome(Genome g) {
      this.genome = g;
   }

   public Genome genome() {
      return this.genome;
   }

   public void setMemory(float[] m) {
      this.memory = m;
   }

   public FlyEggEntity(EntityType<?> type, World world) {
      super(type, world);
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(AGE, 0);
   }

   public int getEggAge() {
      return (Integer)this.dataTracker.get(AGE);
   }

   public void setFertile(boolean f) {
      this.fertile = f;
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      this.dataTracker.set(AGE, nbt.getInt("EggAge"));
      this.fertile = !nbt.contains("Fertile") || nbt.getBoolean("Fertile");
      if (nbt.contains("Genome")) {
         this.genome = io.flyproject.connectomefly.genetics.GenomeNbt.read(nbt.getCompound("Genome"));
      }

      if (nbt.contains("Memory")) {
         int[] bits = nbt.getIntArray("Memory");
         this.memory = new float[bits.length];

         for (int i = 0; i < bits.length; i++) {
            this.memory[i] = Float.intBitsToFloat(bits[i]);
         }
      }
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putInt("EggAge", this.getEggAge());
      nbt.putBoolean("Fertile", this.fertile);
      if (this.genome != null) {
         nbt.put("Genome", io.flyproject.connectomefly.genetics.GenomeNbt.write(this.genome));
      }

      if (this.memory != null) {
         int[] bits = new int[this.memory.length];

         for (int i = 0; i < bits.length; i++) {
            bits[i] = Float.floatToIntBits(this.memory[i]);
         }

         nbt.putIntArray("Memory", bits);
      }
   }

   public boolean canHit() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (!this.getWorld().isClient && !this.isRemoved()) {
         ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.05, this.getZ(), 6, 0.08, 0.04, 0.08, 0.02);
         this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.NEUTRAL, 0.6F, 1.6F);
         this.discard();
         return true;
      } else {
         return false;
      }
   }

   public void tick() {
      super.tick();
      if (!this.hasNoGravity()) {
         this.setVelocity(this.getVelocity().add(0.0, -0.03, 0.0));
      }

      this.move(MovementType.SELF, this.getVelocity());
      this.setVelocity(this.getVelocity().multiply(0.5, this.isOnGround() ? 0.0 : 0.98, 0.5));
      if (!this.getWorld().isClient) {
         int age = this.getEggAge() + 1;
         this.dataTracker.set(AGE, age);
         if (age >= 600) {
            if (this.fertile) {
               this.hatch();
            } else {
               this.discard();
            }
         }
      }
   }

   private void hatch() {
      ServerWorld world = (ServerWorld)this.getWorld();
      world.spawnParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1, this.getZ(), 8, 0.1, 0.05, 0.1, 0.01);
      world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_TURTLE_EGG_HATCH, SoundCategory.NEUTRAL, 0.7F, 1.5F);
      ConnectomeFlyEntity baby = (ConnectomeFlyEntity)ConnectomeFlyMod.CONNECTOME_FLY.create(world);
      if (baby != null) {
         baby.setMale(world.random.nextBoolean());
         baby.setBodyMode(ConnectomeFlyEntity.defaultBody);
         baby.setGrowth(0);
         if (this.genome != null) {
            baby.setGenome(this.genome);
         }

         if (this.memory != null) {
            baby.setBornMemory(this.memory);
         }

         baby.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
         baby.initialize(world, world.getLocalDifficulty(this.getBlockPos()), SpawnReason.BREEDING, null, null);
         world.spawnEntity(baby);
      }

      this.discard();
   }

   public boolean isPushable() {
      return false;
   }

   public Vec3d getLeashOffset() {
      return Vec3d.ZERO;
   }
}

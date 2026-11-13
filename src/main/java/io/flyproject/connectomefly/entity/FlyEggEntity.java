package io.flyproject.connectomefly.entity;

import io.flyproject.connectomefly.ConnectomeFlyMod;
import io.flyproject.connectomefly.genetics.Genome;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FlyEggEntity extends Entity {
   private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(FlyEggEntity.class, EntityDataSerializers.INT);
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

   public FlyEggEntity(EntityType<?> type, Level world) {
      super(type, world);
   }

   protected void defineSynchedData() {
      this.entityData.define(AGE, 0);
   }

   public int getEggAge() {
      return (Integer)this.entityData.get(AGE);
   }

   public void setFertile(boolean f) {
      this.fertile = f;
   }

   protected void readAdditionalSaveData(CompoundTag nbt) {
      this.entityData.set(AGE, nbt.getInt("EggAge"));
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

   protected void addAdditionalSaveData(CompoundTag nbt) {
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

   public boolean isPickable() {
      return true;
   }

   public boolean hurt(DamageSource source, float amount) {
      if (!this.level().isClientSide && !this.isRemoved()) {
         ((ServerLevel)this.level()).sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.05, this.getZ(), 6, 0.08, 0.04, 0.08, 0.02);
         this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TURTLE_EGG_BREAK, SoundSource.NEUTRAL, 0.6F, 1.6F);
         this.discard();
         return true;
      } else {
         return false;
      }
   }

   public void tick() {
      super.tick();
      if (!this.isNoGravity()) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
      }

      this.move(MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, this.onGround() ? 0.0 : 0.98, 0.5));
      if (!this.level().isClientSide) {
         int age = this.getEggAge() + 1;
         this.entityData.set(AGE, age);
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
      ServerLevel world = (ServerLevel)this.level();
      world.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1, this.getZ(), 8, 0.1, 0.05, 0.1, 0.01);
      world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TURTLE_EGG_HATCH, SoundSource.NEUTRAL, 0.7F, 1.5F);
      ConnectomeFlyEntity baby = (ConnectomeFlyEntity)ConnectomeFlyMod.CONNECTOME_FLY.create(world);
      if (baby != null) {
         boolean male = world.random.nextBoolean();
         io.flyproject.connectomefly.genetics.GeneticsState st = io.flyproject.connectomefly.genetics.GeneticsState.get(world.getServer());
         if (st.selection && st.sexBalance) {
            // [model] not biology: stops a small population dying out from a run of coin flips
            int males = 0;
            int females = 0;

            for (ConnectomeFlyEntity f : world.getEntities(ConnectomeFlyMod.CONNECTOME_FLY, this.getBoundingBox().inflate(st.capRadius), e -> e.isAlive())) {
               if (f.isMale()) {
                  males++;
               } else {
                  females++;
               }
            }

            boolean forced = males * 2 < females || females * 2 < males;
            if (forced) {
               boolean want = males < females;
               if (want != male) {
                  st.sexBalanced++;
                  st.setDirty();
                  System.out.println("[connectomefly genetics] sex balance: " + males + " males, " + females + " females nearby, hatchling made " + (want ? "male" : "female"));
               }

               male = want;
            }
         }

         baby.setMale(male);
         baby.setBodyMode(ConnectomeFlyEntity.defaultBody);
         baby.setGrowth(0);
         if (this.genome != null) {
            baby.setGenome(this.genome);
         }

         if (this.memory != null) {
            baby.setBornMemory(this.memory);
         }

         baby.moveTo(this.getX(), this.getY(), this.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
         baby.finalizeSpawn(world, world.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.BREEDING, null, null);
         world.addFreshEntity(baby);
      }

      this.discard();
   }

   public boolean isPushable() {
      return false;
   }

   public Vec3 getLeashOffset() {
      return Vec3.ZERO;
   }
}

package io.flyproject.connectomefly.entity;

import io.flyproject.connectomefly.ConnectomeFlyMod;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1313;
import net.minecraft.class_1937;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_3218;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3730;

public class FlyEggEntity extends class_1297 {
   private static final class_2940<Integer> AGE = class_2945.method_12791(FlyEggEntity.class, class_2943.field_13327);
   public static final int HATCH_TICKS = 600;
   private boolean fertile = true;

   public FlyEggEntity(class_1299<?> type, class_1937 world) {
      super(type, world);
   }

   protected void method_5693() {
      this.field_6011.method_12784(AGE, 0);
   }

   public int getEggAge() {
      return (Integer)this.field_6011.method_12789(AGE);
   }

   public void setFertile(boolean f) {
      this.fertile = f;
   }

   protected void method_5749(class_2487 nbt) {
      this.field_6011.method_12778(AGE, nbt.method_10550("EggAge"));
      this.fertile = !nbt.method_10545("Fertile") || nbt.method_10577("Fertile");
   }

   protected void method_5652(class_2487 nbt) {
      nbt.method_10569("EggAge", this.getEggAge());
      nbt.method_10556("Fertile", this.fertile);
   }

   public boolean method_5863() {
      return true;
   }

   public boolean method_5643(class_1282 source, float amount) {
      if (!this.method_37908().field_9236 && !this.method_31481()) {
         ((class_3218)this.method_37908())
            .method_14199(class_2398.field_11246, this.method_23317(), this.method_23318() + 0.05, this.method_23321(), 6, 0.08, 0.04, 0.08, 0.02);
         this.method_37908()
            .method_43128(null, this.method_23317(), this.method_23318(), this.method_23321(), class_3417.field_14687, class_3419.field_15254, 0.6F, 1.6F);
         this.method_31472();
         return true;
      } else {
         return false;
      }
   }

   public void method_5773() {
      super.method_5773();
      if (!this.method_5740()) {
         this.method_18799(this.method_18798().method_1031(0.0, -0.03, 0.0));
      }

      this.method_5784(class_1313.field_6308, this.method_18798());
      this.method_18799(this.method_18798().method_18805(0.5, this.method_24828() ? 0.0 : 0.98, 0.5));
      if (!this.method_37908().field_9236) {
         int age = this.getEggAge() + 1;
         this.field_6011.method_12778(AGE, age);
         if (age >= 600) {
            if (this.fertile) {
               this.hatch();
            } else {
               this.method_31472();
            }
         }
      }
   }

   private void hatch() {
      class_3218 world = (class_3218)this.method_37908();
      world.method_14199(class_2398.field_11204, this.method_23317(), this.method_23318() + 0.1, this.method_23321(), 8, 0.1, 0.05, 0.1, 0.01);
      world.method_43128(null, this.method_23317(), this.method_23318(), this.method_23321(), class_3417.field_14902, class_3419.field_15254, 0.7F, 1.5F);
      ConnectomeFlyEntity baby = (ConnectomeFlyEntity)ConnectomeFlyMod.CONNECTOME_FLY.method_5883(world);
      if (baby != null) {
         baby.setMale(world.field_9229.method_43056());
         baby.setBodyMode(ConnectomeFlyEntity.defaultBody);
         baby.setGrowth(0);
         baby.method_5808(this.method_23317(), this.method_23318(), this.method_23321(), world.field_9229.method_43057() * 360.0F, 0.0F);
         baby.method_5943(world, world.method_8404(this.method_24515()), class_3730.field_16466, null, null);
         world.method_8649(baby);
      }

      this.method_31472();
   }

   public boolean method_5810() {
      return false;
   }

   public class_243 method_29919() {
      return class_243.field_1353;
   }
}

package io.flyproject.connectomefly.client;

import io.flyproject.connectomefly.entity.FlyEggEntity;
import net.minecraft.class_1059;
import net.minecraft.class_2246;
import net.minecraft.class_2542;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import net.minecraft.class_897;
import net.minecraft.class_5617.class_5618;

public class FlyEggRenderer extends class_897<FlyEggEntity> {
   private static final float TINT_R = 1.0F;
   private static final float TINT_G = 0.94F;
   private static final float TINT_B = 0.72F;

   public FlyEggRenderer(class_5618 ctx) {
      super(ctx);
      this.field_4673 = 0.1F;
   }

   public class_2960 getTexture(FlyEggEntity entity) {
      return class_1059.field_5275;
   }

   public void render(FlyEggEntity entity, float yaw, float tickDelta, class_4587 matrices, class_4597 vertexConsumers, int light) {
      int stage = Math.min(2, entity.getEggAge() * 3 / 600);
      class_2680 state = (class_2680)((class_2680)class_2246.field_10195.method_9564().method_11657(class_2542.field_11710, 1))
         .method_11657(class_2542.field_11711, stage);
      matrices.method_22903();
      float wobble = entity.getEggAge() > 540 ? (float)Math.sin((entity.field_6012 + tickDelta) * 1.4) * 6.0F : 0.0F;
      matrices.method_22907(class_7833.field_40716.rotationDegrees(entity.method_5628() * 37.0F % 360.0F));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(wobble));
      matrices.method_22905(0.7F, 0.7F, 0.7F);
      matrices.method_22904(-0.5, 0.0, -0.5);
      class_4597 tinted = layer -> new FlyEggRenderer.Tinted(vertexConsumers.getBuffer(layer));
      class_310.method_1551().method_1541().method_3353(state, matrices, tinted, light, class_4608.field_21444);
      matrices.method_22909();
      super.method_3936(entity, yaw, tickDelta, matrices, vertexConsumers, light);
   }

   private record Tinted(class_4588 in) implements class_4588 {
      public class_4588 method_22912(double x, double y, double z) {
         this.in.method_22912(x, y, z);
         return this;
      }

      public class_4588 method_1336(int r, int g, int b, int a) {
         this.in.method_1336((int)(r * 1.0F), (int)(g * 0.94F), (int)(b * 0.72F), a);
         return this;
      }

      public class_4588 method_22913(float u, float v) {
         this.in.method_22913(u, v);
         return this;
      }

      public class_4588 method_22917(int u, int v) {
         this.in.method_22917(u, v);
         return this;
      }

      public class_4588 method_22921(int u, int v) {
         this.in.method_22921(u, v);
         return this;
      }

      public class_4588 method_22914(float x, float y, float z) {
         this.in.method_22914(x, y, z);
         return this;
      }

      public void method_1344() {
         this.in.method_1344();
      }

      public void method_22901(int r, int g, int b, int a) {
         this.in.method_22901((int)(r * 1.0F), (int)(g * 0.94F), (int)(b * 0.72F), a);
      }

      public void method_35666() {
         this.in.method_35666();
      }
   }
}

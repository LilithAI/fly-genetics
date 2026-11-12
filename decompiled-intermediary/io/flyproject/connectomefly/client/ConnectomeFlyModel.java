package io.flyproject.connectomefly.client;

import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5603;
import net.minecraft.class_5605;
import net.minecraft.class_5606;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_583;
import net.minecraft.class_630;

public class ConnectomeFlyModel extends class_583<ConnectomeFlyEntity> {
   private final class_630 root;
   private final class_630 head;
   private final class_630 leftWing;
   private final class_630 rightWing;

   public ConnectomeFlyModel(class_630 root) {
      this.root = root;
      class_630 body = root.method_32086("body");
      this.head = body.method_32086("head");
      this.leftWing = body.method_32086("left_wing");
      this.rightWing = body.method_32086("right_wing");
   }

   public static class_5607 getTexturedModelData() {
      class_5609 modelData = new class_5609();
      class_5610 modelRoot = modelData.method_32111();
      class_5610 body = modelRoot.method_32117(
         "body", class_5606.method_32108().method_32101(0, 0).method_32097(-2.0F, -1.5F, -1.0F, 4.0F, 3.0F, 5.0F), class_5603.method_32090(0.0F, 21.0F, 0.0F)
      );
      body.method_32117("thorax", class_5606.method_32108().method_32101(0, 8).method_32097(-1.5F, -1.5F, -3.5F, 3.0F, 3.0F, 3.0F), class_5603.field_27701);
      body.method_32117(
         "head", class_5606.method_32108().method_32101(18, 0).method_32097(-1.5F, -1.0F, -2.0F, 3.0F, 2.0F, 2.0F), class_5603.method_32090(0.0F, -0.5F, -3.5F)
      );
      body.method_32117(
         "left_wing",
         class_5606.method_32108().method_32101(32, 0).method_32098(0.0F, 0.0F, 0.0F, 5.0F, 0.0F, 7.0F, new class_5605(0.0F)),
         class_5603.method_32090(1.0F, -1.5F, -2.5F)
      );
      body.method_32117(
         "right_wing",
         class_5606.method_32108().method_32101(32, 8).method_32096().method_32098(-5.0F, 0.0F, 0.0F, 5.0F, 0.0F, 7.0F, new class_5605(0.0F)),
         class_5603.method_32090(-1.0F, -1.5F, -2.5F)
      );

      for (int i = 0; i < 3; i++) {
         float z = -3.0F + i * 1.5F;
         body.method_32117(
            "left_leg_" + i,
            class_5606.method_32108().method_32101(0, 14).method_32097(0.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F),
            class_5603.method_32091(1.2F, 1.0F, z, 0.0F, 0.0F, -0.5F)
         );
         body.method_32117(
            "right_leg_" + i,
            class_5606.method_32108().method_32101(0, 14).method_32097(-1.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F),
            class_5603.method_32091(-1.2F, 1.0F, z, 0.0F, 0.0F, 0.5F)
         );
      }

      return class_5607.method_32110(modelData, 64, 64);
   }

   public void setAngles(ConnectomeFlyEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.head.field_3675 = headYaw * (float) (Math.PI / 180.0);
      this.head.field_3654 = headPitch * (float) (Math.PI / 180.0);
      float flap = class_3532.method_15374(animationProgress * 2.1F) * 0.55F;
      this.leftWing.field_3674 = 0.25F + flap;
      this.rightWing.field_3674 = -0.25F - flap;
   }

   public void method_2828(class_4587 matrices, class_4588 vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.root.method_22699(matrices, vertices, light, overlay, red, green, blue, alpha);
   }
}

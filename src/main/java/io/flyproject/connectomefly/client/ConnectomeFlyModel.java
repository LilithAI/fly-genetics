package io.flyproject.connectomefly.client;

import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class ConnectomeFlyModel extends EntityModel<ConnectomeFlyEntity> {
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart leftWing;
   private final ModelPart rightWing;

   public ConnectomeFlyModel(ModelPart root) {
      this.root = root;
      ModelPart body = root.getChild("body");
      this.head = body.getChild("head");
      this.leftWing = body.getChild("left_wing");
      this.rightWing = body.getChild("right_wing");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData modelData = new ModelData();
      ModelPartData modelRoot = modelData.getRoot();
      ModelPartData body = modelRoot.addChild(
         "body", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -1.5F, -1.0F, 4.0F, 3.0F, 5.0F), ModelTransform.pivot(0.0F, 21.0F, 0.0F)
      );
      body.addChild("thorax", ModelPartBuilder.create().uv(0, 8).cuboid(-1.5F, -1.5F, -3.5F, 3.0F, 3.0F, 3.0F), ModelTransform.NONE);
      body.addChild("head", ModelPartBuilder.create().uv(18, 0).cuboid(-1.5F, -1.0F, -2.0F, 3.0F, 2.0F, 2.0F), ModelTransform.pivot(0.0F, -0.5F, -3.5F));
      body.addChild(
         "left_wing",
         ModelPartBuilder.create().uv(32, 0).cuboid(0.0F, 0.0F, 0.0F, 5.0F, 0.0F, 7.0F, new Dilation(0.0F)),
         ModelTransform.pivot(1.0F, -1.5F, -2.5F)
      );
      body.addChild(
         "right_wing",
         ModelPartBuilder.create().uv(32, 8).mirrored().cuboid(-5.0F, 0.0F, 0.0F, 5.0F, 0.0F, 7.0F, new Dilation(0.0F)),
         ModelTransform.pivot(-1.0F, -1.5F, -2.5F)
      );

      for (int i = 0; i < 3; i++) {
         float z = -3.0F + i * 1.5F;
         body.addChild(
            "left_leg_" + i,
            ModelPartBuilder.create().uv(0, 14).cuboid(0.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F),
            ModelTransform.of(1.2F, 1.0F, z, 0.0F, 0.0F, -0.5F)
         );
         body.addChild(
            "right_leg_" + i,
            ModelPartBuilder.create().uv(0, 14).cuboid(-1.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F),
            ModelTransform.of(-1.2F, 1.0F, z, 0.0F, 0.0F, 0.5F)
         );
      }

      return TexturedModelData.of(modelData, 64, 64);
   }

   public void setAngles(ConnectomeFlyEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.head.yaw = headYaw * (float) (Math.PI / 180.0);
      this.head.pitch = headPitch * (float) (Math.PI / 180.0);
      float flap = MathHelper.sin(animationProgress * 2.1F) * 0.55F;
      this.leftWing.roll = 0.25F + flap;
      this.rightWing.roll = -0.25F - flap;
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
   }
}

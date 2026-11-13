package io.flyproject.connectomefly.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

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

   public static LayerDefinition getTexturedModelData() {
      MeshDefinition modelData = new MeshDefinition();
      PartDefinition modelRoot = modelData.getRoot();
      PartDefinition body = modelRoot.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.5F, -1.0F, 4.0F, 3.0F, 5.0F), PartPose.offset(0.0F, 21.0F, 0.0F)
      );
      body.addOrReplaceChild("thorax", CubeListBuilder.create().texOffs(0, 8).addBox(-1.5F, -1.5F, -3.5F, 3.0F, 3.0F, 3.0F), PartPose.ZERO);
      body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 0).addBox(-1.5F, -1.0F, -2.0F, 3.0F, 2.0F, 2.0F), PartPose.offset(0.0F, -0.5F, -3.5F));
      body.addOrReplaceChild(
         "left_wing",
         CubeListBuilder.create().texOffs(32, 0).addBox(0.0F, 0.0F, 0.0F, 5.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)),
         PartPose.offset(1.0F, -1.5F, -2.5F)
      );
      body.addOrReplaceChild(
         "right_wing",
         CubeListBuilder.create().texOffs(32, 8).mirror().addBox(-5.0F, 0.0F, 0.0F, 5.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-1.0F, -1.5F, -2.5F)
      );

      for (int i = 0; i < 3; i++) {
         float z = -3.0F + i * 1.5F;
         body.addOrReplaceChild(
            "left_leg_" + i,
            CubeListBuilder.create().texOffs(0, 14).addBox(0.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F),
            PartPose.offsetAndRotation(1.2F, 1.0F, z, 0.0F, 0.0F, -0.5F)
         );
         body.addOrReplaceChild(
            "right_leg_" + i,
            CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.0F, 0.0F, 1.0F, 3.0F, 1.0F),
            PartPose.offsetAndRotation(-1.2F, 1.0F, z, 0.0F, 0.0F, 0.5F)
         );
      }

      return LayerDefinition.create(modelData, 64, 64);
   }

   public void setupAnim(ConnectomeFlyEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.head.yRot = headYaw * (float) (Math.PI / 180.0);
      this.head.xRot = headPitch * (float) (Math.PI / 180.0);
      float flap = Mth.sin(animationProgress * 2.1F) * 0.55F;
      this.leftWing.zRot = 0.25F + flap;
      this.rightWing.zRot = -0.25F - flap;
   }

   public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
   }
}

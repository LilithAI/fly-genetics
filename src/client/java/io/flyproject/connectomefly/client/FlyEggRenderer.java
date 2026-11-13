package io.flyproject.connectomefly.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.flyproject.connectomefly.entity.FlyEggEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FlyEggRenderer extends EntityRenderer<FlyEggEntity> {
   private static final float TINT_R = 1.0F;
   private static final float TINT_G = 0.94F;
   private static final float TINT_B = 0.72F;

   public FlyEggRenderer(Context ctx) {
      super(ctx);
      this.shadowRadius = 0.1F;
   }

   public Identifier getTextureLocation(FlyEggEntity entity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }

   public void render(FlyEggEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
      int stage = Math.min(2, entity.getEggAge() * 3 / 600);
      BlockState state = (BlockState)((BlockState)Blocks.TURTLE_EGG.defaultBlockState().setValue(TurtleEggBlock.EGGS, 1)).setValue(TurtleEggBlock.HATCH, stage);
      matrices.pushPose();
      float wobble = entity.getEggAge() > 540 ? (float)Math.sin((entity.tickCount + tickDelta) * 1.4) * 6.0F : 0.0F;
      matrices.mulPose(Axis.YP.rotationDegrees(entity.getId() * 37.0F % 360.0F));
      matrices.mulPose(Axis.ZP.rotationDegrees(wobble));
      matrices.scale(0.7F, 0.7F, 0.7F);
      matrices.translate(-0.5, 0.0, -0.5);
      MultiBufferSource tinted = layer -> new FlyEggRenderer.Tinted(vertexConsumers.getBuffer(layer));
      Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, matrices, tinted, light, OverlayTexture.NO_OVERLAY);
      matrices.popPose();
      super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
   }

   private record Tinted(VertexConsumer in) implements VertexConsumer {
      public VertexConsumer vertex(double x, double y, double z) {
         this.in.vertex(x, y, z);
         return this;
      }

      public VertexConsumer color(int red, int green, int blue, int alpha) {
         this.in.color((int)(red * 1.0F), (int)(green * 0.94F), (int)(blue * 0.72F), alpha);
         return this;
      }

      public VertexConsumer uv(float u, float v) {
         this.in.uv(u, v);
         return this;
      }

      public VertexConsumer overlayCoords(int u, int v) {
         this.in.overlayCoords(u, v);
         return this;
      }

      public VertexConsumer uv2(int u, int v) {
         this.in.uv2(u, v);
         return this;
      }

      public VertexConsumer normal(float x, float y, float z) {
         this.in.normal(x, y, z);
         return this;
      }

      public void endVertex() {
         this.in.endVertex();
      }

      public void defaultColor(int red, int green, int blue, int alpha) {
         this.in.defaultColor((int)(red * 1.0F), (int)(green * 0.94F), (int)(blue * 0.72F), alpha);
      }

      public void unsetDefaultColor() {
         this.in.unsetDefaultColor();
      }
   }
}

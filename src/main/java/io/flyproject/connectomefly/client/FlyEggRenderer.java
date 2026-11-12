package io.flyproject.connectomefly.client;

import io.flyproject.connectomefly.entity.FlyEggEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class FlyEggRenderer extends EntityRenderer<FlyEggEntity> {
   private static final float TINT_R = 1.0F;
   private static final float TINT_G = 0.94F;
   private static final float TINT_B = 0.72F;

   public FlyEggRenderer(Context ctx) {
      super(ctx);
      this.shadowRadius = 0.1F;
   }

   public Identifier getTexture(FlyEggEntity entity) {
      return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
   }

   public void render(FlyEggEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      int stage = Math.min(2, entity.getEggAge() * 3 / 600);
      BlockState state = (BlockState)((BlockState)Blocks.TURTLE_EGG.getDefaultState().with(TurtleEggBlock.EGGS, 1)).with(TurtleEggBlock.HATCH, stage);
      matrices.push();
      float wobble = entity.getEggAge() > 540 ? (float)Math.sin((entity.age + tickDelta) * 1.4) * 6.0F : 0.0F;
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getId() * 37.0F % 360.0F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(wobble));
      matrices.scale(0.7F, 0.7F, 0.7F);
      matrices.translate(-0.5, 0.0, -0.5);
      VertexConsumerProvider tinted = layer -> new FlyEggRenderer.Tinted(vertexConsumers.getBuffer(layer));
      MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(state, matrices, tinted, light, OverlayTexture.DEFAULT_UV);
      matrices.pop();
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

      public VertexConsumer texture(float u, float v) {
         this.in.texture(u, v);
         return this;
      }

      public VertexConsumer overlay(int u, int v) {
         this.in.overlay(u, v);
         return this;
      }

      public VertexConsumer light(int u, int v) {
         this.in.light(u, v);
         return this;
      }

      public VertexConsumer normal(float x, float y, float z) {
         this.in.normal(x, y, z);
         return this;
      }

      public void next() {
         this.in.next();
      }

      public void fixedColor(int red, int green, int blue, int alpha) {
         this.in.fixedColor((int)(red * 1.0F), (int)(green * 0.94F), (int)(blue * 0.72F), alpha);
      }

      public void unfixColor() {
         this.in.unfixColor();
      }
   }
}

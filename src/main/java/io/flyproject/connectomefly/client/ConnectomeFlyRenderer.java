package io.flyproject.connectomefly.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.BeeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

public class ConnectomeFlyRenderer extends MobEntityRenderer<ConnectomeFlyEntity, ConnectomeFlyModel> {
   public static final Identifier TEXTURE = new Identifier("connectomefly", "textures/entity/connectome_fly.png");
   private static final Identifier BEE_TEXTURE = new Identifier("textures/entity/bee/bee.png");
   private static final Text SPEECH_LABEL = Text.literal("[translated from neuron activity]");
   private static final float BODY_LENGTH = 0.9F;
   private static final float BEE_SCALE = 1.45F;
   private static final double SKIN_INTERVAL = 0.022222222222222223;
   private final BeeEntityModel<BeeEntity> beeModel;
   private BeeEntity dummyBee;
   private final Map<Integer, FlyAnim> anims = new HashMap<>();
   private final Map<Integer, Double> lastSkin = new HashMap<>();
   private final Map<Integer, Long> lastSeen = new HashMap<>();

   public ConnectomeFlyRenderer(Context context) {
      super(context, new ConnectomeFlyModel(context.getPart(ConnectomeFlyClient.MODEL_LAYER)), 0.2F);
      this.beeModel = new BeeEntityModel(context.getPart(EntityModelLayers.BEE));
   }

   public Identifier getTexture(ConnectomeFlyEntity entity) {
      return TEXTURE;
   }

   protected RenderLayer getRenderLayer(ConnectomeFlyEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
      RenderLayer layer = super.getRenderLayer(entity, showBody, translucent, showOutline);
      return layer != null && showBody && !translucent && !showOutline ? RenderLayer.getEntityTranslucent(this.getTexture(entity)) : layer;
   }

   public void render(ConnectomeFlyEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      byte mode = entity.getBodyMode();
      if (mode == 1) {
         this.renderBee(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      } else if (FlyRig.get().ready()) {
         this.renderRigged(entity, yaw, tickDelta, matrices);
      } else {
         super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      }

      if (mode == 1 || FlyRig.get().ready()) {
         Entity holder = entity.getHoldingEntity();
         if (holder != null) {
            this.drawLead(entity, tickDelta, matrices, vertexConsumers, holder);
         }
      }

      String speech = entity.getSpeech();
      if (this.dispatcher.getSquaredDistanceToCamera(entity) <= 576.0 && !entity.isMale() && entity.deathTime == 0) {
         this.renderBars(entity, matrices, vertexConsumers, light, !speech.isEmpty());
      }

      if (!speech.isEmpty() && this.dispatcher.getSquaredDistanceToCamera(entity) <= 1024.0) {
         this.renderSpeech(entity, speech, matrices, vertexConsumers, light);
      }

      if (mode != 1 && FlyRig.get().ready() && this.hasLabel(entity)) {
         this.renderLabelIfPresent(entity, entity.getDisplayName(), matrices, vertexConsumers, light);
      }
   }

   private void renderRigged(ConnectomeFlyEntity entity, float yaw, float tickDelta, MatrixStack matrices) {
      FlyRig rig = FlyRig.get();
      double t = System.nanoTime() / 1.0E9;
      int id = entity.getId();
      FlyAnim anim = this.anims.get(id);
      if (anim == null) {
         anim = new FlyAnim(rig);
         this.anims.put(id, anim);
      }

      this.lastSeen.put(id, System.currentTimeMillis());
      if (this.anims.size() > 1 && (System.nanoTime() & 255L) == 0L) {
         this.gc();
      }

      Vec3d p = entity.getLerpedPos(tickDelta);
      anim.update(entity, t, p, yaw);
      Double ls = this.lastSkin.get(id);
      if (ls == null || t - ls >= 0.022222222222222223 || anim.inFlight && !anim.ghostsReady) {
         this.lastSkin.put(id, t);
         rig.pose(anim.pose);
         rig.uploadSkinned(rig.legsG, anim.legsVb, anim.legsOut, anim.legsBB);
         rig.uploadSkinned(rig.wingLG, anim.wingLVb, anim.wingLOut, anim.wingLBB);
         rig.uploadSkinned(rig.wingRG, anim.wingRVb, anim.wingROut, anim.wingRBB);
         if (anim.inFlight) {
            rig.pose(anim.poseGhostA);
            rig.uploadSkinned(rig.wingLG, anim.wingLGhostA, anim.wingLOut, anim.wingLBB);
            rig.uploadSkinned(rig.wingRG, anim.wingRGhostA, anim.wingROut, anim.wingRBB);
            rig.pose(anim.poseGhostB);
            rig.uploadSkinned(rig.wingLG, anim.wingLGhostB, anim.wingLOut, anim.wingLBB);
            rig.uploadSkinned(rig.wingRG, anim.wingRGhostB, anim.wingROut, anim.wingRBB);
            anim.ghostsReady = true;
         }
      }

      int block = entity.getWorld().getLightLevel(LightType.BLOCK, entity.getBlockPos());
      int sky = entity.getWorld().getLightLevel(LightType.SKY, entity.getBlockPos()) - entity.getWorld().getAmbientDarkness();
      float bright = 0.22F + 0.78F * (MathHelper.clamp(Math.max(block, sky), 0, 15) / 15.0F);
      matrices.push();
      Direction surf = entity.surface();
      if (surf != null) {
         float h = entity.getHeight();
         float w = entity.getWidth() / 2.0F;
         if (surf == Direction.UP) {
            matrices.translate(0.0, h, 0.0);
         } else {
            matrices.translate(surf.getOffsetX() * w, h / 2.0F, surf.getOffsetZ() * w);
         }

         Vec3d f = ConnectomeFlyEntity.surfaceForward(surf, yaw);
         Vec3d n = new Vec3d(-surf.getOffsetX(), -surf.getOffsetY(), -surf.getOffsetZ());
         Vec3d r = n.crossProduct(f);
         Matrix4f basis = new Matrix4f(
            (float)r.x,
            (float)r.y,
            (float)r.z,
            0.0F,
            (float)n.x,
            (float)n.y,
            (float)n.z,
            0.0F,
            (float)f.x,
            (float)f.y,
            (float)f.z,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            1.0F
         );
         matrices.multiplyPositionMatrix(basis);
         matrices.translate(0.0, anim.bob() * 0.9F, 0.0);
      } else {
         matrices.translate(0.0, anim.bob() * 0.9F, 0.0);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
      }

      if (entity.deathTime > 0) {
         float f = (entity.deathTime + tickDelta - 1.0F) / 20.0F * 1.6F;
         f = Math.min(1.0F, (float)Math.sqrt(Math.max(0.0F, f)));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 90.0F));
      }

      matrices.multiply(RotationAxis.POSITIVE_Z.rotation(anim.roll()));
      matrices.multiply(RotationAxis.POSITIVE_X.rotation(-anim.pitch()));
      float sc = 0.9F * entity.getScaleFactor();
      matrices.scale(sc, sc, sc);
      boolean hurt = entity.hurtTime > 0 || entity.deathTime > 0;
      rig.beginDraw(bright);
      if (hurt) {
         RenderSystem.setShaderColor(bright, bright * 0.45F, bright * 0.45F, 1.0F);
      }

      rig.drawBody(matrices);
      rig.drawHead(matrices, anim.headYaw(), anim.headNod());
      Matrix4f mv = matrices.peek().getPositionMatrix();
      rig.draw(anim.legsVb, mv);
      rig.draw(anim.wingLVb, mv);
      rig.draw(anim.wingRVb, mv);
      if (anim.inFlight && anim.ghostsReady) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.depthMask(false);
         RenderSystem.setShaderColor(bright, bright, bright, 0.22F);
         rig.draw(anim.wingLGhostA, mv);
         rig.draw(anim.wingRGhostA, mv);
         rig.draw(anim.wingLGhostB, mv);
         rig.draw(anim.wingRGhostB, mv);
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
      }

      rig.endDraw();
      matrices.pop();
   }

   private void drawLead(ConnectomeFlyEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, Entity holder) {
      matrices.push();
      Vec3d to = holder.getLeashPos(tickDelta);
      double oy = entity.getHeight() * 0.6;
      double gx = MathHelper.lerp(tickDelta, entity.prevX, entity.getX());
      double gy = MathHelper.lerp(tickDelta, entity.prevY, entity.getY()) + oy;
      double gz = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ());
      matrices.translate(0.0, oy, 0.0);
      float dx = (float)(to.x - gx);
      float dy = (float)(to.y - gy);
      float dz = (float)(to.z - gz);
      VertexConsumer vc = provider.getBuffer(RenderLayer.getLeash());
      Matrix4f m = matrices.peek().getPositionMatrix();
      float inv = MathHelper.inverseSqrt(dx * dx + dz * dz + 1.0E-6F) * 0.045F / 2.0F;
      float sx = dz * inv;
      float sz = dx * inv;
      BlockPos a = BlockPos.ofFloored(entity.getCameraPosVec(tickDelta));
      BlockPos b = BlockPos.ofFloored(holder.getCameraPosVec(tickDelta));
      int bl1 = entity.getWorld().getLightLevel(LightType.BLOCK, a);
      int bl2 = entity.getWorld().getLightLevel(LightType.BLOCK, b);
      int sl1 = entity.getWorld().getLightLevel(LightType.SKY, a);
      int sl2 = entity.getWorld().getLightLevel(LightType.SKY, b);

      for (int i = 0; i <= 24; i++) {
         leadPiece(vc, m, dx, dy, dz, bl1, bl2, sl1, sl2, 0.045F, 0.045F, sx, sz, i, false);
      }

      for (int i = 24; i >= 0; i--) {
         leadPiece(vc, m, dx, dy, dz, bl1, bl2, sl1, sl2, 0.045F, 0.0F, sx, sz, i, true);
      }

      matrices.pop();
   }

   private static void leadPiece(
      VertexConsumer vc,
      Matrix4f m,
      float dx,
      float dy,
      float dz,
      int bl1,
      int bl2,
      int sl1,
      int sl2,
      float h1,
      float h2,
      float sx,
      float sz,
      int piece,
      boolean back
   ) {
      float f = piece / 24.0F;
      int light = LightmapTextureManager.pack(MathHelper.lerp(f, bl1, bl2), MathHelper.lerp(f, sl1, sl2));
      float shade = piece % 2 == (back ? 1 : 0) ? 0.7F : 1.0F;
      float r = 0.5F * shade;
      float g = 0.4F * shade;
      float bb = 0.3F * shade;
      float x = dx * f;
      float y = dy > 0.0F ? dy * f * f : dy - dy * (1.0F - f) * (1.0F - f);
      float z = dz * f;
      vc.vertex(m, x - sx, y + h2, z + sz).color(r, g, bb, 1.0F).light(light).next();
      vc.vertex(m, x + sx, y + h1 - h2, z - sz).color(r, g, bb, 1.0F).light(light).next();
   }

   private void gc() {
      long now = System.currentTimeMillis();
      Iterator<Entry<Integer, Long>> it = this.lastSeen.entrySet().iterator();

      while (it.hasNext()) {
         Entry<Integer, Long> e = it.next();
         if (now - e.getValue() > 30000L) {
            FlyAnim a = this.anims.remove(e.getKey());
            if (a != null) {
               a.close();
            }

            this.lastSkin.remove(e.getKey());
            it.remove();
         }
      }
   }

   private void renderBee(ConnectomeFlyEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (this.dummyBee == null || this.dummyBee.getWorld() != entity.getWorld()) {
         this.dummyBee = (BeeEntity)EntityType.BEE.create(entity.getWorld());
         if (this.dummyBee == null) {
            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
         }
      }

      this.dummyBee.setOnGround(entity.isOnGround() && !entity.isFlapping());
      this.dummyBee.setVelocity(entity.getVelocity());
      this.dummyBee.age = entity.age;
      float animationProgress = entity.age + tickDelta;
      float limbDistance = entity.limbAnimator.getSpeed(tickDelta);
      float limbAngle = entity.limbAnimator.getPos(tickDelta);
      float headYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.prevHeadYaw, entity.headYaw) - yaw;
      float headPitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());
      matrices.push();
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - yaw));
      matrices.scale(-1.0F, -1.0F, 1.0F);
      float bs = entity.getScaleFactor() * 1.45F;
      matrices.scale(bs, bs, bs);
      matrices.translate(0.0F, -1.501F, 0.0F);
      this.beeModel.animateModel(this.dummyBee, limbAngle, limbDistance, tickDelta);
      this.beeModel.setAngles(this.dummyBee, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(BEE_TEXTURE));
      this.beeModel.render(matrices, vc, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
      matrices.pop();
   }

   private void renderBars(ConnectomeFlyEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, boolean speechAbove) {
      TextRenderer tr = this.getTextRenderer();
      int hearts = (int)Math.ceil(entity.getHealth() / 2.0F);
      int maxHearts = (int)Math.ceil(entity.getMaxHealth() / 2.0F);
      StringBuilder hp = new StringBuilder();

      for (int i = 0; i < maxHearts; i++) {
         hp.append((char)(i < hearts ? '♥' : '♡'));
      }

      int food = Math.round(entity.getHunger() / 2.0F);
      StringBuilder hu = new StringBuilder();

      for (int i = 0; i < 10; i++) {
         hu.append((char)(i < food ? '|' : '.'));
      }

      matrices.push();
      matrices.translate(0.0, entity.getHeight() + (speechAbove ? 0.45 : 0.45), 0.0);
      matrices.multiply(this.dispatcher.getRotation());
      matrices.scale(-0.018F, -0.018F, 0.018F);
      Matrix4f m = matrices.peek().getPositionMatrix();
      Text h = Text.literal(hp.toString());
      tr.draw(h, -tr.getWidth(h) / 2.0F, -10.0F, -42406, false, m, vertexConsumers, TextLayerType.SEE_THROUGH, 0, light);
      Text f = Text.literal("hunger " + hu);
      tr.draw(f, -tr.getWidth(f) / 2.0F, 0.0F, -2052000, false, m, vertexConsumers, TextLayerType.SEE_THROUGH, 0, light);
      matrices.pop();
   }

   private void renderSpeech(ConnectomeFlyEntity entity, String speech, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      TextRenderer textRenderer = this.getTextRenderer();
      float bgOpacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
      int background = (int)(bgOpacity * 255.0F) << 24;
      matrices.push();
      matrices.translate(0.0, entity.getHeight() + 1.05, 0.0);
      matrices.multiply(this.dispatcher.getRotation());
      matrices.scale(-0.025F, -0.025F, 0.025F);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      Text line = Text.literal("\"" + speech + "\"");
      float x = -textRenderer.getWidth(line) / 2.0F;
      textRenderer.draw(line, x, -12.0F, 16777215, false, matrix, vertexConsumers, TextLayerType.SEE_THROUGH, background, light);
      matrices.scale(0.62F, 0.62F, 0.62F);
      Matrix4f labelMatrix = matrices.peek().getPositionMatrix();
      float lx = -textRenderer.getWidth(SPEECH_LABEL) / 2.0F;
      textRenderer.draw(SPEECH_LABEL, lx, -2.0F, 11184810, false, labelMatrix, vertexConsumers, TextLayerType.SEE_THROUGH, background, light);
      matrices.pop();
   }
}

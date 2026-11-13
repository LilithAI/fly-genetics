package io.flyproject.connectomefly.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ConnectomeFlyRenderer extends MobRenderer<ConnectomeFlyEntity, ConnectomeFlyModel> {
   public static final Identifier TEXTURE = new Identifier("connectomefly", "textures/entity/connectome_fly.png");
   private static final Identifier BEE_TEXTURE = new Identifier("textures/entity/bee/bee.png");
   private static final Component SPEECH_LABEL = Component.literal("[translated from neuron activity]");
   private static final float BODY_LENGTH = 0.9F;
   private static final float BEE_SCALE = 1.45F;
   private static final double SKIN_INTERVAL = 0.022222222222222223;
   private final BeeModel<Bee> beeModel;
   private Bee dummyBee;
   private final Map<Integer, FlyAnim> anims = new HashMap<>();
   private final Map<Integer, Double> lastSkin = new HashMap<>();
   private final Map<Integer, Long> lastSeen = new HashMap<>();

   public ConnectomeFlyRenderer(Context context) {
      super(context, new ConnectomeFlyModel(context.bakeLayer(ConnectomeFlyClient.MODEL_LAYER)), 0.2F);
      this.beeModel = new BeeModel(context.bakeLayer(ModelLayers.BEE));
   }

   public Identifier getTextureLocation(ConnectomeFlyEntity entity) {
      return TEXTURE;
   }

   protected RenderType getRenderType(ConnectomeFlyEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
      RenderType layer = super.getRenderType(entity, showBody, translucent, showOutline);
      return layer != null && showBody && !translucent && !showOutline ? RenderType.entityTranslucent(this.getTextureLocation(entity)) : layer;
   }

   public void render(ConnectomeFlyEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
      byte mode = entity.getBodyMode();
      if (mode == 1) {
         this.renderBee(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      } else if (FlyRig.get().ready()) {
         this.renderRigged(entity, yaw, tickDelta, matrices);
      } else {
         super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      }

      if (mode == 1 || FlyRig.get().ready()) {
         Entity holder = entity.getLeashHolder();
         if (holder != null) {
            this.drawLead(entity, tickDelta, matrices, vertexConsumers, holder);
         }
      }

      String speech = entity.getSpeech();
      if (this.entityRenderDispatcher.distanceToSqr(entity) <= 576.0 && !entity.isMale() && entity.deathTime == 0) {
         this.renderBars(entity, matrices, vertexConsumers, light, !speech.isEmpty());
      }

      if (!speech.isEmpty() && this.entityRenderDispatcher.distanceToSqr(entity) <= 1024.0) {
         this.renderSpeech(entity, speech, matrices, vertexConsumers, light);
      }

      if (mode != 1 && FlyRig.get().ready() && this.shouldShowName(entity)) {
         this.renderNameTag(entity, entity.getDisplayName(), matrices, vertexConsumers, light);
      }
   }

   private void renderRigged(ConnectomeFlyEntity entity, float yaw, float tickDelta, PoseStack matrices) {
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

      Vec3 p = entity.getPosition(tickDelta);
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

      int block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition());
      int sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) - entity.level().getSkyDarken();
      float bright = 0.22F + 0.78F * (Mth.clamp(Math.max(block, sky), 0, 15) / 15.0F);
      matrices.pushPose();
      Direction surf = entity.surface();
      if (surf != null) {
         float h = entity.getBbHeight();
         float w = entity.getBbWidth() / 2.0F;
         if (surf == Direction.UP) {
            matrices.translate(0.0, h, 0.0);
         } else {
            matrices.translate(surf.getStepX() * w, h / 2.0F, surf.getStepZ() * w);
         }

         Vec3 f = ConnectomeFlyEntity.surfaceForward(surf, yaw);
         Vec3 n = new Vec3(-surf.getStepX(), -surf.getStepY(), -surf.getStepZ());
         Vec3 r = n.cross(f);
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
         matrices.mulPoseMatrix(basis);
         matrices.translate(0.0, anim.bob() * 0.9F, 0.0);
      } else {
         matrices.translate(0.0, anim.bob() * 0.9F, 0.0);
         matrices.mulPose(Axis.YP.rotationDegrees(-yaw));
      }

      if (entity.deathTime > 0) {
         float f = (entity.deathTime + tickDelta - 1.0F) / 20.0F * 1.6F;
         f = Math.min(1.0F, (float)Math.sqrt(Math.max(0.0F, f)));
         matrices.mulPose(Axis.ZP.rotationDegrees(f * 90.0F));
      }

      matrices.mulPose(Axis.ZP.rotation(anim.roll()));
      matrices.mulPose(Axis.XP.rotation(-anim.pitch()));
      float sc = 0.9F * entity.getScale();
      matrices.scale(sc, sc, sc);
      boolean hurt = entity.hurtTime > 0 || entity.deathTime > 0;
      rig.beginDraw(bright);
      if (hurt) {
         RenderSystem.setShaderColor(bright, bright * 0.45F, bright * 0.45F, 1.0F);
      }

      rig.drawBody(matrices);
      rig.drawHead(matrices, anim.headYaw(), anim.headNod());
      Matrix4f mv = matrices.last().pose();
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
      matrices.popPose();
   }

   private void drawLead(ConnectomeFlyEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource provider, Entity holder) {
      matrices.pushPose();
      Vec3 to = holder.getRopeHoldPosition(tickDelta);
      double oy = entity.getBbHeight() * 0.6;
      double gx = Mth.lerp(tickDelta, entity.xo, entity.getX());
      double gy = Mth.lerp(tickDelta, entity.yo, entity.getY()) + oy;
      double gz = Mth.lerp(tickDelta, entity.zo, entity.getZ());
      matrices.translate(0.0, oy, 0.0);
      float dx = (float)(to.x - gx);
      float dy = (float)(to.y - gy);
      float dz = (float)(to.z - gz);
      VertexConsumer vc = provider.getBuffer(RenderType.leash());
      Matrix4f m = matrices.last().pose();
      float inv = Mth.invSqrt(dx * dx + dz * dz + 1.0E-6F) * 0.045F / 2.0F;
      float sx = dz * inv;
      float sz = dx * inv;
      BlockPos a = BlockPos.containing(entity.getEyePosition(tickDelta));
      BlockPos b = BlockPos.containing(holder.getEyePosition(tickDelta));
      int bl1 = entity.level().getBrightness(LightLayer.BLOCK, a);
      int bl2 = entity.level().getBrightness(LightLayer.BLOCK, b);
      int sl1 = entity.level().getBrightness(LightLayer.SKY, a);
      int sl2 = entity.level().getBrightness(LightLayer.SKY, b);

      for (int i = 0; i <= 24; i++) {
         leadPiece(vc, m, dx, dy, dz, bl1, bl2, sl1, sl2, 0.045F, 0.045F, sx, sz, i, false);
      }

      for (int i = 24; i >= 0; i--) {
         leadPiece(vc, m, dx, dy, dz, bl1, bl2, sl1, sl2, 0.045F, 0.0F, sx, sz, i, true);
      }

      matrices.popPose();
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
      int light = LightTexture.pack(Mth.lerpInt(f, bl1, bl2), Mth.lerpInt(f, sl1, sl2));
      float shade = piece % 2 == (back ? 1 : 0) ? 0.7F : 1.0F;
      float r = 0.5F * shade;
      float g = 0.4F * shade;
      float bb = 0.3F * shade;
      float x = dx * f;
      float y = dy > 0.0F ? dy * f * f : dy - dy * (1.0F - f) * (1.0F - f);
      float z = dz * f;
      vc.vertex(m, x - sx, y + h2, z + sz).color(r, g, bb, 1.0F).uv2(light).endVertex();
      vc.vertex(m, x + sx, y + h1 - h2, z - sz).color(r, g, bb, 1.0F).uv2(light).endVertex();
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

   private void renderBee(ConnectomeFlyEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
      if (this.dummyBee == null || this.dummyBee.level() != entity.level()) {
         this.dummyBee = (Bee)EntityType.BEE.create(entity.level());
         if (this.dummyBee == null) {
            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
         }
      }

      this.dummyBee.setOnGround(entity.onGround() && !entity.isWingFlapping());
      this.dummyBee.setDeltaMovement(entity.getDeltaMovement());
      this.dummyBee.tickCount = entity.tickCount;
      float animationProgress = entity.tickCount + tickDelta;
      float limbDistance = entity.walkAnimation.speed(tickDelta);
      float limbAngle = entity.walkAnimation.position(tickDelta);
      float headYaw = Mth.rotLerp(tickDelta, entity.yHeadRotO, entity.yHeadRot) - yaw;
      float headPitch = Mth.lerp(tickDelta, entity.xRotO, entity.getXRot());
      matrices.pushPose();
      matrices.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
      matrices.scale(-1.0F, -1.0F, 1.0F);
      float bs = entity.getScale() * 1.45F;
      matrices.scale(bs, bs, bs);
      matrices.translate(0.0F, -1.501F, 0.0F);
      this.beeModel.prepareMobModel(this.dummyBee, limbAngle, limbDistance, tickDelta);
      this.beeModel.setupAnim(this.dummyBee, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      VertexConsumer vc = vertexConsumers.getBuffer(RenderType.entityCutoutNoCull(BEE_TEXTURE));
      this.beeModel.renderToBuffer(matrices, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      matrices.popPose();
   }

   private void renderBars(ConnectomeFlyEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers, int light, boolean speechAbove) {
      Font tr = this.getFont();
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

      matrices.pushPose();
      matrices.translate(0.0, entity.getBbHeight() + (speechAbove ? 0.45 : 0.45), 0.0);
      matrices.mulPose(this.entityRenderDispatcher.cameraOrientation());
      matrices.scale(-0.018F, -0.018F, 0.018F);
      Matrix4f m = matrices.last().pose();
      Component h = Component.literal(hp.toString());
      tr.drawInBatch(h, -tr.width(h) / 2.0F, -10.0F, -42406, false, m, vertexConsumers, DisplayMode.SEE_THROUGH, 0, light);
      Component f = Component.literal("hunger " + hu);
      tr.drawInBatch(f, -tr.width(f) / 2.0F, 0.0F, -2052000, false, m, vertexConsumers, DisplayMode.SEE_THROUGH, 0, light);
      matrices.popPose();
   }

   private void renderSpeech(ConnectomeFlyEntity entity, String speech, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
      Font textRenderer = this.getFont();
      float bgOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
      int background = (int)(bgOpacity * 255.0F) << 24;
      matrices.pushPose();
      matrices.translate(0.0, entity.getBbHeight() + 1.05, 0.0);
      matrices.mulPose(this.entityRenderDispatcher.cameraOrientation());
      matrices.scale(-0.025F, -0.025F, 0.025F);
      Matrix4f matrix = matrices.last().pose();
      Component line = Component.literal("\"" + speech + "\"");
      float x = -textRenderer.width(line) / 2.0F;
      textRenderer.drawInBatch(line, x, -12.0F, 16777215, false, matrix, vertexConsumers, DisplayMode.SEE_THROUGH, background, light);
      matrices.scale(0.62F, 0.62F, 0.62F);
      Matrix4f labelMatrix = matrices.last().pose();
      float lx = -textRenderer.width(SPEECH_LABEL) / 2.0F;
      textRenderer.drawInBatch(SPEECH_LABEL, lx, -2.0F, 11184810, false, labelMatrix, vertexConsumers, DisplayMode.SEE_THROUGH, background, light);
      matrices.popPose();
   }
}

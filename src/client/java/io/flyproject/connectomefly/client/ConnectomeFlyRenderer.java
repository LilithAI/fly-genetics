package io.flyproject.connectomefly.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.model.animal.bee.AdultBeeModel;
import net.minecraft.client.model.animal.bee.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * 26.3 port of the fly renderer. The original drew its own vertex buffers with immediate GL calls, which no
 * longer exist. Here the same mesh (rigid body and head, CPU-skinned legs and wings) is sent through the
 * standard entity pipeline, so it is lit by the world's light map instead of a hand-computed brightness.
 */
public class ConnectomeFlyRenderer extends EntityRenderer<ConnectomeFlyEntity, FlyRenderState> {
   private static final Identifier BEE_TEXTURE = Identifier.withDefaultNamespace("textures/entity/bee/bee.png");
   private static final Component SPEECH_LABEL = Component.literal("[translated from neuron activity]");
   private static final double SKIN_INTERVAL = 0.022222222222222223;
   private final BeeModel beeModel;
   private final BeeRenderState beeState = new BeeRenderState();
   private final Map<Integer, FlyAnim> anims = new HashMap<>();
   private final Map<Integer, Double> lastSkin = new HashMap<>();
   private final Map<Integer, Long> lastSeen = new HashMap<>();

   public ConnectomeFlyRenderer(Context context) {
      super(context);
      this.shadowRadius = 0.2F;
      this.beeModel = new AdultBeeModel(context.bakeLayer(ModelLayers.BEE));
   }

   @Override
   public FlyRenderState createRenderState() {
      return new FlyRenderState();
   }

   @Override
   public void extractRenderState(ConnectomeFlyEntity entity, FlyRenderState s, float tickDelta) {
      super.extractRenderState(entity, s, tickDelta);
      float yaw = entity.getYRot(tickDelta);
      s.bodyMode = entity.getBodyMode();
      s.male = entity.isMale();
      s.yaw = yaw;
      s.surface = entity.surface();
      s.scale = entity.getScale();
      s.deathTime = entity.deathTime > 0 ? entity.deathTime + tickDelta : 0.0F;
      s.hurt = entity.hurtTime > 0 || entity.deathTime > 0;
      s.speech = entity.getSpeech();
      s.showBars = s.distanceToCameraSq <= 576.0 && !entity.isMale() && entity.deathTime == 0;
      s.showSpeech = !s.speech.isEmpty() && s.distanceToCameraSq <= 1024.0;
      s.health = entity.getHealth();
      s.maxHealth = entity.getMaxHealth();
      s.hunger = entity.getHunger();
      s.onGround = entity.onGround() && !entity.isWingFlapping();
      s.walkPos = entity.walkAnimation.position(tickDelta);
      s.walkSpeed = entity.walkAnimation.speed(tickDelta);
      s.headYaw = Mth.rotLerp(tickDelta, entity.yHeadRotO, entity.yHeadRot) - yaw;
      s.headPitch = entity.getXRot(tickDelta);
      FlyRig rig = FlyRig.get();
      s.rigged = s.bodyMode != 1 && rig.ready();
      s.anim = null;
      if (s.rigged) {
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
         // full detail only within 3 blocks; the middle level to 12 blocks; the coarse one beyond
         s.lod = s.distanceToCameraSq < 9.0 ? 0 : (s.distanceToCameraSq < 144.0 ? 1 : 2);
         Double ls = this.lastSkin.get(id);
         if (ls == null || t - ls >= SKIN_INTERVAL || anim.skinnedLod != s.lod || anim.inFlight && !anim.ghostsReady) {
            this.lastSkin.put(id, t);
            int l = s.lod;
            anim.skinnedLod = l;
            rig.pose(anim.pose);
            rig.skin(rig.legsL[l], anim.legsOut);
            rig.skin(rig.wingLL[l], anim.wingLOut);
            rig.skin(rig.wingRL[l], anim.wingROut);
            if (anim.inFlight) {
               rig.pose(anim.poseGhostA);
               rig.skin(rig.wingLL[l], anim.wingLGhostA);
               rig.skin(rig.wingRL[l], anim.wingRGhostA);
               rig.pose(anim.poseGhostB);
               rig.skin(rig.wingLL[l], anim.wingLGhostB);
               rig.skin(rig.wingRL[l], anim.wingRGhostB);
               anim.ghostsReady = true;
            }
         }

         // draw the detail level that was actually skinned, so positions and triangles always match
         s.lod = anim.skinnedLod;

         s.anim = anim;
      }
   }

   @Override
   public void submit(FlyRenderState s, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
      if (s.rigged && s.anim != null) {
         this.submitRigged(s, matrices, collector);
      } else {
         this.submitBee(s, matrices, collector);
      }

      if (s.showBars) {
         this.submitBars(s, matrices, collector, camera);
      }

      if (s.showSpeech) {
         this.submitSpeech(s, matrices, collector, camera);
      }

      super.submit(s, matrices, collector, camera);
   }

   private void submitRigged(FlyRenderState s, PoseStack matrices, SubmitNodeCollector collector) {
      FlyRig rig = FlyRig.get();
      FlyAnim anim = s.anim;
      matrices.pushPose();
      Direction surf = s.surface;
      if (surf != null) {
         float h = s.boundingBoxHeight;
         float w = s.boundingBoxWidth / 2.0F;
         if (surf == Direction.UP) {
            matrices.translate(0.0, h, 0.0);
         } else {
            matrices.translate(surf.getStepX() * w, h / 2.0F, surf.getStepZ() * w);
         }

         Vec3 f = ConnectomeFlyEntity.surfaceForward(surf, s.yaw);
         Vec3 n = new Vec3(-surf.getStepX(), -surf.getStepY(), -surf.getStepZ());
         Vec3 r = n.cross(f);
         Matrix4f basis = new Matrix4f(
            (float)r.x, (float)r.y, (float)r.z, 0.0F, (float)n.x, (float)n.y, (float)n.z, 0.0F, (float)f.x, (float)f.y, (float)f.z, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F
         );
         matrices.mulPose(basis);
         matrices.translate(0.0, anim.bob() * 0.9F, 0.0);
      } else {
         matrices.translate(0.0, anim.bob() * 0.9F, 0.0);
         matrices.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(-s.yaw)));
      }

      if (s.deathTime > 0.0F) {
         float f = (s.deathTime - 1.0F) / 20.0F * 1.6F;
         f = Math.min(1.0F, (float)Math.sqrt(Math.max(0.0F, f)));
         matrices.mulPose(new Matrix4f().rotation(Axis.ZP.rotationDegrees(f * 90.0F)));
      }

      matrices.mulPose(new Matrix4f().rotation(Axis.ZP.rotation(anim.roll())));
      matrices.mulPose(new Matrix4f().rotation(Axis.XP.rotation(-anim.pitch())));
      float sc = 0.9F * s.scale;
      matrices.scale(sc, sc, sc);
      int light = s.lightCoords;
      int overlay = OverlayTexture.pack(0.0F, s.hurt);
      int l = s.lod;
      FlyRig.Group body = rig.bodyL[l];
      FlyRig.Group legs = rig.legsL[l];
      FlyRig.Group wl = rig.wingLL[l];
      FlyRig.Group wr = rig.wingRL[l];
      collector.submitCustomGeometry(matrices, RenderTypes.entityCutout(FlyRig.TEXTURE), (pose, vc) -> {
         FlyRig.emit(vc, pose, body, body.pos, light, overlay, 255, 1);
         FlyRig.emit(vc, pose, legs, anim.legsOut, light, overlay, 255, 1);
         FlyRig.emit(vc, pose, wl, anim.wingLOut, light, overlay, 255, 1);
         FlyRig.emit(vc, pose, wr, anim.wingROut, light, overlay, 255, 1);
      });
      if (rig.hasHead()) {
         FlyRig.Group head = rig.headL[l];
         matrices.pushPose();
         matrices.translate(FlyRig.HEAD_PIVOT[0], FlyRig.HEAD_PIVOT[1], FlyRig.HEAD_PIVOT[2]);
         matrices.mulPose(new Matrix4f().rotation(Axis.YP.rotation(anim.headYaw())));
         matrices.mulPose(new Matrix4f().rotation(Axis.XP.rotation(anim.headNod())));
         matrices.translate(-FlyRig.HEAD_PIVOT[0], -FlyRig.HEAD_PIVOT[1], -FlyRig.HEAD_PIVOT[2]);
         collector.submitCustomGeometry(matrices, RenderTypes.entityCutout(FlyRig.TEXTURE), (pose, vc) -> FlyRig.emit(vc, pose, head, head.pos, light, overlay, 255, 1));
         matrices.popPose();
      }

      if (anim.inFlight && anim.ghostsReady) {
         // wing blur: two faint extra wing poses, as in the original
         collector.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(FlyRig.TEXTURE), (pose, vc) -> {
            FlyRig.emit(vc, pose, wl, anim.wingLGhostA, light, overlay, 56, 1);
            FlyRig.emit(vc, pose, wr, anim.wingRGhostA, light, overlay, 56, 1);
            FlyRig.emit(vc, pose, wl, anim.wingLGhostB, light, overlay, 56, 1);
            FlyRig.emit(vc, pose, wr, anim.wingRGhostB, light, overlay, 56, 1);
         });
      }

      matrices.popPose();
   }

   /** The plain bee body (/flylab body bee), also used if the fly model failed to load. */
   private void submitBee(FlyRenderState s, PoseStack matrices, SubmitNodeCollector collector) {
      BeeRenderState b = this.beeState;
      b.ageInTicks = s.ageInTicks;
      b.isOnGround = s.onGround;
      b.walkAnimationPos = s.walkPos;
      b.walkAnimationSpeed = s.walkSpeed;
      b.yRot = s.headYaw;
      b.xRot = s.headPitch;
      b.bodyRot = s.yaw;
      b.hasStinger = true;
      b.lightCoords = s.lightCoords;
      matrices.pushPose();
      matrices.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(180.0F - s.yaw)));
      matrices.scale(-1.0F, -1.0F, 1.0F);
      float bs = s.scale * 1.45F;
      matrices.scale(bs, bs, bs);
      matrices.translate(0.0F, -1.501F, 0.0F);
      collector.submitModel(this.beeModel, b, matrices, RenderTypes.entityCutout(BEE_TEXTURE), s.lightCoords, OverlayTexture.pack(0.0F, s.hurt), s.outlineColor);
      matrices.popPose();
   }

   private void submitBars(FlyRenderState s, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
      Font tr = this.getFont();
      int hearts = (int)Math.ceil(s.health / 2.0F);
      int maxHearts = (int)Math.ceil(s.maxHealth / 2.0F);
      StringBuilder hp = new StringBuilder();

      for (int i = 0; i < maxHearts; i++) {
         hp.append((char)(i < hearts ? '♥' : '♡'));
      }

      int food = Math.round(s.hunger / 2.0F);
      StringBuilder hu = new StringBuilder();

      for (int i = 0; i < 10; i++) {
         hu.append((char)(i < food ? '|' : '.'));
      }

      matrices.pushPose();
      matrices.translate(0.0, s.boundingBoxHeight + 0.45, 0.0);
      matrices.mulPose(new Matrix4f().rotation(camera.orientation));
      matrices.scale(0.018F, -0.018F, 0.018F);
      Component h = Component.literal(hp.toString());
      collector.submitText(matrices, -tr.width(h) / 2.0F, -10.0F, h.getVisualOrderText(), false, DisplayMode.SEE_THROUGH, s.lightCoords, -42406, 0, 0);
      Component f = Component.literal("hunger " + hu);
      collector.submitText(matrices, -tr.width(f) / 2.0F, 0.0F, f.getVisualOrderText(), false, DisplayMode.SEE_THROUGH, s.lightCoords, -2052000, 0, 0);
      matrices.popPose();
   }

   private void submitSpeech(FlyRenderState s, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
      Font tr = this.getFont();
      int background = 0x40000000;
      matrices.pushPose();
      matrices.translate(0.0, s.boundingBoxHeight + 1.05, 0.0);
      matrices.mulPose(new Matrix4f().rotation(camera.orientation));
      matrices.scale(0.025F, -0.025F, 0.025F);
      Component line = Component.literal("\"" + s.speech + "\"");
      collector.submitText(matrices, -tr.width(line) / 2.0F, -12.0F, line.getVisualOrderText(), false, DisplayMode.SEE_THROUGH, s.lightCoords, 0xFFFFFFFF, background, 0);
      matrices.scale(0.62F, 0.62F, 0.62F);
      collector.submitText(
         matrices, -tr.width(SPEECH_LABEL) / 2.0F, -2.0F, SPEECH_LABEL.getVisualOrderText(), false, DisplayMode.SEE_THROUGH, s.lightCoords, 0xFFAAAAAA, background, 0
      );
      matrices.popPose();
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
}

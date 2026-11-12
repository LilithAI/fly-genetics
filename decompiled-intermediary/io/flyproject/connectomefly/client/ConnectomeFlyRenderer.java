package io.flyproject.connectomefly.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1921;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_3532;
import net.minecraft.class_4466;
import net.minecraft.class_4495;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5602;
import net.minecraft.class_765;
import net.minecraft.class_7833;
import net.minecraft.class_927;
import net.minecraft.class_327.class_6415;
import net.minecraft.class_5617.class_5618;
import org.joml.Matrix4f;

public class ConnectomeFlyRenderer extends class_927<ConnectomeFlyEntity, ConnectomeFlyModel> {
   public static final class_2960 TEXTURE = new class_2960("connectomefly", "textures/entity/connectome_fly.png");
   private static final class_2960 BEE_TEXTURE = new class_2960("textures/entity/bee/bee.png");
   private static final class_2561 SPEECH_LABEL = class_2561.method_43470("[translated from neuron activity]");
   private static final float BODY_LENGTH = 0.9F;
   private static final float BEE_SCALE = 1.45F;
   private static final double SKIN_INTERVAL = 0.022222222222222223;
   private final class_4495<class_4466> beeModel;
   private class_4466 dummyBee;
   private final Map<Integer, FlyAnim> anims = new HashMap<>();
   private final Map<Integer, Double> lastSkin = new HashMap<>();
   private final Map<Integer, Long> lastSeen = new HashMap<>();

   public ConnectomeFlyRenderer(class_5618 context) {
      super(context, new ConnectomeFlyModel(context.method_32167(ConnectomeFlyClient.MODEL_LAYER)), 0.2F);
      this.beeModel = new class_4495(context.method_32167(class_5602.field_27682));
   }

   public class_2960 getTexture(ConnectomeFlyEntity entity) {
      return TEXTURE;
   }

   protected class_1921 getRenderLayer(ConnectomeFlyEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
      class_1921 layer = super.method_24302(entity, showBody, translucent, showOutline);
      return layer != null && showBody && !translucent && !showOutline ? class_1921.method_23580(this.getTexture(entity)) : layer;
   }

   public void render(ConnectomeFlyEntity entity, float yaw, float tickDelta, class_4587 matrices, class_4597 vertexConsumers, int light) {
      byte mode = entity.getBodyMode();
      if (mode == 1) {
         this.renderBee(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      } else if (FlyRig.get().ready()) {
         this.renderRigged(entity, yaw, tickDelta, matrices);
      } else {
         super.method_4072(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      }

      if (mode == 1 || FlyRig.get().ready()) {
         class_1297 holder = entity.method_5933();
         if (holder != null) {
            this.drawLead(entity, tickDelta, matrices, vertexConsumers, holder);
         }
      }

      String speech = entity.getSpeech();
      if (this.field_4676.method_23168(entity) <= 576.0 && !entity.isMale() && entity.field_6213 == 0) {
         this.renderBars(entity, matrices, vertexConsumers, light, !speech.isEmpty());
      }

      if (!speech.isEmpty() && this.field_4676.method_23168(entity) <= 1024.0) {
         this.renderSpeech(entity, speech, matrices, vertexConsumers, light);
      }

      if (mode != 1 && FlyRig.get().ready() && this.method_4071(entity)) {
         this.method_3926(entity, entity.method_5476(), matrices, vertexConsumers, light);
      }
   }

   private void renderRigged(ConnectomeFlyEntity entity, float yaw, float tickDelta, class_4587 matrices) {
      FlyRig rig = FlyRig.get();
      double t = System.nanoTime() / 1.0E9;
      int id = entity.method_5628();
      FlyAnim anim = this.anims.get(id);
      if (anim == null) {
         anim = new FlyAnim(rig);
         this.anims.put(id, anim);
      }

      this.lastSeen.put(id, System.currentTimeMillis());
      if (this.anims.size() > 1 && (System.nanoTime() & 255L) == 0L) {
         this.gc();
      }

      class_243 p = entity.method_30950(tickDelta);
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

      int block = entity.method_37908().method_8314(class_1944.field_9282, entity.method_24515());
      int sky = entity.method_37908().method_8314(class_1944.field_9284, entity.method_24515()) - entity.method_37908().method_8594();
      float bright = 0.22F + 0.78F * (class_3532.method_15340(Math.max(block, sky), 0, 15) / 15.0F);
      matrices.method_22903();
      class_2350 surf = entity.surface();
      if (surf != null) {
         float h = entity.method_17682();
         float w = entity.method_17681() / 2.0F;
         if (surf == class_2350.field_11036) {
            matrices.method_22904(0.0, h, 0.0);
         } else {
            matrices.method_46416(surf.method_10148() * w, h / 2.0F, surf.method_10165() * w);
         }

         class_243 f = ConnectomeFlyEntity.surfaceForward(surf, yaw);
         class_243 n = new class_243(-surf.method_10148(), -surf.method_10164(), -surf.method_10165());
         class_243 r = n.method_1036(f);
         Matrix4f basis = new Matrix4f(
            (float)r.field_1352,
            (float)r.field_1351,
            (float)r.field_1350,
            0.0F,
            (float)n.field_1352,
            (float)n.field_1351,
            (float)n.field_1350,
            0.0F,
            (float)f.field_1352,
            (float)f.field_1351,
            (float)f.field_1350,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            1.0F
         );
         matrices.method_34425(basis);
         matrices.method_22904(0.0, anim.bob() * 0.9F, 0.0);
      } else {
         matrices.method_22904(0.0, anim.bob() * 0.9F, 0.0);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-yaw));
      }

      if (entity.field_6213 > 0) {
         float f = (entity.field_6213 + tickDelta - 1.0F) / 20.0F * 1.6F;
         f = Math.min(1.0F, (float)Math.sqrt(Math.max(0.0F, f)));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(f * 90.0F));
      }

      matrices.method_22907(class_7833.field_40718.rotation(anim.roll()));
      matrices.method_22907(class_7833.field_40714.rotation(-anim.pitch()));
      float sc = 0.9F * entity.method_17825();
      matrices.method_22905(sc, sc, sc);
      boolean hurt = entity.field_6235 > 0 || entity.field_6213 > 0;
      rig.beginDraw(bright);
      if (hurt) {
         RenderSystem.setShaderColor(bright, bright * 0.45F, bright * 0.45F, 1.0F);
      }

      rig.drawBody(matrices);
      rig.drawHead(matrices, anim.headYaw(), anim.headNod());
      Matrix4f mv = matrices.method_23760().method_23761();
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
      matrices.method_22909();
   }

   private void drawLead(ConnectomeFlyEntity entity, float tickDelta, class_4587 matrices, class_4597 provider, class_1297 holder) {
      matrices.method_22903();
      class_243 to = holder.method_30951(tickDelta);
      double oy = entity.method_17682() * 0.6;
      double gx = class_3532.method_16436(tickDelta, entity.field_6014, entity.method_23317());
      double gy = class_3532.method_16436(tickDelta, entity.field_6036, entity.method_23318()) + oy;
      double gz = class_3532.method_16436(tickDelta, entity.field_5969, entity.method_23321());
      matrices.method_22904(0.0, oy, 0.0);
      float dx = (float)(to.field_1352 - gx);
      float dy = (float)(to.field_1351 - gy);
      float dz = (float)(to.field_1350 - gz);
      class_4588 vc = provider.getBuffer(class_1921.method_23587());
      Matrix4f m = matrices.method_23760().method_23761();
      float inv = class_3532.method_48119(dx * dx + dz * dz + 1.0E-6F) * 0.045F / 2.0F;
      float sx = dz * inv;
      float sz = dx * inv;
      class_2338 a = class_2338.method_49638(entity.method_5836(tickDelta));
      class_2338 b = class_2338.method_49638(holder.method_5836(tickDelta));
      int bl1 = entity.method_37908().method_8314(class_1944.field_9282, a);
      int bl2 = entity.method_37908().method_8314(class_1944.field_9282, b);
      int sl1 = entity.method_37908().method_8314(class_1944.field_9284, a);
      int sl2 = entity.method_37908().method_8314(class_1944.field_9284, b);

      for (int i = 0; i <= 24; i++) {
         leadPiece(vc, m, dx, dy, dz, bl1, bl2, sl1, sl2, 0.045F, 0.045F, sx, sz, i, false);
      }

      for (int i = 24; i >= 0; i--) {
         leadPiece(vc, m, dx, dy, dz, bl1, bl2, sl1, sl2, 0.045F, 0.0F, sx, sz, i, true);
      }

      matrices.method_22909();
   }

   private static void leadPiece(
      class_4588 vc,
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
      int light = class_765.method_23687(class_3532.method_48781(f, bl1, bl2), class_3532.method_48781(f, sl1, sl2));
      float shade = piece % 2 == (back ? 1 : 0) ? 0.7F : 1.0F;
      float r = 0.5F * shade;
      float g = 0.4F * shade;
      float bb = 0.3F * shade;
      float x = dx * f;
      float y = dy > 0.0F ? dy * f * f : dy - dy * (1.0F - f) * (1.0F - f);
      float z = dz * f;
      vc.method_22918(m, x - sx, y + h2, z + sz).method_22915(r, g, bb, 1.0F).method_22916(light).method_1344();
      vc.method_22918(m, x + sx, y + h1 - h2, z - sz).method_22915(r, g, bb, 1.0F).method_22916(light).method_1344();
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

   private void renderBee(ConnectomeFlyEntity entity, float yaw, float tickDelta, class_4587 matrices, class_4597 vertexConsumers, int light) {
      if (this.dummyBee == null || this.dummyBee.method_37908() != entity.method_37908()) {
         this.dummyBee = (class_4466)class_1299.field_20346.method_5883(entity.method_37908());
         if (this.dummyBee == null) {
            super.method_4072(entity, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
         }
      }

      this.dummyBee.method_24830(entity.method_24828() && !entity.isFlapping());
      this.dummyBee.method_18799(entity.method_18798());
      this.dummyBee.field_6012 = entity.field_6012;
      float animationProgress = entity.field_6012 + tickDelta;
      float limbDistance = entity.field_42108.method_48570(tickDelta);
      float limbAngle = entity.field_42108.method_48572(tickDelta);
      float headYaw = class_3532.method_17821(tickDelta, entity.field_6259, entity.field_6241) - yaw;
      float headPitch = class_3532.method_16439(tickDelta, entity.field_6004, entity.method_36455());
      matrices.method_22903();
      matrices.method_22907(class_7833.field_40716.rotationDegrees(180.0F - yaw));
      matrices.method_22905(-1.0F, -1.0F, 1.0F);
      float bs = entity.method_17825() * 1.45F;
      matrices.method_22905(bs, bs, bs);
      matrices.method_46416(0.0F, -1.501F, 0.0F);
      this.beeModel.method_22111(this.dummyBee, limbAngle, limbDistance, tickDelta);
      this.beeModel.method_22112(this.dummyBee, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      class_4588 vc = vertexConsumers.getBuffer(class_1921.method_23578(BEE_TEXTURE));
      this.beeModel.method_2828(matrices, vc, light, class_4608.field_21444, 1.0F, 1.0F, 1.0F, 1.0F);
      matrices.method_22909();
   }

   private void renderBars(ConnectomeFlyEntity entity, class_4587 matrices, class_4597 vertexConsumers, int light, boolean speechAbove) {
      class_327 tr = this.method_3932();
      int hearts = (int)Math.ceil(entity.method_6032() / 2.0F);
      int maxHearts = (int)Math.ceil(entity.method_6063() / 2.0F);
      StringBuilder hp = new StringBuilder();

      for (int i = 0; i < maxHearts; i++) {
         hp.append((char)(i < hearts ? '♥' : '♡'));
      }

      int food = Math.round(entity.getHunger() / 2.0F);
      StringBuilder hu = new StringBuilder();

      for (int i = 0; i < 10; i++) {
         hu.append((char)(i < food ? '|' : '.'));
      }

      matrices.method_22903();
      matrices.method_22904(0.0, entity.method_17682() + (speechAbove ? 0.45 : 0.45), 0.0);
      matrices.method_22907(this.field_4676.method_24197());
      matrices.method_22905(-0.018F, -0.018F, 0.018F);
      Matrix4f m = matrices.method_23760().method_23761();
      class_2561 h = class_2561.method_43470(hp.toString());
      tr.method_30882(h, -tr.method_27525(h) / 2.0F, -10.0F, -42406, false, m, vertexConsumers, class_6415.field_33994, 0, light);
      class_2561 f = class_2561.method_43470("hunger " + hu);
      tr.method_30882(f, -tr.method_27525(f) / 2.0F, 0.0F, -2052000, false, m, vertexConsumers, class_6415.field_33994, 0, light);
      matrices.method_22909();
   }

   private void renderSpeech(ConnectomeFlyEntity entity, String speech, class_4587 matrices, class_4597 vertexConsumers, int light) {
      class_327 textRenderer = this.method_3932();
      float bgOpacity = class_310.method_1551().field_1690.method_19343(0.25F);
      int background = (int)(bgOpacity * 255.0F) << 24;
      matrices.method_22903();
      matrices.method_22904(0.0, entity.method_17682() + 1.05, 0.0);
      matrices.method_22907(this.field_4676.method_24197());
      matrices.method_22905(-0.025F, -0.025F, 0.025F);
      Matrix4f matrix = matrices.method_23760().method_23761();
      class_2561 line = class_2561.method_43470("\"" + speech + "\"");
      float x = -textRenderer.method_27525(line) / 2.0F;
      textRenderer.method_30882(line, x, -12.0F, 16777215, false, matrix, vertexConsumers, class_6415.field_33994, background, light);
      matrices.method_22905(0.62F, 0.62F, 0.62F);
      Matrix4f labelMatrix = matrices.method_23760().method_23761();
      float lx = -textRenderer.method_27525(SPEECH_LABEL) / 2.0F;
      textRenderer.method_30882(SPEECH_LABEL, lx, -2.0F, 11184810, false, labelMatrix, vertexConsumers, class_6415.field_33994, background, light);
      matrices.method_22909();
   }
}

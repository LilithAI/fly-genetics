package io.flyproject.connectomefly.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.flyproject.connectomefly.entity.FlyEggEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

/**
 * 26.3 port. The original drew a tinted turtle-egg block model. Block models can no longer be drawn that way
 * from an entity renderer, so the egg is a small rounded box with the mod's own egg texture. It grows a little
 * as it nears hatching and wobbles in the last three seconds, as before.
 */
public class FlyEggRenderer extends EntityRenderer<FlyEggEntity, FlyEggRenderer.EggState> {
   private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("connectomefly", "textures/entity/fly_egg.png");

   public static class EggState extends EntityRenderState {
      public int eggAge;
      public int id;
   }

   public FlyEggRenderer(Context ctx) {
      super(ctx);
      this.shadowRadius = 0.1F;
   }

   @Override
   public EggState createRenderState() {
      return new EggState();
   }

   @Override
   public void extractRenderState(FlyEggEntity entity, EggState s, float tickDelta) {
      super.extractRenderState(entity, s, tickDelta);
      s.eggAge = entity.getEggAge();
      s.id = entity.getId();
   }

   @Override
   public void submit(EggState s, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
      float wobble = s.eggAge > 540 ? (float)Math.sin(s.ageInTicks * 1.4) * 6.0F : 0.0F;
      float grow = 1.0F + 0.25F * Math.min(1.0F, s.eggAge / 600.0F);
      matrices.pushPose();
      matrices.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(s.id * 37.0F % 360.0F)));
      matrices.mulPose(new Matrix4f().rotation(Axis.ZP.rotationDegrees(wobble)));
      matrices.scale(grow, grow, grow);
      int light = s.lightCoords;
      collector.submitCustomGeometry(matrices, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
         box(vc, pose, -0.09F, 0.0F, -0.07F, 0.09F, 0.16F, 0.07F, light);
         box(vc, pose, -0.07F, 0.16F, -0.05F, 0.07F, 0.21F, 0.05F, light);
      });
      matrices.popPose();
      super.submit(s, matrices, collector, camera);
   }

   private static void box(VertexConsumer vc, PoseStack.Pose p, float x0, float y0, float z0, float x1, float y1, float z1, int light) {
      quad(vc, p, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0, 0, 1, light);
      quad(vc, p, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, 0, 0, -1, light);
      quad(vc, p, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, 1, 0, 0, light);
      quad(vc, p, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, -1, 0, 0, light);
      quad(vc, p, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, 0, 1, 0, light);
      quad(vc, p, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, 0, -1, 0, light);
   }

   private static void quad(
      VertexConsumer vc, PoseStack.Pose p, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, float nx, float ny, float nz, int light
   ) {
      v(vc, p, ax, ay, az, 0.0F, 1.0F, nx, ny, nz, light);
      v(vc, p, bx, by, bz, 1.0F, 1.0F, nx, ny, nz, light);
      v(vc, p, cx, cy, cz, 1.0F, 0.0F, nx, ny, nz, light);
      v(vc, p, dx, dy, dz, 0.0F, 0.0F, nx, ny, nz, light);
   }

   private static void v(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z, float u, float w, float nx, float ny, float nz, int light) {
      vc.addVertex(p, x, y, z).setColor(255, 255, 255, 255).setUv(u, w).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
   }
}

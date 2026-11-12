package io.flyproject.connectomefly.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_291;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_7833;
import net.minecraft.class_291.class_8555;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class FlyRig {
   public static final class_2960 TEXTURE = new class_2960("connectomefly", "textures/entity/fly_model.png");
   private static FlyRig instance;
   private boolean loaded;
   private boolean failed;
   public Matrix4f model = new Matrix4f();
   public FlyRig.Bone[] bones;
   public FlyRig.Leg[] legs;
   public FlyRig.Wing[] wings;
   public FlyRig.Group legsG;
   public FlyRig.Group wingLG;
   public FlyRig.Group wingRG;
   private class_291 body;
   private class_291 head;
   public static final float[] HEAD_PIVOT = new float[]{0.0F, 0.5F, 0.29F};
   public float[] hingeL = new float[]{-0.106F, 0.573F, -0.092F};
   public float[] hingeR = new float[]{0.094F, 0.573F, -0.092F};
   private Matrix4f[] world;
   private Matrix4f[] skin;
   private final Matrix4f tmp = new Matrix4f();
   private final Vector4f v4 = new Vector4f();

   private FlyRig() {
   }

   public static FlyRig get() {
      if (instance == null) {
         instance = new FlyRig();
      }

      instance.ensureLoaded();
      return instance;
   }

   public boolean ready() {
      return this.loaded;
   }

   private void ensureLoaded() {
      if (!this.loaded && !this.failed) {
         try {
            long t0 = System.currentTimeMillis();
            this.readBones();
            this.body = this.loadRigid("body");

            try {
               this.head = this.loadRigid("head");
            } catch (Exception var4) {
               this.head = null;
            }

            this.legsG = this.loadSkinned("legs");
            this.wingLG = this.loadSkinned("wing_l");
            this.wingRG = this.loadSkinned("wing_r");
            this.world = new Matrix4f[this.bones.length + 1];
            this.skin = new Matrix4f[this.bones.length + 1];

            for (int i = 0; i <= this.bones.length; i++) {
               this.world[i] = new Matrix4f();
               this.skin[i] = new Matrix4f();
            }

            this.loaded = true;
            System.out.println("[connectomefly] fly model loaded (" + this.bones.length + " animated bones) in " + (System.currentTimeMillis() - t0) + " ms");
         } catch (Exception var5) {
            this.failed = true;
            System.err.println("[connectomefly] fly model failed to load, using the box model: " + var5);
         }
      }
   }

   private static InputStream res(String path) throws IOException {
      Optional<class_3298> r = class_310.method_1551().method_1478().method_14486(new class_2960("connectomefly", path));
      if (r.isEmpty()) {
         throw new IOException("missing " + path);
      } else {
         return r.get().method_14482();
      }
   }

   private static Matrix4f mat(JsonArray a) {
      float[] f = new float[16];

      for (int i = 0; i < 16; i++) {
         f[i] = a.get(i).getAsFloat();
      }

      return new Matrix4f().set(f);
   }

   private void readBones() throws IOException {
      JsonObject j;
      try (InputStream in = res("fly/bones.json")) {
         j = (JsonObject)new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
      }

      this.model = mat(j.getAsJsonArray("model"));
      JsonArray bj = j.getAsJsonArray("bones");
      this.bones = new FlyRig.Bone[bj.size()];

      for (int i = 0; i < bj.size(); i++) {
         JsonObject b = bj.get(i).getAsJsonObject();
         FlyRig.Bone bone = new FlyRig.Bone();
         bone.name = b.get("name").getAsString();
         bone.parent = b.get("parent").getAsInt();
         if (b.has("parentWorld")) {
            bone.parentWorld = mat(b.getAsJsonArray("parentWorld"));
         }

         JsonArray t = b.getAsJsonArray("t");
         bone.t = new Vector3f(t.get(0).getAsFloat(), t.get(1).getAsFloat(), t.get(2).getAsFloat());
         JsonArray q = b.getAsJsonArray("q");
         bone.rest = new Quaternionf(q.get(0).getAsFloat(), q.get(1).getAsFloat(), q.get(2).getAsFloat(), q.get(3).getAsFloat());
         bone.invBind = mat(b.getAsJsonArray("invBind"));
         this.bones[i] = bone;
      }

      JsonArray lj = j.getAsJsonArray("legs");
      this.legs = new FlyRig.Leg[lj.size()];

      for (int i = 0; i < lj.size(); i++) {
         JsonObject l = lj.get(i).getAsJsonObject();
         FlyRig.Leg leg = new FlyRig.Leg();
         leg.root = l.get("root").getAsInt();
         JsonArray k = l.getAsJsonArray("kids");
         leg.kids = new int[k.size()];

         for (int m = 0; m < k.size(); m++) {
            leg.kids[m] = k.get(m).getAsInt();
         }

         leg.sign = l.get("sign").getAsFloat();
         leg.row = l.get("row").getAsInt();
         leg.grp = l.get("grp").getAsInt();
         this.legs[i] = leg;
      }

      JsonArray wj = j.getAsJsonArray("wings");
      this.wings = new FlyRig.Wing[wj.size()];

      for (int i = 0; i < wj.size(); i++) {
         JsonObject w = wj.get(i).getAsJsonObject();
         FlyRig.Wing wing = new FlyRig.Wing();
         wing.root = w.get("root").getAsInt();
         wing.sign = w.get("sign").getAsFloat();
         wing.left = w.get("side").getAsString().equals("wing_l");
         this.wings[i] = wing;
      }

      if (j.has("hinge_l")) {
         JsonArray h = j.getAsJsonArray("hinge_l");
         this.hingeL = new float[]{h.get(0).getAsFloat(), h.get(1).getAsFloat(), h.get(2).getAsFloat()};
         h = j.getAsJsonArray("hinge_r");
         this.hingeR = new float[]{h.get(0).getAsFloat(), h.get(1).getAsFloat(), h.get(2).getAsFloat()};
      }
   }

   private class_291 loadRigid(String name) throws IOException {
      byte[] raw;
      try (InputStream in = res("fly/" + name + ".fmesh2")) {
         raw = in.readAllBytes();
      }

      ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
      int nVerts = bb.getInt();
      int nIdx = bb.getInt();
      float[] v = new float[nVerts * 5];
      int[] c = new int[nVerts];

      for (int i = 0; i < nVerts; i++) {
         v[i * 5] = bb.getFloat();
         v[i * 5 + 1] = bb.getFloat();
         v[i * 5 + 2] = bb.getFloat();
         v[i * 5 + 3] = bb.getFloat();
         v[i * 5 + 4] = bb.getFloat();
         c[i] = bb.getInt();
      }

      class_287 builder = new class_287(nIdx * 24 + 1024);
      builder.method_1328(class_5596.field_27379, class_290.field_1575);

      for (int i = 0; i < nIdx; i++) {
         int k = bb.getInt();
         int col = c[k];
         builder.method_22912(v[k * 5], v[k * 5 + 1], v[k * 5 + 2])
            .method_22913(v[k * 5 + 3], v[k * 5 + 4])
            .method_1336(col & 0xFF, col >> 8 & 0xFF, col >> 16 & 0xFF, 255)
            .method_1344();
      }

      class_291 vb = new class_291(class_8555.field_44793);
      vb.method_1353();
      vb.method_1352(builder.method_1326());
      class_291.method_1354();
      return vb;
   }

   private FlyRig.Group loadSkinned(String name) throws IOException {
      byte[] raw;
      try (InputStream in = res("fly/" + name + ".fmesh3")) {
         raw = in.readAllBytes();
      }

      ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
      FlyRig.Group g = new FlyRig.Group();
      g.nVerts = bb.getInt();
      g.nIdx = bb.getInt();
      g.pos = new float[g.nVerts * 3];
      g.uv = new float[g.nVerts * 2];
      g.rgba = new int[g.nVerts];
      g.joints = new byte[g.nVerts * 4];
      g.w = new float[g.nVerts * 4];

      for (int i = 0; i < g.nVerts; i++) {
         g.pos[i * 3] = bb.getFloat();
         g.pos[i * 3 + 1] = bb.getFloat();
         g.pos[i * 3 + 2] = bb.getFloat();
         g.uv[i * 2] = bb.getFloat();
         g.uv[i * 2 + 1] = bb.getFloat();
         g.rgba[i] = bb.getInt();
         int packed = bb.getInt();
         g.joints[i * 4] = (byte)(packed & 0xFF);
         g.joints[i * 4 + 1] = (byte)(packed >> 8 & 0xFF);
         g.joints[i * 4 + 2] = (byte)(packed >> 16 & 0xFF);
         g.joints[i * 4 + 3] = (byte)(packed >>> 24 & 0xFF);
         g.w[i * 4] = bb.getFloat();
         g.w[i * 4 + 1] = bb.getFloat();
         g.w[i * 4 + 2] = bb.getFloat();
         g.w[i * 4 + 3] = bb.getFloat();
      }

      g.idx = new int[g.nIdx];

      for (int i = 0; i < g.nIdx; i++) {
         g.idx[i] = bb.getInt();
      }

      return g;
   }

   public void pose(Quaternionf[] q) {
      this.skin[0].set(this.model);

      for (int i = 0; i < this.bones.length; i++) {
         FlyRig.Bone b = this.bones[i];
         Matrix4f w = this.world[i + 1];
         if (b.parent < 0) {
            w.set(b.parentWorld);
         } else {
            w.set(this.world[b.parent]);
         }

         w.translate(b.t);
         w.rotate(q != null && q[i + 1] != null ? q[i + 1] : b.rest);
         this.skin[i + 1].set(this.model).mul(w).mul(b.invBind);
      }
   }

   public void uploadSkinned(FlyRig.Group g, class_291 vb, float[] outPos, class_287 builder) {
      float[] p = g.pos;
      float[] w = g.w;
      byte[] jn = g.joints;

      for (int i = 0; i < g.nVerts; i++) {
         float x = p[i * 3];
         float y = p[i * 3 + 1];
         float z = p[i * 3 + 2];
         float ox = 0.0F;
         float oy = 0.0F;
         float oz = 0.0F;

         for (int k = 0; k < 4; k++) {
            float wk = w[i * 4 + k];
            if (!(wk <= 0.0F)) {
               Matrix4f m = this.skin[jn[i * 4 + k] & 255];
               ox += wk * (m.m00() * x + m.m10() * y + m.m20() * z + m.m30());
               oy += wk * (m.m01() * x + m.m11() * y + m.m21() * z + m.m31());
               oz += wk * (m.m02() * x + m.m12() * y + m.m22() * z + m.m32());
            }
         }

         outPos[i * 3] = ox;
         outPos[i * 3 + 1] = oy;
         outPos[i * 3 + 2] = oz;
      }

      builder.method_1328(class_5596.field_27379, class_290.field_1575);
      float[] uv = g.uv;
      int[] col = g.rgba;
      int[] idx = g.idx;

      for (int i = 0; i < g.nIdx; i++) {
         int kx = idx[i];
         int c = col[kx];
         builder.method_22912(outPos[kx * 3], outPos[kx * 3 + 1], outPos[kx * 3 + 2])
            .method_22913(uv[kx * 2], uv[kx * 2 + 1])
            .method_1336(c & 0xFF, c >> 8 & 0xFF, c >> 16 & 0xFF, 255)
            .method_1344();
      }

      vb.method_1353();
      vb.method_1352(builder.method_1326());
      class_291.method_1354();
   }

   public void beginDraw(float bright) {
      RenderSystem.setShader(class_757::method_34543);
      RenderSystem.setShaderTexture(0, TEXTURE);
      RenderSystem.setShaderColor(bright, bright, bright, 1.0F);
      RenderSystem.enableDepthTest();
      RenderSystem.disableCull();
   }

   public void endDraw() {
      RenderSystem.enableCull();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void drawBody(class_4587 ms) {
      this.draw(this.body, ms.method_23760().method_23761());
   }

   public boolean hasHead() {
      return this.head != null;
   }

   public void drawHead(class_4587 ms, float yaw, float nod) {
      if (this.head != null) {
         ms.method_22903();
         ms.method_46416(HEAD_PIVOT[0], HEAD_PIVOT[1], HEAD_PIVOT[2]);
         ms.method_22907(class_7833.field_40716.rotation(yaw));
         ms.method_22907(class_7833.field_40714.rotation(nod));
         ms.method_46416(-HEAD_PIVOT[0], -HEAD_PIVOT[1], -HEAD_PIVOT[2]);
         this.draw(this.head, ms.method_23760().method_23761());
         ms.method_22909();
      }
   }

   public void draw(class_291 vb, Matrix4f modelView) {
      vb.method_1353();
      vb.method_34427(new Matrix4f(modelView), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
      class_291.method_1354();
   }

   public static final class Bone {
      public String name;
      public int parent;
      public Matrix4f parentWorld;
      public Vector3f t;
      public Quaternionf rest;
      public Matrix4f invBind;
   }

   public static final class Group {
      public int nVerts;
      public int nIdx;
      public float[] pos;
      public float[] uv;
      public int[] rgba;
      public byte[] joints;
      public float[] w;
      public int[] idx;
   }

   public static final class Leg {
      public int root;
      public int[] kids;
      public float sign;
      public int row;
      public int grp;
   }

   public static final class Wing {
      public int root;
      public float sign;
      public boolean left;
   }
}

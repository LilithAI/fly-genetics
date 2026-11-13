package io.flyproject.connectomefly.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class FlyRig {
   public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("connectomefly", "textures/entity/fly_model.png");
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
   public FlyRig.Group body;
   public FlyRig.Group head;
   /** 26.3 port: simplified copies for flies that are not right at the camera. Index 0 = full detail. */
   public static final int LODS = 3;
   private static final int[] LOD_CELLS = {0, 44, 16};
   public final FlyRig.Group[] bodyL = new FlyRig.Group[LODS];
   public final FlyRig.Group[] headL = new FlyRig.Group[LODS];
   public final FlyRig.Group[] legsL = new FlyRig.Group[LODS];
   public final FlyRig.Group[] wingLL = new FlyRig.Group[LODS];
   public final FlyRig.Group[] wingRL = new FlyRig.Group[LODS];
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

            StringBuilder tri = new StringBuilder();

            for (int l = 0; l < LODS; l++) {
               this.bodyL[l] = l == 0 ? this.body : decimate(this.body, LOD_CELLS[l]);
               this.headL[l] = this.head == null ? null : (l == 0 ? this.head : decimate(this.head, LOD_CELLS[l]));
               this.legsL[l] = l == 0 ? this.legsG : decimate(this.legsG, LOD_CELLS[l] * 2);
               this.wingLL[l] = l == 0 ? this.wingLG : decimate(this.wingLG, LOD_CELLS[l]);
               this.wingRL[l] = l == 0 ? this.wingRG : decimate(this.wingRG, LOD_CELLS[l]);
               int n = this.bodyL[l].nIdx + (this.headL[l] == null ? 0 : this.headL[l].nIdx) + this.legsL[l].nIdx + this.wingLL[l].nIdx + this.wingRL[l].nIdx;
               tri.append(l == 0 ? "" : ", ").append(n / 3);
            }

            this.loaded = true;
            System.out
               .println(
                  "[connectomefly] fly model loaded ("
                     + this.bones.length
                     + " animated bones) in "
                     + (System.currentTimeMillis() - t0)
                     + " ms; triangles per detail level: "
                     + tri
               );
         } catch (Exception var5) {
            this.failed = true;
            System.err.println("[connectomefly] fly model failed to load, using the box model: " + var5);
         }
      }
   }

   private static InputStream res(String path) throws IOException {
      Optional<Resource> r = Minecraft.getInstance().getResourceManager().getResource(Identifier.fromNamespaceAndPath("connectomefly", path));
      if (r.isEmpty()) {
         throw new IOException("missing " + path);
      } else {
         return r.get().open();
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

   private FlyRig.Group loadRigid(String name) throws IOException {
      byte[] raw;
      try (InputStream in = res("fly/" + name + ".fmesh2")) {
         raw = in.readAllBytes();
      }

      ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
      FlyRig.Group g = new FlyRig.Group();
      g.nVerts = bb.getInt();
      g.nIdx = bb.getInt();
      g.pos = new float[g.nVerts * 3];
      g.uv = new float[g.nVerts * 2];
      g.rgba = new int[g.nVerts];

      for (int i = 0; i < g.nVerts; i++) {
         g.pos[i * 3] = bb.getFloat();
         g.pos[i * 3 + 1] = bb.getFloat();
         g.pos[i * 3 + 2] = bb.getFloat();
         g.uv[i * 2] = bb.getFloat();
         g.uv[i * 2 + 1] = bb.getFloat();
         g.rgba[i] = bb.getInt();
      }

      g.idx = new int[g.nIdx];

      for (int i = 0; i < g.nIdx; i++) {
         g.idx[i] = bb.getInt();
      }

      return g;
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

   /**
    * A simplified copy of a mesh: vertices that fall in the same cell of a grid (cells along the longest side)
    * are merged into the first one seen, and triangles that collapse are dropped. No holes, fewer triangles.
    */
   private static FlyRig.Group decimate(FlyRig.Group g, int cells) {
      float[] lo = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
      float[] hi = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

      for (int i = 0; i < g.nVerts; i++) {
         for (int k = 0; k < 3; k++) {
            lo[k] = Math.min(lo[k], g.pos[i * 3 + k]);
            hi[k] = Math.max(hi[k], g.pos[i * 3 + k]);
         }
      }

      float size = Math.max(hi[0] - lo[0], Math.max(hi[1] - lo[1], hi[2] - lo[2])) / cells;
      java.util.HashMap<Long, Integer> cellToNew = new java.util.HashMap<>();
      int[] remap = new int[g.nVerts];
      int[] keep = new int[g.nVerts];
      int n = 0;

      for (int i = 0; i < g.nVerts; i++) {
         long cx = (long)((g.pos[i * 3] - lo[0]) / size);
         long cy = (long)((g.pos[i * 3 + 1] - lo[1]) / size);
         long cz = (long)((g.pos[i * 3 + 2] - lo[2]) / size);
         long key = cx << 42 | cy << 21 | cz;
         Integer have = cellToNew.get(key);
         if (have == null) {
            cellToNew.put(key, n);
            keep[n] = i;
            have = n++;
         }

         remap[i] = have;
      }

      FlyRig.Group o = new FlyRig.Group();
      o.nVerts = n;
      o.pos = new float[n * 3];
      o.uv = new float[n * 2];
      o.rgba = new int[n];
      if (g.joints != null) {
         o.joints = new byte[n * 4];
         o.w = new float[n * 4];
      }

      for (int j = 0; j < n; j++) {
         int i = keep[j];
         System.arraycopy(g.pos, i * 3, o.pos, j * 3, 3);
         System.arraycopy(g.uv, i * 2, o.uv, j * 2, 2);
         o.rgba[j] = g.rgba[i];
         if (g.joints != null) {
            System.arraycopy(g.joints, i * 4, o.joints, j * 4, 4);
            System.arraycopy(g.w, i * 4, o.w, j * 4, 4);
         }
      }

      int[] idx = new int[g.nIdx];
      int m = 0;

      for (int i = 0; i + 2 < g.nIdx; i += 3) {
         int a = remap[g.idx[i]];
         int b = remap[g.idx[i + 1]];
         int c = remap[g.idx[i + 2]];
         if (a != b && b != c && a != c) {
            idx[m++] = a;
            idx[m++] = b;
            idx[m++] = c;
         }
      }

      o.idx = java.util.Arrays.copyOf(idx, m);
      o.nIdx = m;
      return o;
   }

   /** CPU skinning: writes the posed vertex positions of g into outPos. */
   public void skin(FlyRig.Group g, float[] outPos) {
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

   }

   /**
    * 26.3 port: draw a mesh through an entity render type. The entity vertex format is quads, so each
    * triangle is sent as a quad with its last corner repeated. stride > 1 skips triangles (distant flies).
    */
   /** time spent sending vertices, and how many triangles, since the last reset (for performance checks) */
   public static long emitNanos;
   public static long emitTris;

   public static void emit(VertexConsumer vc, PoseStack.Pose pose, FlyRig.Group g, float[] pos, int light, int overlay, int alpha, int stride) {
      long t0 = System.nanoTime();
      float[] uv = g.uv;
      int[] col = g.rgba;
      int[] idx = g.idx;
      int step = 3 * Math.max(1, stride);

      for (int i = 0; i + 2 < g.nIdx; i += step) {
         int a = idx[i];
         int b = idx[i + 1];
         int c = idx[i + 2];
         float ax = pos[a * 3], ay = pos[a * 3 + 1], az = pos[a * 3 + 2];
         float bx = pos[b * 3], by = pos[b * 3 + 1], bz = pos[b * 3 + 2];
         float cx = pos[c * 3], cy = pos[c * 3 + 1], cz = pos[c * 3 + 2];
         float ux = bx - ax, uy = by - ay, uz = bz - az;
         float vx = cx - ax, vy = cy - ay, vz = cz - az;
         float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
         float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
         if (len < 1.0E-12F) {
            nx = 0.0F;
            ny = 1.0F;
            nz = 0.0F;
         } else {
            nx /= len;
            ny /= len;
            nz /= len;
         }

         vert(vc, pose, ax, ay, az, uv, a, col[a], alpha, light, overlay, nx, ny, nz);
         vert(vc, pose, bx, by, bz, uv, b, col[b], alpha, light, overlay, nx, ny, nz);
         vert(vc, pose, cx, cy, cz, uv, c, col[c], alpha, light, overlay, nx, ny, nz);
         vert(vc, pose, cx, cy, cz, uv, c, col[c], alpha, light, overlay, nx, ny, nz);
      }

      emitNanos += System.nanoTime() - t0;
      emitTris += g.nIdx / step;
   }

   private static void vert(
      VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float[] uv, int k, int c, int alpha, int light, int overlay, float nx, float ny, float nz
   ) {
      vc.addVertex(pose, x, y, z)
         .setColor(c & 0xFF, c >> 8 & 0xFF, c >> 16 & 0xFF, alpha)
         .setUv(uv[k * 2], uv[k * 2 + 1])
         .setOverlay(overlay)
         .setLight(light)
         .setNormal(pose, nx, ny, nz);
   }

   public boolean hasHead() {
      return this.head != null;
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

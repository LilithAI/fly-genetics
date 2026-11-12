import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.flyproject.connectomefly.neural.WholeBrain;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.SplittableRandom;

/**
 * [model] A copy of the step and plasticity rules of Connectome Fly's WholeBrain
 * (by ro0oney, flyproject.io, CC BY 4.0) in which every tuning constant is a per-instance
 * field instead of a static final, and each circuit-gain group has a per-instance multiplier.
 * It uses the ORIGINAL shared wiring from WholeBrain.data(); only state and tuning are per brain.
 * Runs as fast as the CPU allows (no wall clock) with a fixed seed, for sweeps.
 */
public final class ParamBrain {
   // order matters: index into p[]
   public static final String[] NAMES = {"TAU_M", "V_TH", "TAU_SYN", "DRIVE_RATE", "DRIVE_AMP", "DN_DRIVE_RATE", "SYN_MULT",
      "SENS_MULT", "INH_MULT", "ADAPT_INC", "ADAPT_TAU", "BUS_RATE", "ETA", "FORGET"};
   public static final float[] BASE = {20f, 1.0f, 5f, 0.001f, 10f, 0.004f, 2.2f, 4.0f, 1.1f, 0.4f, 250f, 0.05f, 0.06f, 2.0e-5f};
   static final int TAU_M = 0, V_TH = 1, TAU_SYN = 2, DRIVE_RATE = 3, DRIVE_AMP = 4, DN_DRIVE_RATE = 5, SYN_MULT = 6, SENS_MULT = 7,
      INH_MULT = 8, ADAPT_INC = 9, ADAPT_TAU = 10, BUS_RATE = 11, ETA = 12, FORGET = 13;

   // circuit-gain groups, from the mod's brain_circuit_gains.json
   public static String[] groupNames;
   private static byte[] edgeGroups; // bitmask of groups per synapse
   private static boolean anyGroup;

   public final float[] p = BASE.clone();
   public float[] groupMul; // per-brain multiplier per group, on top of the author's gain
   private float[] maskMul; // product per bitmask
   private boolean groupsActive;

   final WholeBrain.Data d;
   final float[] v, isyn, a;
   final int[] spk;
   final int[][] refBuf;
   final int[] refN;
   int refCur;
   public final long[] chanSpikeAcc, outSpikeAcc;
   public long dnL, dnR, spikes, steps;
   public final float[] plastScale, kcTrace;
   int[] kcList;
   float pamLP, ppl1LP, pamBase = -1, ppl1Base = -1;
   int plastTick;
   final int pamOut, ppl1Out;
   final SplittableRandom rng;
   public float[] bus = new float[0];
   public float compassHeading = Float.NaN, compassStrength;
   public double epgCos, epgSin; public long epgN;
   final int[] everSpiked;

   public static synchronized void loadGroups(WholeBrain.Data d) throws Exception {
      if (groupNames != null) return;
      try (InputStream in = WholeBrain.class.getResourceAsStream("/data/connectomefly/brain_circuit_gains.json")) {
         JsonObject j = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
         String[] names = j.keySet().toArray(new String[0]);
         byte[] eg = new byte[d.nEdges];
         int overlap = 0;
         for (int g = 0; g < names.length; g++) {
            JsonObject c = j.getAsJsonObject(names[g]);
            boolean[] src = new boolean[d.n], dst = new boolean[d.n];
            for (JsonElement e : c.getAsJsonArray("src")) { int i = e.getAsInt(); if (i >= 0 && i < d.n) src[i] = true; }
            for (JsonElement e : c.getAsJsonArray("dst")) { int i = e.getAsInt(); if (i >= 0 && i < d.n) dst[i] = true; }
            int ne = 0;
            for (int i = 0; i < d.n; i++) if (src[i]) for (int q = d.indptr[i]; q < d.indptr[i + 1]; q++) if (dst[d.indices[q]]) {
               if (eg[q] != 0) overlap++;
               eg[q] |= (byte) (1 << g); ne++;
            }
            System.out.println("# group " + names[g] + " author gain " + c.get("gain").getAsFloat() + " synapses " + ne);
         }
         System.out.println("# synapses in more than one group: " + overlap);
         edgeGroups = eg; groupNames = names;
      }
   }

   public ParamBrain(long seed) throws Exception {
      d = WholeBrain.data();
      loadGroups(d);
      groupMul = new float[groupNames.length];
      Arrays.fill(groupMul, 1f);
      int n = d.n;
      v = new float[n]; isyn = new float[n]; a = new float[n]; spk = new int[n];
      refBuf = new int[2][n]; refN = new int[2];
      chanSpikeAcc = new long[d.chanNames.length]; outSpikeAcc = new long[d.outNames.length];
      plastScale = new float[d.nPlastic]; Arrays.fill(plastScale, 1f);
      kcTrace = new float[n];
      pamOut = d.out("pam"); ppl1Out = d.out("ppl1");
      rng = new SplittableRandom(seed);
      everSpiked = new int[n];
   }

   public void applyGroups() {
      groupsActive = false;
      for (float m : groupMul) if (m != 1f) groupsActive = true;
      maskMul = new float[1 << groupNames.length];
      for (int m = 0; m < maskMul.length; m++) {
         float x = 1f;
         for (int g = 0; g < groupNames.length; g++) if ((m & (1 << g)) != 0) x *= groupMul[g];
         maskMul[m] = x;
      }
   }

   public void setBus(String... chans) {
      float[] b = new float[d.chanNames.length];
      for (String c : chans) { int i = d.channel(c); if (i < 0) throw new IllegalArgumentException(c); b[i] = 1f; }
      bus = b;
   }

   public void resetCounters() {
      Arrays.fill(chanSpikeAcc, 0); Arrays.fill(outSpikeAcc, 0); Arrays.fill(everSpiked, 0);
      dnL = dnR = spikes = steps = 0; epgCos = epgSin = 0; epgN = 0;
   }

   public int activeNeurons() { int k = 0; for (int x : everSpiked) if (x > 0) k++; return k; }

   public float[] memory() {
      double sa = 0, sp = 0; int na = 0, np = 0;
      for (int q = 0; q < plastScale.length; q++) {
         byte c = d.mbonClass[d.plastTarget[q]];
         if (c == 1) { sa += plastScale[q]; na++; } else if (c == 2) { sp += plastScale[q]; np++; }
      }
      return new float[]{na > 0 ? (float) (sa / na) : 1f, np > 0 ? (float) (sp / np) : 1f};
   }

   public void run(int nSteps) { for (int s = 0; s < nSteps; s++) step(); }

   void step() {
      final int n = d.n;
      final float[] V = v, I = isyn, A = a;
      final float decay = (float) Math.exp(-1.0 / p[TAU_SYN]);
      final float adecay = (float) Math.exp(-1.0 / p[ADAPT_TAU]);
      final float k = 1.0f / p[TAU_M];
      final float vth = p[V_TH], amp = p[DRIVE_AMP];
      for (int i = 0; i < n; i++) {
         float is = I[i] * decay; I[i] = is;
         float ai = A[i] * adecay; A[i] = ai;
         V[i] += k * (is - ai - V[i]);
      }
      int[] sens = d.sensIdx;
      float nk = sens.length * p[DRIVE_RATE];
      int nki = (int) nk + (rng.nextFloat() < nk - (int) nk ? 1 : 0);
      for (int j = 0; j < nki; j++) I[sens[rng.nextInt(sens.length)]] += amp;
      int[] dns = d.dnIdx;
      float nd = dns.length * p[DN_DRIVE_RATE];
      int ndi = (int) nd + (rng.nextFloat() < nd - (int) nd ? 1 : 0);
      for (int j = 0; j < ndi; j++) I[dns[rng.nextInt(dns.length)]] += amp;
      if (compassStrength > 0f && !Float.isNaN(compassHeading)) {
         int[] eidx = d.epgIdx;
         for (int q = 0; q < eidx.length; q++) {
            float c = (float) Math.cos(d.epgAngle[eidx[q]] - compassHeading);
            float tune = c > 0.3f ? (c - 0.3f) / 0.7f : 0f;
            float nb = 0.15f * compassStrength * tune;
            if (nb > 0f && rng.nextFloat() < nb) I[eidx[q]] += amp;
         }
      }
      float[] b = bus;
      for (int c = 0; c < b.length && c < d.chanIdx.length; c++) {
         float val = b[c];
         if (!(val <= 0f)) {
            int[] idx = d.chanIdx[c];
            float nb = idx.length * p[BUS_RATE] * val;
            int nbi = (int) nb + (rng.nextFloat() < nb - (int) nb ? 1 : 0);
            for (int j = 0; j < nbi; j++) I[idx[rng.nextInt(idx.length)]] += amp;
         }
      }
      for (int r = 0; r < 2; r++) { int[] buf = refBuf[r]; for (int j = 0, m = refN[r]; j < m; j++) V[buf[j]] = 0f; }
      int sn = 0;
      final int[] indptr = d.indptr, indices = d.indices, ppos = d.plastPos;
      final float[] w = d.weight, pscale = plastScale;
      final byte[] sensory = d.sensory, dnclass = d.dnclass, nchan = d.neuronChan, eg = edgeGroups;
      final long[] omask = d.neuronOutMask;
      final boolean[] isKC = d.isKC;
      final boolean ga = groupsActive;
      final float[] mm = maskMul;
      int pamNow = 0, ppl1Now = 0;
      for (int i = 0; i < n; i++) {
         if (V[i] >= vth) {
            V[i] = 0f;
            spk[sn++] = i;
            everSpiked[i]++;
            int s = indptr[i], e = indptr[i + 1];
            float g = sensory[i] != 0 ? p[SYN_MULT] * p[SENS_MULT] : p[SYN_MULT];
            float gi = g * p[INH_MULT];
            boolean kc = isKC[i];
            if (kc) kcTrace[i] = Math.min(1f, kcTrace[i] + 0.34f);
            for (int q = s; q < e; q++) {
               float ww = w[q];
               if (kc) { int pp = ppos[q]; if (pp >= 0) ww *= pscale[pp]; }
               if (ga) { byte m = eg[q]; if (m != 0) ww *= mm[m & 0xff]; }
               I[indices[q]] += ww > 0f ? ww * g : ww * gi;
            }
            if (!Float.isNaN(d.epgAngle[i])) { epgCos += Math.cos(d.epgAngle[i]); epgSin += Math.sin(d.epgAngle[i]); epgN++; }
            if (sensory[i] == 0) A[i] += p[ADAPT_INC];
            byte dc = dnclass[i];
            if (dc == 1) dnL++; else if (dc == 2) dnR++;
            byte ch = nchan[i];
            if (ch >= 0) chanSpikeAcc[ch]++;
            for (long om = omask[i]; om != 0L; om &= om - 1L) {
               int ob = Long.numberOfTrailingZeros(om);
               outSpikeAcc[ob]++;
               if (ob == pamOut) pamNow++; else if (ob == ppl1Out) ppl1Now++;
            }
         }
      }
      plasticity(pamNow, ppl1Now);
      int[] cur = refBuf[refCur];
      System.arraycopy(spk, 0, cur, 0, sn);
      refN[refCur] = sn;
      refCur = (refCur + 1) % 2;
      spikes += sn; steps++;
   }

   private static final float TRACE_DECAY20 = (float) Math.pow((float) Math.exp(-6.666666666666666E-4), 20.0);

   private void plasticity(int pamNow, int ppl1Now) {
      pamLP += (pamNow - pamLP) * 0.01f;
      ppl1LP += (ppl1Now - ppl1LP) * 0.01f;
      float[] tr = kcTrace;
      if (++plastTick < 20) return;
      plastTick = 0;
      if (pamBase < 0f) { pamBase = pamLP; ppl1Base = ppl1LP; }
      float pamGate = Math.max(0f, pamLP - Math.max(pamBase * 1.8f, 0.4f));
      float ppl1Gate = Math.max(0f, ppl1LP - Math.max(ppl1Base * 1.8f, 0.04f));
      if (pamGate <= 0f) pamBase += (pamLP - pamBase) * 0.01f;
      if (ppl1Gate <= 0f) ppl1Base += (ppl1LP - ppl1Base) * 0.01f;
      pamGate = Math.min(1f, pamGate / 2.0f);
      ppl1Gate = Math.min(1f, ppl1Gate / 0.12f);
      int[] src = d.plastSource, tgt = d.plastTarget;
      byte[] mc = d.mbonClass;
      float[] sc = plastScale;
      boolean gated = pamGate > 0f || ppl1Gate > 0f;
      float elig = 0.5f;
      if (gated) {
         if (kcList == null) kcList = Arrays.stream(src).distinct().toArray();
         double m = 0, m2 = 0;
         for (int i : kcList) { m += tr[i]; m2 += tr[i] * tr[i]; }
         m /= kcList.length;
         double sd = Math.sqrt(Math.max(0.0, m2 / kcList.length - m * m));
         elig = (float) Math.max(0.5, m + 1.5 * sd);
      }
      final float eta = p[ETA], forget = p[FORGET];
      for (int q = 0; q < sc.length; q++) {
         float x = sc[q];
         if (gated) {
            float t = tr[src[q]];
            if (t > elig) {
               byte c = mc[tgt[q]];
               if (c == 1 && pamGate > 0f) x *= 1f - eta * 0.12f * t * pamGate;       // original: 0.0072 = ETA * 0.12
               else if (c == 2 && ppl1Gate > 0f) x *= 1f - eta * 4.0f * t * ppl1Gate; // original: 0.24 = ETA * 4
            }
         }
         x += (1f - x) * forget;
         sc[q] = Math.max(0.05f, x);
      }
      for (int i = 0; i < tr.length; i++) if (tr[i] > 0f) tr[i] = tr[i] < 0.01f ? 0f : tr[i] * TRACE_DECAY20;
   }
}

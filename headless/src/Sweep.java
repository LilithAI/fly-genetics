import io.flyproject.connectomefly.neural.WholeBrain;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Step 1 sweeps. Modes:
 *   single  - one constant or circuit group at a time, several multipliers
 *   seeds   - baseline with different random seeds (noise floor)
 *   random N BAND - N brains with every constant and group drawn uniformly within +-BAND of baseline
 * Writes one CSV row per run.
 */
public class Sweep {
   static final String[] OUTS = {"dn_giant_fiber", "mn_jump", "mn_proboscis", "mn_pharynx", "mn_wing_power", "mn_leg_front", "mn_leg_middle",
      "mn_leg_hind", "dn_courtship", "dn_aggression", "dn_egg_laying", "dn_backward", "mbon_avoid", "mbon_approach", "pam", "ppl1", "dfb_sleep", "pc1_receptive", "epg"};
   static WholeBrain.Data D;

   record Job(String label, float[] pm, float[] gm, long seed) {}

   public static void main(String[] a) throws Exception {
      D = WholeBrain.data();
      ParamBrain.loadGroups(D);
      int nP = ParamBrain.NAMES.length, nG = ParamBrain.groupNames.length;
      List<Job> jobs = new ArrayList<>();
      String mode = a[0], outFile = a[1];
      if (mode.equals("seeds")) {
         for (int s = 1; s <= Integer.parseInt(a[2]); s++) jobs.add(new Job("baseline", ones(nP), ones(nG), s));
      } else if (mode.equals("single")) {
         float[] ms = {0.5f, 0.7f, 0.8f, 0.9f, 1.1f, 1.2f, 1.3f, 1.5f, 2.0f};
         for (int i = 0; i < nP; i++) for (float m : ms) { float[] pm = ones(nP); pm[i] = m; jobs.add(new Job(ParamBrain.NAMES[i] + " x" + m, pm, ones(nG), 1)); }
         float[] gs = {0f, 0.25f, 0.5f, 0.8f, 1.25f, 2f, 4f};
         for (int g = 0; g < nG; g++) for (float m : gs) { float[] gm = ones(nG); gm[g] = m; jobs.add(new Job("gain:" + ParamBrain.groupNames[g] + " x" + m, ones(nP), gm, 1)); }
      } else if (mode.equals("random")) {
         int N = Integer.parseInt(a[2]); float band = Float.parseFloat(a[3]);
         float gband = a.length > 4 ? Float.parseFloat(a[4]) : band;
         SplittableRandom r = new SplittableRandom(4242);
         for (int k = 0; k < N; k++) {
            float[] pm = ones(nP), gm = ones(nG);
            for (int i = 0; i < nP; i++) pm[i] = 1f + band * (float) (r.nextDouble() * 2 - 1);
            for (int g = 0; g < nG; g++) gm[g] = (float) Math.pow(1.0 + gband, r.nextDouble() * 2 - 1);
            jobs.add(new Job("random" + k, pm, gm, 1));
         }
      }
      else if (mode.equals("bands")) {
         // per-gene bands [lo, hi] as multipliers, chosen from the one-at-a-time sweep; args: N scale
         int N = Integer.parseInt(a[2]); float sc = Float.parseFloat(a[3]);
         float[][] bd = Bands.CONST;
         SplittableRandom r = new SplittableRandom(777);
         for (int k = 0; k < N; k++) {
            float[] pm = ones(nP), gm = ones(nG);
            for (int i = 0; i < nP; i++) { double u = r.nextDouble(); pm[i] = (float) (1 + sc * ((bd[i][0] - 1) + u * (bd[i][1] - bd[i][0]))); }
            for (int g = 0; g < nG; g++) gm[g] = (float) Math.pow(2.0, sc * (r.nextDouble() * 2 - 1));
            jobs.add(new Job("bands" + k, pm, gm, 1));
         }
      }
      int threads = Integer.parseInt(System.getProperty("threads", "4"));
      ExecutorService ex = Executors.newFixedThreadPool(threads);
      List<Future<String>> fs = new ArrayList<>();
      for (Job j : jobs) fs.add(ex.submit(() -> runJob(j)));
      try (PrintWriter w = new PrintWriter(outFile)) {
         StringBuilder h = new StringBuilder("label,seed");
         for (String s : ParamBrain.NAMES) h.append(",m_").append(s);
         for (String s : ParamBrain.groupNames) h.append(",g_").append(s);
         for (String ph : new String[]{"rest", "food", "threat", "recover", "court"}) {
            h.append(',').append(ph).append("_hz,").append(ph).append("_active,").append(ph).append("_dn,").append(ph).append("_turn");
            for (String o : OUTS) h.append(',').append(ph).append('_').append(o);
         }
         h.append(",punish_memAvoid,punish_memApproach,reward_memAvoid,reward_memApproach,ms_per_step");
         w.println(h);
         int done = 0;
         for (Future<String> f : fs) { w.println(f.get()); w.flush(); if (++done % 10 == 0) System.out.println("done " + done + "/" + fs.size()); }
      }
      ex.shutdown();
   }

   static float[] ones(int n) { float[] x = new float[n]; java.util.Arrays.fill(x, 1f); return x; }

   static String runJob(Job j) throws Exception {
      ParamBrain b = new ParamBrain(j.seed);
      for (int i = 0; i < b.p.length; i++) b.p[i] = ParamBrain.BASE[i] * j.pm[i];
      b.groupMul = j.gm.clone();
      b.applyGroups();
      StringBuilder sb = new StringBuilder(j.label).append(',').append(j.seed);
      for (float x : j.pm) sb.append(',').append(x);
      for (float x : j.gm) sb.append(',').append(x);
      long t0 = System.nanoTime();
      b.setBus(); b.run(1000);
      phase(b, sb, 3000);
      phase(b, sb, 3000, "olf_food", "olf_fruit", "gust_sugar", "taste_sugar_labellum");
      phase(b, sb, 3000, "visual", "tactile", "aversive");
      phase(b, sb, 1500);
      phase(b, sb, 2000, "pheromone", "song");
      b.setBus(); b.run(1000);
      b.setBus("olf_food", "punishment"); b.run(3000);
      float[] m = b.memory(); sb.append(',').append(m[0]).append(',').append(m[1]);
      b.setBus(); b.run(1000);
      b.setBus("olf_fruit", "reward"); b.run(3000);
      m = b.memory(); sb.append(',').append(m[0]).append(',').append(m[1]);
      sb.append(',').append((System.nanoTime() - t0) / 1e6 / b.steps * 0 + (System.nanoTime() - t0) / 1e6 / 21500.0);
      return sb.toString();
   }

   static void phase(ParamBrain b, StringBuilder sb, int ms, String... chans) {
      b.setBus(chans);
      b.resetCounters();
      b.run(ms);
      double hz = b.spikes / (double) b.steps * 1000.0 / D.n;
      long tot = b.dnL + b.dnR;
      sb.append(',').append(fmt(hz)).append(',').append(fmt(b.activeNeurons() / (double) D.n)).append(',').append(fmt(tot / (double) b.steps))
         .append(',').append(fmt(tot > 0 ? (b.dnR - b.dnL) / (double) tot : 0));
      for (String o : OUTS) sb.append(',').append(fmt(b.outSpikeAcc[D.out(o)] / (double) b.steps));
   }

   static String fmt(double x) { return String.format("%.5f", x); }
}

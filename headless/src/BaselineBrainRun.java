import io.flyproject.connectomefly.neural.WholeBrain;
import java.util.HashMap;
import java.util.Map;

/**
 * Baseline smoke run of the UNMODIFIED Connectome Fly whole brain, outside Minecraft.
 * Runs one brain in three phases (no input, food smell + sugar taste, looming-like visual + touch)
 * and prints the rates the mod itself reports through WholeBrain.Loco.
 */
public class BaselineBrainRun {
   public static void main(String[] args) throws Exception {
      int secs = args.length > 0 ? Integer.parseInt(args[0]) : 8;
      WholeBrain b = new WholeBrain("baseline");
      b.start();
      long t0 = System.currentTimeMillis();
      while (!b.ready() || b.dataOrNull() == null || b.loco() == null) {
         b.keepAlive();
         Thread.sleep(50);
         if (System.currentTimeMillis() - t0 > 120000) throw new IllegalStateException("brain never became ready");
      }
      WholeBrain.Data d = b.dataOrNull();
      System.out.println("neurons=" + d.n + " synapses=" + d.nEdges + " channels=" + d.chanNames.length
         + " outputs=" + d.outNames.length + " plasticSynapses=" + d.nPlastic);
      phase(b, d, "rest (no input)", new HashMap<>(), secs);
      Map<String, Float> food = new HashMap<>();
      food.put("olf_food", 1f); food.put("olf_fruit", 1f); food.put("gust_sugar", 1f); food.put("taste_sugar_labellum", 1f);
      phase(b, d, "food smell + sugar", food, secs);
      Map<String, Float> threat = new HashMap<>();
      threat.put("visual", 1f); threat.put("tactile", 1f); threat.put("aversive", 1f);
      phase(b, d, "visual + touch + aversive", threat, secs);
      b.stop();
   }

   static void phase(WholeBrain b, WholeBrain.Data d, String name, Map<String, Float> bus, int secs) throws Exception {
      b.setBus(bus);
      int k = 0; double act = 0, walk = 0, sps = 0; double[] out = new double[d.outNames.length];
      long end = System.currentTimeMillis() + secs * 1000L;
      WholeBrain.Loco last = null;
      while (System.currentTimeMillis() < end) {
         b.keepAlive();
         Thread.sleep(100);
         WholeBrain.Loco l = b.loco();
         if (l == last) continue;
         last = l; k++;
         act += l.activity; walk += l.walkDrive; sps += l.stepsPerSec;
         for (int o = 0; o < out.length && o < l.outRate.length; o++) out[o] += l.outRate[o];
      }
      System.out.printf("%n== %s == samples=%d  stepsPerSec=%.0f  spikesPerStep(all neurons)=%.2f  meanRateHz=%.3f  walkDrive=%.4f%n",
         name, k, sps / k, act / k, act / k * 1000.0 / d.n, walk / k);
      StringBuilder sb = new StringBuilder();
      for (int o = 0; o < out.length; o++) sb.append(String.format("%s=%.4f ", d.outNames[o], out[o] / k));
      System.out.println("output spikes/step: " + sb);
   }
}

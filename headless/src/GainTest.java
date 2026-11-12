import io.flyproject.connectomefly.neural.WholeBrain;

/** Step 1: each circuit-gain group tested with an input that actually drives it. Prints a markdown table. */
public class GainTest {
   static WholeBrain.Data D;

   public static void main(String[] a) throws Exception {
      D = WholeBrain.data();
      ParamBrain.loadGroups(D);
      float[] ms = {0f, 0.25f, 0.5f, 0.8f, 1f, 1.25f, 2f, 4f};
      String[][] spec = {
         // group, input channels (or "compass"), readouts
         {"compass_loop", "compass", "epg", "pfl3_left", "pfl3_right"},
         {"compass_out", "compass", "epg", "pfl3_left", "pfl3_right"},
         {"kc_mbon", "olf_food,olf_fruit", "mbon_avoid", "mbon_approach"},
         {"mbon_out", "olf_food,olf_fruit", "mbon_avoid", "mbon_approach", "dn_turn_left", "dn_turn_right", "mn_proboscis"},
         {"lc_out", "small_object_left", "lc_small_left", "dn_turn_left", "dn_turn_right", "dn_giant_fiber", "mn_leg_front"},
         {"aggr_out", "aggression_pc1", "pc1_aggr", "dn_aggression"},
         {"pc1_recurrent", "song_pc1", "pc1_receptive", "vpoen", "dn_courtship"},
      };
      for (String[] sp : spec) {
         int g = java.util.Arrays.asList(ParamBrain.groupNames).indexOf(sp[0]);
         StringBuilder h = new StringBuilder("\n### gain:" + sp[0] + " (input: " + sp[1] + ")\n| x | whole brain Hz |");
         for (int o = 2; o < sp.length; o++) h.append(' ').append(sp[o]).append(" driven | after |");
         if (sp[1].equals("compass")) h.append(" bump amp |");
         System.out.println(h);
         System.out.println(h.toString().lines().skip(2).findFirst().get().replaceAll("[^|]+", "---"));
         for (float m : ms) {
            ParamBrain b = new ParamBrain(1);
            b.groupMul[g] = m; b.applyGroups();
            b.run(1000);
            if (sp[1].equals("compass")) { b.compassHeading = 1.0f; b.compassStrength = 1f; } else b.setBus(sp[1].split(","));
            b.resetCounters(); b.run(3000);
            StringBuilder r = new StringBuilder(String.format("| %s | %.3f |", m == 1f ? "**1**" : String.valueOf(m), b.spikes / 3000.0 * 1000 / D.n));
            double[] on = new double[sp.length];
            for (int o = 2; o < sp.length; o++) on[o] = b.outSpikeAcc[D.out(sp[o])] / 3000.0;
            double amp = b.epgN > 0 ? Math.hypot(b.epgCos, b.epgSin) / b.epgN : 0;
            b.setBus(); b.compassStrength = 0f;
            b.resetCounters(); b.run(2000);
            for (int o = 2; o < sp.length; o++) r.append(String.format(" %.4f | %.4f |", on[o], b.outSpikeAcc[D.out(sp[o])] / 2000.0));
            if (sp[1].equals("compass")) r.append(String.format(" %.2f |", amp));
            System.out.println(r);
         }
      }
   }
}

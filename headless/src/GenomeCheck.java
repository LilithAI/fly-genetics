import io.flyproject.connectomefly.genetics.Genome;
import io.flyproject.connectomefly.neural.WholeBrain;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Step 2 checks against the MOD'S OWN classes (build/classes/java/main), outside Minecraft:
 *  1. inheritance statistics of Genome.child
 *  2. the real WholeBrain with no genome and with a baseline genome matches the original rates
 *  3. single genes move the brain the way the step 1 sweeps predict
 * Exits non-zero if a check fails.
 */
public class GenomeCheck {
   static int fails;

   static void check(boolean ok, String what) {
      System.out.println((ok ? "PASS " : "FAIL ") + what);
      if (!ok) fails++;
   }

   public static void main(String[] a) throws Exception {
      // ---- 1. inheritance
      Random r = new Random(7);
      Genome m = Genome.founder(r), f = Genome.founder(r);
      m.generation = 3; f.generation = 5;
      UUID mid = UUID.randomUUID(), fid = UUID.randomUUID();
      int fromM = 0, fromF = 0, mutated = 0, outOfBand = 0, total = 0, neither = 0;
      int kids = 4000;
      for (int k = 0; k < kids; k++) {
         Genome c = Genome.child(m, f, mid, fid, r);
         if (c.generation != 6 || !mid.equals(c.mother) || !fid.equals(c.father)) fails++;
         mutated += c.mutations;
         for (int i = 0; i < Genome.N; i++) {
            total++;
            if (c.g[i] == m.g[i]) fromM++; else if (c.g[i] == f.g[i]) fromF++; else neither++;
            if (c.g[i] < Genome.GENES[i].lo() || c.g[i] > Genome.GENES[i].hi()) outOfBand++;
         }
      }
      System.out.printf("genes=%d children=%d fromMother=%.3f fromFather=%.3f changed=%.3f mutationsPerChild=%.2f%n", Genome.N, kids,
         fromM / (double) total, fromF / (double) total, neither / (double) total, mutated / (double) kids);
      check(Math.abs(fromM - fromF) / (double) total < 0.02, "genes come from mother and father about equally");
      check(Math.abs(mutated / (double) total - Genome.mutationRate) < 0.01, "mutation rate is about " + Genome.mutationRate);
      check(outOfBand == 0, "no gene leaves its band");
      Genome solo = Genome.child(m, null, mid, null, r);
      check(solo.father == null && solo.generation == 4, "egg with no father: mother's genes only, generation +1");
      Genome f1 = Genome.founder(new Random(1)), f2 = Genome.founder(new Random(2));
      double dif = 0; for (int i = 0; i < Genome.N; i++) dif += Math.abs(f1.g[i] - f2.g[i]);
      check(dif > 0.1 && f1.generation == 0, "founders differ from each other and are generation 0");

      // ---- 2 and 3. the real brain
      double[] none = rates(null);
      double[] base = rates(Genome.baseline());
      System.out.printf("no genome:       rest %.3f Hz, threat %.3f Hz, legs %.4f, aggression %.4f%n", none[0], none[1], none[2], none[3]);
      System.out.printf("baseline genome: rest %.3f Hz, threat %.3f Hz, legs %.4f, aggression %.4f%n", base[0], base[1], base[2], base[3]);
      check(near(none[0], 0.2326, 0.15) && near(none[1], 4.317, 0.03) && near(none[2], 0.3315, 0.12), "brain with no genome matches the original (legs vary 0.30 to 0.33 run to run in the original itself)");
      check(near(base[0], none[0], 0.15) && near(base[1], none[1], 0.03) && near(base[2], none[2], 0.12), "baseline genome = no genome");
      Genome v = Genome.baseline(); v.g[Genome.index("vision")] = 1.6F; v.g[Genome.index("touch")] = 1.6F;
      double[] rv = rates(v);
      System.out.printf("vision+touch x1.6: threat %.3f Hz, legs %.4f%n", rv[1], rv[2]);
      check(rv[1] > base[1] * 1.2 && rv[2] > base[2] * 1.2, "stronger vision and touch genes give a stronger threat response");
      Genome q = Genome.baseline(); q.g[Genome.index("vision")] = 0.6F; q.g[Genome.index("touch")] = 0.6F;
      double[] rq = rates(q);
      System.out.printf("vision+touch x0.6: threat %.3f Hz, legs %.4f%n", rq[1], rq[2]);
      check(rq[1] < base[1] * 0.85 && rq[2] < base[2] * 0.85, "weaker vision and touch genes give a weaker threat response");
      Genome ag = Genome.baseline(); ag.g[Genome.index("aggr_out")] = 2.0F;
      double[] ra = rates(ag);
      Genome al = Genome.baseline(); al.g[Genome.index("aggr_out")] = 0.5F;
      double[] rl = rates(al);
      System.out.printf("aggr_out x2: aggression %.4f; x0.5: %.4f (mod's trigger level 0.035)%n", ra[3], rl[3]);
      check(ra[3] > base[3] * 2 && rl[3] < base[3] * 0.7, "aggression gene scales the aggression output");
      check(near(ra[0], base[0], 0.15) && near(ra[1], base[1], 0.03), "aggression gene leaves the rest of the brain alone");
      System.out.println(fails == 0 ? "ALL CHECKS PASSED" : fails + " CHECK(S) FAILED");
      System.exit(fails == 0 ? 0 : 1);
   }

   static boolean near(double x, double y, double tol) { return Math.abs(x - y) <= tol * Math.abs(y); }

   /** rest Hz, threat Hz, front-leg motor rate under threat, dn_aggression rate under aggression input */
   static double[] rates(Genome g) throws Exception {
      WholeBrain b = new WholeBrain("check", g);
      b.start();
      while (b.loco() == null) { b.keepAlive(); Thread.sleep(50); }
      WholeBrain.Data d = b.dataOrNull();
      double[] out = new double[4];
      double[] p = phase(b, d, new HashMap<>(), 5); out[0] = p[0];
      Map<String, Float> t = new HashMap<>(); t.put("visual", 1f); t.put("tactile", 1f); t.put("aversive", 1f);
      p = phase(b, d, t, 5); out[1] = p[0]; out[2] = p[1];
      Map<String, Float> ag = new HashMap<>(); ag.put("aggression_pc1", 1f);
      p = phase(b, d, ag, 5); out[3] = p[2];
      b.stop();
      Thread.sleep(200);
      return out;
   }

   static double[] phase(WholeBrain b, WholeBrain.Data d, Map<String, Float> bus, int secs) throws Exception {
      b.setBus(bus);
      Thread.sleep(700);
      int k = 0; double act = 0, leg = 0, agr = 0;
      int li = d.out("mn_leg_front"), ai = d.out("dn_aggression");
      long end = System.currentTimeMillis() + secs * 1000L;
      WholeBrain.Loco last = b.loco();
      while (System.currentTimeMillis() < end) {
         b.keepAlive(); Thread.sleep(30);
         WholeBrain.Loco l = b.loco();
         if (l == last) continue;
         last = l; k++; act += l.activity; leg += l.outRate[li]; agr += l.outRate[ai];
      }
      return new double[]{act / k * 1000.0 / d.n, leg / k, agr / k};
   }
}

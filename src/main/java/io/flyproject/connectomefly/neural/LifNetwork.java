package io.flyproject.connectomefly.neural;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class LifNetwork {
   public final int n;
   public final String[] ids;
   public final String[] cellType;
   public final String[] role;
   public final String[] side;
   public final double tauM;
   public final double vRest;
   public final double vTh;
   public final double vReset;
   public final double tRefrac;
   public final double tauSyn;
   public final double synGain;
   public final double dt;
   private final double synDecay;
   private final int[][] outPost;
   private final double[][] outWeight;
   public final double[] v;
   public final double[] iSyn;
   private final double[] refracTimer;
   private final double[] propAcc;
   private final boolean[] spiked;

   public LifNetwork(Circuit circuit, double synGain, LifNetwork.EdgeBoost extraBoost) {
      this(circuit, 20.0, 0.0, 1.0, 0.0, 2.0, 5.0, synGain, 0.5, extraBoost);
   }

   public LifNetwork(
      Circuit circuit,
      double tauM,
      double vRest,
      double vTh,
      double vReset,
      double tRefrac,
      double tauSyn,
      double synGain,
      double dt,
      LifNetwork.EdgeBoost extraBoost
   ) {
      List<Circuit.Node> nodes = circuit.nodes;
      this.n = nodes.size();
      this.ids = new String[this.n];
      this.cellType = new String[this.n];
      this.role = new String[this.n];
      this.side = new String[this.n];
      Map<String, Integer> index = new LinkedHashMap<>();

      for (int i = 0; i < this.n; i++) {
         Circuit.Node node = nodes.get(i);
         this.ids[i] = node.id;
         this.cellType[i] = node.cell_type;
         this.role[i] = node.role;
         this.side[i] = node.side == null ? "" : node.side;
         index.put(node.id, i);
      }

      this.tauM = tauM;
      this.vRest = vRest;
      this.vTh = vTh;
      this.vReset = vReset;
      this.tRefrac = tRefrac;
      this.tauSyn = tauSyn;
      this.synGain = synGain;
      this.dt = dt;
      this.synDecay = Math.exp(-dt / tauSyn);
      List<Map<Integer, Double>> merged = new ArrayList<>(this.n);

      for (int i = 0; i < this.n; i++) {
         merged.add(new LinkedHashMap<>());
      }

      for (Circuit.Edge e : circuit.edges) {
         Integer i = index.get(e.pre);
         Integer j = index.get(e.post);
         if (i == null || j == null) {
            throw new IllegalStateException("edge references unknown node: " + e.pre + " -> " + e.post);
         }

         double boost = gapJunctionBoost(this.cellType[i], this.cellType[j]);
         if (extraBoost != null) {
            boost *= extraBoost.boost(nodes.get(i), nodes.get(j));
         }

         merged.get(i).merge(j, e.weight * boost, Double::sum);
      }

      this.outPost = new int[this.n][];
      this.outWeight = new double[this.n][];

      for (int ix = 0; ix < this.n; ix++) {
         Map<Integer, Double> row = merged.get(ix);
         int[] posts = new int[row.size()];
         double[] weights = new double[row.size()];
         int k = 0;

         for (Entry<Integer, Double> entry : row.entrySet()) {
            posts[k] = entry.getKey();
            weights[k] = entry.getValue();
            k++;
         }

         this.outPost[ix] = posts;
         this.outWeight[ix] = weights;
      }

      this.v = new double[this.n];
      this.iSyn = new double[this.n];
      this.refracTimer = new double[this.n];
      this.propAcc = new double[this.n];
      this.spiked = new boolean[this.n];

      for (int ix = 0; ix < this.n; ix++) {
         this.v[ix] = vRest;
      }
   }

   private static double gapJunctionBoost(String preType, String postType) {
      return "DNp01".equals(preType) && "TTMn".equals(postType) ? 50.0 : 1.0;
   }

   public boolean[] step(double[] iExt) {
      for (int i = 0; i < this.n; i++) {
         this.iSyn[i] = this.iSyn[i] * this.synDecay;
         this.refracTimer[i] = this.refracTimer[i] - this.dt;
      }

      boolean any = false;

      for (int i = 0; i < this.n; i++) {
         boolean active = this.refracTimer[i] <= 0.0;
         if (active) {
            double dv = this.dt / this.tauM * (-(this.v[i] - this.vRest) + this.iSyn[i] + iExt[i]);
            this.v[i] = this.v[i] + dv;
            if (this.v[i] >= this.vTh) {
               this.spiked[i] = true;
               any = true;
               this.v[i] = this.vReset;
               this.refracTimer[i] = this.tRefrac;
            } else {
               this.spiked[i] = false;
            }
         } else {
            this.spiked[i] = false;
         }
      }

      if (any) {
         Arrays.fill(this.propAcc, 0.0);

         for (int ix = 0; ix < this.n; ix++) {
            if (this.spiked[ix]) {
               int[] posts = this.outPost[ix];
               double[] weights = this.outWeight[ix];

               for (int k = 0; k < posts.length; k++) {
                  this.propAcc[posts[k]] = this.propAcc[posts[k]] + weights[k];
               }
            }
         }

         for (int j = 0; j < this.n; j++) {
            if (this.propAcc[j] != 0.0) {
               this.iSyn[j] = this.iSyn[j] + this.propAcc[j] * this.synGain;
            }
         }
      }

      return this.spiked;
   }

   public boolean[] roleMask(String wanted) {
      boolean[] mask = new boolean[this.n];

      for (int i = 0; i < this.n; i++) {
         mask[i] = wanted.equals(this.role[i]);
      }

      return mask;
   }

   public boolean[] cellTypeMask(String wanted) {
      boolean[] mask = new boolean[this.n];

      for (int i = 0; i < this.n; i++) {
         mask[i] = wanted.equals(this.cellType[i]);
      }

      return mask;
   }

   public boolean[] sideMask(String wanted) {
      boolean[] mask = new boolean[this.n];

      for (int i = 0; i < this.n; i++) {
         mask[i] = wanted.equals(this.side[i]);
      }

      return mask;
   }

   public double synDecay() {
      return this.synDecay;
   }

   public interface EdgeBoost {
      double boost(Circuit.Node var1, Circuit.Node var2);
   }
}

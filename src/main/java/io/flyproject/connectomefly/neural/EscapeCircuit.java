package io.flyproject.connectomefly.neural;

public final class EscapeCircuit {
   public static final double SENSORY_GAIN = 8.0;
   public static final double SIZE_GAIN = 3.1999999999999997;
   public static final double MOTION_EPS = 0.02;
   public static final int STEPS_PER_TICK = 100;
   private final LifNetwork net;
   private final double[] sensoryJitter;
   private final double[] iExt;
   private final boolean[] isTtmn;
   private final boolean[] isDn;
   private final boolean[] isSensory;
   private final boolean[] rateMask;
   private final boolean[] sizeMask;
   private final EscapeCircuit.TickResult result = new EscapeCircuit.TickResult();

   public EscapeCircuit(Circuit circuit) {
      this.net = new LifNetwork(circuit, 0.03, null);
      this.sensoryJitter = new double[this.net.n];
      this.iExt = new double[this.net.n];
      this.isTtmn = new boolean[this.net.n];
      this.isDn = new boolean[this.net.n];
      this.isSensory = new boolean[this.net.n];
      this.rateMask = new boolean[this.net.n];
      this.sizeMask = new boolean[this.net.n];
      boolean[] sensoryMask = this.net.roleMask("sensory");

      for (int i = 0; i < this.net.n; i++) {
         this.isSensory[i] = sensoryMask[i];
         this.sensoryJitter[i] = sensoryMask[i] ? NeuronJitter.of(this.net.ids[i]) : 0.0;
         this.isTtmn[i] = "TTMn".equals(this.net.cellType[i]);
         this.isDn[i] = this.net.cellType[i] != null && this.net.cellType[i].startsWith("DNp");
         String ct = this.net.cellType[i];
         this.rateMask[i] = sensoryMask[i] && ("LC4".equals(ct) || "LPLC1".equals(ct));
         this.sizeMask[i] = sensoryMask[i] && "LPLC2".equals(ct);
      }
   }

   public EscapeCircuit.TickResult tick(double rateSig, double sizeSig) {
      return this.tickGains(rateSig, sizeSig, 8.0, 3.1999999999999997);
   }

   public EscapeCircuit.TickResult tickGains(double rateSig, double sizeSig, double rateGain, double sizeGain) {
      double size = rateSig > 0.02 ? sizeSig : 0.0;

      for (int i = 0; i < this.net.n; i++) {
         this.iExt[i] = (this.rateMask[i] ? rateSig * rateGain : 0.0) + (this.sizeMask[i] ? size * sizeGain : 0.0);
         this.iExt[i] = this.iExt[i] * this.sensoryJitter[i];
      }

      return this.run();
   }

   public EscapeCircuit.TickResult tick(double loom) {
      for (int i = 0; i < this.net.n; i++) {
         this.iExt[i] = loom * 8.0 * this.sensoryJitter[i];
      }

      return this.run();
   }

   private EscapeCircuit.TickResult run() {
      this.result.ttmnFired = false;
      this.result.sensorySpikes = 0;
      this.result.dnSpikes = 0;
      this.result.totalSpikes = 0;

      for (int s = 0; s < 100; s++) {
         boolean[] spiked = this.net.step(this.iExt);

         for (int i = 0; i < this.net.n; i++) {
            if (spiked[i]) {
               this.result.totalSpikes++;
               if (this.isTtmn[i]) {
                  this.result.ttmnFired = true;
               } else if (this.isDn[i]) {
                  this.result.dnSpikes++;
               } else if (this.isSensory[i]) {
                  this.result.sensorySpikes++;
               }
            }
         }
      }

      return this.result;
   }

   public static final class TickResult {
      public boolean ttmnFired;
      public int sensorySpikes;
      public int dnSpikes;
      public int totalSpikes;
   }
}

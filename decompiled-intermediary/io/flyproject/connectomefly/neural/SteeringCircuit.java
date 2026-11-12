package io.flyproject.connectomefly.neural;

public final class SteeringCircuit {
   public static final double STEERING_GAIN = 2.0;
   public static final double RIGHT_SIDE_BOOST = 2.0;
   public static final double SYN_GAIN = 0.1;
   public static final int STEPS_PER_TICK = 100;
   private final LifNetwork net;
   private final double[] jitter;
   private final boolean[] leftDn;
   private final boolean[] rightDn;
   private final boolean[] leftMotor;
   private final boolean[] rightMotor;
   private final double[] iExt;
   public int lastLeftMotor;
   public int lastRightMotor;

   public SteeringCircuit(Circuit circuit) {
      this.net = new LifNetwork(
         circuit, 0.1, (pre, post) -> !"right".equals(pre.side) || !"descending".equals(pre.role) && !"vnc_intrinsic".equals(pre.role) ? 1.0 : 2.0
      );
      boolean[] dn = this.net.roleMask("descending");
      boolean[] left = this.net.sideMask("left");
      boolean[] right = this.net.sideMask("right");
      boolean[] motor = this.net.roleMask("motor");
      this.leftDn = new boolean[this.net.n];
      this.rightDn = new boolean[this.net.n];
      this.leftMotor = new boolean[this.net.n];
      this.rightMotor = new boolean[this.net.n];
      this.jitter = new double[this.net.n];
      this.iExt = new double[this.net.n];

      for (int i = 0; i < this.net.n; i++) {
         this.leftDn[i] = dn[i] && left[i];
         this.rightDn[i] = dn[i] && right[i];
         this.leftMotor[i] = motor[i] && left[i];
         this.rightMotor[i] = motor[i] && right[i];
         this.jitter[i] = !this.leftDn[i] && !this.rightDn[i] ? 0.0 : NeuronJitter.of(this.net.ids[i]);
      }
   }

   public void drive(double side) {
      double leftDrive = Math.max(0.0, side) * 2.0;
      double rightDrive = Math.max(0.0, -side) * 2.0;

      for (int i = 0; i < this.net.n; i++) {
         double d = this.leftDn[i] ? leftDrive : (this.rightDn[i] ? rightDrive : 0.0);
         this.iExt[i] = d * this.jitter[i];
      }

      int lm = 0;
      int rm = 0;

      for (int s = 0; s < 100; s++) {
         boolean[] spiked = this.net.step(this.iExt);

         for (int i = 0; i < this.net.n; i++) {
            if (spiked[i]) {
               if (this.leftMotor[i]) {
                  lm++;
               } else if (this.rightMotor[i]) {
                  rm++;
               }
            }
         }
      }

      this.lastLeftMotor = lm;
      this.lastRightMotor = rm;
   }
}

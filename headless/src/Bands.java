/** [model] Proposed per-gene bands (multipliers of the author's value), from docs/step1. Order = ParamBrain.NAMES. */
public final class Bands {
   public static final float[][] CONST = {
      {0.85f, 1.25f}, // TAU_M
      {0.90f, 1.15f}, // V_TH
      {0.93f, 1.05f}, // TAU_SYN
      {0.70f, 1.40f}, // DRIVE_RATE
      {0.85f, 1.30f}, // DRIVE_AMP
      {0.70f, 1.40f}, // DN_DRIVE_RATE
      {0.90f, 1.05f}, // SYN_MULT
      {0.70f, 1.40f}, // SENS_MULT
      {0.70f, 1.40f}, // INH_MULT
      {0.95f, 1.40f}, // ADAPT_INC
      {0.95f, 1.40f}, // ADAPT_TAU
      {0.70f, 1.40f}, // BUS_RATE
      {0.50f, 2.00f}, // ETA
      {0.50f, 2.00f}, // FORGET
   };
}

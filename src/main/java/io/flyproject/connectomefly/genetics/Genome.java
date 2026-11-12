package io.flyproject.connectomefly.genetics;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * [model] A fly's inherited tuning. NOT real genetics: in a real fly genes shape how the brain grows.
 * Here the wiring (BANC connectome, via Connectome Fly by ro0oney, flyproject.io) is identical in every fly,
 * and a "gene" is a multiplier on one of the author's calibrated constants, on one circuit's gain,
 * or on how strongly one sense drives its receptor neurons. Every value is a multiplier; 1.0 = the author's value.
 * Bands come from headless sweeps (docs/step1): outside them brains go silent or run away.
 */
public final class Genome {
   public enum Kind { CONSTANT, CIRCUIT, SENSE }

   public record Gene(String name, Kind kind, float lo, float hi, String what) {}

   public static final Gene[] GENES = {
      // per-fly versions of WholeBrain's constants
      new Gene("tau_m", Kind.CONSTANT, 0.85F, 1.25F, "membrane time constant"),
      new Gene("v_th", Kind.CONSTANT, 0.90F, 1.15F, "spike threshold"),
      new Gene("tau_syn", Kind.CONSTANT, 0.93F, 1.05F, "synaptic current decay"),
      new Gene("drive_rate", Kind.CONSTANT, 0.70F, 1.40F, "background drive to sensory neurons"),
      new Gene("drive_amp", Kind.CONSTANT, 0.85F, 1.30F, "size of each input kick"),
      new Gene("dn_drive_rate", Kind.CONSTANT, 0.70F, 1.40F, "background drive to descending neurons"),
      new Gene("syn_mult", Kind.CONSTANT, 0.90F, 1.05F, "overall synapse strength"),
      new Gene("sens_mult", Kind.CONSTANT, 0.70F, 1.40F, "extra strength of sensory neurons' synapses"),
      new Gene("inh_mult", Kind.CONSTANT, 0.70F, 1.40F, "extra strength of inhibition"),
      new Gene("adapt_inc", Kind.CONSTANT, 0.95F, 1.40F, "spike-frequency adaptation step"),
      new Gene("adapt_tau", Kind.CONSTANT, 0.95F, 1.40F, "adaptation decay time"),
      new Gene("bus_rate", Kind.CONSTANT, 0.70F, 1.40F, "overall sensory input rate"),
      new Gene("eta", Kind.CONSTANT, 0.50F, 2.00F, "learning rate"),
      new Gene("forget", Kind.CONSTANT, 0.50F, 2.00F, "forgetting rate"),
      // circuit gains (groups from brain_circuit_gains.json), on top of the author's gain
      new Gene("aggr_out", Kind.CIRCUIT, 0.50F, 2.00F, "aggression output"),
      new Gene("kc_mbon", Kind.CIRCUIT, 0.50F, 2.00F, "odour to mushroom-body output"),
      new Gene("compass_out", Kind.CIRCUIT, 0.50F, 2.00F, "compass to steering"),
      new Gene("compass_loop", Kind.CIRCUIT, 0.50F, 1.25F, "compass ring recurrence"),
      // how strongly each sense drives its receptor neurons
      new Gene("smell", Kind.SENSE, 0.60F, 1.60F, "olfactory channels"),
      new Gene("taste", Kind.SENSE, 0.60F, 1.60F, "gustatory channels"),
      new Gene("touch", Kind.SENSE, 0.60F, 1.60F, "tactile and wind channels"),
      new Gene("hearing", Kind.SENSE, 0.60F, 1.60F, "auditory and song channels"),
      new Gene("vision", Kind.SENSE, 0.60F, 1.60F, "visual, motion, object and landmark channels"),
      new Gene("pheromone", Kind.SENSE, 0.60F, 1.60F, "pheromone channel"),
      new Gene("pain", Kind.SENSE, 0.60F, 1.60F, "aversive and punishment channels"),
      new Gene("reward", Kind.SENSE, 0.60F, 1.60F, "reward channel"),
      new Gene("hunger_drive", Kind.SENSE, 0.60F, 1.60F, "nutrient-state channel"),
      new Gene("sleep_need", Kind.SENSE, 0.60F, 1.60F, "sleep-pressure channel"),
      new Gene("climate", Kind.SENSE, 0.60F, 1.60F, "heat, cold, humid and dry channels"),
   };
   public static final int N = GENES.length;

   /** [model] chance that a gene mutates in a child, and mutation size as a fraction of the gene's (log) band. */
   public static volatile float mutationRate = Float.parseFloat(System.getProperty("connectomefly.genetics.mutationRate", "0.2"));
   public static volatile float mutationSize = Float.parseFloat(System.getProperty("connectomefly.genetics.mutationSize", "0.12"));
   /** [model] founders (spawned, not hatched) start near the author's values with this spread. */
   public static volatile float founderSpread = Float.parseFloat(System.getProperty("connectomefly.genetics.founderSpread", "0.15"));
   /** [model] not biology: a child starts with this fraction of what its mother had learned. Off by default. */
   public static volatile boolean inheritMemory = Boolean.getBoolean("connectomefly.genetics.inheritMemory");
   public static volatile float inheritMemoryFraction = Float.parseFloat(System.getProperty("connectomefly.genetics.inheritMemoryFraction", "0.25"));

   public final float[] g = new float[N];
   public int generation;
   public UUID mother;
   public UUID father;
   /** how many genes mutated when this genome was made */
   public int mutations;

   private Genome() {
   }

   public static int index(String name) {
      for (int i = 0; i < N; i++) {
         if (GENES[i].name.equals(name)) {
            return i;
         }
      }

      return -1;
   }

   public float get(String name) {
      int i = index(name);
      return i < 0 ? 1.0F : this.g[i];
   }

   /** The author's values exactly: every multiplier 1. */
   public static Genome baseline() {
      Genome x = new Genome();
      java.util.Arrays.fill(x.g, 1.0F);
      return x;
   }

   public static Genome founder(Random rng) {
      Genome x = new Genome();

      for (int i = 0; i < N; i++) {
         x.g[i] = clamp(i, (float)Math.exp(rng.nextGaussian() * founderSpread * logBand(i)));
      }

      return x;
   }

   /** Each gene from mother or father at random, then a few small mutations. father may be null (forced egg). */
   public static Genome child(Genome mother, Genome father, UUID motherId, UUID fatherId, Random rng) {
      Genome x = new Genome();

      for (int i = 0; i < N; i++) {
         float v = father != null && rng.nextBoolean() ? father.g[i] : mother.g[i];
         if (rng.nextFloat() < mutationRate) {
            v *= (float)Math.exp(rng.nextGaussian() * mutationSize * logBand(i));
            x.mutations++;
         }

         x.g[i] = clamp(i, v);
      }

      x.generation = Math.max(mother.generation, father == null ? 0 : father.generation) + 1;
      x.mother = motherId;
      x.father = fatherId;
      return x;
   }

   private static float logBand(int i) {
      return (float)Math.log(GENES[i].hi / GENES[i].lo);
   }

   private static float clamp(int i, float v) {
      return Float.isNaN(v) ? 1.0F : Math.max(GENES[i].lo, Math.min(GENES[i].hi, v));
   }

   static float clampGene(int i, float v) {
      return clamp(i, v);
   }

   /** The genes furthest from the author's value, relative to their band, e.g. "smell x1.31, eta x0.72". */
   public String top(int count) {
      Integer[] order = new Integer[N];

      for (int i = 0; i < N; i++) {
         order[i] = i;
      }

      java.util.Arrays.sort(order, (a, b) -> Float.compare(this.dev(b), this.dev(a)));
      StringBuilder sb = new StringBuilder();

      for (int k = 0; k < count && k < N; k++) {
         int i = order[k];
         if (k > 0) {
            sb.append(", ");
         }

         sb.append(GENES[i].name).append(String.format(Locale.ROOT, " x%.2f", this.g[i]));
      }

      return sb.toString();
   }

   private float dev(int i) {
      return (float)Math.abs(Math.log(this.g[i])) / logBand(i);
   }

   public String all() {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < N; i++) {
         sb.append(i > 0 ? " " : "").append(GENES[i].name).append(String.format(Locale.ROOT, "=%.3f", this.g[i]));
      }

      return sb.toString();
   }

   public static String shortId(UUID id) {
      return id == null ? "none" : id.toString().substring(0, 8);
   }

   /** Which sense gene scales a sensory channel of brain_channels.json; -1 = none. */
   public static int senseGeneFor(String channel) {
      String s;
      if (channel.startsWith("olf_")) {
         s = "smell";
      } else if (channel.startsWith("taste_") || channel.equals("gust_sugar")) {
         s = "taste";
      } else if (channel.equals("tactile") || channel.equals("wind")) {
         s = "touch";
      } else if (channel.equals("auditory") || channel.startsWith("song")) {
         s = "hearing";
      } else if (channel.equals("visual") || channel.startsWith("motion_") || channel.startsWith("small_object_") || channel.startsWith("landmark_")) {
         s = "vision";
      } else if (channel.equals("pheromone")) {
         s = "pheromone";
      } else if (channel.equals("aversive") || channel.equals("punishment")) {
         s = "pain";
      } else if (channel.equals("reward")) {
         s = "reward";
      } else if (channel.equals("nutrient")) {
         s = "hunger_drive";
      } else if (channel.equals("sleep_pressure")) {
         s = "sleep_need";
      } else if (channel.equals("heating") || channel.equals("cooling") || channel.equals("humid") || channel.equals("dry")) {
         s = "climate";
      } else {
         return -1;
      }

      return index(s);
   }
}

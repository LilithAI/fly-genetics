package io.flyproject.connectomefly.neural;

/**
 * Named groups of real neurons for the readout's brain picture. Each region is a set of the mod's sensory channels
 * (brain_channels.json) and output populations (brain_outputs.json), so the ACTIVITY shown is that of real, identified
 * neurons. The POSITIONS in the picture are a schematic of a fly's nervous system; the data has no neuron coordinates.
 */
public final class BrainRegions {
   public record Region(String name, int kind, int x, int y, int r, String[] channels, String[] outputs) {}

   /** kind: 0 = senses coming in, 1 = internal state, 2 = commands and muscles going out */
   public static final Region[] REGIONS = {
      new Region("left eye", 0, 38, 32, 19, new String[]{"visual", "motion_prog_left", "motion_reg_left", "small_object_left", "landmark_left"}, new String[]{"lc_small_left", "lc_bar_left", "hs_left", "vs_left"}),
      new Region("right eye", 0, 198, 32, 19, new String[]{"visual", "motion_prog_right", "motion_reg_right", "small_object_right", "landmark_right"}, new String[]{"lc_small_right", "lc_bar_right", "hs_right", "vs_right"}),
      new Region("smell", 0, 102, 47, 8, new String[]{"olf_food", "olf_fruit", "olf_co2", "pheromone"}, new String[0]),
      new Region("hearing and wind", 0, 134, 47, 8, new String[]{"auditory", "song", "wind"}, new String[0]),
      new Region("heat and damp", 0, 118, 44, 5, new String[]{"heating", "cooling", "humid", "dry"}, new String[0]),
      new Region("memory (mushroom body)", 1, 96, 22, 10, new String[]{"reward", "punishment"}, new String[]{"mbon_avoid", "mbon_approach", "pam", "ppl1"}),
      new Region("compass", 1, 118, 26, 8, new String[]{"pen_left", "pen_right"}, new String[]{"epg", "pfl3_left", "pfl3_right"}),
      new Region("clock and sleep", 1, 141, 20, 8, new String[]{"light_clock", "clock_morning", "clock_evening", "sleep_pressure"}, new String[]{"clock", "clock_morning", "clock_evening", "dfb_sleep"}),
      new Region("courtship and aggression", 1, 140, 35, 6, new String[]{"song_pc1", "aggression_pc1"}, new String[]{"pc1_receptive", "pc1_aggr", "vpoen"}),
      new Region("taste and feeding", 0, 118, 59, 10, new String[]{"gust_sugar", "taste_sugar_labellum", "taste_bitter_labellum", "taste_water", "nutrient"}, new String[]{"mn_proboscis", "mn_pharynx", "ipc"}),
      new Region("commands to the body", 2, 118, 70, 4, new String[0], new String[]{"dn_turn_left", "dn_turn_right", "dn_giant_fiber", "dn_backward", "dn_courtship", "dn_aggression", "dn_egg_laying", "dn_p13", "mn_neck"}),
      new Region("wings", 2, 118, 81, 8, new String[0], new String[]{"mn_wing_power", "mn_wing_steer", "mn_haltere"}),
      new Region("left legs", 2, 102, 91, 9, new String[0], new String[]{"mn_leg_front_left", "mn_leg_middle_left", "mn_leg_hind_left"}),
      new Region("right legs", 2, 134, 91, 9, new String[0], new String[]{"mn_leg_front_right", "mn_leg_middle_right", "mn_leg_hind_right"}),
      new Region("escape jump", 2, 118, 90, 4, new String[0], new String[]{"mn_jump"}),
      new Region("touch and pain", 0, 118, 100, 8, new String[]{"tactile", "aversive", "taste_sugar_leg", "taste_bitter_leg"}, new String[0]),
      new Region("egg laying", 2, 118, 106, 3, new String[0], new String[]{"mn_ovulation"}),
   };

   private BrainRegions() {
   }

   /** One character per region: 'a' = silent up to 'p' = about 13 Hz or more per neuron (log scale), busiest member of the region. */
   public static String encode(WholeBrain.Data d, WholeBrain.Loco l) {
      char[] c = new char[REGIONS.length];

      for (int i = 0; i < c.length; i++) {
         Region r = REGIONS[i];
         float hz = 0.0F;

         for (String ch : r.channels) {
            int k = d.channel(ch);
            if (k >= 0 && k < l.chanSpikes.length && d.chanIdx[k].length > 0) {
               hz = Math.max(hz, l.chanSpikes[k] * 1000.0F / d.chanIdx[k].length);
            }
         }

         for (String o : r.outputs) {
            int k = d.out(o);
            if (k >= 0 && k < l.outRate.length && d.outIdx[k].length > 0) {
               hz = Math.max(hz, l.outRate[k] * 1000.0F / d.outIdx[k].length);
            }
         }

         int level = (int)Math.round(4.0 * Math.log(1.0 + hz) / Math.log(2.0));
         c[i] = (char)(97 + Math.max(0, Math.min(15, level)));
      }

      return new String(c);
   }
}

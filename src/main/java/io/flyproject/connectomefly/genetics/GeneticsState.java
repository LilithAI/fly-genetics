package io.flyproject.connectomefly.genetics;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * [model] Selection settings and population statistics, saved with the world
 * (data/connectomefly_genetics.dat in the overworld). All of these rules are invented.
 */
public final class GeneticsState extends SavedData {
   public static final String[] CAUSES = {"old age", "starvation", "predator", "other", "removed"};
   public static final com.mojang.serialization.Codec<GeneticsState> CODEC = CompoundTag.CODEC.xmap(GeneticsState::read, st -> st.save(new CompoundTag()));
   public static final SavedDataType<GeneticsState> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath("connectomefly", "genetics"), GeneticsState::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE
   );
   // settings
   public boolean selection = true;
   /** minutes an adult lives; each fly draws 80 to 120 percent of this */
   public float adultLifeMinutes = 120.0F;
   /** a female accepts a mate only at or above this hunger value (0 to 20, 20 = full) */
   public float breedHunger = 12.0F;
   /** hunger a female spends on laying one egg */
   public float eggCost = 3.0F;
   /** no egg is laid while this many flies and eggs are within capRadius blocks */
   public int cap = 12;
   public int capRadius = 48;
   /** speeds growing up (the mod's own pace is 90 minutes from egg to adult) */
   public float growthSpeed = 1.0F;
   public boolean inheritMemory = false;
   /** [model] not biology: a hatchling takes the rarer sex when one sex nearby is outnumbered more than 2 to 1 */
   public boolean sexBalance = true;
   public int sexBalanced;
   // statistics
   public final TreeMap<Integer, Integer> born = new TreeMap<>();
   public final TreeMap<Integer, float[]> geneSum = new TreeMap<>();
   public final TreeMap<String, Integer> deaths = new TreeMap<>();
   public final TreeMap<String, Double> deathAdultMinutes = new TreeMap<>();
   public int eggsLaid;
   public int eggsRefusedCap;
   public int matingsRefusedHungry;

   public static GeneticsState get(MinecraftServer server) {
      GeneticsState s = server.overworld().getDataStorage().computeIfAbsent(TYPE);
      Genome.inheritMemory = s.inheritMemory;
      return s;
   }

   public void recordBirth(Genome g) {
      this.born.merge(g.generation, 1, Integer::sum);
      float[] sum = this.geneSum.computeIfAbsent(g.generation, k -> new float[Genome.N]);

      for (int i = 0; i < Genome.N; i++) {
         sum[i] += g.g[i];
      }

      this.setDirty();
   }

   public void recordDeath(String cause, double adultMinutes) {
      this.deaths.merge(cause, 1, Integer::sum);
      this.deathAdultMinutes.merge(cause, adultMinutes, Double::sum);
      this.setDirty();
   }

   /** mean of gene i among all flies ever born in a generation, or NaN */
   public float mean(int generation, int i) {
      Integer n = this.born.get(generation);
      float[] sum = this.geneSum.get(generation);
      return n != null && n != 0 && sum != null ? sum[i] / n : Float.NaN;
   }

   public String settings() {
      return String.format(
         Locale.ROOT,
         "selection=%s adult_life_minutes=%.1f breed_hunger=%.1f egg_cost=%.1f cap=%d cap_radius=%d growth_speed=%.1f inherit_memory=%s sex_balance=%s",
         this.selection,
         this.adultLifeMinutes,
         this.breedHunger,
         this.eggCost,
         this.cap,
         this.capRadius,
         this.growthSpeed,
         this.inheritMemory,
         this.sexBalance
      );
   }

   /** returns null if the key is unknown */
   public String set(String key, float v) {
      switch (key) {
         case "selection":
            this.selection = v != 0.0F;
            break;
         case "adult_life_minutes":
            this.adultLifeMinutes = Math.max(0.1F, v);
            break;
         case "breed_hunger":
            this.breedHunger = Math.max(0.0F, Math.min(20.0F, v));
            break;
         case "egg_cost":
            this.eggCost = Math.max(0.0F, Math.min(20.0F, v));
            break;
         case "cap":
            this.cap = Math.max(1, (int)v);
            break;
         case "cap_radius":
            this.capRadius = Math.max(4, (int)v);
            break;
         case "growth_speed":
            this.growthSpeed = Math.max(0.1F, Math.min(200.0F, v));
            break;
         case "sex_balance":
            this.sexBalance = v != 0.0F;
            break;
         case "inherit_memory":
            this.inheritMemory = v != 0.0F;
            Genome.inheritMemory = this.inheritMemory;
            break;
         default:
            return null;
      }

      this.setDirty();
      return this.settings();
   }

   public CompoundTag save(CompoundTag n) {
      n.putBoolean("Selection", this.selection);
      n.putFloat("AdultLifeMinutes", this.adultLifeMinutes);
      n.putFloat("BreedHunger", this.breedHunger);
      n.putFloat("EggCost", this.eggCost);
      n.putInt("Cap", this.cap);
      n.putInt("CapRadius", this.capRadius);
      n.putFloat("GrowthSpeed", this.growthSpeed);
      n.putBoolean("InheritMemory", this.inheritMemory);
      n.putBoolean("SexBalance", this.sexBalance);
      n.putInt("SexBalanced", this.sexBalanced);
      n.putInt("EggsLaid", this.eggsLaid);
      n.putInt("EggsRefusedCap", this.eggsRefusedCap);
      n.putInt("MatingsRefusedHungry", this.matingsRefusedHungry);
      CompoundTag gens = new CompoundTag();

      for (Map.Entry<Integer, Integer> e : this.born.entrySet()) {
         CompoundTag g = new CompoundTag();
         g.putInt("Born", e.getValue());
         CompoundTag sums = new CompoundTag();
         float[] sum = this.geneSum.get(e.getKey());

         for (int i = 0; i < Genome.N; i++) {
            sums.putFloat(Genome.GENES[i].name(), sum[i]);
         }

         g.put("GeneSums", sums);
         gens.put(String.valueOf(e.getKey()), g);
      }

      n.put("Generations", gens);
      CompoundTag d = new CompoundTag();

      for (Map.Entry<String, Integer> e : this.deaths.entrySet()) {
         CompoundTag c = new CompoundTag();
         c.putInt("Count", e.getValue());
         c.putDouble("AdultMinutes", this.deathAdultMinutes.getOrDefault(e.getKey(), 0.0));
         d.put(e.getKey(), c);
      }

      n.put("Deaths", d);
      return n;
   }

   public static GeneticsState read(CompoundTag n) {
      GeneticsState s = new GeneticsState();
      s.selection = !n.contains("Selection") || n.getBooleanOr("Selection", false);
      if (n.contains("AdultLifeMinutes")) {
         s.adultLifeMinutes = n.getFloatOr("AdultLifeMinutes", 0.0F);
         s.breedHunger = n.getFloatOr("BreedHunger", 0.0F);
         s.eggCost = n.getFloatOr("EggCost", 0.0F);
         s.cap = n.getIntOr("Cap", 0);
         s.capRadius = n.getIntOr("CapRadius", 0);
         s.growthSpeed = n.getFloatOr("GrowthSpeed", 0.0F);
      }

      s.inheritMemory = n.getBooleanOr("InheritMemory", false);
      s.sexBalance = !n.contains("SexBalance") || n.getBooleanOr("SexBalance", false);
      s.sexBalanced = n.getIntOr("SexBalanced", 0);
      s.eggsLaid = n.getIntOr("EggsLaid", 0);
      s.eggsRefusedCap = n.getIntOr("EggsRefusedCap", 0);
      s.matingsRefusedHungry = n.getIntOr("MatingsRefusedHungry", 0);
      CompoundTag gens = n.getCompoundOrEmpty("Generations");

      for (String k : gens.keySet()) {
         CompoundTag g = gens.getCompoundOrEmpty(k);
         int gen = Integer.parseInt(k);
         s.born.put(gen, g.getIntOr("Born", 0));
         float[] sum = new float[Genome.N];
         CompoundTag sums = g.getCompoundOrEmpty("GeneSums");

         for (int i = 0; i < Genome.N; i++) {
            // a gene added in a later version counts as the author's value for flies born before it existed
            sum[i] = sums.contains(Genome.GENES[i].name()) ? sums.getFloatOr(Genome.GENES[i].name(), 0.0F) : g.getIntOr("Born", 0);
         }

         s.geneSum.put(gen, sum);
      }

      CompoundTag d = n.getCompoundOrEmpty("Deaths");

      for (String k : d.keySet()) {
         s.deaths.put(k, d.getCompoundOrEmpty(k).getIntOr("Count", 0));
         s.deathAdultMinutes.put(k, d.getCompoundOrEmpty(k).getDoubleOr("AdultMinutes", 0.0));
      }

      return s;
   }
}

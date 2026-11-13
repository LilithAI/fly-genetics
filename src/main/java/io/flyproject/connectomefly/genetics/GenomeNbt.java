package io.flyproject.connectomefly.genetics;

import net.minecraft.nbt.CompoundTag;

/** Save data for a Genome. Kept apart so Genome itself runs outside Minecraft (headless checks). */
public final class GenomeNbt {
   private GenomeNbt() {
   }

   public static CompoundTag write(Genome g) {
      CompoundTag n = new CompoundTag();
      CompoundTag genes = new CompoundTag();

      for (int i = 0; i < Genome.N; i++) {
         genes.putFloat(Genome.GENES[i].name(), g.g[i]);
      }

      n.put("Genes", genes);
      n.putInt("Generation", g.generation);
      n.putInt("Mutations", g.mutations);
      if (g.mother != null) {
         n.putString("Mother", g.mother.toString());
      }

      if (g.father != null) {
         n.putString("Father", g.father.toString());
      }

      return n;
   }

   /** Genes are stored by name, so genes added or removed in a later version do not corrupt old flies. */
   public static Genome read(CompoundTag n) {
      Genome x = Genome.baseline();
      CompoundTag genes = n.getCompoundOrEmpty("Genes");

      for (int i = 0; i < Genome.N; i++) {
         if (genes.contains(Genome.GENES[i].name())) {
            x.g[i] = Genome.clampGene(i, genes.getFloatOr(Genome.GENES[i].name(), 1.0F));
         }
      }

      x.generation = n.getIntOr("Generation", 0);
      x.mutations = n.getIntOr("Mutations", 0);
      x.mother = n.getString("Mother").map(java.util.UUID::fromString).orElse(null);

      x.father = n.getString("Father").map(java.util.UUID::fromString).orElse(null);

      return x;
   }
}

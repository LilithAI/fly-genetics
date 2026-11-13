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
         n.putUUID("Mother", g.mother);
      }

      if (g.father != null) {
         n.putUUID("Father", g.father);
      }

      return n;
   }

   /** Genes are stored by name, so genes added or removed in a later version do not corrupt old flies. */
   public static Genome read(CompoundTag n) {
      Genome x = Genome.baseline();
      CompoundTag genes = n.getCompound("Genes");

      for (int i = 0; i < Genome.N; i++) {
         if (genes.contains(Genome.GENES[i].name())) {
            x.g[i] = Genome.clampGene(i, genes.getFloat(Genome.GENES[i].name()));
         }
      }

      x.generation = n.getInt("Generation");
      x.mutations = n.getInt("Mutations");
      if (n.hasUUID("Mother")) {
         x.mother = n.getUUID("Mother");
      }

      if (n.hasUUID("Father")) {
         x.father = n.getUUID("Father");
      }

      return x;
   }
}

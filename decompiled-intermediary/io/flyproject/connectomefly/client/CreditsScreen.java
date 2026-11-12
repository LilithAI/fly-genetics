package io.flyproject.connectomefly.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5481;

public class CreditsScreen extends class_437 {
   private static final String[][] CONTENT = new String[][]{
      {"title", "Credits"},
      {"h", "REAL"},
      {
            "t",
            "- Her brain is the BANC connectome (release v888): one real female fruit fly, 158,262 neurons and about 3.1 million synapses. Every neuron ID in this mod can be looked up in the public dataset."
      },
      {"t", "- Who connects to whom, how many synapses, and whether a connection excites or inhibits all come from that data."},
      {
            "t",
            "- What she meets in the world drives her real receptor neurons. What her motor and descending neurons do moves her body: jump, walk, turn, take off, groom, extend the proboscis, lay an egg."
      },
      {"t", "- Her compass cells, memory synapses, sleep and clock cells, insulin cells, taste and smell neurons are her own, read live."},
      {"t", "- The male's courtship circuit comes from the male fruit fly connectome by HHMI Janelia and the University of Cambridge."},
      {"h", "CALIBRATED BY US"},
      {
            "t",
            "- A connectome gives the wiring, not the speeds. Neuron time constants, thresholds and gains are ours, set so signals travel without running away."
      },
      {
            "t",
            "- A few deep circuits needed a boost to be heard: the compass loop x2, the memory synapses x20, the aggression output x25. The Giant Fiber to jump muscle link is x50 because it is partly electrical in the real fly."
      },
      {"t", "- How often she walks, flies and sleeps is calibrated. The moment she does it comes from her neurons."},
      {"h", "LABELED MODELS"},
      {
            "t",
            "- Hunger, thirst, the wind and the smells it carries, counting steps on her compass, the loser backing off after a fight, and what each Minecraft block smells or tastes like are our models. On screen they carry [model]. Actions that came from her neurons carry [brain]."
      },
      {"t", "- Speech bubbles are written by us and triggered by real spikes. They translate, they do not decode."},
      {"t", "- The lead only moves her body. Her brain is untouched by it."},
      {"t", "- The fly body is our own 3D model. The bee body is Minecraft's bee."},
      {"h", "NEVER CLAIMED"},
      {"t", "- Full consciousness, real biological timing."},
      {"h", "THANKS"},
      {"t", "- To the BANC team, and to HHMI Janelia and the University of Cambridge, for making their connectomes public."},
      {"t", "- Made by the Fly Connectome Sim project (flyproject.io)."},
      {"h", "LICENSE"},
      {"t", "- CC BY 4.0. Share it, re-upload it, build on it. Just credit the project. The BANC wiring data stays under its authors' own terms."}
   };
   private final List<CreditsScreen.OrderedLine> lines = new ArrayList<>();
   private double scroll;
   private int contentHeight;

   public CreditsScreen() {
      super(class_2561.method_43470("connectome-fly credits"));
   }

   protected void method_25426() {
      this.lines.clear();
      int width = Math.min(this.field_22789 - 60, 340);

      for (String[] row : CONTENT) {
         String first = row[0];
         class_2561 styled;
         int color;
         switch (first) {
            case "title":
               styled = class_2561.method_43470(row[1]).method_27692(class_124.field_1067);
               color = 16777215;
               break;
            case "h":
               styled = class_2561.method_43470(row[1]).method_27695(new class_124[]{class_124.field_1065, class_124.field_1067});
               color = 16765056;
               break;
            default:
               styled = class_2561.method_43470(row[1]);
               color = 14737632;
         }

         boolean first = true;

         for (class_5481 ordered : this.field_22793.method_1728(styled, width)) {
            this.lines.add(new CreditsScreen.OrderedLine(ordered, color, first && !row[0].equals("t")));
            first = false;
         }
      }

      this.contentHeight = 0;

      for (CreditsScreen.OrderedLine line : this.lines) {
         this.contentHeight = this.contentHeight + (line.gap() ? 16 : 10);
      }

      this.method_37063(
         class_4185.method_46430(class_2561.method_43470("Done"), b -> this.method_25419())
            .method_46434(this.field_22789 / 2 - 50, this.field_22790 - 28, 100, 20)
            .method_46431()
      );
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      int y = 20 - (int)this.scroll;
      int x = (this.field_22789 - Math.min(this.field_22789 - 60, 340)) / 2;

      for (CreditsScreen.OrderedLine line : this.lines) {
         if (line.gap()) {
            y += 6;
         }

         if (y > 8 && y < this.field_22790 - 34) {
            context.method_35720(this.field_22793, line.text(), x, y, line.color());
         }

         y += 10;
      }

      super.method_25394(context, mouseX, mouseY, delta);
   }

   public boolean method_25401(double mouseX, double mouseY, double amount) {
      int max = Math.max(0, this.contentHeight - (this.field_22790 - 60));
      this.scroll = Math.max(0.0, Math.min((double)max, this.scroll - amount * 12.0));
      return true;
   }

   public boolean method_25421() {
      return false;
   }

   private record OrderedLine(class_5481 text, int color, boolean gap) {
   }
}

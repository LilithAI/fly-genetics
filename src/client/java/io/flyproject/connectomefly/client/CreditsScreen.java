package io.flyproject.connectomefly.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class CreditsScreen extends Screen {
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
      super(Component.literal("connectome-fly credits"));
   }

   protected void init() {
      this.lines.clear();
      int width = Math.min(this.width - 60, 340);

      for (String[] row : CONTENT) {
         String kind = row[0];
         Component styled;
         int color;
         switch (kind) {
            case "title":
               styled = Component.literal(row[1]).withStyle(ChatFormatting.BOLD);
               color = 16777215;
               break;
            case "h":
               styled = Component.literal(row[1]).withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD});
               color = 16765056;
               break;
            default:
               styled = Component.literal(row[1]);
               color = 14737632;
         }

         boolean first = true;

         for (FormattedCharSequence ordered : this.font.split(styled, width)) {
            this.lines.add(new CreditsScreen.OrderedLine(ordered, color, first && !row[0].equals("t")));
            first = false;
         }
      }

      this.contentHeight = 0;

      for (CreditsScreen.OrderedLine line : this.lines) {
         this.contentHeight = this.contentHeight + (line.gap() ? 16 : 10);
      }

      this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose()).bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
   }

   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      int y = 20 - (int)this.scroll;
      int x = (this.width - Math.min(this.width - 60, 340)) / 2;

      for (CreditsScreen.OrderedLine line : this.lines) {
         if (line.gap()) {
            y += 6;
         }

         if (y > 8 && y < this.height - 34) {
            context.drawString(this.font, line.text(), x, y, line.color());
         }

         y += 10;
      }

      super.render(context, mouseX, mouseY, delta);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      int max = Math.max(0, this.contentHeight - (this.height - 60));
      this.scroll = Math.max(0.0, Math.min((double)max, this.scroll - amount * 12.0));
      return true;
   }

   public boolean isPauseScreen() {
      return false;
   }

   private record OrderedLine(FormattedCharSequence text, int color, boolean gap) {
   }
}

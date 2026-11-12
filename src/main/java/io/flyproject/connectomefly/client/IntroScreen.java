package io.flyproject.connectomefly.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class IntroScreen extends Screen {
   private static final String[][] CONTENT = new String[][]{
      {"title", "You just installed a real fly's brain."},
      {
            "t",
            "This mod adds one creature, a fruit fly, and every reaction it has is actually computed live by real neural circuits, copied straight from a real, scanned fly brain (the BANC connectome, every neuron and connection in here exists in an actual animal)."
      },
      {"h", "START HERE"},
      {"t", "1. Grab a Connectome Fly spawn egg from the creative menu, or just run /flylab spawn."},
      {
            "t",
            "2. Build the test boxes with /flylab arena all (or one of them, like /flylab arena escape). Each box has a book that explains it and a button that puts the fly inside."
      },
      {"t", "3. Start testing!"},
      {"h", "WHAT'S ACTUALLY INSIDE THIS FLY"},
      {
            "t",
            "This fly runs her ENTIRE real brain: all 158,262 neurons and 3.1 million synapses of the BANC connectome, live, on a background thread. Her senses (heat, cold, wet, taste, smell, CO2, touch, sound, bitter, pheromone, light, wind) drive the real receptor neurons, and her walking is read off her real descending neurons."
      },
      {
            "t",
            "Some of these, vision and steering specifically, run on hand-built, precisely matched real signals: real distance, real size, real angular expansion, fed directly to the exact real neurons known to respond to that exact physical thing."
      },
      {
            "t",
            "The rest run on something broader: a wide range of real Minecraft data, nearby creatures, sound, light, temperature, block type, fed as general signal into the real wiring for every other documented neuron. Its entirely the real brain's own call on what to do."
      },
      {"h", "COMMANDS"},
      {"t", "/flylab                 : help"},
      {"t", "/flylab arena ...       : the 33 test boxes, one at a time or all of them"},
      {"t", "/flylab spawn ...       : a female, or a courting male"},
      {"t", "/flylab body ...        : the real fly model, or a plain bee"},
      {"t", "/flylab stacked on|off  : keep every fly you spawn, or one experiment at a time"},
      {"t", "/flylab hungry, thirsty : make her hungry or thirsty now"},
      {"t", "/flylab fly, mate, loom : send her flying, start a courtship, show her a looming shape"},
      {"t", "/flylab book            : the field guide, as an in-game book"},
      {"t", "/flyhud small|medium|big: the live readout and its size (/flyhud alone hides it)"},
      {"t", "/flycredits             : the credits screen: what is real, what is calibrated, what is labeled"},
      {"h", "ONE MORE THING,"},
      {"t", "Every reaction this thing has, traces back through a real neuron, wired exactly like it is inside an actual living fly's head."},
      {"t", "You're not watching a scripted mob. You're watching an actual piece of a real brain, thinking, live, inside your world."}
   };
   private final List<IntroScreen.OrderedLine> lines = new ArrayList<>();
   private double scroll;
   private int contentHeight;

   public IntroScreen() {
      super(Text.literal("connectome-fly"));
   }

   protected void init() {
      this.lines.clear();
      int width = Math.min(this.width - 60, 340);

      for (String[] row : CONTENT) {
         String kind = row[0];
         Text styled;
         int color;
         switch (kind) {
            case "title":
               styled = Text.literal(row[1]).formatted(Formatting.BOLD);
               color = 16777215;
               break;
            case "h":
               styled = Text.literal(row[1]).formatted(new Formatting[]{Formatting.AQUA, Formatting.BOLD});
               color = 10545407;
               break;
            default:
               styled = Text.literal(row[1]);
               color = 14737632;
         }

         boolean first = true;

         for (OrderedText ordered : this.textRenderer.wrapLines(styled, width)) {
            this.lines.add(new IntroScreen.OrderedLine(ordered, color, first && !row[0].equals("t")));
            first = false;
         }
      }

      this.contentHeight = 0;

      for (IntroScreen.OrderedLine line : this.lines) {
         this.contentHeight = this.contentHeight + (line.gap() ? 16 : 10);
      }

      this.addDrawableChild(
         ButtonWidget.builder(Text.literal("Let's go"), b -> this.close()).dimensions(this.width / 2 - 105, this.height - 28, 100, 20).build()
      );
      this.addDrawableChild(
         ButtonWidget.builder(Text.literal("Credits"), b -> this.client.setScreen(new CreditsScreen()))
            .dimensions(this.width / 2 + 5, this.height - 28, 100, 20)
            .build()
      );
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      int y = 20 - (int)this.scroll;
      int x = (this.width - Math.min(this.width - 60, 340)) / 2;

      for (IntroScreen.OrderedLine line : this.lines) {
         if (line.gap()) {
            y += 6;
         }

         if (y > 8 && y < this.height - 34) {
            context.drawTextWithShadow(this.textRenderer, line.text(), x, y, line.color());
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

   public boolean shouldPause() {
      return false;
   }

   private record OrderedLine(OrderedText text, int color, boolean gap) {
   }
}

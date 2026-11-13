package io.flyproject.connectomefly.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * The fly lab menu (key G): everything the /flygenes and /flylab commands do, as buttons.
 * Each button sends the matching command for the player, so it needs cheats or operator rights, like the commands.
 */
public class FlyMenuScreen extends Screen {
   private static String bodyId = "fly";
   private static String bodyName = "Fly";
   private static boolean male = false;
   private static String search = "";
   private EditBox searchBox;
   private final List<Button> results = new ArrayList<>();
   private final List<String[]> labels = new ArrayList<>();
   private int leftX;
   private int rightX;
   private int top;

   public FlyMenuScreen() {
      super(Component.literal("Fly Lab"));
   }

   private void cmd(String command) {
      if (this.minecraft.player != null) {
         this.minecraft.player.connection.sendCommand(command);
      }
   }

   private Button btn(String text, int x, int y, int w, Runnable run) {
      return this.addRenderableWidget(Button.builder(Component.literal(text), b -> run.run()).bounds(x, y, w, 14).build());
   }

   private void label(String text, int x, int y) {
      this.labels.add(new String[]{text, String.valueOf(x), String.valueOf(y)});
   }

   @Override
   protected void init() {
      this.labels.clear();
      this.results.clear();
      int colW = 196;
      this.leftX = this.width / 2 - colW - 6;
      this.rightX = this.width / 2 + 6;
      this.top = Math.max(16, (this.height - 212) / 2 + 12);
      int x = this.leftX;
      int y = this.top;
      // ---- left: make a creature
      label("Body for the fly's brain", x, y);
      y += 10;
      this.btn("Fly", x, y, 62, () -> this.pick("fly", "Fly"));
      this.btn("Villager", x + 66, y, 62, () -> this.pick("villager", "Villager"));
      this.btn("Cow", x + 132, y, 64, () -> this.pick("minecraft:cow", "Cow"));
      y += 17;
      label("or any mob: type to search", x, y);
      y += 10;
      this.searchBox = new EditBox(this.font, x, y, colW, 14, Component.literal("Search mobs"));
      this.searchBox.setMaxLength(40);
      this.searchBox.setValue(search);
      this.searchBox.setResponder(v -> {
         search = v;
         this.fillResults();
      });
      this.addRenderableWidget(this.searchBox);
      y += 16;

      for (int i = 0; i < 4; i++) {
         final int slot = i;
         Button b = this.btn("", x + i % 2 * 99, y + i / 2 * 15, 97, () -> this.pickResult(slot));
         this.results.add(b);
      }

      y += 33;
      label("", x, y);
      y += 11;
      this.btn(male ? "Sex: male" : "Sex: female", x, y, 96, () -> {
         male = !male;
         this.rebuildWidgets();
      });
      this.btn("Spawn here", x + 100, y, 96, () -> this.cmd("flygenes spawn " + this.bodyArg() + (male ? " male" : " female")));
      y += 16;
      this.btn("Give this body to the nearest one", x, y, colW, () -> this.cmd("flygenes body " + this.bodyArgForChange()));
      y += 19;
      label("Readout", x, y + 3);
      this.btn(FlyHud.modeName(), x + 50, y, 70, () -> {
         FlyHud.cycle();
         this.rebuildWidgets();
      });
      this.btn("small", x + 124, y, 34, () -> FlyHud.setSize("small"));
      this.btn("mid", x + 160, y, 17, () -> FlyHud.setSize("medium"));
      this.btn("big", x + 179, y, 17, () -> FlyHud.setSize("big"));
      // ---- right: world rules. Each button sets that value; the game answers in chat.
      x = this.rightX;
      y = this.top;
      y = this.row("Whole brains running at once", x, y, new String[]{"1", "2", "3", "4", "6", "8"}, v -> "flylab brains " + v);
      y = this.row("Adult life, minutes [model]", x, y, new String[]{"10", "30", "60", "120", "240"}, v -> "flygenes set adult_life_minutes " + v);
      y = this.row("Growing up, times faster [model]", x, y, new String[]{"1", "5", "20", "60"}, v -> "flygenes set growth_speed " + v);
      y = this.row("Population cap [model]", x, y, new String[]{"6", "12", "24", "48"}, v -> "flygenes set cap " + v);
      y = this.row("Death and hunger rules [model]", x, y, new String[]{"on", "off"}, v -> "flygenes set selection " + (v.equals("on") ? "1" : "0"));
      y = this.row("Inherited memory [model]", x, y, new String[]{"on", "off"}, v -> "flygenes memory " + (v.equals("on") ? "true" : "false"));
      y += 1;
      this.btn("Statistics", x, y, 64, () -> {
         this.cmd("flygenes stats");
         this.onClose();
      });
      this.btn("List genes", x + 66, y, 64, () -> {
         this.cmd("flygenes");
         this.onClose();
      });
      this.btn("Field guide", x + 132, y, 64, () -> this.cmd("flylab book"));
      y += 16;
      this.btn("Build 33 test boxes", x, y, 130, () -> {
         this.cmd("flylab arena all");
         this.onClose();
      });
      this.btn("Credits", x + 132, y, 64, () -> this.minecraft.gui.setScreen(new CreditsScreen()));
      this.btn("Done", this.width / 2 - 40, this.top + 186, 80, this::onClose);
      this.fillResults();
   }

   private int row(String title, int x, int y, String[] values, java.util.function.Function<String, String> command) {
      label(title, x, y);
      y += 9;
      int w = Math.min(38, (196 - (values.length - 1) * 2) / values.length);

      for (int i = 0; i < values.length; i++) {
         String v = values[i];
         this.btn(v, x + i * (w + 2), y, w, () -> this.cmd(command.apply(v)));
      }

      return y + 16;
   }

   private void pick(String id, String name) {
      bodyId = id;
      bodyName = name;
      this.rebuildWidgets();
   }

   private String bodyArg() {
      return bodyId.equals("fly") ? "fly" : (bodyId.equals("villager") ? "villager" : "mob " + bodyId);
   }

   private String bodyArgForChange() {
      return bodyId;
   }

   private final List<String[]> found = new ArrayList<>();

   /** Living mobs whose id or name contains the search text. */
   private void fillResults() {
      this.found.clear();
      String q = search.trim().toLowerCase(Locale.ROOT);

      for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
         if (t.getCategory() == MobCategory.MISC && t != net.minecraft.world.entity.EntityTypes.VILLAGER && t != net.minecraft.world.entity.EntityTypes.IRON_GOLEM && t != net.minecraft.world.entity.EntityTypes.SNOW_GOLEM) {
            continue;
         }

         Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(t);
         if (id.getNamespace().equals("connectomefly")) {
            continue;
         }

         String name = t.getDescription().getString();
         if (q.isEmpty() || id.getPath().contains(q) || name.toLowerCase(Locale.ROOT).contains(q)) {
            this.found.add(new String[]{id.toString(), name});
         }
      }

      this.found.sort((a, b) -> a[1].compareToIgnoreCase(b[1]));

      for (int i = 0; i < this.results.size(); i++) {
         Button b = this.results.get(i);
         boolean has = i < this.found.size();
         b.visible = has;
         b.active = has;
         if (has) {
            b.setMessage(Component.literal(this.found.get(i)[1]));
         }
      }
   }

   private void pickResult(int slot) {
      if (slot < this.found.size()) {
         this.pick(this.found.get(slot)[0], this.found.get(slot)[1]);
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
      super.extractRenderState(ctx, mouseX, mouseY, delta);
      ctx.centeredText(this.font, "FLY LAB", this.width / 2, this.top - 14, 0xFFFFFFFF);

      for (String[] l : this.labels) {
         String text = l[0];
         if (text.isEmpty()) {
            text = "Chosen: " + bodyName + (this.found.size() > 4 ? "  (" + this.found.size() + " match, type more)" : "");
            ctx.text(this.font, text, Integer.parseInt(l[1]), Integer.parseInt(l[2]), 0xFF8FE3A0);
         } else {
            ctx.text(this.font, text, Integer.parseInt(l[1]), Integer.parseInt(l[2]), 0xFFB8C4BC);
         }
      }

      ctx.centeredText(
         this.font, "Needs cheats on. [model] = an invented rule.", this.width / 2, this.top + 203, 0xFF8A968E
      );
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}

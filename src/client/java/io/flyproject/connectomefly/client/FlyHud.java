package io.flyproject.connectomefly.client;

import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.AABB;

public final class FlyHud {
   public static boolean enabled = true;
   /** false = compact card (the default), true = every line the original mod shows */
   public static boolean detail = false;
   private static final java.util.Set<String> COMPACT = java.util.Set.of(
      "Doing", "Body [model]", "Generation", "Parents", "Genes [model]", "Age [model]", "Senses firing", "Brain outputs", "Circuit"
   );

   private static Path detailFile() {
      return FabricLoader.getInstance().getConfigDir().resolve("connectomefly_hud_detail");
   }

   public static void setDetail(boolean on) {
      detail = on;

      try {
         if (on) {
            Files.writeString(detailFile(), "on");
         } else {
            Files.deleteIfExists(detailFile());
         }
      } catch (Exception var2) {
      }
   }

   /** compact -> full -> off -> compact */
   public static String cycle() {
      if (!enabled) {
         setEnabled(true);
         setDetail(false);
      } else if (!detail) {
         setDetail(true);
      } else {
         setEnabled(false);
      }

      return modeName();
   }

   public static String modeName() {
      return !enabled ? "off" : (detail ? "full" : "compact");
   }

   private static float num(String s, float fallback) {
      try {
         return Float.parseFloat(s.trim());
      } catch (Exception var3) {
         return fallback;
      }
   }

   /** silent = dark blue-grey, then teal, cyan, near white at 13 Hz and above */
   private static int heat(int level) {
      if (level <= 0) {
         return 0xFF14222B;
      }

      float t = Math.min(1.0F, level / 15.0F);
      int r = (int)(20 + 235 * t * t);
      int g = (int)(60 + 195 * Math.sqrt(t));
      int b = (int)(90 + 165 * Math.sqrt(t));
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   private static void bar(GuiGraphicsExtractor ctx, Font tr, String label, float frac, String text, int x, int y, int w, int color) {
      ctx.text(tr, label, x, y, -7697248);
      int bx = x + tr.width(label) + 3;
      int bw = w - tr.width(label) - 3;
      ctx.fill(bx, y + 1, bx + bw, y + 7, 0xFF1B2A33);
      ctx.fill(bx, y + 1, bx + Math.round(bw * Math.max(0.0F, Math.min(1.0F, frac))), y + 7, color);
   }

   public static float scale = 1.0F;
   private static final int PANEL_W = 236;
   private static final int LINE = 10;

   private static Path offFile() {
      return FabricLoader.getInstance().getConfigDir().resolve("connectomefly_hud_off");
   }

   /** Shows or hides the readout and remembers the choice for next time. */
   public static void setEnabled(boolean on) {
      enabled = on;

      try {
         if (on) {
            Files.deleteIfExists(offFile());
         } else {
            Files.writeString(offFile(), "off");
         }
      } catch (Exception var2) {
      }
   }

   private static Path sizeFile() {
      return FabricLoader.getInstance().getConfigDir().resolve("connectomefly_hud.txt");
   }

   public static void setSize(String name) {
      scale = name.equals("small") ? 0.75F : (name.equals("big") ? 1.3F : 1.0F);

      try {
         Files.writeString(sizeFile(), name);
      } catch (Exception var2) {
      }
   }

   public static void loadSize() {
      try {
         enabled = !Files.exists(offFile());
         detail = Files.exists(detailFile());
      } catch (Exception var2) {
      }

      try {
         if (Files.exists(sizeFile())) {
            setSize(Files.readString(sizeFile()).trim());
         }
      } catch (Exception var1) {
      }
   }

   private FlyHud() {
   }

   public static void register() {
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("connectomefly", "readout"), (ctx, delta) -> render(ctx));
   }

   private static void render(GuiGraphicsExtractor ctx) {
      if (enabled) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null && !mc.gui.hud.isHidden()) {
            ConnectomeFlyEntity fly = null;
            double best = 1600.0;

            for (ConnectomeFlyEntity f : mc.level.getEntitiesOfClass(ConnectomeFlyEntity.class, new AABB(mc.player.blockPosition()).inflate(40.0), e -> true)) {
               double d = mc.player.distanceToSqr(f);
               if (d < best) {
                  best = d;
                  fly = f;
               }
            }

            if (fly != null) {
               String hud = fly.getHud();
               List<FlyHud.Line> lines = new ArrayList<>();
               String[] fields = hud.split(";");
               String gGen = "";
               String gBody = "";
               String gPar = "";
               String gGenes = "";
               String gLife = "";
               String body = "";
               String doing = "";
               String box = "";
               String brain = "";
               String dn = "";
               String chans = "";
               String esc = "";
               String hp = "";
               String walk = "";
               String turn = "";
               String hunger = "";
               String outs = "";
               String cmp = "";
               String mem = "";
               String slp = "";
               String sat = "";
               String see = "";
               String court = "";
               String clock = "";
               String taste = "";
               String opto = "";
               String social = "";
               String thirst = "";
               String air = "";

               for (String fx : fields) {
                  int k = fx.indexOf(61);
                  if (k >= 0) {
                     String key = fx.substring(0, k);
                     String val = fx.substring(k + 1);
                     switch (key) {
                        case "body":
                           body = val;
                           break;
                        case "doing":
                           doing = val;
                           break;
                        case "box":
                           box = val;
                           break;
                        case "brain":
                           brain = val;
                           break;
                        case "dn":
                           dn = val;
                           break;
                        case "chans":
                           chans = val;
                           break;
                        case "esc":
                           esc = val;
                           break;
                        case "hp":
                           hp = val;
                           break;
                        case "walk":
                           walk = val;
                           break;
                        case "turn":
                           turn = val;
                           break;
                        case "hunger":
                           hunger = val;
                           break;
                        case "gen":
                           gGen = val;
                           break;
                        case "bodykind":
                           gBody = val;
                           break;
                        case "par":
                           gPar = val;
                           break;
                        case "genes":
                           gGenes = val;
                           break;
                        case "life":
                           gLife = val;
                           break;
                        case "outs":
                           outs = val;
                           break;
                        case "cmp":
                           cmp = val;
                           break;
                        case "mem":
                           mem = val;
                           break;
                        case "slp":
                           slp = val;
                           break;
                        case "sat":
                           sat = val;
                           break;
                        case "see":
                           see = val;
                           break;
                        case "court":
                           court = val;
                           break;
                        case "clock":
                           clock = val;
                           break;
                        case "taste":
                           taste = val;
                           break;
                        case "opto":
                           opto = val;
                           break;
                        case "social":
                           social = val;
                           break;
                        case "thirst":
                           thirst = val;
                           break;
                        case "air":
                           air = val;
                     }
                  }
               }

               boolean male = fly.isMale();
               lines.add(new FlyHud.Line("Body now", body.isEmpty() ? "-" : body, -1513240));
               lines.add(new FlyHud.Line("Doing", doing.isEmpty() ? "roaming from her brain" : doing, doing.startsWith("[brain]") ? -20256 : -1513240));
               lines.add(new FlyHud.Line("Box", box.isEmpty() ? "open world · every sense live" : box, -4667137));
               if (!male) {
                  lines.add(new FlyHud.Line("Whole brain", brain.isEmpty() ? "waking (158,262 neurons)" : brain + " spikes/step · 158,262 neurons", -1513240));
                  lines.add(new FlyHud.Line("Descending", dn.isEmpty() ? "-" : dn + "  → turn " + turn + " · walk " + walk, -4653104));
                  lines.add(new FlyHud.Line("Senses firing", chans.isEmpty() ? "background only" : chans, -8032));
                  lines.add(new FlyHud.Line("Brain outputs", outs.isEmpty() ? "at rest" : outs, -20256));
                  lines.add(new FlyHud.Line("Compass", cmp.isEmpty() ? "no EPG bump yet" : cmp, -4658945));
                  lines.add(new FlyHud.Line("Memory", mem.isEmpty() ? "-" : mem, mem.contains("PAM") ? -4653104 : (mem.contains("PPL1") ? -28528 : -1513240)));
                  lines.add(new FlyHud.Line("Sleep", slp.isEmpty() ? "-" : slp, slp.contains("ASLEEP") ? -4148993 : -1513240));
                  lines.add(new FlyHud.Line("Satiety", sat.isEmpty() ? "-" : sat, -2052000));
                  lines.add(new FlyHud.Line("Vision", see.isEmpty() ? "-" : see, see.startsWith("[brain]") ? -20256 : -6644560));
                  lines.add(new FlyHud.Line("Optomotor", opto.isEmpty() ? "-" : opto, opto.contains("turn") ? -20256 : -6644560));
                  lines.add(new FlyHud.Line("Courtship", court.isEmpty() ? "-" : court, court.contains("song") ? -16152 : -6644560));
                  lines.add(new FlyHud.Line("Social", social.isEmpty() ? "-" : social, social.contains("rival") ? -28528 : -6644560));
                  lines.add(new FlyHud.Line("Clock", clock.isEmpty() ? "-" : clock, clock.contains("peak") ? -8032 : -6644560));
                  lines.add(new FlyHud.Line("Taste", taste.isEmpty() ? "-" : taste, taste.contains("nothing") ? -6644560 : -2052000));
               } else {
                  lines.add(new FlyHud.Line("Circuit", "MaleCNS courtship: P1 → pIP10 → song CPG", -1513240));
               }

               lines.add(new FlyHud.Line("Escape circuit", esc.isEmpty() ? "-" : esc, esc.contains("FIRED") ? -36768 : -1513240));
               lines.add(new FlyHud.Line("Health", hp.isEmpty() ? "-" : hp + " / 10", -1513240));
               if (!male) {
                  float hv = 0.0F;

                  try {
                     hv = Float.parseFloat(hunger);
                  } catch (Exception var44) {
                  }

                  StringBuilder bar = new StringBuilder();

                  for (int i = 0; i < 10; i++) {
                     bar.append((char)(i < Math.round(hv / 2.0F) ? '|' : '.'));
                  }

                  lines.add(new FlyHud.Line("Hunger", bar + "  " + hunger + " / 20", -2052000));
                  lines.add(new FlyHud.Line("Thirst", thirst.isEmpty() ? "-" : thirst + " / 20", -7288577));
                  lines.add(new FlyHud.Line("Flight", air.isEmpty() ? "-" : air, -4660993));
               }

               boolean villager = !gBody.isEmpty() && !gBody.equals("fly");
               if (villager) {
                  lines.add(3, new FlyHud.Line("Body [model]", gBody + " body, fly brain: none of that mob's own behaviour", -5570646));
               }

               if (!gGen.isEmpty()) {
                  // [model] genetics readout: invented inheritance rules, labelled as such
                  lines.add(3 + (villager ? 1 : 0), new FlyHud.Line("Generation", gGen + " [model]" + (male ? " · genes unused in him" : ""), -5570646));
                  lines.add(4 + (villager ? 1 : 0), new FlyHud.Line("Parents", gPar, -5570646));
                  lines.add(5 + (villager ? 1 : 0), new FlyHud.Line("Genes [model]", gGenes, -5570646));
                  lines.add(6 + (villager ? 1 : 0), new FlyHud.Line("Age [model]", gLife, -5570646));
               }

               if (!detail) {
                  lines.removeIf(l -> !COMPACT.contains(l.key()));
               }

               Font tr = mc.font;
               String map = male ? "" : fly.getBrainMap();
               int cols = 32;
               int rows = map.isEmpty() ? 0 : (map.length() + cols - 1) / cols;
               int cw = 7;
               int ch = 4;
               int mapH = rows == 0 ? 0 : 12 + rows * ch + 12;
               int x = 6;
               int y = 6;
               int h = 22 + lines.size() * 10 + 14 + mapH + 12 + 4;
               // never taller than the window: shrink the whole card if it would run off the bottom
               float fit = Math.min(scale, (mc.getWindow().getGuiScaledHeight() - 6) / (float)(h + y + 2));
               ctx.pose().pushMatrix();
               ctx.pose().scale(fit, fit);
               ctx.fill(x - 2, y - 2, x + 236, y + h, -1609560044);
               ctx.fill(x - 2, y - 2, x + 236, y - 1, -11606332);
               ctx.text(tr, (villager ? "FLY-BRAINED " + gBody.toUpperCase(java.util.Locale.ROOT) + " [model]" : "CONNECTOME FLY") + (male ? "  ·  male" : ""), x + 2, y + 2, -1);
               String live = "LIVE";
               ctx.text(tr, live, x + 236 - 6 - tr.width(live), y + 2, -11606332);
               ctx.text(tr, "real BANC wiring · calibrated readout", x + 2, y + 2 + 10, -7697248);
               int yy = y + 2 + 20 + 4;

               for (FlyHud.Line l : lines) {
                  ctx.text(tr, l.key(), x + 2, yy, -7697248);
                  String v = l.value();
                  int maxW = 148;
                  if (tr.width(v) > maxW) {
                     v = tr.plainSubstrByWidth(v, maxW - 6) + "…";
                  }

                  ctx.text(tr, v, x + 82, yy, l.color() | 0xFF000000);
                  yy += 10;
               }

               // health, hunger and thirst as bars
               yy += 2;
               bar(ctx, tr, "Health", num(hp, 0.0F) / 10.0F, hp, x + 2, yy, male ? 110 : 72, 0xFFE0524E);
               if (!male) {
                  bar(ctx, tr, "Food", num(hunger, 0.0F) / 20.0F, hunger, x + 80, yy, 72, 0xFFE0B040);
                  bar(ctx, tr, "Thirst", num(thirst, 0.0F) / 20.0F, thirst, x + 158, yy, 74, 0xFF58A6FF);
               }

               yy += 12;
               if (rows > 0) {
                  // the whole brain, live: one cell per group of about 309 neurons
                  ctx.text(tr, "Whole brain, live · " + brain + " spikes/ms", x + 2, yy, -1513240);
                  yy += 11;
                  int active = 0;

                  for (int i = 0; i < map.length(); i++) {
                     int level = map.charAt(i) - 'a';
                     if (level > 0) {
                        active++;
                     }

                     int cx = x + 2 + i % cols * cw;
                     int cy = yy + i / cols * ch;
                     ctx.fill(cx, cy, cx + cw - 1, cy + ch - 1, heat(level));
                  }

                  yy += rows * ch + 2;
                  ctx.text(tr, active + " of " + map.length() + " groups firing · index order, not anatomy", x + 2, yy, -9802624);
                  yy += 11;
               }

               ctx.text(tr, "G menu · H readout: " + modeName() + " · speech is translated", x + 2, yy + 1, -9802624);
               ctx.pose().popMatrix();
            }
         }
      }
   }

   private record Line(String key, String value, int color) {
   }
}

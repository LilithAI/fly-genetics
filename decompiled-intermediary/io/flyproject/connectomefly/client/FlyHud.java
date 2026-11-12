package io.flyproject.connectomefly.client;

import io.flyproject.connectomefly.entity.ConnectomeFlyEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_238;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

public final class FlyHud {
   public static boolean enabled = true;
   public static float scale = 1.0F;
   private static final int PANEL_W = 236;
   private static final int LINE = 10;

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
         if (Files.exists(sizeFile())) {
            setSize(Files.readString(sizeFile()).trim());
         }
      } catch (Exception var1) {
      }
   }

   private FlyHud() {
   }

   public static void register() {
      HudRenderCallback.EVENT.register(FlyHud::render);
   }

   private static void render(class_332 ctx, float tickDelta) {
      if (enabled) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 != null && mc.field_1687 != null && !mc.field_1690.field_1842) {
            ConnectomeFlyEntity fly = null;
            double best = 1600.0;

            for (ConnectomeFlyEntity f : mc.field_1687
               .method_8390(ConnectomeFlyEntity.class, new class_238(mc.field_1724.method_24515()).method_1014(40.0), e -> true)) {
               double d = mc.field_1724.method_5858(f);
               if (d < best) {
                  best = d;
                  fly = f;
               }
            }

            if (fly != null) {
               String hud = fly.getHud();
               List<FlyHud.Line> lines = new ArrayList<>();
               String[] fields = hud.split(";");
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

               class_327 tr = mc.field_1772;
               ctx.method_51448().method_22903();
               ctx.method_51448().method_22905(scale, scale, 1.0F);
               int x = 6;
               int y = 6;
               int h = 22 + lines.size() * 10 + 12 + 4;
               ctx.method_25294(x - 2, y - 2, x + 236, y + h, -1609560044);
               ctx.method_25294(x - 2, y - 2, x + 236, y - 1, -11606332);
               ctx.method_25303(tr, "CONNECTOME FLY" + (male ? "  ·  male" : ""), x + 2, y + 2, -1);
               String live = "LIVE";
               ctx.method_25303(tr, live, x + 236 - 6 - tr.method_1727(live), y + 2, -11606332);
               ctx.method_25303(tr, "real BANC wiring · calibrated readout", x + 2, y + 2 + 10, -7697248);
               int yy = y + 2 + 20 + 4;

               for (FlyHud.Line l : lines) {
                  ctx.method_25303(tr, l.key(), x + 2, yy, -7697248);
                  String v = l.value();
                  int maxW = 148;
                  if (tr.method_1727(v) > maxW) {
                     v = tr.method_27523(v, maxW - 6) + "…";
                  }

                  ctx.method_25303(tr, v, x + 82, yy, l.color());
                  yy += 10;
               }

               ctx.method_25303(tr, "speech: translated, not decoded · /flyhud hides this", x + 2, yy + 3, -9802624);
               ctx.method_51448().method_22909();
            }
         }
      }
   }

   private record Line(String key, String value, int color) {
   }
}

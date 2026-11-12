package io.flyproject.connectomefly.lab;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.flyproject.connectomefly.entity.FlyMind;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2269;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2349;
import net.minecraft.class_2350;
import net.minecraft.class_2397;
import net.minecraft.class_2482;
import net.minecraft.class_2508;
import net.minecraft.class_2561;
import net.minecraft.class_2595;
import net.minecraft.class_2625;
import net.minecraft.class_2680;
import net.minecraft.class_2738;
import net.minecraft.class_2771;
import net.minecraft.class_3218;
import net.minecraft.class_3715;
import net.minecraft.class_3722;
import net.minecraft.class_3830;
import net.minecraft.class_5218;
import net.minecraft.class_5556;
import net.minecraft.class_8242;

public final class ArenaBuilder {
   public static final Map<String, String[]> SENSES = new LinkedHashMap<>();
   public static final Map<String, Set<FlyMind.Kind>> ISOLATION = new LinkedHashMap<>();
   private static final Map<String, List<ArenaBuilder.Arena>> REGISTRY = new HashMap<>();
   private static final int H = 5;

   private ArenaBuilder() {
   }

   public static List<String> ids() {
      return List.copyOf(SENSES.keySet());
   }

   private static String key(class_3218 world) {
      return world.method_27983().method_29177().toString();
   }

   private static Path file(class_3218 world) {
      return world.method_8503().method_27050(class_5218.field_24188).resolve("connectomefly_arenas_" + key(world).replace(':', '_') + ".json");
   }

   private static List<ArenaBuilder.Arena> arenas(class_3218 world) {
      return REGISTRY.computeIfAbsent(
         key(world),
         k -> {
            List<ArenaBuilder.Arena> list = new ArrayList<>();

            try {
               Path f = file(world);
               if (Files.exists(f)) {
                  for (JsonElement e : (JsonArray)new Gson().fromJson(Files.readString(f), JsonArray.class)) {
                     JsonObject o = e.getAsJsonObject();
                     list.add(
                        new ArenaBuilder.Arena(
                           o.get("id").getAsString(),
                           o.get("x0").getAsInt(),
                           o.get("y0").getAsInt(),
                           o.get("z0").getAsInt(),
                           o.get("x1").getAsInt(),
                           o.get("y1").getAsInt(),
                           o.get("z1").getAsInt()
                        )
                     );
                  }
               }
            } catch (Exception var8) {
            }

            return list;
         }
      );
   }

   private static void register(class_3218 world, ArenaBuilder.Arena a) {
      List<ArenaBuilder.Arena> list = arenas(world);
      list.removeIf(ox -> ox.contains(new class_2338((a.x0 + a.x1) / 2, a.y0, (a.z0 + a.z1) / 2)));
      list.add(a);

      try {
         JsonArray arr = new JsonArray();

         for (ArenaBuilder.Arena o : list) {
            JsonObject j = new JsonObject();
            j.addProperty("id", o.id());
            j.addProperty("x0", o.x0());
            j.addProperty("y0", o.y0());
            j.addProperty("z0", o.z0());
            j.addProperty("x1", o.x1());
            j.addProperty("y1", o.y1());
            j.addProperty("z1", o.z1());
            arr.add(j);
         }

         Files.writeString(file(world), arr.toString());
      } catch (Exception var7) {
      }
   }

   public static ArenaBuilder.Arena spawnButtonAt(class_3218 world, class_2338 p) {
      for (ArenaBuilder.Arena a : arenas(world)) {
         if (p.method_10263() == (a.x0 + a.x1) / 2 + 1 && p.method_10264() == a.y0 + 2 && p.method_10260() == a.z0 - 1) {
            return a;
         }
      }

      return null;
   }

   public static class_2338 spawnSpot(ArenaBuilder.Arena a) {
      int[] o = spawnOffset(a.id());
      return new class_2338((a.x0 + a.x1) / 2 + o[0], a.y0 + 1, (a.z0 + a.z1) / 2 + o[1]);
   }

   public static List<ArenaBuilder.Arena> all(class_3218 world) {
      return List.copyOf(arenas(world));
   }

   public static ArenaBuilder.Arena arenaAt(class_3218 world, class_2338 p) {
      for (ArenaBuilder.Arena a : arenas(world)) {
         if (a.contains(p)) {
            return a;
         }
      }

      return null;
   }

   public static String instructions(String id) {
      String[] spec = SENSES.getOrDefault(id, SENSES.get("home"));
      return "The book on the lectern by the door explains this box. Real neurons in it: " + spec[2].replace('|', ' ') + ".";
   }

   public static class_2338 build(class_3218 world, class_2338 origin, String id) {
      String[] spec = SENSES.getOrDefault(id, SENSES.get("home"));
      int y = origin.method_10264();
      class_2338 c = new class_2338(origin.method_10263(), y, origin.method_10260());
      class_2680 floor = class_2246.field_10467.method_9564();
      class_2680 rim = class_2246.field_10093.method_9564();
      class_2680 glass = !id.equals("light") && !id.equals("sleep") ? class_2246.field_10033.method_9564() : class_2246.field_27115.method_9564();
      class_2680 air = class_2246.field_10124.method_9564();

      for (int dx = -6; dx <= 6; dx++) {
         for (int dz = -6; dz <= 6; dz++) {
            boolean edge = Math.abs(dx) == 6 || Math.abs(dz) == 6;
            world.method_8501(c.method_10069(dx, -1, dz), edge ? rim : floor);

            for (int dy = 0; dy <= 4; dy++) {
               world.method_8501(c.method_10069(dx, dy, dz), edge && dy <= 3 ? glass : air);
            }

            if (!id.equals("weather")) {
               world.method_8501(c.method_10069(dx, 4, dz), id.equals("light") ? glass : class_2246.field_10033.method_9564());
            }
         }
      }

      class_2338 door = c.method_10069(0, 0, -6);
      world.method_8501(door, (class_2680)class_2246.field_10188.method_9564().method_11657(class_2349.field_11177, class_2350.field_11035));
      world.method_8501(door.method_10084(), air);
      String num = spec[0].matches("^[0-9]+ .*") ? spec[0].substring(0, spec[0].indexOf(32)) : "";
      String name = num.isEmpty() ? spec[0] : spec[0].substring(num.length() + 1);
      sign(world, c.method_10069(-2, 0, -7), num.isEmpty() ? name : "BOX " + num, num.isEmpty() ? "|the book|explains" : name + "||read the book");
      sign(world, c.method_10069(2, 0, -7), "real connectome", spec[2]);
      class_2338 lec = c.method_10069(-1, 0, -7);
      world.method_8501(
         lec,
         (class_2680)((class_2680)class_2246.field_16330.method_9564().method_11657(class_3715.field_16404, class_2350.field_11035))
            .method_11657(class_3715.field_17366, true)
      );
      if (world.method_8321(lec) instanceof class_3722 lb) {
         lb.method_17513(BoxBooks.book(id, spec[0]));
      }

      world.method_8501(c.method_10069(1, 0, -7), class_2246.field_10421.method_9564());
      world.method_8501(c.method_10069(1, 1, -7), (class_2680)class_2246.field_10057.method_9564().method_11657(class_2269.field_11007, class_2738.field_12475));
      register(
         world,
         new ArenaBuilder.Arena(id, c.method_10263() - 5 - 1, y - 1, c.method_10260() - 5 - 1, c.method_10263() + 5 + 1, y + 4, c.method_10260() + 5 + 1)
      );
      switch (id) {
         case "escape":
            for (int dzx = 2; dzx <= 5; dzx++) {
               world.method_8501(c.method_10069(0, -1, dzx), class_2246.field_10458.method_9564());
            }

            world.method_8501(c.method_10069(-2, 0, -4), class_2246.field_10093.method_9564());
            world.method_8501(
               c.method_10069(-2, 1, -4), (class_2680)class_2246.field_10494.method_9564().method_11657(class_2269.field_11007, class_2738.field_12475)
            );
            break;
         case "steering":
            class_2680 pillar = class_2246.field_23151.method_9564();

            for (int dx = -4; dx <= 4; dx += 4) {
               for (int dz = -4; dz <= 4; dz += 4) {
                  if (dx != 0 || dz != 0) {
                     for (int dy = 0; dy <= 2; dy++) {
                        world.method_8501(c.method_10069(dx, dy, dz), pillar);
                     }
                  }
               }
            }
            break;
         case "touch":
            world.method_8501(c.method_10069(2, 0, 2), class_2246.field_10620.method_9564());
            world.method_8501(c.method_10069(-2, 0, -1), class_2246.field_10620.method_9564());
            world.method_8501(c.method_10069(0, 0, 3), class_2246.field_10625.method_9564());
            break;
         case "hearing":
            world.method_8501(c.method_10069(-3, 0, 3), class_2246.field_10179.method_9564());
            world.method_8501(c.method_10069(3, 0, 3), class_2246.field_10223.method_9564());
            discChest(world, c.method_10069(4, 0, 3));
            world.method_8501(c.method_10069(0, 0, 4), class_2246.field_16332.method_9564());
            break;
         case "navigation":
            world.method_8501(c.method_10069(-4, 0, -3), (class_2680)class_2246.field_28674.method_9564().method_11657(class_2397.field_11200, true));
            world.method_8501(c.method_10069(4, 0, 4), class_2246.field_10183.method_9564());
            class_2338 var48 = c.method_10069(-3, 0, -3);
            break;
         case "memory":
            world.method_8501(c.method_10069(-4, 0, 3), class_2246.field_10183.method_9564());
            world.method_8501(c.method_10069(4, 0, 3), class_2246.field_10545.method_9564());
            world.method_8501(c.method_10069(-4, -1, 3), class_2246.field_10107.method_9564());
            world.method_8501(c.method_10069(4, -1, 3), class_2246.field_10421.method_9564());
            class_2338 var47 = c.method_10069(0, 0, -4);
            break;
         case "aggression":
         case "courtship":
         case "grooming":
            world.method_8501(c.method_10069(5, -1, 5), class_2246.field_10346.method_9564());
            if (id.equals("grooming")) {
               world.method_8501(c.method_10069(-5, 0, 5), class_2246.field_10343.method_9564());
            }

            if (id.equals("aggression")) {
               world.method_8501(c.method_10069(0, 0, 1), class_2246.field_10183.method_9564());
               class_2338 var46 = c.method_10069(-1, 0, 0);
            }
            break;
         case "taste":
            world.method_8501(c.method_10069(-2, 0, 2), class_2246.field_21211.method_9564());
            world.method_8501(c.method_10069(0, 0, 3), class_2246.field_10183.method_9564());
            world.method_8501(c.method_10069(2, 0, 2), class_2246.field_10545.method_9564());
            world.method_8501(c.method_10069(-3, 0, -2), class_2246.field_10606.method_9564());
            world.method_8501(c.method_10069(3, 0, -2), (class_2680)class_2246.field_27097.method_9564().method_11657(class_5556.field_27206, 3));
            class_2338 var45 = c.method_10069(0, 0, -3);
            break;
         case "smell":
            world.method_8501(c.method_10069(0, 0, 4), class_2246.field_10183.method_9564());
            world.method_8501(c.method_10069(-4, -1, -4), class_2246.field_17350.method_9564());
            world.method_8501(c.method_10069(-4, 0, -4), class_2246.field_10576.method_9564());
            class_2338 var44 = c.method_10069(0, 0, -3);
            break;
         case "hunger":
            for (int[] p : new int[][]{{-3, 3}, {-2, 3}, {3, 3}}) {
               world.method_8501(c.method_10069(p[0], -1, p[1]), class_2246.field_10566.method_9564());
               world.method_8501(c.method_10069(p[0], 0, p[1]), (class_2680)class_2246.field_16999.method_9564().method_11657(class_3830.field_17000, 3));
            }

            world.method_8501(c.method_10069(0, 0, 4), class_2246.field_10183.method_9564());
            class_2338 var43 = c.method_10069(0, 0, -3);
            break;
         case "lure":
            chest(world, c.method_10069(0, 0, -7).method_10089(4));
            break;
         case "heat":
            world.method_8501(c.method_10069(3, -1, 3), class_2246.field_10164.method_9564());

            for (int[] p : new int[][]{{2, 3}, {4, 3}, {3, 2}, {3, 4}}) {
               world.method_8501(c.method_10069(p[0], -1, p[1]), class_2246.field_10092.method_9564());
            }

            class_2338 var42 = c.method_10069(-3, 0, -3);
            break;
         case "water":
            for (int dx = 1; dx <= 2; dx++) {
               for (int dz = 1; dz <= 2; dz++) {
                  world.method_8501(c.method_10069(dx, -1, dz), class_2246.field_10382.method_9564());
               }
            }

            class_2338 var41 = c.method_10069(-3, 0, -3);
            break;
         case "sleep":
            world.method_8501(c.method_10069(5, -1, 5), class_2246.field_10346.method_9564());
            break;
         case "weather":
            for (int dx = -5; dx <= -2; dx++) {
               for (int dz = 2; dz <= 5; dz++) {
                  world.method_8501(
                     c.method_10069(dx, 2, dz), (class_2680)class_2246.field_10500.method_9564().method_11657(class_2482.field_11501, class_2771.field_12681)
                  );
               }
            }

            for (int[] p : new int[][]{{-5, 2}, {-2, 2}, {-5, 5}, {-2, 5}}) {
               world.method_8501(c.method_10069(p[0], 0, p[1]), class_2246.field_10132.method_9564());
               world.method_8501(c.method_10069(p[0], 1, p[1]), class_2246.field_10132.method_9564());
            }

            class_2338 var40 = c.method_10069(3, 0, -3);
            break;
         case "cold":
            for (int dx = 1; dx <= 3; dx++) {
               for (int dz = 1; dz <= 3; dz++) {
                  world.method_8501(c.method_10069(dx, -1, dz), class_2246.field_10491.method_9564());
               }
            }

            world.method_8501(c.method_10069(2, -1, 2), class_2246.field_10225.method_9564());
            world.method_8501(c.method_10069(-3, -1, -3), class_2246.field_27879.method_9564());
            class_2338 var39 = c.method_10069(-2, 0, 0);
            break;
         case "wind":
            world.method_8501(c.method_10069(-5, 0, 0), class_2246.field_23261.method_9564());
            world.method_8501(c.method_10069(-4, 0, 0), class_2246.field_10576.method_9564());
            class_2338 var38 = c.method_10069(1, 0, 0);
            break;
         case "light":
            for (int dz = -1; dz <= 1; dz++) {
               world.method_8501(c.method_10069(5, 1, dz), class_2246.field_10171.method_9564());
            }

            class_2338 var37 = c.method_10069(-3, 0, 0);
            break;
         case "mould":
            for (int dx = 2; dx <= 4; dx++) {
               for (int dz = 2; dz <= 4; dz++) {
                  world.method_8501(c.method_10069(dx, -1, dz), class_2246.field_10402.method_9564());
               }
            }

            world.method_8501(c.method_10069(3, 0, 3), class_2246.field_10251.method_9564());
            world.method_8501(c.method_10069(2, 0, 4), class_2246.field_10559.method_9564());
            world.method_8501(c.method_10069(4, 0, 2), class_2246.field_10183.method_9564());
            class_2338 var36 = c.method_10069(-3, 0, -3);
            break;
         case "terrain":
            class_2680 slab = (class_2680)class_2246.field_10262.method_9564().method_11657(class_2482.field_11501, class_2771.field_12681);

            for (int dx = -4; dx <= 4; dx++) {
               world.method_8501(c.method_10069(dx, 0, 1), slab);
               world.method_8501(c.method_10069(dx, 0, 2), class_2246.field_10467.method_9564());
               world.method_8501(c.method_10069(dx, 1, 2), slab);
               world.method_8501(c.method_10069(dx, 0, 4), class_2246.field_10056.method_9564());
               world.method_8501(c.method_10069(dx, 1, 4), class_2246.field_10056.method_9564());
            }

            class_2338 var35 = c.method_10069(0, 0, -3);
            break;
         case "bitter":
            world.method_8501(c.method_10069(0, 0, 3), class_2246.field_10183.method_9564());

            for (int[] p : new int[][]{{-1, 3}, {1, 3}, {0, 4}, {-1, 2}, {1, 2}}) {
               world.method_8501(c.method_10069(p[0], 0, p[1]), class_2246.field_10606.method_9564());
            }

            class_2338 var34 = c.method_10069(0, 0, -3);
            break;
         case "co2":
            world.method_8501(c.method_10069(0, -1, 1), class_2246.field_17350.method_9564());
            world.method_8501(c.method_10069(0, 0, 1), class_2246.field_10576.method_9564());
            world.method_8501(c.method_10069(0, 0, 4), class_2246.field_10183.method_9564());
            class_2338 var33 = c.method_10069(0, 0, -3);
            break;
         case "pain":
            for (int[] p : new int[][]{{0, 2}, {-1, 3}, {1, 3}, {-2, 4}, {2, 4}}) {
               world.method_8501(c.method_10069(p[0], -2, p[1]), class_2246.field_10467.method_9564());
               world.method_8501(c.method_10069(p[0], -1, p[1]), class_2246.field_10102.method_9564());
               world.method_8501(c.method_10069(p[0], 0, p[1]), class_2246.field_10029.method_9564());
            }

            world.method_8501(c.method_10069(0, 0, 4), class_2246.field_10183.method_9564());
            class_2338 var32 = c.method_10069(0, 0, -3);
            break;
         case "damp":
            for (int dx = 2; dx <= 4; dx++) {
               for (int dz = 2; dz <= 4; dz++) {
                  world.method_8501(c.method_10069(dx, -1, dz), class_2246.field_37576.method_9564());
               }
            }

            world.method_8501(c.method_10069(3, -1, 3), class_2246.field_10562.method_9564());
            class_2338 var31 = c.method_10069(-2, 0, -2);
            break;
         case "dry":
            for (int dx = 1; dx <= 5; dx++) {
               for (int dz = -5; dz <= 5; dz++) {
                  world.method_8501(c.method_10069(dx, -2, dz), class_2246.field_10467.method_9564());
                  world.method_8501(
                     c.method_10069(dx, -1, dz), (dx + dz & 1) == 0 ? class_2246.field_10102.method_9564() : class_2246.field_10415.method_9564()
                  );
               }
            }

            class_2338 var30 = c.method_10069(-3, 0, 0);
            break;
         case "vision":
            world.method_8501(c.method_10069(5, -1, 5), class_2246.field_10346.method_9564());
            class_2338 var29 = c.method_10069(0, 0, 0);
            break;
         case "optomotor":
            OptomotorDrum.build(world, c);
            class_2338 var28 = c.method_10069(0, 0, 0);
            break;
         case "social":
            world.method_8501(c.method_10069(0, 0, 2), class_2246.field_10183.method_9564());
            class_2338 var27 = c.method_10069(-3, 0, -3);
            break;
         case "looming":
            for (int[] p : new int[][]{{0, 4}, {4, 0}, {0, -4}, {-4, 0}, {3, 3}, {-3, 3}, {3, -3}, {-3, -3}}) {
               world.method_8501(c.method_10069(p[0], -1, p[1]), class_2246.field_10458.method_9564());
            }

            world.method_8501(c.method_10069(-2, 0, -4), class_2246.field_10093.method_9564());
            world.method_8501(
               c.method_10069(-2, 1, -4), (class_2680)class_2246.field_10494.method_9564().method_11657(class_2269.field_11007, class_2738.field_12475)
            );
            class_2338 var26 = c.method_10069(0, 0, 0);
            break;
         case "surfaces":
            for (int dx = -5; dx <= 5; dx++) {
               for (int dz = -5; dz <= 5; dz++) {
                  world.method_8501(c.method_10069(dx, 3, dz), class_2246.field_10033.method_9564());
                  world.method_8501(c.method_10069(dx, 4, dz), class_2246.field_10124.method_9564());
               }
            }

            world.method_8501(c.method_10069(2, 0, 2), class_2246.field_10431.method_9564());
            world.method_8501(c.method_10069(2, 1, 2), class_2246.field_10431.method_9564());
            class_2338 var25 = c.method_10069(-3, 0, -3);
            break;
         default:
            world.method_8501(c.method_10069(5, -1, 5), class_2246.field_10346.method_9564());
      }

      return c.method_10069(spawnOffset(id)[0], 0, spawnOffset(id)[1]);
   }

   public static int[] spawnOffset(String id) {
      return switch (id) {
         case "navigation", "heat", "water", "mould", "social", "surfaces" -> new int[]{-3, -3};
         case "memory" -> new int[]{0, -4};
         case "aggression" -> new int[]{-1, 0};
         case "taste", "smell", "hunger", "terrain", "bitter", "co2", "pain" -> new int[]{0, -3};
         case "weather" -> new int[]{3, -3};
         case "cold" -> new int[]{-2, 0};
         case "wind" -> new int[]{1, 0};
         case "light" -> new int[]{-3, 0};
         case "dry" -> new int[]{3, 0};
         case "damp" -> new int[]{-2, -2};
         default -> new int[]{0, 0};
      };
   }

   public static class_2338 buildAll(class_3218 world, class_2338 origin) {
      int pitch = 16;
      class_2338 first = null;
      int i = 0;

      for (String id : SENSES.keySet()) {
         int col = i % 6;
         int row = i / 6;
         class_2338 c = origin.method_10069((col - 2) * pitch - pitch / 2, 0, row * pitch);
         class_2338 s = build(world, c, id);
         if (first == null) {
            first = s;
         }

         i++;
      }

      return first;
   }

   private static void sign(class_3218 world, class_2338 pos, String title, String body) {
      world.method_8501(pos, (class_2680)class_2246.field_10121.method_9564().method_11657(class_2508.field_11559, 8));
      if (world.method_8321(pos) instanceof class_2625 sign) {
         String[] lines = body.split("\\|");
         class_8242 text = new class_8242();
         text = text.method_49857(0, class_2561.method_43470(title));

         for (int k = 0; k < 3 && k < lines.length; k++) {
            text = text.method_49857(k + 1, class_2561.method_43470(lines[k]));
         }

         sign.method_49840(text, true);
         sign.method_49849(true);
         sign.method_5431();
      }
   }

   private static void discChest(class_3218 world, class_2338 pos) {
      world.method_8501(pos, (class_2680)class_2246.field_10034.method_9564().method_11657(class_2281.field_10768, class_2350.field_11035));
      if (world.method_8321(pos) instanceof class_2595 chest) {
         chest.method_5447(0, new class_1799(class_1802.field_8075, 1));
         chest.method_5447(1, new class_1799(class_1802.field_8425, 1));
         chest.method_5447(2, new class_1799(class_1802.field_8623, 1));
         chest.method_5431();
      }
   }

   private static void chest(class_3218 world, class_2338 pos) {
      world.method_8501(pos, class_2246.field_10034.method_9564());
      if (world.method_8321(pos) instanceof class_2595 chest) {
         chest.method_5447(0, new class_1799(class_1802.field_8880, 8));
         chest.method_5447(1, new class_1799(class_1802.field_8491, 8));
         chest.method_5447(2, new class_1799(class_1802.field_16998, 16));
         chest.method_5447(3, new class_1799(class_1802.field_17534, 2));
         chest.method_5447(4, new class_1799(class_1802.field_8719, 2));
         chest.method_5431();
      }
   }

   static {
      SENSES.put("escape", new String[]{"1 ESCAPE / VISION", "press the BUTTON:|a shape looms in|(or sprint at her)", "LC4 / LPLC2|Giant Fiber|TTMn (jump)"});
      SENSES.put("steering", new String[]{"2 STEERING", "scare her thru|the pillars|(or sprint at her)", "DNa01 / DNa02|313 leg motor|neurons"});
      SENSES.put("touch", new String[]{"3 TOUCH", "punch her once|=> scramble,|then grooming", "644 bristle|neurons, leg|motor neurons"});
      SENSES.put("hearing", new String[]{"4 HEARING", "click the NOTE|BLOCK or ring|the bell", "JO-A / JO-B|onto the|Giant Fiber"});
      SENSES.put("navigation", new String[]{"5 NAVIGATION", "she leaves the|leaf and returns|by her compass", "45 EPG cells:|her compass|ring"});
      SENSES.put("memory", new String[]{"6 MEMORY", "cake or melon:|punch her at one,|she picks the other", "Kenyon cells|PPL1 dopamine|MBONs"});
      SENSES.put("aggression", new String[]{"7 AGGRESSION", "two hungry females|one cake: lunge,|loser backs off", "pC1d / pC1e|DNa11 / DNp37"});
      SENSES.put("courtship", new String[]{"8 COURTSHIP", "he sings ~10 s,|she accepts, mates,|lays an egg", "JO-B hearing|pC1 cells:|her yes"});
      SENSES.put("taste", new String[]{"9 TASTE", "cake = sugar; rose|= bitter; mud +|cauldron = water", "Gr5a / Gr64f|Gr33a / Gr66a|ppk28 (water)"});
      SENSES.put("smell", new String[]{"10 SMELL", "the cake at the|far end: she|tracks the odor", "1655 smell|receptor|neurons"});
      SENSES.put("hunger", new String[]{"11 HUNGER", "she starts hungry|and eats; again:|/flylab hungry", "IPC insulin|cells, 903|food ORNs"});
      SENSES.put("lure", new String[]{"12 LURE", "hold a FLOWER or|berries at the|glass and walk", "752 fruit|smell receptor|neurons"});
      SENSES.put("heat", new String[]{"13 HEAT / PAIN", "lure her toward|the lava cell|(magma ring)", "Gr28a heat|180 pain|neurons"});
      SENSES.put("water", new String[]{"14 WATER", "steer her into|the pool", "20 humid and|35 dry air|neurons"});
      SENSES.put("sleep", new String[]{"15 SLEEP / CLOCK", "/time set 14000:|R5 cells win: sleep|dawn, dusk: active", "R5 sleep cells|l-LNv light|s-LNv / LNd"});
      SENSES.put("weather", new String[]{"16 RAIN / SHELTER", "/weather rain =>|she runs under|the roof", "Ir68a / Ir40a|moisture|neurons"});
      SENSES.put("cold", new String[]{"17 COLD", "the snow patch:|she backs off|(stand her on it)", "41 Ir21a|cooling|neurons"});
      SENSES.put("wind", new String[]{"18 WIND", "the fan blows|east: she turns|to face it", "792 JO wind|neurons"});
      SENSES.put("light", new String[]{"19 LIGHT", "tinted box: she|walks toward the|glowstone", "7548 visual|neurons"});
      SENSES.put("mould", new String[]{"20 MOULD", "mycelium + cake:|she refuses the|mouldy corner", "Or56a geosmin|receptor|neurons"});
      SENSES.put("grooming", new String[]{"21 GROOMING", "let her rest:|she cleans head,|wings, hind legs", "6054 bristle|neurons"});
      SENSES.put("terrain", new String[]{"22 TERRAIN", "slabs = steps she|climbs; the block|wall stops her", "2903 leg|proprioceptor|neurons"});
      SENSES.put("bitter", new String[]{"23 BITTER", "roses touch the|cake: she tastes,|refuses, leaves", "Gr33a / Gr66a|bitter taste|neurons"});
      SENSES.put("co2", new String[]{"24 CO2", "campfire smoke|between her and|the cake: detour", "Gr21a / Gr63a|CO2 receptor|neurons"});
      SENSES.put("pain", new String[]{"25 PAIN", "cactus round the|cake: pricked, she|recoils and learns", "180 pain|neurons, PPL1|dopamine"});
      SENSES.put("damp", new String[]{"26 DAMP", "she starts thirsty:|walks to the mud|and drinks", "Ir68a / Ir40a|ppk28 (water)"});
      SENSES.put("dry", new String[]{"27 DRY", "sand + terracotta|half: her dry|sensors fire", "Ir93a / Ir25a|dry air|neurons"});
      SENSES.put("vision", new String[]{"28 VISION", "walk past the|glass: the HUD says|which eye saw you", "LC11 / LC10a|small object|detectors"});
      SENSES.put("optomotor", new String[]{"29 OPTOMOTOR", "/flylab drum left|or right: the ring|turns, so does she", "T4 / T5 motion|HS cells"});
      SENSES.put("social", new String[]{"30 SOCIAL", "three females and|a cake: they gather|then squabble", "Or47b / Or67d|pheromone|neurons"});
      SENSES.put("surfaces", new String[]{"31 SURFACES", "a low roof: she|lands on the walls|and hangs from it", "the same leg|motor neurons|any way up"});
      SENSES.put("looming", new String[]{"32 LOOMING", "press the BUTTON:|a shape looms in|from a random side", "LC4 / LPLC2|Giant Fiber|TTMn (jump)"});
      SENSES.put("home", new String[]{"HOME", "a calm pen:|she roams from|her own brain", "158,262 neurons|ticking:|she's here"});
      Class<FlyMind.Kind> K = FlyMind.Kind.class;
      ISOLATION.put("escape", EnumSet.noneOf(K));
      ISOLATION.put("steering", EnumSet.of(FlyMind.Kind.PREDATOR));
      ISOLATION.put("touch", EnumSet.noneOf(K));
      ISOLATION.put("hearing", EnumSet.of(FlyMind.Kind.SPEAKER));
      ISOLATION.put("navigation", EnumSet.of(FlyMind.Kind.HOME, FlyMind.Kind.FOOD));
      ISOLATION.put("memory", EnumSet.of(FlyMind.Kind.FOOD));
      ISOLATION.put("aggression", EnumSet.of(FlyMind.Kind.FOOD));
      ISOLATION.put("courtship", EnumSet.of(FlyMind.Kind.MATE));
      ISOLATION.put("taste", EnumSet.of(FlyMind.Kind.FOOD, FlyMind.Kind.LURE, FlyMind.Kind.BITTER, FlyMind.Kind.DAMP));
      ISOLATION.put("smell", EnumSet.of(FlyMind.Kind.FOOD, FlyMind.Kind.LURE, FlyMind.Kind.CO2));
      ISOLATION.put("hunger", EnumSet.of(FlyMind.Kind.FOOD, FlyMind.Kind.LURE));
      ISOLATION.put("lure", EnumSet.of(FlyMind.Kind.LURE, FlyMind.Kind.FOOD));
      ISOLATION.put("heat", EnumSet.of(FlyMind.Kind.LAVA, FlyMind.Kind.LURE));
      ISOLATION.put("water", EnumSet.of(FlyMind.Kind.POND, FlyMind.Kind.LURE));
      ISOLATION.put("sleep", EnumSet.noneOf(K));
      ISOLATION.put("weather", EnumSet.noneOf(K));
      ISOLATION.put("cold", EnumSet.of(FlyMind.Kind.SNOW, FlyMind.Kind.LURE));
      ISOLATION.put("wind", EnumSet.of(FlyMind.Kind.FAN));
      ISOLATION.put("light", EnumSet.of(FlyMind.Kind.LIGHT));
      ISOLATION.put("mould", EnumSet.of(FlyMind.Kind.GEOSMIN, FlyMind.Kind.FOOD));
      ISOLATION.put("grooming", EnumSet.noneOf(K));
      ISOLATION.put("terrain", EnumSet.noneOf(K));
      ISOLATION.put("bitter", EnumSet.of(FlyMind.Kind.BITTER, FlyMind.Kind.FOOD));
      ISOLATION.put("co2", EnumSet.of(FlyMind.Kind.CO2, FlyMind.Kind.FOOD));
      ISOLATION.put("pain", EnumSet.of(FlyMind.Kind.PAIN, FlyMind.Kind.FOOD));
      ISOLATION.put("damp", EnumSet.of(FlyMind.Kind.DAMP));
      ISOLATION.put("dry", EnumSet.of(FlyMind.Kind.DRY));
      ISOLATION.put("vision", EnumSet.noneOf(K));
      ISOLATION.put("optomotor", EnumSet.noneOf(K));
      ISOLATION.put("social", EnumSet.of(FlyMind.Kind.FOOD));
      ISOLATION.put("surfaces", EnumSet.noneOf(K));
      ISOLATION.put("looming", EnumSet.noneOf(K));
   }

   public record Arena(String id, int x0, int y0, int z0, int x1, int y1, int z1) {
      boolean contains(class_2338 p) {
         return p.method_10263() >= this.x0
            && p.method_10263() <= this.x1
            && p.method_10264() >= this.y0
            && p.method_10264() <= this.y1
            && p.method_10260() >= this.z0
            && p.method_10260() <= this.z1;
      }
   }
}

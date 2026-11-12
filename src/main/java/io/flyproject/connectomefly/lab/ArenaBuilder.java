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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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

   private static String key(ServerWorld world) {
      return world.getRegistryKey().getValue().toString();
   }

   private static Path file(ServerWorld world) {
      return world.getServer().getSavePath(WorldSavePath.ROOT).resolve("connectomefly_arenas_" + key(world).replace(':', '_') + ".json");
   }

   private static List<ArenaBuilder.Arena> arenas(ServerWorld world) {
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

   private static void register(ServerWorld world, ArenaBuilder.Arena a) {
      List<ArenaBuilder.Arena> list = arenas(world);
      list.removeIf(ox -> ox.contains(new BlockPos((a.x0 + a.x1) / 2, a.y0, (a.z0 + a.z1) / 2)));
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

   public static ArenaBuilder.Arena spawnButtonAt(ServerWorld world, BlockPos p) {
      for (ArenaBuilder.Arena a : arenas(world)) {
         if (p.getX() == (a.x0 + a.x1) / 2 + 1 && p.getY() == a.y0 + 2 && p.getZ() == a.z0 - 1) {
            return a;
         }
      }

      return null;
   }

   public static BlockPos spawnSpot(ArenaBuilder.Arena a) {
      int[] o = spawnOffset(a.id());
      return new BlockPos((a.x0 + a.x1) / 2 + o[0], a.y0 + 1, (a.z0 + a.z1) / 2 + o[1]);
   }

   public static List<ArenaBuilder.Arena> all(ServerWorld world) {
      return List.copyOf(arenas(world));
   }

   public static ArenaBuilder.Arena arenaAt(ServerWorld world, BlockPos p) {
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

   public static BlockPos build(ServerWorld world, BlockPos origin, String id) {
      String[] spec = SENSES.getOrDefault(id, SENSES.get("home"));
      int y = origin.getY();
      BlockPos c = new BlockPos(origin.getX(), y, origin.getZ());
      BlockState floor = Blocks.SMOOTH_SANDSTONE.getDefaultState();
      BlockState rim = Blocks.POLISHED_ANDESITE.getDefaultState();
      BlockState glass = !id.equals("light") && !id.equals("sleep") ? Blocks.GLASS.getDefaultState() : Blocks.TINTED_GLASS.getDefaultState();
      BlockState air = Blocks.AIR.getDefaultState();

      for (int dx = -6; dx <= 6; dx++) {
         for (int dz = -6; dz <= 6; dz++) {
            boolean edge = Math.abs(dx) == 6 || Math.abs(dz) == 6;
            world.setBlockState(c.add(dx, -1, dz), edge ? rim : floor);

            for (int dy = 0; dy <= 4; dy++) {
               world.setBlockState(c.add(dx, dy, dz), edge && dy <= 3 ? glass : air);
            }

            if (!id.equals("weather")) {
               world.setBlockState(c.add(dx, 4, dz), id.equals("light") ? glass : Blocks.GLASS.getDefaultState());
            }
         }
      }

      BlockPos door = c.add(0, 0, -6);
      world.setBlockState(door, (BlockState)Blocks.OAK_FENCE_GATE.getDefaultState().with(FenceGateBlock.FACING, Direction.SOUTH));
      world.setBlockState(door.up(), air);
      String num = spec[0].matches("^[0-9]+ .*") ? spec[0].substring(0, spec[0].indexOf(32)) : "";
      String name = num.isEmpty() ? spec[0] : spec[0].substring(num.length() + 1);
      sign(world, c.add(-2, 0, -7), num.isEmpty() ? name : "BOX " + num, num.isEmpty() ? "|the book|explains" : name + "||read the book");
      sign(world, c.add(2, 0, -7), "real connectome", spec[2]);
      BlockPos lec = c.add(-1, 0, -7);
      world.setBlockState(
         lec, (BlockState)((BlockState)Blocks.LECTERN.getDefaultState().with(LecternBlock.FACING, Direction.SOUTH)).with(LecternBlock.HAS_BOOK, true)
      );
      if (world.getBlockEntity(lec) instanceof LecternBlockEntity lb) {
         lb.setBook(BoxBooks.book(id, spec[0]));
      }

      world.setBlockState(c.add(1, 0, -7), Blocks.LIME_CONCRETE.getDefaultState());
      world.setBlockState(c.add(1, 1, -7), (BlockState)Blocks.OAK_BUTTON.getDefaultState().with(ButtonBlock.FACE, WallMountLocation.FLOOR));
      register(world, new ArenaBuilder.Arena(id, c.getX() - 5 - 1, y - 1, c.getZ() - 5 - 1, c.getX() + 5 + 1, y + 4, c.getZ() + 5 + 1));
      switch (id) {
         case "escape":
            for (int dzx = 2; dzx <= 5; dzx++) {
               world.setBlockState(c.add(0, -1, dzx), Blocks.BLACK_CONCRETE.getDefaultState());
            }

            world.setBlockState(c.add(-2, 0, -4), Blocks.POLISHED_ANDESITE.getDefaultState());
            world.setBlockState(c.add(-2, 1, -4), (BlockState)Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACE, WallMountLocation.FLOOR));
            break;
         case "steering":
            BlockState pillar = Blocks.POLISHED_BASALT.getDefaultState();

            for (int dx = -4; dx <= 4; dx += 4) {
               for (int dz = -4; dz <= 4; dz += 4) {
                  if (dx != 0 || dz != 0) {
                     for (int dy = 0; dy <= 2; dy++) {
                        world.setBlockState(c.add(dx, dy, dz), pillar);
                     }
                  }
               }
            }
            break;
         case "touch":
            world.setBlockState(c.add(2, 0, 2), Blocks.OAK_FENCE.getDefaultState());
            world.setBlockState(c.add(-2, 0, -1), Blocks.OAK_FENCE.getDefaultState());
            world.setBlockState(c.add(0, 0, 3), Blocks.COBBLESTONE_WALL.getDefaultState());
            break;
         case "hearing":
            world.setBlockState(c.add(-3, 0, 3), Blocks.NOTE_BLOCK.getDefaultState());
            world.setBlockState(c.add(3, 0, 3), Blocks.JUKEBOX.getDefaultState());
            discChest(world, c.add(4, 0, 3));
            world.setBlockState(c.add(0, 0, 4), Blocks.BELL.getDefaultState());
            break;
         case "navigation":
            world.setBlockState(c.add(-4, 0, -3), (BlockState)Blocks.FLOWERING_AZALEA_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true));
            world.setBlockState(c.add(4, 0, 4), Blocks.CAKE.getDefaultState());
            BlockPos var48 = c.add(-3, 0, -3);
            break;
         case "memory":
            world.setBlockState(c.add(-4, 0, 3), Blocks.CAKE.getDefaultState());
            world.setBlockState(c.add(4, 0, 3), Blocks.MELON.getDefaultState());
            world.setBlockState(c.add(-4, -1, 3), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(c.add(4, -1, 3), Blocks.LIME_CONCRETE.getDefaultState());
            BlockPos var47 = c.add(0, 0, -4);
            break;
         case "aggression":
         case "courtship":
         case "grooming":
            world.setBlockState(c.add(5, -1, 5), Blocks.POLISHED_DIORITE.getDefaultState());
            if (id.equals("grooming")) {
               world.setBlockState(c.add(-5, 0, 5), Blocks.COBWEB.getDefaultState());
            }

            if (id.equals("aggression")) {
               world.setBlockState(c.add(0, 0, 1), Blocks.CAKE.getDefaultState());
               BlockPos var46 = c.add(-1, 0, 0);
            }
            break;
         case "taste":
            world.setBlockState(c.add(-2, 0, 2), Blocks.HONEY_BLOCK.getDefaultState());
            world.setBlockState(c.add(0, 0, 3), Blocks.CAKE.getDefaultState());
            world.setBlockState(c.add(2, 0, 2), Blocks.MELON.getDefaultState());
            world.setBlockState(c.add(-3, 0, -2), Blocks.WITHER_ROSE.getDefaultState());
            world.setBlockState(c.add(3, 0, -2), (BlockState)Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
            BlockPos var45 = c.add(0, 0, -3);
            break;
         case "smell":
            world.setBlockState(c.add(0, 0, 4), Blocks.CAKE.getDefaultState());
            world.setBlockState(c.add(-4, -1, -4), Blocks.CAMPFIRE.getDefaultState());
            world.setBlockState(c.add(-4, 0, -4), Blocks.IRON_BARS.getDefaultState());
            BlockPos var44 = c.add(0, 0, -3);
            break;
         case "hunger":
            for (int[] p : new int[][]{{-3, 3}, {-2, 3}, {3, 3}}) {
               world.setBlockState(c.add(p[0], -1, p[1]), Blocks.DIRT.getDefaultState());
               world.setBlockState(c.add(p[0], 0, p[1]), (BlockState)Blocks.SWEET_BERRY_BUSH.getDefaultState().with(SweetBerryBushBlock.AGE, 3));
            }

            world.setBlockState(c.add(0, 0, 4), Blocks.CAKE.getDefaultState());
            BlockPos var43 = c.add(0, 0, -3);
            break;
         case "lure":
            chest(world, c.add(0, 0, -7).east(4));
            break;
         case "heat":
            world.setBlockState(c.add(3, -1, 3), Blocks.LAVA.getDefaultState());

            for (int[] p : new int[][]{{2, 3}, {4, 3}, {3, 2}, {3, 4}}) {
               world.setBlockState(c.add(p[0], -1, p[1]), Blocks.MAGMA_BLOCK.getDefaultState());
            }

            BlockPos var42 = c.add(-3, 0, -3);
            break;
         case "water":
            for (int dx = 1; dx <= 2; dx++) {
               for (int dz = 1; dz <= 2; dz++) {
                  world.setBlockState(c.add(dx, -1, dz), Blocks.WATER.getDefaultState());
               }
            }

            BlockPos var41 = c.add(-3, 0, -3);
            break;
         case "sleep":
            world.setBlockState(c.add(5, -1, 5), Blocks.POLISHED_DIORITE.getDefaultState());
            break;
         case "weather":
            for (int dx = -5; dx <= -2; dx++) {
               for (int dz = 2; dz <= 5; dz++) {
                  world.setBlockState(c.add(dx, 2, dz), (BlockState)Blocks.DARK_OAK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM));
               }
            }

            for (int[] p : new int[][]{{-5, 2}, {-2, 2}, {-5, 5}, {-2, 5}}) {
               world.setBlockState(c.add(p[0], 0, p[1]), Blocks.DARK_OAK_FENCE.getDefaultState());
               world.setBlockState(c.add(p[0], 1, p[1]), Blocks.DARK_OAK_FENCE.getDefaultState());
            }

            BlockPos var40 = c.add(3, 0, -3);
            break;
         case "cold":
            for (int dx = 1; dx <= 3; dx++) {
               for (int dz = 1; dz <= 3; dz++) {
                  world.setBlockState(c.add(dx, -1, dz), Blocks.SNOW_BLOCK.getDefaultState());
               }
            }

            world.setBlockState(c.add(2, -1, 2), Blocks.PACKED_ICE.getDefaultState());
            world.setBlockState(c.add(-3, -1, -3), Blocks.POWDER_SNOW.getDefaultState());
            BlockPos var39 = c.add(-2, 0, 0);
            break;
         case "wind":
            world.setBlockState(c.add(-5, 0, 0), Blocks.LODESTONE.getDefaultState());
            world.setBlockState(c.add(-4, 0, 0), Blocks.IRON_BARS.getDefaultState());
            BlockPos var38 = c.add(1, 0, 0);
            break;
         case "light":
            for (int dz = -1; dz <= 1; dz++) {
               world.setBlockState(c.add(5, 1, dz), Blocks.GLOWSTONE.getDefaultState());
            }

            BlockPos var37 = c.add(-3, 0, 0);
            break;
         case "mould":
            for (int dx = 2; dx <= 4; dx++) {
               for (int dz = 2; dz <= 4; dz++) {
                  world.setBlockState(c.add(dx, -1, dz), Blocks.MYCELIUM.getDefaultState());
               }
            }

            world.setBlockState(c.add(3, 0, 3), Blocks.BROWN_MUSHROOM.getDefaultState());
            world.setBlockState(c.add(2, 0, 4), Blocks.RED_MUSHROOM.getDefaultState());
            world.setBlockState(c.add(4, 0, 2), Blocks.CAKE.getDefaultState());
            BlockPos var36 = c.add(-3, 0, -3);
            break;
         case "terrain":
            BlockState slab = (BlockState)Blocks.SMOOTH_SANDSTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);

            for (int dx = -4; dx <= 4; dx++) {
               world.setBlockState(c.add(dx, 0, 1), slab);
               world.setBlockState(c.add(dx, 0, 2), Blocks.SMOOTH_SANDSTONE.getDefaultState());
               world.setBlockState(c.add(dx, 1, 2), slab);
               world.setBlockState(c.add(dx, 0, 4), Blocks.STONE_BRICKS.getDefaultState());
               world.setBlockState(c.add(dx, 1, 4), Blocks.STONE_BRICKS.getDefaultState());
            }

            BlockPos var35 = c.add(0, 0, -3);
            break;
         case "bitter":
            world.setBlockState(c.add(0, 0, 3), Blocks.CAKE.getDefaultState());

            for (int[] p : new int[][]{{-1, 3}, {1, 3}, {0, 4}, {-1, 2}, {1, 2}}) {
               world.setBlockState(c.add(p[0], 0, p[1]), Blocks.WITHER_ROSE.getDefaultState());
            }

            BlockPos var34 = c.add(0, 0, -3);
            break;
         case "co2":
            world.setBlockState(c.add(0, -1, 1), Blocks.CAMPFIRE.getDefaultState());
            world.setBlockState(c.add(0, 0, 1), Blocks.IRON_BARS.getDefaultState());
            world.setBlockState(c.add(0, 0, 4), Blocks.CAKE.getDefaultState());
            BlockPos var33 = c.add(0, 0, -3);
            break;
         case "pain":
            for (int[] p : new int[][]{{0, 2}, {-1, 3}, {1, 3}, {-2, 4}, {2, 4}}) {
               world.setBlockState(c.add(p[0], -2, p[1]), Blocks.SMOOTH_SANDSTONE.getDefaultState());
               world.setBlockState(c.add(p[0], -1, p[1]), Blocks.SAND.getDefaultState());
               world.setBlockState(c.add(p[0], 0, p[1]), Blocks.CACTUS.getDefaultState());
            }

            world.setBlockState(c.add(0, 0, 4), Blocks.CAKE.getDefaultState());
            BlockPos var32 = c.add(0, 0, -3);
            break;
         case "damp":
            for (int dx = 2; dx <= 4; dx++) {
               for (int dz = 2; dz <= 4; dz++) {
                  world.setBlockState(c.add(dx, -1, dz), Blocks.MUD.getDefaultState());
               }
            }

            world.setBlockState(c.add(3, -1, 3), Blocks.WET_SPONGE.getDefaultState());
            BlockPos var31 = c.add(-2, 0, -2);
            break;
         case "dry":
            for (int dx = 1; dx <= 5; dx++) {
               for (int dz = -5; dz <= 5; dz++) {
                  world.setBlockState(c.add(dx, -2, dz), Blocks.SMOOTH_SANDSTONE.getDefaultState());
                  world.setBlockState(c.add(dx, -1, dz), (dx + dz & 1) == 0 ? Blocks.SAND.getDefaultState() : Blocks.TERRACOTTA.getDefaultState());
               }
            }

            BlockPos var30 = c.add(-3, 0, 0);
            break;
         case "vision":
            world.setBlockState(c.add(5, -1, 5), Blocks.POLISHED_DIORITE.getDefaultState());
            BlockPos var29 = c.add(0, 0, 0);
            break;
         case "optomotor":
            OptomotorDrum.build(world, c);
            BlockPos var28 = c.add(0, 0, 0);
            break;
         case "social":
            world.setBlockState(c.add(0, 0, 2), Blocks.CAKE.getDefaultState());
            BlockPos var27 = c.add(-3, 0, -3);
            break;
         case "looming":
            for (int[] p : new int[][]{{0, 4}, {4, 0}, {0, -4}, {-4, 0}, {3, 3}, {-3, 3}, {3, -3}, {-3, -3}}) {
               world.setBlockState(c.add(p[0], -1, p[1]), Blocks.BLACK_CONCRETE.getDefaultState());
            }

            world.setBlockState(c.add(-2, 0, -4), Blocks.POLISHED_ANDESITE.getDefaultState());
            world.setBlockState(c.add(-2, 1, -4), (BlockState)Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACE, WallMountLocation.FLOOR));
            BlockPos var26 = c.add(0, 0, 0);
            break;
         case "surfaces":
            for (int dx = -5; dx <= 5; dx++) {
               for (int dz = -5; dz <= 5; dz++) {
                  world.setBlockState(c.add(dx, 3, dz), Blocks.GLASS.getDefaultState());
                  world.setBlockState(c.add(dx, 4, dz), Blocks.AIR.getDefaultState());
               }
            }

            world.setBlockState(c.add(2, 0, 2), Blocks.OAK_LOG.getDefaultState());
            world.setBlockState(c.add(2, 1, 2), Blocks.OAK_LOG.getDefaultState());
            BlockPos var25 = c.add(-3, 0, -3);
            break;
         default:
            world.setBlockState(c.add(5, -1, 5), Blocks.POLISHED_DIORITE.getDefaultState());
      }

      return c.add(spawnOffset(id)[0], 0, spawnOffset(id)[1]);
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

   public static BlockPos buildAll(ServerWorld world, BlockPos origin) {
      int pitch = 16;
      BlockPos first = null;
      int i = 0;

      for (String id : SENSES.keySet()) {
         int col = i % 6;
         int row = i / 6;
         BlockPos c = origin.add((col - 2) * pitch - pitch / 2, 0, row * pitch);
         BlockPos s = build(world, c, id);
         if (first == null) {
            first = s;
         }

         i++;
      }

      return first;
   }

   private static void sign(ServerWorld world, BlockPos pos, String title, String body) {
      world.setBlockState(pos, (BlockState)Blocks.OAK_SIGN.getDefaultState().with(SignBlock.ROTATION, 8));
      if (world.getBlockEntity(pos) instanceof SignBlockEntity sign) {
         String[] lines = body.split("\\|");
         SignText text = new SignText();
         text = text.withMessage(0, Text.literal(title));

         for (int k = 0; k < 3 && k < lines.length; k++) {
            text = text.withMessage(k + 1, Text.literal(lines[k]));
         }

         sign.setText(text, true);
         sign.setWaxed(true);
         sign.markDirty();
      }
   }

   private static void discChest(ServerWorld world, BlockPos pos) {
      world.setBlockState(pos, (BlockState)Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH));
      if (world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
         chest.setStack(0, new ItemStack(Items.MUSIC_DISC_CAT, 1));
         chest.setStack(1, new ItemStack(Items.MUSIC_DISC_BLOCKS, 1));
         chest.setStack(2, new ItemStack(Items.MUSIC_DISC_CHIRP, 1));
         chest.markDirty();
      }
   }

   private static void chest(ServerWorld world, BlockPos pos) {
      world.setBlockState(pos, Blocks.CHEST.getDefaultState());
      if (world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
         chest.setStack(0, new ItemStack(Items.POPPY, 8));
         chest.setStack(1, new ItemStack(Items.DANDELION, 8));
         chest.setStack(2, new ItemStack(Items.SWEET_BERRIES, 16));
         chest.setStack(3, new ItemStack(Items.CAKE, 2));
         chest.setStack(4, new ItemStack(Items.LEAD, 2));
         chest.markDirty();
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
      boolean contains(BlockPos p) {
         return p.getX() >= this.x0 && p.getX() <= this.x1 && p.getY() >= this.y0 && p.getY() <= this.y1 && p.getZ() >= this.z0 && p.getZ() <= this.z1;
      }
   }
}

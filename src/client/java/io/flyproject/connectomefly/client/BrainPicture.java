package io.flyproject.connectomefly.client;

import io.flyproject.connectomefly.neural.BrainRegions;
import java.util.Random;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * The readout's live brain picture: a schematic fly nervous system seen from the front (two optic lobes, the
 * central brain, the neck, the nerve cord in the body) with dots that light up from the running brain.
 * What is real: every dot's brightness comes from the firing of real neurons in the last fraction of a second.
 * What is not: where the dots sit. The connectome data shipped with the mod has no neuron positions, so the
 * layout is a drawing, and the readout says so.
 */
public final class BrainPicture {
   public static final int W = 236;
   public static final int H = 112;
   // silhouette: ellipses {cx, cy, rx, ry}
   private static final int[][] SHAPES = {{118, 32, 50, 27}, {38, 32, 27, 23}, {198, 32, 27, 23}, {118, 58, 22, 11}, {118, 70, 5, 6}, {118, 92, 34, 19}};
   private static final int[] KIND_COLOR = {0xFFE9C46A, 0xFF6FD3E0, 0xFFF28482};
   private static final int DOTS = 16;
   private static int[] mapX;
   private static int[] mapY;
   private static int[][] dotX;
   private static int[][] dotY;

   private BrainPicture() {
   }

   private static boolean inside(int x, int y) {
      for (int[] s : SHAPES) {
         double dx = (x - s[0]) / (double)s[2];
         double dy = (y - s[1]) / (double)s[3];
         if (dx * dx + dy * dy <= 1.0) {
            return true;
         }
      }

      return false;
   }

   private static void layout(int bins) {
      if (mapX != null && mapX.length == bins) {
         return;
      }

      Random r = new Random(158262L);
      mapX = new int[bins];
      mapY = new int[bins];

      for (int i = 0; i < bins; i++) {
         int x;
         int y;

         do {
            x = 8 + r.nextInt(W - 16);
            y = 4 + r.nextInt(H - 6);
         } while (!inside(x, y));

         mapX[i] = x;
         mapY[i] = y;
      }

      BrainRegions.Region[] rg = BrainRegions.REGIONS;
      dotX = new int[rg.length][DOTS];
      dotY = new int[rg.length][DOTS];

      for (int k = 0; k < rg.length; k++) {
         for (int j = 0; j < DOTS; j++) {
            double a = r.nextDouble() * Math.PI * 2.0;
            double d = Math.sqrt(r.nextDouble()) * rg[k].r();
            dotX[k][j] = rg[k].x() + (int)Math.round(Math.cos(a) * d);
            dotY[k][j] = rg[k].y() + (int)Math.round(Math.sin(a) * d * 0.9);
         }
      }
   }

   private static int scale(int argb, float f) {
      int r = (int)((argb >> 16 & 0xFF) * f);
      int g = (int)((argb >> 8 & 0xFF) * f);
      int b = (int)((argb & 0xFF) * f);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   /** Draws the picture with its top-left at (x, y) and returns the names of the busiest regions, e.g. "left eye · wings". */
   public static String draw(GuiGraphicsExtractor ctx, int x, int y, String map, String regions) {
      layout(map.length());
      // silhouette, two-pixel rows
      for (int row = 0; row < H; row += 2) {
         int start = -1;

         for (int col = 0; col <= W; col += 2) {
            boolean in = col < W && inside(col, row);
            if (in && start < 0) {
               start = col;
            } else if (!in && start >= 0) {
               ctx.fill(x + start, y + row, x + col, y + row + 2, 0xFF0E1820);
               start = -1;
            }
         }
      }

      long tick = System.currentTimeMillis() / 110L;
      // the whole brain: 512 groups of about 309 neurons each, as faint sparks
      for (int i = 0; i < map.length(); i++) {
         int level = map.charAt(i) - 'a';
         if (level > 0) {
            long h = (i * 0x9E3779B97F4A7C15L ^ tick * 0xC2B2AE3D27D4EB4FL) >>> 40;
            if ((int)(h % 16L) < level + 4) {
               float f = 0.5F + 0.5F * Math.min(1.0F, level / 10.0F);
               ctx.fill(x + mapX[i], y + mapY[i], x + mapX[i] + 2, y + mapY[i] + 2, scale(0xFF5AC8D8, f));
            }
         }
      }

      // named regions: more and brighter dots the faster their neurons fire
      BrainRegions.Region[] rg = BrainRegions.REGIONS;
      int best1 = -1;
      int best2 = -1;
      int best3 = -1;

      for (int k = 0; k < rg.length && k < regions.length(); k++) {
         int level = regions.charAt(k) - 'a';
         if (level <= 0) {
            continue;
         }

         if (best1 < 0 || level > regions.charAt(best1) - 'a') {
            best3 = best2;
            best2 = best1;
            best1 = k;
         } else if (best2 < 0 || level > regions.charAt(best2) - 'a') {
            best3 = best2;
            best2 = k;
         } else if (best3 < 0 || level > regions.charAt(best3) - 'a') {
            best3 = k;
         }

         int color = KIND_COLOR[rg[k].kind()];

         for (int j = 0; j < DOTS; j++) {
            long h = ((k * 31L + j) * 0x9E3779B97F4A7C15L ^ tick * 0xD6E8FEB86659FD93L) >>> 40;
            if ((int)(h % 15L) < level) {
               float f = 0.55F + 0.45F * Math.min(1.0F, level / 12.0F);
               ctx.fill(x + dotX[k][j] - 1, y + dotY[k][j] - 1, x + dotX[k][j] + 2, y + dotY[k][j] + 2, scale(color, f));
            }
         }
      }

      StringBuilder top = new StringBuilder();

      for (int k : new int[]{best1, best2, best3}) {
         if (k >= 0) {
            top.append(top.length() > 0 ? " · " : "").append(rg[k].name());
         }
      }

      return top.toString();
   }

   /** The three-colour key under the picture. */
   public static void legend(GuiGraphicsExtractor ctx, Font tr, int x, int y) {
      String[] names = {"senses in", "inner state", "commands out"};
      int cx = x;

      for (int i = 0; i < 3; i++) {
         ctx.fill(cx, y + 2, cx + 4, y + 6, KIND_COLOR[i]);
         ctx.text(tr, names[i], cx + 6, y, -7697248);
         cx += tr.width(names[i]) + 16;
      }
   }
}

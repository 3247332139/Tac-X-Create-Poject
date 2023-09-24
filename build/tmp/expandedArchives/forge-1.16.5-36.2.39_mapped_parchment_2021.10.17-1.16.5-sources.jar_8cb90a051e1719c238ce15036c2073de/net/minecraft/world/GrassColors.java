package net.minecraft.world;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GrassColors {
   private static int[] pixels = new int[65536];

   public static void init(int[] pGrassBuffer) {
      pixels = pGrassBuffer;
   }

   public static int get(double pTemperature, double pHumidity) {
      pHumidity = pHumidity * pTemperature;
      int i = (int)((1.0D - pTemperature) * 255.0D);
      int j = (int)((1.0D - pHumidity) * 255.0D);
      int k = j << 8 | i;
      return k > pixels.length ? -65281 : pixels[k];
   }
}
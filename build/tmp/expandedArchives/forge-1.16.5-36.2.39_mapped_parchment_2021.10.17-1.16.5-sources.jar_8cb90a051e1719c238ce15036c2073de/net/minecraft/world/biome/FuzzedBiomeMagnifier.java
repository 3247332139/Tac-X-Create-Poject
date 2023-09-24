package net.minecraft.world.biome;

import net.minecraft.util.FastRandom;

public enum FuzzedBiomeMagnifier implements IBiomeMagnifier {
   INSTANCE;

   public Biome getBiome(long pSeed, int pX, int pY, int pZ, BiomeManager.IBiomeReader pBiomeReader) {
      int i = pX - 2;
      int j = pY - 2;
      int k = pZ - 2;
      int l = i >> 2;
      int i1 = j >> 2;
      int j1 = k >> 2;
      double d0 = (double)(i & 3) / 4.0D;
      double d1 = (double)(j & 3) / 4.0D;
      double d2 = (double)(k & 3) / 4.0D;
      double[] adouble = new double[8];

      for(int k1 = 0; k1 < 8; ++k1) {
         boolean flag = (k1 & 4) == 0;
         boolean flag1 = (k1 & 2) == 0;
         boolean flag2 = (k1 & 1) == 0;
         int l1 = flag ? l : l + 1;
         int i2 = flag1 ? i1 : i1 + 1;
         int j2 = flag2 ? j1 : j1 + 1;
         double d3 = flag ? d0 : d0 - 1.0D;
         double d4 = flag1 ? d1 : d1 - 1.0D;
         double d5 = flag2 ? d2 : d2 - 1.0D;
         adouble[k1] = getFiddledDistance(pSeed, l1, i2, j2, d3, d4, d5);
      }

      int k2 = 0;
      double d6 = adouble[0];

      for(int l2 = 1; l2 < 8; ++l2) {
         if (d6 > adouble[l2]) {
            k2 = l2;
            d6 = adouble[l2];
         }
      }

      int i3 = (k2 & 4) == 0 ? l : l + 1;
      int j3 = (k2 & 2) == 0 ? i1 : i1 + 1;
      int k3 = (k2 & 1) == 0 ? j1 : j1 + 1;
      return pBiomeReader.getNoiseBiome(i3, j3, k3);
   }

   private static double getFiddledDistance(long pSeed, int pX, int pY, int pZ, double pScaleX, double pScaleY, double pScaleZ) {
      long lvt_11_1_ = FastRandom.next(pSeed, (long)pX);
      lvt_11_1_ = FastRandom.next(lvt_11_1_, (long)pY);
      lvt_11_1_ = FastRandom.next(lvt_11_1_, (long)pZ);
      lvt_11_1_ = FastRandom.next(lvt_11_1_, (long)pX);
      lvt_11_1_ = FastRandom.next(lvt_11_1_, (long)pY);
      lvt_11_1_ = FastRandom.next(lvt_11_1_, (long)pZ);
      double d0 = getFiddle(lvt_11_1_);
      lvt_11_1_ = FastRandom.next(lvt_11_1_, pSeed);
      double d1 = getFiddle(lvt_11_1_);
      lvt_11_1_ = FastRandom.next(lvt_11_1_, pSeed);
      double d2 = getFiddle(lvt_11_1_);
      return sqr(pScaleZ + d2) + sqr(pScaleY + d1) + sqr(pScaleX + d0);
   }

   private static double getFiddle(long pSeed) {
      double d0 = (double)((int)Math.floorMod(pSeed >> 24, 1024L)) / 1024.0D;
      return (d0 - 0.5D) * 0.9D;
   }

   private static double sqr(double pX) {
      return pX * pX;
   }
}
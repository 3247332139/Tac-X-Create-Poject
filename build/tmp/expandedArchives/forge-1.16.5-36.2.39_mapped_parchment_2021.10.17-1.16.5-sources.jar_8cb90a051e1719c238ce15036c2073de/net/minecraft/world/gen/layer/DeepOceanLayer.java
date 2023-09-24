package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.ICastleTransformer;

public enum DeepOceanLayer implements ICastleTransformer {
   INSTANCE;

   public int apply(INoiseRandom pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter) {
      if (LayerUtil.isShallowOcean(pCenter)) {
         int i = 0;
         if (LayerUtil.isShallowOcean(pNorth)) {
            ++i;
         }

         if (LayerUtil.isShallowOcean(pWest)) {
            ++i;
         }

         if (LayerUtil.isShallowOcean(pEast)) {
            ++i;
         }

         if (LayerUtil.isShallowOcean(pSouth)) {
            ++i;
         }

         if (i > 3) {
            if (pCenter == 44) {
               return 47;
            }

            if (pCenter == 45) {
               return 48;
            }

            if (pCenter == 0) {
               return 24;
            }

            if (pCenter == 46) {
               return 49;
            }

            if (pCenter == 10) {
               return 50;
            }

            return 24;
         }
      }

      return pCenter;
   }
}
package net.minecraft.world.gen.layer;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.ICastleTransformer;

public enum ShoreLayer implements ICastleTransformer {
   INSTANCE;

   private static final IntSet SNOWY = new IntOpenHashSet(new int[]{26, 11, 12, 13, 140, 30, 31, 158, 10});
   private static final IntSet JUNGLES = new IntOpenHashSet(new int[]{168, 169, 21, 22, 23, 149, 151});

   public int apply(INoiseRandom pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter) {
      if (pCenter == 14) {
         if (LayerUtil.isShallowOcean(pNorth) || LayerUtil.isShallowOcean(pWest) || LayerUtil.isShallowOcean(pSouth) || LayerUtil.isShallowOcean(pEast)) {
            return 15;
         }
      } else if (JUNGLES.contains(pCenter)) {
         if (!isJungleCompatible(pNorth) || !isJungleCompatible(pWest) || !isJungleCompatible(pSouth) || !isJungleCompatible(pEast)) {
            return 23;
         }

         if (LayerUtil.isOcean(pNorth) || LayerUtil.isOcean(pWest) || LayerUtil.isOcean(pSouth) || LayerUtil.isOcean(pEast)) {
            return 16;
         }
      } else if (pCenter != 3 && pCenter != 34 && pCenter != 20) {
         if (SNOWY.contains(pCenter)) {
            if (!LayerUtil.isOcean(pCenter) && (LayerUtil.isOcean(pNorth) || LayerUtil.isOcean(pWest) || LayerUtil.isOcean(pSouth) || LayerUtil.isOcean(pEast))) {
               return 26;
            }
         } else if (pCenter != 37 && pCenter != 38) {
            if (!LayerUtil.isOcean(pCenter) && pCenter != 7 && pCenter != 6 && (LayerUtil.isOcean(pNorth) || LayerUtil.isOcean(pWest) || LayerUtil.isOcean(pSouth) || LayerUtil.isOcean(pEast))) {
               return 16;
            }
         } else if (!LayerUtil.isOcean(pNorth) && !LayerUtil.isOcean(pWest) && !LayerUtil.isOcean(pSouth) && !LayerUtil.isOcean(pEast) && (!this.isMesa(pNorth) || !this.isMesa(pWest) || !this.isMesa(pSouth) || !this.isMesa(pEast))) {
            return 2;
         }
      } else if (!LayerUtil.isOcean(pCenter) && (LayerUtil.isOcean(pNorth) || LayerUtil.isOcean(pWest) || LayerUtil.isOcean(pSouth) || LayerUtil.isOcean(pEast))) {
         return 25;
      }

      return pCenter;
   }

   private static boolean isJungleCompatible(int p_151631_0_) {
      return JUNGLES.contains(p_151631_0_) || p_151631_0_ == 4 || p_151631_0_ == 5 || LayerUtil.isOcean(p_151631_0_);
   }

   private boolean isMesa(int p_151633_1_) {
      return p_151633_1_ == 37 || p_151633_1_ == 38 || p_151633_1_ == 39 || p_151633_1_ == 165 || p_151633_1_ == 166 || p_151633_1_ == 167;
   }
}
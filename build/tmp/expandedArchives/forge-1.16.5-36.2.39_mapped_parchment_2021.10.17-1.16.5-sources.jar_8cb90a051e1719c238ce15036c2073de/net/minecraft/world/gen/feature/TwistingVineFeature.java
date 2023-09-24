package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.AbstractTopPlantBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;

public class TwistingVineFeature extends Feature<NoFeatureConfig> {
   public TwistingVineFeature(Codec<NoFeatureConfig> p_i232000_1_) {
      super(p_i232000_1_);
   }

   public boolean place(ISeedReader p_241855_1_, ChunkGenerator p_241855_2_, Random p_241855_3_, BlockPos p_241855_4_, NoFeatureConfig p_241855_5_) {
      return place(p_241855_1_, p_241855_3_, p_241855_4_, 8, 4, 8);
   }

   public static boolean place(IWorld pLevel, Random pRandom, BlockPos pPos, int pVerticalOffset, int pHorizontalOffset, int pHeight) {
      if (isInvalidPlacementLocation(pLevel, pPos)) {
         return false;
      } else {
         placeTwistingVines(pLevel, pRandom, pPos, pVerticalOffset, pHorizontalOffset, pHeight);
         return true;
      }
   }

   private static void placeTwistingVines(IWorld pLevel, Random pRandom, BlockPos pPos, int pVerticalOffset, int pHorizontalOffset, int pHeight) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int i = 0; i < pVerticalOffset * pVerticalOffset; ++i) {
         blockpos$mutable.set(pPos).move(MathHelper.nextInt(pRandom, -pVerticalOffset, pVerticalOffset), MathHelper.nextInt(pRandom, -pHorizontalOffset, pHorizontalOffset), MathHelper.nextInt(pRandom, -pVerticalOffset, pVerticalOffset));
         if (findFirstAirBlockAboveGround(pLevel, blockpos$mutable) && !isInvalidPlacementLocation(pLevel, blockpos$mutable)) {
            int j = MathHelper.nextInt(pRandom, 1, pHeight);
            if (pRandom.nextInt(6) == 0) {
               j *= 2;
            }

            if (pRandom.nextInt(5) == 0) {
               j = 1;
            }

            int k = 17;
            int l = 25;
            placeWeepingVinesColumn(pLevel, pRandom, blockpos$mutable, j, 17, 25);
         }
      }

   }

   private static boolean findFirstAirBlockAboveGround(IWorld pLevel, BlockPos.Mutable pPos) {
      do {
         pPos.move(0, -1, 0);
         if (World.isOutsideBuildHeight(pPos)) {
            return false;
         }
      } while(pLevel.getBlockState(pPos).isAir());

      pPos.move(0, 1, 0);
      return true;
   }

   public static void placeWeepingVinesColumn(IWorld pLevel, Random pRandom, BlockPos.Mutable pPos, int p_236422_3_, int pMinAge, int pMaxAge) {
      for(int i = 1; i <= p_236422_3_; ++i) {
         if (pLevel.isEmptyBlock(pPos)) {
            if (i == p_236422_3_ || !pLevel.isEmptyBlock(pPos.above())) {
               pLevel.setBlock(pPos, Blocks.TWISTING_VINES.defaultBlockState().setValue(AbstractTopPlantBlock.AGE, Integer.valueOf(MathHelper.nextInt(pRandom, pMinAge, pMaxAge))), 2);
               break;
            }

            pLevel.setBlock(pPos, Blocks.TWISTING_VINES_PLANT.defaultBlockState(), 2);
         }

         pPos.move(Direction.UP);
      }

   }

   private static boolean isInvalidPlacementLocation(IWorld pLevel, BlockPos pPos) {
      if (!pLevel.isEmptyBlock(pPos)) {
         return true;
      } else {
         BlockState blockstate = pLevel.getBlockState(pPos.below());
         return !blockstate.is(Blocks.NETHERRACK) && !blockstate.is(Blocks.WARPED_NYLIUM) && !blockstate.is(Blocks.WARPED_WART_BLOCK);
      }
   }
}
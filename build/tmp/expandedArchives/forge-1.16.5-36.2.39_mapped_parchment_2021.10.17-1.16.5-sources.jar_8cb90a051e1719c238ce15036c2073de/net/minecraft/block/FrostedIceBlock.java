package net.minecraft.block;

import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class FrostedIceBlock extends IceBlock {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

   public FrostedIceBlock(AbstractBlock.Properties p_i48394_1_) {
      super(p_i48394_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      this.tick(pState, pLevel, pPos, pRandom);
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if ((pRand.nextInt(3) == 0 || this.fewerNeigboursThan(pLevel, pPos, 4)) && pLevel.getMaxLocalRawBrightness(pPos) > 11 - pState.getValue(AGE) - pState.getLightBlock(pLevel, pPos) && this.slightlyMelt(pState, pLevel, pPos)) {
         BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

         for(Direction direction : Direction.values()) {
            blockpos$mutable.setWithOffset(pPos, direction);
            BlockState blockstate = pLevel.getBlockState(blockpos$mutable);
            if (blockstate.is(this) && !this.slightlyMelt(blockstate, pLevel, blockpos$mutable)) {
               pLevel.getBlockTicks().scheduleTick(blockpos$mutable, this, MathHelper.nextInt(pRand, 20, 40));
            }
         }

      } else {
         pLevel.getBlockTicks().scheduleTick(pPos, this, MathHelper.nextInt(pRand, 20, 40));
      }
   }

   private boolean slightlyMelt(BlockState pState, World pLevel, BlockPos pPos) {
      int i = pState.getValue(AGE);
      if (i < 3) {
         pLevel.setBlock(pPos, pState.setValue(AGE, Integer.valueOf(i + 1)), 2);
         return false;
      } else {
         this.melt(pState, pLevel, pPos);
         return true;
      }
   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (pBlock == this && this.fewerNeigboursThan(pLevel, pPos, 2)) {
         this.melt(pState, pLevel, pPos);
      }

      super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
   }

   private boolean fewerNeigboursThan(IBlockReader pLevel, BlockPos pPos, int pNeighborsRequired) {
      int i = 0;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(Direction direction : Direction.values()) {
         blockpos$mutable.setWithOffset(pPos, direction);
         if (pLevel.getBlockState(blockpos$mutable).is(this)) {
            ++i;
            if (i >= pNeighborsRequired) {
               return false;
            }
         }
      }

      return true;
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE);
   }

   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      return ItemStack.EMPTY;
   }
}
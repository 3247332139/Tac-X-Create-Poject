package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FallingBlock extends Block {
   public FallingBlock(AbstractBlock.Properties p_i48401_1_) {
      super(p_i48401_1_);
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      pLevel.getBlockTicks().scheduleTick(pPos, this, this.getDelayAfterPlace());
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, this.getDelayAfterPlace());
      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if (pLevel.isEmptyBlock(pPos.below()) || isFree(pLevel.getBlockState(pPos.below())) && pPos.getY() >= 0) {
         FallingBlockEntity fallingblockentity = new FallingBlockEntity(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D, pLevel.getBlockState(pPos));
         this.falling(fallingblockentity);
         pLevel.addFreshEntity(fallingblockentity);
      }
   }

   protected void falling(FallingBlockEntity pEntity) {
   }

   protected int getDelayAfterPlace() {
      return 2;
   }

   public static boolean isFree(BlockState pState) {
      Material material = pState.getMaterial();
      return pState.isAir() || pState.is(BlockTags.FIRE) || material.isLiquid() || material.isReplaceable();
   }

   public void onLand(World p_176502_1_, BlockPos p_176502_2_, BlockState p_176502_3_, BlockState p_176502_4_, FallingBlockEntity p_176502_5_) {
   }

   public void onBroken(World p_190974_1_, BlockPos p_190974_2_, FallingBlockEntity p_190974_3_) {
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pRand.nextInt(16) == 0) {
         BlockPos blockpos = pPos.below();
         if (pLevel.isEmptyBlock(blockpos) || isFree(pLevel.getBlockState(blockpos))) {
            double d0 = (double)pPos.getX() + pRand.nextDouble();
            double d1 = (double)pPos.getY() - 0.05D;
            double d2 = (double)pPos.getZ() + pRand.nextDouble();
            pLevel.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, pState), d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public int getDustColor(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return -16777216;
   }
}

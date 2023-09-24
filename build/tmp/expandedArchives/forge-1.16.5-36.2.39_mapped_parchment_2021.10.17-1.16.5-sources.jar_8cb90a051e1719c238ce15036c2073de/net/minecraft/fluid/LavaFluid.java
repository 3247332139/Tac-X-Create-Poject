package net.minecraft.fluid;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class LavaFluid extends FlowingFluid {
   public Fluid getFlowing() {
      return Fluids.FLOWING_LAVA;
   }

   public Fluid getSource() {
      return Fluids.LAVA;
   }

   public Item getBucket() {
      return Items.LAVA_BUCKET;
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(World pLevel, BlockPos pPos, FluidState pState, Random pRandom) {
      BlockPos blockpos = pPos.above();
      if (pLevel.getBlockState(blockpos).isAir() && !pLevel.getBlockState(blockpos).isSolidRender(pLevel, blockpos)) {
         if (pRandom.nextInt(100) == 0) {
            double d0 = (double)pPos.getX() + pRandom.nextDouble();
            double d1 = (double)pPos.getY() + 1.0D;
            double d2 = (double)pPos.getZ() + pRandom.nextDouble();
            pLevel.addParticle(ParticleTypes.LAVA, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            pLevel.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundCategory.BLOCKS, 0.2F + pRandom.nextFloat() * 0.2F, 0.9F + pRandom.nextFloat() * 0.15F, false);
         }

         if (pRandom.nextInt(200) == 0) {
            pLevel.playLocalSound((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), SoundEvents.LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + pRandom.nextFloat() * 0.2F, 0.9F + pRandom.nextFloat() * 0.15F, false);
         }
      }

   }

   public void randomTick(World pLevel, BlockPos pPos, FluidState pState, Random pRandom) {
      if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
         int i = pRandom.nextInt(3);
         if (i > 0) {
            BlockPos blockpos = pPos;

            for(int j = 0; j < i; ++j) {
               blockpos = blockpos.offset(pRandom.nextInt(3) - 1, 1, pRandom.nextInt(3) - 1);
               if (!pLevel.isLoaded(blockpos)) {
                  return;
               }

               BlockState blockstate = pLevel.getBlockState(blockpos);
               if (blockstate.isAir()) {
                  if (this.hasFlammableNeighbours(pLevel, blockpos)) {
                     pLevel.setBlockAndUpdate(blockpos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, blockpos, pPos, Blocks.FIRE.defaultBlockState()));
                     return;
                  }
               } else if (blockstate.getMaterial().blocksMotion()) {
                  return;
               }
            }
         } else {
            for(int k = 0; k < 3; ++k) {
               BlockPos blockpos1 = pPos.offset(pRandom.nextInt(3) - 1, 0, pRandom.nextInt(3) - 1);
               if (!pLevel.isLoaded(blockpos1)) {
                  return;
               }

               if (pLevel.isEmptyBlock(blockpos1.above()) && this.isFlammable(pLevel, blockpos1, Direction.UP)) {
                  pLevel.setBlockAndUpdate(blockpos1.above(), net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, blockpos1.above(), pPos, Blocks.FIRE.defaultBlockState()));
               }
            }
         }

      }
   }

   private boolean hasFlammableNeighbours(IWorldReader pLevel, BlockPos pPos) {
      for(Direction direction : Direction.values()) {
         if (this.isFlammable(pLevel, pPos.relative(direction), direction.getOpposite())) {
            return true;
         }
      }

      return false;
   }

   /** @deprecated Forge: use {@link LavaFluid#isFlammable(IWorldReader,BlockPos,Direction)} instead */
   @Deprecated
   private boolean isFlammable(IWorldReader pLevel, BlockPos pPos) {
      return pPos.getY() >= 0 && pPos.getY() < 256 && !pLevel.hasChunkAt(pPos) ? false : pLevel.getBlockState(pPos).getMaterial().isFlammable();
   }

   private boolean isFlammable(IWorldReader world, BlockPos pos, Direction face) {
      return pos.getY() >= 0 && pos.getY() < 256 && !world.hasChunkAt(pos) ? false : world.getBlockState(pos).isFlammable(world, pos, face);
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public IParticleData getDripParticle() {
      return ParticleTypes.DRIPPING_LAVA;
   }

   protected void beforeDestroyingBlock(IWorld pLevel, BlockPos pPos, BlockState pState) {
      this.fizz(pLevel, pPos);
   }

   public int getSlopeFindDistance(IWorldReader pLevel) {
      return pLevel.dimensionType().ultraWarm() ? 4 : 2;
   }

   public BlockState createLegacyBlock(FluidState pState) {
      return Blocks.LAVA.defaultBlockState().setValue(FlowingFluidBlock.LEVEL, Integer.valueOf(getLegacyLevel(pState)));
   }

   public boolean isSame(Fluid pFluid) {
      return pFluid == Fluids.LAVA || pFluid == Fluids.FLOWING_LAVA;
   }

   public int getDropOff(IWorldReader pLevel) {
      return pLevel.dimensionType().ultraWarm() ? 1 : 2;
   }

   public boolean canBeReplacedWith(FluidState pFluidState, IBlockReader pBlockReader, BlockPos pPos, Fluid pFluid, Direction pDirection) {
      return pFluidState.getHeight(pBlockReader, pPos) >= 0.44444445F && pFluid.is(FluidTags.WATER);
   }

   public int getTickDelay(IWorldReader p_205569_1_) {
      return p_205569_1_.dimensionType().ultraWarm() ? 10 : 30;
   }

   public int getSpreadDelay(World p_215667_1_, BlockPos p_215667_2_, FluidState p_215667_3_, FluidState p_215667_4_) {
      int i = this.getTickDelay(p_215667_1_);
      if (!p_215667_3_.isEmpty() && !p_215667_4_.isEmpty() && !p_215667_3_.getValue(FALLING) && !p_215667_4_.getValue(FALLING) && p_215667_4_.getHeight(p_215667_1_, p_215667_2_) > p_215667_3_.getHeight(p_215667_1_, p_215667_2_) && p_215667_1_.getRandom().nextInt(4) != 0) {
         i *= 4;
      }

      return i;
   }

   private void fizz(IWorld pLevel, BlockPos pPos) {
      pLevel.levelEvent(1501, pPos, 0);
   }

   protected boolean canConvertToSource() {
      return false;
   }

   protected void spreadTo(IWorld pLevel, BlockPos pPos, BlockState pBlockState, Direction pDirection, FluidState pFluidState) {
      if (pDirection == Direction.DOWN) {
         FluidState fluidstate = pLevel.getFluidState(pPos);
         if (this.is(FluidTags.LAVA) && fluidstate.is(FluidTags.WATER)) {
            if (pBlockState.getBlock() instanceof FlowingFluidBlock) {
               pLevel.setBlock(pPos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, pPos, pPos, Blocks.STONE.defaultBlockState()), 3);
            }

            this.fizz(pLevel, pPos);
            return;
         }
      }

      super.spreadTo(pLevel, pPos, pBlockState, pDirection, pFluidState);
   }

   protected boolean isRandomlyTicking() {
      return true;
   }

   protected float getExplosionResistance() {
      return 100.0F;
   }

   public static class Flowing extends LavaFluid {
      protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> pBuilder) {
         super.createFluidStateDefinition(pBuilder);
         pBuilder.add(LEVEL);
      }

      public int getAmount(FluidState pState) {
         return pState.getValue(LEVEL);
      }

      public boolean isSource(FluidState pState) {
         return false;
      }
   }

   public static class Source extends LavaFluid {
      public int getAmount(FluidState pState) {
         return 8;
      }

      public boolean isSource(FluidState pState) {
         return true;
      }
   }
}

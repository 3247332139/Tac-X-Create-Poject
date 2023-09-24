package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public final class Blockreader implements IBlockReader {
   private final BlockState[] column;

   public Blockreader(BlockState[] p_i231623_1_) {
      this.column = p_i231623_1_;
   }

   @Nullable
   public TileEntity getBlockEntity(BlockPos pPos) {
      return null;
   }

   public BlockState getBlockState(BlockPos pPos) {
      int i = pPos.getY();
      return i >= 0 && i < this.column.length ? this.column[i] : Blocks.AIR.defaultBlockState();
   }

   public FluidState getFluidState(BlockPos pPos) {
      return this.getBlockState(pPos).getFluidState();
   }
}
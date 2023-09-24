package net.minecraft.util;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class CachedBlockInfo {
   private final IWorldReader level;
   private final BlockPos pos;
   private final boolean loadChunks;
   private BlockState state;
   private TileEntity entity;
   private boolean cachedEntity;

   public CachedBlockInfo(IWorldReader pLevel, BlockPos pPos, boolean pLoadChunks) {
      this.level = pLevel;
      this.pos = pPos.immutable();
      this.loadChunks = pLoadChunks;
   }

   /**
    * Gets the block state as currently held, or (if it has not gotten it from the level) loads it from the level.
    * This will only look up the state from the world if {@link #loadChunks} is true or the block position is loaded.
    */
   public BlockState getState() {
      if (this.state == null && (this.loadChunks || this.level.hasChunkAt(this.pos))) {
         this.state = this.level.getBlockState(this.pos);
      }

      return this.state;
   }

   /**
    * Gets the BlockEntity as currently held, or (if it has not gotten it from the level) loads it from the level.
    */
   @Nullable
   public TileEntity getEntity() {
      if (this.entity == null && !this.cachedEntity) {
         this.entity = this.level.getBlockEntity(this.pos);
         this.cachedEntity = true;
      }

      return this.entity;
   }

   public IWorldReader getLevel() {
      return this.level;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   /**
    * Creates a new {@link Predicate} that will match when the given {@link IBlockState} predicate matches.
    */
   public static Predicate<CachedBlockInfo> hasState(Predicate<BlockState> pPredicates) {
      return (p_201002_1_) -> {
         return p_201002_1_ != null && pPredicates.test(p_201002_1_.getState());
      };
   }
}
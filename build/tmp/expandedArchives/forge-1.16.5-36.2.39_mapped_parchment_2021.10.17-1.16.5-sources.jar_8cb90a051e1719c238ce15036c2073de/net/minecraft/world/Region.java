package net.minecraft.world;

import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunk;

public class Region implements IBlockReader, ICollisionReader {
   protected final int centerX;
   protected final int centerZ;
   protected final IChunk[][] chunks;
   protected boolean allEmpty;
   protected final World level;

   public Region(World pLevel, BlockPos pCenterPos, BlockPos pOffsetPos) {
      this.level = pLevel;
      this.centerX = pCenterPos.getX() >> 4;
      this.centerZ = pCenterPos.getZ() >> 4;
      int i = pOffsetPos.getX() >> 4;
      int j = pOffsetPos.getZ() >> 4;
      this.chunks = new IChunk[i - this.centerX + 1][j - this.centerZ + 1];
      AbstractChunkProvider abstractchunkprovider = pLevel.getChunkSource();
      this.allEmpty = true;

      for(int k = this.centerX; k <= i; ++k) {
         for(int l = this.centerZ; l <= j; ++l) {
            this.chunks[k - this.centerX][l - this.centerZ] = abstractchunkprovider.getChunkNow(k, l);
         }
      }

      for(int i1 = pCenterPos.getX() >> 4; i1 <= pOffsetPos.getX() >> 4; ++i1) {
         for(int j1 = pCenterPos.getZ() >> 4; j1 <= pOffsetPos.getZ() >> 4; ++j1) {
            IChunk ichunk = this.chunks[i1 - this.centerX][j1 - this.centerZ];
            if (ichunk != null && !ichunk.isYSpaceEmpty(pCenterPos.getY(), pOffsetPos.getY())) {
               this.allEmpty = false;
               return;
            }
         }
      }

   }

   private IChunk getChunk(BlockPos pPos) {
      return this.getChunk(pPos.getX() >> 4, pPos.getZ() >> 4);
   }

   private IChunk getChunk(int pX, int pZ) {
      int i = pX - this.centerX;
      int j = pZ - this.centerZ;
      if (i >= 0 && i < this.chunks.length && j >= 0 && j < this.chunks[i].length) {
         IChunk ichunk = this.chunks[i][j];
         return (IChunk)(ichunk != null ? ichunk : new EmptyChunk(this.level, new ChunkPos(pX, pZ)));
      } else {
         return new EmptyChunk(this.level, new ChunkPos(pX, pZ));
      }
   }

   public WorldBorder getWorldBorder() {
      return this.level.getWorldBorder();
   }

   public IBlockReader getChunkForCollisions(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ);
   }

   @Nullable
   public TileEntity getBlockEntity(BlockPos pPos) {
      IChunk ichunk = this.getChunk(pPos);
      return ichunk.getBlockEntity(pPos);
   }

   public BlockState getBlockState(BlockPos pPos) {
      if (World.isOutsideBuildHeight(pPos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         IChunk ichunk = this.getChunk(pPos);
         return ichunk.getBlockState(pPos);
      }
   }

   public Stream<VoxelShape> getEntityCollisions(@Nullable Entity pEntity, AxisAlignedBB pArea, Predicate<Entity> pFilter) {
      return Stream.empty();
   }

   public Stream<VoxelShape> getCollisions(@Nullable Entity pEntity, AxisAlignedBB pCollisionBox, Predicate<Entity> pFilter) {
      return this.getBlockCollisions(pEntity, pCollisionBox);
   }

   public FluidState getFluidState(BlockPos pPos) {
      if (World.isOutsideBuildHeight(pPos)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         IChunk ichunk = this.getChunk(pPos);
         return ichunk.getFluidState(pPos);
      }
   }
}
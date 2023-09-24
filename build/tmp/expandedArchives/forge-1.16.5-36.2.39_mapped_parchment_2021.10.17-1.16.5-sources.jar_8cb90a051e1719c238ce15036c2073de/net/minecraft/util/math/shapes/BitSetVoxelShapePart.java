package net.minecraft.util.math.shapes;

import java.util.BitSet;
import net.minecraft.util.Direction;

public final class BitSetVoxelShapePart extends VoxelShapePart {
   private final BitSet storage;
   private int xMin;
   private int yMin;
   private int zMin;
   private int xMax;
   private int yMax;
   private int zMax;

   public BitSetVoxelShapePart(int p_i47690_1_, int p_i47690_2_, int p_i47690_3_) {
      this(p_i47690_1_, p_i47690_2_, p_i47690_3_, p_i47690_1_, p_i47690_2_, p_i47690_3_, 0, 0, 0);
   }

   public BitSetVoxelShapePart(int p_i48183_1_, int p_i48183_2_, int p_i48183_3_, int p_i48183_4_, int p_i48183_5_, int p_i48183_6_, int p_i48183_7_, int p_i48183_8_, int p_i48183_9_) {
      super(p_i48183_1_, p_i48183_2_, p_i48183_3_);
      this.storage = new BitSet(p_i48183_1_ * p_i48183_2_ * p_i48183_3_);
      this.xMin = p_i48183_4_;
      this.yMin = p_i48183_5_;
      this.zMin = p_i48183_6_;
      this.xMax = p_i48183_7_;
      this.yMax = p_i48183_8_;
      this.zMax = p_i48183_9_;
   }

   public BitSetVoxelShapePart(VoxelShapePart pShape) {
      super(pShape.xSize, pShape.ySize, pShape.zSize);
      if (pShape instanceof BitSetVoxelShapePart) {
         this.storage = (BitSet)((BitSetVoxelShapePart)pShape).storage.clone();
      } else {
         this.storage = new BitSet(this.xSize * this.ySize * this.zSize);

         for(int i = 0; i < this.xSize; ++i) {
            for(int j = 0; j < this.ySize; ++j) {
               for(int k = 0; k < this.zSize; ++k) {
                  if (pShape.isFull(i, j, k)) {
                     this.storage.set(this.getIndex(i, j, k));
                  }
               }
            }
         }
      }

      this.xMin = pShape.firstFull(Direction.Axis.X);
      this.yMin = pShape.firstFull(Direction.Axis.Y);
      this.zMin = pShape.firstFull(Direction.Axis.Z);
      this.xMax = pShape.lastFull(Direction.Axis.X);
      this.yMax = pShape.lastFull(Direction.Axis.Y);
      this.zMax = pShape.lastFull(Direction.Axis.Z);
   }

   protected int getIndex(int pX, int pY, int pZ) {
      return (pX * this.ySize + pY) * this.zSize + pZ;
   }

   public boolean isFull(int pX, int pY, int pZ) {
      return this.storage.get(this.getIndex(pX, pY, pZ));
   }

   public void setFull(int p_199625_1_, int p_199625_2_, int p_199625_3_, boolean p_199625_4_, boolean p_199625_5_) {
      this.storage.set(this.getIndex(p_199625_1_, p_199625_2_, p_199625_3_), p_199625_5_);
      if (p_199625_4_ && p_199625_5_) {
         this.xMin = Math.min(this.xMin, p_199625_1_);
         this.yMin = Math.min(this.yMin, p_199625_2_);
         this.zMin = Math.min(this.zMin, p_199625_3_);
         this.xMax = Math.max(this.xMax, p_199625_1_ + 1);
         this.yMax = Math.max(this.yMax, p_199625_2_ + 1);
         this.zMax = Math.max(this.zMax, p_199625_3_ + 1);
      }

   }

   public boolean isEmpty() {
      return this.storage.isEmpty();
   }

   public int firstFull(Direction.Axis pAxis) {
      return pAxis.choose(this.xMin, this.yMin, this.zMin);
   }

   public int lastFull(Direction.Axis pAxis) {
      return pAxis.choose(this.xMax, this.yMax, this.zMax);
   }

   protected boolean isZStripFull(int p_197833_1_, int p_197833_2_, int p_197833_3_, int p_197833_4_) {
      if (p_197833_3_ >= 0 && p_197833_4_ >= 0 && p_197833_1_ >= 0) {
         if (p_197833_3_ < this.xSize && p_197833_4_ < this.ySize && p_197833_2_ <= this.zSize) {
            return this.storage.nextClearBit(this.getIndex(p_197833_3_, p_197833_4_, p_197833_1_)) >= this.getIndex(p_197833_3_, p_197833_4_, p_197833_2_);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected void setZStrip(int p_197834_1_, int p_197834_2_, int p_197834_3_, int p_197834_4_, boolean p_197834_5_) {
      this.storage.set(this.getIndex(p_197834_3_, p_197834_4_, p_197834_1_), this.getIndex(p_197834_3_, p_197834_4_, p_197834_2_), p_197834_5_);
   }

   static BitSetVoxelShapePart join(VoxelShapePart pMainShape, VoxelShapePart pSecondaryShape, IDoubleListMerger pMergerX, IDoubleListMerger pMergerY, IDoubleListMerger pMergerZ, IBooleanFunction pOperator) {
      BitSetVoxelShapePart bitsetvoxelshapepart = new BitSetVoxelShapePart(pMergerX.getList().size() - 1, pMergerY.getList().size() - 1, pMergerZ.getList().size() - 1);
      int[] aint = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
      pMergerX.forMergedIndexes((p_199628_7_, p_199628_8_, p_199628_9_) -> {
         boolean[] aboolean = new boolean[]{false};
         boolean flag = pMergerY.forMergedIndexes((p_199627_10_, p_199627_11_, p_199627_12_) -> {
            boolean[] aboolean1 = new boolean[]{false};
            boolean flag1 = pMergerZ.forMergedIndexes((p_199629_12_, p_199629_13_, p_199629_14_) -> {
               boolean flag2 = pOperator.apply(pMainShape.isFullWide(p_199628_7_, p_199627_10_, p_199629_12_), pSecondaryShape.isFullWide(p_199628_8_, p_199627_11_, p_199629_13_));
               if (flag2) {
                  bitsetvoxelshapepart.storage.set(bitsetvoxelshapepart.getIndex(p_199628_9_, p_199627_12_, p_199629_14_));
                  aint[2] = Math.min(aint[2], p_199629_14_);
                  aint[5] = Math.max(aint[5], p_199629_14_);
                  aboolean1[0] = true;
               }

               return true;
            });
            if (aboolean1[0]) {
               aint[1] = Math.min(aint[1], p_199627_12_);
               aint[4] = Math.max(aint[4], p_199627_12_);
               aboolean[0] = true;
            }

            return flag1;
         });
         if (aboolean[0]) {
            aint[0] = Math.min(aint[0], p_199628_9_);
            aint[3] = Math.max(aint[3], p_199628_9_);
         }

         return flag;
      });
      bitsetvoxelshapepart.xMin = aint[0];
      bitsetvoxelshapepart.yMin = aint[1];
      bitsetvoxelshapepart.zMin = aint[2];
      bitsetvoxelshapepart.xMax = aint[3] + 1;
      bitsetvoxelshapepart.yMax = aint[4] + 1;
      bitsetvoxelshapepart.zMax = aint[5] + 1;
      return bitsetvoxelshapepart;
   }
}
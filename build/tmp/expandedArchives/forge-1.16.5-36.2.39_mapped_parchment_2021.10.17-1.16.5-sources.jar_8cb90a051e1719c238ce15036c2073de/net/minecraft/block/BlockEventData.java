package net.minecraft.block;

import net.minecraft.util.math.BlockPos;

public class BlockEventData {
   private final BlockPos pos;
   private final Block block;
   /** Different for each blockID */
   private final int paramA;
   private final int paramB;

   public BlockEventData(BlockPos pPos, Block pBlock, int pParamA, int pParamB) {
      this.pos = pPos;
      this.block = pBlock;
      this.paramA = pParamA;
      this.paramB = pParamB;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Block getBlock() {
      return this.block;
   }

   /**
    * Get the Event ID (different for each BlockID)
    */
   public int getParamA() {
      return this.paramA;
   }

   public int getParamB() {
      return this.paramB;
   }

   public boolean equals(Object p_equals_1_) {
      if (!(p_equals_1_ instanceof BlockEventData)) {
         return false;
      } else {
         BlockEventData blockeventdata = (BlockEventData)p_equals_1_;
         return this.pos.equals(blockeventdata.pos) && this.paramA == blockeventdata.paramA && this.paramB == blockeventdata.paramB && this.block == blockeventdata.block;
      }
   }

   public int hashCode() {
      int i = this.pos.hashCode();
      i = 31 * i + this.block.hashCode();
      i = 31 * i + this.paramA;
      return 31 * i + this.paramB;
   }

   public String toString() {
      return "TE(" + this.pos + ")," + this.paramA + "," + this.paramB + "," + this.block;
   }
}
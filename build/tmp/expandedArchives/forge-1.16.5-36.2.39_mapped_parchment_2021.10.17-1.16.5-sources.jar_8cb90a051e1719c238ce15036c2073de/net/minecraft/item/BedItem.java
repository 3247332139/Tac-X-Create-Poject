package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class BedItem extends BlockItem {
   public BedItem(Block p_i48528_1_, Item.Properties p_i48528_2_) {
      super(p_i48528_1_, p_i48528_2_);
   }

   protected boolean placeBlock(BlockItemUseContext pContext, BlockState pState) {
      return pContext.getLevel().setBlock(pContext.getClickedPos(), pState, 26);
   }
}
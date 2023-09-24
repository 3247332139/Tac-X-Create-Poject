package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public interface ISidedInventory extends IInventory {
   int[] getSlotsForFace(Direction pSide);

   /**
    * Returns true if automation can insert the given item in the given slot from the given side.
    */
   boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection);

   /**
    * Returns true if automation can extract the given item in the given slot from the given side.
    */
   boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection);
}
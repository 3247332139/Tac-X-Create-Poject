package net.minecraft.inventory.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.util.IIntArray;

public class FurnaceContainer extends AbstractFurnaceContainer {
   public FurnaceContainer(int pContainerId, PlayerInventory pPlayerInventory) {
      super(ContainerType.FURNACE, IRecipeType.SMELTING, RecipeBookCategory.FURNACE, pContainerId, pPlayerInventory);
   }

   public FurnaceContainer(int pContainerId, PlayerInventory pPlayerInventory, IInventory pFurnaceContainer, IIntArray pFurnaceData) {
      super(ContainerType.FURNACE, IRecipeType.SMELTING, RecipeBookCategory.FURNACE, pContainerId, pPlayerInventory, pFurnaceContainer, pFurnaceData);
   }
}
package net.minecraft.inventory.container;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.ServerRecipePlacer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class RecipeBookContainer<C extends IInventory> extends Container {
   public RecipeBookContainer(ContainerType<?> p_i50067_1_, int p_i50067_2_) {
      super(p_i50067_1_, p_i50067_2_);
   }

   public void handlePlacement(boolean pPlaceAll, IRecipe<?> pRecipe, ServerPlayerEntity pPlayer) {
      (new ServerRecipePlacer<>(this)).recipeClicked(pPlayer, (IRecipe<C>)pRecipe, pPlaceAll);
   }

   public abstract void fillCraftSlotsStackedContents(RecipeItemHelper pItemHelper);

   public abstract void clearCraftingContent();

   public abstract boolean recipeMatches(IRecipe<? super C> pRecipe);

   public abstract int getResultSlotIndex();

   public abstract int getGridWidth();

   public abstract int getGridHeight();

   @OnlyIn(Dist.CLIENT)
   public abstract int getSize();

   public java.util.List<net.minecraft.client.util.RecipeBookCategories> getRecipeBookCategories() {
      return net.minecraft.client.util.RecipeBookCategories.getCategories(this.getRecipeBookType());
   }

   @OnlyIn(Dist.CLIENT)
   public abstract RecipeBookCategory getRecipeBookType();
}

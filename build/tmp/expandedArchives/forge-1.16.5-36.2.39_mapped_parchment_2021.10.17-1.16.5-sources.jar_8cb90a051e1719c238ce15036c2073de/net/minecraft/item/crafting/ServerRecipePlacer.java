package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlaceGhostRecipePacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerRecipePlacer<C extends IInventory> implements IRecipePlacer<Integer> {
   protected static final Logger LOGGER = LogManager.getLogger();
   protected final RecipeItemHelper stackedContents = new RecipeItemHelper();
   protected PlayerInventory inventory;
   protected RecipeBookContainer<C> menu;

   public ServerRecipePlacer(RecipeBookContainer<C> p_i50752_1_) {
      this.menu = p_i50752_1_;
   }

   public void recipeClicked(ServerPlayerEntity pPlayer, @Nullable IRecipe<C> pRecipe, boolean pPlaceAll) {
      if (pRecipe != null && pPlayer.getRecipeBook().contains(pRecipe)) {
         this.inventory = pPlayer.inventory;
         if (this.testClearGrid() || pPlayer.isCreative()) {
            this.stackedContents.clear();
            pPlayer.inventory.fillStackedContents(this.stackedContents);
            this.menu.fillCraftSlotsStackedContents(this.stackedContents);
            if (this.stackedContents.canCraft(pRecipe, (IntList)null)) {
               this.handleRecipeClicked(pRecipe, pPlaceAll);
            } else {
               this.clearGrid();
               pPlayer.connection.send(new SPlaceGhostRecipePacket(pPlayer.containerMenu.containerId, pRecipe));
            }

            pPlayer.inventory.setChanged();
         }
      }
   }

   protected void clearGrid() {
      for(int i = 0; i < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++i) {
         if (i != this.menu.getResultSlotIndex() || !(this.menu instanceof WorkbenchContainer) && !(this.menu instanceof PlayerContainer)) {
            this.moveItemToInventory(i);
         }
      }

      this.menu.clearCraftingContent();
   }

   protected void moveItemToInventory(int p_201510_1_) {
      ItemStack itemstack = this.menu.getSlot(p_201510_1_).getItem();
      if (!itemstack.isEmpty()) {
         for(; itemstack.getCount() > 0; this.menu.getSlot(p_201510_1_).remove(1)) {
            int i = this.inventory.getSlotWithRemainingSpace(itemstack);
            if (i == -1) {
               i = this.inventory.getFreeSlot();
            }

            ItemStack itemstack1 = itemstack.copy();
            itemstack1.setCount(1);
            if (!this.inventory.add(i, itemstack1)) {
               LOGGER.error("Can't find any space for item in the inventory");
            }
         }

      }
   }

   protected void handleRecipeClicked(IRecipe<C> pRecipe, boolean pPlaceAll) {
      boolean flag = this.menu.recipeMatches(pRecipe);
      int i = this.stackedContents.getBiggestCraftableStack(pRecipe, (IntList)null);
      if (flag) {
         for(int j = 0; j < this.menu.getGridHeight() * this.menu.getGridWidth() + 1; ++j) {
            if (j != this.menu.getResultSlotIndex()) {
               ItemStack itemstack = this.menu.getSlot(j).getItem();
               if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
                  return;
               }
            }
         }
      }

      int j1 = this.getStackSize(pPlaceAll, i, flag);
      IntList intlist = new IntArrayList();
      if (this.stackedContents.canCraft(pRecipe, intlist, j1)) {
         int k = j1;

         for(int l : intlist) {
            int i1 = RecipeItemHelper.fromStackingIndex(l).getMaxStackSize();
            if (i1 < k) {
               k = i1;
            }
         }

         if (this.stackedContents.canCraft(pRecipe, intlist, k)) {
            this.clearGrid();
            this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), pRecipe, intlist.iterator(), k);
         }
      }

   }

   public void addItemToSlot(Iterator<Integer> pIngredients, int pSlot, int pMaxAmount, int pY, int pX) {
      Slot slot = this.menu.getSlot(pSlot);
      ItemStack itemstack = RecipeItemHelper.fromStackingIndex(pIngredients.next());
      if (!itemstack.isEmpty()) {
         for(int i = 0; i < pMaxAmount; ++i) {
            this.moveItemToGrid(slot, itemstack);
         }
      }

   }

   protected int getStackSize(boolean pPlaceAll, int pMaxPossible, boolean pRecipeMatches) {
      int i = 1;
      if (pPlaceAll) {
         i = pMaxPossible;
      } else if (pRecipeMatches) {
         i = 64;

         for(int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++j) {
            if (j != this.menu.getResultSlotIndex()) {
               ItemStack itemstack = this.menu.getSlot(j).getItem();
               if (!itemstack.isEmpty() && i > itemstack.getCount()) {
                  i = itemstack.getCount();
               }
            }
         }

         if (i < 64) {
            ++i;
         }
      }

      return i;
   }

   protected void moveItemToGrid(Slot pSlotToFill, ItemStack pIngredient) {
      int i = this.inventory.findSlotMatchingUnusedItem(pIngredient);
      if (i != -1) {
         ItemStack itemstack = this.inventory.getItem(i).copy();
         if (!itemstack.isEmpty()) {
            if (itemstack.getCount() > 1) {
               this.inventory.removeItem(i, 1);
            } else {
               this.inventory.removeItemNoUpdate(i);
            }

            itemstack.setCount(1);
            if (pSlotToFill.getItem().isEmpty()) {
               pSlotToFill.set(itemstack);
            } else {
               pSlotToFill.getItem().grow(1);
            }

         }
      }
   }

   /**
    * Places the output of the recipe into the player's inventory.
    */
   private boolean testClearGrid() {
      List<ItemStack> list = Lists.newArrayList();
      int i = this.getAmountOfFreeSlotsInInventory();

      for(int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++j) {
         if (j != this.menu.getResultSlotIndex()) {
            ItemStack itemstack = this.menu.getSlot(j).getItem().copy();
            if (!itemstack.isEmpty()) {
               int k = this.inventory.getSlotWithRemainingSpace(itemstack);
               if (k == -1 && list.size() <= i) {
                  for(ItemStack itemstack1 : list) {
                     if (itemstack1.sameItem(itemstack) && itemstack1.getCount() != itemstack1.getMaxStackSize() && itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
                        itemstack1.grow(itemstack.getCount());
                        itemstack.setCount(0);
                        break;
                     }
                  }

                  if (!itemstack.isEmpty()) {
                     if (list.size() >= i) {
                        return false;
                     }

                     list.add(itemstack);
                  }
               } else if (k == -1) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private int getAmountOfFreeSlotsInInventory() {
      int i = 0;

      for(ItemStack itemstack : this.inventory.items) {
         if (itemstack.isEmpty()) {
            ++i;
         }
      }

      return i;
   }
}
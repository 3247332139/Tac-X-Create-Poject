package net.minecraft.inventory.container;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.storage.MapData;

public class CartographyContainer extends Container {
   private final IWorldPosCallable access;
   private long lastSoundTime;
   public final IInventory container = new Inventory(2) {
      /**
       * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         CartographyContainer.this.slotsChanged(this);
         super.setChanged();
      }
   };
   private final CraftResultInventory resultContainer = new CraftResultInventory() {
      /**
       * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         CartographyContainer.this.slotsChanged(this);
         super.setChanged();
      }
   };

   public CartographyContainer(int pContainerId, PlayerInventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, IWorldPosCallable.NULL);
   }

   public CartographyContainer(int pContainerId, PlayerInventory pPlayerInventory, final IWorldPosCallable pAccess) {
      super(ContainerType.CARTOGRAPHY_TABLE, pContainerId);
      this.access = pAccess;
      this.addSlot(new Slot(this.container, 0, 15, 15) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            return pStack.getItem() == Items.FILLED_MAP;
         }
      });
      this.addSlot(new Slot(this.container, 1, 15, 52) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            Item item = pStack.getItem();
            return item == Items.PAPER || item == Items.MAP || item == Items.GLASS_PANE;
         }
      });
      this.addSlot(new Slot(this.resultContainer, 2, 145, 39) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            return false;
         }

         public ItemStack onTake(PlayerEntity pPlayer, ItemStack pStack) {
            CartographyContainer.this.slots.get(0).remove(1);
            CartographyContainer.this.slots.get(1).remove(1);
            pStack.getItem().onCraftedBy(pStack, pPlayer.level, pPlayer);
            pAccess.execute((p_242385_1_, p_242385_2_) -> {
               long l = p_242385_1_.getGameTime();
               if (CartographyContainer.this.lastSoundTime != l) {
                  p_242385_1_.playSound((PlayerEntity)null, p_242385_2_, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  CartographyContainer.this.lastSoundTime = l;
               }

            });
            return super.onTake(pPlayer, pStack);
         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
      }

   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.CARTOGRAPHY_TABLE);
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(IInventory pInventory) {
      ItemStack itemstack = this.container.getItem(0);
      ItemStack itemstack1 = this.container.getItem(1);
      ItemStack itemstack2 = this.resultContainer.getItem(2);
      if (itemstack2.isEmpty() || !itemstack.isEmpty() && !itemstack1.isEmpty()) {
         if (!itemstack.isEmpty() && !itemstack1.isEmpty()) {
            this.setupResultSlot(itemstack, itemstack1, itemstack2);
         }
      } else {
         this.resultContainer.removeItemNoUpdate(2);
      }

   }

   private void setupResultSlot(ItemStack p_216993_1_, ItemStack p_216993_2_, ItemStack p_216993_3_) {
      this.access.execute((p_216996_4_, p_216996_5_) -> {
         Item item = p_216993_2_.getItem();
         MapData mapdata = FilledMapItem.getSavedData(p_216993_1_, p_216996_4_);
         if (mapdata != null) {
            ItemStack itemstack;
            if (item == Items.PAPER && !mapdata.locked && mapdata.scale < 4) {
               itemstack = p_216993_1_.copy();
               itemstack.setCount(1);
               itemstack.getOrCreateTag().putInt("map_scale_direction", 1);
               this.broadcastChanges();
            } else if (item == Items.GLASS_PANE && !mapdata.locked) {
               itemstack = p_216993_1_.copy();
               itemstack.setCount(1);
               itemstack.getOrCreateTag().putBoolean("map_to_lock", true);
               this.broadcastChanges();
            } else {
               if (item != Items.MAP) {
                  this.resultContainer.removeItemNoUpdate(2);
                  this.broadcastChanges();
                  return;
               }

               itemstack = p_216993_1_.copy();
               itemstack.setCount(2);
               this.broadcastChanges();
            }

            if (!ItemStack.matches(itemstack, p_216993_3_)) {
               this.resultContainer.setItem(2, itemstack);
               this.broadcastChanges();
            }

         }
      });
   }

   /**
    * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
    * null for the initial slot that was double-clicked.
    */
   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return pSlot.container != this.resultContainer && super.canTakeItemForPickAll(pStack, pSlot);
   }

   /**
    * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
    * inventory and the other inventory(s).
    */
   public ItemStack quickMoveStack(PlayerEntity pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         Item item = itemstack1.getItem();
         itemstack = itemstack1.copy();
         if (pIndex == 2) {
            item.onCraftedBy(itemstack1, pPlayer.level, pPlayer);
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex != 1 && pIndex != 0) {
            if (item == Items.FILLED_MAP) {
               if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (item != Items.PAPER && item != Items.MAP && item != Items.GLASS_PANE) {
               if (pIndex >= 3 && pIndex < 30) {
                  if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (pIndex >= 30 && pIndex < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         }

         slot.setChanged();
         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, itemstack1);
         this.broadcastChanges();
      }

      return itemstack;
   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      super.removed(pPlayer);
      this.resultContainer.removeItemNoUpdate(2);
      this.access.execute((p_216995_2_, p_216995_3_) -> {
         this.clearContainer(pPlayer, pPlayer.level, this.container);
      });
   }
}
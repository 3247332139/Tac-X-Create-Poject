package net.minecraft.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LecternContainer extends Container {
   private final IInventory lectern;
   private final IIntArray lecternData;

   public LecternContainer(int pContainerId) {
      this(pContainerId, new Inventory(1), new IntArray(1));
   }

   public LecternContainer(int pContainerId, IInventory pLectern, IIntArray pLecternData) {
      super(ContainerType.LECTERN, pContainerId);
      checkContainerSize(pLectern, 1);
      checkContainerDataCount(pLecternData, 1);
      this.lectern = pLectern;
      this.lecternData = pLecternData;
      this.addSlot(new Slot(pLectern, 0, 0, 0) {
         /**
          * Called when the stack in a Slot changes
          */
         public void setChanged() {
            super.setChanged();
            LecternContainer.this.slotsChanged(this.container);
         }
      });
      this.addDataSlots(pLecternData);
   }

   /**
    * Handles the given Button-click on the server, currently only used by enchanting. Name is for legacy.
    */
   public boolean clickMenuButton(PlayerEntity pPlayer, int pId) {
      if (pId >= 100) {
         int k = pId - 100;
         this.setData(0, k);
         return true;
      } else {
         switch(pId) {
         case 1:
            int j = this.lecternData.get(0);
            this.setData(0, j - 1);
            return true;
         case 2:
            int i = this.lecternData.get(0);
            this.setData(0, i + 1);
            return true;
         case 3:
            if (!pPlayer.mayBuild()) {
               return false;
            }

            ItemStack itemstack = this.lectern.removeItemNoUpdate(0);
            this.lectern.setChanged();
            if (!pPlayer.inventory.add(itemstack)) {
               pPlayer.drop(itemstack, false);
            }

            return true;
         default:
            return false;
         }
      }
   }

   public void setData(int pId, int pData) {
      super.setData(pId, pData);
      this.broadcastChanges();
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return this.lectern.stillValid(pPlayer);
   }

   @OnlyIn(Dist.CLIENT)
   public ItemStack getBook() {
      return this.lectern.getItem(0);
   }

   @OnlyIn(Dist.CLIENT)
   public int getPage() {
      return this.lecternData.get(0);
   }
}
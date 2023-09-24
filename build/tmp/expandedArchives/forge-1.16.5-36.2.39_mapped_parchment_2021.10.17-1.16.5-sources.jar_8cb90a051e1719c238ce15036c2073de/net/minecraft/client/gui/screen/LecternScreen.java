package net.minecraft.client.gui.screen;

import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.LecternContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LecternScreen extends ReadBookScreen implements IHasContainer<LecternContainer> {
   private final LecternContainer menu;
   private final IContainerListener listener = new IContainerListener() {
      public void refreshContainer(Container pContainerToSend, NonNullList<ItemStack> pItemsList) {
         LecternScreen.this.bookChanged();
      }

      /**
       * Sends the contents of an inventory slot to the client-side Container. This doesn't have to match the actual
       * contents of that slot.
       */
      public void slotChanged(Container pContainerToSend, int pSlotInd, ItemStack pStack) {
         LecternScreen.this.bookChanged();
      }

      public void setContainerData(Container pContainer, int pVarToUpdate, int pNewValue) {
         if (pVarToUpdate == 0) {
            LecternScreen.this.pageChanged();
         }

      }
   };

   public LecternScreen(LecternContainer pLecternMenu, PlayerInventory pPlayerInventory, ITextComponent pTitle) {
      this.menu = pLecternMenu;
   }

   public LecternContainer getMenu() {
      return this.menu;
   }

   protected void init() {
      super.init();
      this.menu.addSlotListener(this.listener);
   }

   public void onClose() {
      this.minecraft.player.closeContainer();
      super.onClose();
   }

   public void removed() {
      super.removed();
      this.menu.removeSlotListener(this.listener);
   }

   protected void createMenuControls() {
      if (this.minecraft.player.mayBuild()) {
         this.addButton(new Button(this.width / 2 - 100, 196, 98, 20, DialogTexts.GUI_DONE, (p_214181_1_) -> {
            this.minecraft.setScreen((Screen)null);
         }));
         this.addButton(new Button(this.width / 2 + 2, 196, 98, 20, new TranslationTextComponent("lectern.take_book"), (p_214178_1_) -> {
            this.sendButtonClick(3);
         }));
      } else {
         super.createMenuControls();
      }

   }

   /**
    * Moves the display back one page
    */
   protected void pageBack() {
      this.sendButtonClick(1);
   }

   /**
    * Moves the display forward one page
    */
   protected void pageForward() {
      this.sendButtonClick(2);
   }

   /**
    * I'm not sure why this exists. The function it calls is public and does all of the work
    */
   protected boolean forcePage(int pPageNum) {
      if (pPageNum != this.menu.getPage()) {
         this.sendButtonClick(100 + pPageNum);
         return true;
      } else {
         return false;
      }
   }

   private void sendButtonClick(int pPageData) {
      this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, pPageData);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void bookChanged() {
      ItemStack itemstack = this.menu.getBook();
      this.setBookAccess(ReadBookScreen.IBookInfo.fromItem(itemstack));
   }

   private void pageChanged() {
      this.setPage(this.menu.getPage());
   }
}
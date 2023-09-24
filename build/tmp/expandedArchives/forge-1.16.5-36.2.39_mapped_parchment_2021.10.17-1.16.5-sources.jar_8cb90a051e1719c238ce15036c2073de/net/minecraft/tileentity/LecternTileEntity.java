package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IClearable;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.LecternContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class LecternTileEntity extends TileEntity implements IClearable, INamedContainerProvider {
   private final IInventory bookAccess = new IInventory() {
      /**
       * Returns the number of slots in the inventory.
       */
      public int getContainerSize() {
         return 1;
      }

      public boolean isEmpty() {
         return LecternTileEntity.this.book.isEmpty();
      }

      /**
       * Returns the stack in the given slot.
       */
      public ItemStack getItem(int pIndex) {
         return pIndex == 0 ? LecternTileEntity.this.book : ItemStack.EMPTY;
      }

      /**
       * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
       */
      public ItemStack removeItem(int pIndex, int pCount) {
         if (pIndex == 0) {
            ItemStack itemstack = LecternTileEntity.this.book.split(pCount);
            if (LecternTileEntity.this.book.isEmpty()) {
               LecternTileEntity.this.onBookItemRemove();
            }

            return itemstack;
         } else {
            return ItemStack.EMPTY;
         }
      }

      /**
       * Removes a stack from the given slot and returns it.
       */
      public ItemStack removeItemNoUpdate(int pIndex) {
         if (pIndex == 0) {
            ItemStack itemstack = LecternTileEntity.this.book;
            LecternTileEntity.this.book = ItemStack.EMPTY;
            LecternTileEntity.this.onBookItemRemove();
            return itemstack;
         } else {
            return ItemStack.EMPTY;
         }
      }

      /**
       * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
       */
      public void setItem(int pIndex, ItemStack pStack) {
      }

      /**
       * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
       */
      public int getMaxStackSize() {
         return 1;
      }

      /**
       * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         LecternTileEntity.this.setChanged();
      }

      /**
       * Don't rename this method to canInteractWith due to conflicts with Container
       */
      public boolean stillValid(PlayerEntity pPlayer) {
         if (LecternTileEntity.this.level.getBlockEntity(LecternTileEntity.this.worldPosition) != LecternTileEntity.this) {
            return false;
         } else {
            return pPlayer.distanceToSqr((double)LecternTileEntity.this.worldPosition.getX() + 0.5D, (double)LecternTileEntity.this.worldPosition.getY() + 0.5D, (double)LecternTileEntity.this.worldPosition.getZ() + 0.5D) > 64.0D ? false : LecternTileEntity.this.hasBook();
         }
      }

      /**
       * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
       * guis use Slot.isItemValid
       */
      public boolean canPlaceItem(int pIndex, ItemStack pStack) {
         return false;
      }

      public void clearContent() {
      }
   };
   private final IIntArray dataAccess = new IIntArray() {
      public int get(int pIndex) {
         return pIndex == 0 ? LecternTileEntity.this.page : 0;
      }

      public void set(int pIndex, int pValue) {
         if (pIndex == 0) {
            LecternTileEntity.this.setPage(pValue);
         }

      }

      public int getCount() {
         return 1;
      }
   };
   private ItemStack book = ItemStack.EMPTY;
   private int page;
   private int pageCount;

   public LecternTileEntity() {
      super(TileEntityType.LECTERN);
   }

   public ItemStack getBook() {
      return this.book;
   }

   /**
    * @return whether the ItemStack in this lectern is a book or written book
    */
   public boolean hasBook() {
      Item item = this.book.getItem();
      return item == Items.WRITABLE_BOOK || item == Items.WRITTEN_BOOK;
   }

   /**
    * Sets the ItemStack in this lectern. Note that this does not update the block state, use {@link
    * net.minecraft.world.level.block.LecternBlock#tryPlaceBook} for that.
    */
   public void setBook(ItemStack pStack) {
      this.setBook(pStack, (PlayerEntity)null);
   }

   private void onBookItemRemove() {
      this.page = 0;
      this.pageCount = 0;
      LecternBlock.resetBookState(this.getLevel(), this.getBlockPos(), this.getBlockState(), false);
   }

   /**
    * Sets the ItemStack in this lectern. Note that this does not update the block state, use {@link
    * net.minecraft.world.level.block.LecternBlock#tryPlaceBook} for that.
    * @param pPlayer the player used for resolving the components within the book
    */
   public void setBook(ItemStack pStack, @Nullable PlayerEntity pPlayer) {
      this.book = this.resolveBook(pStack, pPlayer);
      this.page = 0;
      this.pageCount = WrittenBookItem.getPageCount(this.book);
      this.setChanged();
   }

   private void setPage(int pPage) {
      int i = MathHelper.clamp(pPage, 0, this.pageCount - 1);
      if (i != this.page) {
         this.page = i;
         this.setChanged();
         LecternBlock.signalPageChange(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public int getPage() {
      return this.page;
   }

   public int getRedstoneSignal() {
      float f = this.pageCount > 1 ? (float)this.getPage() / ((float)this.pageCount - 1.0F) : 1.0F;
      return MathHelper.floor(f * 14.0F) + (this.hasBook() ? 1 : 0);
   }

   /**
    * Resolves the contents of the passed ItemStack, if it is a book
    */
   private ItemStack resolveBook(ItemStack pStack, @Nullable PlayerEntity pPlayer) {
      if (this.level instanceof ServerWorld && pStack.getItem() == Items.WRITTEN_BOOK) {
         WrittenBookItem.resolveBookComponents(pStack, this.createCommandSourceStack(pPlayer), pPlayer);
      }

      return pStack;
   }

   /**
    * Creates a CommandSourceStack for resolving the contents of a book. If the player is null, a CommandSourceStack
    * with the generic name {@code "Lectern"} is used.
    */
   private CommandSource createCommandSourceStack(@Nullable PlayerEntity pPlayer) {
      String s;
      ITextComponent itextcomponent;
      if (pPlayer == null) {
         s = "Lectern";
         itextcomponent = new StringTextComponent("Lectern");
      } else {
         s = pPlayer.getName().getString();
         itextcomponent = pPlayer.getDisplayName();
      }

      Vector3d vector3d = Vector3d.atCenterOf(this.worldPosition);
      return new CommandSource(ICommandSource.NULL, vector3d, Vector2f.ZERO, (ServerWorld)this.level, 2, s, itextcomponent, this.level.getServer(), pPlayer);
   }

   /**
    * Checks if players can use this tile entity to access operator (permission level 2) commands either directly or
    * indirectly, such as give or setblock. A similar method exists for entities at {@link
    * net.minecraft.entity.Entity#ignoreItemEntityData()}.<p>For example, {@link
    * net.minecraft.tileentity.TileEntitySign#onlyOpsCanSetNbt() signs} (player right-clicking) and {@link
    * net.minecraft.tileentity.TileEntityCommandBlock#onlyOpsCanSetNbt() command blocks} are considered
    * accessible.</p>@return true if this block entity offers ways for unauthorized players to use restricted commands
    */
   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      super.load(p_230337_1_, p_230337_2_);
      if (p_230337_2_.contains("Book", 10)) {
         this.book = this.resolveBook(ItemStack.of(p_230337_2_.getCompound("Book")), (PlayerEntity)null);
      } else {
         this.book = ItemStack.EMPTY;
      }

      this.pageCount = WrittenBookItem.getPageCount(this.book);
      this.page = MathHelper.clamp(p_230337_2_.getInt("Page"), 0, this.pageCount - 1);
   }

   public CompoundNBT save(CompoundNBT pCompound) {
      super.save(pCompound);
      if (!this.getBook().isEmpty()) {
         pCompound.put("Book", this.getBook().save(new CompoundNBT()));
         pCompound.putInt("Page", this.page);
      }

      return pCompound;
   }

   public void clearContent() {
      this.setBook(ItemStack.EMPTY);
   }

   public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
      return new LecternContainer(p_createMenu_1_, this.bookAccess, this.dataAccess);
   }

   public ITextComponent getDisplayName() {
      return new TranslationTextComponent("container.lectern");
   }
}
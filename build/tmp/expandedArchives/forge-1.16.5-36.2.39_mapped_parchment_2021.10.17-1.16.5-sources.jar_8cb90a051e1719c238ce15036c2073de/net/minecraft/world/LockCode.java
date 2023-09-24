package net.minecraft.world;

import javax.annotation.concurrent.Immutable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

@Immutable
public class LockCode {
   public static final LockCode NO_LOCK = new LockCode("");
   private final String key;

   public LockCode(String pKey) {
      this.key = pKey;
   }

   public boolean unlocksWith(ItemStack pStack) {
      return this.key.isEmpty() || !pStack.isEmpty() && pStack.hasCustomHoverName() && this.key.equals(pStack.getHoverName().getString());
   }

   public void addToTag(CompoundNBT pNbt) {
      if (!this.key.isEmpty()) {
         pNbt.putString("Lock", this.key);
      }

   }

   public static LockCode fromTag(CompoundNBT pNbt) {
      return pNbt.contains("Lock", 8) ? new LockCode(pNbt.getString("Lock")) : NO_LOCK;
   }
}
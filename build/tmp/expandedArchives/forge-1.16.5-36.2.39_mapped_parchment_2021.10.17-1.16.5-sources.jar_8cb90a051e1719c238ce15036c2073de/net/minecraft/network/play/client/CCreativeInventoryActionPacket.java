package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CCreativeInventoryActionPacket implements IPacket<IServerPlayNetHandler> {
   private int slotNum;
   private ItemStack itemStack = ItemStack.EMPTY;

   public CCreativeInventoryActionPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CCreativeInventoryActionPacket(int pSlotNum, ItemStack pItemStack) {
      this.slotNum = pSlotNum;
      this.itemStack = pItemStack.copy();
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleSetCreativeModeSlot(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.slotNum = p_148837_1_.readShort();
      this.itemStack = p_148837_1_.readItem();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeShort(this.slotNum);
      pBuffer.writeItemStack(this.itemStack, false); //Forge: Include full tag for C->S
   }

   public int getSlotNum() {
      return this.slotNum;
   }

   public ItemStack getItem() {
      return this.itemStack;
   }
}

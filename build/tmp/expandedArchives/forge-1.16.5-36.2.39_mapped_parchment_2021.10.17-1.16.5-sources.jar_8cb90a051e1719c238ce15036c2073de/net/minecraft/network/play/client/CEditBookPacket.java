package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CEditBookPacket implements IPacket<IServerPlayNetHandler> {
   private ItemStack book;
   private boolean signing;
   private int slot;

   public CEditBookPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CEditBookPacket(ItemStack p_i244520_1_, boolean p_i244520_2_, int p_i244520_3_) {
      this.book = p_i244520_1_.copy();
      this.signing = p_i244520_2_;
      this.slot = p_i244520_3_;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.book = p_148837_1_.readItem();
      this.signing = p_148837_1_.readBoolean();
      this.slot = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeItem(this.book);
      pBuffer.writeBoolean(this.signing);
      pBuffer.writeVarInt(this.slot);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleEditBook(this);
   }

   /**
    * The client written book stack containing up to date nbt data.
    */
   public ItemStack getBook() {
      return this.book;
   }

   /**
    * If true it updates author, title and pages. Otherwise just update pages.
    */
   public boolean isSigning() {
      return this.signing;
   }

   public int getSlot() {
      return this.slot;
   }
}
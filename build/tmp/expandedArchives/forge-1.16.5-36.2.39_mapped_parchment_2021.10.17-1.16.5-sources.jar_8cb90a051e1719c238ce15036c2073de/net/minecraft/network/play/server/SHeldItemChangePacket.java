package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SHeldItemChangePacket implements IPacket<IClientPlayNetHandler> {
   private int slot;

   public SHeldItemChangePacket() {
   }

   public SHeldItemChangePacket(int pSlot) {
      this.slot = pSlot;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.slot = p_148837_1_.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.slot);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetCarriedItem(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getSlot() {
      return this.slot;
   }
}
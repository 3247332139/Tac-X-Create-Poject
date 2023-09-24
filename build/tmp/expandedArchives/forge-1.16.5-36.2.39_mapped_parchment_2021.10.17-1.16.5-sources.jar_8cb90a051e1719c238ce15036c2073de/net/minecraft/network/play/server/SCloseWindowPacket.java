package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;

public class SCloseWindowPacket implements IPacket<IClientPlayNetHandler> {
   private int containerId;

   public SCloseWindowPacket() {
   }

   public SCloseWindowPacket(int pContainerId) {
      this.containerId = pContainerId;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleContainerClose(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.containerId = p_148837_1_.readUnsignedByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.containerId);
   }
}
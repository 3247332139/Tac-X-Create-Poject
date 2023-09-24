package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SWindowPropertyPacket implements IPacket<IClientPlayNetHandler> {
   private int containerId;
   private int id;
   private int value;

   public SWindowPropertyPacket() {
   }

   public SWindowPropertyPacket(int pContainerId, int pId, int pValue) {
      this.containerId = pContainerId;
      this.id = pId;
      this.value = pValue;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleContainerSetData(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.containerId = p_148837_1_.readUnsignedByte();
      this.id = p_148837_1_.readShort();
      this.value = p_148837_1_.readShort();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeShort(this.id);
      pBuffer.writeShort(this.value);
   }

   @OnlyIn(Dist.CLIENT)
   public int getContainerId() {
      return this.containerId;
   }

   @OnlyIn(Dist.CLIENT)
   public int getId() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   public int getValue() {
      return this.value;
   }
}
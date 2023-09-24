package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SOpenHorseWindowPacket implements IPacket<IClientPlayNetHandler> {
   private int containerId;
   private int size;
   private int entityId;

   public SOpenHorseWindowPacket() {
   }

   public SOpenHorseWindowPacket(int pContainerId, int pSize, int pEntityId) {
      this.containerId = pContainerId;
      this.size = pSize;
      this.entityId = pEntityId;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleHorseScreenOpen(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.containerId = p_148837_1_.readUnsignedByte();
      this.size = p_148837_1_.readVarInt();
      this.entityId = p_148837_1_.readInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeVarInt(this.size);
      pBuffer.writeInt(this.entityId);
   }

   @OnlyIn(Dist.CLIENT)
   public int getContainerId() {
      return this.containerId;
   }

   @OnlyIn(Dist.CLIENT)
   public int getSize() {
      return this.size;
   }

   @OnlyIn(Dist.CLIENT)
   public int getEntityId() {
      return this.entityId;
   }
}
package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CQueryEntityNBTPacket implements IPacket<IServerPlayNetHandler> {
   private int transactionId;
   private int entityId;

   public CQueryEntityNBTPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CQueryEntityNBTPacket(int pTransactionId, int pEntityId) {
      this.transactionId = pTransactionId;
      this.entityId = pEntityId;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.transactionId = p_148837_1_.readVarInt();
      this.entityId = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.transactionId);
      pBuffer.writeVarInt(this.entityId);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleEntityTagQuery(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   public int getEntityId() {
      return this.entityId;
   }
}
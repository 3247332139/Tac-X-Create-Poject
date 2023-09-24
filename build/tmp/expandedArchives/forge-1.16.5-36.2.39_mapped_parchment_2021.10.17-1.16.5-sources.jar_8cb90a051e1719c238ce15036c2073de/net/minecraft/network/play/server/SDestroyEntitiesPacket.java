package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SDestroyEntitiesPacket implements IPacket<IClientPlayNetHandler> {
   private int[] entityIds;

   public SDestroyEntitiesPacket() {
   }

   public SDestroyEntitiesPacket(int... p_i46926_1_) {
      this.entityIds = p_i46926_1_;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.entityIds = new int[p_148837_1_.readVarInt()];

      for(int i = 0; i < this.entityIds.length; ++i) {
         this.entityIds[i] = p_148837_1_.readVarInt();
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.entityIds.length);

      for(int i : this.entityIds) {
         pBuffer.writeVarInt(i);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleRemoveEntity(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int[] getEntityIds() {
      return this.entityIds;
   }
}
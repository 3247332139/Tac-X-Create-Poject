package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SUpdateViewDistancePacket implements IPacket<IClientPlayNetHandler> {
   private int radius;

   public SUpdateViewDistancePacket() {
   }

   public SUpdateViewDistancePacket(int pRadius) {
      this.radius = pRadius;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.radius = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.radius);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetChunkCacheRadius(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getRadius() {
      return this.radius;
   }
}
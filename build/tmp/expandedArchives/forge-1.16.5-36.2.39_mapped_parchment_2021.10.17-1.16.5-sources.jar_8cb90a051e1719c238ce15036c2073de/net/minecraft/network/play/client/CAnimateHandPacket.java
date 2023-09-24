package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.util.Hand;

public class CAnimateHandPacket implements IPacket<IServerPlayNetHandler> {
   private Hand hand;

   public CAnimateHandPacket() {
   }

   public CAnimateHandPacket(Hand pHand) {
      this.hand = pHand;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.hand = p_148837_1_.readEnum(Hand.class);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeEnum(this.hand);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleAnimate(this);
   }

   public Hand getHand() {
      return this.hand;
   }
}
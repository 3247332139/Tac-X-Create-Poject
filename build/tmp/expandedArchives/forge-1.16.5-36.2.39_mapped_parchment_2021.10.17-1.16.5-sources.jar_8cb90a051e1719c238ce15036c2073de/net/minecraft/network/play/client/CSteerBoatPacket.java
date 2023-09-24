package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;

public class CSteerBoatPacket implements IPacket<IServerPlayNetHandler> {
   private boolean left;
   private boolean right;

   public CSteerBoatPacket() {
   }

   public CSteerBoatPacket(boolean pLeft, boolean pRight) {
      this.left = pLeft;
      this.right = pRight;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.left = p_148837_1_.readBoolean();
      this.right = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBoolean(this.left);
      pBuffer.writeBoolean(this.right);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handlePaddleBoat(this);
   }

   public boolean getLeft() {
      return this.left;
   }

   public boolean getRight() {
      return this.right;
   }
}
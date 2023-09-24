package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;

public class CClientStatusPacket implements IPacket<IServerPlayNetHandler> {
   private CClientStatusPacket.State action;

   public CClientStatusPacket() {
   }

   public CClientStatusPacket(CClientStatusPacket.State pAction) {
      this.action = pAction;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.action = p_148837_1_.readEnum(CClientStatusPacket.State.class);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeEnum(this.action);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleClientCommand(this);
   }

   public CClientStatusPacket.State getAction() {
      return this.action;
   }

   public static enum State {
      PERFORM_RESPAWN,
      REQUEST_STATS;
   }
}
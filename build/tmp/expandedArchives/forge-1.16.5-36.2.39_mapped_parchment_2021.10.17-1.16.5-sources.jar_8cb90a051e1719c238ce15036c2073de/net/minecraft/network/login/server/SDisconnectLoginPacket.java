package net.minecraft.network.login.server;

import java.io.IOException;
import net.minecraft.client.network.login.IClientLoginNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SDisconnectLoginPacket implements IPacket<IClientLoginNetHandler> {
   private ITextComponent reason;

   public SDisconnectLoginPacket() {
   }

   public SDisconnectLoginPacket(ITextComponent pReason) {
      this.reason = pReason;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.reason = ITextComponent.Serializer.fromJsonLenient(p_148837_1_.readUtf(262144));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeComponent(this.reason);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientLoginNetHandler pHandler) {
      pHandler.handleDisconnect(this);
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getReason() {
      return this.reason;
   }
}
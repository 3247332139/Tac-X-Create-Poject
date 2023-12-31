package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Main netty packet encoder. Writes the packet ID as a VarInt based on the current {@link ConnectionProtocol} as well
 * as the packet's data.
 */
public class NettyPacketEncoder extends MessageToByteEncoder<IPacket<?>> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Marker MARKER = MarkerManager.getMarker("PACKET_SENT", NetworkManager.PACKET_MARKER);
   private final PacketDirection flow;

   public NettyPacketEncoder(PacketDirection pFlow) {
      this.flow = pFlow;
   }

   protected void encode(ChannelHandlerContext p_encode_1_, IPacket<?> p_encode_2_, ByteBuf p_encode_3_) throws Exception {
      ProtocolType protocoltype = p_encode_1_.channel().attr(NetworkManager.ATTRIBUTE_PROTOCOL).get();
      if (protocoltype == null) {
         throw new RuntimeException("ConnectionProtocol unknown: " + p_encode_2_);
      } else {
         Integer integer = protocoltype.getPacketId(this.flow, p_encode_2_);
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MARKER, "OUT: [{}:{}] {}", p_encode_1_.channel().attr(NetworkManager.ATTRIBUTE_PROTOCOL).get(), integer, p_encode_2_.getClass().getName());
         }

         if (integer == null) {
            throw new IOException("Can't serialize unregistered packet");
         } else {
            PacketBuffer packetbuffer = new PacketBuffer(p_encode_3_);
            packetbuffer.writeVarInt(integer);

            try {
               p_encode_2_.write(packetbuffer);
            } catch (Throwable throwable) {
               LOGGER.error("Error encoding packet", throwable); // Forge: Get Minecraft to spit out more information about the Throwable we got.
               if (p_encode_2_.isSkippable()) {
                  throw new SkipableEncoderException(throwable);
               } else {
                  throw throwable;
               }
            }
         }
      }
   }
}

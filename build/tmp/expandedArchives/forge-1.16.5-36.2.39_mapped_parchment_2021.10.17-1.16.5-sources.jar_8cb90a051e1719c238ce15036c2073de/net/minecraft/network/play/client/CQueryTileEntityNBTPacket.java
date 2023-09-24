package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CQueryTileEntityNBTPacket implements IPacket<IServerPlayNetHandler> {
   private int transactionId;
   private BlockPos pos;

   public CQueryTileEntityNBTPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CQueryTileEntityNBTPacket(int pTransactionId, BlockPos pPos) {
      this.transactionId = pTransactionId;
      this.pos = pPos;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.transactionId = p_148837_1_.readVarInt();
      this.pos = p_148837_1_.readBlockPos();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.transactionId);
      pBuffer.writeBlockPos(this.pos);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleBlockEntityTagQuery(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}
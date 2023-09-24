package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SChangeBlockPacket implements IPacket<IClientPlayNetHandler> {
   private BlockPos pos;
   private BlockState blockState;

   public SChangeBlockPacket() {
   }

   public SChangeBlockPacket(BlockPos pPos, BlockState pBlockState) {
      this.pos = pPos;
      this.blockState = pBlockState;
   }

   public SChangeBlockPacket(IBlockReader pBlockGetter, BlockPos pPos) {
      this(pPos, pBlockGetter.getBlockState(pPos));
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.pos = p_148837_1_.readBlockPos();
      this.blockState = Block.BLOCK_STATE_REGISTRY.byId(p_148837_1_.readVarInt());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeVarInt(Block.getId(this.blockState));
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleBlockUpdate(this);
   }

   @OnlyIn(Dist.CLIENT)
   public BlockState getBlockState() {
      return this.blockState;
   }

   @OnlyIn(Dist.CLIENT)
   public BlockPos getPos() {
      return this.pos;
   }
}
package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SPlaySoundEventPacket implements IPacket<IClientPlayNetHandler> {
   private int type;
   private BlockPos pos;
   /** can be a block/item id or other depending on the soundtype */
   private int data;
   /** If true the sound is played across the server */
   private boolean globalEvent;

   public SPlaySoundEventPacket() {
   }

   public SPlaySoundEventPacket(int pType, BlockPos pPos, int pData, boolean pGlobalEvent) {
      this.type = pType;
      this.pos = pPos.immutable();
      this.data = pData;
      this.globalEvent = pGlobalEvent;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.type = p_148837_1_.readInt();
      this.pos = p_148837_1_.readBlockPos();
      this.data = p_148837_1_.readInt();
      this.globalEvent = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeInt(this.type);
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeInt(this.data);
      pBuffer.writeBoolean(this.globalEvent);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleLevelEvent(this);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isGlobalEvent() {
      return this.globalEvent;
   }

   @OnlyIn(Dist.CLIENT)
   public int getType() {
      return this.type;
   }

   @OnlyIn(Dist.CLIENT)
   public int getData() {
      return this.data;
   }

   @OnlyIn(Dist.CLIENT)
   public BlockPos getPos() {
      return this.pos;
   }
}
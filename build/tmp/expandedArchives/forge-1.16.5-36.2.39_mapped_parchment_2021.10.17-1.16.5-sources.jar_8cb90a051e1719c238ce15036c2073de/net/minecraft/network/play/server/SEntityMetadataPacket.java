package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SEntityMetadataPacket implements IPacket<IClientPlayNetHandler> {
   private int id;
   private List<EntityDataManager.DataEntry<?>> packedItems;

   public SEntityMetadataPacket() {
   }

   public SEntityMetadataPacket(int pId, EntityDataManager pEntityData, boolean pSendAll) {
      this.id = pId;
      if (pSendAll) {
         this.packedItems = pEntityData.getAll();
         pEntityData.clearDirty();
      } else {
         this.packedItems = pEntityData.packDirty();
      }

   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readVarInt();
      this.packedItems = EntityDataManager.unpack(p_148837_1_);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.id);
      EntityDataManager.pack(this.packedItems, pBuffer);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetEntityData(this);
   }

   @OnlyIn(Dist.CLIENT)
   public List<EntityDataManager.DataEntry<?>> getUnpackedData() {
      return this.packedItems;
   }

   @OnlyIn(Dist.CLIENT)
   public int getId() {
      return this.id;
   }
}
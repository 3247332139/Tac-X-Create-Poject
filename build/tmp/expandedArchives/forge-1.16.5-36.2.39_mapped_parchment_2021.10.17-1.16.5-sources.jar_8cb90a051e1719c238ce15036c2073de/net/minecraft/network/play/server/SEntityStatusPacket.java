package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SEntityStatusPacket implements IPacket<IClientPlayNetHandler> {
   private int entityId;
   private byte eventId;

   public SEntityStatusPacket() {
   }

   public SEntityStatusPacket(Entity pEntity, byte pEventId) {
      this.entityId = pEntity.getId();
      this.eventId = pEventId;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.entityId = p_148837_1_.readInt();
      this.eventId = p_148837_1_.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeInt(this.entityId);
      pBuffer.writeByte(this.eventId);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleEntityEvent(this);
   }

   @OnlyIn(Dist.CLIENT)
   public Entity getEntity(World pLevel) {
      return pLevel.getEntity(this.entityId);
   }

   @OnlyIn(Dist.CLIENT)
   public byte getEventId() {
      return this.eventId;
   }
}
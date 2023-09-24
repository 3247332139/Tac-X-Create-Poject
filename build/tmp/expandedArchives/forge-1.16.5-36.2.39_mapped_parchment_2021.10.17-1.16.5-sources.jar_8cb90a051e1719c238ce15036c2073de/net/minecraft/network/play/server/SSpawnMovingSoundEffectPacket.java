package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

public class SSpawnMovingSoundEffectPacket implements IPacket<IClientPlayNetHandler> {
   private SoundEvent sound;
   private SoundCategory source;
   private int id;
   private float volume;
   private float pitch;

   public SSpawnMovingSoundEffectPacket() {
   }

   public SSpawnMovingSoundEffectPacket(SoundEvent pSound, SoundCategory pSource, Entity pEntity, float pVolume, float pPitch) {
      Validate.notNull(pSound, "sound");
      this.sound = pSound;
      this.source = pSource;
      this.id = pEntity.getId();
      this.volume = pVolume;
      this.pitch = pPitch;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.sound = Registry.SOUND_EVENT.byId(p_148837_1_.readVarInt());
      this.source = p_148837_1_.readEnum(SoundCategory.class);
      this.id = p_148837_1_.readVarInt();
      this.volume = p_148837_1_.readFloat();
      this.pitch = p_148837_1_.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(Registry.SOUND_EVENT.getId(this.sound));
      pBuffer.writeEnum(this.source);
      pBuffer.writeVarInt(this.id);
      pBuffer.writeFloat(this.volume);
      pBuffer.writeFloat(this.pitch);
   }

   @OnlyIn(Dist.CLIENT)
   public SoundEvent getSound() {
      return this.sound;
   }

   @OnlyIn(Dist.CLIENT)
   public SoundCategory getSource() {
      return this.source;
   }

   @OnlyIn(Dist.CLIENT)
   public int getId() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   public float getVolume() {
      return this.volume;
   }

   @OnlyIn(Dist.CLIENT)
   public float getPitch() {
      return this.pitch;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSoundEntityEvent(this);
   }
}
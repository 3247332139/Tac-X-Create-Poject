package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SUpdateBossInfoPacket implements IPacket<IClientPlayNetHandler> {
   private UUID id;
   private SUpdateBossInfoPacket.Operation operation;
   private ITextComponent name;
   private float pct;
   private BossInfo.Color color;
   private BossInfo.Overlay overlay;
   private boolean darkenScreen;
   private boolean playMusic;
   private boolean createWorldFog;

   public SUpdateBossInfoPacket() {
   }

   public SUpdateBossInfoPacket(SUpdateBossInfoPacket.Operation p_i46964_1_, BossInfo p_i46964_2_) {
      this.operation = p_i46964_1_;
      this.id = p_i46964_2_.getId();
      this.name = p_i46964_2_.getName();
      this.pct = p_i46964_2_.getPercent();
      this.color = p_i46964_2_.getColor();
      this.overlay = p_i46964_2_.getOverlay();
      this.darkenScreen = p_i46964_2_.shouldDarkenScreen();
      this.playMusic = p_i46964_2_.shouldPlayBossMusic();
      this.createWorldFog = p_i46964_2_.shouldCreateWorldFog();
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readUUID();
      this.operation = p_148837_1_.readEnum(SUpdateBossInfoPacket.Operation.class);
      switch(this.operation) {
      case ADD:
         this.name = p_148837_1_.readComponent();
         this.pct = p_148837_1_.readFloat();
         this.color = p_148837_1_.readEnum(BossInfo.Color.class);
         this.overlay = p_148837_1_.readEnum(BossInfo.Overlay.class);
         this.decodeProperties(p_148837_1_.readUnsignedByte());
      case REMOVE:
      default:
         break;
      case UPDATE_PCT:
         this.pct = p_148837_1_.readFloat();
         break;
      case UPDATE_NAME:
         this.name = p_148837_1_.readComponent();
         break;
      case UPDATE_STYLE:
         this.color = p_148837_1_.readEnum(BossInfo.Color.class);
         this.overlay = p_148837_1_.readEnum(BossInfo.Overlay.class);
         break;
      case UPDATE_PROPERTIES:
         this.decodeProperties(p_148837_1_.readUnsignedByte());
      }

   }

   private void decodeProperties(int p_186903_1_) {
      this.darkenScreen = (p_186903_1_ & 1) > 0;
      this.playMusic = (p_186903_1_ & 2) > 0;
      this.createWorldFog = (p_186903_1_ & 4) > 0;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeUUID(this.id);
      pBuffer.writeEnum(this.operation);
      switch(this.operation) {
      case ADD:
         pBuffer.writeComponent(this.name);
         pBuffer.writeFloat(this.pct);
         pBuffer.writeEnum(this.color);
         pBuffer.writeEnum(this.overlay);
         pBuffer.writeByte(this.encodeProperties());
      case REMOVE:
      default:
         break;
      case UPDATE_PCT:
         pBuffer.writeFloat(this.pct);
         break;
      case UPDATE_NAME:
         pBuffer.writeComponent(this.name);
         break;
      case UPDATE_STYLE:
         pBuffer.writeEnum(this.color);
         pBuffer.writeEnum(this.overlay);
         break;
      case UPDATE_PROPERTIES:
         pBuffer.writeByte(this.encodeProperties());
      }

   }

   private int encodeProperties() {
      int i = 0;
      if (this.darkenScreen) {
         i |= 1;
      }

      if (this.playMusic) {
         i |= 2;
      }

      if (this.createWorldFog) {
         i |= 4;
      }

      return i;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleBossUpdate(this);
   }

   @OnlyIn(Dist.CLIENT)
   public UUID getId() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   public SUpdateBossInfoPacket.Operation getOperation() {
      return this.operation;
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getName() {
      return this.name;
   }

   @OnlyIn(Dist.CLIENT)
   public float getPercent() {
      return this.pct;
   }

   @OnlyIn(Dist.CLIENT)
   public BossInfo.Color getColor() {
      return this.color;
   }

   @OnlyIn(Dist.CLIENT)
   public BossInfo.Overlay getOverlay() {
      return this.overlay;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldDarkenScreen() {
      return this.darkenScreen;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldPlayMusic() {
      return this.playMusic;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldCreateWorldFog() {
      return this.createWorldFog;
   }

   public static enum Operation {
      ADD,
      REMOVE,
      UPDATE_PCT,
      UPDATE_NAME,
      UPDATE_STYLE,
      UPDATE_PROPERTIES;
   }
}
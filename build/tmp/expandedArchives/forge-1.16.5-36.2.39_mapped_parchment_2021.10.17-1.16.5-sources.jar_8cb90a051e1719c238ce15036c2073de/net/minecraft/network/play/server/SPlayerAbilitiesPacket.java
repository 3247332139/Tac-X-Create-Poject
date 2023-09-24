package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SPlayerAbilitiesPacket implements IPacket<IClientPlayNetHandler> {
   private boolean invulnerable;
   private boolean isFlying;
   private boolean canFly;
   private boolean instabuild;
   private float flyingSpeed;
   private float walkingSpeed;

   public SPlayerAbilitiesPacket() {
   }

   public SPlayerAbilitiesPacket(PlayerAbilities pAbilities) {
      this.invulnerable = pAbilities.invulnerable;
      this.isFlying = pAbilities.flying;
      this.canFly = pAbilities.mayfly;
      this.instabuild = pAbilities.instabuild;
      this.flyingSpeed = pAbilities.getFlyingSpeed();
      this.walkingSpeed = pAbilities.getWalkingSpeed();
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      byte b0 = p_148837_1_.readByte();
      this.invulnerable = (b0 & 1) != 0;
      this.isFlying = (b0 & 2) != 0;
      this.canFly = (b0 & 4) != 0;
      this.instabuild = (b0 & 8) != 0;
      this.flyingSpeed = p_148837_1_.readFloat();
      this.walkingSpeed = p_148837_1_.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      byte b0 = 0;
      if (this.invulnerable) {
         b0 = (byte)(b0 | 1);
      }

      if (this.isFlying) {
         b0 = (byte)(b0 | 2);
      }

      if (this.canFly) {
         b0 = (byte)(b0 | 4);
      }

      if (this.instabuild) {
         b0 = (byte)(b0 | 8);
      }

      pBuffer.writeByte(b0);
      pBuffer.writeFloat(this.flyingSpeed);
      pBuffer.writeFloat(this.walkingSpeed);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handlePlayerAbilities(this);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isFlying() {
      return this.isFlying;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean canFly() {
      return this.canFly;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean canInstabuild() {
      return this.instabuild;
   }

   @OnlyIn(Dist.CLIENT)
   public float getFlyingSpeed() {
      return this.flyingSpeed;
   }

   @OnlyIn(Dist.CLIENT)
   public float getWalkingSpeed() {
      return this.walkingSpeed;
   }
}
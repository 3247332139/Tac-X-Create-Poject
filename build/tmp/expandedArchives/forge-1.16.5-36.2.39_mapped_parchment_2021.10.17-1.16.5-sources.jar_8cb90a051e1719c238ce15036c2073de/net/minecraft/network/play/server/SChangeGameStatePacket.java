package net.minecraft.network.play.server;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SChangeGameStatePacket implements IPacket<IClientPlayNetHandler> {
   public static final SChangeGameStatePacket.State NO_RESPAWN_BLOCK_AVAILABLE = new SChangeGameStatePacket.State(0);
   public static final SChangeGameStatePacket.State START_RAINING = new SChangeGameStatePacket.State(1);
   public static final SChangeGameStatePacket.State STOP_RAINING = new SChangeGameStatePacket.State(2);
   public static final SChangeGameStatePacket.State CHANGE_GAME_MODE = new SChangeGameStatePacket.State(3);
   public static final SChangeGameStatePacket.State WIN_GAME = new SChangeGameStatePacket.State(4);
   public static final SChangeGameStatePacket.State DEMO_EVENT = new SChangeGameStatePacket.State(5);
   public static final SChangeGameStatePacket.State ARROW_HIT_PLAYER = new SChangeGameStatePacket.State(6);
   public static final SChangeGameStatePacket.State RAIN_LEVEL_CHANGE = new SChangeGameStatePacket.State(7);
   public static final SChangeGameStatePacket.State THUNDER_LEVEL_CHANGE = new SChangeGameStatePacket.State(8);
   public static final SChangeGameStatePacket.State PUFFER_FISH_STING = new SChangeGameStatePacket.State(9);
   public static final SChangeGameStatePacket.State GUARDIAN_ELDER_EFFECT = new SChangeGameStatePacket.State(10);
   public static final SChangeGameStatePacket.State IMMEDIATE_RESPAWN = new SChangeGameStatePacket.State(11);
   private SChangeGameStatePacket.State event;
   private float param;

   public SChangeGameStatePacket() {
   }

   public SChangeGameStatePacket(SChangeGameStatePacket.State pEvent, float pParam) {
      this.event = pEvent;
      this.param = pParam;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.event = SChangeGameStatePacket.State.TYPES.get(p_148837_1_.readUnsignedByte());
      this.param = p_148837_1_.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.event.id);
      pBuffer.writeFloat(this.param);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleGameEvent(this);
   }

   @OnlyIn(Dist.CLIENT)
   public SChangeGameStatePacket.State getEvent() {
      return this.event;
   }

   @OnlyIn(Dist.CLIENT)
   public float getParam() {
      return this.param;
   }

   public static class State {
      private static final Int2ObjectMap<SChangeGameStatePacket.State> TYPES = new Int2ObjectOpenHashMap<>();
      private final int id;

      public State(int pId) {
         this.id = pId;
         TYPES.put(pId, this);
      }
   }
}
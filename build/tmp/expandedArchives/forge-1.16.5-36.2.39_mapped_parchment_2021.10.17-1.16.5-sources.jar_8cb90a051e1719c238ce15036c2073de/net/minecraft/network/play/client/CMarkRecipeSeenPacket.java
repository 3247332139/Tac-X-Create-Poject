package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.util.ResourceLocation;

public class CMarkRecipeSeenPacket implements IPacket<IServerPlayNetHandler> {
   private ResourceLocation recipe;

   public CMarkRecipeSeenPacket() {
   }

   public CMarkRecipeSeenPacket(IRecipe<?> pRecipe) {
      this.recipe = pRecipe.getId();
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.recipe = p_148837_1_.readResourceLocation();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeResourceLocation(this.recipe);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleRecipeBookSeenRecipePacket(this);
   }

   public ResourceLocation getRecipe() {
      return this.recipe;
   }
}
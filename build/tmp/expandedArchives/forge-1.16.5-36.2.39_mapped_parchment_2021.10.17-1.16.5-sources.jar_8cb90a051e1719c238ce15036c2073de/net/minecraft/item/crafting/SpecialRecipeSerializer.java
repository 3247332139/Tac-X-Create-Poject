package net.minecraft.item.crafting;

import com.google.gson.JsonObject;
import java.util.function.Function;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class SpecialRecipeSerializer<T extends IRecipe<?>> extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>>  implements IRecipeSerializer<T> {
   private final Function<ResourceLocation, T> constructor;

   public SpecialRecipeSerializer(Function<ResourceLocation, T> pConstructor) {
      this.constructor = pConstructor;
   }

   public T fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
      return this.constructor.apply(pRecipeId);
   }

   public T fromNetwork(ResourceLocation pRecipeId, PacketBuffer pBuffer) {
      return this.constructor.apply(pRecipeId);
   }

   public void toNetwork(PacketBuffer pBuffer, T pRecipe) {
   }
}

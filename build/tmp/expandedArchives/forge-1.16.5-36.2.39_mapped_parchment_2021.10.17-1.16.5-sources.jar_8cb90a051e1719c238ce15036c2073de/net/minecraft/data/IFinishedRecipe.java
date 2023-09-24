package net.minecraft.data;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public interface IFinishedRecipe {
   void serializeRecipeData(JsonObject pJson);

   /**
    * Gets the JSON for the recipe.
    */
   default JsonObject serializeRecipe() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("type", Registry.RECIPE_SERIALIZER.getKey(this.getType()).toString());
      this.serializeRecipeData(jsonobject);
      return jsonobject;
   }

   /**
    * Gets the ID for the recipe.
    */
   ResourceLocation getId();

   IRecipeSerializer<?> getType();

   /**
    * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
    */
   @Nullable
   JsonObject serializeAdvancement();

   /**
    * Gets the ID for the advancement associated with this recipe. Should not be null if {@link #getAdvancementJson} is
    * non-null.
    */
   @Nullable
   ResourceLocation getAdvancementId();
}
package net.minecraft.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.JSONUtils;

/**
 * LootItemFunction that merges a given CompoundTag into the stack's NBT tag.
 */
public class SetNBT extends LootFunction {
   private final CompoundNBT tag;

   private SetNBT(ILootCondition[] pConditions, CompoundNBT pTag) {
      super(pConditions);
      this.tag = pTag;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.SET_NBT;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      pStack.getOrCreateTag().merge(this.tag);
      return pStack;
   }

   public static LootFunction.Builder<?> setTag(CompoundNBT pTag) {
      return simpleBuilder((p_215951_1_) -> {
         return new SetNBT(p_215951_1_, pTag);
      });
   }

   public static class Serializer extends LootFunction.Serializer<SetNBT> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetNBT pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.addProperty("tag", pValue.tag.toString());
      }

      public SetNBT deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         try {
            CompoundNBT compoundnbt = JsonToNBT.parseTag(JSONUtils.getAsString(pObject, "tag"));
            return new SetNBT(pConditions, compoundnbt);
         } catch (CommandSyntaxException commandsyntaxexception) {
            throw new JsonSyntaxException(commandsyntaxexception.getMessage());
         }
      }
   }
}
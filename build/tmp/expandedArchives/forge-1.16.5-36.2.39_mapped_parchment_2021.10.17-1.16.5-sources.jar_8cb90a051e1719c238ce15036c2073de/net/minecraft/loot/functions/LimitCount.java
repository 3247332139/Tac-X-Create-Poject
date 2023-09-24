package net.minecraft.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.IntClamper;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;

/**
 * A LootItemFunction that limits the stack's count to fall within a given {@link IntRange}.
 */
public class LimitCount extends LootFunction {
   private final IntClamper limiter;

   private LimitCount(ILootCondition[] p_i51232_1_, IntClamper p_i51232_2_) {
      super(p_i51232_1_);
      this.limiter = p_i51232_2_;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.LIMIT_COUNT;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      int i = this.limiter.applyAsInt(pStack.getCount());
      pStack.setCount(i);
      return pStack;
   }

   public static LootFunction.Builder<?> limitCount(IntClamper p_215911_0_) {
      return simpleBuilder((p_215912_1_) -> {
         return new LimitCount(p_215912_1_, p_215911_0_);
      });
   }

   public static class Serializer extends LootFunction.Serializer<LimitCount> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, LimitCount pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.add("limit", pSerializationContext.serialize(pValue.limiter));
      }

      public LimitCount deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         IntClamper intclamper = JSONUtils.getAsObject(pObject, "limit", pDeserializationContext, IntClamper.class);
         return new LimitCount(pConditions, intclamper);
      }
   }
}
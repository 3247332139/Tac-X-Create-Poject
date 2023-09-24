package net.minecraft.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;

/**
 * LootItemFunction that reduces a stack's count based on the {@linkplain LootContextParams#EXPLOSION_RADIUS explosion
 * radius}.
 */
public class ExplosionDecay extends LootFunction {
   private ExplosionDecay(ILootCondition[] p_i51244_1_) {
      super(p_i51244_1_);
   }

   public LootFunctionType getType() {
      return LootFunctionManager.EXPLOSION_DECAY;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      Float f = pContext.getParamOrNull(LootParameters.EXPLOSION_RADIUS);
      if (f != null) {
         Random random = pContext.getRandom();
         float f1 = 1.0F / f;
         int i = pStack.getCount();
         int j = 0;

         for(int k = 0; k < i; ++k) {
            if (random.nextFloat() <= f1) {
               ++j;
            }
         }

         pStack.setCount(j);
      }

      return pStack;
   }

   public static LootFunction.Builder<?> explosionDecay() {
      return simpleBuilder(ExplosionDecay::new);
   }

   public static class Serializer extends LootFunction.Serializer<ExplosionDecay> {
      public ExplosionDecay deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         return new ExplosionDecay(pConditions);
      }
   }
}
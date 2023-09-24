package net.minecraft.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.util.JSONUtils;

/**
 * A LootItemCondition that succeeds with a given probability.
 */
public class RandomChance implements ILootCondition {
   private final float probability;

   private RandomChance(float pProbability) {
      this.probability = pProbability;
   }

   public LootConditionType getType() {
      return LootConditionManager.RANDOM_CHANCE;
   }

   public boolean test(LootContext p_test_1_) {
      return p_test_1_.getRandom().nextFloat() < this.probability;
   }

   public static ILootCondition.IBuilder randomChance(float pProbability) {
      return () -> {
         return new RandomChance(pProbability);
      };
   }

   public static class Serializer implements ILootSerializer<RandomChance> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, RandomChance pValue, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("chance", pValue.probability);
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public RandomChance deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         return new RandomChance(JSONUtils.getAsFloat(pJson, "chance"));
      }
   }
}
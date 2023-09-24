package net.minecraft.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.JSONUtils;

/**
 * A LootItemCondition that inverts the output of another one.
 */
public class Inverted implements ILootCondition {
   private final ILootCondition term;

   private Inverted(ILootCondition p_i51202_1_) {
      this.term = p_i51202_1_;
   }

   public LootConditionType getType() {
      return LootConditionManager.INVERTED;
   }

   public final boolean test(LootContext p_test_1_) {
      return !this.term.test(p_test_1_);
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return this.term.getReferencedContextParams();
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationTracker pContext) {
      ILootCondition.super.validate(pContext);
      this.term.validate(pContext);
   }

   public static ILootCondition.IBuilder invert(ILootCondition.IBuilder pToInvert) {
      Inverted inverted = new Inverted(pToInvert.build());
      return () -> {
         return inverted;
      };
   }

   public static class Serializer implements ILootSerializer<Inverted> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, Inverted pValue, JsonSerializationContext pSerializationContext) {
         pJson.add("term", pSerializationContext.serialize(pValue.term));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public Inverted deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         ILootCondition ilootcondition = JSONUtils.getAsObject(pJson, "term", pSerializationContext, ILootCondition.class);
         return new Inverted(ilootcondition);
      }
   }
}
package net.minecraft.advancements;

import com.google.gson.JsonObject;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public interface ICriterionInstance {
   ResourceLocation getCriterion();

   JsonObject serializeToJson(ConditionArraySerializer pConditions);
}
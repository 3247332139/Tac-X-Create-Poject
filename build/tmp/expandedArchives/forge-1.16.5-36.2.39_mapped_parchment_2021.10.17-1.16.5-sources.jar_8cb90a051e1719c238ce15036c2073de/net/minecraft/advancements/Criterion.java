package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class Criterion {
   private final ICriterionInstance trigger;

   public Criterion(ICriterionInstance p_i47470_1_) {
      this.trigger = p_i47470_1_;
   }

   public Criterion() {
      this.trigger = null;
   }

   public void serializeToNetwork(PacketBuffer pBuffer) {
   }

   public static Criterion criterionFromJson(JsonObject pJson, ConditionArrayParser pConditionParser) {
      ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pJson, "trigger"));
      ICriterionTrigger<?> icriteriontrigger = CriteriaTriggers.getCriterion(resourcelocation);
      if (icriteriontrigger == null) {
         throw new JsonSyntaxException("Invalid criterion trigger: " + resourcelocation);
      } else {
         ICriterionInstance icriterioninstance = icriteriontrigger.createInstance(JSONUtils.getAsJsonObject(pJson, "conditions", new JsonObject()), pConditionParser);
         return new Criterion(icriterioninstance);
      }
   }

   public static Criterion criterionFromNetwork(PacketBuffer pBuffer) {
      return new Criterion();
   }

   /**
    * Deserializes all criterions defined within a JSON object.
    */
   public static Map<String, Criterion> criteriaFromJson(JsonObject pJson, ConditionArrayParser pConditionParser) {
      Map<String, Criterion> map = Maps.newHashMap();

      for(Entry<String, JsonElement> entry : pJson.entrySet()) {
         map.put(entry.getKey(), criterionFromJson(JSONUtils.convertToJsonObject(entry.getValue(), "criterion"), pConditionParser));
      }

      return map;
   }

   /**
    * Read criteria from {@code buf}.
    * 
    * @return the read criteria. Each key-value pair consists of a {@code Criterion} and its name.
    * @see #serializeToNetwork(Map, PacketBuffer)
    */
   public static Map<String, Criterion> criteriaFromNetwork(PacketBuffer pBus) {
      Map<String, Criterion> map = Maps.newHashMap();
      int i = pBus.readVarInt();

      for(int j = 0; j < i; ++j) {
         map.put(pBus.readUtf(32767), criterionFromNetwork(pBus));
      }

      return map;
   }

   /**
    * Write {@code criteria} to {@code buf}.
    * 
    * @see #criteriaFromNetwork(PacketBuffer)
    */
   public static void serializeToNetwork(Map<String, Criterion> pCriteria, PacketBuffer pBuf) {
      pBuf.writeVarInt(pCriteria.size());

      for(Entry<String, Criterion> entry : pCriteria.entrySet()) {
         pBuf.writeUtf(entry.getKey());
         entry.getValue().serializeToNetwork(pBuf);
      }

   }

   @Nullable
   public ICriterionInstance getTrigger() {
      return this.trigger;
   }

   public JsonElement serializeToJson() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("trigger", this.trigger.getCriterion().toString());
      JsonObject jsonobject1 = this.trigger.serializeToJson(ConditionArraySerializer.INSTANCE);
      if (jsonobject1.size() != 0) {
         jsonobject.add("conditions", jsonobject1);
      }

      return jsonobject;
   }
}
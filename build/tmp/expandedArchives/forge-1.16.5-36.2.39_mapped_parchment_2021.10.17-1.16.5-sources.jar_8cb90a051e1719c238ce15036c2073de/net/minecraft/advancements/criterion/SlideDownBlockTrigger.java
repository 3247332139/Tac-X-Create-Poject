package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class SlideDownBlockTrigger extends AbstractCriterionTrigger<SlideDownBlockTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("slide_down_block");

   public ResourceLocation getId() {
      return ID;
   }

   public SlideDownBlockTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      Block block = deserializeBlock(pJson);
      StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(pJson.get("state"));
      if (block != null) {
         statepropertiespredicate.checkState(block.getStateDefinition(), (p_227148_1_) -> {
            throw new JsonSyntaxException("Block " + block + " has no property " + p_227148_1_);
         });
      }

      return new SlideDownBlockTrigger.Instance(pEntityPredicate, block, statepropertiespredicate);
   }

   @Nullable
   private static Block deserializeBlock(JsonObject pObject) {
      if (pObject.has("block")) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pObject, "block"));
         return Registry.BLOCK.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block type '" + resourcelocation + "'");
         });
      } else {
         return null;
      }
   }

   public void trigger(ServerPlayerEntity pPlayer, BlockState pState) {
      this.trigger(pPlayer, (p_227149_1_) -> {
         return p_227149_1_.matches(pState);
      });
   }

   public static class Instance extends CriterionInstance {
      private final Block block;
      private final StatePropertiesPredicate state;

      public Instance(EntityPredicate.AndPredicate p_i231896_1_, @Nullable Block p_i231896_2_, StatePropertiesPredicate p_i231896_3_) {
         super(SlideDownBlockTrigger.ID, p_i231896_1_);
         this.block = p_i231896_2_;
         this.state = p_i231896_3_;
      }

      public static SlideDownBlockTrigger.Instance slidesDownBlock(Block pBlock) {
         return new SlideDownBlockTrigger.Instance(EntityPredicate.AndPredicate.ANY, pBlock, StatePropertiesPredicate.ANY);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         if (this.block != null) {
            jsonobject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
         }

         jsonobject.add("state", this.state.serializeToJson());
         return jsonobject;
      }

      public boolean matches(BlockState pState) {
         if (this.block != null && !pState.is(this.block)) {
            return false;
         } else {
            return this.state.matches(pState);
         }
      }
   }
}
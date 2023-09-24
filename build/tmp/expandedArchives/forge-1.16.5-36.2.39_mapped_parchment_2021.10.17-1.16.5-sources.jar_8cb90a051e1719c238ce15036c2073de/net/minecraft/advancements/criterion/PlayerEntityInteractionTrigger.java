package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class PlayerEntityInteractionTrigger extends AbstractCriterionTrigger<PlayerEntityInteractionTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("player_interacted_with_entity");

   public ResourceLocation getId() {
      return ID;
   }

   protected PlayerEntityInteractionTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pJson, "entity", pConditionsParser);
      return new PlayerEntityInteractionTrigger.Instance(pEntityPredicate, itempredicate, entitypredicate$andpredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, ItemStack pStack, Entity pEntity) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_241475_2_) -> {
         return p_241475_2_.matches(pStack, lootcontext);
      });
   }

   public static class Instance extends CriterionInstance {
      private final ItemPredicate item;
      private final EntityPredicate.AndPredicate entity;

      public Instance(EntityPredicate.AndPredicate p_i241240_1_, ItemPredicate p_i241240_2_, EntityPredicate.AndPredicate p_i241240_3_) {
         super(PlayerEntityInteractionTrigger.ID, p_i241240_1_);
         this.item = p_i241240_2_;
         this.entity = p_i241240_3_;
      }

      public static PlayerEntityInteractionTrigger.Instance itemUsedOnEntity(EntityPredicate.AndPredicate pPlayer, ItemPredicate.Builder pStack, EntityPredicate.AndPredicate pEntity) {
         return new PlayerEntityInteractionTrigger.Instance(pPlayer, pStack.build(), pEntity);
      }

      public boolean matches(ItemStack pStack, LootContext pContext) {
         return !this.item.matches(pStack) ? false : this.entity.matches(pContext);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(pConditions));
         return jsonobject;
      }
   }
}
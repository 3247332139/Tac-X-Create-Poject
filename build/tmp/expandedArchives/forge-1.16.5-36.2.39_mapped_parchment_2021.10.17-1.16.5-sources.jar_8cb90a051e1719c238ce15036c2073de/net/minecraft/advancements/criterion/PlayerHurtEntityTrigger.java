package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class PlayerHurtEntityTrigger extends AbstractCriterionTrigger<PlayerHurtEntityTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");

   public ResourceLocation getId() {
      return ID;
   }

   public PlayerHurtEntityTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      DamagePredicate damagepredicate = DamagePredicate.fromJson(pJson.get("damage"));
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pJson, "entity", pConditionsParser);
      return new PlayerHurtEntityTrigger.Instance(pEntityPredicate, damagepredicate, entitypredicate$andpredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, Entity pEntity, DamageSource pSource, float pAmountDealt, float pAmountTaken, boolean pBlocked) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_226956_6_) -> {
         return p_226956_6_.matches(pPlayer, lootcontext, pSource, pAmountDealt, pAmountTaken, pBlocked);
      });
   }

   public static class Instance extends CriterionInstance {
      private final DamagePredicate damage;
      private final EntityPredicate.AndPredicate entity;

      public Instance(EntityPredicate.AndPredicate p_i241190_1_, DamagePredicate p_i241190_2_, EntityPredicate.AndPredicate p_i241190_3_) {
         super(PlayerHurtEntityTrigger.ID, p_i241190_1_);
         this.damage = p_i241190_2_;
         this.entity = p_i241190_3_;
      }

      public static PlayerHurtEntityTrigger.Instance playerHurtEntity(DamagePredicate.Builder pBuilder) {
         return new PlayerHurtEntityTrigger.Instance(EntityPredicate.AndPredicate.ANY, pBuilder.build(), EntityPredicate.AndPredicate.ANY);
      }

      public boolean matches(ServerPlayerEntity pPlayer, LootContext pContext, DamageSource pDamage, float pDealt, float pTaken, boolean pBlocked) {
         if (!this.damage.matches(pPlayer, pDamage, pDealt, pTaken, pBlocked)) {
            return false;
         } else {
            return this.entity.matches(pContext);
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("damage", this.damage.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(pConditions));
         return jsonobject;
      }
   }
}
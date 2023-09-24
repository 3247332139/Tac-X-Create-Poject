package net.minecraft.advancements.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.JSONUtils;

public class DamagePredicate {
   public static final DamagePredicate ANY = DamagePredicate.Builder.damageInstance().build();
   private final MinMaxBounds.FloatBound dealtDamage;
   private final MinMaxBounds.FloatBound takenDamage;
   private final EntityPredicate sourceEntity;
   private final Boolean blocked;
   private final DamageSourcePredicate type;

   public DamagePredicate() {
      this.dealtDamage = MinMaxBounds.FloatBound.ANY;
      this.takenDamage = MinMaxBounds.FloatBound.ANY;
      this.sourceEntity = EntityPredicate.ANY;
      this.blocked = null;
      this.type = DamageSourcePredicate.ANY;
   }

   public DamagePredicate(MinMaxBounds.FloatBound p_i49725_1_, MinMaxBounds.FloatBound p_i49725_2_, EntityPredicate p_i49725_3_, @Nullable Boolean p_i49725_4_, DamageSourcePredicate p_i49725_5_) {
      this.dealtDamage = p_i49725_1_;
      this.takenDamage = p_i49725_2_;
      this.sourceEntity = p_i49725_3_;
      this.blocked = p_i49725_4_;
      this.type = p_i49725_5_;
   }

   public boolean matches(ServerPlayerEntity pPlayer, DamageSource pSource, float pDealt, float pTaken, boolean pBlocked) {
      if (this == ANY) {
         return true;
      } else if (!this.dealtDamage.matches(pDealt)) {
         return false;
      } else if (!this.takenDamage.matches(pTaken)) {
         return false;
      } else if (!this.sourceEntity.matches(pPlayer, pSource.getEntity())) {
         return false;
      } else if (this.blocked != null && this.blocked != pBlocked) {
         return false;
      } else {
         return this.type.matches(pPlayer, pSource);
      }
   }

   public static DamagePredicate fromJson(@Nullable JsonElement pElement) {
      if (pElement != null && !pElement.isJsonNull()) {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "damage");
         MinMaxBounds.FloatBound minmaxbounds$floatbound = MinMaxBounds.FloatBound.fromJson(jsonobject.get("dealt"));
         MinMaxBounds.FloatBound minmaxbounds$floatbound1 = MinMaxBounds.FloatBound.fromJson(jsonobject.get("taken"));
         Boolean obool = jsonobject.has("blocked") ? JSONUtils.getAsBoolean(jsonobject, "blocked") : null;
         EntityPredicate entitypredicate = EntityPredicate.fromJson(jsonobject.get("source_entity"));
         DamageSourcePredicate damagesourcepredicate = DamageSourcePredicate.fromJson(jsonobject.get("type"));
         return new DamagePredicate(minmaxbounds$floatbound, minmaxbounds$floatbound1, entitypredicate, obool, damagesourcepredicate);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("dealt", this.dealtDamage.serializeToJson());
         jsonobject.add("taken", this.takenDamage.serializeToJson());
         jsonobject.add("source_entity", this.sourceEntity.serializeToJson());
         jsonobject.add("type", this.type.serializeToJson());
         if (this.blocked != null) {
            jsonobject.addProperty("blocked", this.blocked);
         }

         return jsonobject;
      }
   }

   public static class Builder {
      private MinMaxBounds.FloatBound dealtDamage = MinMaxBounds.FloatBound.ANY;
      private MinMaxBounds.FloatBound takenDamage = MinMaxBounds.FloatBound.ANY;
      private EntityPredicate sourceEntity = EntityPredicate.ANY;
      private Boolean blocked;
      private DamageSourcePredicate type = DamageSourcePredicate.ANY;

      public static DamagePredicate.Builder damageInstance() {
         return new DamagePredicate.Builder();
      }

      public DamagePredicate.Builder blocked(Boolean pBlocked) {
         this.blocked = pBlocked;
         return this;
      }

      public DamagePredicate.Builder type(DamageSourcePredicate.Builder pDamageType) {
         this.type = pDamageType.build();
         return this;
      }

      public DamagePredicate build() {
         return new DamagePredicate(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
      }
   }
}
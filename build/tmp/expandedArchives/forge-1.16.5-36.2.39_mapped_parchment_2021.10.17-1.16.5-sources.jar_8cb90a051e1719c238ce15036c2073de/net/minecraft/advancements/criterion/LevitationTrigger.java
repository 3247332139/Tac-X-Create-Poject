package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class LevitationTrigger extends AbstractCriterionTrigger<LevitationTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("levitation");

   public ResourceLocation getId() {
      return ID;
   }

   public LevitationTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      DistancePredicate distancepredicate = DistancePredicate.fromJson(pJson.get("distance"));
      MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(pJson.get("duration"));
      return new LevitationTrigger.Instance(pEntityPredicate, distancepredicate, minmaxbounds$intbound);
   }

   public void trigger(ServerPlayerEntity pPlayer, Vector3d pStartPos, int pDuration) {
      this.trigger(pPlayer, (p_226852_3_) -> {
         return p_226852_3_.matches(pPlayer, pStartPos, pDuration);
      });
   }

   public static class Instance extends CriterionInstance {
      private final DistancePredicate distance;
      private final MinMaxBounds.IntBound duration;

      public Instance(EntityPredicate.AndPredicate p_i231638_1_, DistancePredicate p_i231638_2_, MinMaxBounds.IntBound p_i231638_3_) {
         super(LevitationTrigger.ID, p_i231638_1_);
         this.distance = p_i231638_2_;
         this.duration = p_i231638_3_;
      }

      public static LevitationTrigger.Instance levitated(DistancePredicate pDistance) {
         return new LevitationTrigger.Instance(EntityPredicate.AndPredicate.ANY, pDistance, MinMaxBounds.IntBound.ANY);
      }

      public boolean matches(ServerPlayerEntity pPlayer, Vector3d pStartPos, int pDuration) {
         if (!this.distance.matches(pStartPos.x, pStartPos.y, pStartPos.z, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ())) {
            return false;
         } else {
            return this.duration.matches(pDuration);
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("distance", this.distance.serializeToJson());
         jsonobject.add("duration", this.duration.serializeToJson());
         return jsonobject;
      }
   }
}
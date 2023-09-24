package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class NetherTravelTrigger extends AbstractCriterionTrigger<NetherTravelTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("nether_travel");

   public ResourceLocation getId() {
      return ID;
   }

   public NetherTravelTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      LocationPredicate locationpredicate = LocationPredicate.fromJson(pJson.get("entered"));
      LocationPredicate locationpredicate1 = LocationPredicate.fromJson(pJson.get("exited"));
      DistancePredicate distancepredicate = DistancePredicate.fromJson(pJson.get("distance"));
      return new NetherTravelTrigger.Instance(pEntityPredicate, locationpredicate, locationpredicate1, distancepredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, Vector3d pEnteredNetherPosition) {
      this.trigger(pPlayer, (p_226945_2_) -> {
         return p_226945_2_.matches(pPlayer.getLevel(), pEnteredNetherPosition, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
      });
   }

   public static class Instance extends CriterionInstance {
      private final LocationPredicate entered;
      private final LocationPredicate exited;
      private final DistancePredicate distance;

      public Instance(EntityPredicate.AndPredicate p_i231785_1_, LocationPredicate p_i231785_2_, LocationPredicate p_i231785_3_, DistancePredicate p_i231785_4_) {
         super(NetherTravelTrigger.ID, p_i231785_1_);
         this.entered = p_i231785_2_;
         this.exited = p_i231785_3_;
         this.distance = p_i231785_4_;
      }

      public static NetherTravelTrigger.Instance travelledThroughNether(DistancePredicate pDistance) {
         return new NetherTravelTrigger.Instance(EntityPredicate.AndPredicate.ANY, LocationPredicate.ANY, LocationPredicate.ANY, pDistance);
      }

      public boolean matches(ServerWorld pLevel, Vector3d pEnteredNetherPosition, double pX, double pY, double pZ) {
         if (!this.entered.matches(pLevel, pEnteredNetherPosition.x, pEnteredNetherPosition.y, pEnteredNetherPosition.z)) {
            return false;
         } else if (!this.exited.matches(pLevel, pX, pY, pZ)) {
            return false;
         } else {
            return this.distance.matches(pEnteredNetherPosition.x, pEnteredNetherPosition.y, pEnteredNetherPosition.z, pX, pY, pZ);
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("entered", this.entered.serializeToJson());
         jsonobject.add("exited", this.exited.serializeToJson());
         jsonobject.add("distance", this.distance.serializeToJson());
         return jsonobject;
      }
   }
}
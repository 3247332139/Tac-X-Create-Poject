package net.minecraft.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public interface IRideable {
   boolean boost();

   void travelWithInput(Vector3d pTravelVec);

   float getSteeringSpeed();

   default boolean travel(MobEntity pMount, BoostHelper pHelper, Vector3d p_233622_3_) {
      if (!pMount.isAlive()) {
         return false;
      } else {
         Entity entity = pMount.getPassengers().isEmpty() ? null : pMount.getPassengers().get(0);
         if (pMount.isVehicle() && pMount.canBeControlledByRider() && entity instanceof PlayerEntity) {
            pMount.yRot = entity.yRot;
            pMount.yRotO = pMount.yRot;
            pMount.xRot = entity.xRot * 0.5F;
            pMount.setRot(pMount.yRot, pMount.xRot);
            pMount.yBodyRot = pMount.yRot;
            pMount.yHeadRot = pMount.yRot;
            pMount.maxUpStep = 1.0F;
            pMount.flyingSpeed = pMount.getSpeed() * 0.1F;
            if (pHelper.boosting && pHelper.boostTime++ > pHelper.boostTimeTotal) {
               pHelper.boosting = false;
            }

            if (pMount.isControlledByLocalInstance()) {
               float f = this.getSteeringSpeed();
               if (pHelper.boosting) {
                  f += f * 1.15F * MathHelper.sin((float)pHelper.boostTime / (float)pHelper.boostTimeTotal * (float)Math.PI);
               }

               pMount.setSpeed(f);
               this.travelWithInput(new Vector3d(0.0D, 0.0D, 1.0D));
               pMount.lerpSteps = 0;
            } else {
               pMount.calculateEntityAnimation(pMount, false);
               pMount.setDeltaMovement(Vector3d.ZERO);
            }

            return true;
         } else {
            pMount.maxUpStep = 0.5F;
            pMount.flyingSpeed = 0.02F;
            this.travelWithInput(p_233622_3_);
            return false;
         }
      }
   }
}
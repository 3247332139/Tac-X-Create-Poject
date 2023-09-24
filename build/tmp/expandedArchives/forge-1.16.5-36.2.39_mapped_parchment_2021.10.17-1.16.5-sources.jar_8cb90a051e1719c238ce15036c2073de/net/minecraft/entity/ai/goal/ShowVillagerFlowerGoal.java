package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;

public class ShowVillagerFlowerGoal extends Goal {
   private static final EntityPredicate OFFER_TARGER_CONTEXT = (new EntityPredicate()).range(6.0D).allowSameTeam().allowInvulnerable();
   private final IronGolemEntity golem;
   private VillagerEntity villager;
   private int tick;

   public ShowVillagerFlowerGoal(IronGolemEntity p_i1643_1_) {
      this.golem = p_i1643_1_;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (!this.golem.level.isDay()) {
         return false;
      } else if (this.golem.getRandom().nextInt(8000) != 0) {
         return false;
      } else {
         this.villager = this.golem.level.getNearestEntity(VillagerEntity.class, OFFER_TARGER_CONTEXT, this.golem, this.golem.getX(), this.golem.getY(), this.golem.getZ(), this.golem.getBoundingBox().inflate(6.0D, 2.0D, 6.0D));
         return this.villager != null;
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.tick > 0;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.tick = 400;
      this.golem.offerFlower(true);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.golem.offerFlower(false);
      this.villager = null;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      this.golem.getLookControl().setLookAt(this.villager, 30.0F, 30.0F);
      --this.tick;
   }
}
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.GroundPathHelper;

public class RestrictSunGoal extends Goal {
   private final CreatureEntity mob;

   public RestrictSunGoal(CreatureEntity p_i1652_1_) {
      this.mob = p_i1652_1_;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.mob.level.isDay() && this.mob.getItemBySlot(EquipmentSlotType.HEAD).isEmpty() && GroundPathHelper.hasGroundPathNavigation(this.mob);
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      ((GroundPathNavigator)this.mob.getNavigation()).setAvoidSun(true);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      if (GroundPathHelper.hasGroundPathNavigation(this.mob)) {
         ((GroundPathNavigator)this.mob.getNavigation()).setAvoidSun(false);
      }

   }
}
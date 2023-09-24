package net.minecraft.entity.ai.goal;

import net.minecraft.entity.MobEntity;

public class OpenDoorGoal extends InteractDoorGoal {
   private final boolean closeDoor;
   private int forgetTime;

   public OpenDoorGoal(MobEntity p_i1644_1_, boolean p_i1644_2_) {
      super(p_i1644_1_);
      this.mob = p_i1644_1_;
      this.closeDoor = p_i1644_2_;
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.closeDoor && this.forgetTime > 0 && super.canContinueToUse();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.forgetTime = 20;
      this.setOpen(true);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.setOpen(false);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      --this.forgetTime;
      super.tick();
   }
}
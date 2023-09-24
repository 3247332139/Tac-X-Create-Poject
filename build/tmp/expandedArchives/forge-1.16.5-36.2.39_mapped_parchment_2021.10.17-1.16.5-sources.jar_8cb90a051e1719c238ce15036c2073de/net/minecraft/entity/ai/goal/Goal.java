package net.minecraft.entity.ai.goal;

import java.util.EnumSet;

public abstract class Goal {
   private final EnumSet<Goal.Flag> flags = EnumSet.noneOf(Goal.Flag.class);

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public abstract boolean canUse();

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.canUse();
   }

   public boolean isInterruptable() {
      return true;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
   }

   public void setFlags(EnumSet<Goal.Flag> pFlagSet) {
      this.flags.clear();
      this.flags.addAll(pFlagSet);
   }

   public String toString() {
      return this.getClass().getSimpleName();
   }

   public EnumSet<Goal.Flag> getFlags() {
      return this.flags;
   }

   public static enum Flag {
      MOVE,
      LOOK,
      JUMP,
      TARGET;
   }
}
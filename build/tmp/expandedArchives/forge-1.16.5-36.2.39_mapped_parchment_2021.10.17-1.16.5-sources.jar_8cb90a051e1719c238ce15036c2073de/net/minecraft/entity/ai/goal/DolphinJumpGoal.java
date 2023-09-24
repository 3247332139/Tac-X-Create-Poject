package net.minecraft.entity.ai.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class DolphinJumpGoal extends JumpGoal {
   private static final int[] STEPS_TO_CHECK = new int[]{0, 1, 4, 5, 6, 7};
   private final DolphinEntity dolphin;
   private final int interval;
   private boolean breached;

   public DolphinJumpGoal(DolphinEntity p_i50329_1_, int p_i50329_2_) {
      this.dolphin = p_i50329_1_;
      this.interval = p_i50329_2_;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.dolphin.getRandom().nextInt(this.interval) != 0) {
         return false;
      } else {
         Direction direction = this.dolphin.getMotionDirection();
         int i = direction.getStepX();
         int j = direction.getStepZ();
         BlockPos blockpos = this.dolphin.blockPosition();

         for(int k : STEPS_TO_CHECK) {
            if (!this.waterIsClear(blockpos, i, j, k) || !this.surfaceIsClear(blockpos, i, j, k)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean waterIsClear(BlockPos pPos, int pDx, int pDz, int pScale) {
      BlockPos blockpos = pPos.offset(pDx * pScale, 0, pDz * pScale);
      return this.dolphin.level.getFluidState(blockpos).is(FluidTags.WATER) && !this.dolphin.level.getBlockState(blockpos).getMaterial().blocksMotion();
   }

   private boolean surfaceIsClear(BlockPos pPos, int pDx, int pDz, int pScale) {
      return this.dolphin.level.getBlockState(pPos.offset(pDx * pScale, 1, pDz * pScale)).isAir() && this.dolphin.level.getBlockState(pPos.offset(pDx * pScale, 2, pDz * pScale)).isAir();
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      double d0 = this.dolphin.getDeltaMovement().y;
      return (!(d0 * d0 < (double)0.03F) || this.dolphin.xRot == 0.0F || !(Math.abs(this.dolphin.xRot) < 10.0F) || !this.dolphin.isInWater()) && !this.dolphin.isOnGround();
   }

   public boolean isInterruptable() {
      return false;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      Direction direction = this.dolphin.getMotionDirection();
      this.dolphin.setDeltaMovement(this.dolphin.getDeltaMovement().add((double)direction.getStepX() * 0.6D, 0.7D, (double)direction.getStepZ() * 0.6D));
      this.dolphin.getNavigation().stop();
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.dolphin.xRot = 0.0F;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      boolean flag = this.breached;
      if (!flag) {
         FluidState fluidstate = this.dolphin.level.getFluidState(this.dolphin.blockPosition());
         this.breached = fluidstate.is(FluidTags.WATER);
      }

      if (this.breached && !flag) {
         this.dolphin.playSound(SoundEvents.DOLPHIN_JUMP, 1.0F, 1.0F);
      }

      Vector3d vector3d = this.dolphin.getDeltaMovement();
      if (vector3d.y * vector3d.y < (double)0.03F && this.dolphin.xRot != 0.0F) {
         this.dolphin.xRot = MathHelper.rotlerp(this.dolphin.xRot, 0.0F, 0.2F);
      } else {
         double d0 = Math.sqrt(Entity.getHorizontalDistanceSqr(vector3d));
         double d1 = Math.signum(-vector3d.y) * Math.acos(d0 / vector3d.length()) * (double)(180F / (float)Math.PI);
         this.dolphin.xRot = (float)d1;
      }

   }
}
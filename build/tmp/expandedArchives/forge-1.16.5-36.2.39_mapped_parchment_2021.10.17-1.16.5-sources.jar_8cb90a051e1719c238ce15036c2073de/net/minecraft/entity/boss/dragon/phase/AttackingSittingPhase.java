package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.SoundEvents;

public class AttackingSittingPhase extends SittingPhase {
   private int attackingTicks;

   public AttackingSittingPhase(EnderDragonEntity p_i46787_1_) {
      super(p_i46787_1_);
   }

   /**
    * Generates particle effects appropriate to the phase (or sometimes sounds).
    * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
    */
   public void doClientTick() {
      this.dragon.level.playLocalSound(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.dragon.getSoundSource(), 2.5F, 0.8F + this.dragon.getRandom().nextFloat() * 0.3F, false);
   }

   /**
    * Gives the phase a chance to update its status.
    * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
    */
   public void doServerTick() {
      if (this.attackingTicks++ >= 40) {
         this.dragon.getPhaseManager().setPhase(PhaseType.SITTING_FLAMING);
      }

   }

   /**
    * Called when this phase is set to active
    */
   public void begin() {
      this.attackingTicks = 0;
   }

   public PhaseType<AttackingSittingPhase> getPhase() {
      return PhaseType.SITTING_ATTACKING;
   }
}
package net.minecraft.dispenser;

public abstract class OptionalDispenseBehavior extends DefaultDispenseItemBehavior {
   private boolean success = true;

   public boolean isSuccess() {
      return this.success;
   }

   public void setSuccess(boolean pSuccess) {
      this.success = pSuccess;
   }

   /**
    * Play the dispense sound from the specified block.
    */
   protected void playSound(IBlockSource pSource) {
      pSource.getLevel().levelEvent(this.isSuccess() ? 1000 : 1001, pSource.getPos(), 0);
   }
}
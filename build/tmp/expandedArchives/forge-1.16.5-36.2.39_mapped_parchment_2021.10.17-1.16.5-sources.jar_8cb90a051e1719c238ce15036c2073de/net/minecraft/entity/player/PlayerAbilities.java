package net.minecraft.entity.player;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerAbilities {
   public boolean invulnerable;
   public boolean flying;
   public boolean mayfly;
   public boolean instabuild;
   public boolean mayBuild = true;
   private float flyingSpeed = 0.05F;
   private float walkingSpeed = 0.1F;

   public void addSaveData(CompoundNBT pTagCompound) {
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.putBoolean("invulnerable", this.invulnerable);
      compoundnbt.putBoolean("flying", this.flying);
      compoundnbt.putBoolean("mayfly", this.mayfly);
      compoundnbt.putBoolean("instabuild", this.instabuild);
      compoundnbt.putBoolean("mayBuild", this.mayBuild);
      compoundnbt.putFloat("flySpeed", this.flyingSpeed);
      compoundnbt.putFloat("walkSpeed", this.walkingSpeed);
      pTagCompound.put("abilities", compoundnbt);
   }

   public void loadSaveData(CompoundNBT pTagCompound) {
      if (pTagCompound.contains("abilities", 10)) {
         CompoundNBT compoundnbt = pTagCompound.getCompound("abilities");
         this.invulnerable = compoundnbt.getBoolean("invulnerable");
         this.flying = compoundnbt.getBoolean("flying");
         this.mayfly = compoundnbt.getBoolean("mayfly");
         this.instabuild = compoundnbt.getBoolean("instabuild");
         if (compoundnbt.contains("flySpeed", 99)) {
            this.flyingSpeed = compoundnbt.getFloat("flySpeed");
            this.walkingSpeed = compoundnbt.getFloat("walkSpeed");
         }

         if (compoundnbt.contains("mayBuild", 1)) {
            this.mayBuild = compoundnbt.getBoolean("mayBuild");
         }
      }

   }

   public float getFlyingSpeed() {
      return this.flyingSpeed;
   }

   @OnlyIn(Dist.CLIENT)
   public void setFlyingSpeed(float pSpeed) {
      this.flyingSpeed = pSpeed;
   }

   public float getWalkingSpeed() {
      return this.walkingSpeed;
   }

   @OnlyIn(Dist.CLIENT)
   public void setWalkingSpeed(float pSpeed) {
      this.walkingSpeed = pSpeed;
   }
}
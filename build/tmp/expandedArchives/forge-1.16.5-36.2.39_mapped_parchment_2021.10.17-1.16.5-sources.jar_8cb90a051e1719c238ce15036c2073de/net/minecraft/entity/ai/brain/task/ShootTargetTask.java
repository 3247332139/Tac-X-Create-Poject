package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.world.server.ServerWorld;

public class ShootTargetTask<E extends MobEntity & ICrossbowUser, T extends LivingEntity> extends Task<E> {
   private int attackDelay;
   private ShootTargetTask.Status crossbowState = ShootTargetTask.Status.UNCHARGED;

   public ShootTargetTask() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT), 1200);
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      LivingEntity livingentity = getAttackTarget(pOwner);
      return pOwner.isHolding(item -> item instanceof CrossbowItem) && BrainUtil.canSee(pOwner, livingentity) && BrainUtil.isWithinAttackRange(pOwner, livingentity, 0);
   }

   protected boolean canStillUse(ServerWorld pLevel, E pEntity, long pGameTime) {
      return pEntity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(pLevel, pEntity);
   }

   protected void tick(ServerWorld pLevel, E pOwner, long pGameTime) {
      LivingEntity livingentity = getAttackTarget(pOwner);
      this.lookAtTarget(pOwner, livingentity);
      this.crossbowAttack(pOwner, livingentity);
   }

   protected void stop(ServerWorld pLevel, E pEntity, long pGameTime) {
      if (pEntity.isUsingItem()) {
         pEntity.stopUsingItem();
      }

      if (pEntity.isHolding(item -> item instanceof CrossbowItem)) {
         pEntity.setChargingCrossbow(false);
         CrossbowItem.setCharged(pEntity.getUseItem(), false);
      }

   }

   private void crossbowAttack(E p_233888_1_, LivingEntity p_233888_2_) {
      if (this.crossbowState == ShootTargetTask.Status.UNCHARGED) {
         p_233888_1_.startUsingItem(ProjectileHelper.getWeaponHoldingHand(p_233888_1_, item -> item instanceof CrossbowItem));
         this.crossbowState = ShootTargetTask.Status.CHARGING;
         p_233888_1_.setChargingCrossbow(true);
      } else if (this.crossbowState == ShootTargetTask.Status.CHARGING) {
         if (!p_233888_1_.isUsingItem()) {
            this.crossbowState = ShootTargetTask.Status.UNCHARGED;
         }

         int i = p_233888_1_.getTicksUsingItem();
         ItemStack itemstack = p_233888_1_.getUseItem();
         if (i >= CrossbowItem.getChargeDuration(itemstack)) {
            p_233888_1_.releaseUsingItem();
            this.crossbowState = ShootTargetTask.Status.CHARGED;
            this.attackDelay = 20 + p_233888_1_.getRandom().nextInt(20);
            p_233888_1_.setChargingCrossbow(false);
         }
      } else if (this.crossbowState == ShootTargetTask.Status.CHARGED) {
         --this.attackDelay;
         if (this.attackDelay == 0) {
            this.crossbowState = ShootTargetTask.Status.READY_TO_ATTACK;
         }
      } else if (this.crossbowState == ShootTargetTask.Status.READY_TO_ATTACK) {
         p_233888_1_.performRangedAttack(p_233888_2_, 1.0F);
         ItemStack itemstack1 = p_233888_1_.getItemInHand(ProjectileHelper.getWeaponHoldingHand(p_233888_1_, item -> item instanceof CrossbowItem));
         CrossbowItem.setCharged(itemstack1, false);
         this.crossbowState = ShootTargetTask.Status.UNCHARGED;
      }

   }

   private void lookAtTarget(MobEntity p_233889_1_, LivingEntity p_233889_2_) {
      p_233889_1_.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(p_233889_2_, true));
   }

   private static LivingEntity getAttackTarget(LivingEntity p_233887_0_) {
      return p_233887_0_.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
   }

   static enum Status {
      UNCHARGED,
      CHARGING,
      CHARGED,
      READY_TO_ATTACK;
   }
}

package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.world.server.ServerWorld;

public class ShowWaresTask extends Task<VillagerEntity> {
   @Nullable
   private ItemStack playerItemStack;
   private final List<ItemStack> displayItems = Lists.newArrayList();
   private int cycleCounter;
   private int displayIndex;
   private int lookTime;

   public ShowWaresTask(int p_i50343_1_, int p_i50343_2_) {
      super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryModuleStatus.VALUE_PRESENT), p_i50343_1_, p_i50343_2_);
   }

   public boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      Brain<?> brain = pOwner.getBrain();
      if (!brain.getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent()) {
         return false;
      } else {
         LivingEntity livingentity = brain.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
         return livingentity.getType() == EntityType.PLAYER && pOwner.isAlive() && livingentity.isAlive() && !pOwner.isBaby() && pOwner.distanceToSqr(livingentity) <= 17.0D;
      }
   }

   public boolean canStillUse(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      return this.checkExtraStartConditions(pLevel, pEntity) && this.lookTime > 0 && pEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
   }

   public void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      super.start(pLevel, pEntity, pGameTime);
      this.lookAtTarget(pEntity);
      this.cycleCounter = 0;
      this.displayIndex = 0;
      this.lookTime = 40;
   }

   public void tick(ServerWorld pLevel, VillagerEntity pOwner, long pGameTime) {
      LivingEntity livingentity = this.lookAtTarget(pOwner);
      this.findItemsToDisplay(livingentity, pOwner);
      if (!this.displayItems.isEmpty()) {
         this.displayCyclingItems(pOwner);
      } else {
         pOwner.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
         this.lookTime = Math.min(this.lookTime, 40);
      }

      --this.lookTime;
   }

   public void stop(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      super.stop(pLevel, pEntity, pGameTime);
      pEntity.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
      pEntity.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
      this.playerItemStack = null;
   }

   private void findItemsToDisplay(LivingEntity p_220556_1_, VillagerEntity p_220556_2_) {
      boolean flag = false;
      ItemStack itemstack = p_220556_1_.getMainHandItem();
      if (this.playerItemStack == null || !ItemStack.isSame(this.playerItemStack, itemstack)) {
         this.playerItemStack = itemstack;
         flag = true;
         this.displayItems.clear();
      }

      if (flag && !this.playerItemStack.isEmpty()) {
         this.updateDisplayItems(p_220556_2_);
         if (!this.displayItems.isEmpty()) {
            this.lookTime = 900;
            this.displayFirstItem(p_220556_2_);
         }
      }

   }

   private void displayFirstItem(VillagerEntity p_220558_1_) {
      p_220558_1_.setItemSlot(EquipmentSlotType.MAINHAND, this.displayItems.get(0));
   }

   private void updateDisplayItems(VillagerEntity p_220555_1_) {
      for(MerchantOffer merchantoffer : p_220555_1_.getOffers()) {
         if (!merchantoffer.isOutOfStock() && this.playerItemStackMatchesCostOfOffer(merchantoffer)) {
            this.displayItems.add(merchantoffer.getResult());
         }
      }

   }

   private boolean playerItemStackMatchesCostOfOffer(MerchantOffer p_220554_1_) {
      return ItemStack.isSame(this.playerItemStack, p_220554_1_.getCostA()) || ItemStack.isSame(this.playerItemStack, p_220554_1_.getCostB());
   }

   private LivingEntity lookAtTarget(VillagerEntity p_220557_1_) {
      Brain<?> brain = p_220557_1_.getBrain();
      LivingEntity livingentity = brain.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(livingentity, true));
      return livingentity;
   }

   private void displayCyclingItems(VillagerEntity p_220553_1_) {
      if (this.displayItems.size() >= 2 && ++this.cycleCounter >= 40) {
         ++this.displayIndex;
         this.cycleCounter = 0;
         if (this.displayIndex > this.displayItems.size() - 1) {
            this.displayIndex = 0;
         }

         p_220553_1_.setItemSlot(EquipmentSlotType.MAINHAND, this.displayItems.get(this.displayIndex));
      }

   }
}
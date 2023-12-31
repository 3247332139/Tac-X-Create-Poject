package net.minecraft.entity.passive.horse;

import javax.annotation.Nullable;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ZombieHorseEntity extends AbstractHorseEntity {
   public ZombieHorseEntity(EntityType<? extends ZombieHorseEntity> p_i50233_1_, World p_i50233_2_) {
      super(p_i50233_1_, p_i50233_2_);
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0D).add(Attributes.MOVEMENT_SPEED, (double)0.2F);
   }

   protected void randomizeAttributes() {
      this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(this.generateRandomJumpStrength());
   }

   public CreatureAttribute getMobType() {
      return CreatureAttribute.UNDEAD;
   }

   protected SoundEvent getAmbientSound() {
      super.getAmbientSound();
      return SoundEvents.ZOMBIE_HORSE_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      super.getDeathSound();
      return SoundEvents.ZOMBIE_HORSE_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      super.getHurtSound(pDamageSource);
      return SoundEvents.ZOMBIE_HORSE_HURT;
   }

   @Nullable
   public AgeableEntity getBreedOffspring(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
      return EntityType.ZOMBIE_HORSE.create(p_241840_1_);
   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!this.isTamed()) {
         return ActionResultType.PASS;
      } else if (this.isBaby()) {
         return super.mobInteract(pPlayer, pHand);
      } else if (pPlayer.isSecondaryUseActive()) {
         this.openInventory(pPlayer);
         return ActionResultType.sidedSuccess(this.level.isClientSide);
      } else if (this.isVehicle()) {
         return super.mobInteract(pPlayer, pHand);
      } else {
         if (!itemstack.isEmpty()) {
            if (itemstack.getItem() == Items.SADDLE && !this.isSaddled()) {
               this.openInventory(pPlayer);
               return ActionResultType.sidedSuccess(this.level.isClientSide);
            }

            ActionResultType actionresulttype = itemstack.interactLivingEntity(pPlayer, this, pHand);
            if (actionresulttype.consumesAction()) {
               return actionresulttype;
            }
         }

         this.doPlayerRide(pPlayer);
         return ActionResultType.sidedSuccess(this.level.isClientSide);
      }
   }

   protected void addBehaviourGoals() {
   }
}
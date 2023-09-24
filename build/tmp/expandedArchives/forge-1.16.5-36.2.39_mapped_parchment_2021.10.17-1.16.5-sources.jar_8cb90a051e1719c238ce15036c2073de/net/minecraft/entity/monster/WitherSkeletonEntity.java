package net.minecraft.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

public class WitherSkeletonEntity extends AbstractSkeletonEntity {
   public WitherSkeletonEntity(EntityType<? extends WitherSkeletonEntity> p_i50187_1_, World p_i50187_2_) {
      super(p_i50187_1_, p_i50187_2_);
      this.setPathfindingMalus(PathNodeType.LAVA, 8.0F);
   }

   protected void registerGoals() {
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractPiglinEntity.class, true));
      super.registerGoals();
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.WITHER_SKELETON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.WITHER_SKELETON_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.WITHER_SKELETON_DEATH;
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.WITHER_SKELETON_STEP;
   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
      Entity entity = pSource.getEntity();
      if (entity instanceof CreeperEntity) {
         CreeperEntity creeperentity = (CreeperEntity)entity;
         if (creeperentity.canDropMobsSkull()) {
            creeperentity.increaseDroppedSkulls();
            this.spawnAtLocation(Items.WITHER_SKELETON_SKULL);
         }
      }

   }

   /**
    * Gives armor or weapon for entity based on given DifficultyInstance
    */
   protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
      this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.STONE_SWORD));
   }

   /**
    * Enchants Entity's current equipments based on given DifficultyInstance
    */
   protected void populateDefaultEquipmentEnchantments(DifficultyInstance pDifficulty) {
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      ILivingEntityData ilivingentitydata = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
      this.reassessWeaponGoal();
      return ilivingentitydata;
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return 2.1F;
   }

   public boolean doHurtTarget(Entity pEntity) {
      if (!super.doHurtTarget(pEntity)) {
         return false;
      } else {
         if (pEntity instanceof LivingEntity) {
            ((LivingEntity)pEntity).addEffect(new EffectInstance(Effects.WITHER, 200));
         }

         return true;
      }
   }

   /**
    * Fires an arrow
    */
   protected AbstractArrowEntity getArrow(ItemStack pArrowStack, float pDistanceFactor) {
      AbstractArrowEntity abstractarrowentity = super.getArrow(pArrowStack, pDistanceFactor);
      abstractarrowentity.setSecondsOnFire(100);
      return abstractarrowentity;
   }

   public boolean canBeAffected(EffectInstance pPotioneffect) {
      return pPotioneffect.getEffect() == Effects.WITHER ? false : super.canBeAffected(pPotioneffect);
   }
}
package net.minecraft.entity.monster;

import com.google.common.collect.Maps;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.DamageSource;
import net.minecraft.util.GroundPathHelper;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VindicatorEntity extends AbstractIllagerEntity {
   private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = (p_213678_0_) -> {
      return p_213678_0_ == Difficulty.NORMAL || p_213678_0_ == Difficulty.HARD;
   };
   private boolean isJohnny;

   public VindicatorEntity(EntityType<? extends VindicatorEntity> p_i50189_1_, World p_i50189_2_) {
      super(p_i50189_1_, p_i50189_2_);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new SwimGoal(this));
      this.goalSelector.addGoal(1, new VindicatorEntity.BreakDoorGoal(this));
      this.goalSelector.addGoal(2, new AbstractIllagerEntity.RaidOpenDoorGoal(this));
      this.goalSelector.addGoal(3, new AbstractRaiderEntity.FindTargetGoal(this, 10.0F));
      this.goalSelector.addGoal(4, new VindicatorEntity.AttackGoal(this));
      this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, AbstractRaiderEntity.class)).setAlertOthers());
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
      this.targetSelector.addGoal(4, new VindicatorEntity.JohnnyAttackGoal(this));
      this.goalSelector.addGoal(8, new RandomWalkingGoal(this, 0.6D));
      this.goalSelector.addGoal(9, new LookAtGoal(this, PlayerEntity.class, 3.0F, 1.0F));
      this.goalSelector.addGoal(10, new LookAtGoal(this, MobEntity.class, 8.0F));
   }

   protected void customServerAiStep() {
      if (!this.isNoAi() && GroundPathHelper.hasGroundPathNavigation(this)) {
         boolean flag = ((ServerWorld)this.level).isRaided(this.blockPosition());
         ((GroundPathNavigator)this.getNavigation()).setCanOpenDoors(flag);
      }

      super.customServerAiStep();
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MonsterEntity.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.35F).add(Attributes.FOLLOW_RANGE, 12.0D).add(Attributes.MAX_HEALTH, 24.0D).add(Attributes.ATTACK_DAMAGE, 5.0D);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.isJohnny) {
         pCompound.putBoolean("Johnny", true);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public AbstractIllagerEntity.ArmPose getArmPose() {
      if (this.isAggressive()) {
         return AbstractIllagerEntity.ArmPose.ATTACKING;
      } else {
         return this.isCelebrating() ? AbstractIllagerEntity.ArmPose.CELEBRATING : AbstractIllagerEntity.ArmPose.CROSSED;
      }
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("Johnny", 99)) {
         this.isJohnny = pCompound.getBoolean("Johnny");
      }

   }

   public SoundEvent getCelebrateSound() {
      return SoundEvents.VINDICATOR_CELEBRATE;
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      ILivingEntityData ilivingentitydata = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      ((GroundPathNavigator)this.getNavigation()).setCanOpenDoors(true);
      this.populateDefaultEquipmentSlots(pDifficulty);
      this.populateDefaultEquipmentEnchantments(pDifficulty);
      return ilivingentitydata;
   }

   /**
    * Gives armor or weapon for entity based on given DifficultyInstance
    */
   protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
      if (this.getCurrentRaid() == null) {
         this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.IRON_AXE));
      }

   }

   /**
    * Returns whether this Entity is on the same team as the given Entity.
    */
   public boolean isAlliedTo(Entity pEntity) {
      if (super.isAlliedTo(pEntity)) {
         return true;
      } else if (pEntity instanceof LivingEntity && ((LivingEntity)pEntity).getMobType() == CreatureAttribute.ILLAGER) {
         return this.getTeam() == null && pEntity.getTeam() == null;
      } else {
         return false;
      }
   }

   public void setCustomName(@Nullable ITextComponent pName) {
      super.setCustomName(pName);
      if (!this.isJohnny && pName != null && pName.getString().equals("Johnny")) {
         this.isJohnny = true;
      }

   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.VINDICATOR_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.VINDICATOR_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.VINDICATOR_HURT;
   }

   public void applyRaidBuffs(int pWave, boolean p_213660_2_) {
      ItemStack itemstack = new ItemStack(Items.IRON_AXE);
      Raid raid = this.getCurrentRaid();
      int i = 1;
      if (pWave > raid.getNumGroups(Difficulty.NORMAL)) {
         i = 2;
      }

      boolean flag = this.random.nextFloat() <= raid.getEnchantOdds();
      if (flag) {
         Map<Enchantment, Integer> map = Maps.newHashMap();
         map.put(Enchantments.SHARPNESS, i);
         EnchantmentHelper.setEnchantments(map, itemstack);
      }

      this.setItemSlot(EquipmentSlotType.MAINHAND, itemstack);
   }

   class AttackGoal extends MeleeAttackGoal {
      public AttackGoal(VindicatorEntity p_i50577_2_) {
         super(p_i50577_2_, 1.0D, false);
      }

      protected double getAttackReachSqr(LivingEntity pAttackTarget) {
         if (this.mob.getVehicle() instanceof RavagerEntity) {
            float f = this.mob.getVehicle().getBbWidth() - 0.1F;
            return (double)(f * 2.0F * f * 2.0F + pAttackTarget.getBbWidth());
         } else {
            return super.getAttackReachSqr(pAttackTarget);
         }
      }
   }

   static class BreakDoorGoal extends net.minecraft.entity.ai.goal.BreakDoorGoal {
      public BreakDoorGoal(MobEntity p_i50578_1_) {
         super(p_i50578_1_, 6, VindicatorEntity.DOOR_BREAKING_PREDICATE);
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         VindicatorEntity vindicatorentity = (VindicatorEntity)this.mob;
         return vindicatorentity.hasActiveRaid() && super.canContinueToUse();
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         VindicatorEntity vindicatorentity = (VindicatorEntity)this.mob;
         return vindicatorentity.hasActiveRaid() && vindicatorentity.random.nextInt(10) == 0 && super.canUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         this.mob.setNoActionTime(0);
      }
   }

   static class JohnnyAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
      public JohnnyAttackGoal(VindicatorEntity p_i47345_1_) {
         super(p_i47345_1_, LivingEntity.class, 0, true, true, LivingEntity::attackable);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return ((VindicatorEntity)this.mob).isJohnny && super.canUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         this.mob.setNoActionTime(0);
      }
   }
}
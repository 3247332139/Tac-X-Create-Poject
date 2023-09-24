package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.DefendVillageTargetGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PatrolVillageGoal;
import net.minecraft.entity.ai.goal.ResetAngerGoal;
import net.minecraft.entity.ai.goal.ReturnToVillageGoal;
import net.minecraft.entity.ai.goal.ShowVillagerFlowerGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TickRangeConverter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IronGolemEntity extends GolemEntity implements IAngerable {
   protected static final DataParameter<Byte> DATA_FLAGS_ID = EntityDataManager.defineId(IronGolemEntity.class, DataSerializers.BYTE);
   private int attackAnimationTick;
   private int offerFlowerTick;
   private static final RangedInteger PERSISTENT_ANGER_TIME = TickRangeConverter.rangeOfSeconds(20, 39);
   private int remainingPersistentAngerTime;
   private UUID persistentAngerTarget;

   public IronGolemEntity(EntityType<? extends IronGolemEntity> p_i50267_1_, World p_i50267_2_) {
      super(p_i50267_1_, p_i50267_2_);
      this.maxUpStep = 1.0F;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
      this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
      this.goalSelector.addGoal(2, new ReturnToVillageGoal(this, 0.6D, false));
      this.goalSelector.addGoal(4, new PatrolVillageGoal(this, 0.6D));
      this.goalSelector.addGoal(5, new ShowVillagerFlowerGoal(this));
      this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
      this.targetSelector.addGoal(1, new DefendVillageTargetGoal(this));
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::isAngryAt));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, MobEntity.class, 5, false, false, (p_234199_0_) -> {
         return p_234199_0_ instanceof IMob && !(p_234199_0_ instanceof CreeperEntity);
      }));
      this.targetSelector.addGoal(4, new ResetAngerGoal<>(this, false));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_FLAGS_ID, (byte)0);
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 100.0D).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D).add(Attributes.ATTACK_DAMAGE, 15.0D);
   }

   /**
    * Decrements the entity's air supply when underwater
    */
   protected int decreaseAirSupply(int pAir) {
      return pAir;
   }

   protected void doPush(Entity pEntity) {
      if (pEntity instanceof IMob && !(pEntity instanceof CreeperEntity) && this.getRandom().nextInt(20) == 0) {
         this.setTarget((LivingEntity)pEntity);
      }

      super.doPush(pEntity);
   }

   /**
    * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
    * use this to react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

      if (this.offerFlowerTick > 0) {
         --this.offerFlowerTick;
      }

      if (getHorizontalDistanceSqr(this.getDeltaMovement()) > (double)2.5000003E-7F && this.random.nextInt(5) == 0) {
         int i = MathHelper.floor(this.getX());
         int j = MathHelper.floor(this.getY() - (double)0.2F);
         int k = MathHelper.floor(this.getZ());
         BlockPos pos = new BlockPos(i, j, k);
         BlockState blockstate = this.level.getBlockState(pos);
         if (!blockstate.isAir(this.level, pos)) {
            this.level.addParticle(new BlockParticleData(ParticleTypes.BLOCK, blockstate).setPos(pos), this.getX() + ((double)this.random.nextFloat() - 0.5D) * (double)this.getBbWidth(), this.getY() + 0.1D, this.getZ() + ((double)this.random.nextFloat() - 0.5D) * (double)this.getBbWidth(), 4.0D * ((double)this.random.nextFloat() - 0.5D), 0.5D, ((double)this.random.nextFloat() - 0.5D) * 4.0D);
         }
      }

      if (!this.level.isClientSide) {
         this.updatePersistentAnger((ServerWorld)this.level, true);
      }

   }

   public boolean canAttackType(EntityType<?> pType) {
      if (this.isPlayerCreated() && pType == EntityType.PLAYER) {
         return false;
      } else {
         return pType == EntityType.CREEPER ? false : super.canAttackType(pType);
      }
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("PlayerCreated", this.isPlayerCreated());
      this.addPersistentAngerSaveData(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setPlayerCreated(pCompound.getBoolean("PlayerCreated"));
      if(!level.isClientSide) //FORGE: allow this entity to be read from nbt on client. (Fixes MC-189565)
      this.readPersistentAngerSaveData((ServerWorld)this.level, pCompound);
   }

   public void startPersistentAngerTimer() {
      this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.randomValue(this.random));
   }

   public void setRemainingPersistentAngerTime(int pTime) {
      this.remainingPersistentAngerTime = pTime;
   }

   public int getRemainingPersistentAngerTime() {
      return this.remainingPersistentAngerTime;
   }

   public void setPersistentAngerTarget(@Nullable UUID pTarget) {
      this.persistentAngerTarget = pTarget;
   }

   public UUID getPersistentAngerTarget() {
      return this.persistentAngerTarget;
   }

   private float getAttackDamage() {
      return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
   }

   public boolean doHurtTarget(Entity pEntity) {
      this.attackAnimationTick = 10;
      this.level.broadcastEntityEvent(this, (byte)4);
      float f = this.getAttackDamage();
      float f1 = (int)f > 0 ? f / 2.0F + (float)this.random.nextInt((int)f) : f;
      boolean flag = pEntity.hurt(DamageSource.mobAttack(this), f1);
      if (flag) {
         pEntity.setDeltaMovement(pEntity.getDeltaMovement().add(0.0D, (double)0.4F, 0.0D));
         this.doEnchantDamageEffects(this, pEntity);
      }

      this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      return flag;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      IronGolemEntity.Cracks irongolementity$cracks = this.getCrackiness();
      boolean flag = super.hurt(pSource, pAmount);
      if (flag && this.getCrackiness() != irongolementity$cracks) {
         this.playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
      }

      return flag;
   }

   public IronGolemEntity.Cracks getCrackiness() {
      return IronGolemEntity.Cracks.byFraction(this.getHealth() / this.getMaxHealth());
   }

   /**
    * Handler for {@link World#setEntityState}
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 4) {
         this.attackAnimationTick = 10;
         this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      } else if (pId == 11) {
         this.offerFlowerTick = 400;
      } else if (pId == 34) {
         this.offerFlowerTick = 0;
      } else {
         super.handleEntityEvent(pId);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public void offerFlower(boolean pHoldingRose) {
      if (pHoldingRose) {
         this.offerFlowerTick = 400;
         this.level.broadcastEntityEvent(this, (byte)11);
      } else {
         this.offerFlowerTick = 0;
         this.level.broadcastEntityEvent(this, (byte)34);
      }

   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.IRON_GOLEM_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.IRON_GOLEM_DEATH;
   }

   protected ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      Item item = itemstack.getItem();
      if (item != Items.IRON_INGOT) {
         return ActionResultType.PASS;
      } else {
         float f = this.getHealth();
         this.heal(25.0F);
         if (this.getHealth() == f) {
            return ActionResultType.PASS;
         } else {
            float f1 = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, f1);
            if (!pPlayer.abilities.instabuild) {
               itemstack.shrink(1);
            }

            return ActionResultType.sidedSuccess(this.level.isClientSide);
         }
      }
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public int getOfferFlowerTick() {
      return this.offerFlowerTick;
   }

   public boolean isPlayerCreated() {
      return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
   }

   public void setPlayerCreated(boolean pPlayerCreated) {
      byte b0 = this.entityData.get(DATA_FLAGS_ID);
      if (pPlayerCreated) {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 1));
      } else {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -2));
      }

   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      super.die(pCause);
   }

   public boolean checkSpawnObstruction(IWorldReader pLevel) {
      BlockPos blockpos = this.blockPosition();
      BlockPos blockpos1 = blockpos.below();
      BlockState blockstate = pLevel.getBlockState(blockpos1);
      if (!blockstate.entityCanStandOn(pLevel, blockpos1, this)) {
         return false;
      } else {
         for(int i = 1; i < 3; ++i) {
            BlockPos blockpos2 = blockpos.above(i);
            BlockState blockstate1 = pLevel.getBlockState(blockpos2);
            if (!WorldEntitySpawner.isValidEmptySpawnBlock(pLevel, blockpos2, blockstate1, blockstate1.getFluidState(), EntityType.IRON_GOLEM)) {
               return false;
            }
         }

         return WorldEntitySpawner.isValidEmptySpawnBlock(pLevel, blockpos, pLevel.getBlockState(blockpos), Fluids.EMPTY.defaultFluidState(), EntityType.IRON_GOLEM) && pLevel.isUnobstructed(this);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getLeashOffset() {
      return new Vector3d(0.0D, (double)(0.875F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }

   public static enum Cracks {
      NONE(1.0F),
      LOW(0.75F),
      MEDIUM(0.5F),
      HIGH(0.25F);

      private static final List<IronGolemEntity.Cracks> BY_DAMAGE = Stream.of(values()).sorted(Comparator.comparingDouble((p_226516_0_) -> {
         return (double)p_226516_0_.fraction;
      })).collect(ImmutableList.toImmutableList());
      private final float fraction;

      private Cracks(float p_i225732_3_) {
         this.fraction = p_i225732_3_;
      }

      public static IronGolemEntity.Cracks byFraction(float p_226515_0_) {
         for(IronGolemEntity.Cracks irongolementity$cracks : BY_DAMAGE) {
            if (p_226515_0_ < irongolementity$cracks.fraction) {
               return irongolementity$cracks;
            }
         }

         return NONE;
      }
   }
}

package net.minecraft.entity.passive;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Blocks;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AnimalEntity extends AgeableEntity {
   private int inLove;
   private UUID loveCause;

   protected AnimalEntity(EntityType<? extends AnimalEntity> p_i48568_1_, World p_i48568_2_) {
      super(p_i48568_1_, p_i48568_2_);
      this.setPathfindingMalus(PathNodeType.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(PathNodeType.DAMAGE_FIRE, -1.0F);
   }

   protected void customServerAiStep() {
      if (this.getAge() != 0) {
         this.inLove = 0;
      }

      super.customServerAiStep();
   }

   /**
    * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
    * use this to react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.getAge() != 0) {
         this.inLove = 0;
      }

      if (this.inLove > 0) {
         --this.inLove;
         if (this.inLove % 10 == 0) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
         }
      }

   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         this.inLove = 0;
         return super.hurt(pSource, pAmount);
      }
   }

   public float getWalkTargetValue(BlockPos pPos, IWorldReader pLevel) {
      return pLevel.getBlockState(pPos.below()).is(Blocks.GRASS_BLOCK) ? 10.0F : pLevel.getBrightness(pPos) - 0.5F;
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("InLove", this.inLove);
      if (this.loveCause != null) {
         pCompound.putUUID("LoveCause", this.loveCause);
      }

   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return 0.14D;
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.inLove = pCompound.getInt("InLove");
      this.loveCause = pCompound.hasUUID("LoveCause") ? pCompound.getUUID("LoveCause") : null;
   }

   /**
    * Static predicate for determining whether or not an animal can spawn at the provided location.
    * @param pAnimal The animal entity to be spawned
    */
   public static boolean checkAnimalSpawnRules(EntityType<? extends AnimalEntity> pAnimal, IWorld pLevel, SpawnReason pReason, BlockPos pPos, Random pRandom) {
      return pLevel.getBlockState(pPos.below()).is(Blocks.GRASS_BLOCK) && pLevel.getRawBrightness(pPos, 0) > 8;
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 120;
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return false;
   }

   /**
    * Get the experience points the entity currently has.
    */
   protected int getExperienceReward(PlayerEntity pPlayer) {
      return 1 + this.level.random.nextInt(3);
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return pStack.getItem() == Items.WHEAT;
   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (this.isFood(itemstack)) {
         int i = this.getAge();
         if (!this.level.isClientSide && i == 0 && this.canFallInLove()) {
            this.usePlayerItem(pPlayer, itemstack);
            this.setInLove(pPlayer);
            return ActionResultType.SUCCESS;
         }

         if (this.isBaby()) {
            this.usePlayerItem(pPlayer, itemstack);
            this.ageUp((int)((float)(-i / 20) * 0.1F), true);
            return ActionResultType.sidedSuccess(this.level.isClientSide);
         }

         if (this.level.isClientSide) {
            return ActionResultType.CONSUME;
         }
      }

      return super.mobInteract(pPlayer, pHand);
   }

   protected void usePlayerItem(PlayerEntity p_175505_1_, ItemStack p_175505_2_) {
      if (!p_175505_1_.abilities.instabuild) {
         p_175505_2_.shrink(1);
      }

   }

   public boolean canFallInLove() {
      return this.inLove <= 0;
   }

   public void setInLove(@Nullable PlayerEntity pPlayer) {
      this.inLove = 600;
      if (pPlayer != null) {
         this.loveCause = pPlayer.getUUID();
      }

      this.level.broadcastEntityEvent(this, (byte)18);
   }

   public void setInLoveTime(int pTicks) {
      this.inLove = pTicks;
   }

   public int getInLoveTime() {
      return this.inLove;
   }

   @Nullable
   public ServerPlayerEntity getLoveCause() {
      if (this.loveCause == null) {
         return null;
      } else {
         PlayerEntity playerentity = this.level.getPlayerByUUID(this.loveCause);
         return playerentity instanceof ServerPlayerEntity ? (ServerPlayerEntity)playerentity : null;
      }
   }

   /**
    * Returns if the entity is currently in 'love mode'.
    */
   public boolean isInLove() {
      return this.inLove > 0;
   }

   public void resetLove() {
      this.inLove = 0;
   }

   /**
    * Returns true if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(AnimalEntity pOtherAnimal) {
      if (pOtherAnimal == this) {
         return false;
      } else if (pOtherAnimal.getClass() != this.getClass()) {
         return false;
      } else {
         return this.isInLove() && pOtherAnimal.isInLove();
      }
   }

   public void spawnChildFromBreeding(ServerWorld pLevel, AnimalEntity p_234177_2_) {
      AgeableEntity ageableentity = this.getBreedOffspring(pLevel, p_234177_2_);
      final net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(this, p_234177_2_, ageableentity);
      final boolean cancelled = net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
      ageableentity = event.getChild();
      if (cancelled) {
         //Reset the "inLove" state for the animals
         this.setAge(6000);
         p_234177_2_.setAge(6000);
         this.resetLove();
         p_234177_2_.resetLove();
         return;
      }
      if (ageableentity != null) {
         ServerPlayerEntity serverplayerentity = this.getLoveCause();
         if (serverplayerentity == null && p_234177_2_.getLoveCause() != null) {
            serverplayerentity = p_234177_2_.getLoveCause();
         }

         if (serverplayerentity != null) {
            serverplayerentity.awardStat(Stats.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger(serverplayerentity, this, p_234177_2_, ageableentity);
         }

         this.setAge(6000);
         p_234177_2_.setAge(6000);
         this.resetLove();
         p_234177_2_.resetLove();
         ageableentity.setBaby(true);
         ageableentity.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
         pLevel.addFreshEntityWithPassengers(ageableentity);
         pLevel.broadcastEntityEvent(this, (byte)18);
         if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            pLevel.addFreshEntity(new ExperienceOrbEntity(pLevel, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
         }

      }
   }

   /**
    * Handler for {@link World#setEntityState}
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 18) {
         for(int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
         }
      } else {
         super.handleEntityEvent(pId);
      }

   }
}

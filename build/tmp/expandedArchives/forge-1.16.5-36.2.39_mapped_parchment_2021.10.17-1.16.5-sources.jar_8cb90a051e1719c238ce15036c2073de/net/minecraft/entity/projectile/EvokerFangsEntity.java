package net.minecraft.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EvokerFangsEntity extends Entity {
   private int warmupDelayTicks;
   private boolean sentSpikeEvent;
   private int lifeTicks = 22;
   private boolean clientSideAttackStarted;
   private LivingEntity owner;
   private UUID ownerUUID;

   public EvokerFangsEntity(EntityType<? extends EvokerFangsEntity> p_i50170_1_, World p_i50170_2_) {
      super(p_i50170_1_, p_i50170_2_);
   }

   public EvokerFangsEntity(World p_i47276_1_, double p_i47276_2_, double p_i47276_4_, double p_i47276_6_, float p_i47276_8_, int p_i47276_9_, LivingEntity p_i47276_10_) {
      this(EntityType.EVOKER_FANGS, p_i47276_1_);
      this.warmupDelayTicks = p_i47276_9_;
      this.setOwner(p_i47276_10_);
      this.yRot = p_i47276_8_ * (180F / (float)Math.PI);
      this.setPos(p_i47276_2_, p_i47276_4_, p_i47276_6_);
   }

   protected void defineSynchedData() {
   }

   public void setOwner(@Nullable LivingEntity p_190549_1_) {
      this.owner = p_190549_1_;
      this.ownerUUID = p_190549_1_ == null ? null : p_190549_1_.getUUID();
   }

   @Nullable
   public LivingEntity getOwner() {
      if (this.owner == null && this.ownerUUID != null && this.level instanceof ServerWorld) {
         Entity entity = ((ServerWorld)this.level).getEntity(this.ownerUUID);
         if (entity instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
         }
      }

      return this.owner;
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      this.warmupDelayTicks = pCompound.getInt("Warmup");
      if (pCompound.hasUUID("Owner")) {
         this.ownerUUID = pCompound.getUUID("Owner");
      }

   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      pCompound.putInt("Warmup", this.warmupDelayTicks);
      if (this.ownerUUID != null) {
         pCompound.putUUID("Owner", this.ownerUUID);
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.level.isClientSide) {
         if (this.clientSideAttackStarted) {
            --this.lifeTicks;
            if (this.lifeTicks == 14) {
               for(int i = 0; i < 12; ++i) {
                  double d0 = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                  double d1 = this.getY() + 0.05D + this.random.nextDouble();
                  double d2 = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                  double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                  double d4 = 0.3D + this.random.nextDouble() * 0.3D;
                  double d5 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                  this.level.addParticle(ParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
               }
            }
         }
      } else if (--this.warmupDelayTicks < 0) {
         if (this.warmupDelayTicks == -8) {
            for(LivingEntity livingentity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2D, 0.0D, 0.2D))) {
               this.dealDamageTo(livingentity);
            }
         }

         if (!this.sentSpikeEvent) {
            this.level.broadcastEntityEvent(this, (byte)4);
            this.sentSpikeEvent = true;
         }

         if (--this.lifeTicks < 0) {
            this.remove();
         }
      }

   }

   private void dealDamageTo(LivingEntity p_190551_1_) {
      LivingEntity livingentity = this.getOwner();
      if (p_190551_1_.isAlive() && !p_190551_1_.isInvulnerable() && p_190551_1_ != livingentity) {
         if (livingentity == null) {
            p_190551_1_.hurt(DamageSource.MAGIC, 6.0F);
         } else {
            if (livingentity.isAlliedTo(p_190551_1_)) {
               return;
            }

            p_190551_1_.hurt(DamageSource.indirectMagic(this, livingentity), 6.0F);
         }

      }
   }

   /**
    * Handler for {@link World#setEntityState}
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      super.handleEntityEvent(pId);
      if (pId == 4) {
         this.clientSideAttackStarted = true;
         if (!this.isSilent()) {
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_FANGS_ATTACK, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.2F + 0.85F, false);
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public float getAnimationProgress(float pPartialTicks) {
      if (!this.clientSideAttackStarted) {
         return 0.0F;
      } else {
         int i = this.lifeTicks - 2;
         return i <= 0 ? 1.0F : 1.0F - ((float)i - pPartialTicks) / 20.0F;
      }
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnObjectPacket(this);
   }
}
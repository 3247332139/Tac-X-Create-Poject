package net.minecraft.entity.projectile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WitherSkullEntity extends DamagingProjectileEntity {
   private static final DataParameter<Boolean> DATA_DANGEROUS = EntityDataManager.defineId(WitherSkullEntity.class, DataSerializers.BOOLEAN);

   public WitherSkullEntity(EntityType<? extends WitherSkullEntity> p_i50147_1_, World p_i50147_2_) {
      super(p_i50147_1_, p_i50147_2_);
   }

   public WitherSkullEntity(World p_i1794_1_, LivingEntity p_i1794_2_, double p_i1794_3_, double p_i1794_5_, double p_i1794_7_) {
      super(EntityType.WITHER_SKULL, p_i1794_2_, p_i1794_3_, p_i1794_5_, p_i1794_7_, p_i1794_1_);
   }

   @OnlyIn(Dist.CLIENT)
   public WitherSkullEntity(World p_i1795_1_, double p_i1795_2_, double p_i1795_4_, double p_i1795_6_, double p_i1795_8_, double p_i1795_10_, double p_i1795_12_) {
      super(EntityType.WITHER_SKULL, p_i1795_2_, p_i1795_4_, p_i1795_6_, p_i1795_8_, p_i1795_10_, p_i1795_12_, p_i1795_1_);
   }

   /**
    * Return the motion factor for this projectile. The factor is multiplied by the original motion.
    */
   protected float getInertia() {
      return this.isDangerous() ? 0.73F : super.getInertia();
   }

   /**
    * Returns true if the entity is on fire. Used by render to add the fire effect on rendering.
    */
   public boolean isOnFire() {
      return false;
   }

   /**
    * Explosion resistance of a block relative to this entity
    */
   public float getBlockExplosionResistance(Explosion pExplosion, IBlockReader pLevel, BlockPos pPos, BlockState pBlockState, FluidState pFluidState, float pExplosionPower) {
      return this.isDangerous() && pBlockState.canEntityDestroy(pLevel, pPos, this) ? Math.min(0.8F, pExplosionPower) : pExplosionPower;
   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityRayTraceResult pResult) {
      super.onHitEntity(pResult);
      if (!this.level.isClientSide) {
         Entity entity = pResult.getEntity();
         Entity entity1 = this.getOwner();
         boolean flag;
         if (entity1 instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity1;
            flag = entity.hurt(DamageSource.witherSkull(this, livingentity), 8.0F);
            if (flag) {
               if (entity.isAlive()) {
                  this.doEnchantDamageEffects(livingentity, entity);
               } else {
                  livingentity.heal(5.0F);
               }
            }
         } else {
            flag = entity.hurt(DamageSource.MAGIC, 5.0F);
         }

         if (flag && entity instanceof LivingEntity) {
            int i = 0;
            if (this.level.getDifficulty() == Difficulty.NORMAL) {
               i = 10;
            } else if (this.level.getDifficulty() == Difficulty.HARD) {
               i = 40;
            }

            if (i > 0) {
               ((LivingEntity)entity).addEffect(new EffectInstance(Effects.WITHER, 20 * i, 1));
            }
         }

      }
   }

   /**
    * Called when this EntityFireball hits a block or entity.
    */
   protected void onHit(RayTraceResult pResult) {
      super.onHit(pResult);
      if (!this.level.isClientSide) {
         Explosion.Mode explosion$mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this.getOwner()) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
         this.level.explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, explosion$mode);
         this.remove();
      }

   }

   /**
    * Returns true if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return false;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      return false;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_DANGEROUS, false);
   }

   /**
    * Return whether this skull comes from an invulnerable (aura) wither boss.
    */
   public boolean isDangerous() {
      return this.entityData.get(DATA_DANGEROUS);
   }

   /**
    * Set whether this skull comes from an invulnerable (aura) wither boss.
    */
   public void setDangerous(boolean pInvulnerable) {
      this.entityData.set(DATA_DANGEROUS, pInvulnerable);
   }

   protected boolean shouldBurn() {
      return false;
   }
}

package net.minecraft.entity.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnderCrystalEntity extends Entity {
   private static final DataParameter<Optional<BlockPos>> DATA_BEAM_TARGET = EntityDataManager.defineId(EnderCrystalEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
   private static final DataParameter<Boolean> DATA_SHOW_BOTTOM = EntityDataManager.defineId(EnderCrystalEntity.class, DataSerializers.BOOLEAN);
   public int time;

   public EnderCrystalEntity(EntityType<? extends EnderCrystalEntity> p_i50231_1_, World p_i50231_2_) {
      super(p_i50231_1_, p_i50231_2_);
      this.blocksBuilding = true;
      this.time = this.random.nextInt(100000);
   }

   public EnderCrystalEntity(World p_i1699_1_, double p_i1699_2_, double p_i1699_4_, double p_i1699_6_) {
      this(EntityType.END_CRYSTAL, p_i1699_1_);
      this.setPos(p_i1699_2_, p_i1699_4_, p_i1699_6_);
   }

   protected boolean isMovementNoisy() {
      return false;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_BEAM_TARGET, Optional.empty());
      this.getEntityData().define(DATA_SHOW_BOTTOM, true);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      ++this.time;
      if (this.level instanceof ServerWorld) {
         BlockPos blockpos = this.blockPosition();
         if (((ServerWorld)this.level).dragonFight() != null && this.level.getBlockState(blockpos).isAir()) {
            this.level.setBlockAndUpdate(blockpos, AbstractFireBlock.getState(this.level, blockpos));
         }
      }

   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      if (this.getBeamTarget() != null) {
         pCompound.put("BeamTarget", NBTUtil.writeBlockPos(this.getBeamTarget()));
      }

      pCompound.putBoolean("ShowBottom", this.showsBottom());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      if (pCompound.contains("BeamTarget", 10)) {
         this.setBeamTarget(NBTUtil.readBlockPos(pCompound.getCompound("BeamTarget")));
      }

      if (pCompound.contains("ShowBottom", 1)) {
         this.setShowBottom(pCompound.getBoolean("ShowBottom"));
      }

   }

   /**
    * Returns true if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return true;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else if (pSource.getEntity() instanceof EnderDragonEntity) {
         return false;
      } else {
         if (!this.removed && !this.level.isClientSide) {
            this.remove();
            if (!pSource.isExplosion()) {
               this.level.explode((Entity)null, this.getX(), this.getY(), this.getZ(), 6.0F, Explosion.Mode.DESTROY);
            }

            this.onDestroyedBy(pSource);
         }

         return true;
      }
   }

   /**
    * Called by the /kill command.
    */
   public void kill() {
      this.onDestroyedBy(DamageSource.GENERIC);
      super.kill();
   }

   private void onDestroyedBy(DamageSource pSource) {
      if (this.level instanceof ServerWorld) {
         DragonFightManager dragonfightmanager = ((ServerWorld)this.level).dragonFight();
         if (dragonfightmanager != null) {
            dragonfightmanager.onCrystalDestroyed(this, pSource);
         }
      }

   }

   public void setBeamTarget(@Nullable BlockPos pBeamTarget) {
      this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(pBeamTarget));
   }

   @Nullable
   public BlockPos getBeamTarget() {
      return this.getEntityData().get(DATA_BEAM_TARGET).orElse((BlockPos)null);
   }

   public void setShowBottom(boolean pShowBottom) {
      this.getEntityData().set(DATA_SHOW_BOTTOM, pShowBottom);
   }

   public boolean showsBottom() {
      return this.getEntityData().get(DATA_SHOW_BOTTOM);
   }

   /**
    * Checks if the entity is in range to render.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      return super.shouldRenderAtSqrDistance(pDistance) || this.getBeamTarget() != null;
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnObjectPacket(this);
   }
}
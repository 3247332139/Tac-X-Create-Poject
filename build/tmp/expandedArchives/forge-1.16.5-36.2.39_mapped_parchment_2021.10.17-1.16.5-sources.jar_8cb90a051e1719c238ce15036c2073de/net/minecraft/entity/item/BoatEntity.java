package net.minecraft.entity.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.client.CSteerBoatPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TeleportationRepositioner;
import net.minecraft.util.TransportationHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BoatEntity extends Entity {
   private static final DataParameter<Integer> DATA_ID_HURT = EntityDataManager.defineId(BoatEntity.class, DataSerializers.INT);
   private static final DataParameter<Integer> DATA_ID_HURTDIR = EntityDataManager.defineId(BoatEntity.class, DataSerializers.INT);
   private static final DataParameter<Float> DATA_ID_DAMAGE = EntityDataManager.defineId(BoatEntity.class, DataSerializers.FLOAT);
   private static final DataParameter<Integer> DATA_ID_TYPE = EntityDataManager.defineId(BoatEntity.class, DataSerializers.INT);
   private static final DataParameter<Boolean> DATA_ID_PADDLE_LEFT = EntityDataManager.defineId(BoatEntity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Boolean> DATA_ID_PADDLE_RIGHT = EntityDataManager.defineId(BoatEntity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Integer> DATA_ID_BUBBLE_TIME = EntityDataManager.defineId(BoatEntity.class, DataSerializers.INT);
   private final float[] paddlePositions = new float[2];
   private float invFriction;
   private float outOfControlTicks;
   private float deltaRotation;
   private int lerpSteps;
   private double lerpX;
   private double lerpY;
   private double lerpZ;
   private double lerpYRot;
   private double lerpXRot;
   private boolean inputLeft;
   private boolean inputRight;
   private boolean inputUp;
   private boolean inputDown;
   private double waterLevel;
   private float landFriction;
   private BoatEntity.Status status;
   private BoatEntity.Status oldStatus;
   private double lastYd;
   private boolean isAboveBubbleColumn;
   private boolean bubbleColumnDirectionIsDown;
   private float bubbleMultiplier;
   private float bubbleAngle;
   private float bubbleAngleO;

   public BoatEntity(EntityType<? extends BoatEntity> p_i50129_1_, World p_i50129_2_) {
      super(p_i50129_1_, p_i50129_2_);
      this.blocksBuilding = true;
   }

   public BoatEntity(World p_i1705_1_, double p_i1705_2_, double p_i1705_4_, double p_i1705_6_) {
      this(EntityType.BOAT, p_i1705_1_);
      this.setPos(p_i1705_2_, p_i1705_4_, p_i1705_6_);
      this.setDeltaMovement(Vector3d.ZERO);
      this.xo = p_i1705_2_;
      this.yo = p_i1705_4_;
      this.zo = p_i1705_6_;
   }

   protected float getEyeHeight(Pose pPose, EntitySize pSize) {
      return pSize.height;
   }

   protected boolean isMovementNoisy() {
      return false;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_ID_HURT, 0);
      this.entityData.define(DATA_ID_HURTDIR, 1);
      this.entityData.define(DATA_ID_DAMAGE, 0.0F);
      this.entityData.define(DATA_ID_TYPE, BoatEntity.Type.OAK.ordinal());
      this.entityData.define(DATA_ID_PADDLE_LEFT, false);
      this.entityData.define(DATA_ID_PADDLE_RIGHT, false);
      this.entityData.define(DATA_ID_BUBBLE_TIME, 0);
   }

   public boolean canCollideWith(Entity pEntity) {
      return canVehicleCollide(this, pEntity);
   }

   public static boolean canVehicleCollide(Entity p_242378_0_, Entity p_242378_1_) {
      return (p_242378_1_.canBeCollidedWith() || p_242378_1_.isPushable()) && !p_242378_0_.isPassengerOfSameVehicle(p_242378_1_);
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   /**
    * Returns true if this entity should push and be pushed by other entities when colliding.
    */
   public boolean isPushable() {
      return true;
   }

   protected Vector3d getRelativePortalPosition(Direction.Axis pAxis, TeleportationRepositioner.Result pPortal) {
      return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(pAxis, pPortal));
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return -0.1D;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else if (!this.level.isClientSide && !this.removed) {
         this.setHurtDir(-this.getHurtDir());
         this.setHurtTime(10);
         this.setDamage(this.getDamage() + pAmount * 10.0F);
         this.markHurt();
         boolean flag = pSource.getEntity() instanceof PlayerEntity && ((PlayerEntity)pSource.getEntity()).abilities.instabuild;
         if (flag || this.getDamage() > 40.0F) {
            if (!flag && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
               this.spawnAtLocation(this.getDropItem());
            }

            this.remove();
         }

         return true;
      } else {
         return true;
      }
   }

   public void onAboveBubbleCol(boolean pDownwards) {
      if (!this.level.isClientSide) {
         this.isAboveBubbleColumn = true;
         this.bubbleColumnDirectionIsDown = pDownwards;
         if (this.getBubbleTime() == 0) {
            this.setBubbleTime(60);
         }
      }

      this.level.addParticle(ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7D, this.getZ() + (double)this.random.nextFloat(), 0.0D, 0.0D, 0.0D);
      if (this.random.nextInt(20) == 0) {
         this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSwimSplashSound(), this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat(), false);
      }

   }

   /**
    * Applies a velocity to the entities, to push them away from eachother.
    */
   public void push(Entity pEntity) {
      if (pEntity instanceof BoatEntity) {
         if (pEntity.getBoundingBox().minY < this.getBoundingBox().maxY) {
            super.push(pEntity);
         }
      } else if (pEntity.getBoundingBox().minY <= this.getBoundingBox().minY) {
         super.push(pEntity);
      }

   }

   public Item getDropItem() {
      switch(this.getBoatType()) {
      case OAK:
      default:
         return Items.OAK_BOAT;
      case SPRUCE:
         return Items.SPRUCE_BOAT;
      case BIRCH:
         return Items.BIRCH_BOAT;
      case JUNGLE:
         return Items.JUNGLE_BOAT;
      case ACACIA:
         return Items.ACACIA_BOAT;
      case DARK_OAK:
         return Items.DARK_OAK_BOAT;
      }
   }

   /**
    * Setups the entity to do the hurt animation. Only used by packets in multiplayer.
    */
   @OnlyIn(Dist.CLIENT)
   public void animateHurt() {
      this.setHurtDir(-this.getHurtDir());
      this.setHurtTime(10);
      this.setDamage(this.getDamage() * 11.0F);
   }

   /**
    * Returns true if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return !this.removed;
   }

   /**
    * Sets a target for the client to interpolate towards over the next few ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport) {
      this.lerpX = pX;
      this.lerpY = pY;
      this.lerpZ = pZ;
      this.lerpYRot = (double)pYaw;
      this.lerpXRot = (double)pPitch;
      this.lerpSteps = 10;
   }

   /**
    * Gets the horizontal facing direction of this Entity, adjusted to take specially-treated entity types into account.
    */
   public Direction getMotionDirection() {
      return this.getDirection().getClockWise();
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      this.oldStatus = this.status;
      this.status = this.getStatus();
      if (this.status != BoatEntity.Status.UNDER_WATER && this.status != BoatEntity.Status.UNDER_FLOWING_WATER) {
         this.outOfControlTicks = 0.0F;
      } else {
         ++this.outOfControlTicks;
      }

      if (!this.level.isClientSide && this.outOfControlTicks >= 60.0F) {
         this.ejectPassengers();
      }

      if (this.getHurtTime() > 0) {
         this.setHurtTime(this.getHurtTime() - 1);
      }

      if (this.getDamage() > 0.0F) {
         this.setDamage(this.getDamage() - 1.0F);
      }

      super.tick();
      this.tickLerp();
      if (this.isControlledByLocalInstance()) {
         if (this.getPassengers().isEmpty() || !(this.getPassengers().get(0) instanceof PlayerEntity)) {
            this.setPaddleState(false, false);
         }

         this.floatBoat();
         if (this.level.isClientSide) {
            this.controlBoat();
            this.level.sendPacketToServer(new CSteerBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
         }

         this.move(MoverType.SELF, this.getDeltaMovement());
      } else {
         this.setDeltaMovement(Vector3d.ZERO);
      }

      this.tickBubbleColumn();

      for(int i = 0; i <= 1; ++i) {
         if (this.getPaddleState(i)) {
            if (!this.isSilent() && (double)(this.paddlePositions[i] % ((float)Math.PI * 2F)) <= (double)((float)Math.PI / 4F) && ((double)this.paddlePositions[i] + (double)((float)Math.PI / 8F)) % (double)((float)Math.PI * 2F) >= (double)((float)Math.PI / 4F)) {
               SoundEvent soundevent = this.getPaddleSound();
               if (soundevent != null) {
                  Vector3d vector3d = this.getViewVector(1.0F);
                  double d0 = i == 1 ? -vector3d.z : vector3d.z;
                  double d1 = i == 1 ? vector3d.x : -vector3d.x;
                  this.level.playSound((PlayerEntity)null, this.getX() + d0, this.getY(), this.getZ() + d1, soundevent, this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat());
               }
            }

            this.paddlePositions[i] = (float)((double)this.paddlePositions[i] + (double)((float)Math.PI / 8F));
         } else {
            this.paddlePositions[i] = 0.0F;
         }
      }

      this.checkInsideBlocks();
      List<Entity> list = this.level.getEntities(this, this.getBoundingBox().inflate((double)0.2F, (double)-0.01F, (double)0.2F), EntityPredicates.pushableBy(this));
      if (!list.isEmpty()) {
         boolean flag = !this.level.isClientSide && !(this.getControllingPassenger() instanceof PlayerEntity);

         for(int j = 0; j < list.size(); ++j) {
            Entity entity = list.get(j);
            if (!entity.hasPassenger(this)) {
               if (flag && this.getPassengers().size() < 2 && !entity.isPassenger() && entity.getBbWidth() < this.getBbWidth() && entity instanceof LivingEntity && !(entity instanceof WaterMobEntity) && !(entity instanceof PlayerEntity)) {
                  entity.startRiding(this);
               } else {
                  this.push(entity);
               }
            }
         }
      }

   }

   private void tickBubbleColumn() {
      if (this.level.isClientSide) {
         int i = this.getBubbleTime();
         if (i > 0) {
            this.bubbleMultiplier += 0.05F;
         } else {
            this.bubbleMultiplier -= 0.1F;
         }

         this.bubbleMultiplier = MathHelper.clamp(this.bubbleMultiplier, 0.0F, 1.0F);
         this.bubbleAngleO = this.bubbleAngle;
         this.bubbleAngle = 10.0F * (float)Math.sin((double)(0.5F * (float)this.level.getGameTime())) * this.bubbleMultiplier;
      } else {
         if (!this.isAboveBubbleColumn) {
            this.setBubbleTime(0);
         }

         int k = this.getBubbleTime();
         if (k > 0) {
            --k;
            this.setBubbleTime(k);
            int j = 60 - k - 1;
            if (j > 0 && k == 0) {
               this.setBubbleTime(0);
               Vector3d vector3d = this.getDeltaMovement();
               if (this.bubbleColumnDirectionIsDown) {
                  this.setDeltaMovement(vector3d.add(0.0D, -0.7D, 0.0D));
                  this.ejectPassengers();
               } else {
                  this.setDeltaMovement(vector3d.x, this.hasPassenger(PlayerEntity.class) ? 2.7D : 0.6D, vector3d.z);
               }
            }

            this.isAboveBubbleColumn = false;
         }
      }

   }

   @Nullable
   protected SoundEvent getPaddleSound() {
      switch(this.getStatus()) {
      case IN_WATER:
      case UNDER_WATER:
      case UNDER_FLOWING_WATER:
         return SoundEvents.BOAT_PADDLE_WATER;
      case ON_LAND:
         return SoundEvents.BOAT_PADDLE_LAND;
      case IN_AIR:
      default:
         return null;
      }
   }

   private void tickLerp() {
      if (this.isControlledByLocalInstance()) {
         this.lerpSteps = 0;
         this.setPacketCoordinates(this.getX(), this.getY(), this.getZ());
      }

      if (this.lerpSteps > 0) {
         double d0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
         double d1 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
         double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
         double d3 = MathHelper.wrapDegrees(this.lerpYRot - (double)this.yRot);
         this.yRot = (float)((double)this.yRot + d3 / (double)this.lerpSteps);
         this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
         --this.lerpSteps;
         this.setPos(d0, d1, d2);
         this.setRot(this.yRot, this.xRot);
      }
   }

   public void setPaddleState(boolean pLeft, boolean pRight) {
      this.entityData.set(DATA_ID_PADDLE_LEFT, pLeft);
      this.entityData.set(DATA_ID_PADDLE_RIGHT, pRight);
   }

   @OnlyIn(Dist.CLIENT)
   public float getRowingTime(int pSide, float pLimbSwing) {
      return this.getPaddleState(pSide) ? (float)MathHelper.clampedLerp((double)this.paddlePositions[pSide] - (double)((float)Math.PI / 8F), (double)this.paddlePositions[pSide], (double)pLimbSwing) : 0.0F;
   }

   /**
    * Determines whether the boat is in water, gliding on land, or in air
    */
   private BoatEntity.Status getStatus() {
      BoatEntity.Status boatentity$status = this.isUnderwater();
      if (boatentity$status != null) {
         this.waterLevel = this.getBoundingBox().maxY;
         return boatentity$status;
      } else if (this.checkInWater()) {
         return BoatEntity.Status.IN_WATER;
      } else {
         float f = this.getGroundFriction();
         if (f > 0.0F) {
            this.landFriction = f;
            return BoatEntity.Status.ON_LAND;
         } else {
            return BoatEntity.Status.IN_AIR;
         }
      }
   }

   public float getWaterLevelAbove() {
      AxisAlignedBB axisalignedbb = this.getBoundingBox();
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.maxY);
      int l = MathHelper.ceil(axisalignedbb.maxY - this.lastYd);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      label39:
      for(int k1 = k; k1 < l; ++k1) {
         float f = 0.0F;

         for(int l1 = i; l1 < j; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               blockpos$mutable.set(l1, k1, i2);
               FluidState fluidstate = this.level.getFluidState(blockpos$mutable);
               if (fluidstate.is(FluidTags.WATER)) {
                  f = Math.max(f, fluidstate.getHeight(this.level, blockpos$mutable));
               }

               if (f >= 1.0F) {
                  continue label39;
               }
            }
         }

         if (f < 1.0F) {
            return (float)blockpos$mutable.getY() + f;
         }
      }

      return (float)(l + 1);
   }

   /**
    * Decides how much the boat should be gliding on the land (based on any slippery blocks)
    */
   public float getGroundFriction() {
      AxisAlignedBB axisalignedbb = this.getBoundingBox();
      AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY - 0.001D, axisalignedbb.minZ, axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
      int i = MathHelper.floor(axisalignedbb1.minX) - 1;
      int j = MathHelper.ceil(axisalignedbb1.maxX) + 1;
      int k = MathHelper.floor(axisalignedbb1.minY) - 1;
      int l = MathHelper.ceil(axisalignedbb1.maxY) + 1;
      int i1 = MathHelper.floor(axisalignedbb1.minZ) - 1;
      int j1 = MathHelper.ceil(axisalignedbb1.maxZ) + 1;
      VoxelShape voxelshape = VoxelShapes.create(axisalignedbb1);
      float f = 0.0F;
      int k1 = 0;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int l1 = i; l1 < j; ++l1) {
         for(int i2 = i1; i2 < j1; ++i2) {
            int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);
            if (j2 != 2) {
               for(int k2 = k; k2 < l; ++k2) {
                  if (j2 <= 0 || k2 != k && k2 != l - 1) {
                     blockpos$mutable.set(l1, k2, i2);
                     BlockState blockstate = this.level.getBlockState(blockpos$mutable);
                     if (!(blockstate.getBlock() instanceof LilyPadBlock) && VoxelShapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level, blockpos$mutable).move((double)l1, (double)k2, (double)i2), voxelshape, IBooleanFunction.AND)) {
                        f += blockstate.getSlipperiness(this.level, blockpos$mutable, this);
                        ++k1;
                     }
                  }
               }
            }
         }
      }

      return f / (float)k1;
   }

   private boolean checkInWater() {
      AxisAlignedBB axisalignedbb = this.getBoundingBox();
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.minY);
      int l = MathHelper.ceil(axisalignedbb.minY + 0.001D);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      boolean flag = false;
      this.waterLevel = Double.MIN_VALUE;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               blockpos$mutable.set(k1, l1, i2);
               FluidState fluidstate = this.level.getFluidState(blockpos$mutable);
               if (fluidstate.is(FluidTags.WATER)) {
                  float f = (float)l1 + fluidstate.getHeight(this.level, blockpos$mutable);
                  this.waterLevel = Math.max((double)f, this.waterLevel);
                  flag |= axisalignedbb.minY < (double)f;
               }
            }
         }
      }

      return flag;
   }

   /**
    * Decides whether the boat is currently underwater.
    */
   @Nullable
   private BoatEntity.Status isUnderwater() {
      AxisAlignedBB axisalignedbb = this.getBoundingBox();
      double d0 = axisalignedbb.maxY + 0.001D;
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.maxY);
      int l = MathHelper.ceil(d0);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      boolean flag = false;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               blockpos$mutable.set(k1, l1, i2);
               FluidState fluidstate = this.level.getFluidState(blockpos$mutable);
               if (fluidstate.is(FluidTags.WATER) && d0 < (double)((float)blockpos$mutable.getY() + fluidstate.getHeight(this.level, blockpos$mutable))) {
                  if (!fluidstate.isSource()) {
                     return BoatEntity.Status.UNDER_FLOWING_WATER;
                  }

                  flag = true;
               }
            }
         }
      }

      return flag ? BoatEntity.Status.UNDER_WATER : null;
   }

   /**
    * Update the boat's speed, based on momentum.
    */
   private void floatBoat() {
      double d0 = (double)-0.04F;
      double d1 = this.isNoGravity() ? 0.0D : (double)-0.04F;
      double d2 = 0.0D;
      this.invFriction = 0.05F;
      if (this.oldStatus == BoatEntity.Status.IN_AIR && this.status != BoatEntity.Status.IN_AIR && this.status != BoatEntity.Status.ON_LAND) {
         this.waterLevel = this.getY(1.0D);
         this.setPos(this.getX(), (double)(this.getWaterLevelAbove() - this.getBbHeight()) + 0.101D, this.getZ());
         this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
         this.lastYd = 0.0D;
         this.status = BoatEntity.Status.IN_WATER;
      } else {
         if (this.status == BoatEntity.Status.IN_WATER) {
            d2 = (this.waterLevel - this.getY()) / (double)this.getBbHeight();
            this.invFriction = 0.9F;
         } else if (this.status == BoatEntity.Status.UNDER_FLOWING_WATER) {
            d1 = -7.0E-4D;
            this.invFriction = 0.9F;
         } else if (this.status == BoatEntity.Status.UNDER_WATER) {
            d2 = (double)0.01F;
            this.invFriction = 0.45F;
         } else if (this.status == BoatEntity.Status.IN_AIR) {
            this.invFriction = 0.9F;
         } else if (this.status == BoatEntity.Status.ON_LAND) {
            this.invFriction = this.landFriction;
            if (this.getControllingPassenger() instanceof PlayerEntity) {
               this.landFriction /= 2.0F;
            }
         }

         Vector3d vector3d = this.getDeltaMovement();
         this.setDeltaMovement(vector3d.x * (double)this.invFriction, vector3d.y + d1, vector3d.z * (double)this.invFriction);
         this.deltaRotation *= this.invFriction;
         if (d2 > 0.0D) {
            Vector3d vector3d1 = this.getDeltaMovement();
            this.setDeltaMovement(vector3d1.x, (vector3d1.y + d2 * 0.06153846016296973D) * 0.75D, vector3d1.z);
         }
      }

   }

   private void controlBoat() {
      if (this.isVehicle()) {
         float f = 0.0F;
         if (this.inputLeft) {
            --this.deltaRotation;
         }

         if (this.inputRight) {
            ++this.deltaRotation;
         }

         if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown) {
            f += 0.005F;
         }

         this.yRot += this.deltaRotation;
         if (this.inputUp) {
            f += 0.04F;
         }

         if (this.inputDown) {
            f -= 0.005F;
         }

         this.setDeltaMovement(this.getDeltaMovement().add((double)(MathHelper.sin(-this.yRot * ((float)Math.PI / 180F)) * f), 0.0D, (double)(MathHelper.cos(this.yRot * ((float)Math.PI / 180F)) * f)));
         this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
      }
   }

   public void positionRider(Entity pPassenger) {
      if (this.hasPassenger(pPassenger)) {
         float f = 0.0F;
         float f1 = (float)((this.removed ? (double)0.01F : this.getPassengersRidingOffset()) + pPassenger.getMyRidingOffset());
         if (this.getPassengers().size() > 1) {
            int i = this.getPassengers().indexOf(pPassenger);
            if (i == 0) {
               f = 0.2F;
            } else {
               f = -0.6F;
            }

            if (pPassenger instanceof AnimalEntity) {
               f = (float)((double)f + 0.2D);
            }
         }

         Vector3d vector3d = (new Vector3d((double)f, 0.0D, 0.0D)).yRot(-this.yRot * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
         pPassenger.setPos(this.getX() + vector3d.x, this.getY() + (double)f1, this.getZ() + vector3d.z);
         pPassenger.yRot += this.deltaRotation;
         pPassenger.setYHeadRot(pPassenger.getYHeadRot() + this.deltaRotation);
         this.clampRotation(pPassenger);
         if (pPassenger instanceof AnimalEntity && this.getPassengers().size() > 1) {
            int j = pPassenger.getId() % 2 == 0 ? 90 : 270;
            pPassenger.setYBodyRot(((AnimalEntity)pPassenger).yBodyRot + (float)j);
            pPassenger.setYHeadRot(pPassenger.getYHeadRot() + (float)j);
         }

      }
   }

   public Vector3d getDismountLocationForPassenger(LivingEntity pLivingEntity) {
      Vector3d vector3d = getCollisionHorizontalEscapeVector((double)(this.getBbWidth() * MathHelper.SQRT_OF_TWO), (double)pLivingEntity.getBbWidth(), this.yRot);
      double d0 = this.getX() + vector3d.x;
      double d1 = this.getZ() + vector3d.z;
      BlockPos blockpos = new BlockPos(d0, this.getBoundingBox().maxY, d1);
      BlockPos blockpos1 = blockpos.below();
      if (!this.level.isWaterAt(blockpos1)) {
         double d2 = (double)blockpos.getY() + this.level.getBlockFloorHeight(blockpos);
         double d3 = (double)blockpos.getY() + this.level.getBlockFloorHeight(blockpos1);

         for(Pose pose : pLivingEntity.getDismountPoses()) {
            Vector3d vector3d1 = TransportationHelper.findDismountLocation(this.level, d0, d2, d1, pLivingEntity, pose);
            if (vector3d1 != null) {
               pLivingEntity.setPose(pose);
               return vector3d1;
            }

            Vector3d vector3d2 = TransportationHelper.findDismountLocation(this.level, d0, d3, d1, pLivingEntity, pose);
            if (vector3d2 != null) {
               pLivingEntity.setPose(pose);
               return vector3d2;
            }
         }
      }

      return super.getDismountLocationForPassenger(pLivingEntity);
   }

   /**
    * Applies this boat's yaw to the given entity. Used to update the orientation of its passenger.
    */
   protected void clampRotation(Entity pEntityToUpdate) {
      pEntityToUpdate.setYBodyRot(this.yRot);
      float f = MathHelper.wrapDegrees(pEntityToUpdate.yRot - this.yRot);
      float f1 = MathHelper.clamp(f, -105.0F, 105.0F);
      pEntityToUpdate.yRotO += f1 - f;
      pEntityToUpdate.yRot += f1 - f;
      pEntityToUpdate.setYHeadRot(pEntityToUpdate.yRot);
   }

   /**
    * Applies this entity's orientation (pitch/yaw) to another entity. Used to update passenger orientation.
    */
   @OnlyIn(Dist.CLIENT)
   public void onPassengerTurned(Entity pEntityToUpdate) {
      this.clampRotation(pEntityToUpdate);
   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      pCompound.putString("Type", this.getBoatType().getName());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      if (pCompound.contains("Type", 8)) {
         this.setType(BoatEntity.Type.byName(pCompound.getString("Type")));
      }

   }

   public ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
      if (pPlayer.isSecondaryUseActive()) {
         return ActionResultType.PASS;
      } else if (this.outOfControlTicks < 60.0F) {
         if (!this.level.isClientSide) {
            return pPlayer.startRiding(this) ? ActionResultType.CONSUME : ActionResultType.PASS;
         } else {
            return ActionResultType.SUCCESS;
         }
      } else {
         return ActionResultType.PASS;
      }
   }

   protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
      this.lastYd = this.getDeltaMovement().y;
      if (!this.isPassenger()) {
         if (pOnGround) {
            if (this.fallDistance > 3.0F) {
               if (this.status != BoatEntity.Status.ON_LAND) {
                  this.fallDistance = 0.0F;
                  return;
               }

               this.causeFallDamage(this.fallDistance, 1.0F);
               if (!this.level.isClientSide && !this.removed) {
                  this.remove();
                  if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                     for(int i = 0; i < 3; ++i) {
                        this.spawnAtLocation(this.getBoatType().getPlanks());
                     }

                     for(int j = 0; j < 2; ++j) {
                        this.spawnAtLocation(Items.STICK);
                     }
                  }
               }
            }

            this.fallDistance = 0.0F;
         } else if (!this.level.getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && pY < 0.0D) {
            this.fallDistance = (float)((double)this.fallDistance - pY);
         }

      }
   }

   public boolean getPaddleState(int pSide) {
      return this.entityData.<Boolean>get(pSide == 0 ? DATA_ID_PADDLE_LEFT : DATA_ID_PADDLE_RIGHT) && this.getControllingPassenger() != null;
   }

   /**
    * Sets the damage taken from the last hit.
    */
   public void setDamage(float pDamageTaken) {
      this.entityData.set(DATA_ID_DAMAGE, pDamageTaken);
   }

   /**
    * Gets the damage taken from the last hit.
    */
   public float getDamage() {
      return this.entityData.get(DATA_ID_DAMAGE);
   }

   /**
    * Sets the time to count down from since the last time entity was hit.
    */
   public void setHurtTime(int pTimeSinceHit) {
      this.entityData.set(DATA_ID_HURT, pTimeSinceHit);
   }

   /**
    * Gets the time since the last hit.
    */
   public int getHurtTime() {
      return this.entityData.get(DATA_ID_HURT);
   }

   private void setBubbleTime(int pTicks) {
      this.entityData.set(DATA_ID_BUBBLE_TIME, pTicks);
   }

   private int getBubbleTime() {
      return this.entityData.get(DATA_ID_BUBBLE_TIME);
   }

   @OnlyIn(Dist.CLIENT)
   public float getBubbleAngle(float pPartialTicks) {
      return MathHelper.lerp(pPartialTicks, this.bubbleAngleO, this.bubbleAngle);
   }

   /**
    * Sets the forward direction of the entity.
    */
   public void setHurtDir(int pForwardDirection) {
      this.entityData.set(DATA_ID_HURTDIR, pForwardDirection);
   }

   /**
    * Gets the forward direction of the entity.
    */
   public int getHurtDir() {
      return this.entityData.get(DATA_ID_HURTDIR);
   }

   public void setType(BoatEntity.Type pBoatType) {
      this.entityData.set(DATA_ID_TYPE, pBoatType.ordinal());
   }

   public BoatEntity.Type getBoatType() {
      return BoatEntity.Type.byId(this.entityData.get(DATA_ID_TYPE));
   }

   protected boolean canAddPassenger(Entity pPassenger) {
      return this.getPassengers().size() < 2 && !this.isEyeInFluid(FluidTags.WATER);
   }

   /**
    * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
    * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
    */
   @Nullable
   public Entity getControllingPassenger() {
      List<Entity> list = this.getPassengers();
      return list.isEmpty() ? null : list.get(0);
   }

   @OnlyIn(Dist.CLIENT)
   public void setInput(boolean pLeftInputDown, boolean pRightInputDown, boolean pForwardInputDown, boolean pBackInputDown) {
      this.inputLeft = pLeftInputDown;
      this.inputRight = pRightInputDown;
      this.inputUp = pForwardInputDown;
      this.inputDown = pBackInputDown;
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnObjectPacket(this);
   }

   public boolean isUnderWater() {
      return this.status == BoatEntity.Status.UNDER_WATER || this.status == BoatEntity.Status.UNDER_FLOWING_WATER;
   }

   // Forge: Fix MC-119811 by instantly completing lerp on board
   @Override
   protected void addPassenger(Entity passenger) {
      super.addPassenger(passenger);
      if (this.isControlledByLocalInstance() && this.lerpSteps > 0) {
         this.lerpSteps = 0;
         this.absMoveTo(this.lerpX, this.lerpY, this.lerpZ, (float)this.lerpYRot, (float)this.lerpXRot);
      }
   }

   public static enum Status {
      IN_WATER,
      UNDER_WATER,
      UNDER_FLOWING_WATER,
      ON_LAND,
      IN_AIR;
   }

   public static enum Type {
      OAK(Blocks.OAK_PLANKS, "oak"),
      SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
      BIRCH(Blocks.BIRCH_PLANKS, "birch"),
      JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
      ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
      DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak");

      private final String name;
      private final Block planks;

      private Type(Block p_i48146_3_, String p_i48146_4_) {
         this.name = p_i48146_4_;
         this.planks = p_i48146_3_;
      }

      public String getName() {
         return this.name;
      }

      public Block getPlanks() {
         return this.planks;
      }

      public String toString() {
         return this.name;
      }

      /**
       * Get a boat type by it's enum ordinal
       */
      public static BoatEntity.Type byId(int pId) {
         BoatEntity.Type[] aboatentity$type = values();
         if (pId < 0 || pId >= aboatentity$type.length) {
            pId = 0;
         }

         return aboatentity$type[pId];
      }

      public static BoatEntity.Type byName(String pName) {
         BoatEntity.Type[] aboatentity$type = values();

         for(int i = 0; i < aboatentity$type.length; ++i) {
            if (aboatentity$type[i].getName().equals(pName)) {
               return aboatentity$type[i];
            }
         }

         return aboatentity$type[0];
      }
   }
}

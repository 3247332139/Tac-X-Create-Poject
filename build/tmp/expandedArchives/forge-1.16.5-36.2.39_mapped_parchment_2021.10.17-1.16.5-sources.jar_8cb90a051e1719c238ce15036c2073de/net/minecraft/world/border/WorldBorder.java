package net.minecraft.world.border;

import com.google.common.collect.Lists;
import com.mojang.serialization.DynamicLike;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WorldBorder {
   private final List<IBorderListener> listeners = Lists.newArrayList();
   private double damagePerBlock = 0.2D;
   private double damageSafeZone = 5.0D;
   private int warningTime = 15;
   private int warningBlocks = 5;
   private double centerX;
   private double centerZ;
   private int absoluteMaxSize = 29999984;
   private WorldBorder.IBorderInfo extent = new WorldBorder.StationaryBorderInfo(6.0E7D);
   public static final WorldBorder.Serializer DEFAULT_SETTINGS = new WorldBorder.Serializer(0.0D, 0.0D, 0.2D, 5.0D, 5, 15, 6.0E7D, 0L, 0.0D);

   public boolean isWithinBounds(BlockPos pPos) {
      return (double)(pPos.getX() + 1) > this.getMinX() && (double)pPos.getX() < this.getMaxX() && (double)(pPos.getZ() + 1) > this.getMinZ() && (double)pPos.getZ() < this.getMaxZ();
   }

   public boolean isWithinBounds(ChunkPos pRange) {
      return (double)pRange.getMaxBlockX() > this.getMinX() && (double)pRange.getMinBlockX() < this.getMaxX() && (double)pRange.getMaxBlockZ() > this.getMinZ() && (double)pRange.getMinBlockZ() < this.getMaxZ();
   }

   public boolean isWithinBounds(AxisAlignedBB pBb) {
      return pBb.maxX > this.getMinX() && pBb.minX < this.getMaxX() && pBb.maxZ > this.getMinZ() && pBb.minZ < this.getMaxZ();
   }

   public double getDistanceToBorder(Entity pEntity) {
      return this.getDistanceToBorder(pEntity.getX(), pEntity.getZ());
   }

   public VoxelShape getCollisionShape() {
      return this.extent.getCollisionShape();
   }

   public double getDistanceToBorder(double pX, double pZ) {
      double d0 = pZ - this.getMinZ();
      double d1 = this.getMaxZ() - pZ;
      double d2 = pX - this.getMinX();
      double d3 = this.getMaxX() - pX;
      double d4 = Math.min(d2, d3);
      d4 = Math.min(d4, d0);
      return Math.min(d4, d1);
   }

   @OnlyIn(Dist.CLIENT)
   public BorderStatus getStatus() {
      return this.extent.getStatus();
   }

   public double getMinX() {
      return this.extent.getMinX();
   }

   public double getMinZ() {
      return this.extent.getMinZ();
   }

   public double getMaxX() {
      return this.extent.getMaxX();
   }

   public double getMaxZ() {
      return this.extent.getMaxZ();
   }

   public double getCenterX() {
      return this.centerX;
   }

   public double getCenterZ() {
      return this.centerZ;
   }

   public void setCenter(double pX, double pZ) {
      this.centerX = pX;
      this.centerZ = pZ;
      this.extent.onCenterChange();

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onBorderCenterSet(this, pX, pZ);
      }

   }

   public double getSize() {
      return this.extent.getSize();
   }

   public long getLerpRemainingTime() {
      return this.extent.getLerpRemainingTime();
   }

   public double getLerpTarget() {
      return this.extent.getLerpTarget();
   }

   public void setSize(double pNewSize) {
      this.extent = new WorldBorder.StationaryBorderInfo(pNewSize);

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onBorderSizeSet(this, pNewSize);
      }

   }

   public void lerpSizeBetween(double pOldSize, double pNewSize, long pTime) {
      this.extent = (WorldBorder.IBorderInfo)(pOldSize == pNewSize ? new WorldBorder.StationaryBorderInfo(pNewSize) : new WorldBorder.MovingBorderInfo(pOldSize, pNewSize, pTime));

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onBorderSizeLerping(this, pOldSize, pNewSize, pTime);
      }

   }

   protected List<IBorderListener> getListeners() {
      return Lists.newArrayList(this.listeners);
   }

   public void addListener(IBorderListener pListener) {
      this.listeners.add(pListener);
   }

   public void removeListener(IBorderListener listener) {
      this.listeners.remove(listener);
   }

   public void setAbsoluteMaxSize(int pSize) {
      this.absoluteMaxSize = pSize;
      this.extent.onAbsoluteMaxSizeChange();
   }

   public int getAbsoluteMaxSize() {
      return this.absoluteMaxSize;
   }

   public double getDamageSafeZone() {
      return this.damageSafeZone;
   }

   public void setDamageSafeZone(double pBufferSize) {
      this.damageSafeZone = pBufferSize;

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onBorderSetDamageSafeZOne(this, pBufferSize);
      }

   }

   public double getDamagePerBlock() {
      return this.damagePerBlock;
   }

   public void setDamagePerBlock(double pNewAmount) {
      this.damagePerBlock = pNewAmount;

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onBorderSetDamagePerBlock(this, pNewAmount);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public double getLerpSpeed() {
      return this.extent.getLerpSpeed();
   }

   public int getWarningTime() {
      return this.warningTime;
   }

   public void setWarningTime(int pWarningTime) {
      this.warningTime = pWarningTime;

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onBorderSetWarningTime(this, pWarningTime);
      }

   }

   public int getWarningBlocks() {
      return this.warningBlocks;
   }

   public void setWarningBlocks(int pWarningDistance) {
      this.warningBlocks = pWarningDistance;

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onBorderSetWarningBlocks(this, pWarningDistance);
      }

   }

   public void tick() {
      this.extent = this.extent.update();
   }

   public WorldBorder.Serializer createSettings() {
      return new WorldBorder.Serializer(this);
   }

   public void applySettings(WorldBorder.Serializer pSerializer) {
      this.setCenter(pSerializer.getCenterX(), pSerializer.getCenterZ());
      this.setDamagePerBlock(pSerializer.getDamagePerBlock());
      this.setDamageSafeZone(pSerializer.getSafeZone());
      this.setWarningBlocks(pSerializer.getWarningBlocks());
      this.setWarningTime(pSerializer.getWarningTime());
      if (pSerializer.getSizeLerpTime() > 0L) {
         this.lerpSizeBetween(pSerializer.getSize(), pSerializer.getSizeLerpTarget(), pSerializer.getSizeLerpTime());
      } else {
         this.setSize(pSerializer.getSize());
      }

   }

   interface IBorderInfo {
      double getMinX();

      double getMaxX();

      double getMinZ();

      double getMaxZ();

      double getSize();

      @OnlyIn(Dist.CLIENT)
      double getLerpSpeed();

      long getLerpRemainingTime();

      double getLerpTarget();

      @OnlyIn(Dist.CLIENT)
      BorderStatus getStatus();

      void onAbsoluteMaxSizeChange();

      void onCenterChange();

      WorldBorder.IBorderInfo update();

      VoxelShape getCollisionShape();
   }

   class MovingBorderInfo implements WorldBorder.IBorderInfo {
      private final double from;
      private final double to;
      private final long lerpEnd;
      private final long lerpBegin;
      private final double lerpDuration;

      private MovingBorderInfo(double pFrom, double pTo, long pLerpDuration) {
         this.from = pFrom;
         this.to = pTo;
         this.lerpDuration = (double)pLerpDuration;
         this.lerpBegin = Util.getMillis();
         this.lerpEnd = this.lerpBegin + pLerpDuration;
      }

      public double getMinX() {
         return Math.max(WorldBorder.this.getCenterX() - this.getSize() / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize));
      }

      public double getMinZ() {
         return Math.max(WorldBorder.this.getCenterZ() - this.getSize() / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize));
      }

      public double getMaxX() {
         return Math.min(WorldBorder.this.getCenterX() + this.getSize() / 2.0D, (double)WorldBorder.this.absoluteMaxSize);
      }

      public double getMaxZ() {
         return Math.min(WorldBorder.this.getCenterZ() + this.getSize() / 2.0D, (double)WorldBorder.this.absoluteMaxSize);
      }

      public double getSize() {
         double d0 = (double)(Util.getMillis() - this.lerpBegin) / this.lerpDuration;
         return d0 < 1.0D ? MathHelper.lerp(d0, this.from, this.to) : this.to;
      }

      @OnlyIn(Dist.CLIENT)
      public double getLerpSpeed() {
         return Math.abs(this.from - this.to) / (double)(this.lerpEnd - this.lerpBegin);
      }

      public long getLerpRemainingTime() {
         return this.lerpEnd - Util.getMillis();
      }

      public double getLerpTarget() {
         return this.to;
      }

      @OnlyIn(Dist.CLIENT)
      public BorderStatus getStatus() {
         return this.to < this.from ? BorderStatus.SHRINKING : BorderStatus.GROWING;
      }

      public void onCenterChange() {
      }

      public void onAbsoluteMaxSizeChange() {
      }

      public WorldBorder.IBorderInfo update() {
         return (WorldBorder.IBorderInfo)(this.getLerpRemainingTime() <= 0L ? WorldBorder.this.new StationaryBorderInfo(this.to) : this);
      }

      public VoxelShape getCollisionShape() {
         return VoxelShapes.join(VoxelShapes.INFINITY, VoxelShapes.box(Math.floor(this.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ()), Math.ceil(this.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ())), IBooleanFunction.ONLY_FIRST);
      }
   }

   public static class Serializer {
      private final double centerX;
      private final double centerZ;
      private final double damagePerBlock;
      private final double safeZone;
      private final int warningBlocks;
      private final int warningTime;
      private final double size;
      private final long sizeLerpTime;
      private final double sizeLerpTarget;

      private Serializer(double pCenterX, double pCenterZ, double pDamagePerBlock, double pSafeZone, int pWarningBlocks, int pWarningTime, double pSize, long pSizeLerpTime, double pSizeLerpTarget) {
         this.centerX = pCenterX;
         this.centerZ = pCenterZ;
         this.damagePerBlock = pDamagePerBlock;
         this.safeZone = pSafeZone;
         this.warningBlocks = pWarningBlocks;
         this.warningTime = pWarningTime;
         this.size = pSize;
         this.sizeLerpTime = pSizeLerpTime;
         this.sizeLerpTarget = pSizeLerpTarget;
      }

      private Serializer(WorldBorder pBorder) {
         this.centerX = pBorder.getCenterX();
         this.centerZ = pBorder.getCenterZ();
         this.damagePerBlock = pBorder.getDamagePerBlock();
         this.safeZone = pBorder.getDamageSafeZone();
         this.warningBlocks = pBorder.getWarningBlocks();
         this.warningTime = pBorder.getWarningTime();
         this.size = pBorder.getSize();
         this.sizeLerpTime = pBorder.getLerpRemainingTime();
         this.sizeLerpTarget = pBorder.getLerpTarget();
      }

      public double getCenterX() {
         return this.centerX;
      }

      public double getCenterZ() {
         return this.centerZ;
      }

      public double getDamagePerBlock() {
         return this.damagePerBlock;
      }

      public double getSafeZone() {
         return this.safeZone;
      }

      public int getWarningBlocks() {
         return this.warningBlocks;
      }

      public int getWarningTime() {
         return this.warningTime;
      }

      public double getSize() {
         return this.size;
      }

      public long getSizeLerpTime() {
         return this.sizeLerpTime;
      }

      public double getSizeLerpTarget() {
         return this.sizeLerpTarget;
      }

      public static WorldBorder.Serializer read(DynamicLike<?> pDynamic, WorldBorder.Serializer pDefaultValue) {
         double d0 = pDynamic.get("BorderCenterX").asDouble(pDefaultValue.centerX);
         double d1 = pDynamic.get("BorderCenterZ").asDouble(pDefaultValue.centerZ);
         double d2 = pDynamic.get("BorderSize").asDouble(pDefaultValue.size);
         long i = pDynamic.get("BorderSizeLerpTime").asLong(pDefaultValue.sizeLerpTime);
         double d3 = pDynamic.get("BorderSizeLerpTarget").asDouble(pDefaultValue.sizeLerpTarget);
         double d4 = pDynamic.get("BorderSafeZone").asDouble(pDefaultValue.safeZone);
         double d5 = pDynamic.get("BorderDamagePerBlock").asDouble(pDefaultValue.damagePerBlock);
         int j = pDynamic.get("BorderWarningBlocks").asInt(pDefaultValue.warningBlocks);
         int k = pDynamic.get("BorderWarningTime").asInt(pDefaultValue.warningTime);
         return new WorldBorder.Serializer(d0, d1, d5, d4, j, k, d2, i, d3);
      }

      public void write(CompoundNBT pNbt) {
         pNbt.putDouble("BorderCenterX", this.centerX);
         pNbt.putDouble("BorderCenterZ", this.centerZ);
         pNbt.putDouble("BorderSize", this.size);
         pNbt.putLong("BorderSizeLerpTime", this.sizeLerpTime);
         pNbt.putDouble("BorderSafeZone", this.safeZone);
         pNbt.putDouble("BorderDamagePerBlock", this.damagePerBlock);
         pNbt.putDouble("BorderSizeLerpTarget", this.sizeLerpTarget);
         pNbt.putDouble("BorderWarningBlocks", (double)this.warningBlocks);
         pNbt.putDouble("BorderWarningTime", (double)this.warningTime);
      }
   }

   class StationaryBorderInfo implements WorldBorder.IBorderInfo {
      private final double size;
      private double minX;
      private double minZ;
      private double maxX;
      private double maxZ;
      private VoxelShape shape;

      public StationaryBorderInfo(double pSize) {
         this.size = pSize;
         this.updateBox();
      }

      public double getMinX() {
         return this.minX;
      }

      public double getMaxX() {
         return this.maxX;
      }

      public double getMinZ() {
         return this.minZ;
      }

      public double getMaxZ() {
         return this.maxZ;
      }

      public double getSize() {
         return this.size;
      }

      @OnlyIn(Dist.CLIENT)
      public BorderStatus getStatus() {
         return BorderStatus.STATIONARY;
      }

      @OnlyIn(Dist.CLIENT)
      public double getLerpSpeed() {
         return 0.0D;
      }

      public long getLerpRemainingTime() {
         return 0L;
      }

      public double getLerpTarget() {
         return this.size;
      }

      private void updateBox() {
         this.minX = Math.max(WorldBorder.this.getCenterX() - this.size / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize));
         this.minZ = Math.max(WorldBorder.this.getCenterZ() - this.size / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize));
         this.maxX = Math.min(WorldBorder.this.getCenterX() + this.size / 2.0D, (double)WorldBorder.this.absoluteMaxSize);
         this.maxZ = Math.min(WorldBorder.this.getCenterZ() + this.size / 2.0D, (double)WorldBorder.this.absoluteMaxSize);
         this.shape = VoxelShapes.join(VoxelShapes.INFINITY, VoxelShapes.box(Math.floor(this.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ()), Math.ceil(this.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ())), IBooleanFunction.ONLY_FIRST);
      }

      public void onAbsoluteMaxSizeChange() {
         this.updateBox();
      }

      public void onCenterChange() {
         this.updateBox();
      }

      public WorldBorder.IBorderInfo update() {
         return this;
      }

      public VoxelShape getCollisionShape() {
         return this.shape;
      }
   }
}

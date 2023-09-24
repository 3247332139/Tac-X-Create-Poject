package net.minecraft.util.math.shapes;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class VoxelShape {
   protected final VoxelShapePart shape;
   @Nullable
   private VoxelShape[] faces;

   VoxelShape(VoxelShapePart pShape) {
      this.shape = pShape;
   }

   public double min(Direction.Axis pAxis) {
      int i = this.shape.firstFull(pAxis);
      return i >= this.shape.getSize(pAxis) ? Double.POSITIVE_INFINITY : this.get(pAxis, i);
   }

   public double max(Direction.Axis pAxis) {
      int i = this.shape.lastFull(pAxis);
      return i <= 0 ? Double.NEGATIVE_INFINITY : this.get(pAxis, i);
   }

   public AxisAlignedBB bounds() {
      if (this.isEmpty()) {
         throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("No bounds for empty shape."));
      } else {
         return new AxisAlignedBB(this.min(Direction.Axis.X), this.min(Direction.Axis.Y), this.min(Direction.Axis.Z), this.max(Direction.Axis.X), this.max(Direction.Axis.Y), this.max(Direction.Axis.Z));
      }
   }

   protected double get(Direction.Axis pAxis, int pIndex) {
      return this.getCoords(pAxis).getDouble(pIndex);
   }

   protected abstract DoubleList getCoords(Direction.Axis pAxis);

   public boolean isEmpty() {
      return this.shape.isEmpty();
   }

   public VoxelShape move(double pXOffset, double pYOffset, double pZOffset) {
      return (VoxelShape)(this.isEmpty() ? VoxelShapes.empty() : new VoxelShapeArray(this.shape, (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.X), pXOffset)), (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.Y), pYOffset)), (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.Z), pZOffset))));
   }

   public VoxelShape optimize() {
      VoxelShape[] avoxelshape = new VoxelShape[]{VoxelShapes.empty()};
      this.forAllBoxes((p_197763_1_, p_197763_3_, p_197763_5_, p_197763_7_, p_197763_9_, p_197763_11_) -> {
         avoxelshape[0] = VoxelShapes.joinUnoptimized(avoxelshape[0], VoxelShapes.box(p_197763_1_, p_197763_3_, p_197763_5_, p_197763_7_, p_197763_9_, p_197763_11_), IBooleanFunction.OR);
      });
      return avoxelshape[0];
   }

   @OnlyIn(Dist.CLIENT)
   public void forAllEdges(VoxelShapes.ILineConsumer pAction) {
      this.shape.forAllEdges((p_197750_2_, p_197750_3_, p_197750_4_, p_197750_5_, p_197750_6_, p_197750_7_) -> {
         pAction.consume(this.get(Direction.Axis.X, p_197750_2_), this.get(Direction.Axis.Y, p_197750_3_), this.get(Direction.Axis.Z, p_197750_4_), this.get(Direction.Axis.X, p_197750_5_), this.get(Direction.Axis.Y, p_197750_6_), this.get(Direction.Axis.Z, p_197750_7_));
      }, true);
   }

   public void forAllBoxes(VoxelShapes.ILineConsumer pAction) {
      DoubleList doublelist = this.getCoords(Direction.Axis.X);
      DoubleList doublelist1 = this.getCoords(Direction.Axis.Y);
      DoubleList doublelist2 = this.getCoords(Direction.Axis.Z);
      this.shape.forAllBoxes((p_224789_4_, p_224789_5_, p_224789_6_, p_224789_7_, p_224789_8_, p_224789_9_) -> {
         pAction.consume(doublelist.getDouble(p_224789_4_), doublelist1.getDouble(p_224789_5_), doublelist2.getDouble(p_224789_6_), doublelist.getDouble(p_224789_7_), doublelist1.getDouble(p_224789_8_), doublelist2.getDouble(p_224789_9_));
      }, true);
   }

   public List<AxisAlignedBB> toAabbs() {
      List<AxisAlignedBB> list = Lists.newArrayList();
      this.forAllBoxes((p_203431_1_, p_203431_3_, p_203431_5_, p_203431_7_, p_203431_9_, p_203431_11_) -> {
         list.add(new AxisAlignedBB(p_203431_1_, p_203431_3_, p_203431_5_, p_203431_7_, p_203431_9_, p_203431_11_));
      });
      return list;
   }

   @OnlyIn(Dist.CLIENT)
   public double max(Direction.Axis pAxis, double pPrimaryPosition, double pSecondaryPosition) {
      Direction.Axis direction$axis = AxisRotation.FORWARD.cycle(pAxis);
      Direction.Axis direction$axis1 = AxisRotation.BACKWARD.cycle(pAxis);
      int i = this.findIndex(direction$axis, pPrimaryPosition);
      int j = this.findIndex(direction$axis1, pSecondaryPosition);
      int k = this.shape.lastFull(pAxis, i, j);
      return k <= 0 ? Double.NEGATIVE_INFINITY : this.get(pAxis, k);
   }

   protected int findIndex(Direction.Axis pAxis, double pPosition) {
      return MathHelper.binarySearch(0, this.shape.getSize(pAxis) + 1, (p_197761_4_) -> {
         if (p_197761_4_ < 0) {
            return false;
         } else if (p_197761_4_ > this.shape.getSize(pAxis)) {
            return true;
         } else {
            return pPosition < this.get(pAxis, p_197761_4_);
         }
      }) - 1;
   }

   protected boolean isFullWide(double p_211542_1_, double p_211542_3_, double p_211542_5_) {
      return this.shape.isFullWide(this.findIndex(Direction.Axis.X, p_211542_1_), this.findIndex(Direction.Axis.Y, p_211542_3_), this.findIndex(Direction.Axis.Z, p_211542_5_));
   }

   @Nullable
   public BlockRayTraceResult clip(Vector3d pStartVec, Vector3d pEndVec, BlockPos pPos) {
      if (this.isEmpty()) {
         return null;
      } else {
         Vector3d vector3d = pEndVec.subtract(pStartVec);
         if (vector3d.lengthSqr() < 1.0E-7D) {
            return null;
         } else {
            Vector3d vector3d1 = pStartVec.add(vector3d.scale(0.001D));
            return this.isFullWide(vector3d1.x - (double)pPos.getX(), vector3d1.y - (double)pPos.getY(), vector3d1.z - (double)pPos.getZ()) ? new BlockRayTraceResult(vector3d1, Direction.getNearest(vector3d.x, vector3d.y, vector3d.z).getOpposite(), pPos, true) : AxisAlignedBB.clip(this.toAabbs(), pStartVec, pEndVec, pPos);
         }
      }
   }

   /**
    * Projects" this shape onto the given side. For each box in the shape, if it does not touch the given side, it is
    * eliminated. Otherwise, the box is extended in the given axis to cover the entire range [0, 1].
    */
   public VoxelShape getFaceShape(Direction pSide) {
      if (!this.isEmpty() && this != VoxelShapes.block()) {
         if (this.faces != null) {
            VoxelShape voxelshape = this.faces[pSide.ordinal()];
            if (voxelshape != null) {
               return voxelshape;
            }
         } else {
            this.faces = new VoxelShape[6];
         }

         VoxelShape voxelshape1 = this.calculateFace(pSide);
         this.faces[pSide.ordinal()] = voxelshape1;
         return voxelshape1;
      } else {
         return this;
      }
   }

   private VoxelShape calculateFace(Direction pSide) {
      Direction.Axis direction$axis = pSide.getAxis();
      Direction.AxisDirection direction$axisdirection = pSide.getAxisDirection();
      DoubleList doublelist = this.getCoords(direction$axis);
      if (doublelist.size() == 2 && DoubleMath.fuzzyEquals(doublelist.getDouble(0), 0.0D, 1.0E-7D) && DoubleMath.fuzzyEquals(doublelist.getDouble(1), 1.0D, 1.0E-7D)) {
         return this;
      } else {
         int i = this.findIndex(direction$axis, direction$axisdirection == Direction.AxisDirection.POSITIVE ? 0.9999999D : 1.0E-7D);
         return new SplitVoxelShape(this, direction$axis, i);
      }
   }

   public double collide(Direction.Axis pMovementAxis, AxisAlignedBB pCollisionBox, double pDesiredOffset) {
      return this.collideX(AxisRotation.between(pMovementAxis, Direction.Axis.X), pCollisionBox, pDesiredOffset);
   }

   protected double collideX(AxisRotation pMovementAxis, AxisAlignedBB pCollisionBox, double pDesiredOffset) {
      if (this.isEmpty()) {
         return pDesiredOffset;
      } else if (Math.abs(pDesiredOffset) < 1.0E-7D) {
         return 0.0D;
      } else {
         AxisRotation axisrotation = pMovementAxis.inverse();
         Direction.Axis direction$axis = axisrotation.cycle(Direction.Axis.X);
         Direction.Axis direction$axis1 = axisrotation.cycle(Direction.Axis.Y);
         Direction.Axis direction$axis2 = axisrotation.cycle(Direction.Axis.Z);
         double d0 = pCollisionBox.max(direction$axis);
         double d1 = pCollisionBox.min(direction$axis);
         int i = this.findIndex(direction$axis, d1 + 1.0E-7D);
         int j = this.findIndex(direction$axis, d0 - 1.0E-7D);
         int k = Math.max(0, this.findIndex(direction$axis1, pCollisionBox.min(direction$axis1) + 1.0E-7D));
         int l = Math.min(this.shape.getSize(direction$axis1), this.findIndex(direction$axis1, pCollisionBox.max(direction$axis1) - 1.0E-7D) + 1);
         int i1 = Math.max(0, this.findIndex(direction$axis2, pCollisionBox.min(direction$axis2) + 1.0E-7D));
         int j1 = Math.min(this.shape.getSize(direction$axis2), this.findIndex(direction$axis2, pCollisionBox.max(direction$axis2) - 1.0E-7D) + 1);
         int k1 = this.shape.getSize(direction$axis);
         if (pDesiredOffset > 0.0D) {
            for(int l1 = j + 1; l1 < k1; ++l1) {
               for(int i2 = k; i2 < l; ++i2) {
                  for(int j2 = i1; j2 < j1; ++j2) {
                     if (this.shape.isFullWide(axisrotation, l1, i2, j2)) {
                        double d2 = this.get(direction$axis, l1) - d0;
                        if (d2 >= -1.0E-7D) {
                           pDesiredOffset = Math.min(pDesiredOffset, d2);
                        }

                        return pDesiredOffset;
                     }
                  }
               }
            }
         } else if (pDesiredOffset < 0.0D) {
            for(int k2 = i - 1; k2 >= 0; --k2) {
               for(int l2 = k; l2 < l; ++l2) {
                  for(int i3 = i1; i3 < j1; ++i3) {
                     if (this.shape.isFullWide(axisrotation, k2, l2, i3)) {
                        double d3 = this.get(direction$axis, k2 + 1) - d1;
                        if (d3 <= 1.0E-7D) {
                           pDesiredOffset = Math.max(pDesiredOffset, d3);
                        }

                        return pDesiredOffset;
                     }
                  }
               }
            }
         }

         return pDesiredOffset;
      }
   }

   public String toString() {
      return this.isEmpty() ? "EMPTY" : "VoxelShape[" + this.bounds() + "]";
   }
}
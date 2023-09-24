package net.minecraft.util.math.shapes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class VoxelShapes {
   private static final VoxelShape BLOCK = Util.make(() -> {
      VoxelShapePart voxelshapepart = new BitSetVoxelShapePart(1, 1, 1);
      voxelshapepart.setFull(0, 0, 0, true, true);
      return new VoxelShapeCube(voxelshapepart);
   });
   public static final VoxelShape INFINITY = box(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
   private static final VoxelShape EMPTY = new VoxelShapeArray(new BitSetVoxelShapePart(0, 0, 0), (DoubleList)(new DoubleArrayList(new double[]{0.0D})), (DoubleList)(new DoubleArrayList(new double[]{0.0D})), (DoubleList)(new DoubleArrayList(new double[]{0.0D})));

   public static VoxelShape empty() {
      return EMPTY;
   }

   public static VoxelShape block() {
      return BLOCK;
   }

   public static VoxelShape box(double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ) {
      return create(new AxisAlignedBB(pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ));
   }

   public static VoxelShape create(AxisAlignedBB pAabb) {
      int i = findBits(pAabb.minX, pAabb.maxX);
      int j = findBits(pAabb.minY, pAabb.maxY);
      int k = findBits(pAabb.minZ, pAabb.maxZ);
      if (i >= 0 && j >= 0 && k >= 0) {
         if (i == 0 && j == 0 && k == 0) {
            return pAabb.contains(0.5D, 0.5D, 0.5D) ? block() : empty();
         } else {
            int l = 1 << i;
            int i1 = 1 << j;
            int j1 = 1 << k;
            int k1 = (int)Math.round(pAabb.minX * (double)l);
            int l1 = (int)Math.round(pAabb.maxX * (double)l);
            int i2 = (int)Math.round(pAabb.minY * (double)i1);
            int j2 = (int)Math.round(pAabb.maxY * (double)i1);
            int k2 = (int)Math.round(pAabb.minZ * (double)j1);
            int l2 = (int)Math.round(pAabb.maxZ * (double)j1);
            BitSetVoxelShapePart bitsetvoxelshapepart = new BitSetVoxelShapePart(l, i1, j1, k1, i2, k2, l1, j2, l2);

            for(long i3 = (long)k1; i3 < (long)l1; ++i3) {
               for(long j3 = (long)i2; j3 < (long)j2; ++j3) {
                  for(long k3 = (long)k2; k3 < (long)l2; ++k3) {
                     bitsetvoxelshapepart.setFull((int)i3, (int)j3, (int)k3, false, true);
                  }
               }
            }

            return new VoxelShapeCube(bitsetvoxelshapepart);
         }
      } else {
         return new VoxelShapeArray(BLOCK.shape, new double[]{pAabb.minX, pAabb.maxX}, new double[]{pAabb.minY, pAabb.maxY}, new double[]{pAabb.minZ, pAabb.maxZ});
      }
   }

   private static int findBits(double pMinBits, double pMaxBits) {
      if (!(pMinBits < -1.0E-7D) && !(pMaxBits > 1.0000001D)) {
         for(int i = 0; i <= 3; ++i) {
            double d0 = pMinBits * (double)(1 << i);
            double d1 = pMaxBits * (double)(1 << i);
            boolean flag = Math.abs(d0 - Math.floor(d0)) < 1.0E-7D;
            boolean flag1 = Math.abs(d1 - Math.floor(d1)) < 1.0E-7D;
            if (flag && flag1) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   protected static long lcm(int pAa, int pBb) {
      return (long)pAa * (long)(pBb / IntMath.gcd(pAa, pBb));
   }

   public static VoxelShape or(VoxelShape p_197872_0_, VoxelShape p_197872_1_) {
      return join(p_197872_0_, p_197872_1_, IBooleanFunction.OR);
   }

   public static VoxelShape or(VoxelShape p_216384_0_, VoxelShape... p_216384_1_) {
      return Arrays.stream(p_216384_1_).reduce(p_216384_0_, VoxelShapes::or);
   }

   public static VoxelShape join(VoxelShape pShape1, VoxelShape pShape2, IBooleanFunction pFunction) {
      return joinUnoptimized(pShape1, pShape2, pFunction).optimize();
   }

   public static VoxelShape joinUnoptimized(VoxelShape pShape1, VoxelShape pShape2, IBooleanFunction pFunction) {
      if (pFunction.apply(false, false)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
      } else if (pShape1 == pShape2) {
         return pFunction.apply(true, true) ? pShape1 : empty();
      } else {
         boolean flag = pFunction.apply(true, false);
         boolean flag1 = pFunction.apply(false, true);
         if (pShape1.isEmpty()) {
            return flag1 ? pShape2 : empty();
         } else if (pShape2.isEmpty()) {
            return flag ? pShape1 : empty();
         } else {
            IDoubleListMerger idoublelistmerger = createIndexMerger(1, pShape1.getCoords(Direction.Axis.X), pShape2.getCoords(Direction.Axis.X), flag, flag1);
            IDoubleListMerger idoublelistmerger1 = createIndexMerger(idoublelistmerger.getList().size() - 1, pShape1.getCoords(Direction.Axis.Y), pShape2.getCoords(Direction.Axis.Y), flag, flag1);
            IDoubleListMerger idoublelistmerger2 = createIndexMerger((idoublelistmerger.getList().size() - 1) * (idoublelistmerger1.getList().size() - 1), pShape1.getCoords(Direction.Axis.Z), pShape2.getCoords(Direction.Axis.Z), flag, flag1);
            BitSetVoxelShapePart bitsetvoxelshapepart = BitSetVoxelShapePart.join(pShape1.shape, pShape2.shape, idoublelistmerger, idoublelistmerger1, idoublelistmerger2, pFunction);
            return (VoxelShape)(idoublelistmerger instanceof DoubleCubeMergingList && idoublelistmerger1 instanceof DoubleCubeMergingList && idoublelistmerger2 instanceof DoubleCubeMergingList ? new VoxelShapeCube(bitsetvoxelshapepart) : new VoxelShapeArray(bitsetvoxelshapepart, idoublelistmerger.getList(), idoublelistmerger1.getList(), idoublelistmerger2.getList()));
         }
      }
   }

   public static boolean joinIsNotEmpty(VoxelShape pShape1, VoxelShape pShape2, IBooleanFunction pResultOperator) {
      if (pResultOperator.apply(false, false)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
      } else if (pShape1 == pShape2) {
         return pResultOperator.apply(true, true);
      } else if (pShape1.isEmpty()) {
         return pResultOperator.apply(false, !pShape2.isEmpty());
      } else if (pShape2.isEmpty()) {
         return pResultOperator.apply(!pShape1.isEmpty(), false);
      } else {
         boolean flag = pResultOperator.apply(true, false);
         boolean flag1 = pResultOperator.apply(false, true);

         for(Direction.Axis direction$axis : AxisRotation.AXIS_VALUES) {
            if (pShape1.max(direction$axis) < pShape2.min(direction$axis) - 1.0E-7D) {
               return flag || flag1;
            }

            if (pShape2.max(direction$axis) < pShape1.min(direction$axis) - 1.0E-7D) {
               return flag || flag1;
            }
         }

         IDoubleListMerger idoublelistmerger = createIndexMerger(1, pShape1.getCoords(Direction.Axis.X), pShape2.getCoords(Direction.Axis.X), flag, flag1);
         IDoubleListMerger idoublelistmerger1 = createIndexMerger(idoublelistmerger.getList().size() - 1, pShape1.getCoords(Direction.Axis.Y), pShape2.getCoords(Direction.Axis.Y), flag, flag1);
         IDoubleListMerger idoublelistmerger2 = createIndexMerger((idoublelistmerger.getList().size() - 1) * (idoublelistmerger1.getList().size() - 1), pShape1.getCoords(Direction.Axis.Z), pShape2.getCoords(Direction.Axis.Z), flag, flag1);
         return joinIsNotEmpty(idoublelistmerger, idoublelistmerger1, idoublelistmerger2, pShape1.shape, pShape2.shape, pResultOperator);
      }
   }

   private static boolean joinIsNotEmpty(IDoubleListMerger pMergerX, IDoubleListMerger pMergerY, IDoubleListMerger pMergerZ, VoxelShapePart pPrimaryShape, VoxelShapePart pSecondaryShape, IBooleanFunction pResultOperator) {
      return !pMergerX.forMergedIndexes((p_199861_5_, p_199861_6_, p_199861_7_) -> {
         return pMergerY.forMergedIndexes((p_199860_6_, p_199860_7_, p_199860_8_) -> {
            return pMergerZ.forMergedIndexes((p_199862_7_, p_199862_8_, p_199862_9_) -> {
               return !pResultOperator.apply(pPrimaryShape.isFullWide(p_199861_5_, p_199860_6_, p_199862_7_), pSecondaryShape.isFullWide(p_199861_6_, p_199860_7_, p_199862_8_));
            });
         });
      });
   }

   public static double collide(Direction.Axis pMovementAxis, AxisAlignedBB pCollisionBox, Stream<VoxelShape> pPossibleHits, double pDesiredOffset) {
      for(Iterator<VoxelShape> iterator = pPossibleHits.iterator(); iterator.hasNext(); pDesiredOffset = iterator.next().collide(pMovementAxis, pCollisionBox, pDesiredOffset)) {
         if (Math.abs(pDesiredOffset) < 1.0E-7D) {
            return 0.0D;
         }
      }

      return pDesiredOffset;
   }

   public static double collide(Direction.Axis pMovementAxis, AxisAlignedBB pCollisionBox, IWorldReader pLevelReader, double pDesiredOffset, ISelectionContext pSelectionContext, Stream<VoxelShape> pPossibleHits) {
      return collide(pCollisionBox, pLevelReader, pDesiredOffset, pSelectionContext, AxisRotation.between(pMovementAxis, Direction.Axis.Z), pPossibleHits);
   }

   private static double collide(AxisAlignedBB pCollisionBox, IWorldReader pLevelReader, double pDesiredOffset, ISelectionContext pSelectionContext, AxisRotation pRotationAxis, Stream<VoxelShape> pPossibleHits) {
      if (!(pCollisionBox.getXsize() < 1.0E-6D) && !(pCollisionBox.getYsize() < 1.0E-6D) && !(pCollisionBox.getZsize() < 1.0E-6D)) {
         if (Math.abs(pDesiredOffset) < 1.0E-7D) {
            return 0.0D;
         } else {
            AxisRotation axisrotation = pRotationAxis.inverse();
            Direction.Axis direction$axis = axisrotation.cycle(Direction.Axis.X);
            Direction.Axis direction$axis1 = axisrotation.cycle(Direction.Axis.Y);
            Direction.Axis direction$axis2 = axisrotation.cycle(Direction.Axis.Z);
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
            int i = MathHelper.floor(pCollisionBox.min(direction$axis) - 1.0E-7D) - 1;
            int j = MathHelper.floor(pCollisionBox.max(direction$axis) + 1.0E-7D) + 1;
            int k = MathHelper.floor(pCollisionBox.min(direction$axis1) - 1.0E-7D) - 1;
            int l = MathHelper.floor(pCollisionBox.max(direction$axis1) + 1.0E-7D) + 1;
            double d0 = pCollisionBox.min(direction$axis2) - 1.0E-7D;
            double d1 = pCollisionBox.max(direction$axis2) + 1.0E-7D;
            boolean flag = pDesiredOffset > 0.0D;
            int i1 = flag ? MathHelper.floor(pCollisionBox.max(direction$axis2) - 1.0E-7D) - 1 : MathHelper.floor(pCollisionBox.min(direction$axis2) + 1.0E-7D) + 1;
            int j1 = lastC(pDesiredOffset, d0, d1);
            int k1 = flag ? 1 : -1;
            int l1 = i1;

            while(true) {
               if (flag) {
                  if (l1 > j1) {
                     break;
                  }
               } else if (l1 < j1) {
                  break;
               }

               for(int i2 = i; i2 <= j; ++i2) {
                  for(int j2 = k; j2 <= l; ++j2) {
                     int k2 = 0;
                     if (i2 == i || i2 == j) {
                        ++k2;
                     }

                     if (j2 == k || j2 == l) {
                        ++k2;
                     }

                     if (l1 == i1 || l1 == j1) {
                        ++k2;
                     }

                     if (k2 < 3) {
                        blockpos$mutable.set(axisrotation, i2, j2, l1);
                        BlockState blockstate = pLevelReader.getBlockState(blockpos$mutable);
                        if ((k2 != 1 || blockstate.hasLargeCollisionShape()) && (k2 != 2 || blockstate.is(Blocks.MOVING_PISTON))) {
                           pDesiredOffset = blockstate.getCollisionShape(pLevelReader, blockpos$mutable, pSelectionContext).collide(direction$axis2, pCollisionBox.move((double)(-blockpos$mutable.getX()), (double)(-blockpos$mutable.getY()), (double)(-blockpos$mutable.getZ())), pDesiredOffset);
                           if (Math.abs(pDesiredOffset) < 1.0E-7D) {
                              return 0.0D;
                           }

                           j1 = lastC(pDesiredOffset, d0, d1);
                        }
                     }
                  }
               }

               l1 += k1;
            }

            double[] adouble = new double[]{pDesiredOffset};
            pPossibleHits.forEach((p_216388_3_) -> {
               adouble[0] = p_216388_3_.collide(direction$axis2, pCollisionBox, adouble[0]);
            });
            return adouble[0];
         }
      } else {
         return pDesiredOffset;
      }
   }

   private static int lastC(double pDesiredOffset, double pMin, double pMax) {
      return pDesiredOffset > 0.0D ? MathHelper.floor(pMax + pDesiredOffset) + 1 : MathHelper.floor(pMin + pDesiredOffset) - 1;
   }

   @OnlyIn(Dist.CLIENT)
   public static boolean blockOccudes(VoxelShape pShape, VoxelShape pAdjacentShape, Direction pSide) {
      if (pShape == block() && pAdjacentShape == block()) {
         return true;
      } else if (pAdjacentShape.isEmpty()) {
         return false;
      } else {
         Direction.Axis direction$axis = pSide.getAxis();
         Direction.AxisDirection direction$axisdirection = pSide.getAxisDirection();
         VoxelShape voxelshape = direction$axisdirection == Direction.AxisDirection.POSITIVE ? pShape : pAdjacentShape;
         VoxelShape voxelshape1 = direction$axisdirection == Direction.AxisDirection.POSITIVE ? pAdjacentShape : pShape;
         IBooleanFunction ibooleanfunction = direction$axisdirection == Direction.AxisDirection.POSITIVE ? IBooleanFunction.ONLY_FIRST : IBooleanFunction.ONLY_SECOND;
         return DoubleMath.fuzzyEquals(voxelshape.max(direction$axis), 1.0D, 1.0E-7D) && DoubleMath.fuzzyEquals(voxelshape1.min(direction$axis), 0.0D, 1.0E-7D) && !joinIsNotEmpty(new SplitVoxelShape(voxelshape, direction$axis, voxelshape.shape.getSize(direction$axis) - 1), new SplitVoxelShape(voxelshape1, direction$axis, 0), ibooleanfunction);
      }
   }

   public static VoxelShape getFaceShape(VoxelShape pVoxelShape, Direction pDirection) {
      if (pVoxelShape == block()) {
         return block();
      } else {
         Direction.Axis direction$axis = pDirection.getAxis();
         boolean flag;
         int i;
         if (pDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            flag = DoubleMath.fuzzyEquals(pVoxelShape.max(direction$axis), 1.0D, 1.0E-7D);
            i = pVoxelShape.shape.getSize(direction$axis) - 1;
         } else {
            flag = DoubleMath.fuzzyEquals(pVoxelShape.min(direction$axis), 0.0D, 1.0E-7D);
            i = 0;
         }

         return (VoxelShape)(!flag ? empty() : new SplitVoxelShape(pVoxelShape, direction$axis, i));
      }
   }

   public static boolean mergedFaceOccludes(VoxelShape pShape, VoxelShape pAdjacentShape, Direction pSide) {
      if (pShape != block() && pAdjacentShape != block()) {
         Direction.Axis direction$axis = pSide.getAxis();
         Direction.AxisDirection direction$axisdirection = pSide.getAxisDirection();
         VoxelShape voxelshape = direction$axisdirection == Direction.AxisDirection.POSITIVE ? pShape : pAdjacentShape;
         VoxelShape voxelshape1 = direction$axisdirection == Direction.AxisDirection.POSITIVE ? pAdjacentShape : pShape;
         if (!DoubleMath.fuzzyEquals(voxelshape.max(direction$axis), 1.0D, 1.0E-7D)) {
            voxelshape = empty();
         }

         if (!DoubleMath.fuzzyEquals(voxelshape1.min(direction$axis), 0.0D, 1.0E-7D)) {
            voxelshape1 = empty();
         }

         return !joinIsNotEmpty(block(), joinUnoptimized(new SplitVoxelShape(voxelshape, direction$axis, voxelshape.shape.getSize(direction$axis) - 1), new SplitVoxelShape(voxelshape1, direction$axis, 0), IBooleanFunction.OR), IBooleanFunction.ONLY_FIRST);
      } else {
         return true;
      }
   }

   public static boolean faceShapeOccludes(VoxelShape pVoxelShape1, VoxelShape pVoxelShape2) {
      if (pVoxelShape1 != block() && pVoxelShape2 != block()) {
         if (pVoxelShape1.isEmpty() && pVoxelShape2.isEmpty()) {
            return false;
         } else {
            return !joinIsNotEmpty(block(), joinUnoptimized(pVoxelShape1, pVoxelShape2, IBooleanFunction.OR), IBooleanFunction.ONLY_FIRST);
         }
      } else {
         return true;
      }
   }

   @VisibleForTesting
   protected static IDoubleListMerger createIndexMerger(int pSize, DoubleList pList1, DoubleList pList2, boolean p_199410_3_, boolean p_199410_4_) {
      int i = pList1.size() - 1;
      int j = pList2.size() - 1;
      if (pList1 instanceof DoubleRangeList && pList2 instanceof DoubleRangeList) {
         long k = lcm(i, j);
         if ((long)pSize * k <= 256L) {
            return new DoubleCubeMergingList(i, j);
         }
      }

      if (pList1.getDouble(i) < pList2.getDouble(0) - 1.0E-7D) {
         return new NonOverlappingMerger(pList1, pList2, false);
      } else if (pList2.getDouble(j) < pList1.getDouble(0) - 1.0E-7D) {
         return new NonOverlappingMerger(pList2, pList1, true);
      } else if (i == j && Objects.equals(pList1, pList2)) {
         if (pList1 instanceof SimpleDoubleMerger) {
            return (IDoubleListMerger)pList1;
         } else {
            return (IDoubleListMerger)(pList2 instanceof SimpleDoubleMerger ? (IDoubleListMerger)pList2 : new SimpleDoubleMerger(pList1));
         }
      } else {
         return new IndirectMerger(pList1, pList2, p_199410_3_, p_199410_4_);
      }
   }

   public interface ILineConsumer {
      void consume(double p_consume_1_, double p_consume_3_, double p_consume_5_, double p_consume_7_, double p_consume_9_, double p_consume_11_);
   }
}
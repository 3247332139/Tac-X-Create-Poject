package net.minecraft.pathfinding;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PathPoint {
   public final int x;
   public final int y;
   public final int z;
   private final int hash;
   /** The index in the PathHeap. -1 if not assigned. */
   public int heapIdx = -1;
   /** The total cost of all path points up to this one. Corresponds to the A* g-score. */
   public float g;
   /** The estimated cost from this path point to the target. Corresponds to the A* h-score. */
   public float h;
   /**
    * The total cost of the path containing this path point. Used as sort criteria in PathHeap. Corresponds to the A* f-
    * score.
    */
   public float f;
   public PathPoint cameFrom;
   public boolean closed;
   public float walkedDistance;
   /** The additional cost of the path point. If negative, the path point will be sorted out by NodeProcessors. */
   public float costMalus;
   public PathNodeType type = PathNodeType.BLOCKED;

   public PathPoint(int p_i2135_1_, int p_i2135_2_, int p_i2135_3_) {
      this.x = p_i2135_1_;
      this.y = p_i2135_2_;
      this.z = p_i2135_3_;
      this.hash = createHash(p_i2135_1_, p_i2135_2_, p_i2135_3_);
   }

   public PathPoint cloneAndMove(int pX, int pY, int pZ) {
      PathPoint pathpoint = new PathPoint(pX, pY, pZ);
      pathpoint.heapIdx = this.heapIdx;
      pathpoint.g = this.g;
      pathpoint.h = this.h;
      pathpoint.f = this.f;
      pathpoint.cameFrom = this.cameFrom;
      pathpoint.closed = this.closed;
      pathpoint.walkedDistance = this.walkedDistance;
      pathpoint.costMalus = this.costMalus;
      pathpoint.type = this.type;
      return pathpoint;
   }

   public static int createHash(int pX, int pY, int pZ) {
      return pY & 255 | (pX & 32767) << 8 | (pZ & 32767) << 24 | (pX < 0 ? Integer.MIN_VALUE : 0) | (pZ < 0 ? '\u8000' : 0);
   }

   /**
    * Returns the linear distance to another path point
    */
   public float distanceTo(PathPoint pPathpoint) {
      float f = (float)(pPathpoint.x - this.x);
      float f1 = (float)(pPathpoint.y - this.y);
      float f2 = (float)(pPathpoint.z - this.z);
      return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   /**
    * Returns the squared distance to another path point
    */
   public float distanceToSqr(PathPoint pPathpoint) {
      float f = (float)(pPathpoint.x - this.x);
      float f1 = (float)(pPathpoint.y - this.y);
      float f2 = (float)(pPathpoint.z - this.z);
      return f * f + f1 * f1 + f2 * f2;
   }

   public float distanceManhattan(PathPoint p_224757_1_) {
      float f = (float)Math.abs(p_224757_1_.x - this.x);
      float f1 = (float)Math.abs(p_224757_1_.y - this.y);
      float f2 = (float)Math.abs(p_224757_1_.z - this.z);
      return f + f1 + f2;
   }

   public float distanceManhattan(BlockPos p_224758_1_) {
      float f = (float)Math.abs(p_224758_1_.getX() - this.x);
      float f1 = (float)Math.abs(p_224758_1_.getY() - this.y);
      float f2 = (float)Math.abs(p_224758_1_.getZ() - this.z);
      return f + f1 + f2;
   }

   public BlockPos asBlockPos() {
      return new BlockPos(this.x, this.y, this.z);
   }

   public boolean equals(Object p_equals_1_) {
      if (!(p_equals_1_ instanceof PathPoint)) {
         return false;
      } else {
         PathPoint pathpoint = (PathPoint)p_equals_1_;
         return this.hash == pathpoint.hash && this.x == pathpoint.x && this.y == pathpoint.y && this.z == pathpoint.z;
      }
   }

   public int hashCode() {
      return this.hash;
   }

   /**
    * Returns true if this point has already been assigned to a path
    */
   public boolean inOpenSet() {
      return this.heapIdx >= 0;
   }

   public String toString() {
      return "Node{x=" + this.x + ", y=" + this.y + ", z=" + this.z + '}';
   }

   @OnlyIn(Dist.CLIENT)
   public static PathPoint createFromStream(PacketBuffer pBuf) {
      PathPoint pathpoint = new PathPoint(pBuf.readInt(), pBuf.readInt(), pBuf.readInt());
      pathpoint.walkedDistance = pBuf.readFloat();
      pathpoint.costMalus = pBuf.readFloat();
      pathpoint.closed = pBuf.readBoolean();
      pathpoint.type = PathNodeType.values()[pBuf.readInt()];
      pathpoint.f = pBuf.readFloat();
      return pathpoint;
   }
}
package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VisGraph {
   private static final int DX = (int)Math.pow(16.0D, 0.0D);
   private static final int DZ = (int)Math.pow(16.0D, 1.0D);
   private static final int DY = (int)Math.pow(16.0D, 2.0D);
   private static final Direction[] DIRECTIONS = Direction.values();
   private final BitSet bitSet = new BitSet(4096);
   private static final int[] INDEX_OF_EDGES = Util.make(new int[1352], (p_209264_0_) -> {
      int i = 0;
      int j = 15;
      int k = 0;

      for(int l = 0; l < 16; ++l) {
         for(int i1 = 0; i1 < 16; ++i1) {
            for(int j1 = 0; j1 < 16; ++j1) {
               if (l == 0 || l == 15 || i1 == 0 || i1 == 15 || j1 == 0 || j1 == 15) {
                  p_209264_0_[k++] = getIndex(l, i1, j1);
               }
            }
         }
      }

   });
   private int empty = 4096;

   public void setOpaque(BlockPos pPos) {
      this.bitSet.set(getIndex(pPos), true);
      --this.empty;
   }

   private static int getIndex(BlockPos pPos) {
      return getIndex(pPos.getX() & 15, pPos.getY() & 15, pPos.getZ() & 15);
   }

   private static int getIndex(int pX, int pY, int pZ) {
      return pX << 0 | pY << 8 | pZ << 4;
   }

   public SetVisibility resolve() {
      SetVisibility setvisibility = new SetVisibility();
      if (4096 - this.empty < 256) {
         setvisibility.setAll(true);
      } else if (this.empty == 0) {
         setvisibility.setAll(false);
      } else {
         for(int i : INDEX_OF_EDGES) {
            if (!this.bitSet.get(i)) {
               setvisibility.add(this.floodFill(i));
            }
         }
      }

      return setvisibility;
   }

   private Set<Direction> floodFill(int pPos) {
      Set<Direction> set = EnumSet.noneOf(Direction.class);
      IntPriorityQueue intpriorityqueue = new IntArrayFIFOQueue();
      intpriorityqueue.enqueue(pPos);
      this.bitSet.set(pPos, true);

      while(!intpriorityqueue.isEmpty()) {
         int i = intpriorityqueue.dequeueInt();
         this.addEdges(i, set);

         for(Direction direction : DIRECTIONS) {
            int j = this.getNeighborIndexAtFace(i, direction);
            if (j >= 0 && !this.bitSet.get(j)) {
               this.bitSet.set(j, true);
               intpriorityqueue.enqueue(j);
            }
         }
      }

      return set;
   }

   private void addEdges(int pPos, Set<Direction> pSetFacings) {
      int i = pPos >> 0 & 15;
      if (i == 0) {
         pSetFacings.add(Direction.WEST);
      } else if (i == 15) {
         pSetFacings.add(Direction.EAST);
      }

      int j = pPos >> 8 & 15;
      if (j == 0) {
         pSetFacings.add(Direction.DOWN);
      } else if (j == 15) {
         pSetFacings.add(Direction.UP);
      }

      int k = pPos >> 4 & 15;
      if (k == 0) {
         pSetFacings.add(Direction.NORTH);
      } else if (k == 15) {
         pSetFacings.add(Direction.SOUTH);
      }

   }

   private int getNeighborIndexAtFace(int pPos, Direction pFacing) {
      switch(pFacing) {
      case DOWN:
         if ((pPos >> 8 & 15) == 0) {
            return -1;
         }

         return pPos - DY;
      case UP:
         if ((pPos >> 8 & 15) == 15) {
            return -1;
         }

         return pPos + DY;
      case NORTH:
         if ((pPos >> 4 & 15) == 0) {
            return -1;
         }

         return pPos - DZ;
      case SOUTH:
         if ((pPos >> 4 & 15) == 15) {
            return -1;
         }

         return pPos + DZ;
      case WEST:
         if ((pPos >> 0 & 15) == 0) {
            return -1;
         }

         return pPos - DX;
      case EAST:
         if ((pPos >> 0 & 15) == 15) {
            return -1;
         }

         return pPos + DX;
      default:
         return -1;
      }
   }
}
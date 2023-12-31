package net.minecraft.world;

import java.util.Comparator;
import net.minecraft.util.math.BlockPos;

public class NextTickListEntry<T> {
   private static long counter;
   private final T type;
   public final BlockPos pos;
   public final long triggerTick;
   public final TickPriority priority;
   private final long c;

   public NextTickListEntry(BlockPos pPos, T pType) {
      this(pPos, pType, 0L, TickPriority.NORMAL);
   }

   public NextTickListEntry(BlockPos pPos, T pType, long pTriggerTick, TickPriority pPriority) {
      this.c = (long)(counter++);
      this.pos = pPos.immutable();
      this.type = pType;
      this.triggerTick = pTriggerTick;
      this.priority = pPriority;
   }

   public boolean equals(Object p_equals_1_) {
      if (!(p_equals_1_ instanceof NextTickListEntry)) {
         return false;
      } else {
         NextTickListEntry<?> nextticklistentry = (NextTickListEntry)p_equals_1_;
         return this.pos.equals(nextticklistentry.pos) && this.type == nextticklistentry.type;
      }
   }

   public int hashCode() {
      return this.pos.hashCode();
   }

   public static <T> Comparator<NextTickListEntry<T>> createTimeComparator() {
      return Comparator.<NextTickListEntry<T>>comparingLong((p_226710_0_) -> {
         return p_226710_0_.triggerTick;
      }).thenComparing((p_226709_0_) -> {
         return p_226709_0_.priority;
      }).thenComparingLong((p_226708_0_) -> {
         return p_226708_0_.c;
      });
   }

   public String toString() {
      return this.type + ": " + this.pos + ", " + this.triggerTick + ", " + this.priority + ", " + this.c;
   }

   public T getType() {
      return this.type;
   }
}
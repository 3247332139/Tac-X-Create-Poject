package net.minecraft.util.math.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

interface IDoubleListMerger {
   DoubleList getList();

   boolean forMergedIndexes(IDoubleListMerger.IConsumer pConsumer);

   public interface IConsumer {
      boolean merge(int p_merge_1_, int p_merge_2_, int p_merge_3_);
   }
}
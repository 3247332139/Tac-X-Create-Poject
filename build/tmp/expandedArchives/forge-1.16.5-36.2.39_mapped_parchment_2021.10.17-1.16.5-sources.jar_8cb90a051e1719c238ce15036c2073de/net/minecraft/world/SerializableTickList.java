package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class SerializableTickList<T> implements ITickList<T> {
   private final List<SerializableTickList.TickHolder<T>> ticks;
   private final Function<T, ResourceLocation> toId;

   public SerializableTickList(Function<T, ResourceLocation> pToId, List<NextTickListEntry<T>> pTicks, long pGameTime) {
      this(pToId, pTicks.stream().map((p_234854_2_) -> {
         return new SerializableTickList.TickHolder<>(p_234854_2_.getType(), p_234854_2_.pos, (int)(p_234854_2_.triggerTick - pGameTime), p_234854_2_.priority);
      }).collect(Collectors.toList()));
   }

   private SerializableTickList(Function<T, ResourceLocation> pToId, List<SerializableTickList.TickHolder<T>> pTicks) {
      this.ticks = pTicks;
      this.toId = pToId;
   }

   public boolean hasScheduledTick(BlockPos pPos, T pItem) {
      return false;
   }

   public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority pPriority) {
      this.ticks.add(new SerializableTickList.TickHolder<>(pItem, pPos, pScheduledTime, pPriority));
   }

   /**
    * Checks if this position/item is scheduled to be updated this tick
    */
   public boolean willTickThisTick(BlockPos pPos, T pObj) {
      return false;
   }

   public ListNBT save() {
      ListNBT listnbt = new ListNBT();

      for(SerializableTickList.TickHolder<T> tickholder : this.ticks) {
         CompoundNBT compoundnbt = new CompoundNBT();
         compoundnbt.putString("i", this.toId.apply(tickholder.type).toString());
         compoundnbt.putInt("x", tickholder.pos.getX());
         compoundnbt.putInt("y", tickholder.pos.getY());
         compoundnbt.putInt("z", tickholder.pos.getZ());
         compoundnbt.putInt("t", tickholder.delay);
         compoundnbt.putInt("p", tickholder.priority.getValue());
         listnbt.add(compoundnbt);
      }

      return listnbt;
   }

   public static <T> SerializableTickList<T> create(ListNBT pList, Function<T, ResourceLocation> p_222984_1_, Function<ResourceLocation, T> p_222984_2_) {
      List<SerializableTickList.TickHolder<T>> list = Lists.newArrayList();

      for(int i = 0; i < pList.size(); ++i) {
         CompoundNBT compoundnbt = pList.getCompound(i);
         T t = p_222984_2_.apply(new ResourceLocation(compoundnbt.getString("i")));
         if (t != null) {
            BlockPos blockpos = new BlockPos(compoundnbt.getInt("x"), compoundnbt.getInt("y"), compoundnbt.getInt("z"));
            list.add(new SerializableTickList.TickHolder<>(t, blockpos, compoundnbt.getInt("t"), TickPriority.byValue(compoundnbt.getInt("p"))));
         }
      }

      return new SerializableTickList<>(p_222984_1_, list);
   }

   public void copyOut(ITickList<T> pTickList) {
      this.ticks.forEach((p_234856_1_) -> {
         pTickList.scheduleTick(p_234856_1_.pos, p_234856_1_.type, p_234856_1_.delay, p_234856_1_.priority);
      });
   }

   static class TickHolder<T> {
      private final T type;
      public final BlockPos pos;
      public final int delay;
      public final TickPriority priority;

      private TickHolder(T pType, BlockPos pPos, int pDelay, TickPriority pPriority) {
         this.type = pType;
         this.pos = pPos;
         this.delay = pDelay;
         this.priority = pPriority;
      }

      public String toString() {
         return this.type + ": " + this.pos + ", " + this.delay + ", " + this.priority;
      }
   }
}
package net.minecraft.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.server.ServerWorld;

public class ForgetAdmiredItemTask<E extends PiglinEntity> extends Task<E> {
   private final int maxDistanceToItem;

   public ForgetAdmiredItemTask(int p_i231574_1_) {
      super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleStatus.REGISTERED));
      this.maxDistanceToItem = p_i231574_1_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, E pOwner) {
      if (!pOwner.getOffhandItem().isEmpty()) {
         return false;
      } else {
         Optional<ItemEntity> optional = pOwner.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
         if (!optional.isPresent()) {
            return true;
         } else {
            return !optional.get().closerThan(pOwner, (double)this.maxDistanceToItem);
         }
      }
   }

   protected void start(ServerWorld pLevel, E pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.ADMIRING_ITEM);
   }
}
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.server.ServerWorld;

public class WantedItemsSensor extends Sensor<MobEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
   }

   protected void doTick(ServerWorld pLevel, MobEntity pEntity) {
      Brain<?> brain = pEntity.getBrain();
      List<ItemEntity> list = pLevel.getEntitiesOfClass(ItemEntity.class, pEntity.getBoundingBox().inflate(8.0D, 4.0D, 8.0D), (p_234123_0_) -> {
         return true;
      });
      list.sort(Comparator.comparingDouble(pEntity::distanceToSqr));
      Optional<ItemEntity> optional = list.stream().filter((p_234124_1_) -> {
         return pEntity.wantsToPickUp(p_234124_1_.getItem());
      }).filter((p_234122_1_) -> {
         return p_234122_1_.closerThan(pEntity, 9.0D);
      }).filter(pEntity::canSee).findFirst();
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, optional);
   }
}
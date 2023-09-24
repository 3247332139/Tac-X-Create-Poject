package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.world.server.ServerWorld;

public class NearestPlayersSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
   }

   protected void doTick(ServerWorld pLevel, LivingEntity pEntity) {
      List<PlayerEntity> list = pLevel.players().stream().filter(EntityPredicates.NO_SPECTATORS).filter((p_220979_1_) -> {
         return pEntity.closerThan(p_220979_1_, 16.0D);
      }).sorted(Comparator.comparingDouble(pEntity::distanceToSqr)).collect(Collectors.toList());
      Brain<?> brain = pEntity.getBrain();
      brain.setMemory(MemoryModuleType.NEAREST_PLAYERS, list);
      List<PlayerEntity> list1 = list.stream().filter((p_234128_1_) -> {
         return isEntityTargetable(pEntity, p_234128_1_);
      }).collect(Collectors.toList());
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, list1.isEmpty() ? null : list1.get(0));
      Optional<PlayerEntity> optional = list1.stream().filter(EntityPredicates.ATTACK_ALLOWED).findFirst();
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, optional);
   }
}
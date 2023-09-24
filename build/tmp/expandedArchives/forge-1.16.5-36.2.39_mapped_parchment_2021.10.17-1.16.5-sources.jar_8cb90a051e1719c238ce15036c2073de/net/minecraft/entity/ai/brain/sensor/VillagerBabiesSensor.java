package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public class VillagerBabiesSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
   }

   protected void doTick(ServerWorld pLevel, LivingEntity pEntity) {
      pEntity.getBrain().setMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES, this.getNearestVillagerBabies(pEntity));
   }

   private List<LivingEntity> getNearestVillagerBabies(LivingEntity pLivingEntity) {
      return this.getVisibleEntities(pLivingEntity).stream().filter(this::isVillagerBaby).collect(Collectors.toList());
   }

   private boolean isVillagerBaby(LivingEntity p_220993_1_) {
      return p_220993_1_.getType() == EntityType.VILLAGER && p_220993_1_.isBaby();
   }

   private List<LivingEntity> getVisibleEntities(LivingEntity pLivingEntity) {
      return pLivingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList());
   }
}
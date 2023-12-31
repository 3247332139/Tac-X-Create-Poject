package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;

public class FindHidingPlaceTask extends Task<LivingEntity> {
   private final float speedModifier;
   private final int radius;
   private final int closeEnoughDist;
   private Optional<BlockPos> currentPos = Optional.empty();

   public FindHidingPlaceTask(int p_i50361_1_, float p_i50361_2_, int p_i50361_3_) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.HOME, MemoryModuleStatus.REGISTERED, MemoryModuleType.HIDING_PLACE, MemoryModuleStatus.REGISTERED));
      this.radius = p_i50361_1_;
      this.speedModifier = p_i50361_2_;
      this.closeEnoughDist = p_i50361_3_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, LivingEntity pOwner) {
      Optional<BlockPos> optional = pLevel.getPoiManager().find((p_220454_0_) -> {
         return p_220454_0_ == PointOfInterestType.HOME;
      }, (p_220456_0_) -> {
         return true;
      }, pOwner.blockPosition(), this.closeEnoughDist + 1, PointOfInterestManager.Status.ANY);
      if (optional.isPresent() && optional.get().closerThan(pOwner.position(), (double)this.closeEnoughDist)) {
         this.currentPos = optional;
      } else {
         this.currentPos = Optional.empty();
      }

      return true;
   }

   protected void start(ServerWorld pLevel, LivingEntity pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      Optional<BlockPos> optional = this.currentPos;
      if (!optional.isPresent()) {
         optional = pLevel.getPoiManager().getRandom((p_220453_0_) -> {
            return p_220453_0_ == PointOfInterestType.HOME;
         }, (p_220455_0_) -> {
            return true;
         }, PointOfInterestManager.Status.ANY, pEntity.blockPosition(), this.radius, pEntity.getRandom());
         if (!optional.isPresent()) {
            Optional<GlobalPos> optional1 = brain.getMemory(MemoryModuleType.HOME);
            if (optional1.isPresent()) {
               optional = Optional.of(optional1.get().pos());
            }
         }
      }

      if (optional.isPresent()) {
         brain.eraseMemory(MemoryModuleType.PATH);
         brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
         brain.eraseMemory(MemoryModuleType.BREED_TARGET);
         brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
         brain.setMemory(MemoryModuleType.HIDING_PLACE, GlobalPos.of(pLevel.dimension(), optional.get()));
         if (!optional.get().closerThan(pEntity.position(), (double)this.closeEnoughDist)) {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(optional.get(), this.speedModifier, this.closeEnoughDist));
         }
      }

   }
}
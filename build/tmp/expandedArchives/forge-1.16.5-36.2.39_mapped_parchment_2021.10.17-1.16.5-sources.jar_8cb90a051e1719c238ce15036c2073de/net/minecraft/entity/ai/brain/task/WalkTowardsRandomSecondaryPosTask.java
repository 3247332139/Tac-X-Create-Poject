package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

public class WalkTowardsRandomSecondaryPosTask extends Task<VillagerEntity> {
   private final MemoryModuleType<List<GlobalPos>> strollToMemoryType;
   private final MemoryModuleType<GlobalPos> mustBeCloseToMemoryType;
   private final float speedModifier;
   private final int closeEnoughDist;
   private final int maxDistanceFromPoi;
   private long nextOkStartTime;
   @Nullable
   private GlobalPos targetPos;

   public WalkTowardsRandomSecondaryPosTask(MemoryModuleType<List<GlobalPos>> p_i50340_1_, float p_i50340_2_, int p_i50340_3_, int p_i50340_4_, MemoryModuleType<GlobalPos> p_i50340_5_) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED, p_i50340_1_, MemoryModuleStatus.VALUE_PRESENT, p_i50340_5_, MemoryModuleStatus.VALUE_PRESENT));
      this.strollToMemoryType = p_i50340_1_;
      this.speedModifier = p_i50340_2_;
      this.closeEnoughDist = p_i50340_3_;
      this.maxDistanceFromPoi = p_i50340_4_;
      this.mustBeCloseToMemoryType = p_i50340_5_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      Optional<List<GlobalPos>> optional = pOwner.getBrain().getMemory(this.strollToMemoryType);
      Optional<GlobalPos> optional1 = pOwner.getBrain().getMemory(this.mustBeCloseToMemoryType);
      if (optional.isPresent() && optional1.isPresent()) {
         List<GlobalPos> list = optional.get();
         if (!list.isEmpty()) {
            this.targetPos = list.get(pLevel.getRandom().nextInt(list.size()));
            return this.targetPos != null && pLevel.dimension() == this.targetPos.dimension() && optional1.get().pos().closerThan(pOwner.position(), (double)this.maxDistanceFromPoi);
         }
      }

      return false;
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      if (pGameTime > this.nextOkStartTime && this.targetPos != null) {
         pEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos.pos(), this.speedModifier, this.closeEnoughDist));
         this.nextOkStartTime = pGameTime + 100L;
      }

   }
}
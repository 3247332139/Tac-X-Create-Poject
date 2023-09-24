package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class RingBellTask extends Task<LivingEntity> {
   public RingBellTask() {
      super(ImmutableMap.of(MemoryModuleType.MEETING_POINT, MemoryModuleStatus.VALUE_PRESENT));
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, LivingEntity pOwner) {
      return pLevel.random.nextFloat() > 0.95F;
   }

   protected void start(ServerWorld pLevel, LivingEntity pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      BlockPos blockpos = brain.getMemory(MemoryModuleType.MEETING_POINT).get().pos();
      if (blockpos.closerThan(pEntity.blockPosition(), 3.0D)) {
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (blockstate.is(Blocks.BELL)) {
            BellBlock bellblock = (BellBlock)blockstate.getBlock();
            bellblock.attemptToRing(pLevel, blockpos, (Direction)null);
         }
      }

   }
}
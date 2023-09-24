package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.world.server.ServerWorld;

public class FindPotentialJobTask extends Task<VillagerEntity> {
   final float speedModifier;

   public FindPotentialJobTask(float p_i231519_1_) {
      super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleStatus.VALUE_PRESENT), 1200);
      this.speedModifier = p_i231519_1_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      return pOwner.getBrain().getActiveNonCoreActivity().map((p_233904_0_) -> {
         return p_233904_0_ == Activity.IDLE || p_233904_0_ == Activity.WORK || p_233904_0_ == Activity.PLAY;
      }).orElse(true);
   }

   protected boolean canStillUse(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      return pEntity.getBrain().hasMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE);
   }

   protected void tick(ServerWorld pLevel, VillagerEntity pOwner, long pGameTime) {
      BrainUtil.setWalkAndLookTargetMemories(pOwner, pOwner.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos(), this.speedModifier, 1);
   }

   protected void stop(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      Optional<GlobalPos> optional = pEntity.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
      optional.ifPresent((p_233905_1_) -> {
         BlockPos blockpos = p_233905_1_.pos();
         ServerWorld serverworld = pLevel.getServer().getLevel(p_233905_1_.dimension());
         if (serverworld != null) {
            PointOfInterestManager pointofinterestmanager = serverworld.getPoiManager();
            if (pointofinterestmanager.exists(blockpos, (p_241377_0_) -> {
               return true;
            })) {
               pointofinterestmanager.release(blockpos);
            }

            DebugPacketSender.sendPoiTicketCountPacket(pLevel, blockpos);
         }
      });
      pEntity.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
   }
}
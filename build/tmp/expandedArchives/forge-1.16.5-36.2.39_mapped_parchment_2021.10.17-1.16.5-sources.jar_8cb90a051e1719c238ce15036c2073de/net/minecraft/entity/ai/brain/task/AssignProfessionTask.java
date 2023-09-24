package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

public class AssignProfessionTask extends Task<VillagerEntity> {
   public AssignProfessionTask() {
      super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleStatus.VALUE_PRESENT));
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      BlockPos blockpos = pOwner.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos();
      return blockpos.closerThan(pOwner.position(), 2.0D) || pOwner.assignProfessionWhenSpawned();
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      GlobalPos globalpos = pEntity.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get();
      pEntity.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
      pEntity.getBrain().setMemory(MemoryModuleType.JOB_SITE, globalpos);
      pLevel.broadcastEntityEvent(pEntity, (byte)14);
      if (pEntity.getVillagerData().getProfession() == VillagerProfession.NONE) {
         MinecraftServer minecraftserver = pLevel.getServer();
         Optional.ofNullable(minecraftserver.getLevel(globalpos.dimension())).flatMap((p_241376_1_) -> {
            return p_241376_1_.getPoiManager().getType(globalpos.pos());
         }).flatMap((p_220390_0_) -> {
            return Registry.VILLAGER_PROFESSION.stream().filter((p_220389_1_) -> {
               return p_220389_1_.getJobPoiType() == p_220390_0_;
            }).findFirst();
         }).ifPresent((p_220388_2_) -> {
            pEntity.setVillagerData(pEntity.getVillagerData().setProfession(p_220388_2_));
            pEntity.refreshBrain(pLevel);
         });
      }
   }
}
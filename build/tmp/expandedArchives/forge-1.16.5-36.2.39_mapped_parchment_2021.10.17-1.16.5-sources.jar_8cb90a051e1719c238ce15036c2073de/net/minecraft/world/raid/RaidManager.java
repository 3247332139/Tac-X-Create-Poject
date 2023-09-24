package net.minecraft.world.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class RaidManager extends WorldSavedData {
   private final Map<Integer, Raid> raidMap = Maps.newHashMap();
   private final ServerWorld level;
   private int nextAvailableID;
   private int tick;

   public RaidManager(ServerWorld p_i50142_1_) {
      super(getFileId(p_i50142_1_.dimensionType()));
      this.level = p_i50142_1_;
      this.nextAvailableID = 1;
      this.setDirty();
   }

   public Raid get(int pId) {
      return this.raidMap.get(pId);
   }

   public void tick() {
      ++this.tick;
      Iterator<Raid> iterator = this.raidMap.values().iterator();

      while(iterator.hasNext()) {
         Raid raid = iterator.next();
         if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
            raid.stop();
         }

         if (raid.isStopped()) {
            iterator.remove();
            this.setDirty();
         } else {
            raid.tick();
         }
      }

      if (this.tick % 200 == 0) {
         this.setDirty();
      }

      DebugPacketSender.sendRaids(this.level, this.raidMap.values());
   }

   public static boolean canJoinRaid(AbstractRaiderEntity pRaider, Raid pRaid) {
      if (pRaider != null && pRaid != null && pRaid.getLevel() != null) {
         return pRaider.isAlive() && pRaider.canJoinRaid() && pRaider.getNoActionTime() <= 2400 && pRaider.level.dimensionType() == pRaid.getLevel().dimensionType();
      } else {
         return false;
      }
   }

   @Nullable
   public Raid createOrExtendRaid(ServerPlayerEntity pPlayer) {
      if (pPlayer.isSpectator()) {
         return null;
      } else if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
         return null;
      } else {
         DimensionType dimensiontype = pPlayer.level.dimensionType();
         if (!dimensiontype.hasRaids()) {
            return null;
         } else {
            BlockPos blockpos = pPlayer.blockPosition();
            List<PointOfInterest> list = this.level.getPoiManager().getInRange(PointOfInterestType.ALL, blockpos, 64, PointOfInterestManager.Status.IS_OCCUPIED).collect(Collectors.toList());
            int i = 0;
            Vector3d vector3d = Vector3d.ZERO;

            for(PointOfInterest pointofinterest : list) {
               BlockPos blockpos2 = pointofinterest.getPos();
               vector3d = vector3d.add((double)blockpos2.getX(), (double)blockpos2.getY(), (double)blockpos2.getZ());
               ++i;
            }

            BlockPos blockpos1;
            if (i > 0) {
               vector3d = vector3d.scale(1.0D / (double)i);
               blockpos1 = new BlockPos(vector3d);
            } else {
               blockpos1 = blockpos;
            }

            Raid raid = this.getOrCreateRaid(pPlayer.getLevel(), blockpos1);
            boolean flag = false;
            if (!raid.isStarted()) {
               if (!this.raidMap.containsKey(raid.getId())) {
                  this.raidMap.put(raid.getId(), raid);
               }

               flag = true;
            } else if (raid.getBadOmenLevel() < raid.getMaxBadOmenLevel()) {
               flag = true;
            } else {
               pPlayer.removeEffect(Effects.BAD_OMEN);
               pPlayer.connection.send(new SEntityStatusPacket(pPlayer, (byte)43));
            }

            if (flag) {
               raid.absorbBadOmen(pPlayer);
               pPlayer.connection.send(new SEntityStatusPacket(pPlayer, (byte)43));
               if (!raid.hasFirstWaveSpawned()) {
                  pPlayer.awardStat(Stats.RAID_TRIGGER);
                  CriteriaTriggers.BAD_OMEN.trigger(pPlayer);
               }
            }

            this.setDirty();
            return raid;
         }
      }
   }

   private Raid getOrCreateRaid(ServerWorld pLevel, BlockPos pPos) {
      Raid raid = pLevel.getRaidAt(pPos);
      return raid != null ? raid : new Raid(this.getUniqueId(), pLevel, pPos);
   }

   public void load(CompoundNBT p_76184_1_) {
      this.nextAvailableID = p_76184_1_.getInt("NextAvailableID");
      this.tick = p_76184_1_.getInt("Tick");
      ListNBT listnbt = p_76184_1_.getList("Raids", 10);

      for(int i = 0; i < listnbt.size(); ++i) {
         CompoundNBT compoundnbt = listnbt.getCompound(i);
         Raid raid = new Raid(this.level, compoundnbt);
         this.raidMap.put(raid.getId(), raid);
      }

   }

   /**
    * Used to save the {@code SavedData} to a {@code CompoundTag}
    * @param pCompound the {@code CompoundTag} to save the {@code SavedData} to
    */
   public CompoundNBT save(CompoundNBT pCompound) {
      pCompound.putInt("NextAvailableID", this.nextAvailableID);
      pCompound.putInt("Tick", this.tick);
      ListNBT listnbt = new ListNBT();

      for(Raid raid : this.raidMap.values()) {
         CompoundNBT compoundnbt = new CompoundNBT();
         raid.save(compoundnbt);
         listnbt.add(compoundnbt);
      }

      pCompound.put("Raids", listnbt);
      return pCompound;
   }

   public static String getFileId(DimensionType p_234620_0_) {
      return "raids" + p_234620_0_.getFileSuffix();
   }

   private int getUniqueId() {
      return ++this.nextAvailableID;
   }

   @Nullable
   public Raid getNearbyRaid(BlockPos pPos, int pDistance) {
      Raid raid = null;
      double d0 = (double)pDistance;

      for(Raid raid1 : this.raidMap.values()) {
         double d1 = raid1.getCenter().distSqr(pPos);
         if (raid1.isActive() && d1 < d0) {
            raid = raid1;
            d0 = d1;
         }
      }

      return raid;
   }
}
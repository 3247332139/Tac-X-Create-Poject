package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SDisplayObjectivePacket;
import net.minecraft.network.play.server.SScoreboardObjectivePacket;
import net.minecraft.network.play.server.STeamsPacket;
import net.minecraft.network.play.server.SUpdateScorePacket;
import net.minecraft.server.MinecraftServer;

public class ServerScoreboard extends Scoreboard {
   private final MinecraftServer server;
   private final Set<ScoreObjective> trackedObjectives = Sets.newHashSet();
   private Runnable[] dirtyListeners = new Runnable[0];

   public ServerScoreboard(MinecraftServer p_i1501_1_) {
      this.server = p_i1501_1_;
   }

   public void onScoreChanged(Score pScore) {
      super.onScoreChanged(pScore);
      if (this.trackedObjectives.contains(pScore.getObjective())) {
         this.server.getPlayerList().broadcastAll(new SUpdateScorePacket(ServerScoreboard.Action.CHANGE, pScore.getObjective().getName(), pScore.getOwner(), pScore.getScore()));
      }

      this.setDirty();
   }

   public void onPlayerRemoved(String pScoreName) {
      super.onPlayerRemoved(pScoreName);
      this.server.getPlayerList().broadcastAll(new SUpdateScorePacket(ServerScoreboard.Action.REMOVE, (String)null, pScoreName, 0));
      this.setDirty();
   }

   public void onPlayerScoreRemoved(String pScoreName, ScoreObjective pObjective) {
      super.onPlayerScoreRemoved(pScoreName, pObjective);
      if (this.trackedObjectives.contains(pObjective)) {
         this.server.getPlayerList().broadcastAll(new SUpdateScorePacket(ServerScoreboard.Action.REMOVE, pObjective.getName(), pScoreName, 0));
      }

      this.setDirty();
   }

   /**
    * 0 is tab menu, 1 is sidebar, 2 is below name
    */
   public void setDisplayObjective(int pObjectiveSlot, @Nullable ScoreObjective pObjective) {
      ScoreObjective scoreobjective = this.getDisplayObjective(pObjectiveSlot);
      super.setDisplayObjective(pObjectiveSlot, pObjective);
      if (scoreobjective != pObjective && scoreobjective != null) {
         if (this.getObjectiveDisplaySlotCount(scoreobjective) > 0) {
            this.server.getPlayerList().broadcastAll(new SDisplayObjectivePacket(pObjectiveSlot, pObjective));
         } else {
            this.stopTrackingObjective(scoreobjective);
         }
      }

      if (pObjective != null) {
         if (this.trackedObjectives.contains(pObjective)) {
            this.server.getPlayerList().broadcastAll(new SDisplayObjectivePacket(pObjectiveSlot, pObjective));
         } else {
            this.startTrackingObjective(pObjective);
         }
      }

      this.setDirty();
   }

   public boolean addPlayerToTeam(String pPlayerName, ScorePlayerTeam pTeam) {
      if (super.addPlayerToTeam(pPlayerName, pTeam)) {
         this.server.getPlayerList().broadcastAll(new STeamsPacket(pTeam, Arrays.asList(pPlayerName), 3));
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   /**
    * Removes the given username from the given ScorePlayerTeam. If the player is not on the team then an
    * IllegalStateException is thrown.
    */
   public void removePlayerFromTeam(String pUsername, ScorePlayerTeam pPlayerTeam) {
      super.removePlayerFromTeam(pUsername, pPlayerTeam);
      this.server.getPlayerList().broadcastAll(new STeamsPacket(pPlayerTeam, Arrays.asList(pUsername), 4));
      this.setDirty();
   }

   public void onObjectiveAdded(ScoreObjective pObjective) {
      super.onObjectiveAdded(pObjective);
      this.setDirty();
   }

   public void onObjectiveChanged(ScoreObjective pObjective) {
      super.onObjectiveChanged(pObjective);
      if (this.trackedObjectives.contains(pObjective)) {
         this.server.getPlayerList().broadcastAll(new SScoreboardObjectivePacket(pObjective, 2));
      }

      this.setDirty();
   }

   public void onObjectiveRemoved(ScoreObjective pObjective) {
      super.onObjectiveRemoved(pObjective);
      if (this.trackedObjectives.contains(pObjective)) {
         this.stopTrackingObjective(pObjective);
      }

      this.setDirty();
   }

   public void onTeamAdded(ScorePlayerTeam pPlayerTeam) {
      super.onTeamAdded(pPlayerTeam);
      this.server.getPlayerList().broadcastAll(new STeamsPacket(pPlayerTeam, 0));
      this.setDirty();
   }

   public void onTeamChanged(ScorePlayerTeam pPlayerTeam) {
      super.onTeamChanged(pPlayerTeam);
      this.server.getPlayerList().broadcastAll(new STeamsPacket(pPlayerTeam, 2));
      this.setDirty();
   }

   public void onTeamRemoved(ScorePlayerTeam pPlayerTeam) {
      super.onTeamRemoved(pPlayerTeam);
      this.server.getPlayerList().broadcastAll(new STeamsPacket(pPlayerTeam, 1));
      this.setDirty();
   }

   public void addDirtyListener(Runnable pRunnable) {
      this.dirtyListeners = Arrays.copyOf(this.dirtyListeners, this.dirtyListeners.length + 1);
      this.dirtyListeners[this.dirtyListeners.length - 1] = pRunnable;
   }

   protected void setDirty() {
      for(Runnable runnable : this.dirtyListeners) {
         runnable.run();
      }

   }

   public List<IPacket<?>> getStartTrackingPackets(ScoreObjective pObjective) {
      List<IPacket<?>> list = Lists.newArrayList();
      list.add(new SScoreboardObjectivePacket(pObjective, 0));

      for(int i = 0; i < 19; ++i) {
         if (this.getDisplayObjective(i) == pObjective) {
            list.add(new SDisplayObjectivePacket(i, pObjective));
         }
      }

      for(Score score : this.getPlayerScores(pObjective)) {
         list.add(new SUpdateScorePacket(ServerScoreboard.Action.CHANGE, score.getObjective().getName(), score.getOwner(), score.getScore()));
      }

      return list;
   }

   public void startTrackingObjective(ScoreObjective pObjective) {
      List<IPacket<?>> list = this.getStartTrackingPackets(pObjective);

      for(ServerPlayerEntity serverplayerentity : this.server.getPlayerList().getPlayers()) {
         for(IPacket<?> ipacket : list) {
            serverplayerentity.connection.send(ipacket);
         }
      }

      this.trackedObjectives.add(pObjective);
   }

   public List<IPacket<?>> getStopTrackingPackets(ScoreObjective p_96548_1_) {
      List<IPacket<?>> list = Lists.newArrayList();
      list.add(new SScoreboardObjectivePacket(p_96548_1_, 1));

      for(int i = 0; i < 19; ++i) {
         if (this.getDisplayObjective(i) == p_96548_1_) {
            list.add(new SDisplayObjectivePacket(i, p_96548_1_));
         }
      }

      return list;
   }

   public void stopTrackingObjective(ScoreObjective p_96546_1_) {
      List<IPacket<?>> list = this.getStopTrackingPackets(p_96546_1_);

      for(ServerPlayerEntity serverplayerentity : this.server.getPlayerList().getPlayers()) {
         for(IPacket<?> ipacket : list) {
            serverplayerentity.connection.send(ipacket);
         }
      }

      this.trackedObjectives.remove(p_96546_1_);
   }

   public int getObjectiveDisplaySlotCount(ScoreObjective p_96552_1_) {
      int i = 0;

      for(int j = 0; j < 19; ++j) {
         if (this.getDisplayObjective(j) == p_96552_1_) {
            ++i;
         }
      }

      return i;
   }

   public static enum Action {
      CHANGE,
      REMOVE;
   }
}
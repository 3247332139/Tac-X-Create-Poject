package net.minecraft.world.server;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;

public class ServerBossInfo extends BossInfo {
   private final Set<ServerPlayerEntity> players = Sets.newHashSet();
   private final Set<ServerPlayerEntity> unmodifiablePlayers = Collections.unmodifiableSet(this.players);
   private boolean visible = true;

   public ServerBossInfo(ITextComponent p_i46839_1_, BossInfo.Color p_i46839_2_, BossInfo.Overlay p_i46839_3_) {
      super(MathHelper.createInsecureUUID(), p_i46839_1_, p_i46839_2_, p_i46839_3_);
   }

   public void setPercent(float p_186735_1_) {
      if (p_186735_1_ != this.percent) {
         super.setPercent(p_186735_1_);
         this.broadcast(SUpdateBossInfoPacket.Operation.UPDATE_PCT);
      }

   }

   public void setColor(BossInfo.Color pColor) {
      if (pColor != this.color) {
         super.setColor(pColor);
         this.broadcast(SUpdateBossInfoPacket.Operation.UPDATE_STYLE);
      }

   }

   public void setOverlay(BossInfo.Overlay pOverlay) {
      if (pOverlay != this.overlay) {
         super.setOverlay(pOverlay);
         this.broadcast(SUpdateBossInfoPacket.Operation.UPDATE_STYLE);
      }

   }

   public BossInfo setDarkenScreen(boolean pDarkenSky) {
      if (pDarkenSky != this.darkenScreen) {
         super.setDarkenScreen(pDarkenSky);
         this.broadcast(SUpdateBossInfoPacket.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   public BossInfo setPlayBossMusic(boolean pPlayEndBossMusic) {
      if (pPlayEndBossMusic != this.playBossMusic) {
         super.setPlayBossMusic(pPlayEndBossMusic);
         this.broadcast(SUpdateBossInfoPacket.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   public BossInfo setCreateWorldFog(boolean pCreateFog) {
      if (pCreateFog != this.createWorldFog) {
         super.setCreateWorldFog(pCreateFog);
         this.broadcast(SUpdateBossInfoPacket.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   public void setName(ITextComponent pName) {
      if (!Objects.equal(pName, this.name)) {
         super.setName(pName);
         this.broadcast(SUpdateBossInfoPacket.Operation.UPDATE_NAME);
      }

   }

   private void broadcast(SUpdateBossInfoPacket.Operation p_186759_1_) {
      if (this.visible) {
         SUpdateBossInfoPacket supdatebossinfopacket = new SUpdateBossInfoPacket(p_186759_1_, this);

         for(ServerPlayerEntity serverplayerentity : this.players) {
            serverplayerentity.connection.send(supdatebossinfopacket);
         }
      }

   }

   /**
    * Makes the boss visible to the given player.
    */
   public void addPlayer(ServerPlayerEntity pPlayer) {
      if (this.players.add(pPlayer) && this.visible) {
         pPlayer.connection.send(new SUpdateBossInfoPacket(SUpdateBossInfoPacket.Operation.ADD, this));
      }

   }

   /**
    * Makes the boss non-visible to the given player.
    */
   public void removePlayer(ServerPlayerEntity pPlayer) {
      if (this.players.remove(pPlayer) && this.visible) {
         pPlayer.connection.send(new SUpdateBossInfoPacket(SUpdateBossInfoPacket.Operation.REMOVE, this));
      }

   }

   public void removeAllPlayers() {
      if (!this.players.isEmpty()) {
         for(ServerPlayerEntity serverplayerentity : Lists.newArrayList(this.players)) {
            this.removePlayer(serverplayerentity);
         }
      }

   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean pVisible) {
      if (pVisible != this.visible) {
         this.visible = pVisible;

         for(ServerPlayerEntity serverplayerentity : this.players) {
            serverplayerentity.connection.send(new SUpdateBossInfoPacket(pVisible ? SUpdateBossInfoPacket.Operation.ADD : SUpdateBossInfoPacket.Operation.REMOVE, this));
         }
      }

   }

   /**
    * The returned collection is unmodifiable
    */
   public Collection<ServerPlayerEntity> getPlayers() {
      return this.unmodifiablePlayers;
   }
}
package net.minecraft.server.management;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SPlayerAbilitiesPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.network.play.server.STagsListPacket;
import net.minecraft.network.play.server.STeamsPacket;
import net.minecraft.network.play.server.SUpdateRecipesPacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.network.play.server.SUpdateViewDistancePacket;
import net.minecraft.network.play.server.SWorldBorderPacket;
import net.minecraft.network.play.server.SWorldSpawnChangedPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.PlayerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PlayerList {
   public static final File USERBANLIST_FILE = new File("banned-players.json");
   public static final File IPBANLIST_FILE = new File("banned-ips.json");
   public static final File OPLIST_FILE = new File("ops.json");
   public static final File WHITELIST_FILE = new File("whitelist.json");
   private static final Logger LOGGER = LogManager.getLogger();
   private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
   private final MinecraftServer server;
   private final List<ServerPlayerEntity> players = Lists.newArrayList();
   /** A map containing the key-value pairs for UUIDs and their EntityPlayerMP objects. */
   private final Map<UUID, ServerPlayerEntity> playersByUUID = Maps.newHashMap();
   private final BanList bans = new BanList(USERBANLIST_FILE);
   private final IPBanList ipBans = new IPBanList(IPBANLIST_FILE);
   private final OpList ops = new OpList(OPLIST_FILE);
   private final WhiteList whitelist = new WhiteList(WHITELIST_FILE);
   private final Map<UUID, ServerStatisticsManager> stats = Maps.newHashMap();
   private final Map<UUID, PlayerAdvancements> advancements = Maps.newHashMap();
   private final PlayerData playerIo;
   private boolean doWhiteList;
   private final DynamicRegistries.Impl registryHolder;
   protected final int maxPlayers;
   private int viewDistance;
   private GameType overrideGameMode;
   private boolean allowCheatsForAllPlayers;
   private int sendAllPlayerInfoIn;
   private final List<ServerPlayerEntity> playersView = java.util.Collections.unmodifiableList(players);

   public PlayerList(MinecraftServer p_i231425_1_, DynamicRegistries.Impl p_i231425_2_, PlayerData p_i231425_3_, int p_i231425_4_) {
      this.server = p_i231425_1_;
      this.registryHolder = p_i231425_2_;
      this.maxPlayers = p_i231425_4_;
      this.playerIo = p_i231425_3_;
   }

   public void placeNewPlayer(NetworkManager pNetManager, ServerPlayerEntity pPlayer) {
      GameProfile gameprofile = pPlayer.getGameProfile();
      PlayerProfileCache playerprofilecache = this.server.getProfileCache();
      GameProfile gameprofile1 = playerprofilecache.get(gameprofile.getId());
      String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
      playerprofilecache.add(gameprofile);
      CompoundNBT compoundnbt = this.load(pPlayer);
      RegistryKey<World> registrykey = compoundnbt != null ? DimensionType.parseLegacy(new Dynamic<>(NBTDynamicOps.INSTANCE, compoundnbt.get("Dimension"))).resultOrPartial(LOGGER::error).orElse(World.OVERWORLD) : World.OVERWORLD;
      ServerWorld serverworld = this.server.getLevel(registrykey);
      ServerWorld serverworld1;
      if (serverworld == null) {
         LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", (Object)registrykey);
         serverworld1 = this.server.overworld();
      } else {
         serverworld1 = serverworld;
      }

      pPlayer.setLevel(serverworld1);
      pPlayer.gameMode.setLevel((ServerWorld)pPlayer.level);
      String s1 = "local";
      if (pNetManager.getRemoteAddress() != null) {
         s1 = pNetManager.getRemoteAddress().toString();
      }

      LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", pPlayer.getName().getString(), s1, pPlayer.getId(), pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
      IWorldInfo iworldinfo = serverworld1.getLevelData();
      this.updatePlayerGameMode(pPlayer, (ServerPlayerEntity)null, serverworld1);
      ServerPlayNetHandler serverplaynethandler = new ServerPlayNetHandler(this.server, pNetManager, pPlayer);
      net.minecraftforge.fml.network.NetworkHooks.sendMCRegistryPackets(pNetManager, "PLAY_TO_CLIENT");
      GameRules gamerules = serverworld1.getGameRules();
      boolean flag = gamerules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
      boolean flag1 = gamerules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
      serverplaynethandler.send(new SJoinGamePacket(pPlayer.getId(), pPlayer.gameMode.getGameModeForPlayer(), pPlayer.gameMode.getPreviousGameModeForPlayer(), BiomeManager.obfuscateSeed(serverworld1.getSeed()), iworldinfo.isHardcore(), this.server.levelKeys(), this.registryHolder, serverworld1.dimensionType(), serverworld1.dimension(), this.getMaxPlayers(), this.viewDistance, flag1, !flag, serverworld1.isDebug(), serverworld1.isFlat()));
      serverplaynethandler.send(new SCustomPayloadPlayPacket(SCustomPayloadPlayPacket.BRAND, (new PacketBuffer(Unpooled.buffer())).writeUtf(this.getServer().getServerModName())));
      serverplaynethandler.send(new SServerDifficultyPacket(iworldinfo.getDifficulty(), iworldinfo.isDifficultyLocked()));
      serverplaynethandler.send(new SPlayerAbilitiesPacket(pPlayer.abilities));
      serverplaynethandler.send(new SHeldItemChangePacket(pPlayer.inventory.selected));
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.OnDatapackSyncEvent(this, pPlayer));
      serverplaynethandler.send(new SUpdateRecipesPacket(this.server.getRecipeManager().getRecipes()));
      serverplaynethandler.send(new STagsListPacket(this.server.getTags()));
      net.minecraftforge.fml.network.NetworkHooks.syncCustomTagTypes(pPlayer, this.server.getTags());
      this.sendPlayerPermissionLevel(pPlayer);
      pPlayer.getStats().markAllDirty();
      pPlayer.getRecipeBook().sendInitialRecipeBook(pPlayer);
      this.updateEntireScoreboard(serverworld1.getScoreboard(), pPlayer);
      this.server.invalidateStatus();
      IFormattableTextComponent iformattabletextcomponent;
      if (pPlayer.getGameProfile().getName().equalsIgnoreCase(s)) {
         iformattabletextcomponent = new TranslationTextComponent("multiplayer.player.joined", pPlayer.getDisplayName());
      } else {
         iformattabletextcomponent = new TranslationTextComponent("multiplayer.player.joined.renamed", pPlayer.getDisplayName(), s);
      }

      this.broadcastMessage(iformattabletextcomponent.withStyle(TextFormatting.YELLOW), ChatType.SYSTEM, Util.NIL_UUID);
      serverplaynethandler.teleport(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), pPlayer.yRot, pPlayer.xRot);
      this.addPlayer(pPlayer);
      this.playersByUUID.put(pPlayer.getUUID(), pPlayer);
      this.broadcastAll(new SPlayerListItemPacket(SPlayerListItemPacket.Action.ADD_PLAYER, pPlayer));

      for(int i = 0; i < this.players.size(); ++i) {
         pPlayer.connection.send(new SPlayerListItemPacket(SPlayerListItemPacket.Action.ADD_PLAYER, this.players.get(i)));
      }

      serverworld1.addNewPlayer(pPlayer);
      this.server.getCustomBossEvents().onPlayerConnect(pPlayer);
      this.sendLevelInfo(pPlayer, serverworld1);
      if (!this.server.getResourcePack().isEmpty()) {
         pPlayer.sendTexturePack(this.server.getResourcePack(), this.server.getResourcePackHash());
      }

      for(EffectInstance effectinstance : pPlayer.getActiveEffects()) {
         serverplaynethandler.send(new SPlayEntityEffectPacket(pPlayer.getId(), effectinstance));
      }

      if (compoundnbt != null && compoundnbt.contains("RootVehicle", 10)) {
         CompoundNBT compoundnbt1 = compoundnbt.getCompound("RootVehicle");
         Entity entity1 = EntityType.loadEntityRecursive(compoundnbt1.getCompound("Entity"), serverworld1, (p_217885_1_) -> {
            return !serverworld1.addWithUUID(p_217885_1_) ? null : p_217885_1_;
         });
         if (entity1 != null) {
            UUID uuid;
            if (compoundnbt1.hasUUID("Attach")) {
               uuid = compoundnbt1.getUUID("Attach");
            } else {
               uuid = null;
            }

            if (entity1.getUUID().equals(uuid)) {
               pPlayer.startRiding(entity1, true);
            } else {
               for(Entity entity : entity1.getIndirectPassengers()) {
                  if (entity.getUUID().equals(uuid)) {
                     pPlayer.startRiding(entity, true);
                     break;
                  }
               }
            }

            if (!pPlayer.isPassenger()) {
               LOGGER.warn("Couldn't reattach entity to player");
               serverworld1.despawn(entity1);

               for(Entity entity2 : entity1.getIndirectPassengers()) {
                  serverworld1.despawn(entity2);
               }
            }
         }
      }

      pPlayer.initMenu();
      net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerLoggedIn( pPlayer );
   }

   protected void updateEntireScoreboard(ServerScoreboard pScoreboard, ServerPlayerEntity pPlayer) {
      Set<ScoreObjective> set = Sets.newHashSet();

      for(ScorePlayerTeam scoreplayerteam : pScoreboard.getPlayerTeams()) {
         pPlayer.connection.send(new STeamsPacket(scoreplayerteam, 0));
      }

      for(int i = 0; i < 19; ++i) {
         ScoreObjective scoreobjective = pScoreboard.getDisplayObjective(i);
         if (scoreobjective != null && !set.contains(scoreobjective)) {
            for(IPacket<?> ipacket : pScoreboard.getStartTrackingPackets(scoreobjective)) {
               pPlayer.connection.send(ipacket);
            }

            set.add(scoreobjective);
         }
      }

   }

   public void setLevel(ServerWorld p_212504_1_) {
      p_212504_1_.getWorldBorder().addListener(new IBorderListener() {
         public void onBorderSizeSet(WorldBorder pBorder, double pNewSize) {
            PlayerList.this.broadcastAll(new SWorldBorderPacket(pBorder, SWorldBorderPacket.Action.SET_SIZE));
         }

         public void onBorderSizeLerping(WorldBorder pBorder, double pOldSize, double pNewSize, long pTime) {
            PlayerList.this.broadcastAll(new SWorldBorderPacket(pBorder, SWorldBorderPacket.Action.LERP_SIZE));
         }

         public void onBorderCenterSet(WorldBorder pBorder, double pX, double pZ) {
            PlayerList.this.broadcastAll(new SWorldBorderPacket(pBorder, SWorldBorderPacket.Action.SET_CENTER));
         }

         public void onBorderSetWarningTime(WorldBorder pBorder, int pNewTime) {
            PlayerList.this.broadcastAll(new SWorldBorderPacket(pBorder, SWorldBorderPacket.Action.SET_WARNING_TIME));
         }

         public void onBorderSetWarningBlocks(WorldBorder pBorder, int pNewDistance) {
            PlayerList.this.broadcastAll(new SWorldBorderPacket(pBorder, SWorldBorderPacket.Action.SET_WARNING_BLOCKS));
         }

         public void onBorderSetDamagePerBlock(WorldBorder pBorder, double pNewAmount) {
         }

         public void onBorderSetDamageSafeZOne(WorldBorder pBorder, double pNewSize) {
         }
      });
   }

   /**
    * called during player login. reads the player information from disk.
    */
   @Nullable
   public CompoundNBT load(ServerPlayerEntity pPlayer) {
      CompoundNBT compoundnbt = this.server.getWorldData().getLoadedPlayerTag();
      CompoundNBT compoundnbt1;
      if (pPlayer.getName().getString().equals(this.server.getSingleplayerName()) && compoundnbt != null) {
         compoundnbt1 = compoundnbt;
         pPlayer.load(compoundnbt);
         LOGGER.debug("loading single player");
         net.minecraftforge.event.ForgeEventFactory.firePlayerLoadingEvent(pPlayer, this.playerIo, pPlayer.getUUID().toString());
      } else {
         compoundnbt1 = this.playerIo.load(pPlayer);
      }

      return compoundnbt1;
   }

   /**
    * also stores the NBTTags if this is an intergratedPlayerList
    */
   protected void save(ServerPlayerEntity pPlayer) {
      if (pPlayer.connection == null) return;
      this.playerIo.save(pPlayer);
      ServerStatisticsManager serverstatisticsmanager = this.stats.get(pPlayer.getUUID());
      if (serverstatisticsmanager != null) {
         serverstatisticsmanager.save();
      }

      PlayerAdvancements playeradvancements = this.advancements.get(pPlayer.getUUID());
      if (playeradvancements != null) {
         playeradvancements.save();
      }

   }

   /**
    * Called when a player disconnects from the game. Writes player data to disk and removes them from the world.
    */
   public void remove(ServerPlayerEntity pPlayer) {
      net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerLoggedOut(pPlayer);
      ServerWorld serverworld = pPlayer.getLevel();
      pPlayer.awardStat(Stats.LEAVE_GAME);
      this.save(pPlayer);
      if (pPlayer.isPassenger()) {
         Entity entity = pPlayer.getRootVehicle();
         if (entity.hasOnePlayerPassenger()) {
            LOGGER.debug("Removing player mount");
            pPlayer.stopRiding();
            serverworld.despawn(entity);
            entity.removed = true;

            for(Entity entity1 : entity.getIndirectPassengers()) {
               serverworld.despawn(entity1);
               entity1.removed = true;
            }

            serverworld.getChunk(pPlayer.xChunk, pPlayer.zChunk).markUnsaved();
         }
      }

      pPlayer.unRide();
      serverworld.removePlayerImmediately(pPlayer);
      pPlayer.getAdvancements().stopListening();
      this.removePlayer(pPlayer);
      this.server.getCustomBossEvents().onPlayerDisconnect(pPlayer);
      UUID uuid = pPlayer.getUUID();
      ServerPlayerEntity serverplayerentity = this.playersByUUID.get(uuid);
      if (serverplayerentity == pPlayer) {
         this.playersByUUID.remove(uuid);
         this.stats.remove(uuid);
         this.advancements.remove(uuid);
      }

      this.broadcastAll(new SPlayerListItemPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER, pPlayer));
   }

   @Nullable
   public ITextComponent canPlayerLogin(SocketAddress p_206258_1_, GameProfile p_206258_2_) {
      if (this.bans.isBanned(p_206258_2_)) {
         ProfileBanEntry profilebanentry = this.bans.get(p_206258_2_);
         IFormattableTextComponent iformattabletextcomponent1 = new TranslationTextComponent("multiplayer.disconnect.banned.reason", profilebanentry.getReason());
         if (profilebanentry.getExpires() != null) {
            iformattabletextcomponent1.append(new TranslationTextComponent("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(profilebanentry.getExpires())));
         }

         return iformattabletextcomponent1;
      } else if (!this.isWhiteListed(p_206258_2_)) {
         return new TranslationTextComponent("multiplayer.disconnect.not_whitelisted");
      } else if (this.ipBans.isBanned(p_206258_1_)) {
         IPBanEntry ipbanentry = this.ipBans.get(p_206258_1_);
         IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent("multiplayer.disconnect.banned_ip.reason", ipbanentry.getReason());
         if (ipbanentry.getExpires() != null) {
            iformattabletextcomponent.append(new TranslationTextComponent("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipbanentry.getExpires())));
         }

         return iformattabletextcomponent;
      } else {
         return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(p_206258_2_) ? new TranslationTextComponent("multiplayer.disconnect.server_full") : null;
      }
   }

   /**
    * also checks for multiple logins across servers
    */
   public ServerPlayerEntity getPlayerForLogin(GameProfile pProfile) {
      UUID uuid = PlayerEntity.createPlayerUUID(pProfile);
      List<ServerPlayerEntity> list = Lists.newArrayList();

      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayerEntity serverplayerentity = this.players.get(i);
         if (serverplayerentity.getUUID().equals(uuid)) {
            list.add(serverplayerentity);
         }
      }

      ServerPlayerEntity serverplayerentity2 = this.playersByUUID.get(pProfile.getId());
      if (serverplayerentity2 != null && !list.contains(serverplayerentity2)) {
         list.add(serverplayerentity2);
      }

      for(ServerPlayerEntity serverplayerentity1 : list) {
         serverplayerentity1.connection.disconnect(new TranslationTextComponent("multiplayer.disconnect.duplicate_login"));
      }

      ServerWorld serverworld = this.server.overworld();
      PlayerInteractionManager playerinteractionmanager;
      if (this.server.isDemo()) {
         playerinteractionmanager = new DemoPlayerInteractionManager(serverworld);
      } else {
         playerinteractionmanager = new PlayerInteractionManager(serverworld);
      }

      return new ServerPlayerEntity(this.server, serverworld, pProfile, playerinteractionmanager);
   }

   public ServerPlayerEntity respawn(ServerPlayerEntity p_232644_1_, boolean p_232644_2_) {
      this.removePlayer(p_232644_1_);
      p_232644_1_.getLevel().removePlayer(p_232644_1_, true); // Forge: keep data until copyFrom called
      BlockPos blockpos = p_232644_1_.getRespawnPosition();
      float f = p_232644_1_.getRespawnAngle();
      boolean flag = p_232644_1_.isRespawnForced();
      ServerWorld serverworld = this.server.getLevel(p_232644_1_.getRespawnDimension());
      Optional<Vector3d> optional;
      if (serverworld != null && blockpos != null) {
         optional = PlayerEntity.findRespawnPositionAndUseSpawnBlock(serverworld, blockpos, f, flag, p_232644_2_);
      } else {
         optional = Optional.empty();
      }

      ServerWorld serverworld1 = serverworld != null && optional.isPresent() ? serverworld : this.server.overworld();
      PlayerInteractionManager playerinteractionmanager;
      if (this.server.isDemo()) {
         playerinteractionmanager = new DemoPlayerInteractionManager(serverworld1);
      } else {
         playerinteractionmanager = new PlayerInteractionManager(serverworld1);
      }

      ServerPlayerEntity serverplayerentity = new ServerPlayerEntity(this.server, serverworld1, p_232644_1_.getGameProfile(), playerinteractionmanager);
      serverplayerentity.connection = p_232644_1_.connection;
      serverplayerentity.restoreFrom(p_232644_1_, p_232644_2_);
      p_232644_1_.remove(false); // Forge: clone event had a chance to see old data, now discard it
      serverplayerentity.setId(p_232644_1_.getId());
      serverplayerentity.setMainArm(p_232644_1_.getMainArm());

      for(String s : p_232644_1_.getTags()) {
         serverplayerentity.addTag(s);
      }

      this.updatePlayerGameMode(serverplayerentity, p_232644_1_, serverworld1);
      boolean flag2 = false;
      if (optional.isPresent()) {
         BlockState blockstate = serverworld1.getBlockState(blockpos);
         boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
         Vector3d vector3d = optional.get();
         float f1;
         if (!blockstate.is(BlockTags.BEDS) && !flag1) {
            f1 = f;
         } else {
            Vector3d vector3d1 = Vector3d.atBottomCenterOf(blockpos).subtract(vector3d).normalize();
            f1 = (float)MathHelper.wrapDegrees(MathHelper.atan2(vector3d1.z, vector3d1.x) * (double)(180F / (float)Math.PI) - 90.0D);
         }

         serverplayerentity.moveTo(vector3d.x, vector3d.y, vector3d.z, f1, 0.0F);
         serverplayerentity.setRespawnPosition(serverworld1.dimension(), blockpos, f, flag, false);
         flag2 = !p_232644_2_ && flag1;
      } else if (blockpos != null) {
         serverplayerentity.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
      }

      while(!serverworld1.noCollision(serverplayerentity) && serverplayerentity.getY() < 256.0D) {
         serverplayerentity.setPos(serverplayerentity.getX(), serverplayerentity.getY() + 1.0D, serverplayerentity.getZ());
      }

      IWorldInfo iworldinfo = serverplayerentity.level.getLevelData();
      serverplayerentity.connection.send(new SRespawnPacket(serverplayerentity.level.dimensionType(), serverplayerentity.level.dimension(), BiomeManager.obfuscateSeed(serverplayerentity.getLevel().getSeed()), serverplayerentity.gameMode.getGameModeForPlayer(), serverplayerentity.gameMode.getPreviousGameModeForPlayer(), serverplayerentity.getLevel().isDebug(), serverplayerentity.getLevel().isFlat(), p_232644_2_));
      serverplayerentity.connection.teleport(serverplayerentity.getX(), serverplayerentity.getY(), serverplayerentity.getZ(), serverplayerentity.yRot, serverplayerentity.xRot);
      serverplayerentity.connection.send(new SWorldSpawnChangedPacket(serverworld1.getSharedSpawnPos(), serverworld1.getSharedSpawnAngle()));
      serverplayerentity.connection.send(new SServerDifficultyPacket(iworldinfo.getDifficulty(), iworldinfo.isDifficultyLocked()));
      serverplayerentity.connection.send(new SSetExperiencePacket(serverplayerentity.experienceProgress, serverplayerentity.totalExperience, serverplayerentity.experienceLevel));
      this.sendLevelInfo(serverplayerentity, serverworld1);
      this.sendPlayerPermissionLevel(serverplayerentity);
      serverworld1.addRespawnedPlayer(serverplayerentity);
      this.addPlayer(serverplayerentity);
      this.playersByUUID.put(serverplayerentity.getUUID(), serverplayerentity);
      serverplayerentity.initMenu();
      serverplayerentity.setHealth(serverplayerentity.getHealth());
      net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerRespawnEvent(serverplayerentity, p_232644_2_);
      if (flag2) {
         serverplayerentity.connection.send(new SPlaySoundEffectPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 1.0F, 1.0F));
      }

      return serverplayerentity;
   }

   public void sendPlayerPermissionLevel(ServerPlayerEntity pPlayer) {
      GameProfile gameprofile = pPlayer.getGameProfile();
      int i = this.server.getProfilePermissions(gameprofile);
      this.sendPlayerPermissionLevel(pPlayer, i);
   }

   /**
    * self explanitory
    */
   public void tick() {
      if (++this.sendAllPlayerInfoIn > 600) {
         this.broadcastAll(new SPlayerListItemPacket(SPlayerListItemPacket.Action.UPDATE_LATENCY, this.players));
         this.sendAllPlayerInfoIn = 0;
      }

   }

   public void broadcastAll(IPacket<?> pPacket) {
      for(int i = 0; i < this.players.size(); ++i) {
         (this.players.get(i)).connection.send(pPacket);
      }

   }

   public void broadcastAll(IPacket<?> p_232642_1_, RegistryKey<World> p_232642_2_) {
      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayerEntity serverplayerentity = this.players.get(i);
         if (serverplayerentity.level.dimension() == p_232642_2_) {
            serverplayerentity.connection.send(p_232642_1_);
         }
      }

   }

   public void broadcastToTeam(PlayerEntity pPlayer, ITextComponent pMessage) {
      Team team = pPlayer.getTeam();
      if (team != null) {
         for(String s : team.getPlayers()) {
            ServerPlayerEntity serverplayerentity = this.getPlayerByName(s);
            if (serverplayerentity != null && serverplayerentity != pPlayer) {
               serverplayerentity.sendMessage(pMessage, pPlayer.getUUID());
            }
         }

      }
   }

   public void broadcastToAllExceptTeam(PlayerEntity pPlayer, ITextComponent pMessage) {
      Team team = pPlayer.getTeam();
      if (team == null) {
         this.broadcastMessage(pMessage, ChatType.SYSTEM, pPlayer.getUUID());
      } else {
         for(int i = 0; i < this.players.size(); ++i) {
            ServerPlayerEntity serverplayerentity = this.players.get(i);
            if (serverplayerentity.getTeam() != team) {
               serverplayerentity.sendMessage(pMessage, pPlayer.getUUID());
            }
         }

      }
   }

   /**
    * Returns an array of the usernames of all the connected players.
    */
   public String[] getPlayerNamesArray() {
      String[] astring = new String[this.players.size()];

      for(int i = 0; i < this.players.size(); ++i) {
         astring[i] = this.players.get(i).getGameProfile().getName();
      }

      return astring;
   }

   public BanList getBans() {
      return this.bans;
   }

   public IPBanList getIpBans() {
      return this.ipBans;
   }

   public void op(GameProfile pProfile) {
      this.ops.add(new OpEntry(pProfile, this.server.getOperatorUserPermissionLevel(), this.ops.canBypassPlayerLimit(pProfile)));
      ServerPlayerEntity serverplayerentity = this.getPlayer(pProfile.getId());
      if (serverplayerentity != null) {
         this.sendPlayerPermissionLevel(serverplayerentity);
      }

   }

   public void deop(GameProfile pProfile) {
      this.ops.remove(pProfile);
      ServerPlayerEntity serverplayerentity = this.getPlayer(pProfile.getId());
      if (serverplayerentity != null) {
         this.sendPlayerPermissionLevel(serverplayerentity);
      }

   }

   private void sendPlayerPermissionLevel(ServerPlayerEntity pPlayer, int pPermLevel) {
      if (pPlayer.connection != null) {
         byte b0;
         if (pPermLevel <= 0) {
            b0 = 24;
         } else if (pPermLevel >= 4) {
            b0 = 28;
         } else {
            b0 = (byte)(24 + pPermLevel);
         }

         pPlayer.connection.send(new SEntityStatusPacket(pPlayer, b0));
      }

      this.server.getCommands().sendCommands(pPlayer);
   }

   public boolean isWhiteListed(GameProfile pProfile) {
      return !this.doWhiteList || this.ops.contains(pProfile) || this.whitelist.contains(pProfile);
   }

   public boolean isOp(GameProfile pProfile) {
      return this.ops.contains(pProfile) || this.server.isSingleplayerOwner(pProfile) && this.server.getWorldData().getAllowCommands() || this.allowCheatsForAllPlayers;
   }

   @Nullable
   public ServerPlayerEntity getPlayerByName(String pUsername) {
      for(ServerPlayerEntity serverplayerentity : this.players) {
         if (serverplayerentity.getGameProfile().getName().equalsIgnoreCase(pUsername)) {
            return serverplayerentity;
         }
      }

      return null;
   }

   /**
    * params: srcPlayer,x,y,z,r,dimension. The packet is not sent to the srcPlayer, but all other players within the
    * search radius
    */
   public void broadcast(@Nullable PlayerEntity pExcept, double pX, double pY, double pZ, double pRadius, RegistryKey<World> pDimension, IPacket<?> pPacket) {
      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayerEntity serverplayerentity = this.players.get(i);
         if (serverplayerentity != pExcept && serverplayerentity.level.dimension() == pDimension) {
            double d0 = pX - serverplayerentity.getX();
            double d1 = pY - serverplayerentity.getY();
            double d2 = pZ - serverplayerentity.getZ();
            if (d0 * d0 + d1 * d1 + d2 * d2 < pRadius * pRadius) {
               serverplayerentity.connection.send(pPacket);
            }
         }
      }

   }

   /**
    * Saves all of the players' current states.
    */
   public void saveAll() {
      for(int i = 0; i < this.players.size(); ++i) {
         this.save(this.players.get(i));
      }

   }

   public WhiteList getWhiteList() {
      return this.whitelist;
   }

   public String[] getWhiteListNames() {
      return this.whitelist.getUserList();
   }

   public OpList getOps() {
      return this.ops;
   }

   public String[] getOpNames() {
      return this.ops.getUserList();
   }

   public void reloadWhiteList() {
   }

   /**
    * Updates the time and weather for the given player to those of the given world
    */
   public void sendLevelInfo(ServerPlayerEntity pPlayer, ServerWorld pLevel) {
      WorldBorder worldborder = this.server.overworld().getWorldBorder();
      pPlayer.connection.send(new SWorldBorderPacket(worldborder, SWorldBorderPacket.Action.INITIALIZE));
      pPlayer.connection.send(new SUpdateTimePacket(pLevel.getGameTime(), pLevel.getDayTime(), pLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
      pPlayer.connection.send(new SWorldSpawnChangedPacket(pLevel.getSharedSpawnPos(), pLevel.getSharedSpawnAngle()));
      if (pLevel.isRaining()) {
         pPlayer.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.START_RAINING, 0.0F));
         pPlayer.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.RAIN_LEVEL_CHANGE, pLevel.getRainLevel(1.0F)));
         pPlayer.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.THUNDER_LEVEL_CHANGE, pLevel.getThunderLevel(1.0F)));
      }

   }

   /**
    * sends the players inventory to himself
    */
   public void sendAllPlayerInfo(ServerPlayerEntity pPlayer) {
      pPlayer.refreshContainer(pPlayer.inventoryMenu);
      pPlayer.resetSentInfo();
      pPlayer.connection.send(new SHeldItemChangePacket(pPlayer.inventory.selected));
   }

   /**
    * Returns the number of players currently on the server.
    */
   public int getPlayerCount() {
      return this.players.size();
   }

   /**
    * Returns the maximum number of players allowed on the server.
    */
   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   public boolean isUsingWhitelist() {
      return this.doWhiteList;
   }

   public void setUsingWhiteList(boolean pWhitelistEnabled) {
      this.doWhiteList = pWhitelistEnabled;
   }

   public List<ServerPlayerEntity> getPlayersWithAddress(String pAddress) {
      List<ServerPlayerEntity> list = Lists.newArrayList();

      for(ServerPlayerEntity serverplayerentity : this.players) {
         if (serverplayerentity.getIpAddress().equals(pAddress)) {
            list.add(serverplayerentity);
         }
      }

      return list;
   }

   /**
    * Gets the view distance, in chunks.
    */
   public int getViewDistance() {
      return this.viewDistance;
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   /**
    * On integrated servers, returns the host's player data to be written to level.dat.
    */
   public CompoundNBT getSingleplayerData() {
      return null;
   }

   @OnlyIn(Dist.CLIENT)
   public void setOverrideGameMode(GameType p_152604_1_) {
      this.overrideGameMode = p_152604_1_;
   }

   private void updatePlayerGameMode(ServerPlayerEntity p_72381_1_, @Nullable ServerPlayerEntity p_72381_2_, ServerWorld p_72381_3_) {
      if (p_72381_2_ != null) {
         p_72381_1_.gameMode.setGameModeForPlayer(p_72381_2_.gameMode.getGameModeForPlayer(), p_72381_2_.gameMode.getPreviousGameModeForPlayer());
      } else if (this.overrideGameMode != null) {
         p_72381_1_.gameMode.setGameModeForPlayer(this.overrideGameMode, GameType.NOT_SET);
      }

      p_72381_1_.gameMode.updateGameMode(p_72381_3_.getServer().getWorldData().getGameType());
   }

   /**
    * Sets whether all players are allowed to use commands (cheats) on the server.
    */
   @OnlyIn(Dist.CLIENT)
   public void setAllowCheatsForAllPlayers(boolean p_72387_1_) {
      this.allowCheatsForAllPlayers = p_72387_1_;
   }

   /**
    * Kicks everyone with "Server closed" as reason.
    */
   public void removeAll() {
      for(int i = 0; i < this.players.size(); ++i) {
         (this.players.get(i)).connection.disconnect(new TranslationTextComponent("multiplayer.disconnect.server_shutdown"));
      }

   }

   public void broadcastMessage(ITextComponent p_232641_1_, ChatType p_232641_2_, UUID p_232641_3_) {
      this.server.sendMessage(p_232641_1_, p_232641_3_);
      this.broadcastAll(new SChatPacket(p_232641_1_, p_232641_2_, p_232641_3_));
   }

   public ServerStatisticsManager getPlayerStats(PlayerEntity pPlayer) {
      UUID uuid = pPlayer.getUUID();
      ServerStatisticsManager serverstatisticsmanager = uuid == null ? null : this.stats.get(uuid);
      if (serverstatisticsmanager == null) {
         File file1 = this.server.getWorldPath(FolderName.PLAYER_STATS_DIR).toFile();
         File file2 = new File(file1, uuid + ".json");

         serverstatisticsmanager = new ServerStatisticsManager(this.server, file2);
         this.stats.put(uuid, serverstatisticsmanager);
      }

      return serverstatisticsmanager;
   }

   public PlayerAdvancements getPlayerAdvancements(ServerPlayerEntity p_192054_1_) {
      UUID uuid = p_192054_1_.getUUID();
      PlayerAdvancements playeradvancements = this.advancements.get(uuid);
      if (playeradvancements == null) {
         File file1 = this.server.getWorldPath(FolderName.PLAYER_ADVANCEMENTS_DIR).toFile();
         File file2 = new File(file1, uuid + ".json");
         playeradvancements = new PlayerAdvancements(this.server.getFixerUpper(), this, this.server.getAdvancements(), file2, p_192054_1_);
         this.advancements.put(uuid, playeradvancements);
      }

      // Forge: don't overwrite active player with a fake one.
      if (!(p_192054_1_ instanceof net.minecraftforge.common.util.FakePlayer))
      playeradvancements.setPlayer(p_192054_1_);
      return playeradvancements;
   }

   public void setViewDistance(int pViewDistance) {
      this.viewDistance = pViewDistance;
      this.broadcastAll(new SUpdateViewDistancePacket(pViewDistance));

      for(ServerWorld serverworld : this.server.getAllLevels()) {
         if (serverworld != null) {
            serverworld.getChunkSource().setViewDistance(pViewDistance);
         }
      }

   }

   public List<ServerPlayerEntity> getPlayers() {
      return this.playersView; //Unmodifiable view, we don't want people removing things without us knowing.
   }

   /**
    * Get's the EntityPlayerMP object representing the player with the UUID.
    */
   @Nullable
   public ServerPlayerEntity getPlayer(UUID pPlayerUUID) {
      return this.playersByUUID.get(pPlayerUUID);
   }

   public boolean canBypassPlayerLimit(GameProfile pProfile) {
      return false;
   }

   public void reloadResources() {
      for(PlayerAdvancements playeradvancements : this.advancements.values()) {
         playeradvancements.reload(this.server.getAdvancements());
      }

      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.OnDatapackSyncEvent(this, null));
      this.broadcastAll(new STagsListPacket(this.server.getTags()));
      net.minecraftforge.fml.network.NetworkHooks.syncCustomTagTypes(this.server.getTags());
      SUpdateRecipesPacket supdaterecipespacket = new SUpdateRecipesPacket(this.server.getRecipeManager().getRecipes());

      for(ServerPlayerEntity serverplayerentity : this.players) {
         serverplayerentity.connection.send(supdaterecipespacket);
         serverplayerentity.getRecipeBook().sendInitialRecipeBook(serverplayerentity);
      }

   }

   public boolean isAllowCheatsForAllPlayers() {
      return this.allowCheatsForAllPlayers;
   }

   public boolean addPlayer(ServerPlayerEntity player) {
      return players.add(player);
   }

   public boolean removePlayer(ServerPlayerEntity player) {
       return this.players.remove(player);
   }
}

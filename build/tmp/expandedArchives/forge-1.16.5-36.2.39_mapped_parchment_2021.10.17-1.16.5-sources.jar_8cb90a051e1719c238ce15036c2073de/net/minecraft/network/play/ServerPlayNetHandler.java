package net.minecraft.network.play;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.container.BeaconContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WritableBookItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.client.CEditBookPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CJigsawBlockGeneratePacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CLockDifficultyPacket;
import net.minecraft.network.play.client.CMarkRecipeSeenPacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPickItemPacket;
import net.minecraft.network.play.client.CPlaceRecipePacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CQueryEntityNBTPacket;
import net.minecraft.network.play.client.CQueryTileEntityNBTPacket;
import net.minecraft.network.play.client.CRenameItemPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.client.CSeenAdvancementsPacket;
import net.minecraft.network.play.client.CSelectTradePacket;
import net.minecraft.network.play.client.CSetDifficultyPacket;
import net.minecraft.network.play.client.CSpectatePacket;
import net.minecraft.network.play.client.CSteerBoatPacket;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.client.CUpdateBeaconPacket;
import net.minecraft.network.play.client.CUpdateCommandBlockPacket;
import net.minecraft.network.play.client.CUpdateJigsawBlockPacket;
import net.minecraft.network.play.client.CUpdateMinecartCommandBlockPacket;
import net.minecraft.network.play.client.CUpdateRecipeBookStatusPacket;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.network.play.client.CUpdateStructureBlockPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SDisconnectPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.network.play.server.SKeepAlivePacket;
import net.minecraft.network.play.server.SMoveVehiclePacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SQueryNBTResponsePacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.network.play.server.STabCompletePacket;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.JigsawTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Util;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.filter.IChatFilter;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerPlayNetHandler implements IServerPlayNetHandler {
   private static final Logger LOGGER = LogManager.getLogger();
   public final NetworkManager connection;
   private final MinecraftServer server;
   public ServerPlayerEntity player;
   private int tickCount;
   private long keepAliveTime;
   private boolean keepAlivePending;
   private long keepAliveChallenge;
   /** Incremented by 20 each time a user sends a chat message, decreased by one every tick. Non-ops kicked when over 200 */
   private int chatSpamTickCount;
   private int dropSpamTickCount;
   private final Int2ShortMap expectedAcks = new Int2ShortOpenHashMap();
   private double firstGoodX;
   private double firstGoodY;
   private double firstGoodZ;
   private double lastGoodX;
   private double lastGoodY;
   private double lastGoodZ;
   private Entity lastVehicle;
   private double vehicleFirstGoodX;
   private double vehicleFirstGoodY;
   private double vehicleFirstGoodZ;
   private double vehicleLastGoodX;
   private double vehicleLastGoodY;
   private double vehicleLastGoodZ;
   private Vector3d awaitingPositionFromClient;
   private int awaitingTeleport;
   private int awaitingTeleportTime;
   private boolean clientIsFloating;
   /**
    * Used to keep track of how the player is floating while gamerules should prevent that. Surpassing 80 ticks means
    * kick
    */
   private int aboveGroundTickCount;
   private boolean clientVehicleIsFloating;
   private int aboveGroundVehicleTickCount;
   private int receivedMovePacketCount;
   private int knownMovePacketCount;

   public ServerPlayNetHandler(MinecraftServer p_i1530_1_, NetworkManager p_i1530_2_, ServerPlayerEntity p_i1530_3_) {
      this.server = p_i1530_1_;
      this.connection = p_i1530_2_;
      p_i1530_2_.setListener(this);
      this.player = p_i1530_3_;
      p_i1530_3_.connection = this;
      IChatFilter ichatfilter = p_i1530_3_.getTextFilter();
      if (ichatfilter != null) {
         ichatfilter.join();
      }

   }

   public void tick() {
      this.resetPosition();
      this.player.xo = this.player.getX();
      this.player.yo = this.player.getY();
      this.player.zo = this.player.getZ();
      this.player.doTick();
      this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.yRot, this.player.xRot);
      ++this.tickCount;
      this.knownMovePacketCount = this.receivedMovePacketCount;
      if (this.clientIsFloating && !this.player.isSleeping()) {
         if (++this.aboveGroundTickCount > 80) {
            LOGGER.warn("{} was kicked for floating too long!", (Object)this.player.getName().getString());
            this.disconnect(new TranslationTextComponent("multiplayer.disconnect.flying"));
            return;
         }
      } else {
         this.clientIsFloating = false;
         this.aboveGroundTickCount = 0;
      }

      this.lastVehicle = this.player.getRootVehicle();
      if (this.lastVehicle != this.player && this.lastVehicle.getControllingPassenger() == this.player) {
         this.vehicleFirstGoodX = this.lastVehicle.getX();
         this.vehicleFirstGoodY = this.lastVehicle.getY();
         this.vehicleFirstGoodZ = this.lastVehicle.getZ();
         this.vehicleLastGoodX = this.lastVehicle.getX();
         this.vehicleLastGoodY = this.lastVehicle.getY();
         this.vehicleLastGoodZ = this.lastVehicle.getZ();
         if (this.clientVehicleIsFloating && this.player.getRootVehicle().getControllingPassenger() == this.player) {
            if (++this.aboveGroundVehicleTickCount > 80) {
               LOGGER.warn("{} was kicked for floating a vehicle too long!", (Object)this.player.getName().getString());
               this.disconnect(new TranslationTextComponent("multiplayer.disconnect.flying"));
               return;
            }
         } else {
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
         }
      } else {
         this.lastVehicle = null;
         this.clientVehicleIsFloating = false;
         this.aboveGroundVehicleTickCount = 0;
      }

      this.server.getProfiler().push("keepAlive");
      long i = Util.getMillis();
      if (i - this.keepAliveTime >= 15000L) {
         if (this.keepAlivePending) {
            this.disconnect(new TranslationTextComponent("disconnect.timeout"));
         } else {
            this.keepAlivePending = true;
            this.keepAliveTime = i;
            this.keepAliveChallenge = i;
            this.send(new SKeepAlivePacket(this.keepAliveChallenge));
         }
      }

      this.server.getProfiler().pop();
      if (this.chatSpamTickCount > 0) {
         --this.chatSpamTickCount;
      }

      if (this.dropSpamTickCount > 0) {
         --this.dropSpamTickCount;
      }

      if (this.player.getLastActionTime() > 0L && this.server.getPlayerIdleTimeout() > 0 && Util.getMillis() - this.player.getLastActionTime() > (long)(this.server.getPlayerIdleTimeout() * 1000 * 60)) {
         this.disconnect(new TranslationTextComponent("multiplayer.disconnect.idling"));
      }

   }

   public void resetPosition() {
      this.firstGoodX = this.player.getX();
      this.firstGoodY = this.player.getY();
      this.firstGoodZ = this.player.getZ();
      this.lastGoodX = this.player.getX();
      this.lastGoodY = this.player.getY();
      this.lastGoodZ = this.player.getZ();
   }

   /**
    * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
    */
   public NetworkManager getConnection() {
      return this.connection;
   }

   private boolean isSingleplayerOwner() {
      return this.server.isSingleplayerOwner(this.player.getGameProfile());
   }

   /**
    * Disconnect the player with a specified reason
    */
   public void disconnect(ITextComponent pTextComponent) {
      this.connection.send(new SDisconnectPacket(pTextComponent), (p_210161_2_) -> {
         this.connection.disconnect(pTextComponent);
      });
      this.connection.setReadOnly();
      this.server.executeBlocking(this.connection::handleDisconnection);
   }

   private <T> void filterTextPacket(T p_244533_1_, Consumer<T> p_244533_2_, BiFunction<IChatFilter, T, CompletableFuture<Optional<T>>> p_244533_3_) {
      ThreadTaskExecutor<?> threadtaskexecutor = this.player.getLevel().getServer();
      Consumer<T> consumer = (p_244545_2_) -> {
         if (this.getConnection().isConnected()) {
            p_244533_2_.accept(p_244545_2_);
         } else {
            LOGGER.debug("Ignoring packet due to disconnection");
         }

      };
      IChatFilter ichatfilter = this.player.getTextFilter();
      if (ichatfilter != null) {
         p_244533_3_.apply(ichatfilter, p_244533_1_).thenAcceptAsync((p_244539_1_) -> {
            p_244539_1_.ifPresent(consumer);
         }, threadtaskexecutor);
      } else {
         threadtaskexecutor.execute(() -> {
            consumer.accept(p_244533_1_);
         });
      }

   }

   private void filterTextPacket(String p_244535_1_, Consumer<String> p_244535_2_) {
      this.filterTextPacket(p_244535_1_, p_244535_2_, IChatFilter::processStreamMessage);
   }

   private void filterTextPacket(List<String> p_244537_1_, Consumer<List<String>> p_244537_2_) {
      this.filterTextPacket(p_244537_1_, p_244537_2_, IChatFilter::processMessageBundle);
   }

   /**
    * Processes player movement input. Includes walking, strafing, jumping, sneaking" excludes riding and toggling
    * flying/sprinting
    */
   public void handlePlayerInput(CInputPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.setPlayerInput(pPacket.getXxa(), pPacket.getZza(), pPacket.isJumping(), pPacket.isShiftKeyDown());
   }

   private static boolean containsInvalidValues(CPlayerPacket p_183006_0_) {
      if (Doubles.isFinite(p_183006_0_.getX(0.0D)) && Doubles.isFinite(p_183006_0_.getY(0.0D)) && Doubles.isFinite(p_183006_0_.getZ(0.0D)) && Floats.isFinite(p_183006_0_.getXRot(0.0F)) && Floats.isFinite(p_183006_0_.getYRot(0.0F))) {
         return Math.abs(p_183006_0_.getX(0.0D)) > 3.0E7D || Math.abs(p_183006_0_.getY(0.0D)) > 3.0E7D || Math.abs(p_183006_0_.getZ(0.0D)) > 3.0E7D;
      } else {
         return true;
      }
   }

   private static boolean containsInvalidValues(CMoveVehiclePacket p_184341_0_) {
      return !Doubles.isFinite(p_184341_0_.getX()) || !Doubles.isFinite(p_184341_0_.getY()) || !Doubles.isFinite(p_184341_0_.getZ()) || !Floats.isFinite(p_184341_0_.getXRot()) || !Floats.isFinite(p_184341_0_.getYRot());
   }

   public void handleMoveVehicle(CMoveVehiclePacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (containsInvalidValues(pPacket)) {
         this.disconnect(new TranslationTextComponent("multiplayer.disconnect.invalid_vehicle_movement"));
      } else {
         Entity entity = this.player.getRootVehicle();
         if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
            ServerWorld serverworld = this.player.getLevel();
            double d0 = entity.getX();
            double d1 = entity.getY();
            double d2 = entity.getZ();
            double d3 = pPacket.getX();
            double d4 = pPacket.getY();
            double d5 = pPacket.getZ();
            float f = pPacket.getYRot();
            float f1 = pPacket.getXRot();
            double d6 = d3 - this.vehicleFirstGoodX;
            double d7 = d4 - this.vehicleFirstGoodY;
            double d8 = d5 - this.vehicleFirstGoodZ;
            double d9 = entity.getDeltaMovement().lengthSqr();
            double d10 = d6 * d6 + d7 * d7 + d8 * d8;
            if (d10 - d9 > 100.0D && !this.isSingleplayerOwner()) {
               LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName().getString(), this.player.getName().getString(), d6, d7, d8);
               this.connection.send(new SMoveVehiclePacket(entity));
               return;
            }

            boolean flag = serverworld.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));
            d6 = d3 - this.vehicleLastGoodX;
            d7 = d4 - this.vehicleLastGoodY - 1.0E-6D;
            d8 = d5 - this.vehicleLastGoodZ;
            entity.move(MoverType.PLAYER, new Vector3d(d6, d7, d8));
            d6 = d3 - entity.getX();
            d7 = d4 - entity.getY();
            if (d7 > -0.5D || d7 < 0.5D) {
               d7 = 0.0D;
            }

            d8 = d5 - entity.getZ();
            d10 = d6 * d6 + d7 * d7 + d8 * d8;
            boolean flag1 = false;
            if (d10 > 0.0625D) {
               flag1 = true;
               LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", entity.getName().getString(), this.player.getName().getString(), Math.sqrt(d10));
            }

            entity.absMoveTo(d3, d4, d5, f, f1);
            this.player.absMoveTo(d3, d4, d5, this.player.yRot, this.player.xRot); // Forge - Resync player position on vehicle moving
            boolean flag2 = serverworld.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));
            if (flag && (flag1 || !flag2)) {
               entity.absMoveTo(d0, d1, d2, f, f1);
               this.player.absMoveTo(d3, d4, d5, this.player.yRot, this.player.xRot); // Forge - Resync player position on vehicle moving
               this.connection.send(new SMoveVehiclePacket(entity));
               return;
            }

            this.player.getLevel().getChunkSource().move(this.player);
            this.player.checkMovementStatistics(this.player.getX() - d0, this.player.getY() - d1, this.player.getZ() - d2);
            this.clientVehicleIsFloating = d7 >= -0.03125D && !this.server.isFlightAllowed() && this.noBlocksAround(entity);
            this.vehicleLastGoodX = entity.getX();
            this.vehicleLastGoodY = entity.getY();
            this.vehicleLastGoodZ = entity.getZ();
         }

      }
   }

   private boolean noBlocksAround(Entity p_241162_1_) {
      return BlockPos.betweenClosedStream(p_241162_1_.getBoundingBox().inflate(0.0625D).expandTowards(0.0D, -0.55D, 0.0D)).allMatch(b -> p_241162_1_.level.getBlockState(b).isAir(p_241162_1_.level, b));
   }

   public void handleAcceptTeleportPacket(CConfirmTeleportPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (pPacket.getId() == this.awaitingTeleport) {
         this.player.absMoveTo(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.yRot, this.player.xRot);
         this.lastGoodX = this.awaitingPositionFromClient.x;
         this.lastGoodY = this.awaitingPositionFromClient.y;
         this.lastGoodZ = this.awaitingPositionFromClient.z;
         if (this.player.isChangingDimension()) {
            this.player.hasChangedDimension();
         }

         this.awaitingPositionFromClient = null;
      }

   }

   public void handleRecipeBookSeenRecipePacket(CMarkRecipeSeenPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.server.getRecipeManager().byKey(pPacket.getRecipe()).ifPresent(this.player.getRecipeBook()::removeHighlight);
   }

   public void handleRecipeBookChangeSettingsPacket(CUpdateRecipeBookStatusPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.getRecipeBook().setBookSetting(pPacket.getBookType(), pPacket.isOpen(), pPacket.isFiltering());
   }

   public void handleSeenAdvancements(CSeenAdvancementsPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (pPacket.getAction() == CSeenAdvancementsPacket.Action.OPENED_TAB) {
         ResourceLocation resourcelocation = pPacket.getTab();
         Advancement advancement = this.server.getAdvancements().getAdvancement(resourcelocation);
         if (advancement != null) {
            this.player.getAdvancements().setSelectedTab(advancement);
         }
      }

   }

   /**
    * This method is only called for manual tab-completion (the {@link
    * net.minecraft.command.arguments.SuggestionProviders#ASK_SERVER minecraft:ask_server} suggestion provider).
    */
   public void handleCustomCommandSuggestions(CTabCompletePacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      StringReader stringreader = new StringReader(pPacket.getCommand());
      if (stringreader.canRead() && stringreader.peek() == '/') {
         stringreader.skip();
      }

      ParseResults<CommandSource> parseresults = this.server.getCommands().getDispatcher().parse(stringreader, this.player.createCommandSourceStack());
      this.server.getCommands().getDispatcher().getCompletionSuggestions(parseresults).thenAccept((p_195519_2_) -> {
         this.connection.send(new STabCompletePacket(pPacket.getId(), p_195519_2_));
      });
   }

   public void handleSetCommandBlock(CUpdateCommandBlockPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (!this.server.isCommandBlockEnabled()) {
         this.player.sendMessage(new TranslationTextComponent("advMode.notEnabled"), Util.NIL_UUID);
      } else if (!this.player.canUseGameMasterBlocks()) {
         this.player.sendMessage(new TranslationTextComponent("advMode.notAllowed"), Util.NIL_UUID);
      } else {
         CommandBlockLogic commandblocklogic = null;
         CommandBlockTileEntity commandblocktileentity = null;
         BlockPos blockpos = pPacket.getPos();
         TileEntity tileentity = this.player.level.getBlockEntity(blockpos);
         if (tileentity instanceof CommandBlockTileEntity) {
            commandblocktileentity = (CommandBlockTileEntity)tileentity;
            commandblocklogic = commandblocktileentity.getCommandBlock();
         }

         String s = pPacket.getCommand();
         boolean flag = pPacket.isTrackOutput();
         if (commandblocklogic != null) {
            CommandBlockTileEntity.Mode commandblocktileentity$mode = commandblocktileentity.getMode();
            Direction direction = this.player.level.getBlockState(blockpos).getValue(CommandBlockBlock.FACING);
            switch(pPacket.getMode()) {
            case SEQUENCE:
               BlockState blockstate1 = Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
               this.player.level.setBlock(blockpos, blockstate1.setValue(CommandBlockBlock.FACING, direction).setValue(CommandBlockBlock.CONDITIONAL, Boolean.valueOf(pPacket.isConditional())), 2);
               break;
            case AUTO:
               BlockState blockstate = Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
               this.player.level.setBlock(blockpos, blockstate.setValue(CommandBlockBlock.FACING, direction).setValue(CommandBlockBlock.CONDITIONAL, Boolean.valueOf(pPacket.isConditional())), 2);
               break;
            case REDSTONE:
            default:
               BlockState blockstate2 = Blocks.COMMAND_BLOCK.defaultBlockState();
               this.player.level.setBlock(blockpos, blockstate2.setValue(CommandBlockBlock.FACING, direction).setValue(CommandBlockBlock.CONDITIONAL, Boolean.valueOf(pPacket.isConditional())), 2);
            }

            tileentity.clearRemoved();
            this.player.level.setBlockEntity(blockpos, tileentity);
            commandblocklogic.setCommand(s);
            commandblocklogic.setTrackOutput(flag);
            if (!flag) {
               commandblocklogic.setLastOutput((ITextComponent)null);
            }

            commandblocktileentity.setAutomatic(pPacket.isAutomatic());
            if (commandblocktileentity$mode != pPacket.getMode()) {
               commandblocktileentity.onModeSwitch();
            }

            commandblocklogic.onUpdated();
            if (!StringUtils.isNullOrEmpty(s)) {
               this.player.sendMessage(new TranslationTextComponent("advMode.setCommand.success", s), Util.NIL_UUID);
            }
         }

      }
   }

   public void handleSetCommandMinecart(CUpdateMinecartCommandBlockPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (!this.server.isCommandBlockEnabled()) {
         this.player.sendMessage(new TranslationTextComponent("advMode.notEnabled"), Util.NIL_UUID);
      } else if (!this.player.canUseGameMasterBlocks()) {
         this.player.sendMessage(new TranslationTextComponent("advMode.notAllowed"), Util.NIL_UUID);
      } else {
         CommandBlockLogic commandblocklogic = pPacket.getCommandBlock(this.player.level);
         if (commandblocklogic != null) {
            commandblocklogic.setCommand(pPacket.getCommand());
            commandblocklogic.setTrackOutput(pPacket.isTrackOutput());
            if (!pPacket.isTrackOutput()) {
               commandblocklogic.setLastOutput((ITextComponent)null);
            }

            commandblocklogic.onUpdated();
            this.player.sendMessage(new TranslationTextComponent("advMode.setCommand.success", pPacket.getCommand()), Util.NIL_UUID);
         }

      }
   }

   public void handlePickItem(CPickItemPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.inventory.pickSlot(pPacket.getSlot());
      this.player.connection.send(new SSetSlotPacket(-2, this.player.inventory.selected, this.player.inventory.getItem(this.player.inventory.selected)));
      this.player.connection.send(new SSetSlotPacket(-2, pPacket.getSlot(), this.player.inventory.getItem(pPacket.getSlot())));
      this.player.connection.send(new SHeldItemChangePacket(this.player.inventory.selected));
   }

   public void handleRenameItem(CRenameItemPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.containerMenu instanceof RepairContainer) {
         RepairContainer repaircontainer = (RepairContainer)this.player.containerMenu;
         String s = SharedConstants.filterText(pPacket.getName());
         if (s.length() <= 35) {
            repaircontainer.setItemName(s);
         }
      }

   }

   public void handleSetBeaconPacket(CUpdateBeaconPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.containerMenu instanceof BeaconContainer) {
         ((BeaconContainer)this.player.containerMenu).updateEffects(pPacket.getPrimary(), pPacket.getSecondary());
      }

   }

   public void handleSetStructureBlock(CUpdateStructureBlockPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos blockpos = pPacket.getPos();
         BlockState blockstate = this.player.level.getBlockState(blockpos);
         TileEntity tileentity = this.player.level.getBlockEntity(blockpos);
         if (tileentity instanceof StructureBlockTileEntity) {
            StructureBlockTileEntity structureblocktileentity = (StructureBlockTileEntity)tileentity;
            structureblocktileentity.setMode(pPacket.getMode());
            structureblocktileentity.setStructureName(pPacket.getName());
            structureblocktileentity.setStructurePos(pPacket.getOffset());
            structureblocktileentity.setStructureSize(pPacket.getSize());
            structureblocktileentity.setMirror(pPacket.getMirror());
            structureblocktileentity.setRotation(pPacket.getRotation());
            structureblocktileentity.setMetaData(pPacket.getData());
            structureblocktileentity.setIgnoreEntities(pPacket.isIgnoreEntities());
            structureblocktileentity.setShowAir(pPacket.isShowAir());
            structureblocktileentity.setShowBoundingBox(pPacket.isShowBoundingBox());
            structureblocktileentity.setIntegrity(pPacket.getIntegrity());
            structureblocktileentity.setSeed(pPacket.getSeed());
            if (structureblocktileentity.hasStructureName()) {
               String s = structureblocktileentity.getStructureName();
               if (pPacket.getUpdateType() == StructureBlockTileEntity.UpdateCommand.SAVE_AREA) {
                  if (structureblocktileentity.saveStructure()) {
                     this.player.displayClientMessage(new TranslationTextComponent("structure_block.save_success", s), false);
                  } else {
                     this.player.displayClientMessage(new TranslationTextComponent("structure_block.save_failure", s), false);
                  }
               } else if (pPacket.getUpdateType() == StructureBlockTileEntity.UpdateCommand.LOAD_AREA) {
                  if (!structureblocktileentity.isStructureLoadable()) {
                     this.player.displayClientMessage(new TranslationTextComponent("structure_block.load_not_found", s), false);
                  } else if (structureblocktileentity.loadStructure(this.player.getLevel())) {
                     this.player.displayClientMessage(new TranslationTextComponent("structure_block.load_success", s), false);
                  } else {
                     this.player.displayClientMessage(new TranslationTextComponent("structure_block.load_prepare", s), false);
                  }
               } else if (pPacket.getUpdateType() == StructureBlockTileEntity.UpdateCommand.SCAN_AREA) {
                  if (structureblocktileentity.detectSize()) {
                     this.player.displayClientMessage(new TranslationTextComponent("structure_block.size_success", s), false);
                  } else {
                     this.player.displayClientMessage(new TranslationTextComponent("structure_block.size_failure"), false);
                  }
               }
            } else {
               this.player.displayClientMessage(new TranslationTextComponent("structure_block.invalid_structure_name", pPacket.getName()), false);
            }

            structureblocktileentity.setChanged();
            this.player.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
         }

      }
   }

   public void handleSetJigsawBlock(CUpdateJigsawBlockPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos blockpos = pPacket.getPos();
         BlockState blockstate = this.player.level.getBlockState(blockpos);
         TileEntity tileentity = this.player.level.getBlockEntity(blockpos);
         if (tileentity instanceof JigsawTileEntity) {
            JigsawTileEntity jigsawtileentity = (JigsawTileEntity)tileentity;
            jigsawtileentity.setName(pPacket.getName());
            jigsawtileentity.setTarget(pPacket.getTarget());
            jigsawtileentity.setPool(pPacket.getPool());
            jigsawtileentity.setFinalState(pPacket.getFinalState());
            jigsawtileentity.setJoint(pPacket.getJoint());
            jigsawtileentity.setChanged();
            this.player.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
         }

      }
   }

   public void handleJigsawGenerate(CJigsawBlockGeneratePacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos blockpos = pPacket.getPos();
         TileEntity tileentity = this.player.level.getBlockEntity(blockpos);
         if (tileentity instanceof JigsawTileEntity) {
            JigsawTileEntity jigsawtileentity = (JigsawTileEntity)tileentity;
            jigsawtileentity.generate(this.player.getLevel(), pPacket.levels(), pPacket.keepJigsaws());
         }

      }
   }

   public void handleSelectTrade(CSelectTradePacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      int i = pPacket.getItem();
      Container container = this.player.containerMenu;
      if (container instanceof MerchantContainer) {
         MerchantContainer merchantcontainer = (MerchantContainer)container;
         merchantcontainer.setSelectionHint(i);
         merchantcontainer.tryMoveItems(i);
      }

   }

   public void handleEditBook(CEditBookPacket pPacket) {
      ItemStack itemstack = pPacket.getBook();
      if (itemstack.getItem() == Items.WRITABLE_BOOK) {
         CompoundNBT compoundnbt = itemstack.getTag();
         if (WritableBookItem.makeSureTagIsValid(compoundnbt)) {
            List<String> list = Lists.newArrayList();
            boolean flag = pPacket.isSigning();
            if (flag) {
               list.add(compoundnbt.getString("title"));
            }

            ListNBT listnbt = compoundnbt.getList("pages", 8);

            for(int i = 0; i < listnbt.size(); ++i) {
               list.add(listnbt.getString(i));
            }

            int j = pPacket.getSlot();
            if (PlayerInventory.isHotbarSlot(j) || j == 40) {
               this.filterTextPacket(list, flag ? (p_244543_2_) -> {
                  this.signBook(p_244543_2_.get(0), p_244543_2_.subList(1, p_244543_2_.size()), j);
               } : (p_244531_2_) -> {
                  this.updateBookContents(p_244531_2_, j);
               });
            }
         }
      }
   }

   private void updateBookContents(List<String> p_244536_1_, int p_244536_2_) {
      ItemStack itemstack = this.player.inventory.getItem(p_244536_2_);
      if (itemstack.getItem() == Items.WRITABLE_BOOK) {
         ListNBT listnbt = new ListNBT();
         p_244536_1_.stream().map(StringNBT::valueOf).forEach(listnbt::add);
         itemstack.addTagElement("pages", listnbt);
      }
   }

   private void signBook(String p_244534_1_, List<String> p_244534_2_, int p_244534_3_) {
      ItemStack itemstack = this.player.inventory.getItem(p_244534_3_);
      if (itemstack.getItem() == Items.WRITABLE_BOOK) {
         ItemStack itemstack1 = new ItemStack(Items.WRITTEN_BOOK);
         CompoundNBT compoundnbt = itemstack.getTag();
         if (compoundnbt != null) {
            itemstack1.setTag(compoundnbt.copy());
         }

         itemstack1.addTagElement("author", StringNBT.valueOf(this.player.getName().getString()));
         itemstack1.addTagElement("title", StringNBT.valueOf(p_244534_1_));
         ListNBT listnbt = new ListNBT();

         for(String s : p_244534_2_) {
            ITextComponent itextcomponent = new StringTextComponent(s);
            String s1 = ITextComponent.Serializer.toJson(itextcomponent);
            listnbt.add(StringNBT.valueOf(s1));
         }

         itemstack1.addTagElement("pages", listnbt);
         this.player.inventory.setItem(p_244534_3_, itemstack1);
      }
   }

   public void handleEntityTagQuery(CQueryEntityNBTPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.hasPermissions(2)) {
         Entity entity = this.player.getLevel().getEntity(pPacket.getEntityId());
         if (entity != null) {
            CompoundNBT compoundnbt = entity.saveWithoutId(new CompoundNBT());
            this.player.connection.send(new SQueryNBTResponsePacket(pPacket.getTransactionId(), compoundnbt));
         }

      }
   }

   public void handleBlockEntityTagQuery(CQueryTileEntityNBTPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.hasPermissions(2)) {
         TileEntity tileentity = this.player.getLevel().getBlockEntity(pPacket.getPos());
         CompoundNBT compoundnbt = tileentity != null ? tileentity.save(new CompoundNBT()) : null;
         this.player.connection.send(new SQueryNBTResponsePacket(pPacket.getTransactionId(), compoundnbt));
      }
   }

   /**
    * Processes clients perspective on player positioning and/or orientation
    */
   public void handleMovePlayer(CPlayerPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (containsInvalidValues(pPacket)) {
         this.disconnect(new TranslationTextComponent("multiplayer.disconnect.invalid_player_movement"));
      } else {
         ServerWorld serverworld = this.player.getLevel();
         if (!this.player.wonGame) {
            if (this.tickCount == 0) {
               this.resetPosition();
            }

            if (this.awaitingPositionFromClient != null) {
               if (this.tickCount - this.awaitingTeleportTime > 20) {
                  this.awaitingTeleportTime = this.tickCount;
                  this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.yRot, this.player.xRot);
               }

            } else {
               this.awaitingTeleportTime = this.tickCount;
               if (this.player.isPassenger()) {
                  this.player.absMoveTo(this.player.getX(), this.player.getY(), this.player.getZ(), pPacket.getYRot(this.player.yRot), pPacket.getXRot(this.player.xRot));
                  this.player.getLevel().getChunkSource().move(this.player);
               } else {
                  double d0 = this.player.getX();
                  double d1 = this.player.getY();
                  double d2 = this.player.getZ();
                  double d3 = this.player.getY();
                  double d4 = pPacket.getX(this.player.getX());
                  double d5 = pPacket.getY(this.player.getY());
                  double d6 = pPacket.getZ(this.player.getZ());
                  float f = pPacket.getYRot(this.player.yRot);
                  float f1 = pPacket.getXRot(this.player.xRot);
                  double d7 = d4 - this.firstGoodX;
                  double d8 = d5 - this.firstGoodY;
                  double d9 = d6 - this.firstGoodZ;
                  double d10 = this.player.getDeltaMovement().lengthSqr();
                  double d11 = d7 * d7 + d8 * d8 + d9 * d9;
                  if (this.player.isSleeping()) {
                     if (d11 > 1.0D) {
                        this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), pPacket.getYRot(this.player.yRot), pPacket.getXRot(this.player.xRot));
                     }

                  } else {
                     ++this.receivedMovePacketCount;
                     int i = this.receivedMovePacketCount - this.knownMovePacketCount;
                     if (i > 5) {
                        LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), i);
                        i = 1;
                     }

                     if (!this.player.isChangingDimension() && (!this.player.getLevel().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                        float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;
                        if (d11 - d10 > (double)(f2 * (float)i) && !this.isSingleplayerOwner()) {
                           LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName().getString(), d7, d8, d9);
                           this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.yRot, this.player.xRot);
                           return;
                        }
                     }

                     AxisAlignedBB axisalignedbb = this.player.getBoundingBox();
                     d7 = d4 - this.lastGoodX;
                     d8 = d5 - this.lastGoodY;
                     d9 = d6 - this.lastGoodZ;
                     boolean flag = d8 > 0.0D;
                     if (this.player.isOnGround() && !pPacket.isOnGround() && flag) {
                        this.player.jumpFromGround();
                     }

                     this.player.move(MoverType.PLAYER, new Vector3d(d7, d8, d9));
                     d7 = d4 - this.player.getX();
                     d8 = d5 - this.player.getY();
                     if (d8 > -0.5D || d8 < 0.5D) {
                        d8 = 0.0D;
                     }

                     d9 = d6 - this.player.getZ();
                     d11 = d7 * d7 + d8 * d8 + d9 * d9;
                     boolean flag1 = false;
                     if (!this.player.isChangingDimension() && d11 > 0.0625D && !this.player.isSleeping() && !this.player.gameMode.isCreative() && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                        flag1 = true;
                        LOGGER.warn("{} moved wrongly!", (Object)this.player.getName().getString());
                     }

                     this.player.absMoveTo(d4, d5, d6, f, f1);
                     if (this.player.noPhysics || this.player.isSleeping() || (!flag1 || !serverworld.noCollision(this.player, axisalignedbb)) && !this.isPlayerCollidingWithAnythingNew(serverworld, axisalignedbb)) {
                        this.clientIsFloating = d8 >= -0.03125D && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && !this.server.isFlightAllowed() && !this.player.abilities.mayfly && !this.player.hasEffect(Effects.LEVITATION) && !this.player.isFallFlying() && this.noBlocksAround(this.player);
                        this.player.getLevel().getChunkSource().move(this.player);
                        this.player.doCheckFallDamage(this.player.getY() - d3, pPacket.isOnGround());
                        this.player.setOnGround(pPacket.isOnGround());
                        if (flag) {
                           this.player.fallDistance = 0.0F;
                        }

                        this.player.checkMovementStatistics(this.player.getX() - d0, this.player.getY() - d1, this.player.getZ() - d2);
                        this.lastGoodX = this.player.getX();
                        this.lastGoodY = this.player.getY();
                        this.lastGoodZ = this.player.getZ();
                     } else {
                        this.teleport(d0, d1, d2, f, f1);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isPlayerCollidingWithAnythingNew(IWorldReader p_241163_1_, AxisAlignedBB p_241163_2_) {
      Stream<VoxelShape> stream = p_241163_1_.getCollisions(this.player, this.player.getBoundingBox().deflate((double)1.0E-5F), (p_241167_0_) -> {
         return true;
      });
      VoxelShape voxelshape = VoxelShapes.create(p_241163_2_.deflate((double)1.0E-5F));
      return stream.anyMatch((p_241164_1_) -> {
         return !VoxelShapes.joinIsNotEmpty(p_241164_1_, voxelshape, IBooleanFunction.AND);
      });
   }

   public void teleport(double pX, double pY, double pZ, float pYaw, float pPitch) {
      this.teleport(pX, pY, pZ, pYaw, pPitch, Collections.emptySet());
   }

   /**
    * Teleports the player position to the (relative) values specified, and syncs to the client
    */
   public void teleport(double pX, double pY, double pZ, float pYaw, float pPitch, Set<SPlayerPositionLookPacket.Flags> pRelativeSet) {
      double d0 = pRelativeSet.contains(SPlayerPositionLookPacket.Flags.X) ? this.player.getX() : 0.0D;
      double d1 = pRelativeSet.contains(SPlayerPositionLookPacket.Flags.Y) ? this.player.getY() : 0.0D;
      double d2 = pRelativeSet.contains(SPlayerPositionLookPacket.Flags.Z) ? this.player.getZ() : 0.0D;
      float f = pRelativeSet.contains(SPlayerPositionLookPacket.Flags.Y_ROT) ? this.player.yRot : 0.0F;
      float f1 = pRelativeSet.contains(SPlayerPositionLookPacket.Flags.X_ROT) ? this.player.xRot : 0.0F;
      this.awaitingPositionFromClient = new Vector3d(pX, pY, pZ);
      if (++this.awaitingTeleport == Integer.MAX_VALUE) {
         this.awaitingTeleport = 0;
      }

      this.awaitingTeleportTime = this.tickCount;
      this.player.absMoveTo(pX, pY, pZ, pYaw, pPitch);
      this.player.connection.send(new SPlayerPositionLookPacket(pX - d0, pY - d1, pZ - d2, pYaw - f, pPitch - f1, pRelativeSet, this.awaitingTeleport));
   }

   /**
    * Processes the player initiating/stopping digging on a particular spot, as well as a player dropping items
    */
   public void handlePlayerAction(CPlayerDiggingPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      BlockPos blockpos = pPacket.getPos();
      this.player.resetLastActionTime();
      CPlayerDiggingPacket.Action cplayerdiggingpacket$action = pPacket.getAction();
      switch(cplayerdiggingpacket$action) {
      case SWAP_ITEM_WITH_OFFHAND:
         if (!this.player.isSpectator()) {
            ItemStack itemstack = this.player.getItemInHand(Hand.OFF_HAND);
            this.player.setItemInHand(Hand.OFF_HAND, this.player.getItemInHand(Hand.MAIN_HAND));
            this.player.setItemInHand(Hand.MAIN_HAND, itemstack);
            this.player.stopUsingItem();
         }

         return;
      case DROP_ITEM:
         if (!this.player.isSpectator()) {
            this.player.drop(false);
         }

         return;
      case DROP_ALL_ITEMS:
         if (!this.player.isSpectator()) {
            this.player.drop(true);
         }

         return;
      case RELEASE_USE_ITEM:
         this.player.releaseUsingItem();
         return;
      case START_DESTROY_BLOCK:
      case ABORT_DESTROY_BLOCK:
      case STOP_DESTROY_BLOCK:
         this.player.gameMode.handleBlockBreakAction(blockpos, cplayerdiggingpacket$action, pPacket.getDirection(), this.server.getMaxBuildHeight());
         return;
      default:
         throw new IllegalArgumentException("Invalid player action");
      }
   }

   private static boolean wasBlockPlacementAttempt(ServerPlayerEntity p_241166_0_, ItemStack p_241166_1_) {
      if (p_241166_1_.isEmpty()) {
         return false;
      } else {
         Item item = p_241166_1_.getItem();
         return (item instanceof BlockItem || item instanceof BucketItem) && !p_241166_0_.getCooldowns().isOnCooldown(item);
      }
   }

   public void handleUseItemOn(CPlayerTryUseItemOnBlockPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      ServerWorld serverworld = this.player.getLevel();
      Hand hand = pPacket.getHand();
      ItemStack itemstack = this.player.getItemInHand(hand);
      BlockRayTraceResult blockraytraceresult = pPacket.getHitResult();
      BlockPos blockpos = blockraytraceresult.getBlockPos();
      Direction direction = blockraytraceresult.getDirection();
      this.player.resetLastActionTime();
      if (blockpos.getY() < this.server.getMaxBuildHeight()) {
         double dist = player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue() + 3;
         dist *= dist;
         if (this.awaitingPositionFromClient == null && this.player.distanceToSqr((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D) < dist && serverworld.mayInteract(this.player, blockpos)) {
            ActionResultType actionresulttype = this.player.gameMode.useItemOn(this.player, serverworld, itemstack, hand, blockraytraceresult);
            if (direction == Direction.UP && !actionresulttype.consumesAction() && blockpos.getY() >= this.server.getMaxBuildHeight() - 1 && wasBlockPlacementAttempt(this.player, itemstack)) {
               ITextComponent itextcomponent = (new TranslationTextComponent("build.tooHigh", this.server.getMaxBuildHeight())).withStyle(TextFormatting.RED);
               this.player.connection.send(new SChatPacket(itextcomponent, ChatType.GAME_INFO, Util.NIL_UUID));
            } else if (actionresulttype.shouldSwing()) {
               this.player.swing(hand, true);
            }
         }
      } else {
         ITextComponent itextcomponent1 = (new TranslationTextComponent("build.tooHigh", this.server.getMaxBuildHeight())).withStyle(TextFormatting.RED);
         this.player.connection.send(new SChatPacket(itextcomponent1, ChatType.GAME_INFO, Util.NIL_UUID));
      }

      this.player.connection.send(new SChangeBlockPacket(serverworld, blockpos));
      this.player.connection.send(new SChangeBlockPacket(serverworld, blockpos.relative(direction)));
   }

   /**
    * Called when a client is using an item while not pointing at a block, but simply using an item
    */
   public void handleUseItem(CPlayerTryUseItemPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      ServerWorld serverworld = this.player.getLevel();
      Hand hand = pPacket.getHand();
      ItemStack itemstack = this.player.getItemInHand(hand);
      this.player.resetLastActionTime();
      if (!itemstack.isEmpty()) {
         ActionResultType actionresulttype = this.player.gameMode.useItem(this.player, serverworld, itemstack, hand);
         if (actionresulttype.shouldSwing()) {
            this.player.swing(hand, true);
         }

      }
   }

   public void handleTeleportToEntityPacket(CSpectatePacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.isSpectator()) {
         for(ServerWorld serverworld : this.server.getAllLevels()) {
            Entity entity = pPacket.getEntity(serverworld);
            if (entity != null) {
               this.player.teleportTo(serverworld, entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
               return;
            }
         }
      }

   }

   public void handleResourcePackResponse(CResourcePackStatusPacket pPacket) {
   }

   public void handlePaddleBoat(CSteerBoatPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      Entity entity = this.player.getVehicle();
      if (entity instanceof BoatEntity) {
         ((BoatEntity)entity).setPaddleState(pPacket.getLeft(), pPacket.getRight());
      }

   }

   /**
    * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
    */
   public void onDisconnect(ITextComponent pReason) {
      LOGGER.info("{} lost connection: {}", this.player.getName().getString(), pReason.getString());
      this.server.invalidateStatus();
      this.server.getPlayerList().broadcastMessage((new TranslationTextComponent("multiplayer.player.left", this.player.getDisplayName())).withStyle(TextFormatting.YELLOW), ChatType.SYSTEM, Util.NIL_UUID);
      this.player.disconnect();
      this.server.getPlayerList().remove(this.player);
      IChatFilter ichatfilter = this.player.getTextFilter();
      if (ichatfilter != null) {
         ichatfilter.leave();
      }

      if (this.isSingleplayerOwner()) {
         LOGGER.info("Stopping singleplayer server as player logged out");
         this.server.halt(false);
      }

   }

   public void send(IPacket<?> p_147359_1_) {
      this.send(p_147359_1_, (GenericFutureListener<? extends Future<? super Void>>)null);
   }

   public void send(IPacket<?> pPacket, @Nullable GenericFutureListener<? extends Future<? super Void>> pFutureListeners) {
      if (pPacket instanceof SChatPacket) {
         SChatPacket schatpacket = (SChatPacket)pPacket;
         ChatVisibility chatvisibility = this.player.getChatVisibility();
         if (chatvisibility == ChatVisibility.HIDDEN && schatpacket.getType() != ChatType.GAME_INFO) {
            return;
         }

         if (chatvisibility == ChatVisibility.SYSTEM && !schatpacket.isSystem()) {
            return;
         }
      }

      try {
         this.connection.send(pPacket, pFutureListeners);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Sending packet");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Packet being sent");
         crashreportcategory.setDetail("Packet class", () -> {
            return pPacket.getClass().getCanonicalName();
         });
         throw new ReportedException(crashreport);
      }
   }

   /**
    * Updates which quickbar slot is selected
    */
   public void handleSetCarriedItem(CHeldItemChangePacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (pPacket.getSlot() >= 0 && pPacket.getSlot() < PlayerInventory.getSelectionSize()) {
         if (this.player.inventory.selected != pPacket.getSlot() && this.player.getUsedItemHand() == Hand.MAIN_HAND) {
            this.player.stopUsingItem();
         }

         this.player.inventory.selected = pPacket.getSlot();
         this.player.resetLastActionTime();
      } else {
         LOGGER.warn("{} tried to set an invalid carried item", (Object)this.player.getName().getString());
      }
   }

   /**
    * Process chat messages (broadcast back to clients) and commands (executes)
    */
   public void handleChat(CChatMessagePacket pPacket) {
      String s = org.apache.commons.lang3.StringUtils.normalizeSpace(pPacket.getMessage());
      if (s.startsWith("/")) {
         PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
         this.handleChat(s);
      } else {
         this.filterTextPacket(s, this::handleChat);
      }

   }

   private void handleChat(String p_244548_1_) {
      if (this.player.getChatVisibility() == ChatVisibility.HIDDEN) {
         this.send(new SChatPacket((new TranslationTextComponent("chat.cannotSend")).withStyle(TextFormatting.RED), ChatType.SYSTEM, Util.NIL_UUID));
      } else {
         this.player.resetLastActionTime();

         for(int i = 0; i < p_244548_1_.length(); ++i) {
            if (!SharedConstants.isAllowedChatCharacter(p_244548_1_.charAt(i))) {
               this.disconnect(new TranslationTextComponent("multiplayer.disconnect.illegal_characters"));
               return;
            }
         }

         if (p_244548_1_.startsWith("/")) {
            this.handleCommand(p_244548_1_);
         } else {
            ITextComponent itextcomponent = new TranslationTextComponent("chat.type.text", this.player.getDisplayName(), net.minecraftforge.common.ForgeHooks.newChatWithLinks(p_244548_1_));
            itextcomponent = net.minecraftforge.common.ForgeHooks.onServerChatEvent(this, p_244548_1_, itextcomponent);
            if (itextcomponent == null) return;
            this.server.getPlayerList().broadcastMessage(itextcomponent, ChatType.CHAT, this.player.getUUID());
         }

         this.chatSpamTickCount += 20;
         if (this.chatSpamTickCount > 200 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
            this.disconnect(new TranslationTextComponent("disconnect.spam"));
         }

      }
   }

   /**
    * Handle commands that start with a /
    */
   private void handleCommand(String pCommand) {
      this.server.getCommands().performCommand(this.player.createCommandSourceStack(), pCommand);
   }

   public void handleAnimate(CAnimateHandPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.resetLastActionTime();
      this.player.swing(pPacket.getHand());
   }

   /**
    * Processes a range of action-types: sneaking, sprinting, waking from sleep, opening the inventory or setting jump
    * height of the horse the player is riding
    */
   public void handlePlayerCommand(CEntityActionPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.resetLastActionTime();
      switch(pPacket.getAction()) {
      case PRESS_SHIFT_KEY:
         this.player.setShiftKeyDown(true);
         break;
      case RELEASE_SHIFT_KEY:
         this.player.setShiftKeyDown(false);
         break;
      case START_SPRINTING:
         this.player.setSprinting(true);
         break;
      case STOP_SPRINTING:
         this.player.setSprinting(false);
         break;
      case STOP_SLEEPING:
         if (this.player.isSleeping()) {
            this.player.stopSleepInBed(false, true);
            this.awaitingPositionFromClient = this.player.position();
         }
         break;
      case START_RIDING_JUMP:
         if (this.player.getVehicle() instanceof IJumpingMount) {
            IJumpingMount ijumpingmount1 = (IJumpingMount)this.player.getVehicle();
            int i = pPacket.getData();
            if (ijumpingmount1.canJump() && i > 0) {
               ijumpingmount1.handleStartJump(i);
            }
         }
         break;
      case STOP_RIDING_JUMP:
         if (this.player.getVehicle() instanceof IJumpingMount) {
            IJumpingMount ijumpingmount = (IJumpingMount)this.player.getVehicle();
            ijumpingmount.handleStopJump();
         }
         break;
      case OPEN_INVENTORY:
         if (this.player.getVehicle() instanceof AbstractHorseEntity) {
            ((AbstractHorseEntity)this.player.getVehicle()).openInventory(this.player);
         }
         break;
      case START_FALL_FLYING:
         if (!this.player.tryToStartFallFlying()) {
            this.player.stopFallFlying();
         }
         break;
      default:
         throw new IllegalArgumentException("Invalid client command!");
      }

   }

   /**
    * Processes left and right clicks on entities
    */
   public void handleInteract(CUseEntityPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      ServerWorld serverworld = this.player.getLevel();
      Entity entity = pPacket.getTarget(serverworld);
      this.player.resetLastActionTime();
      this.player.setShiftKeyDown(pPacket.isUsingSecondaryAction());
      if (entity != null) {
         double d0 = 36.0D;
         if (this.player.distanceToSqr(entity) < 36.0D) {
            Hand hand = pPacket.getHand();
            ItemStack itemstack = hand != null ? this.player.getItemInHand(hand).copy() : ItemStack.EMPTY;
            Optional<ActionResultType> optional = Optional.empty();
            if (pPacket.getAction() == CUseEntityPacket.Action.INTERACT) {
               optional = Optional.of(this.player.interactOn(entity, hand));
            } else if (pPacket.getAction() == CUseEntityPacket.Action.INTERACT_AT) {
               if (net.minecraftforge.common.ForgeHooks.onInteractEntityAt(player, entity, pPacket.getLocation(), hand) != null) return;
               optional = Optional.of(entity.interactAt(this.player, pPacket.getLocation(), hand));
            } else if (pPacket.getAction() == CUseEntityPacket.Action.ATTACK) {
               if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof AbstractArrowEntity || entity == this.player) {
                  this.disconnect(new TranslationTextComponent("multiplayer.disconnect.invalid_entity_attacked"));
                  LOGGER.warn("Player {} tried to attack an invalid entity", (Object)this.player.getName().getString());
                  return;
               }

               this.player.attack(entity);
            }

            if (optional.isPresent() && optional.get().consumesAction()) {
               CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(this.player, itemstack, entity);
               if (optional.get().shouldSwing()) {
                  this.player.swing(hand, true);
               }
            }
         }
      }

   }

   /**
    * Processes the client status updates: respawn attempt from player, opening statistics or achievements, or acquiring
    * 'open inventory' achievement
    */
   public void handleClientCommand(CClientStatusPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.resetLastActionTime();
      CClientStatusPacket.State cclientstatuspacket$state = pPacket.getAction();
      switch(cclientstatuspacket$state) {
      case PERFORM_RESPAWN:
         if (this.player.wonGame) {
            this.player.wonGame = false;
            this.player = this.server.getPlayerList().respawn(this.player, true);
            CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, World.END, World.OVERWORLD);
         } else {
            if (this.player.getHealth() > 0.0F) {
               return;
            }

            this.player = this.server.getPlayerList().respawn(this.player, false);
            if (this.server.isHardcore()) {
               this.player.setGameMode(GameType.SPECTATOR);
               this.player.getLevel().getGameRules().getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(false, this.server);
            }
         }
         break;
      case REQUEST_STATS:
         this.player.getStats().sendStats(this.player);
      }

   }

   /**
    * Processes the client closing windows (container)
    */
   public void handleContainerClose(CCloseWindowPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.doCloseContainer();
   }

   /**
    * Executes a container/inventory slot manipulation as indicated by the packet. Sends the serverside result if they
    * didn't match the indicated result and prevents further manipulation by the player until he confirms that it has
    * the same open container/inventory
    */
   public void handleContainerClick(CClickWindowPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.resetLastActionTime();
      if (this.player.containerMenu.containerId == pPacket.getContainerId() && this.player.containerMenu.isSynched(this.player)) {
         if (this.player.isSpectator()) {
            NonNullList<ItemStack> nonnulllist = NonNullList.create();

            for(int i = 0; i < this.player.containerMenu.slots.size(); ++i) {
               nonnulllist.add(this.player.containerMenu.slots.get(i).getItem());
            }

            this.player.refreshContainer(this.player.containerMenu, nonnulllist);
         } else {
            ItemStack itemstack1 = this.player.containerMenu.clicked(pPacket.getSlotNum(), pPacket.getButtonNum(), pPacket.getClickType(), this.player);
            if (ItemStack.matches(pPacket.getItem(), itemstack1)) {
               this.player.connection.send(new SConfirmTransactionPacket(pPacket.getContainerId(), pPacket.getUid(), true));
               this.player.ignoreSlotUpdateHack = true;
               this.player.containerMenu.broadcastChanges();
               this.player.broadcastCarriedItem();
               this.player.ignoreSlotUpdateHack = false;
            } else {
               this.expectedAcks.put(this.player.containerMenu.containerId, pPacket.getUid());
               this.player.connection.send(new SConfirmTransactionPacket(pPacket.getContainerId(), pPacket.getUid(), false));
               this.player.containerMenu.setSynched(this.player, false);
               NonNullList<ItemStack> nonnulllist1 = NonNullList.create();

               for(int j = 0; j < this.player.containerMenu.slots.size(); ++j) {
                  ItemStack itemstack = this.player.containerMenu.slots.get(j).getItem();
                  nonnulllist1.add(itemstack.isEmpty() ? ItemStack.EMPTY : itemstack);
               }

               this.player.refreshContainer(this.player.containerMenu, nonnulllist1);
            }
         }
      }

   }

   public void handlePlaceRecipe(CPlaceRecipePacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.resetLastActionTime();
      if (!this.player.isSpectator() && this.player.containerMenu.containerId == pPacket.getContainerId() && this.player.containerMenu.isSynched(this.player) && this.player.containerMenu instanceof RecipeBookContainer) {
         this.server.getRecipeManager().byKey(pPacket.getRecipe()).ifPresent((p_241165_2_) -> {
            ((RecipeBookContainer)this.player.containerMenu).handlePlacement(pPacket.isShiftDown(), p_241165_2_, this.player);
         });
      }
   }

   /**
    * Enchants the item identified by the packet given some convoluted conditions (matching window, which
    * should/shouldn't be in use?)
    */
   public void handleContainerButtonClick(CEnchantItemPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.resetLastActionTime();
      if (this.player.containerMenu.containerId == pPacket.getContainerId() && this.player.containerMenu.isSynched(this.player) && !this.player.isSpectator()) {
         this.player.containerMenu.clickMenuButton(this.player, pPacket.getButtonId());
         this.player.containerMenu.broadcastChanges();
      }

   }

   /**
    * Update the server with an ItemStack in a slot.
    */
   public void handleSetCreativeModeSlot(CCreativeInventoryActionPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.gameMode.isCreative()) {
         boolean flag = pPacket.getSlotNum() < 0;
         ItemStack itemstack = pPacket.getItem();
         CompoundNBT compoundnbt = itemstack.getTagElement("BlockEntityTag");
         if (!itemstack.isEmpty() && compoundnbt != null && compoundnbt.contains("x") && compoundnbt.contains("y") && compoundnbt.contains("z")) {
            BlockPos blockpos = new BlockPos(compoundnbt.getInt("x"), compoundnbt.getInt("y"), compoundnbt.getInt("z"));
            TileEntity tileentity = this.player.level.getBlockEntity(blockpos);
            if (tileentity != null) {
               CompoundNBT compoundnbt1 = tileentity.save(new CompoundNBT());
               compoundnbt1.remove("x");
               compoundnbt1.remove("y");
               compoundnbt1.remove("z");
               itemstack.addTagElement("BlockEntityTag", compoundnbt1);
            }
         }

         boolean flag1 = pPacket.getSlotNum() >= 1 && pPacket.getSlotNum() <= 45;
         boolean flag2 = itemstack.isEmpty() || itemstack.getDamageValue() >= 0 && itemstack.getCount() <= 64 && !itemstack.isEmpty();
         if (flag1 && flag2) {
            if (itemstack.isEmpty()) {
               this.player.inventoryMenu.setItem(pPacket.getSlotNum(), ItemStack.EMPTY);
            } else {
               this.player.inventoryMenu.setItem(pPacket.getSlotNum(), itemstack);
            }

            this.player.inventoryMenu.setSynched(this.player, true);
            this.player.inventoryMenu.broadcastChanges();
         } else if (flag && flag2 && this.dropSpamTickCount < 200) {
            this.dropSpamTickCount += 20;
            this.player.drop(itemstack, true);
         }
      }

   }

   public void handleContainerAck(CConfirmTransactionPacket p_147339_1_) {
      PacketThreadUtil.ensureRunningOnSameThread(p_147339_1_, this, this.player.getLevel());
      int i = this.player.containerMenu.containerId;
      if (i == p_147339_1_.getContainerId() && this.expectedAcks.getOrDefault(i, (short)(p_147339_1_.getUid() + 1)) == p_147339_1_.getUid() && !this.player.containerMenu.isSynched(this.player) && !this.player.isSpectator()) {
         this.player.containerMenu.setSynched(this.player, true);
      }

   }

   public void handleSignUpdate(CUpdateSignPacket pPacket) {
      List<String> list = Stream.of(pPacket.getLines()).map(TextFormatting::stripFormatting).collect(Collectors.toList());
      this.filterTextPacket(list, (p_244547_2_) -> {
         this.updateSignText(pPacket, p_244547_2_);
      });
   }

   private void updateSignText(CUpdateSignPacket p_244542_1_, List<String> p_244542_2_) {
      this.player.resetLastActionTime();
      ServerWorld serverworld = this.player.getLevel();
      BlockPos blockpos = p_244542_1_.getPos();
      if (serverworld.hasChunkAt(blockpos)) {
         BlockState blockstate = serverworld.getBlockState(blockpos);
         TileEntity tileentity = serverworld.getBlockEntity(blockpos);
         if (!(tileentity instanceof SignTileEntity)) {
            return;
         }

         SignTileEntity signtileentity = (SignTileEntity)tileentity;
         if (!signtileentity.isEditable() || signtileentity.getPlayerWhoMayEdit() != this.player) {
            LOGGER.warn("Player {} just tried to change non-editable sign", (Object)this.player.getName().getString());
            return;
         }

         for(int i = 0; i < p_244542_2_.size(); ++i) {
            signtileentity.setMessage(i, new StringTextComponent(p_244542_2_.get(i)));
         }

         signtileentity.setChanged();
         serverworld.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
      }

   }

   /**
    * Updates a players' ping statistics
    */
   public void handleKeepAlive(CKeepAlivePacket pPacket) {
      if (this.keepAlivePending && pPacket.getId() == this.keepAliveChallenge) {
         int i = (int)(Util.getMillis() - this.keepAliveTime);
         this.player.latency = (this.player.latency * 3 + i) / 4;
         this.keepAlivePending = false;
      } else if (!this.isSingleplayerOwner()) {
         this.disconnect(new TranslationTextComponent("disconnect.timeout"));
      }

   }

   /**
    * Processes a player starting/stopping flying
    */
   public void handlePlayerAbilities(CPlayerAbilitiesPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.abilities.flying = pPacket.isFlying() && this.player.abilities.mayfly;
   }

   /**
    * Updates serverside copy of client settings: language, render distance, chat visibility, chat colours, difficulty,
    * and whether to show the cape
    */
   public void handleClientInformation(CClientSettingsPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      this.player.updateOptions(pPacket);
   }

   /**
    * Synchronizes serverside and clientside book contents and signing
    */
   public void handleCustomPayload(CCustomPayloadPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      net.minecraftforge.fml.network.NetworkHooks.onCustomPayload(pPacket, this.connection);
   }

   public void handleChangeDifficulty(CSetDifficultyPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
         this.server.setDifficulty(pPacket.getDifficulty(), false);
      }
   }

   public void handleLockDifficulty(CLockDifficultyPacket pPacket) {
      PacketThreadUtil.ensureRunningOnSameThread(pPacket, this, this.player.getLevel());
      if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
         this.server.setDifficultyLocked(pPacket.isLocked());
      }
   }
}

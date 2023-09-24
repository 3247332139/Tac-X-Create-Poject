package net.minecraft.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockVoxelShape;
import net.minecraft.util.Direction;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Block extends AbstractBlock implements IItemProvider, net.minecraftforge.common.extensions.IForgeBlock {
   protected static final Logger LOGGER = LogManager.getLogger();
   @Deprecated //Forge: Do not use, use GameRegistry
   public static final ObjectIntIdentityMap<BlockState> BLOCK_STATE_REGISTRY = net.minecraftforge.registries.GameData.getBlockStateIDMap();
   private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader<VoxelShape, Boolean>() {
      public Boolean load(VoxelShape p_load_1_) {
         return !VoxelShapes.joinIsNotEmpty(VoxelShapes.block(), p_load_1_, IBooleanFunction.NOT_SAME);
      }
   });
   protected final StateContainer<Block, BlockState> stateDefinition;
   private BlockState defaultBlockState;
   @Nullable
   private String descriptionId;
   @Nullable
   private Item item;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey>(2048, 0.25F) {
         protected void rehash(int p_rehash_1_) {
         }
      };
      object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
      return object2bytelinkedopenhashmap;
   });

   public static int getId(@Nullable BlockState pState) {
      if (pState == null) {
         return 0;
      } else {
         int i = BLOCK_STATE_REGISTRY.getId(pState);
         return i == -1 ? 0 : i;
      }
   }

   public static BlockState stateById(int pId) {
      BlockState blockstate = BLOCK_STATE_REGISTRY.byId(pId);
      return blockstate == null ? Blocks.AIR.defaultBlockState() : blockstate;
   }

   public static Block byItem(@Nullable Item pItem) {
      return pItem instanceof BlockItem ? ((BlockItem)pItem).getBlock() : Blocks.AIR;
   }

   public static BlockState pushEntitiesUp(BlockState pOldState, BlockState pNewState, World pLevel, BlockPos pPos) {
      VoxelShape voxelshape = VoxelShapes.joinUnoptimized(pOldState.getCollisionShape(pLevel, pPos), pNewState.getCollisionShape(pLevel, pPos), IBooleanFunction.ONLY_SECOND).move((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ());

      for(Entity entity : pLevel.getEntities((Entity)null, voxelshape.bounds())) {
         double d0 = VoxelShapes.collide(Direction.Axis.Y, entity.getBoundingBox().move(0.0D, 1.0D, 0.0D), Stream.of(voxelshape), -1.0D);
         entity.teleportTo(entity.getX(), entity.getY() + 1.0D + d0, entity.getZ());
      }

      return pNewState;
   }

   public static VoxelShape box(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2) {
      return VoxelShapes.box(pX1 / 16.0D, pY1 / 16.0D, pZ1 / 16.0D, pX2 / 16.0D, pY2 / 16.0D, pZ2 / 16.0D);
   }

   public boolean is(ITag<Block> p_203417_1_) {
      return p_203417_1_.contains(this);
   }

   public boolean is(Block p_235332_1_) {
      return this == p_235332_1_;
   }

   /**
    * With the provided block state, performs neighbor checks for all neighboring blocks to get an "adjusted" blockstate
    * for placement in the world, if the current state is not valid.
    */
   public static BlockState updateFromNeighbourShapes(BlockState pCurrentState, IWorld pLevel, BlockPos pPos) {
      BlockState blockstate = pCurrentState;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(Direction direction : UPDATE_SHAPE_ORDER) {
         blockpos$mutable.setWithOffset(pPos, direction);
         blockstate = blockstate.updateShape(direction, pLevel.getBlockState(blockpos$mutable), pLevel, pPos, blockpos$mutable);
      }

      return blockstate;
   }

   /**
    * Replaces oldState with newState, possibly playing effects and creating drops. Flags are as in {@link
    * net.minecraft.world.level.Level#setBlock}
    */
   public static void updateOrDestroy(BlockState pOldState, BlockState pNewState, IWorld pLevel, BlockPos pPos, int pFlags) {
      updateOrDestroy(pOldState, pNewState, pLevel, pPos, pFlags, 512);
   }

   public static void updateOrDestroy(BlockState pOldState, BlockState pNewState, IWorld pLevel, BlockPos pPos, int pFlags, int pRecursionLeft) {
      if (pNewState != pOldState) {
         if (pNewState.isAir()) {
            if (!pLevel.isClientSide()) {
               pLevel.destroyBlock(pPos, (pFlags & 32) == 0, (Entity)null, pRecursionLeft);
            }
         } else {
            pLevel.setBlock(pPos, pNewState, pFlags & -33, pRecursionLeft);
         }
      }

   }

   public Block(AbstractBlock.Properties p_i48440_1_) {
      super(p_i48440_1_);
      StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
      this.createBlockStateDefinition(builder);
      this.harvestLevel = p_i48440_1_.getHarvestLevel();
      this.harvestTool = p_i48440_1_.getHarvestTool();
      this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
      this.registerDefaultState(this.stateDefinition.any());
   }

   public static boolean isExceptionForConnection(Block p_220073_0_) {
      return p_220073_0_ instanceof LeavesBlock || p_220073_0_ == Blocks.BARRIER || p_220073_0_ == Blocks.CARVED_PUMPKIN || p_220073_0_ == Blocks.JACK_O_LANTERN || p_220073_0_ == Blocks.MELON || p_220073_0_ == Blocks.PUMPKIN || p_220073_0_.is(BlockTags.SHULKER_BOXES);
   }

   /**
    * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
    * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return this.isRandomlyTicking;
   }

   @OnlyIn(Dist.CLIENT)
   public static boolean shouldRenderFace(BlockState p_176225_0_, IBlockReader p_176225_1_, BlockPos p_176225_2_, Direction p_176225_3_) {
      BlockPos blockpos = p_176225_2_.relative(p_176225_3_);
      BlockState blockstate = p_176225_1_.getBlockState(blockpos);
      if (p_176225_0_.skipRendering(blockstate, p_176225_3_)) {
         return false;
      } else if (blockstate.canOcclude()) {
         Block.RenderSideCacheKey block$rendersidecachekey = new Block.RenderSideCacheKey(p_176225_0_, blockstate, p_176225_3_);
         Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey> object2bytelinkedopenhashmap = OCCLUSION_CACHE.get();
         byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$rendersidecachekey);
         if (b0 != 127) {
            return b0 != 0;
         } else {
            VoxelShape voxelshape = p_176225_0_.getFaceOcclusionShape(p_176225_1_, p_176225_2_, p_176225_3_);
            VoxelShape voxelshape1 = blockstate.getFaceOcclusionShape(p_176225_1_, blockpos, p_176225_3_.getOpposite());
            boolean flag = VoxelShapes.joinIsNotEmpty(voxelshape, voxelshape1, IBooleanFunction.ONLY_FIRST);
            if (object2bytelinkedopenhashmap.size() == 2048) {
               object2bytelinkedopenhashmap.removeLastByte();
            }

            object2bytelinkedopenhashmap.putAndMoveToFirst(block$rendersidecachekey, (byte)(flag ? 1 : 0));
            return flag;
         }
      } else {
         return true;
      }
   }

   /**
    * @return whether the given position has a rigid top face
    */
   public static boolean canSupportRigidBlock(IBlockReader pLevel, BlockPos pPos) {
      return pLevel.getBlockState(pPos).isFaceSturdy(pLevel, pPos, Direction.UP, BlockVoxelShape.RIGID);
   }

   /**
    * @return whether the given position has a solid center in the given direction
    */
   public static boolean canSupportCenter(IWorldReader pLevel, BlockPos pPos, Direction pDirection) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return pDirection == Direction.DOWN && blockstate.is(BlockTags.UNSTABLE_BOTTOM_CENTER) ? false : blockstate.isFaceSturdy(pLevel, pPos, pDirection, BlockVoxelShape.CENTER);
   }

   public static boolean isFaceFull(VoxelShape pShape, Direction pFace) {
      VoxelShape voxelshape = pShape.getFaceShape(pFace);
      return isShapeFullBlock(voxelshape);
   }

   /**
    * @return whether the provided {@link net.minecraft.world.phys.shapes.VoxelShape} is a full block (1x1x1)
    */
   public static boolean isShapeFullBlock(VoxelShape pShape) {
      return SHAPE_FULL_BLOCK_CACHE.getUnchecked(pShape);
   }

   public boolean propagatesSkylightDown(BlockState pState, IBlockReader pReader, BlockPos pPos) {
      return !isShapeFullBlock(pState.getShape(pReader, pPos)) && pState.getFluidState().isEmpty();
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
   }

   /**
    * Called after a player destroys this Block - the posiiton pos may no longer hold the state indicated.
    */
   public void destroy(IWorld pLevel, BlockPos pPos, BlockState pState) {
   }

   public static List<ItemStack> getDrops(BlockState pState, ServerWorld pLevel, BlockPos pPos, @Nullable TileEntity pBlockEntity) {
      LootContext.Builder lootcontext$builder = (new LootContext.Builder(pLevel)).withRandom(pLevel.random).withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(pPos)).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withOptionalParameter(LootParameters.BLOCK_ENTITY, pBlockEntity);
      return pState.getDrops(lootcontext$builder);
   }

   public static List<ItemStack> getDrops(BlockState pState, ServerWorld pLevel, BlockPos pPos, @Nullable TileEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool) {
      LootContext.Builder lootcontext$builder = (new LootContext.Builder(pLevel)).withRandom(pLevel.random).withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(pPos)).withParameter(LootParameters.TOOL, pTool).withOptionalParameter(LootParameters.THIS_ENTITY, pEntity).withOptionalParameter(LootParameters.BLOCK_ENTITY, pBlockEntity);
      return pState.getDrops(lootcontext$builder);
   }

   public static void dropResources(BlockState pState, World pLevel, BlockPos pPos) {
      if (pLevel instanceof ServerWorld) {
         getDrops(pState, (ServerWorld)pLevel, pPos, (TileEntity)null).forEach((p_220079_2_) -> {
            popResource(pLevel, pPos, p_220079_2_);
         });
         pState.spawnAfterBreak((ServerWorld)pLevel, pPos, ItemStack.EMPTY);
      }

   }

   public static void dropResources(BlockState pState, IWorld pLevel, BlockPos pPos, @Nullable TileEntity pBlockEntity) {
      if (pLevel instanceof ServerWorld) {
         getDrops(pState, (ServerWorld)pLevel, pPos, pBlockEntity).forEach((p_220061_2_) -> {
            popResource((ServerWorld)pLevel, pPos, p_220061_2_);
         });
         pState.spawnAfterBreak((ServerWorld)pLevel, pPos, ItemStack.EMPTY);
      }

   }

   public static void dropResources(BlockState pState, World pLevel, BlockPos pPos, @Nullable TileEntity pBlockEntity, Entity pEntity, ItemStack pTool) {
      if (pLevel instanceof ServerWorld) {
         getDrops(pState, (ServerWorld)pLevel, pPos, pBlockEntity, pEntity, pTool).forEach((p_220057_2_) -> {
            popResource(pLevel, pPos, p_220057_2_);
         });
         pState.spawnAfterBreak((ServerWorld)pLevel, pPos, pTool);
      }

   }

   /**
    * Spawns the given stack into the Level at the given position, respecting the doTileDrops gamerule
    */
   public static void popResource(World pLevel, BlockPos pPos, ItemStack pStack) {
      if (!pLevel.isClientSide && !pStack.isEmpty() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !pLevel.restoringBlockSnapshots) {
         float f = 0.5F;
         double d0 = (double)(pLevel.random.nextFloat() * 0.5F) + 0.25D;
         double d1 = (double)(pLevel.random.nextFloat() * 0.5F) + 0.25D;
         double d2 = (double)(pLevel.random.nextFloat() * 0.5F) + 0.25D;
         ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + d0, (double)pPos.getY() + d1, (double)pPos.getZ() + d2, pStack);
         itementity.setDefaultPickUpDelay();
         pLevel.addFreshEntity(itementity);
      }
   }

   /**
    * Spawns the given amount of experience into the Level as experience orb entities.
    */
   public void popExperience(ServerWorld pLevel, BlockPos pPos, int pAmount) {
      if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !pLevel.restoringBlockSnapshots) {
         while(pAmount > 0) {
            int i = ExperienceOrbEntity.getExperienceValue(pAmount);
            pAmount -= i;
            pLevel.addFreshEntity(new ExperienceOrbEntity(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, i));
         }
      }

   }

   /**
    * Returns how much this block can resist explosions from the passed in entity.
    */
   @Deprecated //Forge: Use more sensitive version
   public float getExplosionResistance() {
      return this.explosionResistance;
   }

   /**
    * Called when this Block is destroyed by an Explosion
    */
   public void wasExploded(World pLevel, BlockPos pPos, Explosion pExplosion) {
   }

   public void stepOn(World p_176199_1_, BlockPos p_176199_2_, Entity p_176199_3_) {
   }

   @Nullable
   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.defaultBlockState();
   }

   /**
    * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
    * Block.removedByPlayer
    */
   public void playerDestroy(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, @Nullable TileEntity pTe, ItemStack pStack) {
      pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
      pPlayer.causeFoodExhaustion(0.005F);
      dropResources(pState, pLevel, pPos, pTe, pPlayer, pStack);
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
   }

   /**
    * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
    */
   public boolean isPossibleToRespawnInThis() {
      return !this.material.isSolid() && !this.material.isLiquid();
   }

   @OnlyIn(Dist.CLIENT)
   public IFormattableTextComponent getName() {
      return new TranslationTextComponent(this.getDescriptionId());
   }

   /**
    * Returns the unlocalized name of the block with "tile." appended to the front.
    */
   public String getDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("block", Registry.BLOCK.getKey(this));
      }

      return this.descriptionId;
   }

   public void fallOn(World p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
      p_180658_3_.causeFallDamage(p_180658_4_, 1.0F);
   }

   /**
    * Called when an Entity lands on this Block. This method *must* update motionY because the entity will not do that
    * on its own
    */
   public void updateEntityAfterFallOn(IBlockReader pLevel, Entity pEntity) {
      pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
   }

   @Deprecated //Forge: Use more sensitive version
   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      return new ItemStack(this);
   }

   /**
    * Fill the given creative tab with the ItemStacks for this block.
    */
   public void fillItemCategory(ItemGroup pTab, NonNullList<ItemStack> pItems) {
      pItems.add(new ItemStack(this));
   }

   public float getFriction() {
      return this.friction;
   }

   public float getSpeedFactor() {
      return this.speedFactor;
   }

   public float getJumpFactor() {
      return this.jumpFactor;
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(World pLevel, BlockPos pPos, BlockState pState, PlayerEntity pPlayer) {
      pLevel.levelEvent(pPlayer, 2001, pPos, getId(pState));
      if (this.is(BlockTags.GUARDED_BY_PIGLINS)) {
         PiglinTasks.angerNearbyPiglins(pPlayer, false);
      }

   }

   public void handleRain(World p_176224_1_, BlockPos p_176224_2_) {
   }

   /**
    * @return whether this block should drop its drops when destroyed by the given explosion
    */
   @Deprecated //Forge: Use more sensitive version
   public boolean dropFromExplosion(Explosion pExplosion) {
      return true;
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
   }

   public StateContainer<Block, BlockState> getStateDefinition() {
      return this.stateDefinition;
   }

   protected final void registerDefaultState(BlockState pState) {
      this.defaultBlockState = pState;
   }

   /**
    * Gets the default state for this block
    */
   public final BlockState defaultBlockState() {
      return this.defaultBlockState;
   }

   @Deprecated //Forge: Use more sensitive version {@link IForgeBlockState#getSoundType(IWorldReader, BlockPos, Entity) }
   public SoundType getSoundType(BlockState pState) {
      return this.soundType;
   }

   public Item asItem() {
      if (this.item == null) {
         this.item = Item.byBlock(this);
      }

      return this.item.delegate.get(); //Forge: Vanilla caches the items, update with registry replacements.
   }

   public boolean hasDynamicShape() {
      return this.dynamicShape;
   }

   public String toString() {
      return "Block{" + getRegistryName() + "}";
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable IBlockReader pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
   }

   protected Block asBlock() {
      return this;
   }

   /* ======================================== FORGE START =====================================*/
   protected Random RANDOM = new Random();
   private net.minecraftforge.common.ToolType harvestTool;
   private int harvestLevel;
   private final net.minecraftforge.common.util.ReverseTagWrapper<Block> reverseTags = new net.minecraftforge.common.util.ReverseTagWrapper<>(this, BlockTags::getAllTags);

   @Nullable
   @Override
   public net.minecraftforge.common.ToolType getHarvestTool(BlockState state) {
      return harvestTool; //TODO: RE-Evaluate
   }

   @Override
   public int getHarvestLevel(BlockState state) {
     return harvestLevel; //TODO: RE-Evaluate
   }

   @Override
   public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, net.minecraftforge.common.IPlantable plantable) {
      BlockState plant = plantable.getPlant(world, pos.relative(facing));
      net.minecraftforge.common.PlantType type = plantable.getPlantType(world, pos.relative(facing));

      if (plant.getBlock() == Blocks.CACTUS)
         return state.is(Blocks.CACTUS) || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND);

      if (plant.getBlock() == Blocks.SUGAR_CANE && this == Blocks.SUGAR_CANE)
         return true;

      if (plantable instanceof BushBlock && ((BushBlock)plantable).mayPlaceOn(state, world, pos))
         return true;

      if (net.minecraftforge.common.PlantType.DESERT.equals(type)) {
         return this.getBlock() == Blocks.SAND || this.getBlock() == Blocks.TERRACOTTA || this.getBlock() instanceof GlazedTerracottaBlock;
      } else if (net.minecraftforge.common.PlantType.NETHER.equals(type)) {
         return this.getBlock() == Blocks.SOUL_SAND;
      } else if (net.minecraftforge.common.PlantType.CROP.equals(type)) {
         return state.is(Blocks.FARMLAND);
      } else if (net.minecraftforge.common.PlantType.CAVE.equals(type)) {
         return state.isFaceSturdy(world, pos, Direction.UP);
      } else if (net.minecraftforge.common.PlantType.PLAINS.equals(type)) {
         return this.getBlock() == Blocks.GRASS_BLOCK || net.minecraftforge.common.Tags.Blocks.DIRT.contains(this) || this.getBlock() == Blocks.FARMLAND;
      } else if (net.minecraftforge.common.PlantType.WATER.equals(type)) {
         return state.getMaterial() == net.minecraft.block.material.Material.WATER; //&& state.getValue(BlockLiquidWrapper)
      } else if (net.minecraftforge.common.PlantType.BEACH.equals(type)) {
         boolean isBeach = state.is(Blocks.GRASS_BLOCK) || net.minecraftforge.common.Tags.Blocks.DIRT.contains(this) || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND);
         boolean hasWater = false;
         for (Direction face : Direction.Plane.HORIZONTAL) {
             BlockState blockState = world.getBlockState(pos.relative(face));
             net.minecraft.fluid.FluidState fluidState = world.getFluidState(pos.relative(face));
             hasWater |= blockState.is(Blocks.FROSTED_ICE);
             hasWater |= fluidState.is(net.minecraft.tags.FluidTags.WATER);
             if (hasWater)
                break; //No point continuing.
         }
         return isBeach && hasWater;
      }
      return false;
  }

  @Override
  public final java.util.Set<net.minecraft.util.ResourceLocation> getTags() {
     return reverseTags.getTagNames();
  }

  static {
      net.minecraftforge.common.ForgeHooks.setBlockToolSetter((block, tool, level) -> {
            block.harvestTool = tool;
            block.harvestLevel = level;
      });
  }
   /* ========================================= FORGE END ======================================*/

   public static final class RenderSideCacheKey {
      private final BlockState first;
      private final BlockState second;
      private final Direction direction;

      public RenderSideCacheKey(BlockState pFirst, BlockState pSecond, Direction pDirection) {
         this.first = pFirst;
         this.second = pSecond;
         this.direction = pDirection;
      }

      public boolean equals(Object p_equals_1_) {
         if (this == p_equals_1_) {
            return true;
         } else if (!(p_equals_1_ instanceof Block.RenderSideCacheKey)) {
            return false;
         } else {
            Block.RenderSideCacheKey block$rendersidecachekey = (Block.RenderSideCacheKey)p_equals_1_;
            return this.first == block$rendersidecachekey.first && this.second == block$rendersidecachekey.second && this.direction == block$rendersidecachekey.direction;
         }
      }

      public int hashCode() {
         int i = this.first.hashCode();
         i = 31 * i + this.second.hashCode();
         return 31 * i + this.direction.hashCode();
      }
   }
}

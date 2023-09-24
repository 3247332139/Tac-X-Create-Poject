package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AnvilBlock extends FallingBlock {
   public static final DirectionProperty FACING = HorizontalBlock.FACING;
   private static final VoxelShape BASE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
   private static final VoxelShape X_LEG1 = Block.box(3.0D, 4.0D, 4.0D, 13.0D, 5.0D, 12.0D);
   private static final VoxelShape X_LEG2 = Block.box(4.0D, 5.0D, 6.0D, 12.0D, 10.0D, 10.0D);
   private static final VoxelShape X_TOP = Block.box(0.0D, 10.0D, 3.0D, 16.0D, 16.0D, 13.0D);
   private static final VoxelShape Z_LEG1 = Block.box(4.0D, 4.0D, 3.0D, 12.0D, 5.0D, 13.0D);
   private static final VoxelShape Z_LEG2 = Block.box(6.0D, 5.0D, 4.0D, 10.0D, 10.0D, 12.0D);
   private static final VoxelShape Z_TOP = Block.box(3.0D, 10.0D, 0.0D, 13.0D, 16.0D, 16.0D);
   private static final VoxelShape X_AXIS_AABB = VoxelShapes.or(BASE, X_LEG1, X_LEG2, X_TOP);
   private static final VoxelShape Z_AXIS_AABB = VoxelShapes.or(BASE, Z_LEG1, Z_LEG2, Z_TOP);
   private static final ITextComponent CONTAINER_TITLE = new TranslationTextComponent("container.repair");

   public AnvilBlock(AbstractBlock.Properties p_i48450_1_) {
      super(p_i48450_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getClockWise());
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (pLevel.isClientSide) {
         return ActionResultType.SUCCESS;
      } else {
         pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
         pPlayer.awardStat(Stats.INTERACT_WITH_ANVIL);
         return ActionResultType.CONSUME;
      }
   }

   @Nullable
   public INamedContainerProvider getMenuProvider(BlockState pState, World pLevel, BlockPos pPos) {
      return new SimpleNamedContainerProvider((p_220272_2_, p_220272_3_, p_220272_4_) -> {
         return new RepairContainer(p_220272_2_, p_220272_3_, IWorldPosCallable.create(pLevel, pPos));
      }, CONTAINER_TITLE);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      Direction direction = pState.getValue(FACING);
      return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
   }

   protected void falling(FallingBlockEntity pEntity) {
      pEntity.setHurtsEntities(true);
   }

   public void onLand(World p_176502_1_, BlockPos p_176502_2_, BlockState p_176502_3_, BlockState p_176502_4_, FallingBlockEntity p_176502_5_) {
      if (!p_176502_5_.isSilent()) {
         p_176502_1_.levelEvent(1031, p_176502_2_, 0);
      }

   }

   public void onBroken(World p_190974_1_, BlockPos p_190974_2_, FallingBlockEntity p_190974_3_) {
      if (!p_190974_3_.isSilent()) {
         p_190974_1_.levelEvent(1029, p_190974_2_, 0);
      }

   }

   @Nullable
   public static BlockState damage(BlockState pState) {
      if (pState.is(Blocks.ANVIL)) {
         return Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(FACING, pState.getValue(FACING));
      } else {
         return pState.is(Blocks.CHIPPED_ANVIL) ? Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(FACING, pState.getValue(FACING)) : null;
      }
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING);
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public int getDustColor(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return pState.getMapColor(pLevel, pPos).col;
   }
}
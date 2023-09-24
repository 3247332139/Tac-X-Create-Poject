package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ScaffoldingBlock extends Block implements IWaterLoggable {
   private static final VoxelShape STABLE_SHAPE;
   private static final VoxelShape UNSTABLE_SHAPE;
   private static final VoxelShape UNSTABLE_SHAPE_BOTTOM = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   private static final VoxelShape BELOW_BLOCK = VoxelShapes.block().move(0.0D, -1.0D, 0.0D);
   public static final IntegerProperty DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;

   public ScaffoldingBlock(AbstractBlock.Properties p_i49976_1_) {
      super(p_i49976_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, Integer.valueOf(7)).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(BOTTOM, Boolean.valueOf(false)));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(DISTANCE, WATERLOGGED, BOTTOM);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      if (!pContext.isHoldingItem(pState.getBlock().asItem())) {
         return pState.getValue(BOTTOM) ? UNSTABLE_SHAPE : STABLE_SHAPE;
      } else {
         return VoxelShapes.block();
      }
   }

   public VoxelShape getInteractionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return VoxelShapes.block();
   }

   public boolean canBeReplaced(BlockState pState, BlockItemUseContext pUseContext) {
      return pUseContext.getItemInHand().getItem() == this.asItem();
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      World world = pContext.getLevel();
      int i = getDistance(world, blockpos);
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(world.getFluidState(blockpos).getType() == Fluids.WATER)).setValue(DISTANCE, Integer.valueOf(i)).setValue(BOTTOM, Boolean.valueOf(this.isBottom(world, blockpos, i)));
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pLevel.isClientSide) {
         pLevel.getBlockTicks().scheduleTick(pPos, this, 1);
      }

   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      if (!pLevel.isClientSide()) {
         pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 1);
      }

      return pState;
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      int i = getDistance(pLevel, pPos);
      BlockState blockstate = pState.setValue(DISTANCE, Integer.valueOf(i)).setValue(BOTTOM, Boolean.valueOf(this.isBottom(pLevel, pPos, i)));
      if (blockstate.getValue(DISTANCE) == 7) {
         if (pState.getValue(DISTANCE) == 7) {
            pLevel.addFreshEntity(new FallingBlockEntity(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D, blockstate.setValue(WATERLOGGED, Boolean.valueOf(false))));
         } else {
            pLevel.destroyBlock(pPos, true);
         }
      } else if (pState != blockstate) {
         pLevel.setBlock(pPos, blockstate, 3);
      }

   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      return getDistance(pLevel, pPos) < 7;
   }

   public VoxelShape getCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      if (pContext.isAbove(VoxelShapes.block(), pPos, true) && !pContext.isDescending()) {
         return STABLE_SHAPE;
      } else {
         return pState.getValue(DISTANCE) != 0 && pState.getValue(BOTTOM) && pContext.isAbove(BELOW_BLOCK, pPos, true) ? UNSTABLE_SHAPE_BOTTOM : VoxelShapes.empty();
      }
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   private boolean isBottom(IBlockReader pLevel, BlockPos pPos, int pDistance) {
      return pDistance > 0 && !pLevel.getBlockState(pPos.below()).is(this);
   }

   public static int getDistance(IBlockReader pLevel, BlockPos pPos) {
      BlockPos.Mutable blockpos$mutable = pPos.mutable().move(Direction.DOWN);
      BlockState blockstate = pLevel.getBlockState(blockpos$mutable);
      int i = 7;
      if (blockstate.is(Blocks.SCAFFOLDING)) {
         i = blockstate.getValue(DISTANCE);
      } else if (blockstate.isFaceSturdy(pLevel, blockpos$mutable, Direction.UP)) {
         return 0;
      }

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockState blockstate1 = pLevel.getBlockState(blockpos$mutable.setWithOffset(pPos, direction));
         if (blockstate1.is(Blocks.SCAFFOLDING)) {
            i = Math.min(i, blockstate1.getValue(DISTANCE) + 1);
            if (i == 1) {
               break;
            }
         }
      }

      return i;
   }

   static {
      VoxelShape voxelshape = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
      VoxelShape voxelshape1 = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 2.0D);
      VoxelShape voxelshape2 = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
      VoxelShape voxelshape3 = Block.box(0.0D, 0.0D, 14.0D, 2.0D, 16.0D, 16.0D);
      VoxelShape voxelshape4 = Block.box(14.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
      STABLE_SHAPE = VoxelShapes.or(voxelshape, voxelshape1, voxelshape2, voxelshape3, voxelshape4);
      VoxelShape voxelshape5 = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 2.0D, 16.0D);
      VoxelShape voxelshape6 = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
      VoxelShape voxelshape7 = Block.box(0.0D, 0.0D, 14.0D, 16.0D, 2.0D, 16.0D);
      VoxelShape voxelshape8 = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 2.0D);
      UNSTABLE_SHAPE = VoxelShapes.or(ScaffoldingBlock.UNSTABLE_SHAPE_BOTTOM, STABLE_SHAPE, voxelshape6, voxelshape5, voxelshape8, voxelshape7);
   }
}
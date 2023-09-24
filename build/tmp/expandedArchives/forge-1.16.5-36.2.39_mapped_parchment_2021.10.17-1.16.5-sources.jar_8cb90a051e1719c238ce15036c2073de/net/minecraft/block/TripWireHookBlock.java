package net.minecraft.block;

import com.google.common.base.MoreObjects;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TripWireHookBlock extends Block {
   public static final DirectionProperty FACING = HorizontalBlock.FACING;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
   protected static final VoxelShape NORTH_AABB = Block.box(5.0D, 0.0D, 10.0D, 11.0D, 10.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(5.0D, 0.0D, 0.0D, 11.0D, 10.0D, 6.0D);
   protected static final VoxelShape WEST_AABB = Block.box(10.0D, 0.0D, 5.0D, 16.0D, 10.0D, 11.0D);
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 5.0D, 6.0D, 10.0D, 11.0D);

   public TripWireHookBlock(AbstractBlock.Properties p_i48304_1_) {
      super(p_i48304_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false)));
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      switch((Direction)pState.getValue(FACING)) {
      case EAST:
      default:
         return EAST_AABB;
      case WEST:
         return WEST_AABB;
      case SOUTH:
         return SOUTH_AABB;
      case NORTH:
         return NORTH_AABB;
      }
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      Direction direction = pState.getValue(FACING);
      BlockPos blockpos = pPos.relative(direction.getOpposite());
      BlockState blockstate = pLevel.getBlockState(blockpos);
      return direction.getAxis().isHorizontal() && blockstate.isFaceSturdy(pLevel, blockpos, direction);
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing.getOpposite() == pState.getValue(FACING) && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      BlockState blockstate = this.defaultBlockState().setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false));
      IWorldReader iworldreader = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      Direction[] adirection = pContext.getNearestLookingDirections();

      for(Direction direction : adirection) {
         if (direction.getAxis().isHorizontal()) {
            Direction direction1 = direction.getOpposite();
            blockstate = blockstate.setValue(FACING, direction1);
            if (blockstate.canSurvive(iworldreader, blockpos)) {
               return blockstate;
            }
         }
      }

      return null;
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      this.calculateState(pLevel, pPos, pState, false, false, -1, (BlockState)null);
   }

   public void calculateState(World pLevel, BlockPos pPos, BlockState pHookState, boolean pAttaching, boolean pShouldNotifyNeighbours, int pSearchRange, @Nullable BlockState pState) {
      Direction direction = pHookState.getValue(FACING);
      boolean flag = pHookState.getValue(ATTACHED);
      boolean flag1 = pHookState.getValue(POWERED);
      boolean flag2 = !pAttaching;
      boolean flag3 = false;
      int i = 0;
      BlockState[] ablockstate = new BlockState[42];

      for(int j = 1; j < 42; ++j) {
         BlockPos blockpos = pPos.relative(direction, j);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (blockstate.is(Blocks.TRIPWIRE_HOOK)) {
            if (blockstate.getValue(FACING) == direction.getOpposite()) {
               i = j;
            }
            break;
         }

         if (!blockstate.is(Blocks.TRIPWIRE) && j != pSearchRange) {
            ablockstate[j] = null;
            flag2 = false;
         } else {
            if (j == pSearchRange) {
               blockstate = MoreObjects.firstNonNull(pState, blockstate);
            }

            boolean flag4 = !blockstate.getValue(TripWireBlock.DISARMED);
            boolean flag5 = blockstate.getValue(TripWireBlock.POWERED);
            flag3 |= flag4 && flag5;
            ablockstate[j] = blockstate;
            if (j == pSearchRange) {
               pLevel.getBlockTicks().scheduleTick(pPos, this, 10);
               flag2 &= flag4;
            }
         }
      }

      flag2 = flag2 & i > 1;
      flag3 = flag3 & flag2;
      BlockState blockstate1 = this.defaultBlockState().setValue(ATTACHED, Boolean.valueOf(flag2)).setValue(POWERED, Boolean.valueOf(flag3));
      if (i > 0) {
         BlockPos blockpos1 = pPos.relative(direction, i);
         Direction direction1 = direction.getOpposite();
         pLevel.setBlock(blockpos1, blockstate1.setValue(FACING, direction1), 3);
         this.notifyNeighbors(pLevel, blockpos1, direction1);
         this.playSound(pLevel, blockpos1, flag2, flag3, flag, flag1);
      }

      this.playSound(pLevel, pPos, flag2, flag3, flag, flag1);
      if (!pAttaching) {
         pLevel.setBlock(pPos, blockstate1.setValue(FACING, direction), 3);
         if (pShouldNotifyNeighbours) {
            this.notifyNeighbors(pLevel, pPos, direction);
         }
      }

      if (flag != flag2) {
         for(int k = 1; k < i; ++k) {
            BlockPos blockpos2 = pPos.relative(direction, k);
            BlockState blockstate2 = ablockstate[k];
            if (blockstate2 != null) {
               if (!pLevel.getBlockState(blockpos2).isAir()) { // FORGE: fix MC-129055
               pLevel.setBlock(blockpos2, blockstate2.setValue(ATTACHED, Boolean.valueOf(flag2)), 3);
               }
            }
         }
      }

   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      this.calculateState(pLevel, pPos, pState, false, true, -1, (BlockState)null);
   }

   private void playSound(World pLevel, BlockPos pPos, boolean pAttaching, boolean pActivated, boolean pDetaching, boolean pDeactivating) {
      if (pActivated && !pDeactivating) {
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.TRIPWIRE_CLICK_ON, SoundCategory.BLOCKS, 0.4F, 0.6F);
      } else if (!pActivated && pDeactivating) {
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.TRIPWIRE_CLICK_OFF, SoundCategory.BLOCKS, 0.4F, 0.5F);
      } else if (pAttaching && !pDetaching) {
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.TRIPWIRE_ATTACH, SoundCategory.BLOCKS, 0.4F, 0.7F);
      } else if (!pAttaching && pDetaching) {
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.TRIPWIRE_DETACH, SoundCategory.BLOCKS, 0.4F, 1.2F / (pLevel.random.nextFloat() * 0.2F + 0.9F));
      }

   }

   private void notifyNeighbors(World pLevel, BlockPos pPos, Direction pDirection) {
      pLevel.updateNeighborsAt(pPos, this);
      pLevel.updateNeighborsAt(pPos.relative(pDirection.getOpposite()), this);
   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving && !pState.is(pNewState.getBlock())) {
         boolean flag = pState.getValue(ATTACHED);
         boolean flag1 = pState.getValue(POWERED);
         if (flag || flag1) {
            this.calculateState(pLevel, pPos, pState, true, false, -1, (BlockState)null);
         }

         if (flag1) {
            pLevel.updateNeighborsAt(pPos, this);
            pLevel.updateNeighborsAt(pPos.relative(pState.getValue(FACING).getOpposite()), this);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   /**
    * @deprecated call via {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return pBlockState.getValue(POWERED) ? 15 : 0;
   }

   /**
    * @deprecated call via {@link IBlockState#getStrongPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getDirectSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      if (!pBlockState.getValue(POWERED)) {
         return 0;
      } else {
         return pBlockState.getValue(FACING) == pSide ? 15 : 0;
      }
   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
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

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, POWERED, ATTACHED);
   }
}

package net.minecraft.block;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.PistonType;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class MovingPistonBlock extends ContainerBlock {
   public static final DirectionProperty FACING = PistonHeadBlock.FACING;
   public static final EnumProperty<PistonType> TYPE = PistonHeadBlock.TYPE;

   public MovingPistonBlock(AbstractBlock.Properties p_i48282_1_) {
      super(p_i48282_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, PistonType.DEFAULT));
   }

   @Nullable
   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return null;
   }

   public static TileEntity newMovingBlockEntity(BlockState p_196343_0_, Direction p_196343_1_, boolean p_196343_2_, boolean p_196343_3_) {
      return new PistonTileEntity(p_196343_0_, p_196343_1_, p_196343_2_, p_196343_3_);
   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof PistonTileEntity) {
            ((PistonTileEntity)tileentity).finalTick();
         }

      }
   }

   /**
    * Called after a player destroys this Block - the posiiton pos may no longer hold the state indicated.
    */
   public void destroy(IWorld pLevel, BlockPos pPos, BlockState pState) {
      BlockPos blockpos = pPos.relative(pState.getValue(FACING).getOpposite());
      BlockState blockstate = pLevel.getBlockState(blockpos);
      if (blockstate.getBlock() instanceof PistonBlock && blockstate.getValue(PistonBlock.EXTENDED)) {
         pLevel.removeBlock(blockpos, false);
      }

   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) == null) {
         pLevel.removeBlock(pPos, false);
         return ActionResultType.CONSUME;
      } else {
         return ActionResultType.PASS;
      }
   }

   public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
      PistonTileEntity pistontileentity = this.getBlockEntity(pBuilder.getLevel(), new BlockPos(pBuilder.getParameter(LootParameters.ORIGIN)));
      return pistontileentity == null ? Collections.emptyList() : pistontileentity.getMovedState().getDrops(pBuilder);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return VoxelShapes.empty();
   }

   public VoxelShape getCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      PistonTileEntity pistontileentity = this.getBlockEntity(pLevel, pPos);
      return pistontileentity != null ? pistontileentity.getCollisionShape(pLevel, pPos) : VoxelShapes.empty();
   }

   @Nullable
   private PistonTileEntity getBlockEntity(IBlockReader pBlockReader, BlockPos pPos) {
      TileEntity tileentity = pBlockReader.getBlockEntity(pPos);
      return tileentity instanceof PistonTileEntity ? (PistonTileEntity)tileentity : null;
   }

   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      return ItemStack.EMPTY;
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
      pBuilder.add(FACING, TYPE);
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}
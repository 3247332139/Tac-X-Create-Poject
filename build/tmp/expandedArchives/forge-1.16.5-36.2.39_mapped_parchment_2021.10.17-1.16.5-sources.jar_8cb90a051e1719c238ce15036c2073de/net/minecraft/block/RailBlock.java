package net.minecraft.block;

import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RailBlock extends AbstractRailBlock {
   public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

   public RailBlock(AbstractBlock.Properties p_i48346_1_) {
      super(false, p_i48346_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH));
   }

   protected void updateState(BlockState pState, World pLevel, BlockPos pPos, Block pBlock) {
      if (pBlock.defaultBlockState().isSignalSource() && (new RailState(pLevel, pPos, pState)).countPotentialConnections() == 3) {
         this.updateDir(pLevel, pPos, pState, false);
      }

   }

   public Property<RailShape> getShapeProperty() {
      return SHAPE;
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      switch(pRotation) {
      case CLOCKWISE_180:
         switch((RailShape)pState.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
         case ASCENDING_WEST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
         case ASCENDING_NORTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_SOUTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
         case SOUTH_EAST:
            return pState.setValue(SHAPE, RailShape.NORTH_WEST);
         case SOUTH_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_EAST);
         case NORTH_WEST:
            return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_EAST:
            return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
         case NORTH_SOUTH: //Forge fix: MC-196102
         case EAST_WEST:
            return pState;
         }
      case COUNTERCLOCKWISE_90:
         switch((RailShape)pState.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
         case ASCENDING_WEST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_NORTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
         case ASCENDING_SOUTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
         case SOUTH_EAST:
            return pState.setValue(SHAPE, RailShape.NORTH_EAST);
         case SOUTH_WEST:
            return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_WEST:
            return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
         case NORTH_EAST:
            return pState.setValue(SHAPE, RailShape.NORTH_WEST);
         case NORTH_SOUTH:
            return pState.setValue(SHAPE, RailShape.EAST_WEST);
         case EAST_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_SOUTH);
         }
      case CLOCKWISE_90:
         switch((RailShape)pState.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_WEST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
         case ASCENDING_NORTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
         case ASCENDING_SOUTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
         case SOUTH_EAST:
            return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
         case SOUTH_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_WEST);
         case NORTH_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_EAST);
         case NORTH_EAST:
            return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_SOUTH:
            return pState.setValue(SHAPE, RailShape.EAST_WEST);
         case EAST_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_SOUTH);
         }
      default:
         return pState;
      }
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      RailShape railshape = pState.getValue(SHAPE);
      switch(pMirror) {
      case LEFT_RIGHT:
         switch(railshape) {
         case ASCENDING_NORTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_SOUTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
         case SOUTH_EAST:
            return pState.setValue(SHAPE, RailShape.NORTH_EAST);
         case SOUTH_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_WEST);
         case NORTH_WEST:
            return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
         case NORTH_EAST:
            return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
         default:
            return super.mirror(pState, pMirror);
         }
      case FRONT_BACK:
         switch(railshape) {
         case ASCENDING_EAST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
         case ASCENDING_WEST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
         case ASCENDING_NORTH:
         case ASCENDING_SOUTH:
         default:
            break;
         case SOUTH_EAST:
            return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
         case SOUTH_WEST:
            return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_EAST);
         case NORTH_EAST:
            return pState.setValue(SHAPE, RailShape.NORTH_WEST);
         }
      }

      return super.mirror(pState, pMirror);
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(SHAPE);
   }
}

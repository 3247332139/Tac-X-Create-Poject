package net.minecraft.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractSignBlock extends ContainerBlock implements IWaterLoggable {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
   private final WoodType type;

   protected AbstractSignBlock(AbstractBlock.Properties pProperties, WoodType pType) {
      super(pProperties);
      this.type = pType;
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

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   /**
    * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
    */
   public boolean isPossibleToRespawnInThis() {
      return true;
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new SignTileEntity();
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      boolean flag = itemstack.getItem() instanceof DyeItem && pPlayer.abilities.mayBuild;
      if (pLevel.isClientSide) {
         return flag ? ActionResultType.SUCCESS : ActionResultType.CONSUME;
      } else {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof SignTileEntity) {
            SignTileEntity signtileentity = (SignTileEntity)tileentity;
            if (flag) {
               boolean flag1 = signtileentity.setColor(((DyeItem)itemstack.getItem()).getDyeColor());
               if (flag1 && !pPlayer.isCreative()) {
                  itemstack.shrink(1);
               }
            }

            return signtileentity.executeClickCommands(pPlayer) ? ActionResultType.SUCCESS : ActionResultType.PASS;
         } else {
            return ActionResultType.PASS;
         }
      }
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   @OnlyIn(Dist.CLIENT)
   public WoodType type() {
      return this.type;
   }
}
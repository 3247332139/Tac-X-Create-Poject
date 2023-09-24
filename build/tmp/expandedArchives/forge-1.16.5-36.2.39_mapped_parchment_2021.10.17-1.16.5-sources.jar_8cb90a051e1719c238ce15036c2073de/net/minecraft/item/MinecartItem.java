package net.minecraft.item;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MinecartItem extends Item {
   private static final IDispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
      private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

      /**
       * Dispense the specified stack, play the dispense sound and spawn particles.
       */
      public ItemStack execute(IBlockSource pSource, ItemStack pStack) {
         Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
         World world = pSource.getLevel();
         double d0 = pSource.x() + (double)direction.getStepX() * 1.125D;
         double d1 = Math.floor(pSource.y()) + (double)direction.getStepY();
         double d2 = pSource.z() + (double)direction.getStepZ() * 1.125D;
         BlockPos blockpos = pSource.getPos().relative(direction);
         BlockState blockstate = world.getBlockState(blockpos);
         RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock ? ((AbstractRailBlock)blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null) : RailShape.NORTH_SOUTH;
         double d3;
         if (blockstate.is(BlockTags.RAILS)) {
            if (railshape.isAscending()) {
               d3 = 0.6D;
            } else {
               d3 = 0.1D;
            }
         } else {
            if (!blockstate.isAir() || !world.getBlockState(blockpos.below()).is(BlockTags.RAILS)) {
               return this.defaultDispenseItemBehavior.dispense(pSource, pStack);
            }

            BlockState blockstate1 = world.getBlockState(blockpos.below());
            RailShape railshape1 = blockstate1.getBlock() instanceof AbstractRailBlock ? blockstate1.getValue(((AbstractRailBlock)blockstate1.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            if (direction != Direction.DOWN && railshape1.isAscending()) {
               d3 = -0.4D;
            } else {
               d3 = -0.9D;
            }
         }

         AbstractMinecartEntity abstractminecartentity = AbstractMinecartEntity.createMinecart(world, d0, d1 + d3, d2, ((MinecartItem)pStack.getItem()).type);
         if (pStack.hasCustomHoverName()) {
            abstractminecartentity.setCustomName(pStack.getHoverName());
         }

         world.addFreshEntity(abstractminecartentity);
         pStack.shrink(1);
         return pStack;
      }

      /**
       * Play the dispense sound from the specified block.
       */
      protected void playSound(IBlockSource pSource) {
         pSource.getLevel().levelEvent(1000, pSource.getPos(), 0);
      }
   };
   private final AbstractMinecartEntity.Type type;

   public MinecartItem(AbstractMinecartEntity.Type pType, Item.Properties pProperties) {
      super(pProperties);
      this.type = pType;
      DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = world.getBlockState(blockpos);
      if (!blockstate.is(BlockTags.RAILS)) {
         return ActionResultType.FAIL;
      } else {
         ItemStack itemstack = pContext.getItemInHand();
         if (!world.isClientSide) {
            RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock ? ((AbstractRailBlock)blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null) : RailShape.NORTH_SOUTH;
            double d0 = 0.0D;
            if (railshape.isAscending()) {
               d0 = 0.5D;
            }

            AbstractMinecartEntity abstractminecartentity = AbstractMinecartEntity.createMinecart(world, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.0625D + d0, (double)blockpos.getZ() + 0.5D, this.type);
            if (itemstack.hasCustomHoverName()) {
               abstractminecartentity.setCustomName(itemstack.getHoverName());
            }

            world.addFreshEntity(abstractminecartentity);
         }

         itemstack.shrink(1);
         return ActionResultType.sidedSuccess(world.isClientSide);
      }
   }
}

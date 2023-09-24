package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WritableBookItem extends Item {
   public WritableBookItem(Item.Properties p_i48455_1_) {
      super(p_i48455_1_);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = world.getBlockState(blockpos);
      if (blockstate.is(Blocks.LECTERN)) {
         return LecternBlock.tryPlaceBook(world, blockpos, blockstate, pContext.getItemInHand()) ? ActionResultType.sidedSuccess(world.isClientSide) : ActionResultType.PASS;
      } else {
         return ActionResultType.PASS;
      }
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      pPlayer.openItemGui(itemstack, pHand);
      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      return ActionResult.sidedSuccess(itemstack, pLevel.isClientSide());
   }

   /**
    * this method returns true if the book's NBT Tag List "pages" is valid
    */
   public static boolean makeSureTagIsValid(@Nullable CompoundNBT pCompoundTag) {
      if (pCompoundTag == null) {
         return false;
      } else if (!pCompoundTag.contains("pages", 9)) {
         return false;
      } else {
         ListNBT listnbt = pCompoundTag.getList("pages", 8);

         for(int i = 0; i < listnbt.size(); ++i) {
            String s = listnbt.getString(i);
            if (s.length() > 32767) {
               return false;
            }
         }

         return true;
      }
   }
}
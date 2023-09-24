package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ThrowablePotionItem extends PotionItem {
   public ThrowablePotionItem(Item.Properties p_i225739_1_) {
      super(p_i225739_1_);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!pLevel.isClientSide) {
         PotionEntity potionentity = new PotionEntity(pLevel, pPlayer);
         potionentity.setItem(itemstack);
         potionentity.shootFromRotation(pPlayer, pPlayer.xRot, pPlayer.yRot, -20.0F, 0.5F, 1.0F);
         pLevel.addFreshEntity(potionentity);
      }

      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      if (!pPlayer.abilities.instabuild) {
         itemstack.shrink(1);
      }

      return ActionResult.sidedSuccess(itemstack, pLevel.isClientSide());
   }
}
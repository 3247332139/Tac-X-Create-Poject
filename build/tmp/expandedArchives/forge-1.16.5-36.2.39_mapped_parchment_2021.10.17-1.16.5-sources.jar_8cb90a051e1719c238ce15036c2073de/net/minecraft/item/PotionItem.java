package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionItem extends Item {
   public PotionItem(Item.Properties p_i48476_1_) {
      super(p_i48476_1_);
   }

   public ItemStack getDefaultInstance() {
      return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, World pLevel, LivingEntity pEntityLiving) {
      PlayerEntity playerentity = pEntityLiving instanceof PlayerEntity ? (PlayerEntity)pEntityLiving : null;
      if (playerentity instanceof ServerPlayerEntity) {
         CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayerEntity)playerentity, pStack);
      }

      if (!pLevel.isClientSide) {
         for(EffectInstance effectinstance : PotionUtils.getMobEffects(pStack)) {
            if (effectinstance.getEffect().isInstantenous()) {
               effectinstance.getEffect().applyInstantenousEffect(playerentity, playerentity, pEntityLiving, effectinstance.getAmplifier(), 1.0D);
            } else {
               pEntityLiving.addEffect(new EffectInstance(effectinstance));
            }
         }
      }

      if (playerentity != null) {
         playerentity.awardStat(Stats.ITEM_USED.get(this));
         if (!playerentity.abilities.instabuild) {
            pStack.shrink(1);
         }
      }

      if (playerentity == null || !playerentity.abilities.instabuild) {
         if (pStack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
         }

         if (playerentity != null) {
            playerentity.inventory.add(new ItemStack(Items.GLASS_BOTTLE));
         }
      }

      return pStack;
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 32;
   }

   /**
    * returns the action that specifies what animation to play when the items is being used
    */
   public UseAction getUseAnimation(ItemStack pStack) {
      return UseAction.DRINK;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      return DrinkHelper.useDrink(pLevel, pPlayer, pHand);
   }

   /**
    * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
    * different names based on their damage or NBT.
    */
   public String getDescriptionId(ItemStack pStack) {
      return PotionUtils.getPotion(pStack).getName(this.getDescriptionId() + ".effect.");
   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
      PotionUtils.addPotionTooltip(pStack, pTooltip, 1.0F);
   }

   /**
    * Returns true if this item has an enchantment glint. By default, this returns <code>stack.isItemEnchanted()</code>,
    * but other items can override it (for instance, written books always return true).
    * 
    * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
    * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
    */
   public boolean isFoil(ItemStack pStack) {
      return super.isFoil(pStack) || !PotionUtils.getMobEffects(pStack).isEmpty();
   }

   /**
    * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
    */
   public void fillItemCategory(ItemGroup pGroup, NonNullList<ItemStack> pItems) {
      if (this.allowdedIn(pGroup)) {
         for(Potion potion : Registry.POTION) {
            if (potion != Potions.EMPTY) {
               pItems.add(PotionUtils.setPotion(new ItemStack(this), potion));
            }
         }
      }

   }
}
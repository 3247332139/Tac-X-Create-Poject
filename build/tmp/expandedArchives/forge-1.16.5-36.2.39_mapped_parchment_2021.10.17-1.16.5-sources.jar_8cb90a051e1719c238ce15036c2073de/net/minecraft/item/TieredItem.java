package net.minecraft.item;

public class TieredItem extends Item {
   private final IItemTier tier;

   public TieredItem(IItemTier pTier, Item.Properties pProperties) {
      super(pProperties.defaultDurability(pTier.getUses()));
      this.tier = pTier;
   }

   public IItemTier getTier() {
      return this.tier;
   }

   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   public int getEnchantmentValue() {
      return this.tier.getEnchantmentValue();
   }

   /**
    * Return whether this item is repairable in an anvil.
    */
   public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
      return this.tier.getRepairIngredient().test(pRepair) || super.isValidRepairItem(pToRepair, pRepair);
   }
}
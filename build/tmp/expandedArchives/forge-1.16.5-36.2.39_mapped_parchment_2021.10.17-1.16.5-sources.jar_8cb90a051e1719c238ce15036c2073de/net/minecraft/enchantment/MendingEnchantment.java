package net.minecraft.enchantment;

import net.minecraft.inventory.EquipmentSlotType;

public class MendingEnchantment extends Enchantment {
   public MendingEnchantment(Enchantment.Rarity pRarity, EquipmentSlotType... pApplicableSlots) {
      super(pRarity, EnchantmentType.BREAKABLE, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return pEnchantmentLevel * 25;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 50;
   }

   /**
    * Checks if the enchantment should be considered a treasure enchantment. These enchantments can not be obtained
    * using the enchantment table. The mending enchantment is an example of a treasure enchantment.
    * @return Whether or not the enchantment is a treasure enchantment.
    */
   public boolean isTreasureOnly() {
      return true;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 1;
   }
}
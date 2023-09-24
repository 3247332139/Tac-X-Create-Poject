package net.minecraft.enchantment;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Enchantment extends net.minecraftforge.registries.ForgeRegistryEntry<Enchantment> {
   private final net.minecraftforge.common.util.ReverseTagWrapper<Enchantment> reverseTags = new net.minecraftforge.common.util.ReverseTagWrapper<>(this, () -> net.minecraft.tags.TagCollectionManager.getInstance().getCustomTypeCollection(net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS));
   /** The valid equipment slots for the enchantment. */
   private final EquipmentSlotType[] slots;
   /** The rarity of the enchantment. */
   private final Enchantment.Rarity rarity;
   /**
    * The category of the enchantment. This often controls which items the enchantment can be applied to and which
    * creative tabs the enchantment will be displayed under.
    */
   public final EnchantmentType category;
   /** The descriptionId for the enchantment. Commonly used to create localization keys. */
   @Nullable
   protected String descriptionId;

   /**
    * Gets an enchantment from the registry using it's internal numeric Id.
    * @param pId The internal numeric id of the enchantment.
    */
   @Nullable
   @OnlyIn(Dist.CLIENT)
   public static Enchantment byId(int pId) {
      return Registry.ENCHANTMENT.byId(pId);
   }

   protected Enchantment(Enchantment.Rarity pRarity, EnchantmentType pCategory, EquipmentSlotType[] pApplicableSlots) {
      this.rarity = pRarity;
      this.category = pCategory;
      this.slots = pApplicableSlots;
   }

   public java.util.Set<net.minecraft.util.ResourceLocation> getTags() {
      return reverseTags.getTagNames();
   }

   public boolean isIn(net.minecraft.tags.ITag<Enchantment> tag) {
      return tag.contains(this);
   }

   /**
    * Creates a new map containing all items equipped by an entity in {@linkplain #slots slots that the enchantment
    * cares about}. These items are not tested for having the enchantment.
    * @param pEntity The entity to collect equipment for.
    */
   public Map<EquipmentSlotType, ItemStack> getSlotItems(LivingEntity pEntity) {
      Map<EquipmentSlotType, ItemStack> map = Maps.newEnumMap(EquipmentSlotType.class);

      for(EquipmentSlotType equipmentslottype : this.slots) {
         ItemStack itemstack = pEntity.getItemBySlot(equipmentslottype);
         if (!itemstack.isEmpty()) {
            map.put(equipmentslottype, itemstack);
         }
      }

      return map;
   }

   /**
    * Gets the rarity of the enchantment. This determines how rare the enchantment will be in enchantment pools such as
    * the enchanting table and loot tables.
    */
   public Enchantment.Rarity getRarity() {
      return this.rarity;
   }

   /**
    * Gets the minimum level of the enchantment under normal circumstances such as the enchanting table. This limit is
    * not strictly enforced and may be ignored through custom item NBT or other customizations.
    */
   public int getMinLevel() {
      return 1;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 1;
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 1 + pEnchantmentLevel * 10;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 5;
   }

   /**
    * Calculates the damage protection of the enchantment based on level and damage source passed.
    * @param pLevel The level of the enchantment being used.
    * @param pSource The source of the damage.
    */
   public int getDamageProtection(int pLevel, DamageSource pSource) {
      return 0;
   }

   /**
    * Calculates the additional damage that will be dealt by an item with this enchantment. This alternative to
    * calcModifierDamage is sensitive to the targets EnumCreatureAttribute.
    * @param pLevel The level of the enchantment being used.
    */
   public float getDamageBonus(int pLevel, CreatureAttribute pCreatureType) {
      return 0.0F;
   }

   /**
    * Checks if the enchantment is compatible with another enchantment.
    * @return Whether or not both enchantments agree that they are compatible with eachother.
    * @param pOther The other enchantment to test compatibility with.
    */
   public final boolean isCompatibleWith(Enchantment pOther) {
      return this.checkCompatibility(pOther) && pOther.checkCompatibility(this);
   }

   /**
    * Determines if the enchantment passed can be applyied together with this enchantment.
    * @param pEnch The other enchantment to test compatibility with.
    */
   protected boolean checkCompatibility(Enchantment pEnch) {
      return this != pEnch;
   }

   /**
    * Lazily initializes a descriptionId for an enchantment using the enchantments registry name.
    */
   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("enchantment", Registry.ENCHANTMENT.getKey(this));
      }

      return this.descriptionId;
   }

   /**
    * Gets the description Id of the enchantment. This is commonly used to create localization keys.
    */
   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   /**
    * Gets the name of the enchantment with the level appended to the end. The game will attempt to convert the letter
    * to roman numerals.
    * @return The name of the enchantment with the level appened to the end.
    * @param pLevel The level of the enchantment.
    */
   public ITextComponent getFullname(int pLevel) {
      IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent(this.getDescriptionId());
      if (this.isCurse()) {
         iformattabletextcomponent.withStyle(TextFormatting.RED);
      } else {
         iformattabletextcomponent.withStyle(TextFormatting.GRAY);
      }

      if (pLevel != 1 || this.getMaxLevel() != 1) {
         iformattabletextcomponent.append(" ").append(new TranslationTextComponent("enchantment.level." + pLevel));
      }

      return iformattabletextcomponent;
   }

   /**
    * Determines if this enchantment can be applied to a specific ItemStack.
    * @param pStack The ItemStack to test.
    */
   public boolean canEnchant(ItemStack pStack) {
      return canApplyAtEnchantingTable(pStack);
   }

   /**
    * Called whenever a mob is damaged with an item that has this enchantment on it.
    * @param pUser The user of the enchantment.
    * @param pTarget The entity being attacked.
    * @param pLevel The level of the enchantment.
    */
   public void doPostAttack(LivingEntity pUser, Entity pTarget, int pLevel) {
   }

   /**
    * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be called.
    * @param pUser The user of the enchantment.
    * @param pAttacker The entity that attacked the user.
    * @param pLevel The level of the enchantment.
    */
   public void doPostHurt(LivingEntity pUser, Entity pAttacker, int pLevel) {
   }

   /**
    * Checks if the enchantment should be considered a treasure enchantment. These enchantments can not be obtained
    * using the enchantment table. The mending enchantment is an example of a treasure enchantment.
    * @return Whether or not the enchantment is a treasure enchantment.
    */
   public boolean isTreasureOnly() {
      return false;
   }

   /**
    * Checks if the enchantment is considered a curse. These enchantments are treated as debuffs and can not be removed
    * from items under normal circumstances.
    * @return Whether or not the enchantment is a curse.
    */
   public boolean isCurse() {
      return false;
   }

   /**
    * Checks if the enchantment can be sold by villagers in their trades.
    */
   public boolean isTradeable() {
      return true;
   }

   /**
    * Checks if the enchantment can be applied to loot table drops.
    */
   public boolean isDiscoverable() {
      return true;
   }

   /**
    * This applies specifically to applying at the enchanting table. The other method {@link #canApply(ItemStack)}
    * applies for <i>all possible</i> enchantments.
    * @param stack
    * @return
    */
   public boolean canApplyAtEnchantingTable(ItemStack stack) {
      return stack.canApplyAtEnchantingTable(this);
   }

   /**
    * Is this enchantment allowed to be enchanted on books via Enchantment Table
    * @return false to disable the vanilla feature
    */
   public boolean isAllowedOnBooks() {
      return true;
   }

   public static enum Rarity {
      COMMON(10),
      UNCOMMON(5),
      RARE(2),
      VERY_RARE(1);

      private final int weight;

      private Rarity(int pWeight) {
         this.weight = pWeight;
      }

      /**
       * Retrieves the weight of Rarity.
       */
      public int getWeight() {
         return this.weight;
      }
   }
}

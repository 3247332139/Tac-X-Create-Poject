package net.minecraft.loot;

import net.minecraft.loot.conditions.ILootCondition;

/**
 * The SerializerType for {@link LootItemCondition}.
 */
public class LootConditionType extends LootType<ILootCondition> {
   public LootConditionType(ILootSerializer<? extends ILootCondition> p_i232175_1_) {
      super(p_i232175_1_);
   }
}
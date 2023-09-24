package net.minecraft.util;

import net.minecraft.util.math.MathHelper;

public class CombatRules {
   public static float getDamageAfterAbsorb(float pDamage, float pTotalArmor, float pToughnessAttribute) {
      float f = 2.0F + pToughnessAttribute / 4.0F;
      float f1 = MathHelper.clamp(pTotalArmor - pDamage / f, pTotalArmor * 0.2F, 20.0F);
      return pDamage * (1.0F - f1 / 25.0F);
   }

   public static float getDamageAfterMagicAbsorb(float pDamage, float pEnchantModifiers) {
      float f = MathHelper.clamp(pEnchantModifiers, 0.0F, 20.0F);
      return pDamage * (1.0F - f / 25.0F);
   }
}
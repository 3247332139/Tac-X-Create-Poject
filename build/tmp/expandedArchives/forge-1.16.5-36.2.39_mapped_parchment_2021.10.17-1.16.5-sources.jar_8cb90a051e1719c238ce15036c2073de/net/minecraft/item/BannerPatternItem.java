package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BannerPatternItem extends Item {
   private final BannerPattern bannerPattern;

   public BannerPatternItem(BannerPattern pBannerPattern, Item.Properties pProperties) {
      super(pProperties);
      this.bannerPattern = pBannerPattern;
   }

   public BannerPattern getBannerPattern() {
      return this.bannerPattern;
   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
      pTooltip.add(this.getDisplayName().withStyle(TextFormatting.GRAY));
   }

   @OnlyIn(Dist.CLIENT)
   public IFormattableTextComponent getDisplayName() {
      return new TranslationTextComponent(this.getDescriptionId() + ".desc");
   }
}
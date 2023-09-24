package net.minecraft.client.gui.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Button extends AbstractButton {
   public static final Button.ITooltip NO_TOOLTIP = (p_238488_0_, p_238488_1_, p_238488_2_, p_238488_3_) -> {
   };
   protected final Button.IPressable onPress;
   protected final Button.ITooltip onTooltip;

   public Button(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, Button.IPressable pOnPress) {
      this(pX, pY, pWidth, pHeight, pMessage, pOnPress, NO_TOOLTIP);
   }

   public Button(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, Button.IPressable pOnPress, Button.ITooltip pOnTooltip) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.onPress = pOnPress;
      this.onTooltip = pOnTooltip;
   }

   public void onPress() {
      this.onPress.onPress(this);
   }

   public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      super.renderButton(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      if (this.isHovered()) {
         this.renderToolTip(pMatrixStack, pMouseX, pMouseY);
      }

   }

   public void renderToolTip(MatrixStack pPoseStack, int pMouseX, int pMouseY) {
      this.onTooltip.onTooltip(this, pPoseStack, pMouseX, pMouseY);
   }

   @OnlyIn(Dist.CLIENT)
   public interface IPressable {
      void onPress(Button p_onPress_1_);
   }

   @OnlyIn(Dist.CLIENT)
   public interface ITooltip {
      void onTooltip(Button p_onTooltip_1_, MatrixStack p_onTooltip_2_, int p_onTooltip_3_, int p_onTooltip_4_);
   }
}
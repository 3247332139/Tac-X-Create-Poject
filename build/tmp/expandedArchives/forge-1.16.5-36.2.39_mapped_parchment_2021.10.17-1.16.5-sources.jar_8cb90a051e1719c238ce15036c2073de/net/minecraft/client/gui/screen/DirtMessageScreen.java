package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DirtMessageScreen extends Screen {
   public DirtMessageScreen(ITextComponent p_i51114_1_) {
      super(p_i51114_1_);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderDirtBackground(0);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 70, 16777215);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }
}
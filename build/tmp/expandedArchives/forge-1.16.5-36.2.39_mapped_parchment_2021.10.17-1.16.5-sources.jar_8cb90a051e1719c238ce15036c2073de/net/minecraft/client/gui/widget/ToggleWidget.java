package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToggleWidget extends Widget {
   protected ResourceLocation resourceLocation;
   protected boolean isStateTriggered;
   protected int xTexStart;
   protected int yTexStart;
   protected int xDiffTex;
   protected int yDiffTex;

   public ToggleWidget(int pX, int pY, int pWidth, int pHeight, boolean pInitialState) {
      super(pX, pY, pWidth, pHeight, StringTextComponent.EMPTY);
      this.isStateTriggered = pInitialState;
   }

   public void initTextureValues(int pXTexStart, int pYTexStart, int pXDiffTex, int pYDiffTex, ResourceLocation pResourceLocation) {
      this.xTexStart = pXTexStart;
      this.yTexStart = pYTexStart;
      this.xDiffTex = pXDiffTex;
      this.yDiffTex = pYDiffTex;
      this.resourceLocation = pResourceLocation;
   }

   public void setStateTriggered(boolean pTriggered) {
      this.isStateTriggered = pTriggered;
   }

   public boolean isStateTriggered() {
      return this.isStateTriggered;
   }

   public void setPosition(int pX, int pY) {
      this.x = pX;
      this.y = pY;
   }

   public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.getTextureManager().bind(this.resourceLocation);
      RenderSystem.disableDepthTest();
      int i = this.xTexStart;
      int j = this.yTexStart;
      if (this.isStateTriggered) {
         i += this.xDiffTex;
      }

      if (this.isHovered()) {
         j += this.yDiffTex;
      }

      this.blit(pMatrixStack, this.x, this.y, i, j, this.width, this.height);
      RenderSystem.enableDepthTest();
   }
}
package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.GrindstoneContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GrindstoneScreen extends ContainerScreen<GrindstoneContainer> {
   private static final ResourceLocation GRINDSTONE_LOCATION = new ResourceLocation("textures/gui/container/grindstone.png");

   public GrindstoneScreen(GrindstoneContainer pGrindstoneMenu, PlayerInventory pPlayerInventory, ITextComponent pTitle) {
      super(pGrindstoneMenu, pPlayerInventory, pTitle);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      this.renderBg(pMatrixStack, pPartialTicks, pMouseX, pMouseY);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
   }

   protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(GRINDSTONE_LOCATION);
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
      if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem()) {
         this.blit(pMatrixStack, i + 92, j + 31, this.imageWidth, 0, 28, 21);
      }

   }
}
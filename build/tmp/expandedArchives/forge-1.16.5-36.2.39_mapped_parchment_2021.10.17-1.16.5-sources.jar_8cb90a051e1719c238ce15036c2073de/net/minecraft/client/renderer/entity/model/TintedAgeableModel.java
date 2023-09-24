package net.minecraft.client.renderer.entity.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TintedAgeableModel<E extends Entity> extends AgeableModel<E> {
   private float r = 1.0F;
   private float g = 1.0F;
   private float b = 1.0F;

   public void setColor(float p_228253_1_, float p_228253_2_, float p_228253_3_) {
      this.r = p_228253_1_;
      this.g = p_228253_2_;
      this.b = p_228253_3_;
   }

   public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      super.renderToBuffer(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, this.r * pRed, this.g * pGreen, this.b * pBlue, pAlpha);
   }
}
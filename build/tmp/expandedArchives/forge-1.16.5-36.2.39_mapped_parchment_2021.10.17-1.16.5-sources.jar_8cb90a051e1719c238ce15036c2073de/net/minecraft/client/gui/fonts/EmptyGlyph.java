package net.minecraft.client.gui.fonts;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EmptyGlyph extends TexturedGlyph {
   public EmptyGlyph() {
      super(RenderType.text(new ResourceLocation("")), RenderType.textSeeThrough(new ResourceLocation("")), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   public void render(boolean pItalic, float pX, float pY, Matrix4f pMatrix, IVertexBuilder pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pPackedLight) {
   }
}
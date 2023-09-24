package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpriteAwareVertexBuilder implements IVertexBuilder {
   private final IVertexBuilder delegate;
   private final TextureAtlasSprite sprite;

   public SpriteAwareVertexBuilder(IVertexBuilder p_i225999_1_, TextureAtlasSprite p_i225999_2_) {
      this.delegate = p_i225999_1_;
      this.sprite = p_i225999_2_;
   }

   public IVertexBuilder vertex(double pX, double pY, double pZ) {
      return this.delegate.vertex(pX, pY, pZ);
   }

   public IVertexBuilder color(int pRed, int pGreen, int pBlue, int pAlpha) {
      return this.delegate.color(pRed, pGreen, pBlue, pAlpha);
   }

   public IVertexBuilder uv(float pU, float pV) {
      return this.delegate.uv(this.sprite.getU((double)(pU * 16.0F)), this.sprite.getV((double)(pV * 16.0F)));
   }

   public IVertexBuilder overlayCoords(int pU, int pV) {
      return this.delegate.overlayCoords(pU, pV);
   }

   public IVertexBuilder uv2(int pU, int pV) {
      return this.delegate.uv2(pU, pV);
   }

   public IVertexBuilder normal(float pX, float pY, float pZ) {
      return this.delegate.normal(pX, pY, pZ);
   }

   public void endVertex() {
      this.delegate.endVertex();
   }

   public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
      this.delegate.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, this.sprite.getU((double)(pTexU * 16.0F)), this.sprite.getV((double)(pTexV * 16.0F)), pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
   }
}
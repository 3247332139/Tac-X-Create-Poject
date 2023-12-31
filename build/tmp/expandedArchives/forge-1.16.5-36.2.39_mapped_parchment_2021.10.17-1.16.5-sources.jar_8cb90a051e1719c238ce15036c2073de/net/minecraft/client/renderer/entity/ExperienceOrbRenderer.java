package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExperienceOrbRenderer extends EntityRenderer<ExperienceOrbEntity> {
   private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");
   private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

   public ExperienceOrbRenderer(EntityRendererManager p_i46178_1_) {
      super(p_i46178_1_);
      this.shadowRadius = 0.15F;
      this.shadowStrength = 0.75F;
   }

   protected int getBlockLightLevel(ExperienceOrbEntity pEntity, BlockPos pPos) {
      return MathHelper.clamp(super.getBlockLightLevel(pEntity, pPos) + 7, 0, 15);
   }

   public void render(ExperienceOrbEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      pMatrixStack.pushPose();
      int i = pEntity.getIcon();
      float f = (float)(i % 4 * 16 + 0) / 64.0F;
      float f1 = (float)(i % 4 * 16 + 16) / 64.0F;
      float f2 = (float)(i / 4 * 16 + 0) / 64.0F;
      float f3 = (float)(i / 4 * 16 + 16) / 64.0F;
      float f4 = 1.0F;
      float f5 = 0.5F;
      float f6 = 0.25F;
      float f7 = 255.0F;
      float f8 = ((float)pEntity.tickCount + pPartialTicks) / 2.0F;
      int j = (int)((MathHelper.sin(f8 + 0.0F) + 1.0F) * 0.5F * 255.0F);
      int k = 255;
      int l = (int)((MathHelper.sin(f8 + 4.1887903F) + 1.0F) * 0.1F * 255.0F);
      pMatrixStack.translate(0.0D, (double)0.1F, 0.0D);
      pMatrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
      float f9 = 0.3F;
      pMatrixStack.scale(0.3F, 0.3F, 0.3F);
      IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RENDER_TYPE);
      MatrixStack.Entry matrixstack$entry = pMatrixStack.last();
      Matrix4f matrix4f = matrixstack$entry.pose();
      Matrix3f matrix3f = matrixstack$entry.normal();
      vertex(ivertexbuilder, matrix4f, matrix3f, -0.5F, -0.25F, j, 255, l, f, f3, pPackedLight);
      vertex(ivertexbuilder, matrix4f, matrix3f, 0.5F, -0.25F, j, 255, l, f1, f3, pPackedLight);
      vertex(ivertexbuilder, matrix4f, matrix3f, 0.5F, 0.75F, j, 255, l, f1, f2, pPackedLight);
      vertex(ivertexbuilder, matrix4f, matrix3f, -0.5F, 0.75F, j, 255, l, f, f2, pPackedLight);
      pMatrixStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
   }

   private static void vertex(IVertexBuilder pBuffer, Matrix4f pMatrix, Matrix3f pMatrixNormal, float pX, float pY, int pRed, int pGreen, int pBlue, float pTexU, float pTexV, int pPackedLight) {
      pBuffer.vertex(pMatrix, pX, pY, 0.0F).color(pRed, pGreen, pBlue, 128).uv(pTexU, pTexV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(pMatrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(ExperienceOrbEntity pEntity) {
      return EXPERIENCE_ORB_LOCATION;
   }
}
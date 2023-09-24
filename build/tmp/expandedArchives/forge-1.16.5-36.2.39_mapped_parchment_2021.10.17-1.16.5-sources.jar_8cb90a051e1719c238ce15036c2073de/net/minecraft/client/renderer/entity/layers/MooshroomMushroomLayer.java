package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.CowModel;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MooshroomMushroomLayer<T extends MooshroomEntity> extends LayerRenderer<T, CowModel<T>> {
   public MooshroomMushroomLayer(IEntityRenderer<T, CowModel<T>> p_i50931_1_) {
      super(p_i50931_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (!pLivingEntity.isBaby() && !pLivingEntity.isInvisible()) {
         BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
         BlockState blockstate = pLivingEntity.getMushroomType().getBlockState();
         int i = LivingRenderer.getOverlayCoords(pLivingEntity, 0.0F);
         pMatrixStack.pushPose();
         pMatrixStack.translate((double)0.2F, (double)-0.35F, 0.5D);
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
         pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
         pMatrixStack.translate(-0.5D, -0.5D, -0.5D);
         blockrendererdispatcher.renderSingleBlock(blockstate, pMatrixStack, pBuffer, pPackedLight, i);
         pMatrixStack.popPose();
         pMatrixStack.pushPose();
         pMatrixStack.translate((double)0.2F, (double)-0.35F, 0.5D);
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(42.0F));
         pMatrixStack.translate((double)0.1F, 0.0D, (double)-0.6F);
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
         pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
         pMatrixStack.translate(-0.5D, -0.5D, -0.5D);
         blockrendererdispatcher.renderSingleBlock(blockstate, pMatrixStack, pBuffer, pPackedLight, i);
         pMatrixStack.popPose();
         pMatrixStack.pushPose();
         this.getParentModel().getHead().translateAndRotate(pMatrixStack);
         pMatrixStack.translate(0.0D, (double)-0.7F, (double)-0.2F);
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-78.0F));
         pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
         pMatrixStack.translate(-0.5D, -0.5D, -0.5D);
         blockrendererdispatcher.renderSingleBlock(blockstate, pMatrixStack, pBuffer, pPackedLight, i);
         pMatrixStack.popPose();
      }
   }
}
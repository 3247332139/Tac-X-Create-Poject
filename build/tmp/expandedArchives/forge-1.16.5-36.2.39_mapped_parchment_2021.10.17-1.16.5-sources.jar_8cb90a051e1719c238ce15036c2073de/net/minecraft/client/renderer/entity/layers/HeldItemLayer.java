package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HeldItemLayer<T extends LivingEntity, M extends EntityModel<T> & IHasArm> extends LayerRenderer<T, M> {
   public HeldItemLayer(IEntityRenderer<T, M> p_i50934_1_) {
      super(p_i50934_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      boolean flag = pLivingEntity.getMainArm() == HandSide.RIGHT;
      ItemStack itemstack = flag ? pLivingEntity.getOffhandItem() : pLivingEntity.getMainHandItem();
      ItemStack itemstack1 = flag ? pLivingEntity.getMainHandItem() : pLivingEntity.getOffhandItem();
      if (!itemstack.isEmpty() || !itemstack1.isEmpty()) {
         pMatrixStack.pushPose();
         if (this.getParentModel().young) {
            float f = 0.5F;
            pMatrixStack.translate(0.0D, 0.75D, 0.0D);
            pMatrixStack.scale(0.5F, 0.5F, 0.5F);
         }

         this.renderArmWithItem(pLivingEntity, itemstack1, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, pMatrixStack, pBuffer, pPackedLight);
         this.renderArmWithItem(pLivingEntity, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, pMatrixStack, pBuffer, pPackedLight);
         pMatrixStack.popPose();
      }
   }

   private void renderArmWithItem(LivingEntity p_229135_1_, ItemStack p_229135_2_, ItemCameraTransforms.TransformType p_229135_3_, HandSide p_229135_4_, MatrixStack p_229135_5_, IRenderTypeBuffer p_229135_6_, int p_229135_7_) {
      if (!p_229135_2_.isEmpty()) {
         p_229135_5_.pushPose();
         this.getParentModel().translateToHand(p_229135_4_, p_229135_5_);
         p_229135_5_.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
         p_229135_5_.mulPose(Vector3f.YP.rotationDegrees(180.0F));
         boolean flag = p_229135_4_ == HandSide.LEFT;
         p_229135_5_.translate((double)((float)(flag ? -1 : 1) / 16.0F), 0.125D, -0.625D);
         Minecraft.getInstance().getItemInHandRenderer().renderItem(p_229135_1_, p_229135_2_, p_229135_3_, flag, p_229135_5_, p_229135_6_, p_229135_7_);
         p_229135_5_.popPose();
      }
   }
}
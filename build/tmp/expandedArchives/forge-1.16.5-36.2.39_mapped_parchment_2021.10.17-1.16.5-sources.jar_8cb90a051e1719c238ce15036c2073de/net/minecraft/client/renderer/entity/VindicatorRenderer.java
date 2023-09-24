package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.IllagerModel;
import net.minecraft.entity.monster.VindicatorEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VindicatorRenderer extends IllagerRenderer<VindicatorEntity> {
   private static final ResourceLocation VINDICATOR = new ResourceLocation("textures/entity/illager/vindicator.png");

   public VindicatorRenderer(EntityRendererManager p_i47189_1_) {
      super(p_i47189_1_, new IllagerModel<>(0.0F, 0.0F, 64, 64), 0.5F);
      this.addLayer(new HeldItemLayer<VindicatorEntity, IllagerModel<VindicatorEntity>>(this) {
         public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, VindicatorEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            if (pLivingEntity.isAggressive()) {
               super.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks, pAgeInTicks, pNetHeadYaw, pHeadPitch);
            }

         }
      });
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(VindicatorEntity pEntity) {
      return VINDICATOR;
   }
}
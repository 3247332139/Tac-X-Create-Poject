package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfCollarLayer extends LayerRenderer<WolfEntity, WolfModel<WolfEntity>> {
   private static final ResourceLocation WOLF_COLLAR_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_collar.png");

   public WolfCollarLayer(IEntityRenderer<WolfEntity, WolfModel<WolfEntity>> p_i50914_1_) {
      super(p_i50914_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, WolfEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (pLivingEntity.isTame() && !pLivingEntity.isInvisible()) {
         float[] afloat = pLivingEntity.getCollarColor().getTextureDiffuseColors();
         renderColoredCutoutModel(this.getParentModel(), WOLF_COLLAR_LOCATION, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, afloat[0], afloat[1], afloat[2]);
      }
   }
}
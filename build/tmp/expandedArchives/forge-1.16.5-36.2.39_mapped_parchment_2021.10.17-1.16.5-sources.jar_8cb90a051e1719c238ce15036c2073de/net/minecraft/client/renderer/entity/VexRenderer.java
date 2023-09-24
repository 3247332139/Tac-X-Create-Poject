package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.model.VexModel;
import net.minecraft.entity.monster.VexEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VexRenderer extends BipedRenderer<VexEntity, VexModel> {
   private static final ResourceLocation VEX_LOCATION = new ResourceLocation("textures/entity/illager/vex.png");
   private static final ResourceLocation VEX_CHARGING_LOCATION = new ResourceLocation("textures/entity/illager/vex_charging.png");

   public VexRenderer(EntityRendererManager p_i47190_1_) {
      super(p_i47190_1_, new VexModel(), 0.3F);
   }

   protected int getBlockLightLevel(VexEntity pEntity, BlockPos pPos) {
      return 15;
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(VexEntity pEntity) {
      return pEntity.isCharging() ? VEX_CHARGING_LOCATION : VEX_LOCATION;
   }

   protected void scale(VexEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      pMatrixStack.scale(0.4F, 0.4F, 0.4F);
   }
}
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HuskRenderer extends ZombieRenderer {
   private static final ResourceLocation HUSK_LOCATION = new ResourceLocation("textures/entity/zombie/husk.png");

   public HuskRenderer(EntityRendererManager p_i47204_1_) {
      super(p_i47204_1_);
   }

   protected void scale(ZombieEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      float f = 1.0625F;
      pMatrixStack.scale(1.0625F, 1.0625F, 1.0625F);
      super.scale(pLivingEntity, pMatrixStack, pPartialTickTime);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(ZombieEntity pEntity) {
      return HUSK_LOCATION;
   }
}
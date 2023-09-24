package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.layers.WitherAuraLayer;
import net.minecraft.client.renderer.entity.model.WitherModel;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherRenderer extends MobRenderer<WitherEntity, WitherModel<WitherEntity>> {
   private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
   private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");

   public WitherRenderer(EntityRendererManager p_i46130_1_) {
      super(p_i46130_1_, new WitherModel<>(0.0F), 1.0F);
      this.addLayer(new WitherAuraLayer(this));
   }

   protected int getBlockLightLevel(WitherEntity pEntity, BlockPos pPos) {
      return 15;
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(WitherEntity pEntity) {
      int i = pEntity.getInvulnerableTicks();
      return i > 0 && (i > 80 || i / 5 % 2 != 1) ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
   }

   protected void scale(WitherEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      float f = 2.0F;
      int i = pLivingEntity.getInvulnerableTicks();
      if (i > 0) {
         f -= ((float)i - pPartialTickTime) / 220.0F * 0.5F;
      }

      pMatrixStack.scale(f, f, f);
   }
}
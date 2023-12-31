package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.GiantModel;
import net.minecraft.entity.monster.GiantEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GiantZombieRenderer extends MobRenderer<GiantEntity, BipedModel<GiantEntity>> {
   private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");
   private final float scale;

   public GiantZombieRenderer(EntityRendererManager p_i47206_1_, float p_i47206_2_) {
      super(p_i47206_1_, new GiantModel(), 0.5F * p_i47206_2_);
      this.scale = p_i47206_2_;
      this.addLayer(new HeldItemLayer<>(this));
      this.addLayer(new BipedArmorLayer<>(this, new GiantModel(0.5F, true), new GiantModel(1.0F, true)));
   }

   protected void scale(GiantEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      pMatrixStack.scale(this.scale, this.scale, this.scale);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(GiantEntity pEntity) {
      return ZOMBIE_LOCATION;
   }
}
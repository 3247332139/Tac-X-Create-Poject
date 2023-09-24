package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.EndermanEyesLayer;
import net.minecraft.client.renderer.entity.layers.HeldBlockLayer;
import net.minecraft.client.renderer.entity.model.EndermanModel;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndermanRenderer extends MobRenderer<EndermanEntity, EndermanModel<EndermanEntity>> {
   private static final ResourceLocation ENDERMAN_LOCATION = new ResourceLocation("textures/entity/enderman/enderman.png");
   private final Random random = new Random();

   public EndermanRenderer(EntityRendererManager p_i46182_1_) {
      super(p_i46182_1_, new EndermanModel<>(0.0F), 0.5F);
      this.addLayer(new EndermanEyesLayer<>(this));
      this.addLayer(new HeldBlockLayer(this));
   }

   public void render(EndermanEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      BlockState blockstate = pEntity.getCarriedBlock();
      EndermanModel<EndermanEntity> endermanmodel = this.getModel();
      endermanmodel.carrying = blockstate != null;
      endermanmodel.creepy = pEntity.isCreepy();
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
   }

   public Vector3d getRenderOffset(EndermanEntity pEntity, float pPartialTicks) {
      if (pEntity.isCreepy()) {
         double d0 = 0.02D;
         return new Vector3d(this.random.nextGaussian() * 0.02D, 0.0D, this.random.nextGaussian() * 0.02D);
      } else {
         return super.getRenderOffset(pEntity, pPartialTicks);
      }
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(EndermanEntity pEntity) {
      return ENDERMAN_LOCATION;
   }
}
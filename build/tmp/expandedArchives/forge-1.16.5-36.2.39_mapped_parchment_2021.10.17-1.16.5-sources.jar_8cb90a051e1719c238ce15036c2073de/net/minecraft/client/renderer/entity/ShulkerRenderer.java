package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.layers.ShulkerColorLayer;
import net.minecraft.client.renderer.entity.model.ShulkerModel;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerRenderer extends MobRenderer<ShulkerEntity, ShulkerModel<ShulkerEntity>> {
   public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation("textures/" + Atlases.DEFAULT_SHULKER_TEXTURE_LOCATION.texture().getPath() + ".png");
   public static final ResourceLocation[] TEXTURE_LOCATION = Atlases.SHULKER_TEXTURE_LOCATION.stream().map((p_229125_0_) -> {
      return new ResourceLocation("textures/" + p_229125_0_.texture().getPath() + ".png");
   }).toArray((p_229124_0_) -> {
      return new ResourceLocation[p_229124_0_];
   });

   public ShulkerRenderer(EntityRendererManager p_i47194_1_) {
      super(p_i47194_1_, new ShulkerModel<>(), 0.0F);
      this.addLayer(new ShulkerColorLayer(this));
   }

   public Vector3d getRenderOffset(ShulkerEntity pEntity, float pPartialTicks) {
      int i = pEntity.getClientSideTeleportInterpolation();
      if (i > 0 && pEntity.hasValidInterpolationPositions()) {
         BlockPos blockpos = pEntity.getAttachPosition();
         BlockPos blockpos1 = pEntity.getOldAttachPosition();
         double d0 = (double)((float)i - pPartialTicks) / 6.0D;
         d0 = d0 * d0;
         double d1 = (double)(blockpos.getX() - blockpos1.getX()) * d0;
         double d2 = (double)(blockpos.getY() - blockpos1.getY()) * d0;
         double d3 = (double)(blockpos.getZ() - blockpos1.getZ()) * d0;
         return new Vector3d(-d1, -d2, -d3);
      } else {
         return super.getRenderOffset(pEntity, pPartialTicks);
      }
   }

   public boolean shouldRender(ShulkerEntity pLivingEntity, ClippingHelper pCamera, double pCamX, double pCamY, double pCamZ) {
      if (super.shouldRender(pLivingEntity, pCamera, pCamX, pCamY, pCamZ)) {
         return true;
      } else {
         if (pLivingEntity.getClientSideTeleportInterpolation() > 0 && pLivingEntity.hasValidInterpolationPositions()) {
            Vector3d vector3d = Vector3d.atLowerCornerOf(pLivingEntity.getAttachPosition());
            Vector3d vector3d1 = Vector3d.atLowerCornerOf(pLivingEntity.getOldAttachPosition());
            if (pCamera.isVisible(new AxisAlignedBB(vector3d1.x, vector3d1.y, vector3d1.z, vector3d.x, vector3d.y, vector3d.z))) {
               return true;
            }
         }

         return false;
      }
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(ShulkerEntity pEntity) {
      return pEntity.getColor() == null ? DEFAULT_TEXTURE_LOCATION : TEXTURE_LOCATION[pEntity.getColor().getId()];
   }

   protected void setupRotations(ShulkerEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw + 180.0F, pPartialTicks);
      pMatrixStack.translate(0.0D, 0.5D, 0.0D);
      pMatrixStack.mulPose(pEntityLiving.getAttachFace().getOpposite().getRotation());
      pMatrixStack.translate(0.0D, -0.5D, 0.0D);
   }
}
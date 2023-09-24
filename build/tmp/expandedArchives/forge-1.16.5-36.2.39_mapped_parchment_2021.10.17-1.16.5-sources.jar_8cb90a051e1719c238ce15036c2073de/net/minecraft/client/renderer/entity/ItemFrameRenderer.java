package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemFrameRenderer extends EntityRenderer<ItemFrameEntity> {
   private static final ModelResourceLocation FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=false");
   private static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=true");
   private final Minecraft minecraft = Minecraft.getInstance();
   private final net.minecraft.client.renderer.ItemRenderer itemRenderer;

   public ItemFrameRenderer(EntityRendererManager p_i46166_1_, net.minecraft.client.renderer.ItemRenderer p_i46166_2_) {
      super(p_i46166_1_);
      this.itemRenderer = p_i46166_2_;
   }

   public void render(ItemFrameEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
      pMatrixStack.pushPose();
      Direction direction = pEntity.getDirection();
      Vector3d vector3d = this.getRenderOffset(pEntity, pPartialTicks);
      pMatrixStack.translate(-vector3d.x(), -vector3d.y(), -vector3d.z());
      double d0 = 0.46875D;
      pMatrixStack.translate((double)direction.getStepX() * 0.46875D, (double)direction.getStepY() * 0.46875D, (double)direction.getStepZ() * 0.46875D);
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(pEntity.xRot));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pEntity.yRot));
      boolean flag = pEntity.isInvisible();
      if (!flag) {
         BlockRendererDispatcher blockrendererdispatcher = this.minecraft.getBlockRenderer();
         ModelManager modelmanager = blockrendererdispatcher.getBlockModelShaper().getModelManager();
         ModelResourceLocation modelresourcelocation = pEntity.getItem().getItem() instanceof FilledMapItem ? MAP_FRAME_LOCATION : FRAME_LOCATION;
         pMatrixStack.pushPose();
         pMatrixStack.translate(-0.5D, -0.5D, -0.5D);
         blockrendererdispatcher.getModelRenderer().renderModel(pMatrixStack.last(), pBuffer.getBuffer(Atlases.solidBlockSheet()), (BlockState)null, modelmanager.getModel(modelresourcelocation), 1.0F, 1.0F, 1.0F, pPackedLight, OverlayTexture.NO_OVERLAY);
         pMatrixStack.popPose();
      }

      ItemStack itemstack = pEntity.getItem();
      if (!itemstack.isEmpty()) {
         MapData mapdata = FilledMapItem.getOrCreateSavedData(itemstack, pEntity.level);
         if (flag) {
            pMatrixStack.translate(0.0D, 0.0D, 0.5D);
         } else {
            pMatrixStack.translate(0.0D, 0.0D, 0.4375D);
         }

         int i = mapdata != null ? pEntity.getRotation() % 4 * 2 : pEntity.getRotation();
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)i * 360.0F / 8.0F));
         if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderItemInFrameEvent(pEntity, this, pMatrixStack, pBuffer, pPackedLight))) {
         if (mapdata != null) {
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            float f = 0.0078125F;
            pMatrixStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
            pMatrixStack.translate(-64.0D, -64.0D, 0.0D);
            pMatrixStack.translate(0.0D, 0.0D, -1.0D);
            if (mapdata != null) {
               this.minecraft.gameRenderer.getMapRenderer().render(pMatrixStack, pBuffer, mapdata, true, pPackedLight);
            }
         } else {
            pMatrixStack.scale(0.5F, 0.5F, 0.5F);
            this.itemRenderer.renderStatic(itemstack, ItemCameraTransforms.TransformType.FIXED, pPackedLight, OverlayTexture.NO_OVERLAY, pMatrixStack, pBuffer);
         }
         }
      }

      pMatrixStack.popPose();
   }

   public Vector3d getRenderOffset(ItemFrameEntity pEntity, float pPartialTicks) {
      return new Vector3d((double)((float)pEntity.getDirection().getStepX() * 0.3F), -0.25D, (double)((float)pEntity.getDirection().getStepZ() * 0.3F));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(ItemFrameEntity pEntity) {
      return AtlasTexture.LOCATION_BLOCKS;
   }

   protected boolean shouldShowName(ItemFrameEntity pEntity) {
      if (Minecraft.renderNames() && !pEntity.getItem().isEmpty() && pEntity.getItem().hasCustomHoverName() && this.entityRenderDispatcher.crosshairPickEntity == pEntity) {
         double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
         float f = pEntity.isDiscrete() ? 32.0F : 64.0F;
         return d0 < (double)(f * f);
      } else {
         return false;
      }
   }

   protected void renderNameTag(ItemFrameEntity pEntity, ITextComponent pDisplayName, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      super.renderNameTag(pEntity, pEntity.getItem().getHoverName(), pMatrixStack, pBuffer, pPackedLight);
   }
}

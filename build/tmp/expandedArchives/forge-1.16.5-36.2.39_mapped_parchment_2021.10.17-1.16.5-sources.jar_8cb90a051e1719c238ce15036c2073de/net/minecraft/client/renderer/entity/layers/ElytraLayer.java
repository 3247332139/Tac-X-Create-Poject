package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
   private static final ResourceLocation WINGS_LOCATION = new ResourceLocation("textures/entity/elytra.png");
   private final ElytraModel<T> elytraModel = new ElytraModel<>();

   public ElytraLayer(IEntityRenderer<T, M> p_i50942_1_) {
      super(p_i50942_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlotType.CHEST);
      if (shouldRender(itemstack, pLivingEntity)) {
         ResourceLocation resourcelocation;
         if (pLivingEntity instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity abstractclientplayerentity = (AbstractClientPlayerEntity)pLivingEntity;
            if (abstractclientplayerentity.isElytraLoaded() && abstractclientplayerentity.getElytraTextureLocation() != null) {
               resourcelocation = abstractclientplayerentity.getElytraTextureLocation();
            } else if (abstractclientplayerentity.isCapeLoaded() && abstractclientplayerentity.getCloakTextureLocation() != null && abstractclientplayerentity.isModelPartShown(PlayerModelPart.CAPE)) {
               resourcelocation = abstractclientplayerentity.getCloakTextureLocation();
            } else {
               resourcelocation = getElytraTexture(itemstack, pLivingEntity);
            }
         } else {
            resourcelocation = getElytraTexture(itemstack, pLivingEntity);
         }

         pMatrixStack.pushPose();
         pMatrixStack.translate(0.0D, 0.0D, 0.125D);
         this.getParentModel().copyPropertiesTo(this.elytraModel);
         this.elytraModel.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
         IVertexBuilder ivertexbuilder = ItemRenderer.getArmorFoilBuffer(pBuffer, RenderType.armorCutoutNoCull(resourcelocation), false, itemstack.hasFoil());
         this.elytraModel.renderToBuffer(pMatrixStack, ivertexbuilder, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         pMatrixStack.popPose();
      }
   }

   /**
    * Determines if the ElytraLayer should render.
    * ItemStack and Entity are provided for modder convenience,
    * For example, using the same ElytraLayer for multiple custom Elytra.
    *
    * @param stack  The Elytra ItemStack
    * @param entity The entity being rendered.
    * @return If the ElytraLayer should render.
    */
   public boolean shouldRender(ItemStack stack, T entity) {
      return stack.getItem() == Items.ELYTRA;
   }

   /**
    * Gets the texture to use with this ElytraLayer.
    * This assumes the vanilla Elytra model.
    *
    * @param stack  The Elytra ItemStack.
    * @param entity The entity being rendered.
    * @return The texture.
    */
   public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
      return WINGS_LOCATION;
   }
}

package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.ShulkerModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBoxTileEntityRenderer extends TileEntityRenderer<ShulkerBoxTileEntity> {
   private final ShulkerModel<?> model;

   public ShulkerBoxTileEntityRenderer(ShulkerModel<?> p_i226013_1_, TileEntityRendererDispatcher p_i226013_2_) {
      super(p_i226013_2_);
      this.model = p_i226013_1_;
   }

   public void render(ShulkerBoxTileEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      Direction direction = Direction.UP;
      if (pBlockEntity.hasLevel()) {
         BlockState blockstate = pBlockEntity.getLevel().getBlockState(pBlockEntity.getBlockPos());
         if (blockstate.getBlock() instanceof ShulkerBoxBlock) {
            direction = blockstate.getValue(ShulkerBoxBlock.FACING);
         }
      }

      DyeColor dyecolor = pBlockEntity.getColor();
      RenderMaterial rendermaterial;
      if (dyecolor == null) {
         rendermaterial = Atlases.DEFAULT_SHULKER_TEXTURE_LOCATION;
      } else {
         rendermaterial = Atlases.SHULKER_TEXTURE_LOCATION.get(dyecolor.getId());
      }

      pMatrixStack.pushPose();
      pMatrixStack.translate(0.5D, 0.5D, 0.5D);
      float f = 0.9995F;
      pMatrixStack.scale(0.9995F, 0.9995F, 0.9995F);
      pMatrixStack.mulPose(direction.getRotation());
      pMatrixStack.scale(1.0F, -1.0F, -1.0F);
      pMatrixStack.translate(0.0D, -1.0D, 0.0D);
      IVertexBuilder ivertexbuilder = rendermaterial.buffer(pBuffer, RenderType::entityCutoutNoCull);
      this.model.getBase().render(pMatrixStack, ivertexbuilder, pCombinedLight, pCombinedOverlay);
      pMatrixStack.translate(0.0D, (double)(-pBlockEntity.getProgress(pPartialTicks) * 0.5F), 0.0D);
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(270.0F * pBlockEntity.getProgress(pPartialTicks)));
      this.model.getLid().render(pMatrixStack, ivertexbuilder, pCombinedLight, pCombinedOverlay);
      pMatrixStack.popPose();
   }
}
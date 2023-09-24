package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LecternTileEntityRenderer extends TileEntityRenderer<LecternTileEntity> {
   private final BookModel bookModel = new BookModel();

   public LecternTileEntityRenderer(TileEntityRendererDispatcher p_i226011_1_) {
      super(p_i226011_1_);
   }

   public void render(LecternTileEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      BlockState blockstate = pBlockEntity.getBlockState();
      if (blockstate.getValue(LecternBlock.HAS_BOOK)) {
         pMatrixStack.pushPose();
         pMatrixStack.translate(0.5D, 1.0625D, 0.5D);
         float f = blockstate.getValue(LecternBlock.FACING).getClockWise().toYRot();
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-f));
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(67.5F));
         pMatrixStack.translate(0.0D, -0.125D, 0.0D);
         this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
         IVertexBuilder ivertexbuilder = EnchantmentTableTileEntityRenderer.BOOK_LOCATION.buffer(pBuffer, RenderType::entitySolid);
         this.bookModel.render(pMatrixStack, ivertexbuilder, pCombinedLight, pCombinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
         pMatrixStack.popPose();
      }
   }
}
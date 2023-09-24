package net.minecraft.client.renderer.entity.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SegmentedModel<E extends Entity> extends EntityModel<E> {
   public SegmentedModel() {
      this(RenderType::entityCutoutNoCull);
   }

   public SegmentedModel(Function<ResourceLocation, RenderType> p_i232335_1_) {
      super(p_i232335_1_);
   }

   public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.parts().forEach((p_228272_8_) -> {
         p_228272_8_.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
      });
   }

   public abstract Iterable<ModelRenderer> parts();
}
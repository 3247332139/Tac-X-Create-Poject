package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndGatewayTileEntityRenderer extends EndPortalTileEntityRenderer<EndGatewayTileEntity> {
   private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/end_gateway_beam.png");

   public EndGatewayTileEntityRenderer(TileEntityRendererDispatcher p_i226018_1_) {
      super(p_i226018_1_);
   }

   public void render(EndGatewayTileEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      if (pBlockEntity.isSpawning() || pBlockEntity.isCoolingDown()) {
         float f = pBlockEntity.isSpawning() ? pBlockEntity.getSpawnPercent(pPartialTicks) : pBlockEntity.getCooldownPercent(pPartialTicks);
         double d0 = pBlockEntity.isSpawning() ? 256.0D : 50.0D;
         f = MathHelper.sin(f * (float)Math.PI);
         int i = MathHelper.floor((double)f * d0);
         float[] afloat = pBlockEntity.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColors() : DyeColor.PURPLE.getTextureDiffuseColors();
         long j = pBlockEntity.getLevel().getGameTime();
         BeaconTileEntityRenderer.renderBeaconBeam(pMatrixStack, pBuffer, BEAM_LOCATION, pPartialTicks, f, j, 0, i, afloat, 0.15F, 0.175F);
         BeaconTileEntityRenderer.renderBeaconBeam(pMatrixStack, pBuffer, BEAM_LOCATION, pPartialTicks, f, j, 0, -i, afloat, 0.15F, 0.175F);
      }

      super.render(pBlockEntity, pPartialTicks, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay);
   }

   protected int getPasses(double p_191286_1_) {
      return super.getPasses(p_191286_1_) + 1;
   }

   protected float getOffset() {
      return 1.0F;
   }
}
package net.minecraft.client.particle;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemPickupParticle extends Particle {
   private final RenderTypeBuffers renderBuffers;
   private final Entity itemEntity;
   private final Entity target;
   private int life;
   private final EntityRendererManager entityRenderDispatcher;

   public ItemPickupParticle(EntityRendererManager pEntityRenderDispatcher, RenderTypeBuffers pBuffers, ClientWorld pLevel, Entity pItemEntity, Entity pTarget) {
      this(pEntityRenderDispatcher, pBuffers, pLevel, pItemEntity, pTarget, pItemEntity.getDeltaMovement());
   }

   private ItemPickupParticle(EntityRendererManager pEntityRenderDispatcher, RenderTypeBuffers pBuffers, ClientWorld pLevel, Entity pItemEntity, Entity pTarget, Vector3d pSpeedVector) {
      super(pLevel, pItemEntity.getX(), pItemEntity.getY(), pItemEntity.getZ(), pSpeedVector.x, pSpeedVector.y, pSpeedVector.z);
      this.renderBuffers = pBuffers;
      this.itemEntity = this.getSafeCopy(pItemEntity);
      this.target = pTarget;
      this.entityRenderDispatcher = pEntityRenderDispatcher;
   }

   private Entity getSafeCopy(Entity pEntity) {
      return (Entity)(!(pEntity instanceof ItemEntity) ? pEntity : ((ItemEntity)pEntity).copy());
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.CUSTOM;
   }

   public void render(IVertexBuilder pBuffer, ActiveRenderInfo pRenderInfo, float pPartialTicks) {
      float f = ((float)this.life + pPartialTicks) / 3.0F;
      f = f * f;
      double d0 = MathHelper.lerp((double)pPartialTicks, this.target.xOld, this.target.getX());
      double d1 = MathHelper.lerp((double)pPartialTicks, this.target.yOld, this.target.getY()) + 0.5D;
      double d2 = MathHelper.lerp((double)pPartialTicks, this.target.zOld, this.target.getZ());
      double d3 = MathHelper.lerp((double)f, this.itemEntity.getX(), d0);
      double d4 = MathHelper.lerp((double)f, this.itemEntity.getY(), d1);
      double d5 = MathHelper.lerp((double)f, this.itemEntity.getZ(), d2);
      IRenderTypeBuffer.Impl irendertypebuffer$impl = this.renderBuffers.bufferSource();
      Vector3d vector3d = pRenderInfo.getPosition();
      this.entityRenderDispatcher.render(this.itemEntity, d3 - vector3d.x(), d4 - vector3d.y(), d5 - vector3d.z(), this.itemEntity.yRot, pPartialTicks, new MatrixStack(), irendertypebuffer$impl, this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, pPartialTicks));
      irendertypebuffer$impl.endBatch();
   }

   public void tick() {
      ++this.life;
      if (this.life == 3) {
         this.remove();
      }

   }
}
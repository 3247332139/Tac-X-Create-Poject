package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TexturedParticle extends Particle {
   protected float quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;

   protected TexturedParticle(ClientWorld p_i232423_1_, double p_i232423_2_, double p_i232423_4_, double p_i232423_6_) {
      super(p_i232423_1_, p_i232423_2_, p_i232423_4_, p_i232423_6_);
   }

   protected TexturedParticle(ClientWorld p_i232424_1_, double p_i232424_2_, double p_i232424_4_, double p_i232424_6_, double p_i232424_8_, double p_i232424_10_, double p_i232424_12_) {
      super(p_i232424_1_, p_i232424_2_, p_i232424_4_, p_i232424_6_, p_i232424_8_, p_i232424_10_, p_i232424_12_);
   }

   public void render(IVertexBuilder pBuffer, ActiveRenderInfo pRenderInfo, float pPartialTicks) {
      Vector3d vector3d = pRenderInfo.getPosition();
      float f = (float)(MathHelper.lerp((double)pPartialTicks, this.xo, this.x) - vector3d.x());
      float f1 = (float)(MathHelper.lerp((double)pPartialTicks, this.yo, this.y) - vector3d.y());
      float f2 = (float)(MathHelper.lerp((double)pPartialTicks, this.zo, this.z) - vector3d.z());
      Quaternion quaternion;
      if (this.roll == 0.0F) {
         quaternion = pRenderInfo.rotation();
      } else {
         quaternion = new Quaternion(pRenderInfo.rotation());
         float f3 = MathHelper.lerp(pPartialTicks, this.oRoll, this.roll);
         quaternion.mul(Vector3f.ZP.rotation(f3));
      }

      Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
      vector3f1.transform(quaternion);
      Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
      float f4 = this.getQuadSize(pPartialTicks);

      for(int i = 0; i < 4; ++i) {
         Vector3f vector3f = avector3f[i];
         vector3f.transform(quaternion);
         vector3f.mul(f4);
         vector3f.add(f, f1, f2);
      }

      float f7 = this.getU0();
      float f8 = this.getU1();
      float f5 = this.getV0();
      float f6 = this.getV1();
      int j = this.getLightColor(pPartialTicks);
      pBuffer.vertex((double)avector3f[0].x(), (double)avector3f[0].y(), (double)avector3f[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
      pBuffer.vertex((double)avector3f[1].x(), (double)avector3f[1].y(), (double)avector3f[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
      pBuffer.vertex((double)avector3f[2].x(), (double)avector3f[2].y(), (double)avector3f[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
      pBuffer.vertex((double)avector3f[3].x(), (double)avector3f[3].y(), (double)avector3f[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
   }

   public float getQuadSize(float pScaleFactor) {
      return this.quadSize;
   }

   public Particle scale(float pScale) {
      this.quadSize *= pScale;
      return super.scale(pScale);
   }

   protected abstract float getU0();

   protected abstract float getU1();

   protected abstract float getV0();

   protected abstract float getV1();
}
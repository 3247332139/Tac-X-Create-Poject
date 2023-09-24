package net.minecraft.client.particle;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DripParticle extends SpriteTexturedParticle {
   private final Fluid type;
   protected boolean isGlowing;

   private DripParticle(ClientWorld pLevel, double pX, double pY, double pZ, Fluid pType) {
      super(pLevel, pX, pY, pZ);
      this.setSize(0.01F, 0.01F);
      this.gravity = 0.06F;
      this.type = pType;
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public int getLightColor(float pPartialTick) {
      return this.isGlowing ? 240 : super.getLightColor(pPartialTick);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.preMoveUpdate();
      if (!this.removed) {
         this.yd -= (double)this.gravity;
         this.move(this.xd, this.yd, this.zd);
         this.postMoveUpdate();
         if (!this.removed) {
            this.xd *= (double)0.98F;
            this.yd *= (double)0.98F;
            this.zd *= (double)0.98F;
            BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
            FluidState fluidstate = this.level.getFluidState(blockpos);
            if (fluidstate.getType() == this.type && this.y < (double)((float)blockpos.getY() + fluidstate.getHeight(this.level, blockpos))) {
               this.remove();
            }

         }
      }
   }

   protected void preMoveUpdate() {
      if (this.lifetime-- <= 0) {
         this.remove();
      }

   }

   protected void postMoveUpdate() {
   }

   @OnlyIn(Dist.CLIENT)
   static class Dripping extends DripParticle {
      private final IParticleData fallingParticle;

      private Dripping(ClientWorld pLevel, double pX, double pY, double pZ, Fluid pType, IParticleData pFallingParticle) {
         super(pLevel, pX, pY, pZ, pType);
         this.fallingParticle = pFallingParticle;
         this.gravity *= 0.02F;
         this.lifetime = 40;
      }

      protected void preMoveUpdate() {
         if (this.lifetime-- <= 0) {
            this.remove();
            this.level.addParticle(this.fallingParticle, this.x, this.y, this.z, this.xd, this.yd, this.zd);
         }

      }

      protected void postMoveUpdate() {
         this.xd *= 0.02D;
         this.yd *= 0.02D;
         this.zd *= 0.02D;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class DrippingHoneyFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public DrippingHoneyFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle.Dripping dripparticle$dripping = new DripParticle.Dripping(pLevel, pX, pY, pZ, Fluids.EMPTY, ParticleTypes.FALLING_HONEY);
         dripparticle$dripping.gravity *= 0.01F;
         dripparticle$dripping.lifetime = 100;
         dripparticle$dripping.setColor(0.622F, 0.508F, 0.082F);
         dripparticle$dripping.pickSprite(this.sprite);
         return dripparticle$dripping;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class DrippingLava extends DripParticle.Dripping {
      private DrippingLava(ClientWorld p_i232363_1_, double p_i232363_2_, double p_i232363_4_, double p_i232363_6_, Fluid p_i232363_8_, IParticleData p_i232363_9_) {
         super(p_i232363_1_, p_i232363_2_, p_i232363_4_, p_i232363_6_, p_i232363_8_, p_i232363_9_);
      }

      protected void preMoveUpdate() {
         this.rCol = 1.0F;
         this.gCol = 16.0F / (float)(40 - this.lifetime + 16);
         this.bCol = 4.0F / (float)(40 - this.lifetime + 8);
         super.preMoveUpdate();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class DrippingLavaFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public DrippingLavaFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle.DrippingLava dripparticle$drippinglava = new DripParticle.DrippingLava(pLevel, pX, pY, pZ, Fluids.LAVA, ParticleTypes.FALLING_LAVA);
         dripparticle$drippinglava.pickSprite(this.sprite);
         return dripparticle$drippinglava;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class DrippingObsidianTearFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public DrippingObsidianTearFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle.Dripping dripparticle$dripping = new DripParticle.Dripping(pLevel, pX, pY, pZ, Fluids.EMPTY, ParticleTypes.FALLING_OBSIDIAN_TEAR);
         dripparticle$dripping.isGlowing = true;
         dripparticle$dripping.gravity *= 0.01F;
         dripparticle$dripping.lifetime = 100;
         dripparticle$dripping.setColor(0.51171875F, 0.03125F, 0.890625F);
         dripparticle$dripping.pickSprite(this.sprite);
         return dripparticle$dripping;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class DrippingWaterFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public DrippingWaterFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle dripparticle = new DripParticle.Dripping(pLevel, pX, pY, pZ, Fluids.WATER, ParticleTypes.FALLING_WATER);
         dripparticle.setColor(0.2F, 0.3F, 1.0F);
         dripparticle.pickSprite(this.sprite);
         return dripparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class FallingHoneyFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public FallingHoneyFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle dripparticle = new DripParticle.FallingHoneyParticle(pLevel, pX, pY, pZ, Fluids.EMPTY, ParticleTypes.LANDING_HONEY);
         dripparticle.gravity = 0.01F;
         dripparticle.setColor(0.582F, 0.448F, 0.082F);
         dripparticle.pickSprite(this.sprite);
         return dripparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class FallingHoneyParticle extends DripParticle.FallingLiquidParticle {
      private FallingHoneyParticle(ClientWorld p_i232373_1_, double p_i232373_2_, double p_i232373_4_, double p_i232373_6_, Fluid p_i232373_8_, IParticleData p_i232373_9_) {
         super(p_i232373_1_, p_i232373_2_, p_i232373_4_, p_i232373_6_, p_i232373_8_, p_i232373_9_);
      }

      protected void postMoveUpdate() {
         if (this.onGround) {
            this.remove();
            this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
            this.level.playLocalSound(this.x + 0.5D, this.y, this.z + 0.5D, SoundEvents.BEEHIVE_DRIP, SoundCategory.BLOCKS, 0.3F + this.level.random.nextFloat() * 2.0F / 3.0F, 1.0F, false);
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class FallingLavaFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public FallingLavaFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle dripparticle = new DripParticle.FallingLiquidParticle(pLevel, pX, pY, pZ, Fluids.LAVA, ParticleTypes.LANDING_LAVA);
         dripparticle.setColor(1.0F, 0.2857143F, 0.083333336F);
         dripparticle.pickSprite(this.sprite);
         return dripparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class FallingLiquidParticle extends DripParticle.FallingNectarParticle {
      protected final IParticleData landParticle;

      private FallingLiquidParticle(ClientWorld pLevel, double pX, double pY, double pZ, Fluid pType, IParticleData pLandParticle) {
         super(pLevel, pX, pY, pZ, pType);
         this.landParticle = pLandParticle;
      }

      protected void postMoveUpdate() {
         if (this.onGround) {
            this.remove();
            this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class FallingNectarFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public FallingNectarFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle dripparticle = new DripParticle.FallingNectarParticle(pLevel, pX, pY, pZ, Fluids.EMPTY);
         dripparticle.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
         dripparticle.gravity = 0.007F;
         dripparticle.setColor(0.92F, 0.782F, 0.72F);
         dripparticle.pickSprite(this.sprite);
         return dripparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class FallingNectarParticle extends DripParticle {
      private FallingNectarParticle(ClientWorld p_i232371_1_, double p_i232371_2_, double p_i232371_4_, double p_i232371_6_, Fluid p_i232371_8_) {
         super(p_i232371_1_, p_i232371_2_, p_i232371_4_, p_i232371_6_, p_i232371_8_);
         this.lifetime = (int)(64.0D / (Math.random() * 0.8D + 0.2D));
      }

      protected void postMoveUpdate() {
         if (this.onGround) {
            this.remove();
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class FallingObsidianTearFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public FallingObsidianTearFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle dripparticle = new DripParticle.FallingLiquidParticle(pLevel, pX, pY, pZ, Fluids.EMPTY, ParticleTypes.LANDING_OBSIDIAN_TEAR);
         dripparticle.isGlowing = true;
         dripparticle.gravity = 0.01F;
         dripparticle.setColor(0.51171875F, 0.03125F, 0.890625F);
         dripparticle.pickSprite(this.sprite);
         return dripparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class FallingWaterFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public FallingWaterFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle dripparticle = new DripParticle.FallingLiquidParticle(pLevel, pX, pY, pZ, Fluids.WATER, ParticleTypes.SPLASH);
         dripparticle.setColor(0.2F, 0.3F, 1.0F);
         dripparticle.pickSprite(this.sprite);
         return dripparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class Landing extends DripParticle {
      private Landing(ClientWorld p_i232367_1_, double p_i232367_2_, double p_i232367_4_, double p_i232367_6_, Fluid p_i232367_8_) {
         super(p_i232367_1_, p_i232367_2_, p_i232367_4_, p_i232367_6_, p_i232367_8_);
         this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class LandingHoneyFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public LandingHoneyFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle dripparticle = new DripParticle.Landing(pLevel, pX, pY, pZ, Fluids.EMPTY);
         dripparticle.lifetime = (int)(128.0D / (Math.random() * 0.8D + 0.2D));
         dripparticle.setColor(0.522F, 0.408F, 0.082F);
         dripparticle.pickSprite(this.sprite);
         return dripparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class LandingLavaFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public LandingLavaFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle dripparticle = new DripParticle.Landing(pLevel, pX, pY, pZ, Fluids.LAVA);
         dripparticle.setColor(1.0F, 0.2857143F, 0.083333336F);
         dripparticle.pickSprite(this.sprite);
         return dripparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class LandingObsidianTearFactory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public LandingObsidianTearFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         DripParticle dripparticle = new DripParticle.Landing(pLevel, pX, pY, pZ, Fluids.EMPTY);
         dripparticle.isGlowing = true;
         dripparticle.lifetime = (int)(28.0D / (Math.random() * 0.8D + 0.2D));
         dripparticle.setColor(0.51171875F, 0.03125F, 0.890625F);
         dripparticle.pickSprite(this.sprite);
         return dripparticle;
      }
   }
}
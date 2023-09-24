package net.minecraft.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderSkybox {
   private final Minecraft minecraft;
   private final RenderSkyboxCube cubeMap;
   private float time;

   public RenderSkybox(RenderSkyboxCube p_i49377_1_) {
      this.cubeMap = p_i49377_1_;
      this.minecraft = Minecraft.getInstance();
   }

   public void render(float pDeltaT, float pAlpha) {
      this.time += pDeltaT;
      this.cubeMap.render(this.minecraft, MathHelper.sin(this.time * 0.001F) * 5.0F + 25.0F, -this.time * 0.1F, pAlpha);
   }
}
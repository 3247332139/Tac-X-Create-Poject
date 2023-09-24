package net.minecraft.client.resources;

import java.io.IOException;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ColorMapLoader {
   @Deprecated
   public static int[] getPixels(IResourceManager pManager, ResourceLocation pLocation) throws IOException {
      int[] aint;
      try (
         IResource iresource = pManager.getResource(pLocation);
         NativeImage nativeimage = NativeImage.read(iresource.getInputStream());
      ) {
         aint = nativeimage.makePixelArray();
      }

      return aint;
   }
}
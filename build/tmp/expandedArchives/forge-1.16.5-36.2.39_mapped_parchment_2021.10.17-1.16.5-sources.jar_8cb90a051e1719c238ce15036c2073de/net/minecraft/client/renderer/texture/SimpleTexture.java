package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SimpleTexture extends Texture {
   private static final Logger LOGGER = LogManager.getLogger();
   protected final ResourceLocation location;

   public SimpleTexture(ResourceLocation p_i1275_1_) {
      this.location = p_i1275_1_;
   }

   public void load(IResourceManager pManager) throws IOException {
      SimpleTexture.TextureData simpletexture$texturedata = this.getTextureImage(pManager);
      simpletexture$texturedata.throwIfError();
      TextureMetadataSection texturemetadatasection = simpletexture$texturedata.getTextureMetadata();
      boolean flag;
      boolean flag1;
      if (texturemetadatasection != null) {
         flag = texturemetadatasection.isBlur();
         flag1 = texturemetadatasection.isClamp();
      } else {
         flag = false;
         flag1 = false;
      }

      NativeImage nativeimage = simpletexture$texturedata.getImage();
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> {
            this.doLoad(nativeimage, flag, flag1);
         });
      } else {
         this.doLoad(nativeimage, flag, flag1);
      }

   }

   private void doLoad(NativeImage pImage, boolean pBlur, boolean pClamp) {
      TextureUtil.prepareImage(this.getId(), 0, pImage.getWidth(), pImage.getHeight());
      pImage.upload(0, 0, 0, 0, 0, pImage.getWidth(), pImage.getHeight(), pBlur, pClamp, false, true);
   }

   protected SimpleTexture.TextureData getTextureImage(IResourceManager pResourceManager) {
      return SimpleTexture.TextureData.load(pResourceManager, this.location);
   }

   @OnlyIn(Dist.CLIENT)
   public static class TextureData implements Closeable {
      @Nullable
      private final TextureMetadataSection metadata;
      @Nullable
      private final NativeImage image;
      @Nullable
      private final IOException exception;

      public TextureData(IOException p_i50473_1_) {
         this.exception = p_i50473_1_;
         this.metadata = null;
         this.image = null;
      }

      public TextureData(@Nullable TextureMetadataSection p_i50474_1_, NativeImage p_i50474_2_) {
         this.exception = null;
         this.metadata = p_i50474_1_;
         this.image = p_i50474_2_;
      }

      public static SimpleTexture.TextureData load(IResourceManager pResourceManager, ResourceLocation pLocation) {
         try (IResource iresource = pResourceManager.getResource(pLocation)) {
            NativeImage nativeimage = NativeImage.read(iresource.getInputStream());
            TextureMetadataSection texturemetadatasection = null;

            try {
               texturemetadatasection = iresource.getMetadata(TextureMetadataSection.SERIALIZER);
            } catch (RuntimeException runtimeexception) {
               SimpleTexture.LOGGER.warn("Failed reading metadata of: {}", pLocation, runtimeexception);
            }

            return new SimpleTexture.TextureData(texturemetadatasection, nativeimage);
         } catch (IOException ioexception) {
            return new SimpleTexture.TextureData(ioexception);
         }
      }

      @Nullable
      public TextureMetadataSection getTextureMetadata() {
         return this.metadata;
      }

      public NativeImage getImage() throws IOException {
         if (this.exception != null) {
            throw this.exception;
         } else {
            return this.image;
         }
      }

      public void close() {
         if (this.image != null) {
            this.image.close();
         }

      }

      public void throwIfError() throws IOException {
         if (this.exception != null) {
            throw this.exception;
         }
      }
   }
}
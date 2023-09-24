package net.minecraft.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IResourcePack extends AutoCloseable, net.minecraftforge.common.extensions.IForgeResourcePack {
   @OnlyIn(Dist.CLIENT)
   InputStream getRootResource(String pFileName) throws IOException;

   InputStream getResource(ResourcePackType pType, ResourceLocation pLocation) throws IOException;

   Collection<ResourceLocation> getResources(ResourcePackType pType, String pNamespace, String pPath, int pMaxDepth, Predicate<String> pFilter);

   boolean hasResource(ResourcePackType pType, ResourceLocation pLocation);

   Set<String> getNamespaces(ResourcePackType pType);

   @Nullable
   <T> T getMetadataSection(IMetadataSectionSerializer<T> pDeserializer) throws IOException;

   String getName();

   void close();
}

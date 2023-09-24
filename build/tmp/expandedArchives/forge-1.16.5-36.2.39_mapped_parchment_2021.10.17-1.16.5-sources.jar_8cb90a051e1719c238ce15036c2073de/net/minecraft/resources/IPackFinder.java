package net.minecraft.resources;

import java.util.function.Consumer;

public interface IPackFinder {
   void loadPacks(Consumer<ResourcePackInfo> pInfoConsumer, ResourcePackInfo.IFactory pInfoFactory);
}
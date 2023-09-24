package net.minecraft.util.registry;

import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPatternRegistry;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
import net.minecraft.world.gen.feature.template.ProcessorLists;
import net.minecraft.world.gen.feature.template.StructureProcessorList;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenRegistries {
   protected static final Logger LOGGER = LogManager.getLogger();
   private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.newLinkedHashMap();
   private static final MutableRegistry<MutableRegistry<?>> WRITABLE_REGISTRY = new SimpleRegistry<>(RegistryKey.createRegistryKey(new ResourceLocation("root")), Lifecycle.experimental());
   public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
   public static final Registry<ConfiguredSurfaceBuilder<?>> CONFIGURED_SURFACE_BUILDER = registerSimple(Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, () -> {
      return ConfiguredSurfaceBuilders.NOPE;
   });
   public static final Registry<ConfiguredCarver<?>> CONFIGURED_CARVER = registerSimple(Registry.CONFIGURED_CARVER_REGISTRY, () -> {
      return ConfiguredCarvers.CAVE;
   });
   public static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE = registerSimple(Registry.CONFIGURED_FEATURE_REGISTRY, () -> {
      return Features.OAK;
   });
   public static final Registry<StructureFeature<?, ?>> CONFIGURED_STRUCTURE_FEATURE = registerSimple(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, () -> {
      return StructureFeatures.MINESHAFT;
   });
   public static final Registry<StructureProcessorList> PROCESSOR_LIST = registerSimple(Registry.PROCESSOR_LIST_REGISTRY, () -> {
      return ProcessorLists.ZOMBIE_PLAINS;
   });
   public static final Registry<JigsawPattern> TEMPLATE_POOL = registerSimple(Registry.TEMPLATE_POOL_REGISTRY, JigsawPatternRegistry::bootstrap);
   @Deprecated public static final Registry<Biome> BIOME = forge(Registry.BIOME_REGISTRY, () -> {
      return BiomeRegistry.PLAINS;
   });
   public static final Registry<DimensionSettings> NOISE_GENERATOR_SETTINGS = registerSimple(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DimensionSettings::bootstrap);

   private static <T> Registry<T> registerSimple(RegistryKey<? extends Registry<T>> pRegistryKey, Supplier<T> pDefaultSupplier) {
      return registerSimple(pRegistryKey, Lifecycle.stable(), pDefaultSupplier);
   }

   private static <T extends net.minecraftforge.registries.IForgeRegistryEntry<T>> Registry<T> forge(RegistryKey<? extends Registry<T>> key, Supplier<T> def) {
      return internalRegister(key, net.minecraftforge.registries.GameData.getWrapper(key, Lifecycle.stable()), def, Lifecycle.stable());
   }

   /**
    * Creates a new simple registry and registers it
    */
   private static <T> Registry<T> registerSimple(RegistryKey<? extends Registry<T>> pRegistryKey, Lifecycle pLifecycle, Supplier<T> pDefaultSupplier) {
      return internalRegister(pRegistryKey, new SimpleRegistry<>(pRegistryKey, pLifecycle), pDefaultSupplier, pLifecycle);
   }

   /**
    * Registers the passed registry
    */
   private static <T, R extends MutableRegistry<T>> R internalRegister(RegistryKey<? extends Registry<T>> pRegistryKey, R pRegistry, Supplier<T> pDefaultSupplier, Lifecycle pLifecycle) {
      ResourceLocation resourcelocation = pRegistryKey.location();
      LOADERS.put(resourcelocation, pDefaultSupplier);
      MutableRegistry<R> mutableregistry = (MutableRegistry<R>)WRITABLE_REGISTRY;
      return (R)mutableregistry.register((RegistryKey)pRegistryKey, pRegistry, pLifecycle);
   }

   /**
    * Creates a new simple registry and registers it
    */
   public static <T> T register(Registry<? super T> pRegistry, String pId, T pValue) {
      return register(pRegistry, new ResourceLocation(pId), pValue);
   }

   /**
    * Registers the given value to the registry
    */
   public static <V, T extends V> T register(Registry<V> pRegistry, ResourceLocation pId, T pValue) {
      return ((MutableRegistry<V>)pRegistry).register(RegistryKey.create(pRegistry.key(), pId), pValue, Lifecycle.stable());
   }

   /**
    * Registers the given value to the registry
    */
   public static <V, T extends V> T registerMapping(Registry<V> pRegistry, int pIndex, RegistryKey<V> pRegistryKey, T pValue) {
      return ((MutableRegistry<V>)pRegistry).registerMapping(pIndex, pRegistryKey, pValue, Lifecycle.stable());
   }

   /**
    * Dummy method to ensure all static variables are loaded before Registry loads registries.
    */
   public static void bootstrap() {
   }

   static {
      LOADERS.forEach((p_243668_0_, p_243668_1_) -> {
         if (p_243668_1_.get() == null) {
            LOGGER.error("Unable to bootstrap registry '{}'", (Object)p_243668_0_);
         }

      });
      Registry.checkRegistry(WRITABLE_REGISTRY);
   }
}

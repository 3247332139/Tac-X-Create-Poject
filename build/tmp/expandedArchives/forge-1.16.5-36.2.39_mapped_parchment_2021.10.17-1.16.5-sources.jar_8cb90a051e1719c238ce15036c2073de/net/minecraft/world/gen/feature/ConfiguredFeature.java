package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKeyCodec;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.IDecoratable;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfiguredFeature<FC extends IFeatureConfig, F extends Feature<FC>> implements IDecoratable<ConfiguredFeature<?, ?>> {
   public static final Codec<ConfiguredFeature<?, ?>> DIRECT_CODEC = Registry.FEATURE.dispatch((p_236266_0_) -> {
      return p_236266_0_.feature;
   }, Feature::configuredCodec);
   public static final Codec<Supplier<ConfiguredFeature<?, ?>>> CODEC = RegistryKeyCodec.create(Registry.CONFIGURED_FEATURE_REGISTRY, DIRECT_CODEC);
   public static final Codec<List<Supplier<ConfiguredFeature<?, ?>>>> LIST_CODEC = RegistryKeyCodec.homogeneousList(Registry.CONFIGURED_FEATURE_REGISTRY, DIRECT_CODEC);
   public static final Logger LOGGER = LogManager.getLogger();
   public final F feature;
   public final FC config;

   public ConfiguredFeature(F pFeature, FC pConfig) {
      this.feature = pFeature;
      this.config = pConfig;
   }

   public F feature() {
      return this.feature;
   }

   public FC config() {
      return this.config;
   }

   public ConfiguredFeature<?, ?> decorated(ConfiguredPlacement<?> pDecorator) {
      return Feature.DECORATED.configured(new DecoratedFeatureConfig(() -> {
         return this;
      }, pDecorator));
   }

   public ConfiguredRandomFeatureList weighted(float pChance) {
      return new ConfiguredRandomFeatureList(this, pChance);
   }

   public boolean place(ISeedReader pReader, ChunkGenerator pChunkGenerator, Random pRandom, BlockPos pPos) {
      return this.feature.place(pReader, pChunkGenerator, pRandom, pPos, this.config);
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return Stream.concat(Stream.of(this), this.config.getFeatures());
   }
}
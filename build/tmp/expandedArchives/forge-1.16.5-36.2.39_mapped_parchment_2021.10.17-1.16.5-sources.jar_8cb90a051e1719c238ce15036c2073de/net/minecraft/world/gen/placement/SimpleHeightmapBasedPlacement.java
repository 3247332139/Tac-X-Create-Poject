package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public abstract class SimpleHeightmapBasedPlacement<DC extends IPlacementConfig> extends HeightmapBasedPlacement<DC> {
   public SimpleHeightmapBasedPlacement(Codec<DC> p_i242013_1_) {
      super(p_i242013_1_);
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, DC pConfig, BlockPos pPos) {
      int i = pPos.getX();
      int j = pPos.getZ();
      int k = pHelper.getHeight(this.type(pConfig), i, j);
      return k > 0 ? Stream.of(new BlockPos(i, k, j)) : Stream.of();
   }
}
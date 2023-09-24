package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class CaveEdge extends Placement<CaveEdgeConfig> {
   public CaveEdge(Codec<CaveEdgeConfig> p_i232065_1_) {
      super(p_i232065_1_);
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pHelper, Random pRandom, CaveEdgeConfig pConfig, BlockPos pPos) {
      ChunkPos chunkpos = new ChunkPos(pPos);
      BitSet bitset = pHelper.getCarvingMask(chunkpos, pConfig.step);
      return IntStream.range(0, bitset.length()).filter((p_215067_3_) -> {
         return bitset.get(p_215067_3_) && pRandom.nextFloat() < pConfig.probability;
      }).mapToObj((p_215068_1_) -> {
         int i = p_215068_1_ & 15;
         int j = p_215068_1_ >> 4 & 15;
         int k = p_215068_1_ >> 8;
         return new BlockPos(chunkpos.getMinBlockX() + i, k, chunkpos.getMinBlockZ() + j);
      });
   }
}
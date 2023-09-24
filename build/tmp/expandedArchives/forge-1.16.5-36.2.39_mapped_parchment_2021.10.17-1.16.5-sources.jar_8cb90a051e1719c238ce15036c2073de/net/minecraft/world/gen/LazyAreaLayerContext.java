package net.minecraft.world.gen;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.Random;
import net.minecraft.util.FastRandom;
import net.minecraft.world.gen.area.LazyArea;
import net.minecraft.world.gen.layer.traits.IPixelTransformer;

public class LazyAreaLayerContext implements IExtendedNoiseRandom<LazyArea> {
   private final Long2IntLinkedOpenHashMap cache;
   private final int maxCache;
   private final ImprovedNoiseGenerator biomeNoise;
   private final long seed;
   private long rval;

   public LazyAreaLayerContext(int p_i51285_1_, long p_i51285_2_, long p_i51285_4_) {
      this.seed = mixSeed(p_i51285_2_, p_i51285_4_);
      this.biomeNoise = new ImprovedNoiseGenerator(new Random(p_i51285_2_));
      this.cache = new Long2IntLinkedOpenHashMap(16, 0.25F);
      this.cache.defaultReturnValue(Integer.MIN_VALUE);
      this.maxCache = p_i51285_1_;
   }

   public LazyArea createResult(IPixelTransformer pPixelTransformer) {
      return new LazyArea(this.cache, this.maxCache, pPixelTransformer);
   }

   public LazyArea createResult(IPixelTransformer pPixelTransformer, LazyArea pArea) {
      return new LazyArea(this.cache, Math.min(1024, pArea.getMaxCache() * 4), pPixelTransformer);
   }

   public LazyArea createResult(IPixelTransformer p_212860_1_, LazyArea pFirstArea, LazyArea pSecondArea) {
      return new LazyArea(this.cache, Math.min(1024, Math.max(pFirstArea.getMaxCache(), pSecondArea.getMaxCache()) * 4), p_212860_1_);
   }

   public void initRandom(long pX, long pZ) {
      long i = this.seed;
      i = FastRandom.next(i, pX);
      i = FastRandom.next(i, pZ);
      i = FastRandom.next(i, pX);
      i = FastRandom.next(i, pZ);
      this.rval = i;
   }

   public int nextRandom(int pBound) {
      int i = (int)Math.floorMod(this.rval >> 24, (long)pBound);
      this.rval = FastRandom.next(this.rval, this.seed);
      return i;
   }

   public ImprovedNoiseGenerator getBiomeNoise() {
      return this.biomeNoise;
   }

   private static long mixSeed(long pLeft, long pRight) {
      long lvt_4_1_ = FastRandom.next(pRight, pRight);
      lvt_4_1_ = FastRandom.next(lvt_4_1_, pRight);
      lvt_4_1_ = FastRandom.next(lvt_4_1_, pRight);
      long lvt_6_1_ = FastRandom.next(pLeft, lvt_4_1_);
      lvt_6_1_ = FastRandom.next(lvt_6_1_, lvt_4_1_);
      return FastRandom.next(lvt_6_1_, lvt_4_1_);
   }
}
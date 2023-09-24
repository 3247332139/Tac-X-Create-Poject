package net.minecraft.world.gen.layer.traits;

import net.minecraft.world.gen.IExtendedNoiseRandom;
import net.minecraft.world.gen.area.IArea;
import net.minecraft.world.gen.area.IAreaFactory;

public interface IAreaTransformer1 extends IDimTransformer {
   default <R extends IArea> IAreaFactory<R> run(IExtendedNoiseRandom<R> pContext, IAreaFactory<R> pAreaFactory) {
      return () -> {
         R r = pAreaFactory.make();
         return pContext.createResult((p_202711_3_, p_202711_4_) -> {
            pContext.initRandom((long)p_202711_3_, (long)p_202711_4_);
            return this.applyPixel(pContext, r, p_202711_3_, p_202711_4_);
         }, r);
      };
   }

   int applyPixel(IExtendedNoiseRandom<?> pContext, IArea pArea, int pX, int pZ);
}
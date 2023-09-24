package net.minecraft.world.gen.layer.traits;

import net.minecraft.world.gen.IExtendedNoiseRandom;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.area.IArea;
import net.minecraft.world.gen.area.IAreaFactory;

public interface IAreaTransformer2 extends IDimTransformer {
   default <R extends IArea> IAreaFactory<R> run(IExtendedNoiseRandom<R> pContext, IAreaFactory<R> pAreaFactory, IAreaFactory<R> pAreaFactoryConflicting) {
      return () -> {
         R r = pAreaFactory.make();
         R r1 = pAreaFactoryConflicting.make();
         return pContext.createResult((p_215724_4_, p_215724_5_) -> {
            pContext.initRandom((long)p_215724_4_, (long)p_215724_5_);
            return this.applyPixel(pContext, r, r1, p_215724_4_, p_215724_5_);
         }, r, r1);
      };
   }

   int applyPixel(INoiseRandom p_215723_1_, IArea p_215723_2_, IArea p_215723_3_, int p_215723_4_, int p_215723_5_);
}
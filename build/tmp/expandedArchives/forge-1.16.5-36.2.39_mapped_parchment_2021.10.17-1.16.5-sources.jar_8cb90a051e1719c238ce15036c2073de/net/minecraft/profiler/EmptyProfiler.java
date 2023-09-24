package net.minecraft.profiler;

import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EmptyProfiler implements IResultableProfiler {
   public static final EmptyProfiler INSTANCE = new EmptyProfiler();

   private EmptyProfiler() {
   }

   public void startTick() {
   }

   public void endTick() {
   }

   /**
    * Start section
    */
   public void push(String pName) {
   }

   public void push(Supplier<String> pNameSupplier) {
   }

   /**
    * End section
    */
   public void pop() {
   }

   public void popPush(String pName) {
   }

   @OnlyIn(Dist.CLIENT)
   public void popPush(Supplier<String> pNameSupplier) {
   }

   public void incrementCounter(String pEntryId) {
   }

   public void incrementCounter(Supplier<String> pEntryIdSupplier) {
   }

   public IProfileResult getResults() {
      return EmptyProfileResult.EMPTY;
   }
}
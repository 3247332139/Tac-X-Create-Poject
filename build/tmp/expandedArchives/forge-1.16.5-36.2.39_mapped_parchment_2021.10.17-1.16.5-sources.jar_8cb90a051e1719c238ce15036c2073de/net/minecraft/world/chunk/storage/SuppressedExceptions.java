package net.minecraft.world.chunk.storage;

import javax.annotation.Nullable;

public class SuppressedExceptions<T extends Throwable> {
   @Nullable
   private T result;

   public void add(T pException) {
      if (this.result == null) {
         this.result = pException;
      } else {
         this.result.addSuppressed(pException);
      }

   }

   public void throwIfPresent() throws T {
      if (this.result != null) {
         throw this.result;
      }
   }
}
package net.minecraft.util.registry;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;

public class DefaultedRegistry<T> extends SimpleRegistry<T> {
   /** The key of the default value. */
   private final ResourceLocation defaultKey;
   /** The default value for this registry, retrurned in the place of a null value. */
   private T defaultValue;

   public DefaultedRegistry(String pDefaultName, RegistryKey<? extends Registry<T>> pRegistryKey, Lifecycle pLifecycle) {
      super(pRegistryKey, pLifecycle);
      this.defaultKey = new ResourceLocation(pDefaultName);
   }

   public <V extends T> V registerMapping(int pId, RegistryKey<T> pName, V pInstance, Lifecycle pLifecycle) {
      if (this.defaultKey.equals(pName.location())) {
         this.defaultValue = (T)pInstance;
      }

      return super.registerMapping(pId, pName, pInstance, pLifecycle);
   }

   /**
    * Gets the integer ID we use to identify the given object.
    */
   public int getId(@Nullable T pValue) {
      int i = super.getId(pValue);
      return i == -1 ? super.getId(this.defaultValue) : i;
   }

   /**
    * Gets the name we use to identify the given object.
    */
   @Nonnull
   public ResourceLocation getKey(T pValue) {
      ResourceLocation resourcelocation = super.getKey(pValue);
      return resourcelocation == null ? this.defaultKey : resourcelocation;
   }

   @Nonnull
   public T get(@Nullable ResourceLocation pName) {
      T t = super.get(pName);
      return (T)(t == null ? this.defaultValue : t);
   }

   public Optional<T> getOptional(@Nullable ResourceLocation pId) {
      return Optional.ofNullable(super.get(pId));
   }

   @Nonnull
   public T byId(int pValue) {
      T t = super.byId(pValue);
      return (T)(t == null ? this.defaultValue : t);
   }

   @Nonnull
   public T getRandom(Random p_186801_1_) {
      T t = super.getRandom(p_186801_1_);
      return (T)(t == null ? this.defaultValue : t);
   }

   public ResourceLocation getDefaultKey() {
      return this.defaultKey;
   }
}
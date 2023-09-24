package net.minecraft.advancements.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class MinMaxBounds<T extends Number> {
   public static final SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(new TranslationTextComponent("argument.range.empty"));
   public static final SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(new TranslationTextComponent("argument.range.swapped"));
   protected final T min;
   protected final T max;

   protected MinMaxBounds(@Nullable T p_i49720_1_, @Nullable T p_i49720_2_) {
      this.min = p_i49720_1_;
      this.max = p_i49720_2_;
   }

   @Nullable
   public T getMin() {
      return this.min;
   }

   @Nullable
   public T getMax() {
      return this.max;
   }

   public boolean isAny() {
      return this.min == null && this.max == null;
   }

   public JsonElement serializeToJson() {
      if (this.isAny()) {
         return JsonNull.INSTANCE;
      } else if (this.min != null && this.min.equals(this.max)) {
         return new JsonPrimitive(this.min);
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.min != null) {
            jsonobject.addProperty("min", this.min);
         }

         if (this.max != null) {
            jsonobject.addProperty("max", this.max);
         }

         return jsonobject;
      }
   }

   protected static <T extends Number, R extends MinMaxBounds<T>> R fromJson(@Nullable JsonElement pElement, R pDefaultValue, BiFunction<JsonElement, String, T> pBiFunction, MinMaxBounds.IBoundFactory<T, R> pBoundedFactory) {
      if (pElement != null && !pElement.isJsonNull()) {
         if (JSONUtils.isNumberValue(pElement)) {
            T t2 = pBiFunction.apply(pElement, "value");
            return pBoundedFactory.create(t2, t2);
         } else {
            JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "value");
            T t = jsonobject.has("min") ? pBiFunction.apply(jsonobject.get("min"), "min") : null;
            T t1 = jsonobject.has("max") ? pBiFunction.apply(jsonobject.get("max"), "max") : null;
            return pBoundedFactory.create(t, t1);
         }
      } else {
         return pDefaultValue;
      }
   }

   protected static <T extends Number, R extends MinMaxBounds<T>> R fromReader(StringReader pReader, MinMaxBounds.IBoundReader<T, R> pMinMaxReader, Function<String, T> pValueFunction, Supplier<DynamicCommandExceptionType> pCommandExceptionSupplier, Function<T, T> pFunction) throws CommandSyntaxException {
      if (!pReader.canRead()) {
         throw ERROR_EMPTY.createWithContext(pReader);
      } else {
         int i = pReader.getCursor();

         try {
            T t = optionallyFormat(readNumber(pReader, pValueFunction, pCommandExceptionSupplier), pFunction);
            T t1;
            if (pReader.canRead(2) && pReader.peek() == '.' && pReader.peek(1) == '.') {
               pReader.skip();
               pReader.skip();
               t1 = optionallyFormat(readNumber(pReader, pValueFunction, pCommandExceptionSupplier), pFunction);
               if (t == null && t1 == null) {
                  throw ERROR_EMPTY.createWithContext(pReader);
               }
            } else {
               t1 = t;
            }

            if (t == null && t1 == null) {
               throw ERROR_EMPTY.createWithContext(pReader);
            } else {
               return pMinMaxReader.create(pReader, t, t1);
            }
         } catch (CommandSyntaxException commandsyntaxexception) {
            pReader.setCursor(i);
            throw new CommandSyntaxException(commandsyntaxexception.getType(), commandsyntaxexception.getRawMessage(), commandsyntaxexception.getInput(), i);
         }
      }
   }

   @Nullable
   private static <T extends Number> T readNumber(StringReader pReader, Function<String, T> pStringToValueFunction, Supplier<DynamicCommandExceptionType> pCommandExceptionSupplier) throws CommandSyntaxException {
      int i = pReader.getCursor();

      while(pReader.canRead() && isAllowedInputChat(pReader)) {
         pReader.skip();
      }

      String s = pReader.getString().substring(i, pReader.getCursor());
      if (s.isEmpty()) {
         return (T)null;
      } else {
         try {
            return pStringToValueFunction.apply(s);
         } catch (NumberFormatException numberformatexception) {
            throw pCommandExceptionSupplier.get().createWithContext(pReader, s);
         }
      }
   }

   private static boolean isAllowedInputChat(StringReader pReader) {
      char c0 = pReader.peek();
      if ((c0 < '0' || c0 > '9') && c0 != '-') {
         if (c0 != '.') {
            return false;
         } else {
            return !pReader.canRead(2) || pReader.peek(1) != '.';
         }
      } else {
         return true;
      }
   }

   @Nullable
   private static <T> T optionallyFormat(@Nullable T pValue, Function<T, T> pFormatterFunction) {
      return (T)(pValue == null ? null : pFormatterFunction.apply(pValue));
   }

   public static class FloatBound extends MinMaxBounds<Float> {
      public static final MinMaxBounds.FloatBound ANY = new MinMaxBounds.FloatBound((Float)null, (Float)null);
      private final Double minSq;
      private final Double maxSq;

      private static MinMaxBounds.FloatBound create(StringReader p_211352_0_, @Nullable Float p_211352_1_, @Nullable Float p_211352_2_) throws CommandSyntaxException {
         if (p_211352_1_ != null && p_211352_2_ != null && p_211352_1_ > p_211352_2_) {
            throw ERROR_SWAPPED.createWithContext(p_211352_0_);
         } else {
            return new MinMaxBounds.FloatBound(p_211352_1_, p_211352_2_);
         }
      }

      @Nullable
      private static Double squareOpt(@Nullable Float p_211350_0_) {
         return p_211350_0_ == null ? null : p_211350_0_.doubleValue() * p_211350_0_.doubleValue();
      }

      private FloatBound(@Nullable Float p_i49717_1_, @Nullable Float p_i49717_2_) {
         super(p_i49717_1_, p_i49717_2_);
         this.minSq = squareOpt(p_i49717_1_);
         this.maxSq = squareOpt(p_i49717_2_);
      }

      public static MinMaxBounds.FloatBound atLeast(float p_211355_0_) {
         return new MinMaxBounds.FloatBound(p_211355_0_, (Float)null);
      }

      public boolean matches(float p_211354_1_) {
         if (this.min != null && this.min > p_211354_1_) {
            return false;
         } else {
            return this.max == null || !(this.max < p_211354_1_);
         }
      }

      public boolean matchesSqr(double p_211351_1_) {
         if (this.minSq != null && this.minSq > p_211351_1_) {
            return false;
         } else {
            return this.maxSq == null || !(this.maxSq < p_211351_1_);
         }
      }

      public static MinMaxBounds.FloatBound fromJson(@Nullable JsonElement p_211356_0_) {
         return fromJson(p_211356_0_, ANY, JSONUtils::convertToFloat, MinMaxBounds.FloatBound::new);
      }

      public static MinMaxBounds.FloatBound fromReader(StringReader p_211357_0_) throws CommandSyntaxException {
         return fromReader(p_211357_0_, (p_211358_0_) -> {
            return p_211358_0_;
         });
      }

      public static MinMaxBounds.FloatBound fromReader(StringReader p_211353_0_, Function<Float, Float> p_211353_1_) throws CommandSyntaxException {
         return fromReader(p_211353_0_, MinMaxBounds.FloatBound::create, Float::parseFloat, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidFloat, p_211353_1_);
      }
   }

   @FunctionalInterface
   public interface IBoundFactory<T extends Number, R extends MinMaxBounds<T>> {
      R create(@Nullable T p_create_1_, @Nullable T p_create_2_);
   }

   @FunctionalInterface
   public interface IBoundReader<T extends Number, R extends MinMaxBounds<T>> {
      R create(StringReader p_create_1_, @Nullable T p_create_2_, @Nullable T p_create_3_) throws CommandSyntaxException;
   }

   public static class IntBound extends MinMaxBounds<Integer> {
      public static final MinMaxBounds.IntBound ANY = new MinMaxBounds.IntBound((Integer)null, (Integer)null);
      private final Long minSq;
      private final Long maxSq;

      private static MinMaxBounds.IntBound create(StringReader p_211338_0_, @Nullable Integer p_211338_1_, @Nullable Integer p_211338_2_) throws CommandSyntaxException {
         if (p_211338_1_ != null && p_211338_2_ != null && p_211338_1_ > p_211338_2_) {
            throw ERROR_SWAPPED.createWithContext(p_211338_0_);
         } else {
            return new MinMaxBounds.IntBound(p_211338_1_, p_211338_2_);
         }
      }

      @Nullable
      private static Long squareOpt(@Nullable Integer pValue) {
         return pValue == null ? null : pValue.longValue() * pValue.longValue();
      }

      private IntBound(@Nullable Integer p_i49716_1_, @Nullable Integer p_i49716_2_) {
         super(p_i49716_1_, p_i49716_2_);
         this.minSq = squareOpt(p_i49716_1_);
         this.maxSq = squareOpt(p_i49716_2_);
      }

      public static MinMaxBounds.IntBound exactly(int pValue) {
         return new MinMaxBounds.IntBound(pValue, pValue);
      }

      public static MinMaxBounds.IntBound atLeast(int pValue) {
         return new MinMaxBounds.IntBound(pValue, (Integer)null);
      }

      public boolean matches(int pValue) {
         if (this.min != null && this.min > pValue) {
            return false;
         } else {
            return this.max == null || this.max >= pValue;
         }
      }

      public static MinMaxBounds.IntBound fromJson(@Nullable JsonElement pElement) {
         return fromJson(pElement, ANY, JSONUtils::convertToInt, MinMaxBounds.IntBound::new);
      }

      public static MinMaxBounds.IntBound fromReader(StringReader pReader) throws CommandSyntaxException {
         return fromReader(pReader, (p_211346_0_) -> {
            return p_211346_0_;
         });
      }

      public static MinMaxBounds.IntBound fromReader(StringReader pReader, Function<Integer, Integer> pValueFunction) throws CommandSyntaxException {
         return fromReader(pReader, MinMaxBounds.IntBound::create, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, pValueFunction);
      }
   }
}
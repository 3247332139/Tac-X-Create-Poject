package net.minecraft.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeManager extends JsonReloadListener {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private static final Logger LOGGER = LogManager.getLogger();
   private Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes = ImmutableMap.of();
   private boolean hasErrors;

   public RecipeManager() {
      super(GSON, "recipes");
   }

   protected void apply(Map<ResourceLocation, JsonElement> pObject, IResourceManager pResourceManager, IProfiler pProfiler) {
      this.hasErrors = false;
      Map<IRecipeType<?>, Builder<ResourceLocation, IRecipe<?>>> map = Maps.newHashMap();

      for(Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
         ResourceLocation resourcelocation = entry.getKey();
         if (resourcelocation.getPath().startsWith("_")) continue; //Forge: filter anything beginning with "_" as it's used for metadata.

         try {
            if (entry.getValue().isJsonObject() && !net.minecraftforge.common.crafting.CraftingHelper.processConditions(entry.getValue().getAsJsonObject(), "conditions")) {
               LOGGER.debug("Skipping loading recipe {} as it's conditions were not met", resourcelocation);
               continue;
            }
            IRecipe<?> irecipe = fromJson(resourcelocation, JSONUtils.convertToJsonObject(entry.getValue(), "top element"));
            if (irecipe == null) {
               LOGGER.info("Skipping loading recipe {} as it's serializer returned null", resourcelocation);
               continue;
            }
            map.computeIfAbsent(irecipe.getType(), (p_223391_0_) -> {
               return ImmutableMap.builder();
            }).put(resourcelocation, irecipe);
         } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
            LOGGER.error("Parsing error loading recipe {}", resourcelocation, jsonparseexception);
         }
      }

      this.recipes = map.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (p_223400_0_) -> {
         return p_223400_0_.getValue().build();
      }));
      LOGGER.info("Loaded {} recipes", (int)map.size());
   }

   public <C extends IInventory, T extends IRecipe<C>> Optional<T> getRecipeFor(IRecipeType<T> pRecipeType, C pInventory, World pLevel) {
      return this.byType(pRecipeType).values().stream().flatMap((p_215372_3_) -> {
         return Util.toStream(pRecipeType.tryMatch(p_215372_3_, pLevel, pInventory));
      }).findFirst();
   }

   public <C extends IInventory, T extends IRecipe<C>> List<T> getAllRecipesFor(IRecipeType<T> pRecipeType) {
      return this.byType(pRecipeType).values().stream().map((p_241453_0_) -> {
         return (T) p_241453_0_;
      }).collect(Collectors.toList());
   }

   public <C extends IInventory, T extends IRecipe<C>> List<T> getRecipesFor(IRecipeType<T> pRecipeType, C pInventory, World pLevel) {
      return this.byType(pRecipeType).values().stream().flatMap((p_215380_3_) -> {
         return Util.toStream(pRecipeType.tryMatch(p_215380_3_, pLevel, pInventory));
      }).sorted(Comparator.comparing((p_215379_0_) -> {
         return p_215379_0_.getResultItem().getDescriptionId();
      })).collect(Collectors.toList());
   }

   private <C extends IInventory, T extends IRecipe<C>> Map<ResourceLocation, IRecipe<C>> byType(IRecipeType<T> pRecipeType) {
      return (Map)this.recipes.getOrDefault(pRecipeType, Collections.emptyMap());
   }

   public <C extends IInventory, T extends IRecipe<C>> NonNullList<ItemStack> getRemainingItemsFor(IRecipeType<T> pRecipeType, C pInventory, World pLevel) {
      Optional<T> optional = this.getRecipeFor(pRecipeType, pInventory, pLevel);
      if (optional.isPresent()) {
         return optional.get().getRemainingItems(pInventory);
      } else {
         NonNullList<ItemStack> nonnulllist = NonNullList.withSize(pInventory.getContainerSize(), ItemStack.EMPTY);

         for(int i = 0; i < nonnulllist.size(); ++i) {
            nonnulllist.set(i, pInventory.getItem(i));
         }

         return nonnulllist;
      }
   }

   public Optional<? extends IRecipe<?>> byKey(ResourceLocation pRecipeId) {
      return this.recipes.values().stream().map((p_215368_1_) -> {
         return p_215368_1_.get(pRecipeId);
      }).filter(Objects::nonNull).findFirst();
   }

   public Collection<IRecipe<?>> getRecipes() {
      return this.recipes.values().stream().flatMap((p_215376_0_) -> {
         return p_215376_0_.values().stream();
      }).collect(Collectors.toSet());
   }

   public Stream<ResourceLocation> getRecipeIds() {
      return this.recipes.values().stream().flatMap((p_215375_0_) -> {
         return p_215375_0_.keySet().stream();
      });
   }

   /**
    * Deserializes a recipe object from json data.
    */
   public static IRecipe<?> fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
      String s = JSONUtils.getAsString(pJson, "type");
      return Registry.RECIPE_SERIALIZER.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
         return new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'");
      }).fromJson(pRecipeId, pJson);
   }

   @OnlyIn(Dist.CLIENT)
   public void replaceRecipes(Iterable<IRecipe<?>> pRecipes) {
      this.hasErrors = false;
      Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> map = Maps.newHashMap();
      pRecipes.forEach((p_223392_1_) -> {
         Map<ResourceLocation, IRecipe<?>> map1 = map.computeIfAbsent(p_223392_1_.getType(), (p_223390_0_) -> {
            return Maps.newHashMap();
         });
         IRecipe<?> irecipe = map1.put(p_223392_1_.getId(), p_223392_1_);
         if (irecipe != null) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + p_223392_1_.getId());
         }
      });
      this.recipes = ImmutableMap.copyOf(map);
   }
}

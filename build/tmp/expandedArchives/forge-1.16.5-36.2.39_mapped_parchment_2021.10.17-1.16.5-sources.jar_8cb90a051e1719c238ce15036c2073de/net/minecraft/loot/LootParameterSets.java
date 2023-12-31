package net.minecraft.loot;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

/**
 * Registry for {@link LootContextParamSet}s.
 */
public class LootParameterSets {
   private static final BiMap<ResourceLocation, LootParameterSet> REGISTRY = HashBiMap.create();
   public static final LootParameterSet EMPTY = register("empty", (p_216249_0_) -> {
   });
   public static final LootParameterSet CHEST = register("chest", (p_216259_0_) -> {
      p_216259_0_.required(LootParameters.ORIGIN).optional(LootParameters.THIS_ENTITY);
      p_216259_0_.optional(LootParameters.KILLER_ENTITY); //Forge: Chest Minecarts can have killers.
   });
   public static final LootParameterSet COMMAND = register("command", (p_216250_0_) -> {
      p_216250_0_.required(LootParameters.ORIGIN).optional(LootParameters.THIS_ENTITY);
   });
   public static final LootParameterSet SELECTOR = register("selector", (p_216254_0_) -> {
      p_216254_0_.required(LootParameters.ORIGIN).required(LootParameters.THIS_ENTITY);
   });
   public static final LootParameterSet FISHING = register("fishing", (p_216258_0_) -> {
      p_216258_0_.required(LootParameters.ORIGIN).required(LootParameters.TOOL).optional(LootParameters.THIS_ENTITY);
      p_216258_0_.optional(LootParameters.KILLER_ENTITY).optional(LootParameters.THIS_ENTITY); //Forge: Allow fisher, and bobber
   });
   public static final LootParameterSet ENTITY = register("entity", (p_216251_0_) -> {
      p_216251_0_.required(LootParameters.THIS_ENTITY).required(LootParameters.ORIGIN).required(LootParameters.DAMAGE_SOURCE).optional(LootParameters.KILLER_ENTITY).optional(LootParameters.DIRECT_KILLER_ENTITY).optional(LootParameters.LAST_DAMAGE_PLAYER);
   });
   public static final LootParameterSet GIFT = register("gift", (p_216255_0_) -> {
      p_216255_0_.required(LootParameters.ORIGIN).required(LootParameters.THIS_ENTITY);
   });
   public static final LootParameterSet PIGLIN_BARTER = register("barter", (p_216252_0_) -> {
      p_216252_0_.required(LootParameters.THIS_ENTITY);
   });
   public static final LootParameterSet ADVANCEMENT_REWARD = register("advancement_reward", (p_227560_0_) -> {
      p_227560_0_.required(LootParameters.THIS_ENTITY).required(LootParameters.ORIGIN);
   });
   public static final LootParameterSet ADVANCEMENT_ENTITY = register("advancement_entity", (p_227559_0_) -> {
      p_227559_0_.required(LootParameters.THIS_ENTITY).required(LootParameters.ORIGIN);
   });
   public static final LootParameterSet ALL_PARAMS = register("generic", (p_237456_0_) -> {
      p_237456_0_.required(LootParameters.THIS_ENTITY).required(LootParameters.LAST_DAMAGE_PLAYER).required(LootParameters.DAMAGE_SOURCE).required(LootParameters.KILLER_ENTITY).required(LootParameters.DIRECT_KILLER_ENTITY).required(LootParameters.ORIGIN).required(LootParameters.BLOCK_STATE).required(LootParameters.BLOCK_ENTITY).required(LootParameters.TOOL).required(LootParameters.EXPLOSION_RADIUS);
   });
   public static final LootParameterSet BLOCK = register("block", (p_237455_0_) -> {
      p_237455_0_.required(LootParameters.BLOCK_STATE).required(LootParameters.ORIGIN).required(LootParameters.TOOL).optional(LootParameters.THIS_ENTITY).optional(LootParameters.BLOCK_ENTITY).optional(LootParameters.EXPLOSION_RADIUS);
   });

   private static LootParameterSet register(String pRegistryName, Consumer<LootParameterSet.Builder> pBuilderConsumer) {
      LootParameterSet.Builder lootparameterset$builder = new LootParameterSet.Builder();
      pBuilderConsumer.accept(lootparameterset$builder);
      LootParameterSet lootparameterset = lootparameterset$builder.build();
      ResourceLocation resourcelocation = new ResourceLocation(pRegistryName);
      LootParameterSet lootparameterset1 = REGISTRY.put(resourcelocation, lootparameterset);
      if (lootparameterset1 != null) {
         throw new IllegalStateException("Loot table parameter set " + resourcelocation + " is already registered");
      } else {
         return lootparameterset;
      }
   }

   @Nullable
   public static LootParameterSet get(ResourceLocation pRegistryName) {
      return REGISTRY.get(pRegistryName);
   }

   @Nullable
   public static ResourceLocation getKey(LootParameterSet pParameterSet) {
      return REGISTRY.inverse().get(pParameterSet);
   }
}

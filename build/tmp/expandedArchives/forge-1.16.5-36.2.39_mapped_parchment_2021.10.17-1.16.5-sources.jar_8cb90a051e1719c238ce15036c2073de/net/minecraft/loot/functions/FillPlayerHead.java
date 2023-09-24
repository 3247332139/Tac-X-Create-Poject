package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.authlib.GameProfile;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.JSONUtils;

/**
 * LootItemFunction that applies the {@code "SkullOwner"} NBT tag to any player heads based on the given {@link
 * LootContext.EntityTarget}.
 * If the given target does not resolve to a player, nothing happens.
 */
public class FillPlayerHead extends LootFunction {
   private final LootContext.EntityTarget entityTarget;

   public FillPlayerHead(ILootCondition[] pConditions, LootContext.EntityTarget pEntityTarget) {
      super(pConditions);
      this.entityTarget = pEntityTarget;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.FILL_PLAYER_HEAD;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(this.entityTarget.getParam());
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.getItem() == Items.PLAYER_HEAD) {
         Entity entity = pContext.getParamOrNull(this.entityTarget.getParam());
         if (entity instanceof PlayerEntity) {
            GameProfile gameprofile = ((PlayerEntity)entity).getGameProfile();
            pStack.getOrCreateTag().put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), gameprofile));
         }
      }

      return pStack;
   }

   public static class Serializer extends LootFunction.Serializer<FillPlayerHead> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, FillPlayerHead pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.add("entity", pSerializationContext.serialize(pValue.entityTarget));
      }

      public FillPlayerHead deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         LootContext.EntityTarget lootcontext$entitytarget = JSONUtils.getAsObject(pObject, "entity", pDeserializationContext, LootContext.EntityTarget.class);
         return new FillPlayerHead(pConditions, lootcontext$entitytarget);
      }
   }
}
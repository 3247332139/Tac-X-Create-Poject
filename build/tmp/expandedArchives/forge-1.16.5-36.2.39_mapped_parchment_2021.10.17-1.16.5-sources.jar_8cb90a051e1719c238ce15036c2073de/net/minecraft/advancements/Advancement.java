package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;

public class Advancement {
   private final Advancement parent;
   private final DisplayInfo display;
   private final AdvancementRewards rewards;
   private final ResourceLocation id;
   private final Map<String, Criterion> criteria;
   private final String[][] requirements;
   private final Set<Advancement> children = Sets.newLinkedHashSet();
   private final ITextComponent chatComponent;

   public Advancement(ResourceLocation p_i47472_1_, @Nullable Advancement p_i47472_2_, @Nullable DisplayInfo p_i47472_3_, AdvancementRewards p_i47472_4_, Map<String, Criterion> p_i47472_5_, String[][] p_i47472_6_) {
      this.id = p_i47472_1_;
      this.display = p_i47472_3_;
      this.criteria = ImmutableMap.copyOf(p_i47472_5_);
      this.parent = p_i47472_2_;
      this.rewards = p_i47472_4_;
      this.requirements = p_i47472_6_;
      if (p_i47472_2_ != null) {
         p_i47472_2_.addChild(this);
      }

      if (p_i47472_3_ == null) {
         this.chatComponent = new StringTextComponent(p_i47472_1_.toString());
      } else {
         ITextComponent itextcomponent = p_i47472_3_.getTitle();
         TextFormatting textformatting = p_i47472_3_.getFrame().getChatColor();
         ITextComponent itextcomponent1 = TextComponentUtils.mergeStyles(itextcomponent.copy(), Style.EMPTY.withColor(textformatting)).append("\n").append(p_i47472_3_.getDescription());
         ITextComponent itextcomponent2 = itextcomponent.copy().withStyle((p_211567_1_) -> {
            return p_211567_1_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1));
         });
         this.chatComponent = TextComponentUtils.wrapInSquareBrackets(itextcomponent2).withStyle(textformatting);
      }

   }

   /**
    * Creates a new advancement builder with the data from this advancement
    */
   public Advancement.Builder deconstruct() {
      return new Advancement.Builder(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements);
   }

   /**
    * Get the {@code Advancement} that is this {@code Advancement}'s parent. This determines the tree structure that
    * appears in the {@linkplain GuiScreenAdvancements GUI}.
    * 
    * @return the parent {@code Advancement} of this {@code Advancement}, or {@code null} to signify that this {@code
    * Advancement} is a root with no parent.
    */
   @Nullable
   public Advancement getParent() {
      return this.parent;
   }

   /**
    * Get information that defines this {@code Advancement}'s appearance in GUIs.
    * 
    * @return information that defines this {@code Advancement}'s appearance in GUIs. If {@code null}, signifies an
    * invisible {@code Advancement}.
    */
   @Nullable
   public DisplayInfo getDisplay() {
      return this.display;
   }

   public AdvancementRewards getRewards() {
      return this.rewards;
   }

   public String toString() {
      return "SimpleAdvancement{id=" + this.getId() + ", parent=" + (this.parent == null ? "null" : this.parent.getId()) + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
   }

   /**
    * Get the children of this {@code Advancement}.
    * 
    * @return an {@code Iterable} of this {@code Advancement}'s children.
    * @see #getParent()
    */
   public Iterable<Advancement> getChildren() {
      return this.children;
   }

   /**
    * Get the {@link Criterion Criteria} used to decide the completion of this {@code Advancement}. Each key-value pair
    * consists of a {@code Criterion} and its name.
    * 
    * @return the criteria used to decide the completion of this {@code Advancement}
    * @see #getRequirements()
    */
   public Map<String, Criterion> getCriteria() {
      return this.criteria;
   }

   /**
    * Get how many requirements this {@code Advancement} has.
    * 
    * @return {@code this.getRequirements().length}
    * @see #getRequirements()
    */
   @OnlyIn(Dist.CLIENT)
   public int getMaxCriteraRequired() {
      return this.requirements.length;
   }

   /**
    * Add the given {@code Advancement} as a child of this {@code Advancement}.
    * 
    * @see #getParent()
    */
   public void addChild(Advancement pAdvancement) {
      this.children.add(pAdvancement);
   }

   /**
    * Get this {@code Advancement}'s unique identifier.
    * 
    * @return this {@code Advancement}'s unique identifier
    */
   public ResourceLocation getId() {
      return this.id;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof Advancement)) {
         return false;
      } else {
         Advancement advancement = (Advancement)p_equals_1_;
         return this.id.equals(advancement.id);
      }
   }

   public int hashCode() {
      return this.id.hashCode();
   }

   public String[][] getRequirements() {
      return this.requirements;
   }

   /**
    * Returns the {@code ITextComponent} that is shown in the chat message sent after this {@code Advancement} is
    * completed.
    * 
    * @return the {@code ITextComponent} that is shown in the chat message sent after this {@code Advancement} is
    * completed. If this {@code Advancement} is {@linkplain #getDisplay() invisible}, then it consists simply of {@link
    * #getId()}. Otherwise, it is the {@linkplain DisplayInfo#getTitle() title} inside square brackets, colored by the
    * {@linkplain net.minecraft.advancements.FrameType#getFormat frame type}, and hovering over it shows the {@linkplain
    * DisplayInfo#getDescription() description}.
    */
   public ITextComponent getChatComponent() {
      return this.chatComponent;
   }

   public static class Builder implements net.minecraftforge.common.extensions.IForgeAdvancementBuilder {
      private ResourceLocation parentId;
      private Advancement parent;
      private DisplayInfo display;
      private AdvancementRewards rewards = AdvancementRewards.EMPTY;
      private Map<String, Criterion> criteria = Maps.newLinkedHashMap();
      private String[][] requirements;
      private IRequirementsStrategy requirementsStrategy = IRequirementsStrategy.AND;

      private Builder(@Nullable ResourceLocation p_i47414_1_, @Nullable DisplayInfo p_i47414_2_, AdvancementRewards p_i47414_3_, Map<String, Criterion> p_i47414_4_, String[][] p_i47414_5_) {
         this.parentId = p_i47414_1_;
         this.display = p_i47414_2_;
         this.rewards = p_i47414_3_;
         this.criteria = p_i47414_4_;
         this.requirements = p_i47414_5_;
      }

      private Builder() {
      }

      public static Advancement.Builder advancement() {
         return new Advancement.Builder();
      }

      public Advancement.Builder parent(Advancement pParent) {
         this.parent = pParent;
         return this;
      }

      public Advancement.Builder parent(ResourceLocation pParentId) {
         this.parentId = pParentId;
         return this;
      }

      public Advancement.Builder display(ItemStack pStack, ITextComponent pTitle, ITextComponent pDescription, @Nullable ResourceLocation pBackground, FrameType pFrame, boolean pShowToast, boolean pAnnounceToChat, boolean pHidden) {
         return this.display(new DisplayInfo(pStack, pTitle, pDescription, pBackground, pFrame, pShowToast, pAnnounceToChat, pHidden));
      }

      public Advancement.Builder display(IItemProvider pItem, ITextComponent pTitle, ITextComponent pDescription, @Nullable ResourceLocation pBackground, FrameType pFrame, boolean pShowToast, boolean pAnnounceToChat, boolean pHidden) {
         return this.display(new DisplayInfo(new ItemStack(pItem.asItem()), pTitle, pDescription, pBackground, pFrame, pShowToast, pAnnounceToChat, pHidden));
      }

      public Advancement.Builder display(DisplayInfo pDisplay) {
         this.display = pDisplay;
         return this;
      }

      public Advancement.Builder rewards(AdvancementRewards.Builder pRewardsBuilder) {
         return this.rewards(pRewardsBuilder.build());
      }

      public Advancement.Builder rewards(AdvancementRewards pRewards) {
         this.rewards = pRewards;
         return this;
      }

      /**
       * Adds a criterion to the list of criteria
       */
      public Advancement.Builder addCriterion(String pKey, ICriterionInstance pCriterion) {
         return this.addCriterion(pKey, new Criterion(pCriterion));
      }

      /**
       * Adds a criterion to the list of criteria
       */
      public Advancement.Builder addCriterion(String pKey, Criterion pCriterion) {
         if (this.criteria.containsKey(pKey)) {
            throw new IllegalArgumentException("Duplicate criterion " + pKey);
         } else {
            this.criteria.put(pKey, pCriterion);
            return this;
         }
      }

      public Advancement.Builder requirements(IRequirementsStrategy pStrategy) {
         this.requirementsStrategy = pStrategy;
         return this;
      }

      /**
       * Tries to resolve the parent of this advancement, if possible. Returns true on success.
       */
      public boolean canBuild(Function<ResourceLocation, Advancement> pLookup) {
         if (this.parentId == null) {
            return true;
         } else {
            if (this.parent == null) {
               this.parent = pLookup.apply(this.parentId);
            }

            return this.parent != null;
         }
      }

      public Advancement build(ResourceLocation pId) {
         if (!this.canBuild((p_199750_0_) -> {
            return null;
         })) {
            throw new IllegalStateException("Tried to build incomplete advancement!");
         } else {
            if (this.requirements == null) {
               this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
            }

            return new Advancement(pId, this.parent, this.display, this.rewards, this.criteria, this.requirements);
         }
      }

      public Advancement save(Consumer<Advancement> pConsumer, String pId) {
         Advancement advancement = this.build(new ResourceLocation(pId));
         pConsumer.accept(advancement);
         return advancement;
      }

      public JsonObject serializeToJson() {
         if (this.requirements == null) {
            this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
         }

         JsonObject jsonobject = new JsonObject();
         if (this.parent != null) {
            jsonobject.addProperty("parent", this.parent.getId().toString());
         } else if (this.parentId != null) {
            jsonobject.addProperty("parent", this.parentId.toString());
         }

         if (this.display != null) {
            jsonobject.add("display", this.display.serializeToJson());
         }

         jsonobject.add("rewards", this.rewards.serializeToJson());
         JsonObject jsonobject1 = new JsonObject();

         for(Entry<String, Criterion> entry : this.criteria.entrySet()) {
            jsonobject1.add(entry.getKey(), entry.getValue().serializeToJson());
         }

         jsonobject.add("criteria", jsonobject1);
         JsonArray jsonarray1 = new JsonArray();

         for(String[] astring : this.requirements) {
            JsonArray jsonarray = new JsonArray();

            for(String s : astring) {
               jsonarray.add(s);
            }

            jsonarray1.add(jsonarray);
         }

         jsonobject.add("requirements", jsonarray1);
         return jsonobject;
      }

      public void serializeToNetwork(PacketBuffer pBuf) {
         if (this.parentId == null) {
            pBuf.writeBoolean(false);
         } else {
            pBuf.writeBoolean(true);
            pBuf.writeResourceLocation(this.parentId);
         }

         if (this.display == null) {
            pBuf.writeBoolean(false);
         } else {
            pBuf.writeBoolean(true);
            this.display.serializeToNetwork(pBuf);
         }

         Criterion.serializeToNetwork(this.criteria, pBuf);
         pBuf.writeVarInt(this.requirements.length);

         for(String[] astring : this.requirements) {
            pBuf.writeVarInt(astring.length);

            for(String s : astring) {
               pBuf.writeUtf(s);
            }
         }

      }

      public String toString() {
         return "Task Advancement{parentId=" + this.parentId + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
      }

      public static Advancement.Builder fromJson(JsonObject pJson, ConditionArrayParser pConditionParser) {
         if ((pJson = net.minecraftforge.common.crafting.ConditionalAdvancement.processConditional(pJson)) == null) return null;
         ResourceLocation resourcelocation = pJson.has("parent") ? new ResourceLocation(JSONUtils.getAsString(pJson, "parent")) : null;
         DisplayInfo displayinfo = pJson.has("display") ? DisplayInfo.fromJson(JSONUtils.getAsJsonObject(pJson, "display")) : null;
         AdvancementRewards advancementrewards = pJson.has("rewards") ? AdvancementRewards.deserialize(JSONUtils.getAsJsonObject(pJson, "rewards")) : AdvancementRewards.EMPTY;
         Map<String, Criterion> map = Criterion.criteriaFromJson(JSONUtils.getAsJsonObject(pJson, "criteria"), pConditionParser);
         if (map.isEmpty()) {
            throw new JsonSyntaxException("Advancement criteria cannot be empty");
         } else {
            JsonArray jsonarray = JSONUtils.getAsJsonArray(pJson, "requirements", new JsonArray());
            String[][] astring = new String[jsonarray.size()][];

            for(int i = 0; i < jsonarray.size(); ++i) {
               JsonArray jsonarray1 = JSONUtils.convertToJsonArray(jsonarray.get(i), "requirements[" + i + "]");
               astring[i] = new String[jsonarray1.size()];

               for(int j = 0; j < jsonarray1.size(); ++j) {
                  astring[i][j] = JSONUtils.convertToString(jsonarray1.get(j), "requirements[" + i + "][" + j + "]");
               }
            }

            if (astring.length == 0) {
               astring = new String[map.size()][];
               int k = 0;

               for(String s2 : map.keySet()) {
                  astring[k++] = new String[]{s2};
               }
            }

            for(String[] astring1 : astring) {
               if (astring1.length == 0 && map.isEmpty()) {
                  throw new JsonSyntaxException("Requirement entry cannot be empty");
               }

               for(String s : astring1) {
                  if (!map.containsKey(s)) {
                     throw new JsonSyntaxException("Unknown required criterion '" + s + "'");
                  }
               }
            }

            for(String s1 : map.keySet()) {
               boolean flag = false;

               for(String[] astring2 : astring) {
                  if (ArrayUtils.contains(astring2, s1)) {
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  throw new JsonSyntaxException("Criterion '" + s1 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
               }
            }

            return new Advancement.Builder(resourcelocation, displayinfo, advancementrewards, map, astring);
         }
      }

      public static Advancement.Builder fromNetwork(PacketBuffer pBuf) {
         ResourceLocation resourcelocation = pBuf.readBoolean() ? pBuf.readResourceLocation() : null;
         DisplayInfo displayinfo = pBuf.readBoolean() ? DisplayInfo.fromNetwork(pBuf) : null;
         Map<String, Criterion> map = Criterion.criteriaFromNetwork(pBuf);
         String[][] astring = new String[pBuf.readVarInt()][];

         for(int i = 0; i < astring.length; ++i) {
            astring[i] = new String[pBuf.readVarInt()];

            for(int j = 0; j < astring[i].length; ++j) {
               astring[i][j] = pBuf.readUtf(32767);
            }
         }

         return new Advancement.Builder(resourcelocation, displayinfo, AdvancementRewards.EMPTY, map, astring);
      }

      public Map<String, Criterion> getCriteria() {
         return this.criteria;
      }
   }
}

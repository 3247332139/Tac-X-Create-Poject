package net.minecraft.entity.ai.attributes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModifiableAttributeInstance {
   /** The Attribute this is an instance of */
   private final Attribute attribute;
   private final Map<AttributeModifier.Operation, Set<AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
   private final Map<UUID, AttributeModifier> modifierById = new Object2ObjectArrayMap<>();
   private final Set<AttributeModifier> permanentModifiers = new ObjectArraySet<>();
   private double baseValue;
   private boolean dirty = true;
   private double cachedValue;
   private final Consumer<ModifiableAttributeInstance> onDirty;

   public ModifiableAttributeInstance(Attribute p_i231501_1_, Consumer<ModifiableAttributeInstance> p_i231501_2_) {
      this.attribute = p_i231501_1_;
      this.onDirty = p_i231501_2_;
      this.baseValue = p_i231501_1_.getDefaultValue();
   }

   /**
    * Get the Attribute this is an instance of
    */
   public Attribute getAttribute() {
      return this.attribute;
   }

   public double getBaseValue() {
      return this.baseValue;
   }

   public void setBaseValue(double pBaseValue) {
      if (pBaseValue != this.baseValue) {
         this.baseValue = pBaseValue;
         this.setDirty();
      }
   }

   public Set<AttributeModifier> getModifiers(AttributeModifier.Operation pOperation) {
      return this.modifiersByOperation.computeIfAbsent(pOperation, (p_233768_0_) -> {
         return Sets.newHashSet();
      });
   }

   public Set<AttributeModifier> getModifiers() {
      return ImmutableSet.copyOf(this.modifierById.values());
   }

   /**
    * Returns attribute modifier, if any, by the given UUID
    */
   @Nullable
   public AttributeModifier getModifier(UUID pUuid) {
      return this.modifierById.get(pUuid);
   }

   public boolean hasModifier(AttributeModifier pModifier) {
      return this.modifierById.get(pModifier.getId()) != null;
   }

   private void addModifier(AttributeModifier pModifier) {
      AttributeModifier attributemodifier = this.modifierById.putIfAbsent(pModifier.getId(), pModifier);
      if (attributemodifier != null) {
         throw new IllegalArgumentException("Modifier is already applied on this attribute!");
      } else {
         this.getModifiers(pModifier.getOperation()).add(pModifier);
         this.setDirty();
      }
   }

   public void addTransientModifier(AttributeModifier pModifier) {
      this.addModifier(pModifier);
   }

   public void addPermanentModifier(AttributeModifier pModifier) {
      this.addModifier(pModifier);
      this.permanentModifiers.add(pModifier);
   }

   protected void setDirty() {
      this.dirty = true;
      this.onDirty.accept(this);
   }

   public void removeModifier(AttributeModifier pModifier) {
      this.getModifiers(pModifier.getOperation()).remove(pModifier);
      this.modifierById.remove(pModifier.getId());
      this.permanentModifiers.remove(pModifier);
      this.setDirty();
   }

   public void removeModifier(UUID pIdentifier) {
      AttributeModifier attributemodifier = this.getModifier(pIdentifier);
      if (attributemodifier != null) {
         this.removeModifier(attributemodifier);
      }

   }

   public boolean removePermanentModifier(UUID pIdentifier) {
      AttributeModifier attributemodifier = this.getModifier(pIdentifier);
      if (attributemodifier != null && this.permanentModifiers.contains(attributemodifier)) {
         this.removeModifier(attributemodifier);
         return true;
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void removeModifiers() {
      for(AttributeModifier attributemodifier : this.getModifiers()) {
         this.removeModifier(attributemodifier);
      }

   }

   public double getValue() {
      if (this.dirty) {
         this.cachedValue = this.calculateValue();
         this.dirty = false;
      }

      return this.cachedValue;
   }

   private double calculateValue() {
      double d0 = this.getBaseValue();

      for(AttributeModifier attributemodifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADDITION)) {
         d0 += attributemodifier.getAmount();
      }

      double d1 = d0;

      for(AttributeModifier attributemodifier1 : this.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_BASE)) {
         d1 += d0 * attributemodifier1.getAmount();
      }

      for(AttributeModifier attributemodifier2 : this.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
         d1 *= 1.0D + attributemodifier2.getAmount();
      }

      return this.attribute.sanitizeValue(d1);
   }

   private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation pOperation) {
      return this.modifiersByOperation.getOrDefault(pOperation, Collections.emptySet());
   }

   public void replaceFrom(ModifiableAttributeInstance pInstance) {
      this.baseValue = pInstance.baseValue;
      this.modifierById.clear();
      this.modifierById.putAll(pInstance.modifierById);
      this.permanentModifiers.clear();
      this.permanentModifiers.addAll(pInstance.permanentModifiers);
      this.modifiersByOperation.clear();
      pInstance.modifiersByOperation.forEach((p_233764_1_, p_233764_2_) -> {
         this.getModifiers(p_233764_1_).addAll(p_233764_2_);
      });
      this.setDirty();
   }

   public CompoundNBT save() {
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.putString("Name", Registry.ATTRIBUTE.getKey(this.attribute).toString());
      compoundnbt.putDouble("Base", this.baseValue);
      if (!this.permanentModifiers.isEmpty()) {
         ListNBT listnbt = new ListNBT();

         for(AttributeModifier attributemodifier : this.permanentModifiers) {
            listnbt.add(attributemodifier.save());
         }

         compoundnbt.put("Modifiers", listnbt);
      }

      return compoundnbt;
   }

   public void load(CompoundNBT pNbt) {
      this.baseValue = pNbt.getDouble("Base");
      if (pNbt.contains("Modifiers", 9)) {
         ListNBT listnbt = pNbt.getList("Modifiers", 10);

         for(int i = 0; i < listnbt.size(); ++i) {
            AttributeModifier attributemodifier = AttributeModifier.load(listnbt.getCompound(i));
            if (attributemodifier != null) {
               this.modifierById.put(attributemodifier.getId(), attributemodifier);
               this.getModifiers(attributemodifier.getOperation()).add(attributemodifier);
               this.permanentModifiers.add(attributemodifier);
            }
         }
      }

      this.setDirty();
   }
}
package net.minecraft.state.properties;

import net.minecraft.util.IStringSerializable;

public enum ComparatorMode implements IStringSerializable {
   COMPARE("compare"),
   SUBTRACT("subtract");

   private final String name;

   private ComparatorMode(String pName) {
      this.name = pName;
   }

   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }
}
package net.minecraft.state.properties;

import net.minecraft.util.IStringSerializable;

public enum RedstoneSide implements IStringSerializable {
   UP("up"),
   SIDE("side"),
   NONE("none");

   private final String name;

   private RedstoneSide(String pName) {
      this.name = pName;
   }

   public String toString() {
      return this.getSerializedName();
   }

   public String getSerializedName() {
      return this.name;
   }

   public boolean isConnected() {
      return this != NONE;
   }
}
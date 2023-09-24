package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface INBTType<T extends INBT> {
   T load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException;

   default boolean isValue() {
      return false;
   }

   String getName();

   String getPrettyName();

   static INBTType<EndNBT> createInvalid(final int pId) {
      return new INBTType<EndNBT>() {
         public EndNBT load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException {
            throw new IllegalArgumentException("Invalid tag id: " + pId);
         }

         public String getName() {
            return "INVALID[" + pId + "]";
         }

         public String getPrettyName() {
            return "UNKNOWN_" + pId;
         }
      };
   }
}
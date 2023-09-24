package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class StringNBT implements INBT {
   public static final INBTType<StringNBT> TYPE = new INBTType<StringNBT>() {
      public StringNBT load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException {
         pAccounter.accountBits(288L);
         String s = pInput.readUTF();
         pAccounter.readUTF(s);
         return StringNBT.valueOf(s);
      }

      public String getName() {
         return "STRING";
      }

      public String getPrettyName() {
         return "TAG_String";
      }

      public boolean isValue() {
         return true;
      }
   };
   private static final StringNBT EMPTY = new StringNBT("");
   private final String data;

   private StringNBT(String pData) {
      Objects.requireNonNull(pData, "Null string not allowed");
      this.data = pData;
   }

   public static StringNBT valueOf(String pData) {
      return pData.isEmpty() ? EMPTY : new StringNBT(pData);
   }

   /**
    * Write the actual data contents of the tag, implemented in NBT extension classes
    */
   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeUTF(this.data);
   }

   /**
    * Gets the type byte for the tag.
    */
   public byte getId() {
      return 8;
   }

   public INBTType<StringNBT> getType() {
      return TYPE;
   }

   public String toString() {
      return quoteAndEscape(this.data);
   }

   /**
    * Creates a clone of the tag.
    */
   public StringNBT copy() {
      return this;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else {
         return p_equals_1_ instanceof StringNBT && Objects.equals(this.data, ((StringNBT)p_equals_1_).data);
      }
   }

   public int hashCode() {
      return this.data.hashCode();
   }

   public String getAsString() {
      return this.data;
   }

   public ITextComponent getPrettyDisplay(String p_199850_1_, int p_199850_2_) {
      String s = quoteAndEscape(this.data);
      String s1 = s.substring(0, 1);
      ITextComponent itextcomponent = (new StringTextComponent(s.substring(1, s.length() - 1))).withStyle(SYNTAX_HIGHLIGHTING_STRING);
      return (new StringTextComponent(s1)).append(itextcomponent).append(s1);
   }

   public static String quoteAndEscape(String pText) {
      StringBuilder stringbuilder = new StringBuilder(" ");
      char c0 = 0;

      for(int i = 0; i < pText.length(); ++i) {
         char c1 = pText.charAt(i);
         if (c1 == '\\') {
            stringbuilder.append('\\');
         } else if (c1 == '"' || c1 == '\'') {
            if (c0 == 0) {
               c0 = (char)(c1 == '"' ? 39 : 34);
            }

            if (c0 == c1) {
               stringbuilder.append('\\');
            }
         }

         stringbuilder.append(c1);
      }

      if (c0 == 0) {
         c0 = '"';
      }

      stringbuilder.setCharAt(0, c0);
      stringbuilder.append(c0);
      return stringbuilder.toString();
   }
}

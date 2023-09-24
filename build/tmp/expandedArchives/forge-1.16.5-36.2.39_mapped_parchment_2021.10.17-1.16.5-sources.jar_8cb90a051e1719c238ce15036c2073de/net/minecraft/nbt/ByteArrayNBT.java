package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayNBT extends CollectionNBT<ByteNBT> {
   public static final INBTType<ByteArrayNBT> TYPE = new INBTType<ByteArrayNBT>() {
      public ByteArrayNBT load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException {
         pAccounter.accountBits(192L);
         int i = pInput.readInt();
         pAccounter.accountBits(8L * (long)i);
         byte[] abyte = new byte[i];
         pInput.readFully(abyte);
         return new ByteArrayNBT(abyte);
      }

      public String getName() {
         return "BYTE[]";
      }

      public String getPrettyName() {
         return "TAG_Byte_Array";
      }
   };
   private byte[] data;

   public ByteArrayNBT(byte[] pData) {
      this.data = pData;
   }

   public ByteArrayNBT(List<Byte> pDataList) {
      this(toArray(pDataList));
   }

   private static byte[] toArray(List<Byte> pDataList) {
      byte[] abyte = new byte[pDataList.size()];

      for(int i = 0; i < pDataList.size(); ++i) {
         Byte obyte = pDataList.get(i);
         abyte[i] = obyte == null ? 0 : obyte;
      }

      return abyte;
   }

   /**
    * Write the actual data contents of the tag, implemented in NBT extension classes
    */
   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeInt(this.data.length);
      pOutput.write(this.data);
   }

   /**
    * Gets the type byte for the tag.
    */
   public byte getId() {
      return 7;
   }

   public INBTType<ByteArrayNBT> getType() {
      return TYPE;
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder("[B;");

      for(int i = 0; i < this.data.length; ++i) {
         if (i != 0) {
            stringbuilder.append(',');
         }

         stringbuilder.append((int)this.data[i]).append('B');
      }

      return stringbuilder.append(']').toString();
   }

   /**
    * Creates a clone of the tag.
    */
   public INBT copy() {
      byte[] abyte = new byte[this.data.length];
      System.arraycopy(this.data, 0, abyte, 0, this.data.length);
      return new ByteArrayNBT(abyte);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else {
         return p_equals_1_ instanceof ByteArrayNBT && Arrays.equals(this.data, ((ByteArrayNBT)p_equals_1_).data);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.data);
   }

   public ITextComponent getPrettyDisplay(String p_199850_1_, int p_199850_2_) {
      ITextComponent itextcomponent = (new StringTextComponent("B")).withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      IFormattableTextComponent iformattabletextcomponent = (new StringTextComponent("[")).append(itextcomponent).append(";");

      for(int i = 0; i < this.data.length; ++i) {
         IFormattableTextComponent iformattabletextcomponent1 = (new StringTextComponent(String.valueOf((int)this.data[i]))).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
         iformattabletextcomponent.append(" ").append(iformattabletextcomponent1).append(itextcomponent);
         if (i != this.data.length - 1) {
            iformattabletextcomponent.append(",");
         }
      }

      iformattabletextcomponent.append("]");
      return iformattabletextcomponent;
   }

   public byte[] getAsByteArray() {
      return this.data;
   }

   public int size() {
      return this.data.length;
   }

   public ByteNBT get(int p_get_1_) {
      return ByteNBT.valueOf(this.data[p_get_1_]);
   }

   public ByteNBT set(int p_set_1_, ByteNBT p_set_2_) {
      byte b0 = this.data[p_set_1_];
      this.data[p_set_1_] = p_set_2_.getAsByte();
      return ByteNBT.valueOf(b0);
   }

   public void add(int p_add_1_, ByteNBT p_add_2_) {
      this.data = ArrayUtils.add(this.data, p_add_1_, p_add_2_.getAsByte());
   }

   public boolean setTag(int pIndex, INBT pTag) {
      if (pTag instanceof NumberNBT) {
         this.data[pIndex] = ((NumberNBT)pTag).getAsByte();
         return true;
      } else {
         return false;
      }
   }

   public boolean addTag(int pIndex, INBT pTag) {
      if (pTag instanceof NumberNBT) {
         this.data = ArrayUtils.add(this.data, pIndex, ((NumberNBT)pTag).getAsByte());
         return true;
      } else {
         return false;
      }
   }

   public ByteNBT remove(int p_remove_1_) {
      byte b0 = this.data[p_remove_1_];
      this.data = ArrayUtils.remove(this.data, p_remove_1_);
      return ByteNBT.valueOf(b0);
   }

   public byte getElementType() {
      return 1;
   }

   public void clear() {
      this.data = new byte[0];
   }
}
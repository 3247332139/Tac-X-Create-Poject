package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class DoubleNBT extends NumberNBT {
   public static final DoubleNBT ZERO = new DoubleNBT(0.0D);
   public static final INBTType<DoubleNBT> TYPE = new INBTType<DoubleNBT>() {
      public DoubleNBT load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException {
         pAccounter.accountBits(128L);
         return DoubleNBT.valueOf(pInput.readDouble());
      }

      public String getName() {
         return "DOUBLE";
      }

      public String getPrettyName() {
         return "TAG_Double";
      }

      public boolean isValue() {
         return true;
      }
   };
   private final double data;

   private DoubleNBT(double pData) {
      this.data = pData;
   }

   public static DoubleNBT valueOf(double pData) {
      return pData == 0.0D ? ZERO : new DoubleNBT(pData);
   }

   /**
    * Write the actual data contents of the tag, implemented in NBT extension classes
    */
   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeDouble(this.data);
   }

   /**
    * Gets the type byte for the tag.
    */
   public byte getId() {
      return 6;
   }

   public INBTType<DoubleNBT> getType() {
      return TYPE;
   }

   public String toString() {
      return this.data + "d";
   }

   /**
    * Creates a clone of the tag.
    */
   public DoubleNBT copy() {
      return this;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else {
         return p_equals_1_ instanceof DoubleNBT && this.data == ((DoubleNBT)p_equals_1_).data;
      }
   }

   public int hashCode() {
      long i = Double.doubleToLongBits(this.data);
      return (int)(i ^ i >>> 32);
   }

   public ITextComponent getPrettyDisplay(String p_199850_1_, int p_199850_2_) {
      ITextComponent itextcomponent = (new StringTextComponent("d")).withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      return (new StringTextComponent(String.valueOf(this.data))).append(itextcomponent).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   public long getAsLong() {
      return (long)Math.floor(this.data);
   }

   public int getAsInt() {
      return MathHelper.floor(this.data);
   }

   public short getAsShort() {
      return (short)(MathHelper.floor(this.data) & '\uffff');
   }

   public byte getAsByte() {
      return (byte)(MathHelper.floor(this.data) & 255);
   }

   public double getAsDouble() {
      return this.data;
   }

   public float getAsFloat() {
      return (float)this.data;
   }

   public Number getAsNumber() {
      return this.data;
   }
}
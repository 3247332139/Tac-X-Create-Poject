package net.minecraft.nbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;

public class CompressedStreamTools {
   public static CompoundNBT readCompressed(File pFile) throws IOException {
      CompoundNBT compoundnbt;
      try (InputStream inputstream = new FileInputStream(pFile)) {
         compoundnbt = readCompressed(inputstream);
      }

      return compoundnbt;
   }

   /**
    * Reads a compressed compound tag from a GNU zipped file.
    * @see #readCompressed(File)
    */
   public static CompoundNBT readCompressed(InputStream pZippedStream) throws IOException {
      CompoundNBT compoundnbt;
      try (DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(pZippedStream)))) {
         compoundnbt = read(datainputstream, NBTSizeTracker.UNLIMITED);
      }

      return compoundnbt;
   }

   public static void writeCompressed(CompoundNBT pCompoundTag, File pFile) throws IOException {
      try (OutputStream outputstream = new FileOutputStream(pFile)) {
         writeCompressed(pCompoundTag, outputstream);
      }

   }

   /**
    * Writes and compresses a compound tag to a GNU zipped file.
    * @see #writeCompressed(CompoundTag, File)
    */
   public static void writeCompressed(CompoundNBT pCompoundTag, OutputStream pOutputStream) throws IOException {
      try (DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(pOutputStream)))) {
         write(pCompoundTag, dataoutputstream);
      }

   }

   public static void write(CompoundNBT pCompoundTag, File pFile) throws IOException {
      try (
         FileOutputStream fileoutputstream = new FileOutputStream(pFile);
         DataOutputStream dataoutputstream = new DataOutputStream(fileoutputstream);
      ) {
         write(pCompoundTag, dataoutputstream);
      }

   }

   @Nullable
   public static CompoundNBT read(File pFile) throws IOException {
      if (!pFile.exists()) {
         return null;
      } else {
         CompoundNBT compoundnbt;
         try (
            FileInputStream fileinputstream = new FileInputStream(pFile);
            DataInputStream datainputstream = new DataInputStream(fileinputstream);
         ) {
            compoundnbt = read(datainputstream, NBTSizeTracker.UNLIMITED);
         }

         return compoundnbt;
      }
   }

   /**
    * Reads a compound tag from a file. The size of the file can be infinite.
    */
   public static CompoundNBT read(DataInput pInput) throws IOException {
      return read(pInput, NBTSizeTracker.UNLIMITED);
   }

   /**
    * Reads a compound tag from a file. The size of the file is limited by the {@code accounter}.
    * @throws RuntimeException if the size of the file is larger than the maximum amount of bytes specified by the
    * {@code accounter}
    */
   public static CompoundNBT read(DataInput pInput, NBTSizeTracker pAccounter) throws IOException {
      INBT inbt = readUnnamedTag(pInput, 0, pAccounter);
      if (inbt instanceof CompoundNBT) {
         return (CompoundNBT)inbt;
      } else {
         throw new IOException("Root tag must be a named compound tag");
      }
   }

   public static void write(CompoundNBT pCompoundTag, DataOutput pOutput) throws IOException {
      writeUnnamedTag(pCompoundTag, pOutput);
   }

   private static void writeUnnamedTag(INBT pTag, DataOutput pOutput) throws IOException {
      pOutput.writeByte(pTag.getId());
      if (pTag.getId() != 0) {
         pOutput.writeUTF("");
         pTag.write(pOutput);
      }
   }

   private static INBT readUnnamedTag(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException {
      byte b0 = pInput.readByte();
      pAccounter.accountBits(8); // Forge: Count everything!
      if (b0 == 0) {
         return EndNBT.INSTANCE;
      } else {
         pAccounter.readUTF(pInput.readUTF()); //Forge: Count this string.
         pAccounter.accountBits(32); //Forge: 4 extra bytes for the object allocation.

         try {
            return NBTTypes.getType(b0).load(pInput, pDepth, pAccounter);
         } catch (IOException ioexception) {
            CrashReport crashreport = CrashReport.forThrowable(ioexception, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.addCategory("NBT Tag");
            crashreportcategory.setDetail("Tag type", b0);
            throw new ReportedException(crashreport);
         }
      }
   }
}

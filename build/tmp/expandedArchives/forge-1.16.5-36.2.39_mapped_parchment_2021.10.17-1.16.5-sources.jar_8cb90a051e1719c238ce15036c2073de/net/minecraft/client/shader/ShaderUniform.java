package net.minecraft.client.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class ShaderUniform extends ShaderDefault implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private int location;
   private final int count;
   private final int type;
   private final IntBuffer intValues;
   private final FloatBuffer floatValues;
   private final String name;
   private boolean dirty;
   private final IShaderManager parent;

   public ShaderUniform(String p_i45092_1_, int p_i45092_2_, int p_i45092_3_, IShaderManager p_i45092_4_) {
      this.name = p_i45092_1_;
      this.count = p_i45092_3_;
      this.type = p_i45092_2_;
      this.parent = p_i45092_4_;
      if (p_i45092_2_ <= 3) {
         this.intValues = MemoryUtil.memAllocInt(p_i45092_3_);
         this.floatValues = null;
      } else {
         this.intValues = null;
         this.floatValues = MemoryUtil.memAllocFloat(p_i45092_3_);
      }

      this.location = -1;
      this.markDirty();
   }

   public static int glGetUniformLocation(int pProgram, CharSequence pName) {
      return GlStateManager._glGetUniformLocation(pProgram, pName);
   }

   public static void uploadInteger(int pLocation, int pValue) {
      RenderSystem.glUniform1i(pLocation, pValue);
   }

   public static int glGetAttribLocation(int pProgram, CharSequence pName) {
      return GlStateManager._glGetAttribLocation(pProgram, pName);
   }

   public void close() {
      if (this.intValues != null) {
         MemoryUtil.memFree(this.intValues);
      }

      if (this.floatValues != null) {
         MemoryUtil.memFree(this.floatValues);
      }

   }

   private void markDirty() {
      this.dirty = true;
      if (this.parent != null) {
         this.parent.markDirty();
      }

   }

   public static int getTypeFromString(String pTypeName) {
      int i = -1;
      if ("int".equals(pTypeName)) {
         i = 0;
      } else if ("float".equals(pTypeName)) {
         i = 4;
      } else if (pTypeName.startsWith("matrix")) {
         if (pTypeName.endsWith("2x2")) {
            i = 8;
         } else if (pTypeName.endsWith("3x3")) {
            i = 9;
         } else if (pTypeName.endsWith("4x4")) {
            i = 10;
         }
      }

      return i;
   }

   public void setLocation(int pLocation) {
      this.location = pLocation;
   }

   public String getName() {
      return this.name;
   }

   public void set(float pX) {
      ((Buffer)this.floatValues).position(0);
      this.floatValues.put(0, pX);
      this.markDirty();
   }

   public void set(float pX, float pY) {
      ((Buffer)this.floatValues).position(0);
      this.floatValues.put(0, pX);
      this.floatValues.put(1, pY);
      this.markDirty();
   }

   public void set(float pX, float pY, float pZ) {
      ((Buffer)this.floatValues).position(0);
      this.floatValues.put(0, pX);
      this.floatValues.put(1, pY);
      this.floatValues.put(2, pZ);
      this.markDirty();
   }

   public void set(float pX, float pY, float pZ, float pW) {
      ((Buffer)this.floatValues).position(0);
      this.floatValues.put(pX);
      this.floatValues.put(pY);
      this.floatValues.put(pZ);
      this.floatValues.put(pW);
      ((Buffer)this.floatValues).flip();
      this.markDirty();
   }

   public void setSafe(float pX, float pY, float pZ, float pW) {
      ((Buffer)this.floatValues).position(0);
      if (this.type >= 4) {
         this.floatValues.put(0, pX);
      }

      if (this.type >= 5) {
         this.floatValues.put(1, pY);
      }

      if (this.type >= 6) {
         this.floatValues.put(2, pZ);
      }

      if (this.type >= 7) {
         this.floatValues.put(3, pW);
      }

      this.markDirty();
   }

   public void setSafe(int pX, int pY, int pZ, int pW) {
      ((Buffer)this.intValues).position(0);
      if (this.type >= 0) {
         this.intValues.put(0, pX);
      }

      if (this.type >= 1) {
         this.intValues.put(1, pY);
      }

      if (this.type >= 2) {
         this.intValues.put(2, pZ);
      }

      if (this.type >= 3) {
         this.intValues.put(3, pW);
      }

      this.markDirty();
   }

   public void set(float[] pValueArray) {
      if (pValueArray.length < this.count) {
         LOGGER.warn("Uniform.set called with a too-small value array (expected {}, got {}). Ignoring.", this.count, pValueArray.length);
      } else {
         ((Buffer)this.floatValues).position(0);
         this.floatValues.put(pValueArray);
         ((Buffer)this.floatValues).position(0);
         this.markDirty();
      }
   }

   public void set(Matrix4f pMatrix) {
      ((Buffer)this.floatValues).position(0);
      pMatrix.store(this.floatValues);
      this.markDirty();
   }

   public void upload() {
      if (!this.dirty) {
      }

      this.dirty = false;
      if (this.type <= 3) {
         this.uploadAsInteger();
      } else if (this.type <= 7) {
         this.uploadAsFloat();
      } else {
         if (this.type > 10) {
            LOGGER.warn("Uniform.upload called, but type value ({}) is not a valid type. Ignoring.", (int)this.type);
            return;
         }

         this.uploadAsMatrix();
      }

   }

   private void uploadAsInteger() {
      ((Buffer)this.floatValues).clear();
      switch(this.type) {
      case 0:
         RenderSystem.glUniform1(this.location, this.intValues);
         break;
      case 1:
         RenderSystem.glUniform2(this.location, this.intValues);
         break;
      case 2:
         RenderSystem.glUniform3(this.location, this.intValues);
         break;
      case 3:
         RenderSystem.glUniform4(this.location, this.intValues);
         break;
      default:
         LOGGER.warn("Uniform.upload called, but count value ({}) is  not in the range of 1 to 4. Ignoring.", (int)this.count);
      }

   }

   private void uploadAsFloat() {
      ((Buffer)this.floatValues).clear();
      switch(this.type) {
      case 4:
         RenderSystem.glUniform1(this.location, this.floatValues);
         break;
      case 5:
         RenderSystem.glUniform2(this.location, this.floatValues);
         break;
      case 6:
         RenderSystem.glUniform3(this.location, this.floatValues);
         break;
      case 7:
         RenderSystem.glUniform4(this.location, this.floatValues);
         break;
      default:
         LOGGER.warn("Uniform.upload called, but count value ({}) is not in the range of 1 to 4. Ignoring.", (int)this.count);
      }

   }

   private void uploadAsMatrix() {
      ((Buffer)this.floatValues).clear();
      switch(this.type) {
      case 8:
         RenderSystem.glUniformMatrix2(this.location, false, this.floatValues);
         break;
      case 9:
         RenderSystem.glUniformMatrix3(this.location, false, this.floatValues);
         break;
      case 10:
         RenderSystem.glUniformMatrix4(this.location, false, this.floatValues);
      }

   }
}
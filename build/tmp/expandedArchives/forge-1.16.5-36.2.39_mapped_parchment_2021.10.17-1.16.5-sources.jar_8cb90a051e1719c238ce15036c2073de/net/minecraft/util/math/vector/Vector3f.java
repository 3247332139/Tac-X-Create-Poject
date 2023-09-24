package net.minecraft.util.math.vector;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class Vector3f {
   public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
   public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
   public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
   public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
   public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
   public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);
   private float x;
   private float y;
   private float z;

   public Vector3f() {
   }

   public Vector3f(float pX, float pY, float pZ) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
   }

   public Vector3f(Vector3d pVec3) {
      this((float)pVec3.x, (float)pVec3.y, (float)pVec3.z);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
         Vector3f vector3f = (Vector3f)p_equals_1_;
         if (Float.compare(vector3f.x, this.x) != 0) {
            return false;
         } else if (Float.compare(vector3f.y, this.y) != 0) {
            return false;
         } else {
            return Float.compare(vector3f.z, this.z) == 0;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = Float.floatToIntBits(this.x);
      i = 31 * i + Float.floatToIntBits(this.y);
      return 31 * i + Float.floatToIntBits(this.z);
   }

   public float x() {
      return this.x;
   }

   public float y() {
      return this.y;
   }

   public float z() {
      return this.z;
   }

   @OnlyIn(Dist.CLIENT)
   public void mul(float pMultiplier) {
      this.x *= pMultiplier;
      this.y *= pMultiplier;
      this.z *= pMultiplier;
   }

   @OnlyIn(Dist.CLIENT)
   public void mul(float pMx, float pMy, float pMz) {
      this.x *= pMx;
      this.y *= pMy;
      this.z *= pMz;
   }

   @OnlyIn(Dist.CLIENT)
   public void clamp(float pMin, float pMax) {
      this.x = MathHelper.clamp(this.x, pMin, pMax);
      this.y = MathHelper.clamp(this.y, pMin, pMax);
      this.z = MathHelper.clamp(this.z, pMin, pMax);
   }

   public void set(float pX, float pY, float pZ) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
   }

   @OnlyIn(Dist.CLIENT)
   public void add(float pX, float pY, float pZ) {
      this.x += pX;
      this.y += pY;
      this.z += pZ;
   }

   @OnlyIn(Dist.CLIENT)
   public void add(Vector3f pOther) {
      this.x += pOther.x;
      this.y += pOther.y;
      this.z += pOther.z;
   }

   @OnlyIn(Dist.CLIENT)
   public void sub(Vector3f pOther) {
      this.x -= pOther.x;
      this.y -= pOther.y;
      this.z -= pOther.z;
   }

   @OnlyIn(Dist.CLIENT)
   public float dot(Vector3f pOther) {
      return this.x * pOther.x + this.y * pOther.y + this.z * pOther.z;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean normalize() {
      float f = this.x * this.x + this.y * this.y + this.z * this.z;
      if ((double)f < 1.0E-5D) {
         return false;
      } else {
         float f1 = MathHelper.fastInvSqrt(f);
         this.x *= f1;
         this.y *= f1;
         this.z *= f1;
         return true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void cross(Vector3f pOther) {
      float f = this.x;
      float f1 = this.y;
      float f2 = this.z;
      float f3 = pOther.x();
      float f4 = pOther.y();
      float f5 = pOther.z();
      this.x = f1 * f5 - f2 * f4;
      this.y = f2 * f3 - f * f5;
      this.z = f * f4 - f1 * f3;
   }

   @OnlyIn(Dist.CLIENT)
   public void transform(Matrix3f pMatrix) {
      float f = this.x;
      float f1 = this.y;
      float f2 = this.z;
      this.x = pMatrix.m00 * f + pMatrix.m01 * f1 + pMatrix.m02 * f2;
      this.y = pMatrix.m10 * f + pMatrix.m11 * f1 + pMatrix.m12 * f2;
      this.z = pMatrix.m20 * f + pMatrix.m21 * f1 + pMatrix.m22 * f2;
   }

   public void transform(Quaternion pQuaternion) {
      Quaternion quaternion = new Quaternion(pQuaternion);
      quaternion.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
      Quaternion quaternion1 = new Quaternion(pQuaternion);
      quaternion1.conj();
      quaternion.mul(quaternion1);
      this.set(quaternion.i(), quaternion.j(), quaternion.k());
   }

   @OnlyIn(Dist.CLIENT)
   public void lerp(Vector3f pVector, float pDelta) {
      float f = 1.0F - pDelta;
      this.x = this.x * f + pVector.x * pDelta;
      this.y = this.y * f + pVector.y * pDelta;
      this.z = this.z * f + pVector.z * pDelta;
   }

   @OnlyIn(Dist.CLIENT)
   public Quaternion rotation(float pValue) {
      return new Quaternion(this, pValue, false);
   }

   @OnlyIn(Dist.CLIENT)
   public Quaternion rotationDegrees(float pValue) {
      return new Quaternion(this, pValue, true);
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3f copy() {
      return new Vector3f(this.x, this.y, this.z);
   }

   @OnlyIn(Dist.CLIENT)
   public void map(Float2FloatFunction pMapper) {
      this.x = pMapper.get(this.x);
      this.y = pMapper.get(this.y);
      this.z = pMapper.get(this.z);
   }

   public String toString() {
      return "[" + this.x + ", " + this.y + ", " + this.z + "]";
   }

    // Forge start
    public Vector3f(float[] values) {
        set(values);
    }
    public void set(float[] values) {
        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setZ(float z) { this.z = z; }
}

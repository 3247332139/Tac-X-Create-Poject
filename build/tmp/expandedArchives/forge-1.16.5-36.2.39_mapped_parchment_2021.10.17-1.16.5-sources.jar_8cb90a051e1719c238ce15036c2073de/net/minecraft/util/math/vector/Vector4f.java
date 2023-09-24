package net.minecraft.util.math.vector;

import net.minecraft.util.math.MathHelper;

public class Vector4f {
   private float x;
   private float y;
   private float z;
   private float w;

   public Vector4f() {
   }

   public Vector4f(float pX, float pY, float pZ, float pW) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.w = pW;
   }

   public Vector4f(Vector3f pVector3f) {
      this(pVector3f.x(), pVector3f.y(), pVector3f.z(), 1.0F);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
         Vector4f vector4f = (Vector4f)p_equals_1_;
         if (Float.compare(vector4f.x, this.x) != 0) {
            return false;
         } else if (Float.compare(vector4f.y, this.y) != 0) {
            return false;
         } else if (Float.compare(vector4f.z, this.z) != 0) {
            return false;
         } else {
            return Float.compare(vector4f.w, this.w) == 0;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = Float.floatToIntBits(this.x);
      i = 31 * i + Float.floatToIntBits(this.y);
      i = 31 * i + Float.floatToIntBits(this.z);
      return 31 * i + Float.floatToIntBits(this.w);
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

   public float w() {
      return this.w;
   }

   public void mul(Vector3f pVec) {
      this.x *= pVec.x();
      this.y *= pVec.y();
      this.z *= pVec.z();
   }

   public void set(float pX, float pY, float pZ, float pW) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.w = pW;
   }

   public float dot(Vector4f pOther) {
      return this.x * pOther.x + this.y * pOther.y + this.z * pOther.z + this.w * pOther.w;
   }

   public boolean normalize() {
      float f = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
      if ((double)f < 1.0E-5D) {
         return false;
      } else {
         float f1 = MathHelper.fastInvSqrt(f);
         this.x *= f1;
         this.y *= f1;
         this.z *= f1;
         this.w *= f1;
         return true;
      }
   }

   public void transform(Matrix4f pMatrix) {
      float f = this.x;
      float f1 = this.y;
      float f2 = this.z;
      float f3 = this.w;
      this.x = pMatrix.m00 * f + pMatrix.m01 * f1 + pMatrix.m02 * f2 + pMatrix.m03 * f3;
      this.y = pMatrix.m10 * f + pMatrix.m11 * f1 + pMatrix.m12 * f2 + pMatrix.m13 * f3;
      this.z = pMatrix.m20 * f + pMatrix.m21 * f1 + pMatrix.m22 * f2 + pMatrix.m23 * f3;
      this.w = pMatrix.m30 * f + pMatrix.m31 * f1 + pMatrix.m32 * f2 + pMatrix.m33 * f3;
   }

   public void transform(Quaternion pQuaternion) {
      Quaternion quaternion = new Quaternion(pQuaternion);
      quaternion.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
      Quaternion quaternion1 = new Quaternion(pQuaternion);
      quaternion1.conj();
      quaternion.mul(quaternion1);
      this.set(quaternion.i(), quaternion.j(), quaternion.k(), this.w());
   }

   public void perspectiveDivide() {
      this.x /= this.w;
      this.y /= this.w;
      this.z /= this.w;
      this.w = 1.0F;
   }

   public String toString() {
      return "[" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + "]";
   }

    // Forge start
    public void set(float[] values) {
        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
        this.w = values[3];
    }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setZ(float z) { this.z = z; }
    public void setW(float z) { this.w = z; }
}

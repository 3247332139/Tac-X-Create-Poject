package com.mojang.blaze3d.vertex;

import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IVertexConsumer extends IVertexBuilder {
   VertexFormatElement currentElement();

   void nextElement();

   void putByte(int pIndex, byte pByteValue);

   void putShort(int pIndex, short pShortValue);

   void putFloat(int pIndex, float pFloatValue);

   default IVertexBuilder vertex(double pX, double pY, double pZ) {
      if (this.currentElement().getType() != VertexFormatElement.Type.FLOAT) {
         throw new IllegalStateException();
      } else {
         this.putFloat(0, (float)pX);
         this.putFloat(4, (float)pY);
         this.putFloat(8, (float)pZ);
         this.nextElement();
         return this;
      }
   }

   default IVertexBuilder color(int pRed, int pGreen, int pBlue, int pAlpha) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() != VertexFormatElement.Usage.COLOR) {
         return this;
      } else if (vertexformatelement.getType() != VertexFormatElement.Type.UBYTE) {
         throw new IllegalStateException();
      } else {
         this.putByte(0, (byte)pRed);
         this.putByte(1, (byte)pGreen);
         this.putByte(2, (byte)pBlue);
         this.putByte(3, (byte)pAlpha);
         this.nextElement();
         return this;
      }
   }

   default IVertexBuilder uv(float pU, float pV) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == 0) {
         if (vertexformatelement.getType() != VertexFormatElement.Type.FLOAT) {
            throw new IllegalStateException();
         } else {
            this.putFloat(0, pU);
            this.putFloat(4, pV);
            this.nextElement();
            return this;
         }
      } else {
         return this;
      }
   }

   default IVertexBuilder overlayCoords(int pU, int pV) {
      return this.uvShort((short)pU, (short)pV, 1);
   }

   default IVertexBuilder uv2(int pU, int pV) {
      return this.uvShort((short)pU, (short)pV, 2);
   }

   default IVertexBuilder uvShort(short pU, short pV, int pIndex) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == pIndex) {
         if (vertexformatelement.getType() != VertexFormatElement.Type.SHORT) {
            throw new IllegalStateException();
         } else {
            this.putShort(0, pU);
            this.putShort(2, pV);
            this.nextElement();
            return this;
         }
      } else {
         return this;
      }
   }

   default IVertexBuilder normal(float pX, float pY, float pZ) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() != VertexFormatElement.Usage.NORMAL) {
         return this;
      } else if (vertexformatelement.getType() != VertexFormatElement.Type.BYTE) {
         throw new IllegalStateException();
      } else {
         this.putByte(0, normalIntValue(pX));
         this.putByte(1, normalIntValue(pY));
         this.putByte(2, normalIntValue(pZ));
         this.nextElement();
         return this;
      }
   }

   static byte normalIntValue(float pNum) {
      return (byte)((int)(MathHelper.clamp(pNum, -1.0F, 1.0F) * 127.0F) & 255);
   }
}
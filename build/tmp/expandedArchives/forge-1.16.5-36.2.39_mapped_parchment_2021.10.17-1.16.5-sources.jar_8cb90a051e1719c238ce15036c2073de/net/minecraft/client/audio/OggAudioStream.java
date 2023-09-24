package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisAlloc;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class OggAudioStream implements IAudioStream {
   private long handle;
   private final AudioFormat audioFormat;
   private final InputStream input;
   private ByteBuffer buffer = MemoryUtil.memAlloc(8192);

   public OggAudioStream(InputStream pInput) throws IOException {
      this.input = pInput;
      ((java.nio.Buffer)this.buffer).limit(0);

      try (MemoryStack memorystack = MemoryStack.stackPush()) {
         IntBuffer intbuffer = memorystack.mallocInt(1);
         IntBuffer intbuffer1 = memorystack.mallocInt(1);

         while(this.handle == 0L) {
            if (!this.refillFromStream()) {
               throw new IOException("Failed to find Ogg header");
            }

            int i = this.buffer.position();
            ((java.nio.Buffer)this.buffer).position(0);
            this.handle = STBVorbis.stb_vorbis_open_pushdata(this.buffer, intbuffer, intbuffer1, (STBVorbisAlloc)null);
            ((java.nio.Buffer)this.buffer).position(i);
            int j = intbuffer1.get(0);
            if (j == 1) {
               this.forwardBuffer();
            } else if (j != 0) {
               throw new IOException("Failed to read Ogg file " + j);
            }
         }

         ((java.nio.Buffer)this.buffer).position(this.buffer.position() + intbuffer.get(0));
         STBVorbisInfo stbvorbisinfo = STBVorbisInfo.mallocStack(memorystack);
         STBVorbis.stb_vorbis_get_info(this.handle, stbvorbisinfo);
         this.audioFormat = new AudioFormat((float)stbvorbisinfo.sample_rate(), 16, stbvorbisinfo.channels(), true, false);
      }

   }

   private boolean refillFromStream() throws IOException {
      int i = this.buffer.limit();
      int j = this.buffer.capacity() - i;
      if (j == 0) {
         return true;
      } else {
         byte[] abyte = new byte[j];
         int k = this.input.read(abyte);
         if (k == -1) {
            return false;
         } else {
            int l = this.buffer.position();
            ((java.nio.Buffer)this.buffer).limit(i + k);
            ((java.nio.Buffer)this.buffer).position(i);
            this.buffer.put(abyte, 0, k);
            ((java.nio.Buffer)this.buffer).position(l);
            return true;
         }
      }
   }

   private void forwardBuffer() {
      boolean flag = this.buffer.position() == 0;
      boolean flag1 = this.buffer.position() == this.buffer.limit();
      if (flag1 && !flag) {
         ((java.nio.Buffer)this.buffer).position(0);
         ((java.nio.Buffer)this.buffer).limit(0);
      } else {
         ByteBuffer bytebuffer = MemoryUtil.memAlloc(flag ? 2 * this.buffer.capacity() : this.buffer.capacity());
         bytebuffer.put(this.buffer);
         MemoryUtil.memFree(this.buffer);
         ((java.nio.Buffer)bytebuffer).flip();
         this.buffer = bytebuffer;
      }

   }

   private boolean readFrame(OggAudioStream.Buffer pOutput) throws IOException {
      if (this.handle == 0L) {
         return false;
      } else {
         try (MemoryStack memorystack = MemoryStack.stackPush()) {
            PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
            IntBuffer intbuffer = memorystack.mallocInt(1);
            IntBuffer intbuffer1 = memorystack.mallocInt(1);

            while(true) {
               int i = STBVorbis.stb_vorbis_decode_frame_pushdata(this.handle, this.buffer, intbuffer, pointerbuffer, intbuffer1);
               ((java.nio.Buffer)this.buffer).position(this.buffer.position() + i);
               int j = STBVorbis.stb_vorbis_get_error(this.handle);
               if (j == 1) {
                  this.forwardBuffer();
                  if (!this.refillFromStream()) {
                     return false;
                  }
               } else {
                  if (j != 0) {
                     throw new IOException("Failed to read Ogg file " + j);
                  }

                  int k = intbuffer1.get(0);
                  if (k != 0) {
                     int l = intbuffer.get(0);
                     PointerBuffer pointerbuffer1 = pointerbuffer.getPointerBuffer(l);
                     if (l != 1) {
                        if (l == 2) {
                           this.convertStereo(pointerbuffer1.getFloatBuffer(0, k), pointerbuffer1.getFloatBuffer(1, k), pOutput);
                           return true;
                        }

                        throw new IllegalStateException("Invalid number of channels: " + l);
                     }

                     this.convertMono(pointerbuffer1.getFloatBuffer(0, k), pOutput);
                     return true;
                  }
               }
            }
         }
      }
   }

   private void convertMono(FloatBuffer pChannel, OggAudioStream.Buffer pOutput) {
      while(pChannel.hasRemaining()) {
         pOutput.put(pChannel.get());
      }

   }

   private void convertStereo(FloatBuffer pLeftChannel, FloatBuffer pRightChannel, OggAudioStream.Buffer pOutput) {
      while(pLeftChannel.hasRemaining() && pRightChannel.hasRemaining()) {
         pOutput.put(pLeftChannel.get());
         pOutput.put(pRightChannel.get());
      }

   }

   public void close() throws IOException {
      if (this.handle != 0L) {
         STBVorbis.stb_vorbis_close(this.handle);
         this.handle = 0L;
      }

      MemoryUtil.memFree(this.buffer);
      this.input.close();
   }

   public AudioFormat getFormat() {
      return this.audioFormat;
   }

   public ByteBuffer read(int pSize) throws IOException {
      OggAudioStream.Buffer oggaudiostream$buffer = new OggAudioStream.Buffer(pSize + 8192);

      while(this.readFrame(oggaudiostream$buffer) && oggaudiostream$buffer.byteCount < pSize) {
      }

      return oggaudiostream$buffer.get();
   }

   public ByteBuffer readAll() throws IOException {
      OggAudioStream.Buffer oggaudiostream$buffer = new OggAudioStream.Buffer(16384);

      while(this.readFrame(oggaudiostream$buffer)) {
      }

      return oggaudiostream$buffer.get();
   }

   @OnlyIn(Dist.CLIENT)
   static class Buffer {
      private final List<ByteBuffer> buffers = Lists.newArrayList();
      private final int bufferSize;
      private int byteCount;
      private ByteBuffer currentBuffer;

      public Buffer(int pSize) {
         this.bufferSize = pSize + 1 & -2;
         this.createNewBuffer();
      }

      private void createNewBuffer() {
         this.currentBuffer = BufferUtils.createByteBuffer(this.bufferSize);
      }

      public void put(float pSample) {
         if (this.currentBuffer.remaining() == 0) {
            ((java.nio.Buffer)this.currentBuffer).flip();
            this.buffers.add(this.currentBuffer);
            this.createNewBuffer();
         }

         int i = MathHelper.clamp((int)(pSample * 32767.5F - 0.5F), -32768, 32767);
         this.currentBuffer.putShort((short)i);
         this.byteCount += 2;
      }

      public ByteBuffer get() {
         ((java.nio.Buffer)this.currentBuffer).flip();
         if (this.buffers.isEmpty()) {
            return this.currentBuffer;
         } else {
            ByteBuffer bytebuffer = BufferUtils.createByteBuffer(this.byteCount);
            this.buffers.forEach(bytebuffer::put);
            bytebuffer.put(this.currentBuffer);
            ((java.nio.Buffer)bytebuffer).flip();
            return bytebuffer;
         }
      }
   }
}
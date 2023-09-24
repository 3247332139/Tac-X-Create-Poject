package net.minecraft.world.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.concurrent.DelegatedTaskExecutor;
import net.minecraft.util.concurrent.ITaskQueue;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IOWorker implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final AtomicBoolean shutdownRequested = new AtomicBoolean();
   private final DelegatedTaskExecutor<ITaskQueue.RunnableWithPriority> mailbox;
   private final RegionFileCache storage;
   private final Map<ChunkPos, IOWorker.Entry> pendingWrites = Maps.newLinkedHashMap();

   protected IOWorker(File pFolder, boolean pSync, String pWorkerName) {
      this.storage = new RegionFileCache(pFolder, pSync);
      this.mailbox = new DelegatedTaskExecutor<>(new ITaskQueue.Priority(IOWorker.Priority.values().length), Util.ioPool(), "IOWorker-" + pWorkerName);
   }

   public CompletableFuture<Void> store(ChunkPos pChunkPos, CompoundNBT pChunkData) {
      return this.submitTask(() -> {
         IOWorker.Entry ioworker$entry = this.pendingWrites.computeIfAbsent(pChunkPos, (p_235977_1_) -> {
            return new IOWorker.Entry(pChunkData);
         });
         ioworker$entry.data = pChunkData;
         return Either.left(ioworker$entry.result);
      }).thenCompose(Function.identity());
   }

   @Nullable
   public CompoundNBT load(ChunkPos pChunkPos) throws IOException {
      CompletableFuture<CompoundNBT> completablefuture = this.submitTask(() -> {
         IOWorker.Entry ioworker$entry = this.pendingWrites.get(pChunkPos);
         if (ioworker$entry != null) {
            return Either.left(ioworker$entry.data);
         } else {
            try {
               CompoundNBT compoundnbt = this.storage.read(pChunkPos);
               return Either.left(compoundnbt);
            } catch (Exception exception) {
               LOGGER.warn("Failed to read chunk {}", pChunkPos, exception);
               return Either.right(exception);
            }
         }
      });

      try {
         return completablefuture.join();
      } catch (CompletionException completionexception) {
         if (completionexception.getCause() instanceof IOException) {
            throw (IOException)completionexception.getCause();
         } else {
            throw completionexception;
         }
      }
   }

   public CompletableFuture<Void> synchronize() {
      CompletableFuture<Void> completablefuture = this.submitTask(() -> {
         return Either.left(CompletableFuture.allOf(this.pendingWrites.values().stream().map((p_235973_0_) -> {
            return p_235973_0_.result;
         }).toArray((p_235970_0_) -> {
            return new CompletableFuture[p_235970_0_];
         })));
      }).thenCompose(Function.identity());
      return completablefuture.thenCompose((p_235974_1_) -> {
         return this.submitTask(() -> {
            try {
               this.storage.flush();
               return Either.left((Void)null);
            } catch (Exception exception) {
               LOGGER.warn("Failed to synchronized chunks", (Throwable)exception);
               return Either.right(exception);
            }
         });
      });
   }

   private <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> pTask) {
      return this.mailbox.askEither((p_235976_2_) -> {
         return new ITaskQueue.RunnableWithPriority(IOWorker.Priority.HIGH.ordinal(), () -> {
            if (!this.shutdownRequested.get()) {
               p_235976_2_.tell(pTask.get());
            }

            this.tellStorePending();
         });
      });
   }

   private void storePendingChunk() {
      Iterator<Map.Entry<ChunkPos, IOWorker.Entry>> iterator = this.pendingWrites.entrySet().iterator();
      if (iterator.hasNext()) {
         Map.Entry<ChunkPos, IOWorker.Entry> entry = iterator.next();
         iterator.remove();
         this.runStore(entry.getKey(), entry.getValue());
         this.tellStorePending();
      }
   }

   private void tellStorePending() {
      this.mailbox.tell(new ITaskQueue.RunnableWithPriority(IOWorker.Priority.LOW.ordinal(), this::storePendingChunk));
   }

   private void runStore(ChunkPos p_227091_1_, IOWorker.Entry p_227091_2_) {
      try {
         this.storage.write(p_227091_1_, p_227091_2_.data);
         p_227091_2_.result.complete((Void)null);
      } catch (Exception exception) {
         LOGGER.error("Failed to store chunk {}", p_227091_1_, exception);
         p_227091_2_.result.completeExceptionally(exception);
      }

   }

   public void close() throws IOException {
      if (this.shutdownRequested.compareAndSet(false, true)) {
         CompletableFuture<Unit> completablefuture = this.mailbox.ask((p_235971_0_) -> {
            return new ITaskQueue.RunnableWithPriority(IOWorker.Priority.HIGH.ordinal(), () -> {
               p_235971_0_.tell(Unit.INSTANCE);
            });
         });

         try {
            completablefuture.join();
         } catch (CompletionException completionexception) {
            if (completionexception.getCause() instanceof IOException) {
               throw (IOException)completionexception.getCause();
            }

            throw completionexception;
         }

         this.mailbox.close();
         this.pendingWrites.forEach(this::runStore);
         this.pendingWrites.clear();

         try {
            this.storage.close();
         } catch (Exception exception) {
            LOGGER.error("Failed to close storage", (Throwable)exception);
         }

      }
   }

   static class Entry {
      private CompoundNBT data;
      private final CompletableFuture<Void> result = new CompletableFuture<>();

      public Entry(CompoundNBT pData) {
         this.data = pData;
      }
   }

   static enum Priority {
      HIGH,
      LOW;
   }
}
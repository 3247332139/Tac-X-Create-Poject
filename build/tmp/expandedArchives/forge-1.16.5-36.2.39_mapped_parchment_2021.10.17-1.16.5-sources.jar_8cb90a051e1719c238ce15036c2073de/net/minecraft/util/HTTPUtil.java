package net.minecraft.util;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HTTPUtil {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ListeningExecutorService DOWNLOAD_EXECUTOR = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool((new ThreadFactoryBuilder()).setDaemon(true).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER)).setNameFormat("Downloader %d").build()));

   @OnlyIn(Dist.CLIENT)
   public static CompletableFuture<?> downloadTo(File pSaveFile, String pPackUrl, Map<String, String> pRequestProperties, int pMaxSize, @Nullable IProgressUpdate pProgressCallback, Proxy pProxy) {
      return CompletableFuture.supplyAsync(() -> {
         HttpURLConnection httpurlconnection = null;
         InputStream inputstream = null;
         OutputStream outputstream = null;
         if (pProgressCallback != null) {
            pProgressCallback.progressStart(new TranslationTextComponent("resourcepack.downloading"));
            pProgressCallback.progressStage(new TranslationTextComponent("resourcepack.requesting"));
         }

         try {
            try {
               byte[] abyte = new byte[4096];
               URL url = new URL(pPackUrl);
               httpurlconnection = (HttpURLConnection)url.openConnection(pProxy);
               httpurlconnection.setInstanceFollowRedirects(true);
               float f = 0.0F;
               float f1 = (float)pRequestProperties.entrySet().size();

               for(Entry<String, String> entry : pRequestProperties.entrySet()) {
                  httpurlconnection.setRequestProperty(entry.getKey(), entry.getValue());
                  if (pProgressCallback != null) {
                     pProgressCallback.progressStagePercentage((int)(++f / f1 * 100.0F));
                  }
               }

               inputstream = httpurlconnection.getInputStream();
               f1 = (float)httpurlconnection.getContentLength();
               int i = httpurlconnection.getContentLength();
               if (pProgressCallback != null) {
                  pProgressCallback.progressStage(new TranslationTextComponent("resourcepack.progress", String.format(Locale.ROOT, "%.2f", f1 / 1000.0F / 1000.0F)));
               }

               if (pSaveFile.exists()) {
                  long j = pSaveFile.length();
                  if (j == (long)i) {
                     if (pProgressCallback != null) {
                        pProgressCallback.stop();
                     }

                     return null;
                  }

                  LOGGER.warn("Deleting {} as it does not match what we currently have ({} vs our {}).", pSaveFile, i, j);
                  FileUtils.deleteQuietly(pSaveFile);
               } else if (pSaveFile.getParentFile() != null) {
                  pSaveFile.getParentFile().mkdirs();
               }

               outputstream = new DataOutputStream(new FileOutputStream(pSaveFile));
               if (pMaxSize > 0 && f1 > (float)pMaxSize) {
                  if (pProgressCallback != null) {
                     pProgressCallback.stop();
                  }

                  throw new IOException("Filesize is bigger than maximum allowed (file is " + f + ", limit is " + pMaxSize + ")");
               }

               int k;
               while((k = inputstream.read(abyte)) >= 0) {
                  f += (float)k;
                  if (pProgressCallback != null) {
                     pProgressCallback.progressStagePercentage((int)(f / f1 * 100.0F));
                  }

                  if (pMaxSize > 0 && f > (float)pMaxSize) {
                     if (pProgressCallback != null) {
                        pProgressCallback.stop();
                     }

                     throw new IOException("Filesize was bigger than maximum allowed (got >= " + f + ", limit was " + pMaxSize + ")");
                  }

                  if (Thread.interrupted()) {
                     LOGGER.error("INTERRUPTED");
                     if (pProgressCallback != null) {
                        pProgressCallback.stop();
                     }

                     return null;
                  }

                  outputstream.write(abyte, 0, k);
               }

               if (pProgressCallback != null) {
                  pProgressCallback.stop();
                  return null;
               }
            } catch (Throwable throwable) {
               throwable.printStackTrace();
               if (httpurlconnection != null) {
                  InputStream inputstream1 = httpurlconnection.getErrorStream();

                  try {
                     LOGGER.error(IOUtils.toString(inputstream1));
                  } catch (IOException ioexception) {
                     ioexception.printStackTrace();
                  }
               }

               if (pProgressCallback != null) {
                  pProgressCallback.stop();
                  return null;
               }
            }

            return null;
         } finally {
            IOUtils.closeQuietly(inputstream);
            IOUtils.closeQuietly(outputstream);
         }
      }, DOWNLOAD_EXECUTOR);
   }

   public static int getAvailablePort() {
      try (ServerSocket serversocket = new ServerSocket(0)) {
         return serversocket.getLocalPort();
      } catch (IOException ioexception) {
         return 25564;
      }
   }
}
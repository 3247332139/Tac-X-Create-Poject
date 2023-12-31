package net.minecraft.data;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.registry.Bootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataGenerator {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Collection<Path> inputFolders;
   private final Path outputFolder;
   private final List<IDataProvider> providers = Lists.newArrayList();
   private final List<IDataProvider> providerView = java.util.Collections.unmodifiableList(providers);

   public DataGenerator(Path pOutputFolder, Collection<Path> pInputFolders) {
      this.outputFolder = pOutputFolder;
      this.inputFolders = Lists.newArrayList(pInputFolders);
   }

   /**
    * Gets a collection of folders to look for data to convert in
    */
   public Collection<Path> getInputFolders() {
      return this.inputFolders;
   }

   /**
    * Gets the location to put generated data into
    */
   public Path getOutputFolder() {
      return this.outputFolder;
   }

   /**
    * Runs all the previously registered data providors.
    */
   public void run() throws IOException {
      DirectoryCache directorycache = new DirectoryCache(this.outputFolder, "cache");
      directorycache.keep(this.getOutputFolder().resolve("version.json"));
      Stopwatch stopwatch = Stopwatch.createStarted();
      Stopwatch stopwatch1 = Stopwatch.createUnstarted();

      for(IDataProvider idataprovider : this.providers) {
         LOGGER.info("Starting provider: {}", (Object)idataprovider.getName());
         net.minecraftforge.fml.StartupMessageManager.addModMessage("Generating: " + idataprovider.getName());
         stopwatch1.start();
         idataprovider.run(directorycache);
         stopwatch1.stop();
         LOGGER.info("{} finished after {} ms", idataprovider.getName(), stopwatch1.elapsed(TimeUnit.MILLISECONDS));
         stopwatch1.reset();
      }

      LOGGER.info("All providers took: {} ms", (long)stopwatch.elapsed(TimeUnit.MILLISECONDS));
      directorycache.purgeStaleAndWrite();
   }

   /**
    * Adds a data provider to the list of providers to run
    */
   public void addProvider(IDataProvider pProvider) {
      this.providers.add(pProvider);
   }

   public List<IDataProvider> getProviders() {
       return this.providerView;
   }

   public void addInput(Path value) {
      this.inputFolders.add(value);
   }

   static {
      Bootstrap.bootStrap();
   }
}

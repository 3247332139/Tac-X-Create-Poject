package net.minecraft.client.audio;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.GameSettings;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SoundHandler extends ReloadListener<SoundHandler.Loader> {
   public static final Sound EMPTY_SOUND = new Sound("meta:missing_sound", 1.0F, 1.0F, 1, Sound.Type.FILE, false, false, 16);
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer()).registerTypeAdapter(SoundList.class, new SoundListSerializer()).create();
   private static final TypeToken<Map<String, SoundList>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<Map<String, SoundList>>() {
   };
   private final Map<ResourceLocation, SoundEventAccessor> registry = Maps.newHashMap();
   private final SoundEngine soundEngine;

   public SoundHandler(IResourceManager p_i45122_1_, GameSettings p_i45122_2_) {
      this.soundEngine = new SoundEngine(this, p_i45122_2_, p_i45122_1_);
   }

   /**
    * Performs any reloading that can be done off-thread, such as file IO
    */
   protected SoundHandler.Loader prepare(IResourceManager pResourceManager, IProfiler pProfiler) {
      SoundHandler.Loader soundhandler$loader = new SoundHandler.Loader();
      pProfiler.startTick();

      for(String s : pResourceManager.getNamespaces()) {
         pProfiler.push(s);

         try {
            for(IResource iresource : pResourceManager.getResources(new ResourceLocation(s, "sounds.json"))) {
               pProfiler.push(iresource.getSourceName());

               try (
                  InputStream inputstream = iresource.getInputStream();
                  Reader reader = new InputStreamReader(inputstream, StandardCharsets.UTF_8);
               ) {
                  pProfiler.push("parse");
                  Map<String, SoundList> map = JSONUtils.fromJson(GSON, reader, SOUND_EVENT_REGISTRATION_TYPE);
                  pProfiler.popPush("register");

                  for(Entry<String, SoundList> entry : map.entrySet()) {
                     soundhandler$loader.handleRegistration(new ResourceLocation(s, entry.getKey()), entry.getValue(), pResourceManager);
                  }

                  pProfiler.pop();
               } catch (RuntimeException runtimeexception) {
                  LOGGER.warn("Invalid sounds.json in resourcepack: '{}'", iresource.getSourceName(), runtimeexception);
               }

               pProfiler.pop();
            }
         } catch (IOException ioexception) {
         }

         pProfiler.pop();
      }

      pProfiler.endTick();
      return soundhandler$loader;
   }

   protected void apply(SoundHandler.Loader pObject, IResourceManager pResourceManager, IProfiler pProfiler) {
      pObject.apply(this.registry, this.soundEngine);

      for(ResourceLocation resourcelocation : this.registry.keySet()) {
         SoundEventAccessor soundeventaccessor = this.registry.get(resourcelocation);
         if (soundeventaccessor.getSubtitle() instanceof TranslationTextComponent) {
            String s = ((TranslationTextComponent)soundeventaccessor.getSubtitle()).getKey();
            if (!I18n.exists(s)) {
               LOGGER.debug("Missing subtitle {} for event: {}", s, resourcelocation);
            }
         }
      }

      if (LOGGER.isDebugEnabled()) {
         for(ResourceLocation resourcelocation1 : this.registry.keySet()) {
            if (!Registry.SOUND_EVENT.containsKey(resourcelocation1)) {
               LOGGER.debug("Not having sound event for: {}", (Object)resourcelocation1);
            }
         }
      }

      this.soundEngine.reload();
   }

   private static boolean validateSoundResource(Sound pSound, ResourceLocation pSoundLocation, IResourceManager pResourceManager) {
      ResourceLocation resourcelocation = pSound.getPath();
      if (!pResourceManager.hasResource(resourcelocation)) {
         LOGGER.warn("File {} does not exist, cannot add it to event {}", resourcelocation, pSoundLocation);
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   public SoundEventAccessor getSoundEvent(ResourceLocation pLocation) {
      return this.registry.get(pLocation);
   }

   public Collection<ResourceLocation> getAvailableSounds() {
      return this.registry.keySet();
   }

   public void queueTickingSound(ITickableSound pTickableSound) {
      this.soundEngine.queueTickingSound(pTickableSound);
   }

   /**
    * Play a sound
    */
   public void play(ISound pSound) {
      this.soundEngine.play(pSound);
   }

   /**
    * Plays the sound in n ticks
    */
   public void playDelayed(ISound pSound, int pDelay) {
      this.soundEngine.playDelayed(pSound, pDelay);
   }

   public void updateSource(ActiveRenderInfo pActiveRenderInfo) {
      this.soundEngine.updateSource(pActiveRenderInfo);
   }

   public void pause() {
      this.soundEngine.pause();
   }

   public void stop() {
      this.soundEngine.stopAll();
   }

   public void destroy() {
      this.soundEngine.destroy();
   }

   public void tick(boolean pIsGamePaused) {
      this.soundEngine.tick(pIsGamePaused);
   }

   public void resume() {
      this.soundEngine.resume();
   }

   public void updateSourceVolume(SoundCategory pCategory, float pVolume) {
      if (pCategory == SoundCategory.MASTER && pVolume <= 0.0F) {
         this.stop();
      }

      this.soundEngine.updateCategoryVolume(pCategory, pVolume);
   }

   public void stop(ISound pSound) {
      this.soundEngine.stop(pSound);
   }

   public boolean isActive(ISound pSound) {
      return this.soundEngine.isActive(pSound);
   }

   public void addListener(ISoundEventListener pListener) {
      this.soundEngine.addEventListener(pListener);
   }

   public void removeListener(ISoundEventListener pListener) {
      this.soundEngine.removeEventListener(pListener);
   }

   public void stop(@Nullable ResourceLocation pId, @Nullable SoundCategory pCategory) {
      this.soundEngine.stop(pId, pCategory);
   }

   //@Override //TODO: Filtered reload
   public net.minecraftforge.resource.IResourceType getResourceType() {
      return net.minecraftforge.resource.VanillaResourceType.SOUNDS;
   }

   public String getDebugString() {
      return this.soundEngine.getDebugString();
   }

   @OnlyIn(Dist.CLIENT)
   public static class Loader {
      private final Map<ResourceLocation, SoundEventAccessor> registry = Maps.newHashMap();

      protected Loader() {
      }

      private void handleRegistration(ResourceLocation pSoundLocation, SoundList pSoundList, IResourceManager pResourceManager) {
         SoundEventAccessor soundeventaccessor = this.registry.get(pSoundLocation);
         boolean flag = soundeventaccessor == null;
         if (flag || pSoundList.isReplace()) {
            if (!flag) {
               SoundHandler.LOGGER.debug("Replaced sound event location {}", (Object)pSoundLocation);
            }

            soundeventaccessor = new SoundEventAccessor(pSoundLocation, pSoundList.getSubtitle());
            this.registry.put(pSoundLocation, soundeventaccessor);
         }

         for(final Sound sound : pSoundList.getSounds()) {
            final ResourceLocation resourcelocation = sound.getLocation();
            ISoundEventAccessor<Sound> isoundeventaccessor;
            switch(sound.getType()) {
            case FILE:
               if (!SoundHandler.validateSoundResource(sound, pSoundLocation, pResourceManager)) {
                  continue;
               }

               isoundeventaccessor = sound;
               break;
            case SOUND_EVENT:
               isoundeventaccessor = new ISoundEventAccessor<Sound>() {
                  public int getWeight() {
                     SoundEventAccessor soundeventaccessor1 = Loader.this.registry.get(resourcelocation);
                     return soundeventaccessor1 == null ? 0 : soundeventaccessor1.getWeight();
                  }

                  public Sound getSound() {
                     SoundEventAccessor soundeventaccessor1 = Loader.this.registry.get(resourcelocation);
                     if (soundeventaccessor1 == null) {
                        return SoundHandler.EMPTY_SOUND;
                     } else {
                        Sound sound1 = soundeventaccessor1.getSound();
                        return new Sound(sound1.getLocation().toString(), sound1.getVolume() * sound.getVolume(), sound1.getPitch() * sound.getPitch(), sound.getWeight(), Sound.Type.FILE, sound1.shouldStream() || sound.shouldStream(), sound1.shouldPreload(), sound1.getAttenuationDistance());
                     }
                  }

                  public void preloadIfRequired(SoundEngine pEngine) {
                     SoundEventAccessor soundeventaccessor1 = Loader.this.registry.get(resourcelocation);
                     if (soundeventaccessor1 != null) {
                        soundeventaccessor1.preloadIfRequired(pEngine);
                     }
                  }
               };
               break;
            default:
               throw new IllegalStateException("Unknown SoundEventRegistration type: " + sound.getType());
            }

            soundeventaccessor.addSound(isoundeventaccessor);
         }

      }

      public void apply(Map<ResourceLocation, SoundEventAccessor> pSoundRegistry, SoundEngine pSoundManager) {
         pSoundRegistry.clear();

         for(Entry<ResourceLocation, SoundEventAccessor> entry : this.registry.entrySet()) {
            pSoundRegistry.put(entry.getKey(), entry.getValue());
            entry.getValue().preloadIfRequired(pSoundManager);
         }

      }
   }
}

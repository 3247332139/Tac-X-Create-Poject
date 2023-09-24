package net.minecraft.client;

import com.mojang.authlib.properties.PropertyMap;
import java.io.File;
import java.net.Proxy;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ScreenSize;
import net.minecraft.client.resources.FolderResourceIndex;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.util.Session;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameConfiguration {
   public final GameConfiguration.UserInformation user;
   public final ScreenSize display;
   public final GameConfiguration.FolderInformation location;
   public final GameConfiguration.GameInformation game;
   public final GameConfiguration.ServerInformation server;

   public GameConfiguration(GameConfiguration.UserInformation pUser, ScreenSize pDisplay, GameConfiguration.FolderInformation pLocation, GameConfiguration.GameInformation pGame, GameConfiguration.ServerInformation pServer) {
      this.user = pUser;
      this.display = pDisplay;
      this.location = pLocation;
      this.game = pGame;
      this.server = pServer;
   }

   @OnlyIn(Dist.CLIENT)
   public static class FolderInformation {
      public final File gameDirectory;
      public final File resourcePackDirectory;
      public final File assetDirectory;
      @Nullable
      public final String assetIndex;

      public FolderInformation(File pGameDirectory, File pResourcePackDirectory, File pAssetDirectory, @Nullable String pAssetIndex) {
         this.gameDirectory = pGameDirectory;
         this.resourcePackDirectory = pResourcePackDirectory;
         this.assetDirectory = pAssetDirectory;
         this.assetIndex = pAssetIndex;
      }

      public ResourceIndex getAssetIndex() {
         return (ResourceIndex)(this.assetIndex == null ? new FolderResourceIndex(this.assetDirectory) : new ResourceIndex(this.assetDirectory, this.assetIndex));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class GameInformation {
      public final boolean demo;
      public final String launchVersion;
      public final String versionType;
      public final boolean disableMultiplayer;
      public final boolean disableChat;

      public GameInformation(boolean pDemo, String pLaunchVersion, String pVersionType, boolean pDisableMultiplayer, boolean pDisableChat) {
         this.demo = pDemo;
         this.launchVersion = pLaunchVersion;
         this.versionType = pVersionType;
         this.disableMultiplayer = pDisableMultiplayer;
         this.disableChat = pDisableChat;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class ServerInformation {
      @Nullable
      public final String hostname;
      public final int port;

      public ServerInformation(@Nullable String pHostname, int pPort) {
         this.hostname = pHostname;
         this.port = pPort;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class UserInformation {
      public final Session user;
      public final PropertyMap userProperties;
      public final PropertyMap profileProperties;
      public final Proxy proxy;

      public UserInformation(Session pUser, PropertyMap pUserProperties, PropertyMap pProfileProperties, Proxy pProxy) {
         this.user = pUser;
         this.userProperties = pUserProperties;
         this.profileProperties = pProfileProperties;
         this.proxy = pProxy;
      }
   }
}
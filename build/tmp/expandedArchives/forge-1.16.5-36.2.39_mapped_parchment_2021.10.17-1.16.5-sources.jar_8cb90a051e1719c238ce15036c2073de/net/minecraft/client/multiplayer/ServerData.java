package net.minecraft.client.multiplayer;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerData {
   public String name;
   public String ip;
   public ITextComponent status;
   public ITextComponent motd;
   public long ping;
   public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
   public ITextComponent version = new StringTextComponent(SharedConstants.getCurrentVersion().getName());
   public boolean pinged;
   public List<ITextComponent> playerList = Collections.emptyList();
   private ServerData.ServerResourceMode packStatus = ServerData.ServerResourceMode.PROMPT;
   @Nullable
   private String iconB64;
   /** True if the server is a LAN server */
   private boolean lan;
   public net.minecraftforge.fml.client.ExtendedServerListData forgeData = null;

   public ServerData(String p_i46420_1_, String p_i46420_2_, boolean p_i46420_3_) {
      this.name = p_i46420_1_;
      this.ip = p_i46420_2_;
      this.lan = p_i46420_3_;
   }

   /**
    * Returns an NBTTagCompound with the server's name, IP and maybe acceptTextures.
    */
   public CompoundNBT write() {
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.putString("name", this.name);
      compoundnbt.putString("ip", this.ip);
      if (this.iconB64 != null) {
         compoundnbt.putString("icon", this.iconB64);
      }

      if (this.packStatus == ServerData.ServerResourceMode.ENABLED) {
         compoundnbt.putBoolean("acceptTextures", true);
      } else if (this.packStatus == ServerData.ServerResourceMode.DISABLED) {
         compoundnbt.putBoolean("acceptTextures", false);
      }

      return compoundnbt;
   }

   public ServerData.ServerResourceMode getResourcePackStatus() {
      return this.packStatus;
   }

   public void setResourcePackStatus(ServerData.ServerResourceMode pMode) {
      this.packStatus = pMode;
   }

   /**
    * Takes an NBTTagCompound with 'name' and 'ip' keys, returns a ServerData instance.
    */
   public static ServerData read(CompoundNBT pNbtCompound) {
      ServerData serverdata = new ServerData(pNbtCompound.getString("name"), pNbtCompound.getString("ip"), false);
      if (pNbtCompound.contains("icon", 8)) {
         serverdata.setIconB64(pNbtCompound.getString("icon"));
      }

      if (pNbtCompound.contains("acceptTextures", 1)) {
         if (pNbtCompound.getBoolean("acceptTextures")) {
            serverdata.setResourcePackStatus(ServerData.ServerResourceMode.ENABLED);
         } else {
            serverdata.setResourcePackStatus(ServerData.ServerResourceMode.DISABLED);
         }
      } else {
         serverdata.setResourcePackStatus(ServerData.ServerResourceMode.PROMPT);
      }

      return serverdata;
   }

   /**
    * Returns the base-64 encoded representation of the server's icon, or null if not available
    */
   @Nullable
   public String getIconB64() {
      return this.iconB64;
   }

   public void setIconB64(@Nullable String pIcon) {
      this.iconB64 = pIcon;
   }

   /**
    * Return true if the server is a LAN server
    */
   public boolean isLan() {
      return this.lan;
   }

   public void copyFrom(ServerData pServerData) {
      this.ip = pServerData.ip;
      this.name = pServerData.name;
      this.setResourcePackStatus(pServerData.getResourcePackStatus());
      this.iconB64 = pServerData.iconB64;
      this.lan = pServerData.lan;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum ServerResourceMode {
      ENABLED("enabled"),
      DISABLED("disabled"),
      PROMPT("prompt");

      private final ITextComponent name;

      private ServerResourceMode(String p_i1053_3_) {
         this.name = new TranslationTextComponent("addServer.resourcePack." + p_i1053_3_);
      }

      public ITextComponent getName() {
         return this.name;
      }
   }
}

package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class BanList extends UserList<GameProfile, ProfileBanEntry> {
   public BanList(File p_i1138_1_) {
      super(p_i1138_1_);
   }

   protected UserListEntry<GameProfile> createEntry(JsonObject pEntryData) {
      return new ProfileBanEntry(pEntryData);
   }

   public boolean isBanned(GameProfile pProfile) {
      return this.contains(pProfile);
   }

   public String[] getUserList() {
      String[] astring = new String[this.getEntries().size()];
      int i = 0;

      for(UserListEntry<GameProfile> userlistentry : this.getEntries()) {
         astring[i++] = userlistentry.getUser().getName();
      }

      return astring;
   }

   /**
    * Gets the key value for the given object
    */
   protected String getKeyForUser(GameProfile pObj) {
      return pObj.getId().toString();
   }
}
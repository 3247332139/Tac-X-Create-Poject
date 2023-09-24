package net.minecraft.server.management;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class UserList<K, V extends UserListEntry<K>> {
   protected static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final File file;
   private final Map<String, V> map = Maps.newHashMap();

   public UserList(File p_i1144_1_) {
      this.file = p_i1144_1_;
   }

   public File getFile() {
      return this.file;
   }

   /**
    * Adds an entry to the list
    */
   public void add(V pEntry) {
      this.map.put(this.getKeyForUser(pEntry.getUser()), pEntry);

      try {
         this.save();
      } catch (IOException ioexception) {
         LOGGER.warn("Could not save the list after adding a user.", (Throwable)ioexception);
      }

   }

   @Nullable
   public V get(K pObj) {
      this.removeExpired();
      return this.map.get(this.getKeyForUser(pObj));
   }

   public void remove(K pEntry) {
      this.map.remove(this.getKeyForUser(pEntry));

      try {
         this.save();
      } catch (IOException ioexception) {
         LOGGER.warn("Could not save the list after removing a user.", (Throwable)ioexception);
      }

   }

   public void remove(UserListEntry<K> p_199042_1_) {
      this.remove(p_199042_1_.getUser());
   }

   public String[] getUserList() {
      return this.map.keySet().toArray(new String[this.map.size()]);
   }

   public boolean isEmpty() {
      return this.map.size() < 1;
   }

   /**
    * Gets the key value for the given object
    */
   protected String getKeyForUser(K pObj) {
      return pObj.toString();
   }

   protected boolean contains(K pEntry) {
      return this.map.containsKey(this.getKeyForUser(pEntry));
   }

   /**
    * Removes expired bans from the list. See {@link BanEntry#hasBanExpired}
    */
   private void removeExpired() {
      List<K> list = Lists.newArrayList();

      for(V v : this.map.values()) {
         if (v.hasExpired()) {
            list.add(v.getUser());
         }
      }

      for(K k : list) {
         this.map.remove(this.getKeyForUser(k));
      }

   }

   protected abstract UserListEntry<K> createEntry(JsonObject pEntryData);

   public Collection<V> getEntries() {
      return this.map.values();
   }

   public void save() throws IOException {
      JsonArray jsonarray = new JsonArray();
      this.map.values().stream().map((p_232646_0_) -> {
         return Util.make(new JsonObject(), p_232646_0_::serialize);
      }).forEach(jsonarray::add);

      try (BufferedWriter bufferedwriter = Files.newWriter(this.file, StandardCharsets.UTF_8)) {
         GSON.toJson((JsonElement)jsonarray, bufferedwriter);
      }

   }

   public void load() throws IOException {
      if (this.file.exists()) {
         try (BufferedReader bufferedreader = Files.newReader(this.file, StandardCharsets.UTF_8)) {
            JsonArray jsonarray = GSON.fromJson(bufferedreader, JsonArray.class);
            this.map.clear();

            for(JsonElement jsonelement : jsonarray) {
               JsonObject jsonobject = JSONUtils.convertToJsonObject(jsonelement, "entry");
               UserListEntry<K> userlistentry = this.createEntry(jsonobject);
               if (userlistentry.getUser() != null) {
                  this.map.put(this.getKeyForUser(userlistentry.getUser()), (V)userlistentry);
               }
            }
         }

      }
   }
}
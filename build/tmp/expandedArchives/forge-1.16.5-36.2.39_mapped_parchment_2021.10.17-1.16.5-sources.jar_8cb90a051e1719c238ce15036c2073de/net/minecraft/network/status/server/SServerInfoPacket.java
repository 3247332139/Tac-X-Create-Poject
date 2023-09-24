package net.minecraft.network.status.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import net.minecraft.client.network.status.IClientStatusNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SServerInfoPacket implements IPacket<IClientStatusNetHandler> {
   public static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(ServerStatusResponse.Version.class, new ServerStatusResponse.Version.Serializer()).registerTypeAdapter(ServerStatusResponse.Players.class, new ServerStatusResponse.Players.Serializer()).registerTypeAdapter(ServerStatusResponse.class, new ServerStatusResponse.Serializer()).registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer()).registerTypeHierarchyAdapter(Style.class, new Style.Serializer()).registerTypeAdapterFactory(new EnumTypeAdapterFactory()).create();
   private ServerStatusResponse status;

   public SServerInfoPacket() {
   }

   public SServerInfoPacket(ServerStatusResponse pStatus) {
      this.status = pStatus;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.status = JSONUtils.fromJson(GSON, p_148837_1_.readUtf(32767), ServerStatusResponse.class);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeUtf(this.status.getJson()); //Forge: Let the response cache the JSON
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientStatusNetHandler pHandler) {
      pHandler.handleStatusResponse(this);
   }

   @OnlyIn(Dist.CLIENT)
   public ServerStatusResponse getStatus() {
      return this.status;
   }
}

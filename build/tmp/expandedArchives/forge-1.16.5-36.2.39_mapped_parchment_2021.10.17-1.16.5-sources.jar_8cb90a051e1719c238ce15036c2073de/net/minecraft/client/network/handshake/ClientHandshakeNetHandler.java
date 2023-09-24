package net.minecraft.client.network.handshake;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.IHandshakeNetHandler;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.ServerLoginNetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientHandshakeNetHandler implements IHandshakeNetHandler {
   private final MinecraftServer server;
   private final NetworkManager connection;

   public ClientHandshakeNetHandler(MinecraftServer p_i45287_1_, NetworkManager p_i45287_2_) {
      this.server = p_i45287_1_;
      this.connection = p_i45287_2_;
   }

   /**
    * There are two recognized intentions for initiating a handshake: logging in and acquiring server status. The
    * NetworkManager's protocol will be reconfigured according to the specified intention, although a login-intention
    * must pass a versioncheck or receive a disconnect otherwise
    */
   public void handleIntention(CHandshakePacket pPacket) {
      if (!net.minecraftforge.fml.server.ServerLifecycleHooks.handleServerLogin(pPacket, this.connection)) return;
      this.connection.setProtocol(pPacket.getIntention());
      this.connection.setListener(new ServerLoginNetHandler(this.server, this.connection));
   }

   /**
    * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
    */
   public void onDisconnect(ITextComponent pReason) {
   }

   /**
    * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
    */
   public NetworkManager getConnection() {
      return this.connection;
   }
}

package net.minecraft.network.login.client;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.IServerLoginNetHandler;
import net.minecraft.util.CryptException;
import net.minecraft.util.CryptManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CEncryptionResponsePacket implements IPacket<IServerLoginNetHandler> {
   private byte[] keybytes = new byte[0];
   private byte[] nonce = new byte[0];

   public CEncryptionResponsePacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CEncryptionResponsePacket(SecretKey pSecretKey, PublicKey pPublicKey, byte[] pNonce) throws CryptException {
      this.keybytes = CryptManager.encryptUsingKey(pPublicKey, pSecretKey.getEncoded());
      this.nonce = CryptManager.encryptUsingKey(pPublicKey, pNonce);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.keybytes = p_148837_1_.readByteArray();
      this.nonce = p_148837_1_.readByteArray();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByteArray(this.keybytes);
      pBuffer.writeByteArray(this.nonce);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerLoginNetHandler pHandler) {
      pHandler.handleKey(this);
   }

   public SecretKey getSecretKey(PrivateKey pKey) throws CryptException {
      return CryptManager.decryptByteToSecretKey(pKey, this.keybytes);
   }

   public byte[] getNonce(PrivateKey pKey) throws CryptException {
      return CryptManager.decryptUsingKey(pKey, this.nonce);
   }
}
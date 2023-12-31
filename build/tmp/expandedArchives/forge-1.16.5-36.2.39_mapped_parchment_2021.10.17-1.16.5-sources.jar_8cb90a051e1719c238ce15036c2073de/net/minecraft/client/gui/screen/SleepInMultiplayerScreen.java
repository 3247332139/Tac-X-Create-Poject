package net.minecraft.client.gui.screen;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SleepInMultiplayerScreen extends ChatScreen {
   public SleepInMultiplayerScreen() {
      super("");
   }

   protected void init() {
      super.init();
      this.addButton(new Button(this.width / 2 - 100, this.height - 40, 200, 20, new TranslationTextComponent("multiplayer.stopSleeping"), (p_212998_1_) -> {
         this.sendWakeUp();
      }));
   }

   public void onClose() {
      this.sendWakeUp();
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 256) {
         this.sendWakeUp();
      } else if (pKeyCode == 257 || pKeyCode == 335) {
         String s = this.input.getValue().trim();
         if (!s.isEmpty()) {
            this.sendMessage(s);
         }

         this.input.setValue("");
         this.minecraft.gui.getChat().resetChatScroll();
         return true;
      }

      return super.keyPressed(pKeyCode, pScanCode, pModifiers);
   }

   private void sendWakeUp() {
      ClientPlayNetHandler clientplaynethandler = this.minecraft.player.connection;
      clientplaynethandler.send(new CEntityActionPacket(this.minecraft.player, CEntityActionPacket.Action.STOP_SLEEPING));
   }
}
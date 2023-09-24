package net.minecraft.client.gui.chat;

import com.mojang.text2speech.Narrator;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.settings.NarratorStatus;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class NarratorChatListener implements IChatListener {
   public static final ITextComponent NO_TITLE = StringTextComponent.EMPTY;
   private static final Logger LOGGER = LogManager.getLogger();
   public static final NarratorChatListener INSTANCE = new NarratorChatListener();
   private final Narrator narrator = Narrator.getNarrator();

   /**
    * Called whenever this listener receives a chat message, if this listener is registered to the given type in {@link
    * net.minecraft.client.gui.GuiIngame#chatListeners chatListeners}
    */
   public void handle(ChatType pChatType, ITextComponent pMessage, UUID pSender) {
      NarratorStatus narratorstatus = getStatus();
      if (narratorstatus != NarratorStatus.OFF && this.narrator.active()) {
         if (narratorstatus == NarratorStatus.ALL || narratorstatus == NarratorStatus.CHAT && pChatType == ChatType.CHAT || narratorstatus == NarratorStatus.SYSTEM && pChatType == ChatType.SYSTEM) {
            ITextComponent itextcomponent;
            if (pMessage instanceof TranslationTextComponent && "chat.type.text".equals(((TranslationTextComponent)pMessage).getKey())) {
               itextcomponent = new TranslationTextComponent("chat.type.text.narrate", ((TranslationTextComponent)pMessage).getArgs());
            } else {
               itextcomponent = pMessage;
            }

            this.doSay(pChatType.shouldInterrupt(), itextcomponent.getString());
         }

      }
   }

   public void sayNow(String pMsg) {
      NarratorStatus narratorstatus = getStatus();
      if (this.narrator.active() && narratorstatus != NarratorStatus.OFF && narratorstatus != NarratorStatus.CHAT && !pMsg.isEmpty()) {
         this.narrator.clear();
         this.doSay(true, pMsg);
      }

   }

   private static NarratorStatus getStatus() {
      return Minecraft.getInstance().options.narratorStatus;
   }

   private void doSay(boolean p_216866_1_, String p_216866_2_) {
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         LOGGER.debug("Narrating: {}", (Object)p_216866_2_.replaceAll("\n", "\\\\n"));
      }

      this.narrator.say(p_216866_2_, p_216866_1_);
   }

   public void updateNarratorStatus(NarratorStatus pStatus) {
      this.clear();
      this.narrator.say((new TranslationTextComponent("options.narrator")).append(" : ").append(pStatus.getName()).getString(), true);
      ToastGui toastgui = Minecraft.getInstance().getToasts();
      if (this.narrator.active()) {
         if (pStatus == NarratorStatus.OFF) {
            SystemToast.addOrUpdate(toastgui, SystemToast.Type.NARRATOR_TOGGLE, new TranslationTextComponent("narrator.toast.disabled"), (ITextComponent)null);
         } else {
            SystemToast.addOrUpdate(toastgui, SystemToast.Type.NARRATOR_TOGGLE, new TranslationTextComponent("narrator.toast.enabled"), pStatus.getName());
         }
      } else {
         SystemToast.addOrUpdate(toastgui, SystemToast.Type.NARRATOR_TOGGLE, new TranslationTextComponent("narrator.toast.disabled"), new TranslationTextComponent("options.narrator.notavailable"));
      }

   }

   public boolean isActive() {
      return this.narrator.active();
   }

   public void clear() {
      if (getStatus() != NarratorStatus.OFF && this.narrator.active()) {
         this.narrator.clear();
      }
   }

   public void destroy() {
      this.narrator.destroy();
   }
}
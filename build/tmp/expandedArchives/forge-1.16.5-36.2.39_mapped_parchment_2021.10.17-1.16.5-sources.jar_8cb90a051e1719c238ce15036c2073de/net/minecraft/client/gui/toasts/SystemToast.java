package net.minecraft.client.gui.toasts;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SystemToast implements IToast {
   private final SystemToast.Type id;
   private ITextComponent title;
   private List<IReorderingProcessor> messageLines;
   private long lastChanged;
   private boolean changed;
   private final int width;

   public SystemToast(SystemToast.Type pId, ITextComponent pTitle, @Nullable ITextComponent pMessage) {
      this(pId, pTitle, nullToEmpty(pMessage), 160);
   }

   public static SystemToast multiline(Minecraft pMinecraft, SystemToast.Type pId, ITextComponent pTitle, ITextComponent pMessage) {
      FontRenderer fontrenderer = pMinecraft.font;
      List<IReorderingProcessor> list = fontrenderer.split(pMessage, 200);
      int i = Math.max(200, list.stream().mapToInt(fontrenderer::width).max().orElse(200));
      return new SystemToast(pId, pTitle, list, i + 30);
   }

   private SystemToast(SystemToast.Type pId, ITextComponent pTitle, List<IReorderingProcessor> pMessageLines, int pWidth) {
      this.id = pId;
      this.title = pTitle;
      this.messageLines = pMessageLines;
      this.width = pWidth;
   }

   private static ImmutableList<IReorderingProcessor> nullToEmpty(@Nullable ITextComponent pMessage) {
      return pMessage == null ? ImmutableList.of() : ImmutableList.of(pMessage.getVisualOrderText());
   }

   public int width() {
      return this.width;
   }

   public IToast.Visibility render(MatrixStack pPoseStack, ToastGui pToastComponent, long p_230444_3_) {
      if (this.changed) {
         this.lastChanged = p_230444_3_;
         this.changed = false;
      }

      pToastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
      RenderSystem.color3f(1.0F, 1.0F, 1.0F);
      int i = this.width();
      int j = 12;
      if (i == 160 && this.messageLines.size() <= 1) {
         pToastComponent.blit(pPoseStack, 0, 0, 0, 64, i, this.height());
      } else {
         int k = this.height() + Math.max(0, this.messageLines.size() - 1) * 12;
         int l = 28;
         int i1 = Math.min(4, k - 28);
         this.renderBackgroundRow(pPoseStack, pToastComponent, i, 0, 0, 28);

         for(int j1 = 28; j1 < k - i1; j1 += 10) {
            this.renderBackgroundRow(pPoseStack, pToastComponent, i, 16, j1, Math.min(16, k - j1 - i1));
         }

         this.renderBackgroundRow(pPoseStack, pToastComponent, i, 32 - i1, k - i1, i1);
      }

      if (this.messageLines == null) {
         pToastComponent.getMinecraft().font.draw(pPoseStack, this.title, 18.0F, 12.0F, -256);
      } else {
         pToastComponent.getMinecraft().font.draw(pPoseStack, this.title, 18.0F, 7.0F, -256);

         for(int k1 = 0; k1 < this.messageLines.size(); ++k1) {
            pToastComponent.getMinecraft().font.draw(pPoseStack, this.messageLines.get(k1), 18.0F, (float)(18 + k1 * 12), -1);
         }
      }

      return p_230444_3_ - this.lastChanged < 5000L ? IToast.Visibility.SHOW : IToast.Visibility.HIDE;
   }

   private void renderBackgroundRow(MatrixStack pPoseStack, ToastGui pToastComponent, int p_238533_3_, int p_238533_4_, int p_238533_5_, int p_238533_6_) {
      int i = p_238533_4_ == 0 ? 20 : 5;
      int j = Math.min(60, p_238533_3_ - i);
      pToastComponent.blit(pPoseStack, 0, p_238533_5_, 0, 64 + p_238533_4_, i, p_238533_6_);

      for(int k = i; k < p_238533_3_ - j; k += 64) {
         pToastComponent.blit(pPoseStack, k, p_238533_5_, 32, 64 + p_238533_4_, Math.min(64, p_238533_3_ - k - j), p_238533_6_);
      }

      pToastComponent.blit(pPoseStack, p_238533_3_ - j, p_238533_5_, 160 - j, 64 + p_238533_4_, j, p_238533_6_);
   }

   public void reset(ITextComponent pTitle, @Nullable ITextComponent pMessage) {
      this.title = pTitle;
      this.messageLines = nullToEmpty(pMessage);
      this.changed = true;
   }

   public SystemToast.Type getToken() {
      return this.id;
   }

   public static void add(ToastGui pToastComponent, SystemToast.Type pId, ITextComponent pTitle, @Nullable ITextComponent pMessage) {
      pToastComponent.addToast(new SystemToast(pId, pTitle, pMessage));
   }

   public static void addOrUpdate(ToastGui pToastComponent, SystemToast.Type pId, ITextComponent pTitle, @Nullable ITextComponent pMessage) {
      SystemToast systemtoast = pToastComponent.getToast(SystemToast.class, pId);
      if (systemtoast == null) {
         add(pToastComponent, pId, pTitle, pMessage);
      } else {
         systemtoast.reset(pTitle, pMessage);
      }

   }

   public static void onWorldAccessFailure(Minecraft pMinecraft, String pMessage) {
      add(pMinecraft.getToasts(), SystemToast.Type.WORLD_ACCESS_FAILURE, new TranslationTextComponent("selectWorld.access_failure"), new StringTextComponent(pMessage));
   }

   public static void onWorldDeleteFailure(Minecraft pMinecraft, String pMessage) {
      add(pMinecraft.getToasts(), SystemToast.Type.WORLD_ACCESS_FAILURE, new TranslationTextComponent("selectWorld.delete_failure"), new StringTextComponent(pMessage));
   }

   public static void onPackCopyFailure(Minecraft pMinecraft, String pMessage) {
      add(pMinecraft.getToasts(), SystemToast.Type.PACK_COPY_FAILURE, new TranslationTextComponent("pack.copyFailure"), new StringTextComponent(pMessage));
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Type {
      TUTORIAL_HINT,
      NARRATOR_TOGGLE,
      WORLD_BACKUP,
      WORLD_GEN_SETTINGS_TRANSFER,
      PACK_LOAD_FAILURE,
      WORLD_ACCESS_FAILURE,
      PACK_COPY_FAILURE;
   }
}
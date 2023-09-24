package net.minecraft.client.gui.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ClientAdvancementManager;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.client.CSeenAdvancementsPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementsScreen extends Screen implements ClientAdvancementManager.IListener {
   private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
   private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
   private static final ITextComponent VERY_SAD_LABEL = new TranslationTextComponent("advancements.sad_label");
   private static final ITextComponent NO_ADVANCEMENTS_LABEL = new TranslationTextComponent("advancements.empty");
   private static final ITextComponent TITLE = new TranslationTextComponent("gui.advancements");
   private final ClientAdvancementManager advancements;
   private final Map<Advancement, AdvancementTabGui> tabs = Maps.newLinkedHashMap();
   private AdvancementTabGui selectedTab;
   private boolean isScrolling;
   private static int tabPage, maxPages;

   public AdvancementsScreen(ClientAdvancementManager p_i47383_1_) {
      super(NarratorChatListener.NO_TITLE);
      this.advancements = p_i47383_1_;
   }

   protected void init() {
      this.tabs.clear();
      this.selectedTab = null;
      this.advancements.setListener(this);
      if (this.selectedTab == null && !this.tabs.isEmpty()) {
         this.advancements.setSelectedTab(this.tabs.values().iterator().next().getAdvancement(), true);
      } else {
         this.advancements.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getAdvancement(), true);
      }
      if (this.tabs.size() > AdvancementTabType.MAX_TABS) {
          int guiLeft = (this.width - 252) / 2;
          int guiTop = (this.height - 140) / 2;
          addButton(new net.minecraft.client.gui.widget.button.Button(guiLeft,            guiTop - 50, 20, 20, new net.minecraft.util.text.StringTextComponent("<"), b -> tabPage = Math.max(tabPage - 1, 0       )));
          addButton(new net.minecraft.client.gui.widget.button.Button(guiLeft + 252 - 20, guiTop - 50, 20, 20, new net.minecraft.util.text.StringTextComponent(">"), b -> tabPage = Math.min(tabPage + 1, maxPages)));
          maxPages = this.tabs.size() / AdvancementTabType.MAX_TABS;
      }
   }

   public void removed() {
      this.advancements.setListener((ClientAdvancementManager.IListener)null);
      ClientPlayNetHandler clientplaynethandler = this.minecraft.getConnection();
      if (clientplaynethandler != null) {
         clientplaynethandler.send(CSeenAdvancementsPacket.closedScreen());
      }

   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (pButton == 0) {
         int i = (this.width - 252) / 2;
         int j = (this.height - 140) / 2;

         for(AdvancementTabGui advancementtabgui : this.tabs.values()) {
            if (advancementtabgui.getPage() == tabPage && advancementtabgui.isMouseOver(i, j, pMouseX, pMouseY)) {
               this.advancements.setSelectedTab(advancementtabgui.getAdvancement(), true);
               break;
            }
         }
      }

      return super.mouseClicked(pMouseX, pMouseY, pButton);
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (this.minecraft.options.keyAdvancements.matches(pKeyCode, pScanCode)) {
         this.minecraft.setScreen((Screen)null);
         this.minecraft.mouseHandler.grabMouse();
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      int i = (this.width - 252) / 2;
      int j = (this.height - 140) / 2;
      this.renderBackground(pMatrixStack);
      if (maxPages != 0) {
          net.minecraft.util.text.ITextComponent page = new net.minecraft.util.text.StringTextComponent(String.format("%d / %d", tabPage + 1, maxPages + 1));
         int width = this.font.width(page);
         RenderSystem.disableLighting();
         this.font.drawShadow(pMatrixStack, page.getVisualOrderText(), i + (252 / 2) - (width / 2), j - 44, -1);
      }
      this.renderInside(pMatrixStack, pMouseX, pMouseY, i, j);
      this.renderWindow(pMatrixStack, i, j);
      this.renderTooltips(pMatrixStack, pMouseX, pMouseY, i, j);
   }

   public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      if (pButton != 0) {
         this.isScrolling = false;
         return false;
      } else {
         if (!this.isScrolling) {
            this.isScrolling = true;
         } else if (this.selectedTab != null) {
            this.selectedTab.scroll(pDragX, pDragY);
         }

         return true;
      }
   }

   private void renderInside(MatrixStack pMatrixStack, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY) {
      AdvancementTabGui advancementtabgui = this.selectedTab;
      if (advancementtabgui == null) {
         fill(pMatrixStack, pOffsetX + 9, pOffsetY + 18, pOffsetX + 9 + 234, pOffsetY + 18 + 113, -16777216);
         int i = pOffsetX + 9 + 117;
         drawCenteredString(pMatrixStack, this.font, NO_ADVANCEMENTS_LABEL, i, pOffsetY + 18 + 56 - 9 / 2, -1);
         drawCenteredString(pMatrixStack, this.font, VERY_SAD_LABEL, i, pOffsetY + 18 + 113 - 9, -1);
      } else {
         RenderSystem.pushMatrix();
         RenderSystem.translatef((float)(pOffsetX + 9), (float)(pOffsetY + 18), 0.0F);
         advancementtabgui.drawContents(pMatrixStack);
         RenderSystem.popMatrix();
         RenderSystem.depthFunc(515);
         RenderSystem.disableDepthTest();
      }
   }

   public void renderWindow(MatrixStack pMatrixStack, int pOffsetX, int pOffsetY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      this.minecraft.getTextureManager().bind(WINDOW_LOCATION);
      this.blit(pMatrixStack, pOffsetX, pOffsetY, 0, 0, 252, 140);
      if (this.tabs.size() > 1) {
         this.minecraft.getTextureManager().bind(TABS_LOCATION);

         for(AdvancementTabGui advancementtabgui : this.tabs.values()) {
            if (advancementtabgui.getPage() == tabPage)
            advancementtabgui.drawTab(pMatrixStack, pOffsetX, pOffsetY, advancementtabgui == this.selectedTab);
         }

         RenderSystem.enableRescaleNormal();
         RenderSystem.defaultBlendFunc();

         for(AdvancementTabGui advancementtabgui1 : this.tabs.values()) {
            if (advancementtabgui1.getPage() == tabPage)
            advancementtabgui1.drawIcon(pOffsetX, pOffsetY, this.itemRenderer);
         }

         RenderSystem.disableBlend();
      }

      this.font.draw(pMatrixStack, TITLE, (float)(pOffsetX + 8), (float)(pOffsetY + 6), 4210752);
   }

   private void renderTooltips(MatrixStack pMatrixStack, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.selectedTab != null) {
         RenderSystem.pushMatrix();
         RenderSystem.enableDepthTest();
         RenderSystem.translatef((float)(pOffsetX + 9), (float)(pOffsetY + 18), 400.0F);
         this.selectedTab.drawTooltips(pMatrixStack, pMouseX - pOffsetX - 9, pMouseY - pOffsetY - 18, pOffsetX, pOffsetY);
         RenderSystem.disableDepthTest();
         RenderSystem.popMatrix();
      }

      if (this.tabs.size() > 1) {
         for(AdvancementTabGui advancementtabgui : this.tabs.values()) {
            if (advancementtabgui.getPage() == tabPage && advancementtabgui.isMouseOver(pOffsetX, pOffsetY, (double)pMouseX, (double)pMouseY)) {
               this.renderTooltip(pMatrixStack, advancementtabgui.getTitle(), pMouseX, pMouseY);
            }
         }
      }

   }

   public void onAddAdvancementRoot(Advancement pAdvancement) {
      AdvancementTabGui advancementtabgui = AdvancementTabGui.create(this.minecraft, this, this.tabs.size(), pAdvancement);
      if (advancementtabgui != null) {
         this.tabs.put(pAdvancement, advancementtabgui);
      }
   }

   public void onRemoveAdvancementRoot(Advancement pAdvancement) {
   }

   public void onAddAdvancementTask(Advancement pAdvancement) {
      AdvancementTabGui advancementtabgui = this.getTab(pAdvancement);
      if (advancementtabgui != null) {
         advancementtabgui.addAdvancement(pAdvancement);
      }

   }

   public void onRemoveAdvancementTask(Advancement pAdvancement) {
   }

   public void onUpdateAdvancementProgress(Advancement pAdvancement, AdvancementProgress pProgress) {
      AdvancementEntryGui advancemententrygui = this.getAdvancementWidget(pAdvancement);
      if (advancemententrygui != null) {
         advancemententrygui.setProgress(pProgress);
      }

   }

   public void onSelectedTabChanged(@Nullable Advancement pAdvancement) {
      this.selectedTab = this.tabs.get(pAdvancement);
   }

   public void onAdvancementsCleared() {
      this.tabs.clear();
      this.selectedTab = null;
   }

   @Nullable
   public AdvancementEntryGui getAdvancementWidget(Advancement pAdvancement) {
      AdvancementTabGui advancementtabgui = this.getTab(pAdvancement);
      return advancementtabgui == null ? null : advancementtabgui.getWidget(pAdvancement);
   }

   @Nullable
   private AdvancementTabGui getTab(Advancement pAdvancement) {
      while(pAdvancement.getParent() != null) {
         pAdvancement = pAdvancement.getParent();
      }

      return this.tabs.get(pAdvancement);
   }
}

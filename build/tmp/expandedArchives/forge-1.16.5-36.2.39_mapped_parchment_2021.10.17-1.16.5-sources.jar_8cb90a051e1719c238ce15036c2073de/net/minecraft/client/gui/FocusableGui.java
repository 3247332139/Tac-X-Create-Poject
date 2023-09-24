package net.minecraft.client.gui;

import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class FocusableGui extends AbstractGui implements INestedGuiEventHandler {
   @Nullable
   private IGuiEventListener focused;
   private boolean isDragging;

   public final boolean isDragging() {
      return this.isDragging;
   }

   public final void setDragging(boolean pDragging) {
      this.isDragging = pDragging;
   }

   @Nullable
   public IGuiEventListener getFocused() {
      return this.focused;
   }

   public void setFocused(@Nullable IGuiEventListener pListener) {
      this.focused = pListener;
   }
}
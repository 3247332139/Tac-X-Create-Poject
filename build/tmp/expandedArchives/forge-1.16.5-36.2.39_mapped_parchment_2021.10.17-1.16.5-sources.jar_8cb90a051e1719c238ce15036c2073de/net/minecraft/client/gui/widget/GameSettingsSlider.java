package net.minecraft.client.gui.widget;

import net.minecraft.client.GameSettings;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GameSettingsSlider extends AbstractSlider {
   protected final GameSettings options;

   protected GameSettingsSlider(GameSettings pOptions, int pX, int pY, int pWidth, int pHeight, double pValue) {
      super(pX, pY, pWidth, pHeight, StringTextComponent.EMPTY, pValue);
      this.options = pOptions;
   }
}
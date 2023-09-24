package net.minecraft.realms;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLabel implements IGuiEventListener {
   private final ITextComponent text;
   private final int x;
   private final int y;
   private final int color;

   public RealmsLabel(ITextComponent pText, int pX, int pY, int pColor) {
      this.text = pText;
      this.x = pX;
      this.y = pY;
      this.color = pColor;
   }

   public void render(Screen p_239560_1_, MatrixStack p_239560_2_) {
      Screen.drawCenteredString(p_239560_2_, Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
   }

   public String getText() {
      return this.text.getString();
   }
}
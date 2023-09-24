package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Monitor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class VirtualScreen implements AutoCloseable {
   private final Minecraft minecraft;
   private final MonitorHandler screenManager;

   public VirtualScreen(Minecraft p_i47668_1_) {
      this.minecraft = p_i47668_1_;
      this.screenManager = new MonitorHandler(Monitor::new);
   }

   public MainWindow newWindow(ScreenSize pScreenSize, @Nullable String pVideoModeName, String pTitle) {
      return new MainWindow(this.minecraft, this.screenManager, pScreenSize, pVideoModeName, pTitle);
   }

   public void close() {
      this.screenManager.shutdown();
   }
}
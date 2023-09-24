package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBulletModel<T extends Entity> extends SegmentedModel<T> {
   private final ModelRenderer main;

   public ShulkerBulletModel() {
      this.texWidth = 64;
      this.texHeight = 32;
      this.main = new ModelRenderer(this);
      this.main.texOffs(0, 0).addBox(-4.0F, -4.0F, -1.0F, 8.0F, 8.0F, 2.0F, 0.0F);
      this.main.texOffs(0, 10).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F, 0.0F);
      this.main.texOffs(20, 0).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F, 0.0F);
      this.main.setPos(0.0F, 0.0F, 0.0F);
   }

   public Iterable<ModelRenderer> parts() {
      return ImmutableList.of(this.main);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.main.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      this.main.xRot = pHeadPitch * ((float)Math.PI / 180F);
   }
}
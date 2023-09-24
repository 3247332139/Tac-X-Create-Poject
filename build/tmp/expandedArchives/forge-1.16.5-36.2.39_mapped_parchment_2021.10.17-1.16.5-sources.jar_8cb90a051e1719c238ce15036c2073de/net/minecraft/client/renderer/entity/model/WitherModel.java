package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherModel<T extends WitherEntity> extends SegmentedModel<T> {
   private final ModelRenderer[] upperBodyParts;
   private final ModelRenderer[] heads;
   private final ImmutableList<ModelRenderer> parts;

   public WitherModel(float p_i46302_1_) {
      this.texWidth = 64;
      this.texHeight = 64;
      this.upperBodyParts = new ModelRenderer[3];
      this.upperBodyParts[0] = new ModelRenderer(this, 0, 16);
      this.upperBodyParts[0].addBox(-10.0F, 3.9F, -0.5F, 20.0F, 3.0F, 3.0F, p_i46302_1_);
      this.upperBodyParts[1] = (new ModelRenderer(this)).setTexSize(this.texWidth, this.texHeight);
      this.upperBodyParts[1].setPos(-2.0F, 6.9F, -0.5F);
      this.upperBodyParts[1].texOffs(0, 22).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, p_i46302_1_);
      this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0F, 1.5F, 0.5F, 11.0F, 2.0F, 2.0F, p_i46302_1_);
      this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0F, 4.0F, 0.5F, 11.0F, 2.0F, 2.0F, p_i46302_1_);
      this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0F, 6.5F, 0.5F, 11.0F, 2.0F, 2.0F, p_i46302_1_);
      this.upperBodyParts[2] = new ModelRenderer(this, 12, 22);
      this.upperBodyParts[2].addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, p_i46302_1_);
      this.heads = new ModelRenderer[3];
      this.heads[0] = new ModelRenderer(this, 0, 0);
      this.heads[0].addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, p_i46302_1_);
      this.heads[1] = new ModelRenderer(this, 32, 0);
      this.heads[1].addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, p_i46302_1_);
      this.heads[1].x = -8.0F;
      this.heads[1].y = 4.0F;
      this.heads[2] = new ModelRenderer(this, 32, 0);
      this.heads[2].addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, p_i46302_1_);
      this.heads[2].x = 10.0F;
      this.heads[2].y = 4.0F;
      Builder<ModelRenderer> builder = ImmutableList.builder();
      builder.addAll(Arrays.asList(this.heads));
      builder.addAll(Arrays.asList(this.upperBodyParts));
      this.parts = builder.build();
   }

   public ImmutableList<ModelRenderer> parts() {
      return this.parts;
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      float f = MathHelper.cos(pAgeInTicks * 0.1F);
      this.upperBodyParts[1].xRot = (0.065F + 0.05F * f) * (float)Math.PI;
      this.upperBodyParts[2].setPos(-2.0F, 6.9F + MathHelper.cos(this.upperBodyParts[1].xRot) * 10.0F, -0.5F + MathHelper.sin(this.upperBodyParts[1].xRot) * 10.0F);
      this.upperBodyParts[2].xRot = (0.265F + 0.1F * f) * (float)Math.PI;
      this.heads[0].yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      this.heads[0].xRot = pHeadPitch * ((float)Math.PI / 180F);
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      for(int i = 1; i < 3; ++i) {
         this.heads[i].yRot = (pEntity.getHeadYRot(i - 1) - pEntity.yBodyRot) * ((float)Math.PI / 180F);
         this.heads[i].xRot = pEntity.getHeadXRot(i - 1) * ((float)Math.PI / 180F);
      }

   }
}
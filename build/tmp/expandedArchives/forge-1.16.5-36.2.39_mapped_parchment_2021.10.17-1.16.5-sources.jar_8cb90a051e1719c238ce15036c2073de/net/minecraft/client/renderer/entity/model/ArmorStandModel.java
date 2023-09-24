package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandModel extends ArmorStandArmorModel {
   private final ModelRenderer bodyStick1;
   private final ModelRenderer bodyStick2;
   private final ModelRenderer shoulderStick;
   private final ModelRenderer basePlate;

   public ArmorStandModel() {
      this(0.0F);
   }

   public ArmorStandModel(float p_i46306_1_) {
      super(p_i46306_1_, 64, 64);
      this.head = new ModelRenderer(this, 0, 0);
      this.head.addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F, p_i46306_1_);
      this.head.setPos(0.0F, 0.0F, 0.0F);
      this.body = new ModelRenderer(this, 0, 26);
      this.body.addBox(-6.0F, 0.0F, -1.5F, 12.0F, 3.0F, 3.0F, p_i46306_1_);
      this.body.setPos(0.0F, 0.0F, 0.0F);
      this.rightArm = new ModelRenderer(this, 24, 0);
      this.rightArm.addBox(-2.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, p_i46306_1_);
      this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
      this.leftArm = new ModelRenderer(this, 32, 16);
      this.leftArm.mirror = true;
      this.leftArm.addBox(0.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, p_i46306_1_);
      this.leftArm.setPos(5.0F, 2.0F, 0.0F);
      this.rightLeg = new ModelRenderer(this, 8, 0);
      this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F, p_i46306_1_);
      this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
      this.leftLeg = new ModelRenderer(this, 40, 16);
      this.leftLeg.mirror = true;
      this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F, p_i46306_1_);
      this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
      this.bodyStick1 = new ModelRenderer(this, 16, 0);
      this.bodyStick1.addBox(-3.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F, p_i46306_1_);
      this.bodyStick1.setPos(0.0F, 0.0F, 0.0F);
      this.bodyStick1.visible = true;
      this.bodyStick2 = new ModelRenderer(this, 48, 16);
      this.bodyStick2.addBox(1.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F, p_i46306_1_);
      this.bodyStick2.setPos(0.0F, 0.0F, 0.0F);
      this.shoulderStick = new ModelRenderer(this, 0, 48);
      this.shoulderStick.addBox(-4.0F, 10.0F, -1.0F, 8.0F, 2.0F, 2.0F, p_i46306_1_);
      this.shoulderStick.setPos(0.0F, 0.0F, 0.0F);
      this.basePlate = new ModelRenderer(this, 0, 32);
      this.basePlate.addBox(-6.0F, 11.0F, -6.0F, 12.0F, 1.0F, 12.0F, p_i46306_1_);
      this.basePlate.setPos(0.0F, 12.0F, 0.0F);
      this.hat.visible = false;
   }

   public void prepareMobModel(ArmorStandEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      this.basePlate.xRot = 0.0F;
      this.basePlate.yRot = ((float)Math.PI / 180F) * -MathHelper.rotLerp(pPartialTick, pEntity.yRotO, pEntity.yRot);
      this.basePlate.zRot = 0.0F;
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(ArmorStandEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
      this.leftArm.visible = pEntity.isShowArms();
      this.rightArm.visible = pEntity.isShowArms();
      this.basePlate.visible = !pEntity.isNoBasePlate();
      this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
      this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
      this.bodyStick1.xRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getX();
      this.bodyStick1.yRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getY();
      this.bodyStick1.zRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getZ();
      this.bodyStick2.xRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getX();
      this.bodyStick2.yRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getY();
      this.bodyStick2.zRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getZ();
      this.shoulderStick.xRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getX();
      this.shoulderStick.yRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getY();
      this.shoulderStick.zRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getZ();
   }

   protected Iterable<ModelRenderer> bodyParts() {
      return Iterables.concat(super.bodyParts(), ImmutableList.of(this.bodyStick1, this.bodyStick2, this.shoulderStick, this.basePlate));
   }

   public void translateToHand(HandSide pSide, MatrixStack pMatrixStack) {
      ModelRenderer modelrenderer = this.getArm(pSide);
      boolean flag = modelrenderer.visible;
      modelrenderer.visible = true;
      super.translateToHand(pSide, pMatrixStack);
      modelrenderer.visible = flag;
   }
}
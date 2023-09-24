package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
   value = Dist.CLIENT,
   _interface = IRendersAsItem.class
)
public abstract class ProjectileItemEntity extends ThrowableEntity implements IRendersAsItem {
   private static final DataParameter<ItemStack> DATA_ITEM_STACK = EntityDataManager.defineId(ProjectileItemEntity.class, DataSerializers.ITEM_STACK);

   public ProjectileItemEntity(EntityType<? extends ProjectileItemEntity> p_i50155_1_, World p_i50155_2_) {
      super(p_i50155_1_, p_i50155_2_);
   }

   public ProjectileItemEntity(EntityType<? extends ProjectileItemEntity> p_i50156_1_, double p_i50156_2_, double p_i50156_4_, double p_i50156_6_, World p_i50156_8_) {
      super(p_i50156_1_, p_i50156_2_, p_i50156_4_, p_i50156_6_, p_i50156_8_);
   }

   public ProjectileItemEntity(EntityType<? extends ProjectileItemEntity> p_i50157_1_, LivingEntity p_i50157_2_, World p_i50157_3_) {
      super(p_i50157_1_, p_i50157_2_, p_i50157_3_);
   }

   public void setItem(ItemStack pStack) {
      if (pStack.getItem() != this.getDefaultItem() || pStack.hasTag()) {
         this.getEntityData().set(DATA_ITEM_STACK, Util.make(pStack.copy(), (p_213883_0_) -> {
            p_213883_0_.setCount(1);
         }));
      }

   }

   protected abstract Item getDefaultItem();

   protected ItemStack getItemRaw() {
      return this.getEntityData().get(DATA_ITEM_STACK);
   }

   public ItemStack getItem() {
      ItemStack itemstack = this.getItemRaw();
      return itemstack.isEmpty() ? new ItemStack(this.getDefaultItem()) : itemstack;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      ItemStack itemstack = this.getItemRaw();
      if (!itemstack.isEmpty()) {
         pCompound.put("Item", itemstack.save(new CompoundNBT()));
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      ItemStack itemstack = ItemStack.of(pCompound.getCompound("Item"));
      this.setItem(itemstack);
   }
}
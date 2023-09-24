package net.minecraft.entity.passive.horse;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public abstract class AbstractChestedHorseEntity extends AbstractHorseEntity {
   private static final DataParameter<Boolean> DATA_ID_CHEST = EntityDataManager.defineId(AbstractChestedHorseEntity.class, DataSerializers.BOOLEAN);

   protected AbstractChestedHorseEntity(EntityType<? extends AbstractChestedHorseEntity> p_i48564_1_, World p_i48564_2_) {
      super(p_i48564_1_, p_i48564_2_);
      this.canGallop = false;
   }

   protected void randomizeAttributes() {
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)this.generateRandomMaxHealth());
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_CHEST, false);
   }

   public static AttributeModifierMap.MutableAttribute createBaseChestedHorseAttributes() {
      return createBaseHorseAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.175F).add(Attributes.JUMP_STRENGTH, 0.5D);
   }

   public boolean hasChest() {
      return this.entityData.get(DATA_ID_CHEST);
   }

   public void setChest(boolean pChested) {
      this.entityData.set(DATA_ID_CHEST, pChested);
   }

   protected int getInventorySize() {
      return this.hasChest() ? 17 : super.getInventorySize();
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return super.getPassengersRidingOffset() - 0.25D;
   }

   protected void dropEquipment() {
      super.dropEquipment();
      if (this.hasChest()) {
         if (!this.level.isClientSide) {
            this.spawnAtLocation(Blocks.CHEST);
         }

         this.setChest(false);
      }

   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("ChestedHorse", this.hasChest());
      if (this.hasChest()) {
         ListNBT listnbt = new ListNBT();

         for(int i = 2; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
               CompoundNBT compoundnbt = new CompoundNBT();
               compoundnbt.putByte("Slot", (byte)i);
               itemstack.save(compoundnbt);
               listnbt.add(compoundnbt);
            }
         }

         pCompound.put("Items", listnbt);
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setChest(pCompound.getBoolean("ChestedHorse"));
      if (this.hasChest()) {
         ListNBT listnbt = pCompound.getList("Items", 10);
         this.createInventory();

         for(int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            if (j >= 2 && j < this.inventory.getContainerSize()) {
               this.inventory.setItem(j, ItemStack.of(compoundnbt));
            }
         }
      }

      this.updateContainerEquipment();
   }

   public boolean setSlot(int pSlotIndex, ItemStack pStack) {
      if (pSlotIndex == 499) {
         if (this.hasChest() && pStack.isEmpty()) {
            this.setChest(false);
            this.createInventory();
            return true;
         }

         if (!this.hasChest() && pStack.getItem() == Blocks.CHEST.asItem()) {
            this.setChest(true);
            this.createInventory();
            return true;
         }
      }

      return super.setSlot(pSlotIndex, pStack);
   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!this.isBaby()) {
         if (this.isTamed() && pPlayer.isSecondaryUseActive()) {
            this.openInventory(pPlayer);
            return ActionResultType.sidedSuccess(this.level.isClientSide);
         }

         if (this.isVehicle()) {
            return super.mobInteract(pPlayer, pHand);
         }
      }

      if (!itemstack.isEmpty()) {
         if (this.isFood(itemstack)) {
            return this.fedFood(pPlayer, itemstack);
         }

         if (!this.isTamed()) {
            this.makeMad();
            return ActionResultType.sidedSuccess(this.level.isClientSide);
         }

         if (!this.hasChest() && itemstack.getItem() == Blocks.CHEST.asItem()) {
            this.setChest(true);
            this.playChestEquipsSound();
            if (!pPlayer.abilities.instabuild) {
               itemstack.shrink(1);
            }

            this.createInventory();
            return ActionResultType.sidedSuccess(this.level.isClientSide);
         }

         if (!this.isBaby() && !this.isSaddled() && itemstack.getItem() == Items.SADDLE) {
            this.openInventory(pPlayer);
            return ActionResultType.sidedSuccess(this.level.isClientSide);
         }
      }

      if (this.isBaby()) {
         return super.mobInteract(pPlayer, pHand);
      } else {
         this.doPlayerRide(pPlayer);
         return ActionResultType.sidedSuccess(this.level.isClientSide);
      }
   }

   protected void playChestEquipsSound() {
      this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
   }

   public int getInventoryColumns() {
      return 5;
   }
}
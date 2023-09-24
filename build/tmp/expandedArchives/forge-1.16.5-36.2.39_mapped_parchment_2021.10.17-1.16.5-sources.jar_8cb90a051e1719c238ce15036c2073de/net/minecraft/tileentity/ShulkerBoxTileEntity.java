package net.minecraft.tileentity;

import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShulkerBoxTileEntity extends LockableLootTileEntity implements ISidedInventory, ITickableTileEntity {
   private static final int[] SLOTS = IntStream.range(0, 27).toArray();
   private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
   private int openCount;
   private ShulkerBoxTileEntity.AnimationStatus animationStatus = ShulkerBoxTileEntity.AnimationStatus.CLOSED;
   private float progress;
   private float progressOld;
   @Nullable
   private DyeColor color;
   private boolean loadColorFromBlock;

   public ShulkerBoxTileEntity(@Nullable DyeColor p_i47242_1_) {
      super(TileEntityType.SHULKER_BOX);
      this.color = p_i47242_1_;
   }

   public ShulkerBoxTileEntity() {
      this((DyeColor)null);
      this.loadColorFromBlock = true;
   }

   public void tick() {
      this.updateAnimation();
      if (this.animationStatus == ShulkerBoxTileEntity.AnimationStatus.OPENING || this.animationStatus == ShulkerBoxTileEntity.AnimationStatus.CLOSING) {
         this.moveCollidedEntities();
      }

   }

   protected void updateAnimation() {
      this.progressOld = this.progress;
      switch(this.animationStatus) {
      case CLOSED:
         this.progress = 0.0F;
         break;
      case OPENING:
         this.progress += 0.1F;
         if (this.progress >= 1.0F) {
            this.moveCollidedEntities();
            this.animationStatus = ShulkerBoxTileEntity.AnimationStatus.OPENED;
            this.progress = 1.0F;
            this.doNeighborUpdates();
         }
         break;
      case CLOSING:
         this.progress -= 0.1F;
         if (this.progress <= 0.0F) {
            this.animationStatus = ShulkerBoxTileEntity.AnimationStatus.CLOSED;
            this.progress = 0.0F;
            this.doNeighborUpdates();
         }
         break;
      case OPENED:
         this.progress = 1.0F;
      }

   }

   public ShulkerBoxTileEntity.AnimationStatus getAnimationStatus() {
      return this.animationStatus;
   }

   public AxisAlignedBB getBoundingBox(BlockState pState) {
      return this.getBoundingBox(pState.getValue(ShulkerBoxBlock.FACING));
   }

   public AxisAlignedBB getBoundingBox(Direction p_190587_1_) {
      float f = this.getProgress(1.0F);
      return VoxelShapes.block().bounds().expandTowards((double)(0.5F * f * (float)p_190587_1_.getStepX()), (double)(0.5F * f * (float)p_190587_1_.getStepY()), (double)(0.5F * f * (float)p_190587_1_.getStepZ()));
   }

   private AxisAlignedBB getTopBoundingBox(Direction p_190588_1_) {
      Direction direction = p_190588_1_.getOpposite();
      return this.getBoundingBox(p_190588_1_).contract((double)direction.getStepX(), (double)direction.getStepY(), (double)direction.getStepZ());
   }

   private void moveCollidedEntities() {
      BlockState blockstate = this.level.getBlockState(this.getBlockPos());
      if (blockstate.getBlock() instanceof ShulkerBoxBlock) {
         Direction direction = blockstate.getValue(ShulkerBoxBlock.FACING);
         AxisAlignedBB axisalignedbb = this.getTopBoundingBox(direction).move(this.worldPosition);
         List<Entity> list = this.level.getEntities((Entity)null, axisalignedbb);
         if (!list.isEmpty()) {
            for(int i = 0; i < list.size(); ++i) {
               Entity entity = list.get(i);
               if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                  double d0 = 0.0D;
                  double d1 = 0.0D;
                  double d2 = 0.0D;
                  AxisAlignedBB axisalignedbb1 = entity.getBoundingBox();
                  switch(direction.getAxis()) {
                  case X:
                     if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                        d0 = axisalignedbb.maxX - axisalignedbb1.minX;
                     } else {
                        d0 = axisalignedbb1.maxX - axisalignedbb.minX;
                     }

                     d0 = d0 + 0.01D;
                     break;
                  case Y:
                     if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                        d1 = axisalignedbb.maxY - axisalignedbb1.minY;
                     } else {
                        d1 = axisalignedbb1.maxY - axisalignedbb.minY;
                     }

                     d1 = d1 + 0.01D;
                     break;
                  case Z:
                     if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                        d2 = axisalignedbb.maxZ - axisalignedbb1.minZ;
                     } else {
                        d2 = axisalignedbb1.maxZ - axisalignedbb.minZ;
                     }

                     d2 = d2 + 0.01D;
                  }

                  entity.move(MoverType.SHULKER_BOX, new Vector3d(d0 * (double)direction.getStepX(), d1 * (double)direction.getStepY(), d2 * (double)direction.getStepZ()));
               }
            }

         }
      }
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return this.itemStacks.size();
   }

   /**
    * See {@link Block#eventReceived} for more information. This must return true serverside before it is called
    * clientside.
    */
   public boolean triggerEvent(int pId, int pType) {
      if (pId == 1) {
         this.openCount = pType;
         if (pType == 0) {
            this.animationStatus = ShulkerBoxTileEntity.AnimationStatus.CLOSING;
            this.doNeighborUpdates();
         }

         if (pType == 1) {
            this.animationStatus = ShulkerBoxTileEntity.AnimationStatus.OPENING;
            this.doNeighborUpdates();
         }

         return true;
      } else {
         return super.triggerEvent(pId, pType);
      }
   }

   private void doNeighborUpdates() {
      this.getBlockState().updateNeighbourShapes(this.getLevel(), this.getBlockPos(), 3);
   }

   public void startOpen(PlayerEntity pPlayer) {
      if (!pPlayer.isSpectator()) {
         if (this.openCount < 0) {
            this.openCount = 0;
         }

         ++this.openCount;
         this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
         if (this.openCount == 1) {
            this.level.playSound((PlayerEntity)null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
         }
      }

   }

   public void stopOpen(PlayerEntity pPlayer) {
      if (!pPlayer.isSpectator()) {
         --this.openCount;
         this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
         if (this.openCount <= 0) {
            this.level.playSound((PlayerEntity)null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
         }
      }

   }

   protected ITextComponent getDefaultName() {
      return new TranslationTextComponent("container.shulkerBox");
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      super.load(p_230337_1_, p_230337_2_);
      this.loadFromTag(p_230337_2_);
   }

   public CompoundNBT save(CompoundNBT pCompound) {
      super.save(pCompound);
      return this.saveToTag(pCompound);
   }

   public void loadFromTag(CompoundNBT pTag) {
      this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(pTag) && pTag.contains("Items", 9)) {
         ItemStackHelper.loadAllItems(pTag, this.itemStacks);
      }

   }

   public CompoundNBT saveToTag(CompoundNBT pTag) {
      if (!this.trySaveLootTable(pTag)) {
         ItemStackHelper.saveAllItems(pTag, this.itemStacks, false);
      }

      return pTag;
   }

   protected NonNullList<ItemStack> getItems() {
      return this.itemStacks;
   }

   protected void setItems(NonNullList<ItemStack> pItems) {
      this.itemStacks = pItems;
   }

   public int[] getSlotsForFace(Direction pSide) {
      return SLOTS;
   }

   /**
    * Returns true if automation can insert the given item in the given slot from the given side.
    */
   public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
      return !(Block.byItem(pItemStack.getItem()) instanceof ShulkerBoxBlock);
   }

   /**
    * Returns true if automation can extract the given item in the given slot from the given side.
    */
   public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
      return true;
   }

   public float getProgress(float pPartialTicks) {
      return MathHelper.lerp(pPartialTicks, this.progressOld, this.progress);
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public DyeColor getColor() {
      if (this.loadColorFromBlock) {
         this.color = ShulkerBoxBlock.getColorFromBlock(this.getBlockState().getBlock());
         this.loadColorFromBlock = false;
      }

      return this.color;
   }

   protected Container createMenu(int pId, PlayerInventory pPlayer) {
      return new ShulkerBoxContainer(pId, pPlayer, this);
   }

   public boolean isClosed() {
      return this.animationStatus == ShulkerBoxTileEntity.AnimationStatus.CLOSED;
   }

   @Override
   protected net.minecraftforge.items.IItemHandler createUnSidedHandler() {
      return new net.minecraftforge.items.wrapper.SidedInvWrapper(this, Direction.UP);
   }

   public static enum AnimationStatus {
      CLOSED,
      OPENING,
      OPENED,
      CLOSING;
   }
}

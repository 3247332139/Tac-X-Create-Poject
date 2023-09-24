package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ContainerBlock extends Block implements ITileEntityProvider {
   protected ContainerBlock(AbstractBlock.Properties p_i48446_1_) {
      super(p_i48446_1_);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.INVISIBLE;
   }

   /**
    * Called on server when World#addBlockEvent is called. If server returns true, then also called on the client. On
    * the Server, this may perform additional changes to the world, like pistons replacing the block with an extended
    * base. On the client, the update may involve replacing tile entities or effects such as sounds or particles
    * @deprecated call via {@link IBlockState#onBlockEventReceived(World,BlockPos,int,int)} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean triggerEvent(BlockState pState, World pLevel, BlockPos pPos, int pId, int pParam) {
      super.triggerEvent(pState, pLevel, pPos, pId, pParam);
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      return tileentity == null ? false : tileentity.triggerEvent(pId, pParam);
   }

   @Nullable
   public INamedContainerProvider getMenuProvider(BlockState pState, World pLevel, BlockPos pPos) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider)tileentity : null;
   }
}
package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.BlastFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlastFurnaceBlock extends AbstractFurnaceBlock {
   public BlastFurnaceBlock(AbstractBlock.Properties p_i49992_1_) {
      super(p_i49992_1_);
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new BlastFurnaceTileEntity();
   }

   /**
    * Interface for handling interaction with blocks that impliment AbstractFurnaceBlock. Called in onBlockActivated
    * inside AbstractFurnaceBlock.
    */
   protected void openContainer(World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof BlastFurnaceTileEntity) {
         pPlayer.openMenu((INamedContainerProvider)tileentity);
         pPlayer.awardStat(Stats.INTERACT_WITH_BLAST_FURNACE);
      }

   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(LIT)) {
         double d0 = (double)pPos.getX() + 0.5D;
         double d1 = (double)pPos.getY();
         double d2 = (double)pPos.getZ() + 0.5D;
         if (pRand.nextDouble() < 0.1D) {
            pLevel.playLocalSound(d0, d1, d2, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
         }

         Direction direction = pState.getValue(FACING);
         Direction.Axis direction$axis = direction.getAxis();
         double d3 = 0.52D;
         double d4 = pRand.nextDouble() * 0.6D - 0.3D;
         double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
         double d6 = pRand.nextDouble() * 9.0D / 16.0D;
         double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;
         pLevel.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
      }
   }
}
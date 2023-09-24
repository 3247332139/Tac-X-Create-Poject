package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.CartographyContainer;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CartographyTableBlock extends Block {
   private static final ITextComponent CONTAINER_TITLE = new TranslationTextComponent("container.cartography_table");

   public CartographyTableBlock(AbstractBlock.Properties p_i49987_1_) {
      super(p_i49987_1_);
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (pLevel.isClientSide) {
         return ActionResultType.SUCCESS;
      } else {
         pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
         pPlayer.awardStat(Stats.INTERACT_WITH_CARTOGRAPHY_TABLE);
         return ActionResultType.CONSUME;
      }
   }

   @Nullable
   public INamedContainerProvider getMenuProvider(BlockState pState, World pLevel, BlockPos pPos) {
      return new SimpleNamedContainerProvider((p_220267_2_, p_220267_3_, p_220267_4_) -> {
         return new CartographyContainer(p_220267_2_, p_220267_3_, IWorldPosCallable.create(pLevel, pPos));
      }, CONTAINER_TITLE);
   }
}
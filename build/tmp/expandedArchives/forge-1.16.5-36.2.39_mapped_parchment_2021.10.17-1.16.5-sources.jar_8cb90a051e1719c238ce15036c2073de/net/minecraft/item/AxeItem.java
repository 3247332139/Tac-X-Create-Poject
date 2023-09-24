package net.minecraft.item;

import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AxeItem extends ToolItem {
   private static final Set<Material> DIGGABLE_MATERIALS = Sets.newHashSet(Material.WOOD, Material.NETHER_WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.BAMBOO, Material.VEGETABLE);
   private static final Set<Block> OTHER_DIGGABLE_BLOCKS = Sets.newHashSet(Blocks.LADDER, Blocks.SCAFFOLDING, Blocks.OAK_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.BIRCH_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.ACACIA_BUTTON, Blocks.CRIMSON_BUTTON, Blocks.WARPED_BUTTON);
   protected static final Map<Block, Block> STRIPABLES = (new Builder<Block, Block>()).put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD).put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG).put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD).put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG).put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD).put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG).put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD).put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG).put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD).put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG).put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD).put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG).put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM).put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE).put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM).put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE).build();

   public AxeItem(IItemTier pTier, float pAttackDamageModifier, float pAttackSpeedModifier, Item.Properties pProperties) {
      super(pAttackDamageModifier, pAttackSpeedModifier, pTier, OTHER_DIGGABLE_BLOCKS, pProperties.addToolType(net.minecraftforge.common.ToolType.AXE, pTier.getLevel()));
   }

   public float getDestroySpeed(ItemStack pStack, BlockState pState) {
      Material material = pState.getMaterial();
      return DIGGABLE_MATERIALS.contains(material) ? this.speed : super.getDestroySpeed(pStack, pState);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = world.getBlockState(blockpos);
      BlockState block = blockstate.getToolModifiedState(world, blockpos, pContext.getPlayer(), pContext.getItemInHand(), net.minecraftforge.common.ToolType.AXE);
      if (block != null) {
         PlayerEntity playerentity = pContext.getPlayer();
         world.playSound(playerentity, blockpos, SoundEvents.AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
         if (!world.isClientSide) {
            world.setBlock(blockpos, block, 11);
            if (playerentity != null) {
               pContext.getItemInHand().hurtAndBreak(1, playerentity, (p_220040_1_) -> {
                  p_220040_1_.broadcastBreakEvent(pContext.getHand());
               });
            }
         }

         return ActionResultType.sidedSuccess(world.isClientSide);
      } else {
         return ActionResultType.PASS;
      }
   }

   @javax.annotation.Nullable
   public static BlockState getAxeStrippingState(BlockState originalState) {
      Block block = STRIPABLES.get(originalState.getBlock());
      return block != null ? block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, originalState.getValue(RotatedPillarBlock.AXIS)) : null;
   }
}

package net.minecraft.client.renderer.color;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.state.Property;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.GrassColors;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockColors {
   // FORGE: Use RegistryDelegates as non-Vanilla block ids are not constant
   private final java.util.Map<net.minecraftforge.registries.IRegistryDelegate<Block>, IBlockColor> blockColors = new java.util.HashMap<>();
   private final Map<Block, Set<Property<?>>> coloringStates = Maps.newHashMap();

   public static BlockColors createDefault() {
      BlockColors blockcolors = new BlockColors();
      blockcolors.register((p_228065_0_, p_228065_1_, p_228065_2_, p_228065_3_) -> {
         return p_228065_1_ != null && p_228065_2_ != null ? BiomeColors.getAverageGrassColor(p_228065_1_, p_228065_0_.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? p_228065_2_.below() : p_228065_2_) : -1;
      }, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
      blockcolors.addColoringState(DoublePlantBlock.HALF, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
      blockcolors.register((p_228064_0_, p_228064_1_, p_228064_2_, p_228064_3_) -> {
         return p_228064_1_ != null && p_228064_2_ != null ? BiomeColors.getAverageGrassColor(p_228064_1_, p_228064_2_) : GrassColors.get(0.5D, 1.0D);
      }, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.GRASS, Blocks.POTTED_FERN);
      blockcolors.register((p_228063_0_, p_228063_1_, p_228063_2_, p_228063_3_) -> {
         return FoliageColors.getEvergreenColor();
      }, Blocks.SPRUCE_LEAVES);
      blockcolors.register((p_228062_0_, p_228062_1_, p_228062_2_, p_228062_3_) -> {
         return FoliageColors.getBirchColor();
      }, Blocks.BIRCH_LEAVES);
      blockcolors.register((p_228061_0_, p_228061_1_, p_228061_2_, p_228061_3_) -> {
         return p_228061_1_ != null && p_228061_2_ != null ? BiomeColors.getAverageFoliageColor(p_228061_1_, p_228061_2_) : FoliageColors.getDefaultColor();
      }, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE);
      blockcolors.register((p_228060_0_, p_228060_1_, p_228060_2_, p_228060_3_) -> {
         return p_228060_1_ != null && p_228060_2_ != null ? BiomeColors.getAverageWaterColor(p_228060_1_, p_228060_2_) : -1;
      }, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.CAULDRON);
      blockcolors.register((p_228059_0_, p_228059_1_, p_228059_2_, p_228059_3_) -> {
         return RedstoneWireBlock.getColorForPower(p_228059_0_.getValue(RedstoneWireBlock.POWER));
      }, Blocks.REDSTONE_WIRE);
      blockcolors.addColoringState(RedstoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
      blockcolors.register((p_228058_0_, p_228058_1_, p_228058_2_, p_228058_3_) -> {
         return p_228058_1_ != null && p_228058_2_ != null ? BiomeColors.getAverageGrassColor(p_228058_1_, p_228058_2_) : -1;
      }, Blocks.SUGAR_CANE);
      blockcolors.register((p_228057_0_, p_228057_1_, p_228057_2_, p_228057_3_) -> {
         return 14731036;
      }, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
      blockcolors.register((p_228056_0_, p_228056_1_, p_228056_2_, p_228056_3_) -> {
         int i = p_228056_0_.getValue(StemBlock.AGE);
         int j = i * 32;
         int k = 255 - i * 8;
         int l = i * 4;
         return j << 16 | k << 8 | l;
      }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      blockcolors.addColoringState(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      blockcolors.register((p_228055_0_, p_228055_1_, p_228055_2_, p_228055_3_) -> {
         return p_228055_1_ != null && p_228055_2_ != null ? 2129968 : 7455580;
      }, Blocks.LILY_PAD);
      net.minecraftforge.client.ForgeHooksClient.onBlockColorsInit(blockcolors);
      return blockcolors;
   }

   public int getColor(BlockState pState, World pLevel, BlockPos pBlockPos) {
      IBlockColor iblockcolor = this.blockColors.get(pState.getBlock().delegate);
      if (iblockcolor != null) {
         return iblockcolor.getColor(pState, (IBlockDisplayReader)null, (BlockPos)null, 0);
      } else {
         MaterialColor materialcolor = pState.getMapColor(pLevel, pBlockPos);
         return materialcolor != null ? materialcolor.col : -1;
      }
   }

   public int getColor(BlockState pBlockState, @Nullable IBlockDisplayReader pLightReader, @Nullable BlockPos pBlockPos, int pTintIndex) {
      IBlockColor iblockcolor = this.blockColors.get(pBlockState.getBlock().delegate);
      return iblockcolor == null ? -1 : iblockcolor.getColor(pBlockState, pLightReader, pBlockPos, pTintIndex);
   }

   public void register(IBlockColor pBlockColor, Block... pBlocks) {
      for(Block block : pBlocks) {
         this.blockColors.put(block.delegate, pBlockColor);
      }

   }

   private void addColoringStates(Set<Property<?>> pProperties, Block... pBlocks) {
      for(Block block : pBlocks) {
         this.coloringStates.put(block, pProperties);
      }

   }

   private void addColoringState(Property<?> pProperty, Block... pBlocks) {
      this.addColoringStates(ImmutableSet.of(pProperty), pBlocks);
   }

   public Set<Property<?>> getColoringProperties(Block pBlock) {
      return this.coloringStates.getOrDefault(pBlock, ImmutableSet.of());
   }
}

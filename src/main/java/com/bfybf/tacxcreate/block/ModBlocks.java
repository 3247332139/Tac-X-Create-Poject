package com.bfybf.tacxcreate.block;

import com.bfybf.tacxcreate.item.ModItemGroup;
import com.bfybf.tacxcreate.item.Moditems;
import com.bfybf.tacxcreate.tacxcreate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =DeferredRegister.create(ForgeRegistries.BLOCKS, tacxcreate.MOD_ID);

    public static final RegistryObject<Block> UNCRAFTING_TABLE = registerBlock("uncraftingtable",
            () -> new Block(AbstractBlock.Properties.create(Material.WOOD).harvestLevel(0).harvestTool(ToolType.AXE)
                    .hardnessAndResistance(2.5f)));
    private static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T>block){
        RegistryObject<T> tRegistryObject =BLOCKS.register(name,block);
        registerBlockItem(name, tRegistryObject);
        return  tRegistryObject;
    }
    private static <T extends  Block> void registerBlockItem(String name, Supplier<T>block){
        Moditems.ITEMS.register(name,() -> new BlockItem(block.get(),new Item.Properties().group(ModItemGroup.TACXCREATE_TAB)));
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}

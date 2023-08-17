package com.bfybf.tacxcreate.item;

import com.bfybf.tacxcreate.tacxcreate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Moditems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, tacxcreate.MOD_ID);

    public static final RegistryObject<Item> NEKO_EGG = ITEMS.register("nekoegg",
            () -> new Item(new Item.Properties().group(ModItemGroup.TACXCREATE_TAB)));
    public static  void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}

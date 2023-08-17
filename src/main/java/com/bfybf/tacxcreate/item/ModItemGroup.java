package com.bfybf.tacxcreate.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroup {
    public static final ItemGroup TACXCREATE_TAB = new ItemGroup("tacxcreatetab") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Moditems.NEKO_EGG.get());
        }
    };
}

package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class BambooSpikesItem extends BlockItem {
    public BambooSpikesItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            items.add(new ItemStack(ModRegistry.BAMBOO_SPIKES_ITEM.get()));
        }
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return 150;
    }
}

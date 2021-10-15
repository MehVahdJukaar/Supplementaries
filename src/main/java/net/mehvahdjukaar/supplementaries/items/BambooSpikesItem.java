package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import net.minecraft.world.item.Item.Properties;

public class BambooSpikesItem extends BlockItem {
    public BambooSpikesItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            items.add(new ItemStack(ModRegistry.BAMBOO_SPIKES_ITEM.get()));
        }
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return 150;
    }
}

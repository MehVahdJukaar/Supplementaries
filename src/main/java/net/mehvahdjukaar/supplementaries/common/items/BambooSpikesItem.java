package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.selene.items.WoodBasedBlockItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class BambooSpikesItem extends WoodBasedBlockItem {
    public BambooSpikesItem(Block blockIn, Properties builder) {
        super(blockIn, builder, 150);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            items.add(new ItemStack(ModRegistry.BAMBOO_SPIKES_ITEM.get()));
        }
    }
}

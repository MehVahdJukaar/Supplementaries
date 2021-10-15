package net.mehvahdjukaar.supplementaries.items;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.Item.Properties;

public class BurnableBlockItem extends BlockItem {
    private final int burnTime;
    public BurnableBlockItem(Block blockIn, Properties builder, int burnTicks) {
        super(blockIn, builder);
        this.burnTime = burnTicks;
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return this.burnTime;
    }
}

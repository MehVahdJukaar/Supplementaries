package net.mehvahdjukaar.supplementaries.items;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

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

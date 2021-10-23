package net.mehvahdjukaar.supplementaries.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class BurnableBlockItem extends BlockItem {
    private final int burnTime;

    public BurnableBlockItem(Block blockIn, Properties builder, int burnTicks) {
        super(blockIn, builder);
        this.burnTime = burnTicks;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return this.burnTime;
    }
}

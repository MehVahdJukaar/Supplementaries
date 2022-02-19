package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.selene.util.BlockSetHandler;
import net.mehvahdjukaar.selene.util.WoodSetType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class WoodBasedBlockItem extends BlockItem {
    private final int burnTime;
    private final WoodSetType woodType;


    public WoodBasedBlockItem(Block blockIn, Properties builder, int burnTicks) {
        this(blockIn, builder, burnTicks, WoodSetType.OAK_WOOD_TYPE);
    }

    public WoodBasedBlockItem(Block blockIn, Properties builder, int burnTicks, WoodSetType woodType) {
        super(blockIn, builder);
        this.woodType = woodType;
        this.burnTime = woodType.canBurn() ? burnTicks : 0;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return this.burnTime;
    }

    @Override
    protected boolean allowdedIn(CreativeModeTab pCategory) {
        if (woodType.plankBlock.asItem().getItemCategory() == null) return false;
        return super.allowdedIn(pCategory);
    }
}

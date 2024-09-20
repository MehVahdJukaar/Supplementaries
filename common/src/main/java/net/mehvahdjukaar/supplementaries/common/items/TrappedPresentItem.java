package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class TrappedPresentItem extends PresentItem {
    private final Item normal;

    public TrappedPresentItem(Block block, Properties properties) {
        super(block, properties);
        this.normal = ModRegistry.PRESENTS.get(this.getColor()).get().asItem();
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.has(DataComponents.CONTAINER)) {
            return this.normal.getName(stack);
        }
        return super.getName(stack);
    }
}

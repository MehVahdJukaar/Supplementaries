package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.block.IColored;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AbstractPresentBlock;
import net.mehvahdjukaar.supplementaries.common.components.PresentAddress;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class PresentItem extends BlockItem implements IColored {

    public PresentItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        PresentAddress address = stack.get(ModComponents.ADDRESS.get());
        if (address != null) {
            address.addToTooltip(context, tooltipComponents::add, tooltipFlag);
        }
    }

    @Override
    public DyeColor getColor() {
        return ((AbstractPresentBlock) this.getBlock()).getColor();
    }

    @Override
    public boolean supportsBlankColor() {
        return true;
    }
}

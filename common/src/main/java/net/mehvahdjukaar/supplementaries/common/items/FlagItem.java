package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.block.IColored;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class FlagItem extends BlockItem implements IColored {

    public FlagItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public DyeColor getColor() {
        return ((FlagBlock) this.getBlock()).getColor();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        BannerItem.appendHoverTextFromBannerBlockEntityTag(stack, tooltipComponents);
    }
}

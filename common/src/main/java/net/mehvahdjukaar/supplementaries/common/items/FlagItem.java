package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.block.IColored;
import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.ILoomItem;
import net.mehvahdjukaar.supplementaries.client.renderers.FlagLoomRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Supplier;

public class FlagItem extends BlockItem implements IColored, ILoomItem {

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

    @Override
    public DyeColor getLoomBaseColor(ItemStack stack) {
        return this.getColor();
    }

    @Override
    public ResourceLocation getLoomSlotIcon() {
        return ModTextures.FLAG_ICON;
    }

    @Override
    public Supplier<LoomItemRenderer> getLoomRenderer() {
        return () -> FlagLoomRenderer.INSTANCE;
    }
}

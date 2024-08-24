package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.BubbleBlockItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class BubbleBlockItem extends BlockItem implements ICustomItemRendererProvider {

    public BubbleBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Supplier<ItemStackRenderer> getRendererFactory() {
        return BubbleBlockItemRenderer::new;
    }
}

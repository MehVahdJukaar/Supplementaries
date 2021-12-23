package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.client.renderers.items.BubbleBlockItemRenderer;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

public class BubbleBlockItem extends BlockItem {
    public BubbleBlockItem(Block block, Properties properties) {
        super(block, properties);

    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        ClientRegistry.registerISTER(consumer, BubbleBlockItemRenderer::new);
    }

}

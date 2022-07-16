package net.mehvahdjukaar.supplementaries.common.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.BubbleBlockItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.apache.commons.compress.archivers.sevenz.CLI;

import java.util.function.Consumer;

public class BubbleBlockItem extends BlockItem implements ICustomItemRendererProvider {

    public BubbleBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public ItemStackRenderer createRenderer() {
        return new BubbleBlockItemRenderer();
    }
}

package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.client.renderers.items.BlackboardItemRenderer;
import net.mehvahdjukaar.supplementaries.forge.SupplementariesForgeClient;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

public abstract class SelfBlackboardItemMixin extends BlockItem {


    public SelfBlackboardItemMixin(Block arg, Properties arg2) {
        super(arg, arg2);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        SupplementariesForgeClient.registerISTER(consumer, BlackboardItemRenderer::new);
    }

}

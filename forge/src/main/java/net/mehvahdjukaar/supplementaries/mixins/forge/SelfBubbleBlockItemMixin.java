package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.client.renderers.items.BubbleBlockItemRenderer;
import net.mehvahdjukaar.supplementaries.common.items.BubbleBlockItem;
import net.mehvahdjukaar.supplementaries.forge.SupplementariesForgeClient;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(BubbleBlockItem.class)
public abstract class SelfBubbleBlockItemMixin extends Item {

    public SelfBubbleBlockItemMixin(Properties arg) {
        super(arg);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        SupplementariesForgeClient.registerISTER(consumer, BubbleBlockItemRenderer::new);
    }
}

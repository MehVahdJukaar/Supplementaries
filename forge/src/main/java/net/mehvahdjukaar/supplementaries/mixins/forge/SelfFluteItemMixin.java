package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.client.renderers.items.FluteItemRenderer;
import net.mehvahdjukaar.supplementaries.common.items.FluteItem;
import net.mehvahdjukaar.supplementaries.forge.SupplementariesForgeClient;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.IItemRenderProperties;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(FluteItem.class)
public  abstract class SelfFluteItemMixin extends Item {

    public SelfFluteItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        SupplementariesForgeClient.registerISTER(consumer, FluteItemRenderer::new);
    }
}

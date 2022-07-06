package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.client.renderers.items.FlagItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.FluteItemRenderer;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.common.items.FluteItem;
import net.mehvahdjukaar.supplementaries.forge.SupplementariesForgeClient;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.IItemRenderProperties;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(FlagItem.class)
public  abstract class SelfFlagItemMixin extends Item {

    public SelfFlagItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        SupplementariesForgeClient.registerISTER(consumer, FlagItemRenderer::new);
    }
}

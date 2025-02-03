package net.mehvahdjukaar.supplementaries.integration;

import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip.SackPreviewProvider;
import net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip.SafePreviewProvider;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxTooltipCompat implements ShulkerBoxTooltipApi {

    @Override
    public void registerProviders(PreviewProviderRegistry registry) {
        registry.register(new ResourceLocation(Supplementaries.MOD_ID, "safe"), new SafePreviewProvider(),
            ModRegistry.SAFE_ITEM.get());
        registry.register(new ResourceLocation(Supplementaries.MOD_ID, "sack"), new SackPreviewProvider(),
            ModRegistry.SACK_ITEM.get());
    }

    public static boolean hasPreviewProvider(ItemStack stack) {
        return ShulkerBoxTooltipApi.getPreviewProviderForStack(stack) != null;
    }

    @ExpectPlatform
    public static void registerPlugin() {
        throw new AssertionError();
    }

}

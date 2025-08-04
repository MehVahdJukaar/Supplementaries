package net.mehvahdjukaar.supplementaries.integration;

import com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip.LunchBasketPreviewProvider;
import net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip.SackPreviewProvider;
import net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip.SafePreviewProvider;
import net.mehvahdjukaar.supplementaries.reg.ModConstants;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxTooltipCompat implements ShulkerBoxTooltipApi {

    @Override
    public void registerProviders(PreviewProviderRegistry registry) {
        registry.register(Supplementaries.res(ModConstants.SAFE_NAME), new SafePreviewProvider(),
            ModRegistry.SAFE_ITEM.get());
        registry.register(Supplementaries.res(ModConstants.SACK_NAME), new SackPreviewProvider(),
            ModRegistry.SACK_ITEM.get());
        registry.register(Supplementaries.res(ModConstants.LUNCH_BASKET_NAME), new LunchBasketPreviewProvider(),
                ModRegistry.LUNCH_BASKET_ITEM.get());
    }

    public static boolean hasPreviewProvider(ItemStack stack) {
        return ShulkerBoxTooltipApi.getPreviewProviderForStack(stack) != null;
    }

    @ExpectPlatform
    public static void setup() {
        throw new AssertionError();
    }

}

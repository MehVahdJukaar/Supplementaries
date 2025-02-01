package net.mehvahdjukaar.supplementaries.integration.forge;

import com.misterpemodder.shulkerboxtooltip.api.forge.ShulkerBoxTooltipPlugin;
import net.mehvahdjukaar.supplementaries.integration.ShulkerBoxTooltipCompat;
import net.minecraftforge.fml.ModLoadingContext;

public class ShulkerBoxTooltipCompatImpl {

    public static void registerPlugin() {
        ModLoadingContext.get().registerExtensionPoint(ShulkerBoxTooltipPlugin.class,
            () -> new ShulkerBoxTooltipPlugin(ShulkerBoxTooltipCompat::new));
    }

}

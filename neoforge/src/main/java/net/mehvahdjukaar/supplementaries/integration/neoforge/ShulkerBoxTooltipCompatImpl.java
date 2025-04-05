package net.mehvahdjukaar.supplementaries.integration.neoforge;

import com.misterpemodder.shulkerboxtooltip.api.neoforge.ShulkerBoxTooltipPlugin;
import net.mehvahdjukaar.supplementaries.integration.ShulkerBoxTooltipCompat;
import net.neoforged.fml.ModLoadingContext;

public class ShulkerBoxTooltipCompatImpl {

    public static void setup() {
        ModLoadingContext.get().registerExtensionPoint(ShulkerBoxTooltipPlugin.class,
            () -> new ShulkerBoxTooltipPlugin(ShulkerBoxTooltipCompat::new));
    }

}

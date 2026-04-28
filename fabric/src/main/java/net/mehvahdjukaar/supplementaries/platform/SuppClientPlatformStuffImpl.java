package net.mehvahdjukaar.supplementaries.platform;

import net.mehvahdjukaar.supplementaries.client.renderers.platform.ModSlider;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.ISlider;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.network.chat.Component;

public class SuppClientPlatformStuffImpl {

    public static ISlider createSlider(int x, int y, int width, int height, Component prefix, Component suffix,
                                       double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString) {
        return new ModSlider(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue);
    }

    public static boolean hasFixedAO() {
        return CompatHandler.SODIUM || CompatHandler.EMBEDDIUM;
    }

}

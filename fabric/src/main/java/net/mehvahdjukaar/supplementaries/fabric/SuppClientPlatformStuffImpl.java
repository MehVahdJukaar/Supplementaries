package net.mehvahdjukaar.supplementaries.fabric;

import net.mehvahdjukaar.supplementaries.client.renderers.fabric.ModSlider;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.ISlider;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class SuppClientPlatformStuffImpl {

    public static RenderType staticNoise(ResourceLocation location) {
        return RenderType.entityCutout(location);
    }

    public static ShaderInstance getNoiseShader() {
        return null;
    }

    public static ISlider createSlider(int x, int y, int width, int height, Component prefix, Component suffix,
                                       double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString) {
        return new ModSlider(x,y,width, height, prefix,suffix, minValue, maxValue, currentValue);
    }

    public static ShaderInstance getEntityOffsetShader() {
        return null;
    }

    public static boolean hasFixedAO() {
        return CompatHandler.SODIUM || CompatHandler.EMBEDDIUM;
    }

}

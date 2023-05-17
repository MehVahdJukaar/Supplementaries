package net.mehvahdjukaar.supplementaries;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class SuppClientPlatformStuff {
    @ExpectPlatform
    public static RenderType staticNoise(ResourceLocation location) {
        throw new ArrayStoreException();
    }

    @ExpectPlatform
    public static ShaderInstance getNoiseShader() {
        throw new ArrayStoreException();
    }

    @ExpectPlatform
    public static AbstractSliderButton createSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue,
                                                    double currentValue, double stepSize, int precision, boolean drawString) {
        throw new ArrayStoreException();
    }
}

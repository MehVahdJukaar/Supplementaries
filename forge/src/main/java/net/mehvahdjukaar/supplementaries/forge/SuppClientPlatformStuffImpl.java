package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.supplementaries.client.renderers.forge.ModSlider;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

public class SuppClientPlatformStuffImpl {
    public static RenderType staticNoise(ResourceLocation location) {
        return SupplementariesForgeClient.staticNoise(location);
    }

    public static ShaderInstance getNoiseShader() {
        return SupplementariesForgeClient.getStaticNoiseShader();
    }

    public static AbstractSliderButton createSlider(int x, int y, int width, int height, Component prefix, Component suffix,
                                                    double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString) {
        return new ModSlider(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize, precision, drawString);
    }
}

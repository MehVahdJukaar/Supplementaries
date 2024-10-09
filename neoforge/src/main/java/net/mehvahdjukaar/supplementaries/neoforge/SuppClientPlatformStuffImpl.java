package net.mehvahdjukaar.supplementaries.neoforge;

import net.mehvahdjukaar.supplementaries.client.renderers.neoforge.ModSlider;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.ISlider;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeConfig;

public class SuppClientPlatformStuffImpl {

    public static ISlider createSlider(int x, int y, int width, int height, Component prefix, Component suffix,
                                       double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString) {
        return new ModSlider(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize, precision, drawString);
    }

    public static boolean hasFixedAO() {
        return CompatHandler.SODIUM || CompatHandler.EMBEDDIUM || NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get();
    }

}

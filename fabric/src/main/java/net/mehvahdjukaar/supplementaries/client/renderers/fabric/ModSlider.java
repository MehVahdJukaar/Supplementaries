package net.mehvahdjukaar.supplementaries.client.renderers.fabric;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.ISlider;
import net.mehvahdjukaar.supplementaries.common.block.dispenser.DispenserBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.worldgen.BasaltAshFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ModSlider extends AbstractSliderButton implements ISlider {
    private final double maxValue;
    private final double minValue;
    private final Component prefix;
    private final Component suffix;

    public ModSlider(int x, int y, int width, int height, Component component,
                     Component suffix, double minValue, double maxValue, double currentValue) {
        super(x, y, width, height, component, (currentValue- minValue)/(maxValue-minValue));
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.prefix = component;
        this.suffix = suffix;
        this.updateMessage();
    }

    @Override
    public double getValue() {
        return this.value * (this.maxValue - this.minValue) + this.minValue;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal("").append(prefix).append(String.valueOf((int)this.getValue())).append(suffix));
    }

    @Override
    protected void applyValue() {
    }

    @Override
    public void onReleased(double mouseX, double mouseY) {
        super.playDownSound(Minecraft.getInstance().getSoundManager());
    }
}

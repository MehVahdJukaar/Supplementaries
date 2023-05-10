package net.mehvahdjukaar.supplementaries.client;

import net.mehvahdjukaar.supplementaries.client.screens.widgets.ISlider;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class ModSlider extends AbstractSliderButton implements ISlider {
    private final double maxValue;
    private final double minValue;

    public ModSlider(int x, int y, int width, int height, Component component, double minValue, double maxValue, double currentValue) {
        super(x, y, width, height, component, currentValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public double getValue() {
        return this.value * (this.maxValue - this.minValue) + this.minValue;
    }

    @Override
    protected void updateMessage() {

    }

    @Override
    protected void applyValue() {

    }

}

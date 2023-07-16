package net.mehvahdjukaar.supplementaries.client.screens.widgets;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public interface ISlider extends GuiEventListener, Renderable, NarratableEntry {
    double getValue();

    void onRelease(double x, double y);
}

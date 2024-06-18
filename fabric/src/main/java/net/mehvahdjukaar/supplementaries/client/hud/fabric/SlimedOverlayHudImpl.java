package net.mehvahdjukaar.supplementaries.client.hud.fabric;

import net.mehvahdjukaar.supplementaries.client.hud.SlimedOverlayHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SlimedOverlayHudImpl extends SlimedOverlayHud {

    public static final SlimedOverlayHudImpl INSTANCE = new SlimedOverlayHudImpl(Minecraft.getInstance());

    protected SlimedOverlayHudImpl(Minecraft minecraft) {
        super(minecraft);
    }

    public void render(GuiGraphics graphics, float partialTicks) {
        var w = this.mc.getWindow();
        this.render(graphics, partialTicks, w.getGuiScaledWidth(), w.getGuiScaledHeight());
    }
}

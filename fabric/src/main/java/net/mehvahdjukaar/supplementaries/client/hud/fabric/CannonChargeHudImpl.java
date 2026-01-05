package net.mehvahdjukaar.supplementaries.client.hud.fabric;

import net.mehvahdjukaar.supplementaries.client.cannon.CannonChargeHud;
import net.mehvahdjukaar.supplementaries.client.hud.SlimedOverlayHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CannonChargeHudImpl extends CannonChargeHud {

    public static final CannonChargeHudImpl INSTANCE = new CannonChargeHudImpl(Minecraft.getInstance());

    protected CannonChargeHudImpl(Minecraft minecraft) {
        super(minecraft);
    }

    public void render(GuiGraphics graphics, float partialTicks) {
        var w = this.mc.getWindow();
        this.render(graphics, partialTicks, w.getGuiScaledWidth(), w.getGuiScaledHeight());
    }
}

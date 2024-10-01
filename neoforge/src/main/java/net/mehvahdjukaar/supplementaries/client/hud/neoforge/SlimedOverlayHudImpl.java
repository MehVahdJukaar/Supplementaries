package net.mehvahdjukaar.supplementaries.client.hud.neoforge;

import net.mehvahdjukaar.supplementaries.client.hud.SlimedOverlayHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SlimedOverlayHudImpl extends SlimedOverlayHud implements IGuiOverlay {

    public SlimedOverlayHudImpl() {
        super(Minecraft.getInstance());
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics graphics, float partial, int width, int height) {
        render( graphics, partial, width, height);
    }
}

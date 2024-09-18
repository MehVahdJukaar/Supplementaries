package net.mehvahdjukaar.supplementaries.client.renderers.neoforge;

import net.mehvahdjukaar.supplementaries.client.cannon.CannonChargeHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class CannonChargeOverlayImpl extends CannonChargeHud implements IGuiOverlay {

    public CannonChargeOverlayImpl() {
        super(Minecraft.getInstance());
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics graphics, float f, int i, int j) {
        render(graphics, f, i, j);
    }
}

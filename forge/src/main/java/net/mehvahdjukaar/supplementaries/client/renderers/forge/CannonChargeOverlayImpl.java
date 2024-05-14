package net.mehvahdjukaar.supplementaries.client.renderers.forge;

import net.mehvahdjukaar.supplementaries.client.cannon.CannonChargeOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class CannonChargeOverlayImpl extends CannonChargeOverlay implements IGuiOverlay {

    public CannonChargeOverlayImpl() {
        super(Minecraft.getInstance(), Minecraft.getInstance().getItemRenderer());
    }
    @Override
    public void render(ForgeGui forgeGui, GuiGraphics graphics, float f, int i, int j) {
        renderBar(graphics, f, i, j);
    }
}

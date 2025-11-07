package net.mehvahdjukaar.supplementaries.client.renderers.items;

import net.mehvahdjukaar.moonlight.api.item.IItemDecoratorRenderer;
import net.mehvahdjukaar.supplementaries.common.items.SelectableContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.components.SelectableContainerContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class SelectableItemOverlayRenderer implements IItemDecoratorRenderer {

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        if (Minecraft.getInstance().player != null) {
            SelectableContainerContent<?> data = stack.get(((SelectableContainerItem<?, ?>) stack.getItem()).getComponentType());
            if (data != null) {
                ItemStack ammo = data.getSelectedUnsafe();
                ProjectileWeaponOverlayRenderer.renderAmmo(graphics, x, y, ammo);
            }
        }
        return true;
    }


}

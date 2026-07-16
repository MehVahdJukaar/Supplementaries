package net.mehvahdjukaar.supplementaries.integration.platform;


import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.inventories.VariableSizeContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.InventoryViewTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.client.module.ChestSearchingModule;
import org.violetmoon.quark.content.client.tooltip.ShulkerBoxTooltips;

public class InventoryTooltipComponent implements ClientTooltipComponent {

    private static final int CORNER = 5;
    private static final int BUFFER = 1;
    private static final int EDGE = 18;

    private final NonNullList<ItemStack> items;
    private final int[] dimensions;
    private final int size;
    protected ChestSearchingModule module = Quark.ZETA.modules.get(ChestSearchingModule.class);

    public InventoryTooltipComponent(InventoryViewTooltip tooltip) {
        ItemContainerContents contents = tooltip.contents();
        this.size = tooltip.size();
        this.dimensions = VariableSizeContainerMenu.getRatio(this.size);
        this.items = NonNullList.withSize(this.size, ItemStack.EMPTY);
        contents.copyInto(this.items);
    }

    @Override
    public void renderImage(Font font, int tooltipX, int tooltipY, GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();

        int currentX = tooltipX;

        int texWidth = CORNER * 2 + EDGE * dimensions[0];

        int right = currentX + texWidth;
        Window window = mc.getWindow();
        if (right > window.getGuiScaledWidth()) {
            currentX -= (right - window.getGuiScaledWidth());
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0D, 0.0D, 700.0D);

        int color = -1;

        ShulkerBoxTooltips.ShulkerComponent.renderTooltipBackground(graphics, mc, pose, currentX, tooltipY, dimensions[0], dimensions[1], color);

        for (int i = 0; i < size; i++) {
            ItemStack itemstack = items.get(i);
            int xp = currentX + 6 + (i % dimensions[0]) * EDGE;
            int yp = tooltipY + 6 + (i / dimensions[0]) * EDGE;

            if (!itemstack.isEmpty()) {
                graphics.renderFakeItem(itemstack, xp, yp);
                graphics.renderItemDecorations(mc.font, itemstack, xp, yp);
            }

            if (!module.namesMatch(itemstack)) {
                RenderSystem.disableDepthTest();
                graphics.fill(xp, yp, xp + 16, yp + 16, 0xAA000000);
            }
        }

        pose.popPose();
    }

    @Override
    public int getHeight() {
        return CORNER * 2 + EDGE * dimensions[1] + BUFFER;
    }

    @Override
    public int getWidth(Font font) {
        return CORNER * 2 + EDGE * dimensions[0];
    }
}

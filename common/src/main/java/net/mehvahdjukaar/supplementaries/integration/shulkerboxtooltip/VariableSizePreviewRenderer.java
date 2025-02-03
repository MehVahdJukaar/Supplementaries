package net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.PreviewType;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import com.misterpemodder.shulkerboxtooltip.api.renderer.PreviewRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.supplementaries.common.inventories.VariableSizeContainerMenu;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/**
 * A PreviewRenderer that mimics the way slots are placed within a variable-size container.
 * Delegates the rendering to the standard ModPreviewRenderer in COMPACT PreviewMode.
 */
@Environment(EnvType.CLIENT)
public class VariableSizePreviewRenderer implements PreviewRenderer {

    private static final PreviewRenderer DELEGATE = PreviewRenderer.getModRendererInstance();

    private final Supplier<Integer> unlockedSlots;
    private List<ItemStack> items;
    private PreviewType previewType;
    private int[] dims;

    public VariableSizePreviewRenderer(Supplier<Integer> unlockedSlots) {
        this.unlockedSlots = unlockedSlots;
        this.items = List.of();
        this.initDims();
    }

    @Override
    public int getHeight() {
        if (this.previewType != PreviewType.FULL) {
            return DELEGATE.getHeight();
        }
        return 7 + this.dims[1] * 18 + 7;
    }

    @Override
    public int getWidth() {
        if (this.previewType != PreviewType.FULL) {
            return DELEGATE.getHeight();
        }
        return 7 + this.dims[0] * 18 + 7;
    }

    @Override
    public void setPreview(PreviewContext context, PreviewProvider provider) {
        this.items = provider.getInventory(context);
        this.initDims();
        DELEGATE.setPreview(context, provider);
    }

    private void initDims() {
        int size = this.unlockedSlots.get();
        this.dims = VariableSizeContainerMenu.getRatio(size);
        if (this.dims[0] > 9) {
            this.dims[0] = 9;
            this.dims[1] = (int) Math.ceil(size / 9f);
        }
    }

    @Override
    public void setPreviewType(PreviewType type) {
        this.previewType = type;
        DELEGATE.setPreviewType(type);
    }

    @Override
    public void draw(int x, int y, GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        if (this.previewType != PreviewType.FULL) {
            DELEGATE.draw(x, y, graphics, font, mouseX, mouseY);
            return;
        }
        RenderSystem.enableDepthTest();
        this.renderBackground(x, y, graphics);
        this.renderSlots(x, y, graphics, font);
        this.renderInnerTooltip(x, y, graphics, font, mouseX, mouseY);
    }

    private void renderBackground(int x, int y, GuiGraphics graphics) {
        int w = this.dims[0] * 18;
        int h = this.dims[1] * 18;
        int rEdgeOffset = 7 + w;
        int bEdgeOffset = 7 + h;

        graphics.blit(ModTextures.VARIABLE_SIZE_CONTAINER_TEXTURE, x, y, 0, 0, rEdgeOffset, 7);
        graphics.blit(ModTextures.VARIABLE_SIZE_CONTAINER_TEXTURE, x + rEdgeOffset, y, 7 + 9 * 18, 0, 7, 7);

        graphics.blit(ModTextures.VARIABLE_SIZE_CONTAINER_TEXTURE, x, y + 7, 0, 7, rEdgeOffset, h);
        graphics.blit(ModTextures.VARIABLE_SIZE_CONTAINER_TEXTURE, x + rEdgeOffset, y + 7, 7 + 9 * 18, 7, 7, h);

        graphics.blit(ModTextures.VARIABLE_SIZE_CONTAINER_TEXTURE, x, y + bEdgeOffset, 0, 159, rEdgeOffset, 7);
        graphics.blit(ModTextures.VARIABLE_SIZE_CONTAINER_TEXTURE, x + rEdgeOffset, y + bEdgeOffset, 7 + 9 * 18, 159,
            7, 7);
    }

    private void renderSlots(int x, int y, GuiGraphics graphics, Font font) {
        int dimx;
        int slot = 0;
        int size = this.unlockedSlots.get();
        for (int h = 0; h < this.dims[1]; ++h) {
            dimx = Math.min(this.dims[0], size);
            int xp = 7 + (this.dims[0] * 18) / 2 - (dimx * 18) / 2;
            for (int j = 0; j < dimx; ++j) {
                int slotX = xp + x + j * 18;
                int slotY = 7 + y + 18 * h;
                graphics.blitSprite(ModTextures.SLOT_SPRITE, slotX, slotY, 18, 18);

                if (slot < this.items.size()) {
                    ItemStack stack = this.items.get(slot);
                    graphics.renderFakeItem(stack, slotX + 1, slotY + 1);
                    graphics.renderItemDecorations(font, stack, slotX + 1, slotY + 1);
                }
                ++slot;

            }
            size -= dims[0];
        }
    }

    private ItemStack getStackAt(int x, int y) {
        int slot = -1;

        if (y >= 7) {
            int slotY = (y - 7) / 18;
            int size = this.unlockedSlots.get() - this.dims[0] * slotY;
            int dimx = Math.min(this.dims[0], size);
            int xp = 7 + (this.dims[0] * 18) / 2 - (dimx * 18) / 2;

            if (x >= xp) {
                int slotX = (x - xp) / 18;
                if (slotX < dimx) {
                    slot = slotX + slotY * this.dims[0];
                }
            }
        }

        if (slot >= 0 && slot < this.items.size()) {
            return this.items.get(slot);
        }
        return ItemStack.EMPTY;
    }

    private void renderInnerTooltip(
        int x, int y, GuiGraphics graphics, Font font, int mouseX,
        int mouseY
    ) {
        ItemStack stack = this.getStackAt(mouseX - x, mouseY - y);

        if (!stack.isEmpty()) {
            graphics.renderTooltip(font, stack, mouseX, mouseY);
        }
    }

}

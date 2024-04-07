package net.mehvahdjukaar.supplementaries.client.screens;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class CannonScreen extends AbstractContainerScreen<CannonContainerMenu> implements ContainerListener {

    private final CannonBlockTile tile;

    private Button manouverButton;
    private EditBox pitchSelector;
    private EditBox yawSelector;
    private PowerSelectorWidget powerSelector;

    public CannonScreen(CannonContainerMenu menu, Inventory inventory, Component text) {
        super(menu, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;

        this.tile = menu.getContainer();
    }

    @Override
    public void init() {
        super.init();

        this.titleLabelX = 8;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.manouverButton = this.addRenderableWidget(new ManouverButton(i + 156, j + 10 + 6));

        this.yawSelector = this.addRenderableWidget(new NumberEditBox(this.font, i + 146, j + 29 + 6, 18, 10));
        this.pitchSelector = this.addRenderableWidget(new NumberEditBox(this.font, i + 146, j + 49 + 6, 18, 10));

        this.powerSelector = this.addRenderableWidget(new PowerSelectorWidget(i + 18, j + 24, 4));
        this.menu.addSlotListener(this);
    }


    private void onManeuverPressed(Button button) {
        CannonController.activateCannonCamera(tile);
        this.onClose();
    }

    private void setYaw(String h) {
    }

    private String getYaw() {
        return "0";
    }

    private void setPitch(String h) {
    }

    private String getPitch() {
        return "0";
    }

    private int getActualPower() {
        return Math.min(this.powerSelector.getPower(), tile.getFuel().getCount());
    }

    private boolean isValidAngle(String s) {
        this.minecraft.font.width(s);
        return true;
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slot, ItemStack stack) {
        if (slot == 0) {

        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu container, int dataSlotIndex, int value) {
        this.slotChanged(container, 0, container.getSlot(0).getItem());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        this.renderBackground(graphics);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        graphics.blit(Supplementaries.res("textures/gui/cannon_gui.png"), k, l, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);
        int color = 4210752;
        int wantedPower = this.powerSelector.getPower();
        if (wantedPower > this.getActualPower()) {
            color = ChatFormatting.GRAY.getColor();
        }
        graphics.drawString(this.font, wantedPower + "x", 37, 25, color, false);
    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
        }
        return super.keyPressed(key, a, b);
    }

    @Override
    public void containerTick() {
        super.containerTick();
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }


    private final class ManouverButton extends Button {

        public ManouverButton(int x, int y) {
            super(x, y, 10, 10, Component.empty(), CannonScreen.this::onManeuverPressed, Button.DEFAULT_NARRATION);
            this.setTooltip(Tooltip.create(Component.translatable("gui.supplementaries.cannon.maneuver")));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
            int x = 176;
            int y = 36;
            if (this.isHovered())
                x += this.width;

            guiGraphics.blit(ModTextures.CANNON_GUI_TECTURE, this.getX(), this.getY(), x, y, this.width, this.height);
        }
    }

    private static class NumberEditBox extends EditBox {
        public NumberEditBox(Font font, int x, int y, int width, int height) {
            super(font, x, y, width, height, Component.empty());
            this.setMaxLength(4);
            this.setBordered(false);
            this.setFilter(this::isValidAngle);
        }

        private boolean isValidAngle(String str) {
            try {
                if (str.isEmpty()) return true;
                double d = Double.parseDouble(str);
                // chck if it contains characters
                if (str.contains("[a-zA-Z]+")) return false;
                return d <= 360 && d >= -360;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    private class PowerSelectorWidget extends AbstractWidget {
        private final int levels;
        private int power = 2;

        public PowerSelectorWidget(int x, int y, int levels) {
            super(x, y, 12, 36, Component.empty());
            this.levels = levels;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
            int hoveredLevel = 0;
            int levelH = this.height / levels;

            if (this.isHovered) {
                hoveredLevel = getSelectedHoveredLevel(mouseY);
            }
            int actualPower = CannonScreen.this.getActualPower();
            for (int p = 1; p <= levels; p++) {
                int selectedH = levelH * p;

                int y = this.height - selectedH;
                int x = 176 + (p == hoveredLevel ? this.width : 0);
                if (p > power) {
                    x += this.width * 2;
                } else if (p > actualPower) {
                    x += this.width * 4;
                }
                guiGraphics.blit(Supplementaries.res("textures/gui/cannon_gui.png"), this.getX(), this.getY() + y, x, y,
                        this.width, levelH);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.power = getSelectedHoveredLevel(mouseY);
        }

        private int getSelectedHoveredLevel(double mouseY) {
            float levelH = (float) this.height / levels;
            return levels - (int) Math.floor((mouseY - this.getY()) / levelH);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        public int getPower() {
            return this.power;
        }
    }

}

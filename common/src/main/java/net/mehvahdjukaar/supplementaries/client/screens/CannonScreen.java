package net.mehvahdjukaar.supplementaries.client.screens;


import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class CannonScreen extends AbstractContainerScreen<CannonContainerMenu> implements ContainerListener {


    private NumberEditBox pitchSelector;
    private NumberEditBox yawSelector;
    private PowerSelectorWidget powerSelector;

    public CannonScreen(CannonContainerMenu menu, Inventory inventory, Component text) {
        super(menu, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void init() {
        super.init();

        this.titleLabelX = 8;
        int i = this.leftPos;
        int j = this.topPos;
        CannonBlockTile cannon = this.menu.cannon;
        boolean manActive = cannon.canManeuverFromGUI(Minecraft.getInstance().player);
        ManeuverButton maneuver = new ManeuverButton(i + 154, j + 10 + 6, manActive);
        this.addRenderableWidget(maneuver);

        this.yawSelector = this.addRenderableWidget(new NumberEditBox(this.font, i + 144, j + 49 + 6, 18, 10));
        this.yawSelector.setNumber(cannon.getYaw());
        this.pitchSelector = this.addRenderableWidget(new NumberEditBox(this.font, i + 144, j + 29 + 6, 18, 10));
        this.pitchSelector.setNumber(cannon.getPitch());

        this.powerSelector = this.addRenderableWidget(new PowerSelectorWidget(i + 18, j + 24, CannonBlock.MAX_POWER_LEVELS));
        this.powerSelector.power = cannon.getPowerLevel();
        this.menu.addSlotListener(this);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void onManeuverPressed(Button button) {
        if (button.active) {
            CannonController.startControlling(this.menu.cannon);
            //dont sync cannon and dont clear owner
            this.onClose();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        float yaw = this.yawSelector.getNumber();
        float pitch = this.pitchSelector.getNumber();
        byte power = this.powerSelector.getPower();
        //update client immediately too
        this.menu.cannon.setAttributes(yaw, pitch, power, false, minecraft.player);
        this.menu.cannon.syncToServer(false, !CannonController.isActive());
    }

    private int getActualPower() {
        return Math.min(this.powerSelector.getPower(), this.menu.cannon.getFuel().getCount());
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
        graphics.blit(ModTextures.CANNON_GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
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
        return super.keyPressed(key, a, b);
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }


    private final class ManeuverButton extends Button {

        public ManeuverButton(int x, int y, boolean active) {
            super(x, y, 10, 10, Component.empty(), CannonScreen.this::onManeuverPressed, Button.DEFAULT_NARRATION);
            if (active) this.setTooltip(Tooltip.create(Component.translatable("gui.supplementaries.cannon.maneuver")));
            this.active = active;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
            var texture = this.isHovered() ? ModTextures.CANNON_MANEUVER_HOVERED_SPRITE : ModTextures.CANNON_MANEUVER_SPRITE;
            texture = active ? texture : ModTextures.CANNON_MANEUVER_DISABLED_SPRITE;
            guiGraphics.blitSprite(texture, this.getX(), this.getY(), this.width, this.height);
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
                if (str.isEmpty() || str.equals("+") || str.equals("-")) return true;
                double d = Double.parseDouble(str);
                // chck if it contains characters
                if (str.contains("[a-zA-Z]+")) return false;
                return d <= 360 && d >= -360;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public void setNumber(float value) {
            this.setValue(String.valueOf((int) value));
        }

        public float getNumber() {
            try {
                return Float.parseFloat(this.getValue());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    private class PowerSelectorWidget extends AbstractWidget {
        private final byte levels;
        private byte power = 2;

        public PowerSelectorWidget(int x, int y, int levels) {
            super(x, y, 12, 36, Component.empty());
            this.levels = (byte) levels;
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
                ResourceLocation texture;
                boolean hovered = p == hoveredLevel;
                if (p > power) {
                    texture = hovered ? ModTextures.CANNON_EMPTY_HOVERED_SPRITE : ModTextures.CANNON_EMPTY_SPRITE;
                } else if (p > actualPower) {
                    texture = hovered ? ModTextures.CANNON_DEPLETED_HOVERED_SPRITE : ModTextures.CANNON_DEPLETED_SPRITE;
                } else {
                    texture = hovered ? ModTextures.CANNON_POWER_HOVERED_SPRITE : ModTextures.CANNON_POWER_SPRITE;
                }
                guiGraphics.blitSprite(texture,
                        this.width, this.height, 0, y,
                        this.getX(), this.getY() + y,
                        this.width, levelH);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.power = getSelectedHoveredLevel(mouseY);
        }

        private byte getSelectedHoveredLevel(double mouseY) {
            float levelH = (float) this.height / levels;
            return (byte) (levels - Math.floor((mouseY - this.getY()) / levelH));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        public byte getPower() {
            return this.power;
        }
    }

}

package net.mehvahdjukaar.supplementaries.client.screens;


import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.TrappedPresentContainerMenu;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetTrappedPresentPacket;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TrappedPresentScreen extends AbstractContainerScreen<TrappedPresentContainerMenu> implements ContainerListener {

    private final TrappedPresentBlockTile tile;

    private PackButton packButton;
    private boolean primed;
    //hasn't received items yet
    private boolean needsInitialization = true;


    public TrappedPresentScreen(TrappedPresentContainerMenu menu, Inventory inventory, Component text) {
        super(menu, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;

        this.tile = (TrappedPresentBlockTile) menu.getContainer();
        Preconditions.checkNotNull(this.tile);
    }

    @Override
    public void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        int i = this.leftPos;
        int j = this.topPos;
        this.packButton = this.addRenderableWidget(new PackButton(i + 60 + 33, j + 33));

        this.primed = tile.isPrimed();

        this.updateState();

        this.menu.addSlotListener(this);
    }

    private void pack() {
        this.updateStateAndTryToPack(true);
    }

    private void updateState() {
        this.updateStateAndTryToPack(false);
    }

    private void updateStateAndTryToPack(boolean tryToPack) {
        boolean hasItem = this.needsInitialization ? this.primed : this.menu.getSlot(0).hasItem();
        //pack
        boolean hasChanged = false;
        //truth table shit. idk, could be written more readable
        if (this.primed && !hasItem) {
            this.primed = false;
            hasChanged = true;
        } else if (tryToPack && !this.primed && hasItem) {
            this.primed = true;
            hasChanged = true;
        }

        if (hasChanged) {

            NetworkHelper.sendToServer(new ServerBoundSetTrappedPresentPacket(this.tile.getBlockPos(),
                    this.primed));
            this.tile.updateState(this.primed);

            //close on client when packed. server side is handled by packet when it arrives
            if (this.primed) this.minecraft.player.clientSideCloseContainer();
        }

        this.packButton.setState(hasItem, this.primed);
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slot, ItemStack stack) {
        if (slot == 0) {
            updateState();
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu container, int dataSlotIndex, int value) {
        this.slotChanged(container, 0, container.getSlot(0).getItem());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        int k = this.leftPos;
        int l = this.topPos;
        graphics.blit(ModTextures.TRAPPED_PRESENT_GUI_TEXTURE, k, l, 0, 0,
                this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);
        //packButton.renderToolTip(graphics, x - this.leftPos, y - this.topPos);
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
        this.needsInitialization = false;
        super.containerTick();
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    public class PackButton extends AbstractButton {
        private static final Tooltip TOOLTIP = Tooltip.create(
                Component.translatable("gui.supplementaries.present.trapped"));

        private boolean packed;

        protected PackButton(int x, int y) {
            super(x, y, 22, 22, CommonComponents.EMPTY);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation texture;
            if (!this.active) {
                texture = ModTextures.TRAPPED_PRESENT_BUTTON_SELECTED_SPRITE;
            } else if (this.packed) {
                texture = ModTextures.TRAPPED_PRESENT_BUTTON_DISABLED_SPRITE;
            } else if (this.isHovered) {
               texture = ModTextures.TRAPPED_PRESENT_BUTTON_HIGHLIGHTED_SPRITE;
            }else{
                texture = ModTextures.TRAPPED_PRESENT_BUTTON_SPRITE;
            }
            graphics.blitSprite(texture, this.getX(), this.getY(), this.width, this.height);
        }

        public void setState(boolean hasItem, boolean packed) {
            this.packed = packed;
            this.active = hasItem;
            this.setTooltip(!packed ? TOOLTIP : null);
        }

        @Override
        public void onPress() {
            TrappedPresentScreen.this.pack();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }
    }

}

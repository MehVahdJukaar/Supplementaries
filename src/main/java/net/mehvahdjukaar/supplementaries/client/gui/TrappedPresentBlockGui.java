package net.mehvahdjukaar.supplementaries.client.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.TrappedPresentContainerMenu;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetTrappedPresentPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrappedPresentBlockGui extends AbstractContainerScreen<TrappedPresentContainerMenu> implements ContainerListener {


    private final TrappedPresentBlockTile tile;

    private PackButton packButton;

    private boolean primed;
    //hasn't received items yet
    private boolean needsInitialization = true;

    public static MenuScreens.ScreenConstructor<TrappedPresentContainerMenu, TrappedPresentBlockGui> GUI_FACTORY =
            (container, inventory, title) -> {
                BlockEntity te = Minecraft.getInstance().level.getBlockEntity(container.getPos());
                if (te instanceof TrappedPresentBlockTile presentBlockTile) {
                    return new TrappedPresentBlockGui(container, inventory, title, presentBlockTile);
                }
                return null;
            };

    public TrappedPresentBlockGui(TrappedPresentContainerMenu container, Inventory inventory, Component text, TrappedPresentBlockTile tile) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;

        this.tile = tile;
    }

    @Override
    public void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        this.packButton = this.addRenderableWidget(new PackButton(i + 60+33, j + 33));

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

            NetworkHandler.INSTANCE.sendToServer(new ServerBoundSetTrappedPresentPacket(this.tile.getBlockPos(),
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
    public void dataChanged(AbstractContainerMenu container, int p_150525_, int p_150526_) {
        this.slotChanged(container, 0, container.getSlot(0).getItem());
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
        this.renderBackground(matrixStack);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Textures.TRAPPED_PRESENT_GUI_TEXTURE);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        if(this.primed){
            int k = (this.width - this.imageWidth) / 2;
            int l = (this.height - this.imageHeight) / 2;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, Textures.TRAPPED_PRESENT_GUI_TEXTURE);
            Slot slot = this.menu.getSlot(0);

            blit(poseStack, k + slot.x, l + slot.y,  400,12, 232, 16, 16, 256, 256);
        }
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int x, int y) {
        super.renderLabels(poseStack, x, y);
        packButton.renderToolTip(poseStack, x - this.leftPos, y - this.topPos);
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
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    public class PackButton extends AbstractButton {
        private boolean packed;

        protected PackButton(int x, int y) {
            super(x, y, 22, 22, TextComponent.EMPTY);
        }

        @Override
        public void renderButton(PoseStack poseStack, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
            RenderSystem.setShaderTexture(0, Textures.TRAPPED_PRESENT_GUI_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int i = 198;
            int j = 0;
            if (!this.active) {
                j += this.width * 2;
            } else if (this.packed) {
                j += this.width * 1;
            } else if (this.isHovered) {
                j += this.width * 3;
            }

            this.blit(poseStack, this.x, this.y, j, i, this.width, this.height);
        }

        public void setState(boolean hasItem, boolean packed) {
            this.packed = packed;
            this.active = hasItem;
        }

        @Override
        public void renderToolTip(PoseStack matrixStack, int x, int y) {
            if (this.isActive() && this.isHoveredOrFocused() && !this.packed) {
                TrappedPresentBlockGui.this.renderTooltip(matrixStack, new TranslatableComponent("gui.supplementaries.present.trapped"), x, y);
            }
        }

        @Override
        public void onPress() {
            TrappedPresentBlockGui.this.pack();
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
        }
    }

}

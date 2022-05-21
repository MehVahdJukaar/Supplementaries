package net.mehvahdjukaar.supplementaries.client.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.gui.widgets.MultiLineEditBoxWidget;
import net.mehvahdjukaar.supplementaries.client.gui.widgets.PlayerSuggestionBoxWidget;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.PresentContainerMenu;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetPresentPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class PresentBlockGui extends AbstractContainerScreen<PresentContainerMenu> implements ContainerListener {

    private static final TranslatableComponent PACK_BUTTON = new TranslatableComponent("gui.supplementaries.present.pack");

    private static final int DESCRIPTION_BOX_X = 53;
    private static final int DESCRIPTION_BOX_Y = 33;
    private static final int DESCRIPTION_BOX_H = 36;
    private static final int DESCRIPTION_BOX_W = 105;
    private static final int SUGGESTION_BOX_Y = 19;
    private static final int SUGGESTION_BOX_W = 99;
    private static final int SUGGESTION_BOX_H = 12;

    private final PresentBlockTile tile;

    private PackButton packButton;
    private PlayerSuggestionBoxWidget recipient;
    private MultiLineEditBoxWidget descriptionBox;

    private boolean packed;
    //hasn't received items yet
    private boolean needsInitialization = true;

    public static MenuScreens.ScreenConstructor<PresentContainerMenu, PresentBlockGui> GUI_FACTORY =
            (container, inventory, title) -> {
                BlockEntity te = Minecraft.getInstance().level.getBlockEntity(container.getPos());
                if (te instanceof PresentBlockTile presentBlockTile) {
                    return new PresentBlockGui(container, inventory, title, presentBlockTile);
                }
                return null;
            };

    public PresentBlockGui(PresentContainerMenu container, Inventory inventory, Component text, PresentBlockTile tile) {
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

        this.packButton = this.addRenderableWidget(new PackButton(i + 14, j + 45));

        this.recipient = this.addRenderableWidget(new PlayerSuggestionBoxWidget(this.minecraft,
                i + DESCRIPTION_BOX_X, j + SUGGESTION_BOX_Y, SUGGESTION_BOX_W, SUGGESTION_BOX_H));

        this.recipient.setOutOfBoundResponder(up -> {
            if (!up) {
                this.setFocused(descriptionBox);
                this.recipient.setFocused(false);
                this.descriptionBox.setFocused(true);
            }
        });

        this.descriptionBox = this.addRenderableWidget(new MultiLineEditBoxWidget(this.minecraft,
                i + DESCRIPTION_BOX_X, j + DESCRIPTION_BOX_Y, DESCRIPTION_BOX_W, DESCRIPTION_BOX_H));

        this.descriptionBox.setOutOfBoundResponder(up -> {
            if (up) {
                this.setFocused(recipient);
                this.recipient.setFocused(true);
                this.descriptionBox.setFocused(false);
            }
        });

        this.setFocused(this.recipient);

        this.recipient.setText(this.tile.getRecipient());
        this.descriptionBox.setText(this.tile.getDescription());
        this.packed = tile.isPacked();

        this.updateState();

        this.menu.addSlotListener(this);
    }

    public void onAddPlayer(PlayerInfo info) {
        this.recipient.addPlayer(info);
    }

    public void onRemovePlayer(UUID uuid) {
        this.recipient.removePlayer(uuid);
    }

    private void pack() {
        this.updateStateAndTryToPack(true);
    }

    private void updateState() {
        this.updateStateAndTryToPack(false);
    }

    private void updateStateAndTryToPack(boolean tryToPack) {
        boolean hasItem = this.needsInitialization ? this.packed : this.menu.getSlot(0).hasItem();
        //pack
        boolean hasChanged = false;
        //truth table shit. idk, could be written more readable
        if (this.packed && !hasItem) {
            this.packed = false;
            hasChanged = true;
        } else if (tryToPack && !this.packed && hasItem) {
            this.packed = true;
            hasChanged = true;
        }

        if (hasChanged) {
            String sender = Minecraft.getInstance().player.getName().getString();
            String recipient = this.recipient.getText();
            String description = this.descriptionBox.getText();
            NetworkHandler.INSTANCE.sendToServer(new ServerBoundSetPresentPacket(this.tile.getBlockPos(),
                    this.packed, recipient, sender, description));
            this.tile.updateState(this.packed, recipient, sender, description);

            //close on client when packed. server side is handled by packet when it arrives
            if (this.packed) this.minecraft.player.clientSideCloseContainer();
        }

        this.recipient.setState(hasItem, this.packed);
        this.packButton.setState(hasItem, this.packed);
        this.descriptionBox.setState(hasItem, this.packed);
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
        RenderSystem.setShaderTexture(0, Textures.PRESENT_GUI_TEXTURE);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        if(this.packed){
            int k = (this.width - this.imageWidth) / 2;
            int l = (this.height - this.imageHeight) / 2;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, Textures.PRESENT_GUI_TEXTURE);
            Slot slot = this.menu.getSlot(0);

            blit(poseStack, k + slot.x, l + slot.y,  300,12, 232, 16, 16, 256, 256);
        }
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int x, int y) {
        super.renderLabels(poseStack, x, y);
        packButton.renderToolTip(poseStack, x - this.leftPos, y - this.topPos);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int key) {
        this.recipient.setFocused(false);
        this.descriptionBox.setFocused(false);
        return super.mouseClicked(mouseX, mouseY, key);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.recipient.mouseScrolled(mouseX, mouseY, amount) ||
                this.descriptionBox.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
        }
        return this.recipient.keyPressed(key, a, b) || this.recipient.canConsumeInput() ||
            this.descriptionBox.keyPressed(key, a, b) || this.descriptionBox.canConsumeInput()
                || super.keyPressed(key, a, b);
    }

    @Override
    public boolean mouseDragged(double dx, double dy, int key, double mouseX, double mouseY) {
        if (key == 0) {
            if (this.descriptionBox.mouseDragged(dx, dy, key, mouseX, mouseY)) return true;
        }
        return super.mouseDragged(dx, dy, key, mouseX, mouseY);
    }

    @Override
    public void containerTick() {
        this.needsInitialization = false;
        super.containerTick();
        this.recipient.tick();
        this.descriptionBox.tick();
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
            RenderSystem.setShaderTexture(0, Textures.PRESENT_GUI_TEXTURE);
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
                PresentBlockGui.this.renderTooltip(matrixStack, PACK_BUTTON, x, y);
            }
        }

        @Override
        public void onPress() {
            PresentBlockGui.this.pack();
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
        }
    }

}

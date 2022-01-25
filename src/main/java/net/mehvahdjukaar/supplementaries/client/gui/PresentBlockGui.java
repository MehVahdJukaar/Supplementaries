package net.mehvahdjukaar.supplementaries.client.gui;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.inventories.PresentContainer;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.ServerBoundSetPresentPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

public class PresentBlockGui extends ContainerScreen<PresentContainer> implements IContainerListener {


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

    public static ScreenManager.IScreenFactory<PresentContainer, PresentBlockGui> GUI_FACTORY =
            (container, inventory, title) -> {
                TileEntity te = Minecraft.getInstance().level.getBlockEntity(container.getPos());
                if (te instanceof PresentBlockTile) {
                    return new PresentBlockGui(container, inventory, title, (PresentBlockTile) te);
                }
                return null;
            };

    public PresentBlockGui(PresentContainer container, PlayerInventory inventory, ITextComponent text, PresentBlockTile tile) {
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

        this.packButton = this.addButton(new PackButton(i + 14, j + 45));

        this.recipient = this.addButton(new PlayerSuggestionBoxWidget(this.minecraft,
                i + DESCRIPTION_BOX_X, j + SUGGESTION_BOX_Y, SUGGESTION_BOX_W, SUGGESTION_BOX_H));

        this.recipient.setOutOfBoundResponder(up -> {
            if (!up) {
                this.setFocused(descriptionBox);
                this.recipient.setFocused(false);
                this.descriptionBox.setFocused(true);
            }
        });

        this.descriptionBox = this.addButton(new MultiLineEditBoxWidget(this.minecraft,
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

    public void onAddPlayer(NetworkPlayerInfo info) {
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
    public void refreshContainer(Container container, NonNullList<ItemStack> itemStacks) {
        this.slotChanged(container, 0, container.getSlot(0).getItem());
    }

    @Override
    public void setContainerData(Container p_71112_1_, int p_71112_2_, int p_71112_3_) {
    }

    @Override
    public void slotChanged(Container container, int slot, ItemStack stack) {
        if (slot == 0) {
            updateState();
        }
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        this.renderBackground(matrixStack);
        RenderSystem.color4f(1.0F, 1, 1, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(Textures.PRESENT_BLOCK_GUI_TEXTURE);

        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        if (this.packed) {
            int k = (this.width - this.imageWidth) / 2;
            int l = (this.height - this.imageHeight) / 2;
            RenderSystem.color4f(1.0F, 1, 1, 1.0F);
            Minecraft.getInstance().getTextureManager().bind(Textures.PRESENT_BLOCK_GUI_TEXTURE);
            Slot slot = this.menu.getSlot(0);

            blit(poseStack, k + slot.x, l + slot.y, 400, 12, 232, 16, 16, 256, 256);
        }
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack poseStack, int x, int y) {
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
    public void tick() {
        super.tick();
        this.needsInitialization = false;

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
            super(x, y, 22, 22, StringTextComponent.EMPTY);
        }

        @Override
        public void renderButton(MatrixStack poseStack, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
            RenderSystem.color4f(1.0F, 1, 1, 1.0F);
            Minecraft.getInstance().getTextureManager().bind(Textures.PRESENT_BLOCK_GUI_TEXTURE);
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
        public void renderToolTip(MatrixStack matrixStack, int x, int y) {
            if (this.active && this.isHovered && !this.packed) {
                PresentBlockGui.this.renderTooltip(matrixStack, new TranslationTextComponent("gui.supplementaries.present.pack"), x, y);

            }
        }

        @Override
        public void onPress() {
            PresentBlockGui.this.pack();
        }


    }

}

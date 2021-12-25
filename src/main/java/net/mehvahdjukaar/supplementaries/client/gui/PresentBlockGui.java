package net.mehvahdjukaar.supplementaries.client.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.PresentContainerMenu;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetPresentPacket;
import net.mehvahdjukaar.supplementaries.common.utils.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
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

import java.util.List;
import java.util.UUID;

public class PresentBlockGui extends AbstractContainerScreen<PresentContainerMenu> implements ContainerListener {

    private static final Component SEND_TO = (new TranslatableComponent("gui.supplementaries.present.send"))
            .withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC);
    private static final Component PACK = (new TranslatableComponent("gui.supplementaries.present.pack"));

    private static final int SUGGESTION_BOX_H = 12;
    private static final int MAX_SUGGESTIONS = 3;
    private static final int SUGGESTION_LIST_H = SUGGESTION_BOX_H * MAX_SUGGESTIONS;
    private static final int SUGGESTION_LIST_Y = 33;
    private static final int SCROLL_BAR_START_X = 153;
    private static final int SUGGESTION_LIST_X = 51;
    private static final int SUGGESTION_BOX_W = 101;
    private static final int SCROLLER_W = 6;
    private static final int SCROLLER_H = 17;


    protected PresentTextFieldWidget recipient;
    private PackButton packButton;
    private final PresentBlockTile tile;

    private FilteredPlayerListWidget playerList;

    private boolean packed;
    //hasn't received items yet
    private boolean needsInitialization = true;

    public static MenuScreens.ScreenConstructor<PresentContainerMenu, PresentBlockGui> GUI_FACTORY =
            (container, inventory, title) -> {
                BlockEntity te = Minecraft.getInstance().level.getBlockEntity(container.getPos());
                if (te instanceof PresentBlockTile presentBlockTile)
                    return new PresentBlockGui(container, inventory, title, presentBlockTile);
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

        this.menu.addSlotListener(this);

        this.playerList = this.addRenderableWidget(new FilteredPlayerListWidget(this.minecraft,
                i + SUGGESTION_LIST_X, j + SUGGESTION_LIST_Y, this::setRecipient));

        this.packButton = this.addRenderableWidget(new PackButton(i + 14, j + 45));

        this.recipient = this.addRenderableWidget(new PresentTextFieldWidget(this.font, i + 53, j + 18,
                99, 12, new TranslatableComponent("container.repair")));
        this.recipient.setValue(this.tile.getRecipient());


        this.setFocused(this.recipient);

        this.packed = this.tile.isPacked();
        this.updateState();
    }

    public void onAddPlayer(PlayerInfo info) {
        this.playerList.addPlayer(info);
    }

    public void onRemovePlayer(UUID uuid) {
        this.playerList.removePlayer(uuid);
    }

    protected void setRecipient(String recipient) {
        this.recipient.setValue(recipient);
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
            NetworkHandler.INSTANCE.sendToServer(new ServerBoundSetPresentPacket(this.tile.getBlockPos(),
                    this.packed, this.recipient.getValue(), sender));
            this.tile.updateState(this.recipient.getValue(), sender, this.packed);

            //close on client when packed. server side is handled by packet when it arrives
            if(this.packed) this.minecraft.player.clientSideCloseContainer();
        }

        this.recipient.setState(hasItem, this.packed);
        this.packButton.setState(hasItem, this.packed);
        this.playerList.setState(hasItem, this.packed);
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
        RenderSystem.setShaderTexture(0, Textures.PRESENT_BLOCK_GUI_TEXTURE);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        if (this.packed) {
            Component c = tile.getSenderMessage();
            if (c != null) {
                drawString(poseStack, this.font, c, this.playerList.x + 3, this.playerList.y + 3, -1);
            }
            int k = (this.width - this.imageWidth) / 2;
            int l = (this.height - this.imageHeight) / 2;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, Textures.PRESENT_BLOCK_GUI_TEXTURE);
            Slot slot = this.menu.getSlot(0);

            this.blit(poseStack, k + slot.x, l + slot.y, 12, 232, 16, 16);
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
        //, enter
        else if (key == 257 || key == 335) {
            this.pack();
            return true;
        }
        return this.playerList.keyPressed(key, a, b) ||
                this.recipient.keyPressed(key, a, b) || this.recipient.canConsumeInput() ||
                super.keyPressed(key, a, b);
    }

    @Override
    public boolean mouseDragged(double dx, double dy, int key, double mouseX, double mouseY) {
        if (key == 0) {
            if (this.playerList.mouseDragged(dx, dy, key, mouseX, mouseY)) return true;
        }
        return super.mouseDragged(dx, dy, key, mouseX, mouseY);
    }

    @Override
    public void containerTick() {
        this.needsInitialization = false;
        super.containerTick();
        this.recipient.tick();
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
            RenderSystem.setShaderTexture(0, Textures.PRESENT_BLOCK_GUI_TEXTURE);
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
                PresentBlockGui.this.renderTooltip(matrixStack, PACK, x, y);
            }
        }

        @Override
        public void onPress() {
            PresentBlockGui.this.pack();
        }

        @Override
        public void updateNarration(NarrationElementOutput p_169152_) {

        }
    }

    private class PresentTextFieldWidget extends EditBox {
        private final Font font;
        private String fullSuggestion = "";

        public PresentTextFieldWidget(Font fontRenderer, int x, int y, int width, int height, Component text) {
            super(fontRenderer, x, y, width, height, text);
            this.font = fontRenderer;
            this.setResponder(this::onValueChanged);
            this.setTextColor(-1);
            this.setTextColorUneditable(-1);
            this.setBordered(false);
            this.setMaxLength(15);
            this.setCanLoseFocus(false);
        }

        @Override
        public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
            if (this.active) {
                PresentBlockGui.this.setFocused(this);
                return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
            }
            return false;
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            this.blit(poseStack, this.x - 3, this.y - 4, 0,
                    166 + (this.active ? 0 : 16), 110, 16);
            super.render(poseStack, mouseX, mouseY, partialTicks);
            if (this.getValue().isEmpty() && this.isFocused()) {
                drawString(poseStack, this.font, SEND_TO, this.x, this.y, 0);
            }
        }

        @Override
        public boolean keyPressed(int key, int p_94133_, int p_94134_) {
            //fill in suggestion
            if (key == 258 && this.canConsumeInput()) {
                if (!this.fullSuggestion.isEmpty()) {
                    this.setValue(this.fullSuggestion);
                }
                return true;
            }
            return super.keyPressed(key, p_94133_, p_94134_);
        }

        private void onValueChanged(String newValue) {
            List<String> list = PresentBlockGui.this.playerList.setFilter(newValue);
            String suggestion = "";
            this.fullSuggestion = "";
            if (!newValue.isEmpty()) {
                for (String s : list) {
                    suggestion = s.substring(newValue.length());
                    this.fullSuggestion = s;
                    break;
                }
            }
            this.setSuggestion(suggestion);
        }

        //editble/ non editable
        public void setState(boolean hasItem, boolean packed) {
            this.setSuggestion(null);
            if (packed) {
                this.setFocus(false);
                this.active = true;
            } else {
                this.setFocus(hasItem);
                this.setEditable(hasItem);
                this.active = hasItem;
                if (!hasItem) this.setValue("");
            }
        }
    }


}

package net.mehvahdjukaar.supplementaries.client.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.Textures;
import net.mehvahdjukaar.supplementaries.common.inventories.PresentContainerMenu;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetPresentPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class PresentBlockGui extends AbstractContainerScreen<PresentContainerMenu> implements ContainerListener {

    private static final int SUGGESTION_BOX_H = 12;
    private static final int MAX_SUGGESTIONS = 3;
    private static final int SUGGESTION_LIST_H = SUGGESTION_BOX_H * MAX_SUGGESTIONS;
    private static final int SCROLL_BAR_TOP_POS_Y = 33;
    private static final int SCROLL_BAR_START_X = 153;
    private static final int SCROLLER_W = 6;
    private static final int SCROLLER_H = 17;

    protected EditBox recipient;
    private PackButton packButton;
    private final PresentBlockTile tile;
    private int scrollOff;
    private boolean isDragging;

    private final List<String> suggestions = new ArrayList<>();

    public static MenuScreens.ScreenConstructor<PresentContainerMenu, PresentBlockGui> GUI_FACTORY =
            (container, inventory, title) -> {
                BlockEntity te = Minecraft.getInstance().level.getBlockEntity(container.getPos());
                if (te instanceof PresentBlockTile) {
                    return new PresentBlockGui(container, inventory, title, (PresentBlockTile) te);
                }
                return null;
            };

    public PresentBlockGui(PresentContainerMenu container, Inventory inventory, Component text, PresentBlockTile tile) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;

        this.tile = tile;
    }

    public void addPlayer(){

    }

    public void removePlayer(){

    }

    @Override
    public void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;


        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        this.packButton = this.addRenderableWidget(new PackButton(i + 14, j + 48));

        this.recipient = new PresentTextFieldWidget(this.font, i + 53, j + 18,
                99, 12, new TranslatableComponent("container.repair"));
        this.recipient.setCanLoseFocus(true);
        this.recipient.setTextColor(-1);
        this.recipient.setTextColorUneditable(-1);
        this.recipient.setBordered(false);
        this.recipient.setMaxLength(35);
        String rec = this.tile.getRecipient();
        if (!rec.isEmpty()) this.recipient.setValue(rec);

        this.addRenderableWidget(recipient);

        this.recipient.active = false;
        this.recipient.setEditable(false);

        this.menu.addSlotListener(this);

        if (!tile.getDisplayedItem().isEmpty()) {
            this.recipient.setFocus(true);
        }
        if (tile.isPacked()) this.setPacked();


    }


    private void setPacked() {
        this.packButton.active = false;
        this.recipient.active = false;
        this.recipient.setFocus(false);
        this.recipient.setEditable(false);
        this.setFocused(null);
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slot, ItemStack stack) {
        if (slot == 0) {
            if (stack.isEmpty()) {
                this.setFocused(null);
                this.recipient.active = false;
                this.recipient.setEditable(false);
                this.packButton.active = true;
                this.recipient.setValue("");

            } else {
                this.setFocused(recipient);
                this.recipient.setFocus(true);
                this.recipient.active = true;
                this.recipient.setEditable(true);
            }
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu container, int p_150525_, int p_150526_) {
        this.slotChanged(container, 0, container.getSlot(0).getItem());
    }


    private boolean canWrite() {
        return !tile.isPacked() && this.menu.getSlot(0).hasItem();
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Textures.PRESENT_BLOCK_GUI_TEXTURE);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, k, l, 0, 0, this.imageWidth, this.imageHeight);

        this.blit(matrixStack, k + 50, l + 14, 0, this.imageHeight + (this.canWrite() ? 0 : 16), 110, 16);

    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.recipient.render(poseStack, mouseX, mouseY, partialTicks);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.renderScroller(poseStack, x, y, 3);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    private void renderScroller(PoseStack poseStack, int x, int y, int playerListSize) {
        RenderSystem.setShaderTexture(0, Textures.PRESENT_BLOCK_GUI_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int currentIndex = playerListSize + 1 - MAX_SUGGESTIONS;
        if (currentIndex > 1) {
            int a = SUGGESTION_LIST_H - (SCROLLER_H + (currentIndex - 1) * SUGGESTION_LIST_H / currentIndex);
            int b = 1 + a / currentIndex + SUGGESTION_LIST_H / currentIndex;
            int scroll = Math.min(SUGGESTION_LIST_H - SCROLLER_H, this.scrollOff * b);
            if (this.scrollOff == currentIndex - 1) {
                scroll = SUGGESTION_LIST_H - SCROLLER_H;
            }

            blit(poseStack, x + SCROLL_BAR_START_X, y + SCROLL_BAR_TOP_POS_Y + scroll, this.getBlitOffset(), 0.0F, 232, SCROLLER_W, SCROLLER_H, 256, 256);
        } else {
            blit(poseStack, x + SCROLL_BAR_START_X, y + SCROLL_BAR_TOP_POS_Y, this.getBlitOffset(), SCROLLER_W, 232, SCROLLER_W, SCROLLER_H, 256, 256);
        }

    }

    @Override
    protected void renderLabels(PoseStack poseStack, int x, int y) {
        super.renderLabels(poseStack, x, y);

        if (packButton.isHoveredOrFocused()) {
            packButton.renderToolTip(poseStack, x - this.leftPos, y - this.topPos);
        }

    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
        }
        //up arrow
        if (key == 265) {
            //this.switchFocus();
            return true;
        }
        // down arrow, enter
        else if (key == 264 || key == 257 || key == 335) {
            //this.switchFocus();
            return true;
        }
        return this.recipient.keyPressed(key, a, b) || this.recipient.canConsumeInput() || super.keyPressed(key, a, b);
    }

    private void updateSuggestionList() {

    }

    private boolean canScroll(int i) {
        return i > MAX_SUGGESTIONS;
    }

    @Override
    public boolean mouseScrolled(double a, double b, double c) {
        int i = this.suggestions.size();
        if (this.canScroll(i)) {
            int j = i - MAX_SUGGESTIONS;
            this.scrollOff = (int) ((double) this.scrollOff - c);
            this.scrollOff = Mth.clamp(this.scrollOff, 0, j);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double dx, double dy, int key, double x, double y) {
        int i = this.suggestions.size();
        if (this.isDragging) {
            int j = this.topPos + SCROLL_BAR_TOP_POS_Y;
            int k = j + SUGGESTION_LIST_H;
            int l = i - MAX_SUGGESTIONS;
            float f = ((float) dy - (float) j - 13.5F) / ((float) (k - j) - SCROLLER_H);
            f = f * (float) l + 0.5F;
            this.scrollOff = Mth.clamp((int) f, 0, l);
            return true;
        } else {
            return super.mouseDragged(dx, dy, key, x, y);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int key) {
        this.isDragging = false;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (this.canScroll(this.suggestions.size()) && x > (double) (i + SCROLL_BAR_START_X) && x < (double) (i + SCROLL_BAR_START_X + SCROLLER_W) &&
                y > (double) (j + SCROLL_BAR_TOP_POS_Y) && y <= (double) (j + SCROLL_BAR_TOP_POS_Y + SUGGESTION_LIST_H + 1)) {
            this.isDragging = true;
        }

        return super.mouseClicked(x, y, key);
    }

    @Override
    public void removed() {
        super.removed();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
        String sender = Minecraft.getInstance().player.getName().getString();
        NetworkHandler.INSTANCE.sendToServer(new ServerBoundSetPresentPacket(this.tile.getBlockPos(),
                !this.packButton.active, this.recipient.getValue(), sender));
        //update client immediately
        this.tile.pack(this.recipient.getValue(), sender, !this.packButton.active);
    }

    public class PackButton extends AbstractButton {
        private boolean selected;

        protected PackButton(int x, int y) {
            super(x, y, 22, 22, TextComponent.EMPTY);
        }

        @Override
        public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
            RenderSystem.setShaderTexture(0, Textures.PRESENT_BLOCK_GUI_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int i = 198;
            int j = 0;
            if (!this.active) {
                j += this.width * 2;
            } else if (this.selected) {
                j += this.width * 1;
            } else if (this.isHovered) {
                j += this.width * 3;
            }

            this.blit(p_230431_1_, this.x, this.y, j, i, this.width, this.height);
        }


        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void renderToolTip(PoseStack matrixStack, int x, int y) {
            PresentBlockGui.this.renderTooltip(matrixStack, CommonComponents.GUI_DONE, x, y);
        }

        @Override
        public void onPress() {
            PresentBlockGui.this.setPacked();
        }

        @Override
        public void updateNarration(NarrationElementOutput p_169152_) {

        }
    }

    private class PresentTextFieldWidget extends EditBox {

        public PresentTextFieldWidget(Font fontRenderer, int x, int y, int width, int height, Component text) {
            super(fontRenderer, x, y, width, height, text);
        }

        @Override
        public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
            if (this.active) {
                PresentBlockGui.this.setFocused(this);
                return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
            }
            return false;
        }
    }


}

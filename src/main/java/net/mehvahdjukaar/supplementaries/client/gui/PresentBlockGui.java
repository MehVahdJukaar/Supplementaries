package net.mehvahdjukaar.supplementaries.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.inventories.PresentContainer;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.UpdateServerPresentPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;


public class PresentBlockGui extends AbstractContainerScreen<PresentContainer> implements ContainerListener {

    protected EditBox recipient;
    protected EditBox sender;

    private PackButton packButton;

    private final PresentBlockTile tile;

    public static MenuScreens.ScreenConstructor<PresentContainer, PresentBlockGui> GUI_FACTORY =
            (container, inventory, title) -> {
                BlockEntity te = Minecraft.getInstance().level.getBlockEntity(container.getPos());
                if (te instanceof PresentBlockTile) {
                    return new PresentBlockGui(container, inventory, title, (PresentBlockTile) te);
                }
                return null;
            };

    public PresentBlockGui(PresentContainer container, Inventory inventory, Component text, PresentBlockTile tile) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;

        this.tile = tile;
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;


        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        this.packButton = this.addButton(new PackButton(i + 14, j + 46));

        this.recipient = new PresentTextFieldWidget(this.font, i + 53, j + 27,
                99, 12, new TranslatableComponent("container.repair"));
        this.recipient.setCanLoseFocus(true);
        this.recipient.setTextColor(-1);
        this.recipient.setTextColorUneditable(-1);
        this.recipient.setBordered(false);
        this.recipient.setMaxLength(35);
        String rec = this.tile.getRecipient();
        if(!rec.isEmpty()) this.recipient.setValue(rec);
        this.children.add(this.recipient);

        this.sender = new PresentTextFieldWidget(this.font, i + 53, j + 53,
                99, 12, new TranslatableComponent("container.repair"));
        this.sender.setCanLoseFocus(true);
        this.sender.setTextColor(-1);
        this.sender.setTextColorUneditable(-1);
        this.sender.setBordered(false);
        this.sender.setMaxLength(35);
        String send = this.tile.getSender();
        if(!send.isEmpty()) this.sender.setValue(send);
        this.children.add(this.sender);

        this.sender.active = false;
        this.sender.setEditable(false);
        this.recipient.active = false;
        this.recipient.setEditable(false);

        this.menu.addSlotListener(this);

        if(!tile.getDisplayedItem().isEmpty()){
            this.recipient.setFocus(true);
        }
        if(tile.isPacked()) this.setPacked();


    }

    @Override
    public void tick() {
        super.tick();
        if (this.recipient != null)
            this.recipient.tick();
        if (this.sender != null)
            this.sender.tick();
    }

    @Override
    public void refreshContainer(AbstractContainerMenu container, NonNullList<ItemStack> itemStacks) {
        this.slotChanged(container, 0, container.getSlot(0).getItem());
    }

    @Override
    public void setContainerData(AbstractContainerMenu p_71112_1_, int p_71112_2_, int p_71112_3_) {
    }

    private void setPacked(){
        this.packButton.active = false;
        this.recipient.active = false;
        this.recipient.setFocus(false);
        this.recipient.setEditable(false);
        this.sender.active = false;
        this.sender.setFocus(false);
        this.sender.setEditable(false);
        this.setFocused(null);
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slot, ItemStack stack) {
        if (slot == 0) {
            if (stack.isEmpty()) {
                this.setFocused(null);
                this.recipient.active = false;
                this.sender.active = false;
                this.sender.setEditable(false);
                this.recipient.setEditable(false);
                this.packButton.active = true;
                this.sender.setValue("");
                this.recipient.setValue("");

            } else {
                this.setFocused(recipient);
                this.recipient.active = true;
                this.sender.active = true;
                this.sender.setEditable(true);
                this.recipient.setEditable(true);
            }
        }
    }


    private boolean canWrite() {
        return !tile.isPacked() && this.menu.getSlot(0).hasItem();
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(Textures.PRESENT_BLOCK_GUI_TEXTURE);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, k, l, 0, 0, this.imageWidth, this.imageHeight);

        this.blit(matrixStack, k + 50, l + 23, 0, this.imageHeight + (this.canWrite() ? 0 : 16), 110, 16);

        this.blit(matrixStack, k + 50, l + 49, 0, this.imageHeight + (this.canWrite() ? 0 : 16), 110, 16);

    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.sender.render(matrixStack, mouseX, mouseY, partialTicks);
        this.recipient.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack p_230451_1_, int p_230451_2_, int p_230451_3_) {
        super.renderLabels(p_230451_1_, p_230451_2_, p_230451_3_);

        if (packButton.isHovered()) {
            packButton.renderToolTip(p_230451_1_, p_230451_2_ - this.leftPos, p_230451_3_ - this.topPos);
        }

    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
        }
        //up arrow
        if (key == 265) {
            this.switchFocus();
            return true;
        }
        // down arrow, enter
        else if (key == 264 || key == 257 || key == 335) {
            this.switchFocus();
            return true;
        }

        return this.sender.keyPressed(key, a, b) || this.sender.canConsumeInput() ||
                this.recipient.keyPressed(key, a, b) || this.recipient.canConsumeInput()
                || super.keyPressed(key, a, b);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (((int) delta) % 2 != 0) this.switchFocus();
        return true;
    }

    public void switchFocus() {
        GuiEventListener focus = this.getFocused();
        if(focus == sender){
            this.sender.setFocus(false);
            this.recipient.setFocus(true);
            this.setFocused(recipient);
        }
        else if(focus == recipient){
            this.recipient.setFocus(false);
            this.sender.setFocus(true);
            this.setFocused(sender);
        }
    }


    @Override
    public void removed() {
        super.removed();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
        NetworkHandler.INSTANCE.sendToServer(new UpdateServerPresentPacket(this.tile.getBlockPos(),
                !this.packButton.active, this.recipient.getValue(), this.sender.getValue()));
    }

    public class PackButton extends AbstractButton {
        private boolean selected;

        protected PackButton(int x, int y) {
            super(x, y, 22, 22, TextComponent.EMPTY);
        }

        @Override
        public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
            Minecraft.getInstance().getTextureManager().bind(Textures.PRESENT_BLOCK_GUI_TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int i = 198;
            int j = 0;
            if (!this.active) {
                j += this.width * 2;
            } else if (this.selected) {
                j += this.width * 1;
            } else if (this.isHovered()) {
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

            //BeaconScreen.this.minecraft.getConnection().send(new CUpdateBeaconPacket(Effect.getId(BeaconScreen.this.primary), Effect.getId(BeaconScreen.this.secondary)));
            //BeaconScreen.this.minecraft.player.connection.send(new CCloseWindowPacket(BeaconScreen.this.minecraft.player.containerMenu.containerId));
            //BeaconScreen.this.minecraft.setScreen((Screen)null);
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
                PresentBlockGui.this.sender.setFocus(false);
                PresentBlockGui.this.recipient.setFocus(false);
                return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
            }
            return false;
        }
    }


}
package net.mehvahdjukaar.supplementaries.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.inventories.PresentContainer;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.UpdateServerPresentPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;


public class PresentBlockGui extends ContainerScreen<PresentContainer> implements IContainerListener {

    private TextFieldWidget recipient;
    private TextFieldWidget sender;

    private PackButton packButton;

    private final PresentBlockTile tile;

    public static ScreenManager.IScreenFactory<PresentContainer, PresentBlockGui> GUI_FACTORY =
            (container, inventory, title) -> {
                TileEntity te = Minecraft.getInstance().level.getBlockEntity(container.getPos());
                if(te instanceof PresentBlockTile){
                    return new PresentBlockGui(container,inventory,title, (PresentBlockTile) te);
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
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;


        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        this.packButton = this.addButton(new PackButton(i + 14, j + 46));

        this.recipient = new TextFieldWidget(this.font, i + 53, j + 27,
                99, 12, new TranslationTextComponent("container.repair"));
        this.recipient.setCanLoseFocus(true);
        this.recipient.setTextColor(-1);
        this.recipient.setTextColorUneditable(-1);
        this.recipient.setBordered(false);
        this.recipient.setMaxLength(35);
        this.children.add(this.recipient);

        this.sender = new TextFieldWidget(this.font, i + 53, j + 53,
                99, 12, new TranslationTextComponent("container.repair"));
        this.sender.setCanLoseFocus(true);
        this.sender.setTextColor(-1);
        this.sender.setTextColorUneditable(-1);
        this.sender.setBordered(false);
        this.sender.setMaxLength(35);
        this.children.add(this.sender);

        this.sender.active = false;
        this.sender.setEditable(false);
        this.recipient.active = false;
        this.recipient.setEditable(false);

        this.menu.addSlotListener(this);
    }

    @Override
    public void tick() {
        super.tick();
        if(this.recipient!=null)
        this.recipient.tick();
        if(this.sender!=null)
            this.sender.tick();
    }

    @Override
    public void refreshContainer(Container container, NonNullList<ItemStack> itemStacks) {
        this.slotChanged(container, 0, container.getSlot(0).getItem());
    }

    @Override
    public void setContainerData(Container p_71112_1_, int p_71112_2_, int p_71112_3_) {
    }

    public void slotChanged(Container container, int slot, ItemStack stack) {
        if (slot == 0) {
            if(stack.isEmpty()){
                this.setFocused(null);
                this.recipient.active = false;
                this.sender.active = false;
                this.sender.setEditable(false);
                this.recipient.setEditable(false);

            }
            else {
                this.setFocused(recipient);
                this.recipient.active = true;
                this.sender.active = true;
                this.sender.setEditable(true);
                this.recipient.setEditable(true);
            }
        }
    }


    private boolean canWrite(){
        return !tile.isPacked() && this.menu.getSlot(0).hasItem();
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(Textures.PRESENT_BLOCK_GUI_TEXTURE);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, k, l, 0, 0, this.imageWidth, this.imageHeight);

        this.blit(matrixStack, k + 50, l + 23, 0, this.imageHeight + (this.canWrite() ? 0 : 16), 110, 16);

        this.blit(matrixStack, k + 50, l + 49, 0, this.imageHeight + (this.canWrite() ? 0 : 16), 110, 16);

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.sender.render(matrixStack, mouseX, mouseY, partialTicks);
        this.recipient.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {
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
        else if(key == 264 || key == 257 || key == 335) {
            this.switchFocus();
            return true;
        }

        return this.sender.keyPressed(key, a, b) || this.sender.canConsumeInput() ||
                this.recipient.keyPressed(key, a, b) || this.recipient.canConsumeInput()
                || super.keyPressed(key, a, b);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(((int) delta)%2!=0)this.switchFocus();
        return true;
    }

    public void switchFocus(){
        this.setFocused(this.getFocused() == sender? recipient : sender);
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
            super(x, y, 22, 22, StringTextComponent.EMPTY);
        }

        public void renderButton(MatrixStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
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

        public void renderToolTip(MatrixStack matrixStack, int x, int y) {
            PresentBlockGui.this.renderTooltip(matrixStack, DialogTexts.GUI_DONE, x, y);
        }

        public void onPress() {
            this.active = false;
            PresentBlockGui.this.recipient.active = false;
            PresentBlockGui.this.sender.active = false;
            PresentBlockGui.this.setFocused(null);
            PresentBlockGui.this.sender.setEditable(false);
            PresentBlockGui.this.recipient.setEditable(false);
            //BeaconScreen.this.minecraft.getConnection().send(new CUpdateBeaconPacket(Effect.getId(BeaconScreen.this.primary), Effect.getId(BeaconScreen.this.secondary)));
            //BeaconScreen.this.minecraft.player.connection.send(new CCloseWindowPacket(BeaconScreen.this.minecraft.player.containerMenu.containerId));
            //BeaconScreen.this.minecraft.setScreen((Screen)null);
        }
    }



}
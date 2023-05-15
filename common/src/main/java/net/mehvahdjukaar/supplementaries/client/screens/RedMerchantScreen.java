package net.mehvahdjukaar.supplementaries.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.supplementaries.common.inventories.RedMerchantMenu;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.Iterator;


public class RedMerchantScreen extends AbstractContainerScreen<RedMerchantMenu> {

    private static final ResourceLocation TEXTURE = ModTextures.RED_MERCHANT_GUI_TEXTURE;
    private static final MutableComponent TRADE_OFFER = Component.translatable("gui.supplementaries.orange_trader.trade");
    private static final MutableComponent I_RECEIVE = Component.translatable("gui.supplementaries.orange_trader.get");
    private static final MutableComponent YOU_RECEIVE = Component.translatable("gui.supplementaries.orange_trader.receive");
    private static final Component TRADES_LABEL = Component.translatable("merchant.trades");

    private static final Component LEVEL_SEPARATOR = Component.literal(" - ");
    private static final Component DEPRECATED_TOOLTIP = Component.translatable("merchant.deprecated");
    /**
     * The integer value corresponding to the currently selected merchant recipe.
     */
    private int shopItem;
    private final TradeOfferButton[] tradeOfferButtons = new TradeOfferButton[7];
    int scrollOff;
    private boolean isDragging;

    public RedMerchantScreen(RedMerchantMenu merchantMenu, Inventory inventory, Component component) {
        super(merchantMenu, inventory, component);
        this.imageWidth = 276;
        this.inventoryLabelX = 107;
    }

    private void postButtonClick() {
        (this.menu).setSelectionHint(this.shopItem);
        (this.menu).tryMoveItems(this.shopItem);
        this.minecraft.getConnection().send((new ServerboundSelectTradePacket(this.shopItem)));
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 16 + 2;

        for (int l = 0; l < 7; ++l) {
            this.tradeOfferButtons[l] = this.addRenderableWidget(new TradeOfferButton(i + 5, k, l, (button) -> {
                if (button instanceof TradeOfferButton b) {
                    this.shopItem = (b).getIndex() + this.scrollOff;
                    this.postButtonClick();
                }

            }));
            k += 20;
        }

    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        int i = (this.menu).getTraderLevel();
        if (i > 0 && i <= 5 && (this.menu).showProgressBar()) {
            Component component = this.title.copy().append(LEVEL_SEPARATOR).append( Component.translatable("merchant.level." + i));
            int j = this.font.width(component);
            int k = 49 + this.imageWidth / 2 - j / 2;
            this.font.draw(poseStack, component, k, 6.0F, 4210752);
        } else {
            this.font.draw(poseStack, this.title, (49 + this.imageWidth / 2f - this.font.width(this.title) / 2f), 6.0F, 4210752);
        }

        this.font.draw(poseStack, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752);
        int l = this.font.width(TRADES_LABEL);
        this.font.draw(poseStack, TRADES_LABEL, (5 - l / 2f + 48), 6.0F, 4210752);


        //extra stuff here

        MutableComponent tradeOffer = TRADE_OFFER
                .withStyle(ChatFormatting.WHITE)
                .withStyle(ChatFormatting.BOLD);
        this.font.draw(poseStack, tradeOffer, (49 + this.imageWidth / 2f - this.font.width(tradeOffer) / 2f), 10.0F, 4210752);

        Component iReceive = I_RECEIVE
                .withStyle(ChatFormatting.WHITE);
        this.font.draw(poseStack, iReceive, (49 - 29 + this.imageWidth / 2f - this.font.width(iReceive) / 2f), 24.0F, 4210752);

        Component uReceive = YOU_RECEIVE
                .withStyle(ChatFormatting.WHITE);
        this.font.draw(poseStack, uReceive, (49 + 42 + this.imageWidth / 2f - this.font.width(uReceive) / 2f), 24.0F, 4210752);

    }

    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        blit(poseStack, i, j, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 256);
        MerchantOffers merchantOffers = this.menu.getOffers();
        if (!merchantOffers.isEmpty()) {
            int k = this.shopItem;
            if (k < 0 || k >= merchantOffers.size()) {
                return;
            }

            MerchantOffer merchantOffer = merchantOffers.get(k);
            if (merchantOffer.isOutOfStock()) {
                RenderSystem.setShaderTexture(0, TEXTURE);
                blit(poseStack, this.leftPos + 83 + 99, this.topPos + 35, 0, 311.0F, 0.0F, 28, 21, 512, 256);
            }
        }

    }

    private void renderProgressBar(PoseStack poseStack, int posX, int posY, MerchantOffer merchantOffer) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.menu).getTraderLevel();
        int j = (this.menu).getTraderXp();
        if (i < 5) {
            blit(poseStack, posX + 136, posY + 16, 0, 0.0F, 186.0F, 102, 5, 512, 256);
            int k = VillagerData.getMinXpPerLevel(i);
            if (j >= k && VillagerData.canLevelUp(i)) {
                float f = 100.0F / (VillagerData.getMaxXpPerLevel(i) - k);
                int m = Math.min(Mth.floor(f * (j - k)), 100);
                blit(poseStack, posX + 136, posY + 16, 0, 0.0F, 191.0F, m + 1, 5, 512, 256);
                int n = (this.menu).getFutureTraderXp();
                if (n > 0) {
                    int o = Math.min(Mth.floor(n * f), 100 - m);
                    blit(poseStack, posX + 136 + m + 1, posY + 16 + 1, 0, 2.0F, 182.0F, o, 3, 512, 256);
                }

            }
        }
    }

    private void renderScroller(PoseStack poseStack, int posX, int posY, MerchantOffers merchantOffers) {
        int i = merchantOffers.size() + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int m = Math.min(113, this.scrollOff * k);
            if (this.scrollOff == i - 1) {
                m = 113;
            }

            blit(poseStack, posX + 94, posY + 18 + m, 0, 0.0F, 199.0F, 6, 27, 512, 256);
        } else {
            blit(poseStack, posX + 94, posY + 18, 0, 6.0F, 199.0F, 6, 27, 512, 256);
        }

    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        MerchantOffers merchantOffers = (this.menu).getOffers();
        if (!merchantOffers.isEmpty()) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            int k = j + 16 + 1;
            int l = i + 5 + 5;
            RenderSystem.setShaderTexture(0, TEXTURE);
            this.renderScroller(poseStack, i, j, merchantOffers);
            int m = 0;
            Iterator<MerchantOffer> var11 = merchantOffers.iterator();

            MerchantOffer merchantOffer;
            while (var11.hasNext()) {
                merchantOffer = var11.next();
                if (!this.canScroll(merchantOffers.size()) || (m >= this.scrollOff && m < 7 + this.scrollOff)) {
                    ItemStack itemStack = merchantOffer.getBaseCostA();
                    ItemStack itemStack2 = merchantOffer.getCostA();
                    ItemStack itemStack3 = merchantOffer.getCostB();
                    ItemStack itemStack4 = merchantOffer.getResult();
                    poseStack.pushPose();
                    poseStack.translate(0.0F, 0.0F, 100.0F);
                    int n = k + 2;
                    this.renderAndDecorateCostA(poseStack, itemStack2, itemStack, l, n);
                    if (!itemStack3.isEmpty()) {
                        this.itemRenderer.renderAndDecorateFakeItem(poseStack, itemStack3, i + 5 + 35, n);
                        this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, itemStack3, i + 5 + 35, n);
                    }

                    this.renderButtonArrows(poseStack, merchantOffer, i, n);
                    this.itemRenderer.renderAndDecorateFakeItem(poseStack, itemStack4, i + 5 + 68, n);
                    this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, itemStack4, i + 5 + 68, n);
                    poseStack.popPose();
                    k += 20;
                }
                ++m;
            }

            int o = this.shopItem;
            merchantOffer = merchantOffers.get(o);
            if ((this.menu).showProgressBar()) {
                this.renderProgressBar(poseStack, i, j, merchantOffer);
            }

            if (merchantOffer.isOutOfStock() && this.isHovering(186, 35, 22, 21, mouseX, mouseY) && (this.menu).canRestock()) {
                this.renderTooltip(poseStack, DEPRECATED_TOOLTIP, mouseX, mouseY);
            }

            for (TradeOfferButton tradeOfferButton : this.tradeOfferButtons) {
                if (tradeOfferButton.isHoveredOrFocused()) {
                    tradeOfferButton.renderToolTip(poseStack, mouseX, mouseY);
                }

                tradeOfferButton.visible = tradeOfferButton.index < (this.menu).getOffers().size();
            }

            RenderSystem.enableDepthTest();
        }

        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    private void renderButtonArrows(PoseStack poseStack, MerchantOffer merchantOffer, int posX, int posY) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, TEXTURE);
        if (merchantOffer.isOutOfStock()) {
            blit(poseStack, posX + 5 + 35 + 20, posY + 3, 0, 25.0F, 171.0F, 10, 9, 512, 256);
        } else {
            blit(poseStack, posX + 5 + 35 + 20, posY + 3, 0, 15.0F, 171.0F, 10, 9, 512, 256);
        }

    }

    private void renderAndDecorateCostA(PoseStack poseStack, ItemStack realCost, ItemStack baseCost, int x, int y) {
        this.itemRenderer.renderAndDecorateFakeItem(poseStack, realCost, x, y);
        if (baseCost.getCount() == realCost.getCount()) {
            this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, realCost, x, y);
        } else {
            this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, baseCost, x, y, baseCost.getCount() == 1 ? "1" : null);
            this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, realCost, x + 14, y, realCost.getCount() == 1 ? "1" : null);
            RenderSystem.setShaderTexture(0, TEXTURE);
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 300.0F);
            blit(poseStack, x + 7, y + 12, 0, 0.0F, 176.0F, 9, 2, 512, 256);
            poseStack.popPose();
        }

    }

    private boolean canScroll(int numOffers) {
        return numOffers > 7;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int i = (this.menu).getOffers().size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.scrollOff = Mth.clamp((int) (this.scrollOff - delta), 0, j);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int i = (this.menu).getOffers().size();
        if (this.isDragging) {
            int j = this.topPos + 18;
            int k = j + 139;
            int l = i - 7;
            float f = ((float) mouseY - j - 13.5F) / ((k - j) - 27.0F);
            f = f * l + 0.5F;
            this.scrollOff = Mth.clamp((int) f, 0, l);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (this.canScroll((this.menu).getOffers().size()) && mouseX > (i + 94) && mouseX < (i + 94 + 6) && mouseY > (j + 18) && mouseY <= (j + 18 + 139 + 1)) {
            this.isDragging = true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Environment(EnvType.CLIENT)
    class TradeOfferButton extends Button {
        final int index;

        public TradeOfferButton(int i, int j, int k, Button.OnPress onPress) {
            super(i, j, 88, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
            this.index = k;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
            if (this.isHovered && (RedMerchantScreen.this.menu).getOffers().size() > this.index + RedMerchantScreen.this.scrollOff) {
                ItemStack itemStack;
                if (mouseX < this.getX() + 20) {
                    itemStack = ((RedMerchantScreen.this.menu).getOffers().get(this.index + RedMerchantScreen.this.scrollOff)).getCostA();
                    RedMerchantScreen.this.renderTooltip(poseStack, itemStack, mouseX, mouseY);
                } else if (mouseX < this.getX() + 50 && mouseX > this.getX() + 30) {
                    itemStack = ((RedMerchantScreen.this.menu).getOffers().get(this.index + RedMerchantScreen.this.scrollOff)).getCostB();
                    if (!itemStack.isEmpty()) {
                        RedMerchantScreen.this.renderTooltip(poseStack, itemStack, mouseX, mouseY);
                    }
                } else if (mouseX > this.getX() + 65) {
                    itemStack = ((RedMerchantScreen.this.menu).getOffers().get(this.index + RedMerchantScreen.this.scrollOff)).getResult();
                    RedMerchantScreen.this.renderTooltip(poseStack, itemStack, mouseX, mouseY);
                }
            }

        }
    }
}


package net.mehvahdjukaar.supplementaries.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.inventories.RedMerchantContainer;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.SelectOrangeTraderTradePacket;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OrangeMerchantGui extends ContainerScreen<RedMerchantContainer> {
    private static final ResourceLocation VILLAGER_LOCATION = Textures.ORANGE_MERCHANT_GUI_TEXTURE;
    private static final ITextComponent TRADES_LABEL = new TranslationTextComponent("merchant.trades");
    private static final ITextComponent LEVEL_SEPARATOR = new StringTextComponent(" - ");
    private static final ITextComponent DEPRECATED_TOOLTIP = new TranslationTextComponent("merchant.deprecated");
    private int shopItem;
    private final OrangeMerchantGui.TradeButton[] tradeOfferButtons = new OrangeMerchantGui.TradeButton[7];
    private int scrollOff;
    private boolean isDragging;

    public OrangeMerchantGui(RedMerchantContainer p_i51080_1_, PlayerInventory p_i51080_2_, ITextComponent p_i51080_3_) {
        super(p_i51080_1_, p_i51080_2_, p_i51080_3_);
        this.imageWidth = 276;
        this.inventoryLabelX = 107;
    }

    private void postButtonClick() {
        this.menu.setSelectionHint(this.shopItem);
        this.menu.tryMoveItems(this.shopItem);

        NetworkHandler.sendToServerPlayer(new SelectOrangeTraderTradePacket(this.shopItem));
    }

    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 16 + 2;

        for(int l = 0; l < 7; ++l) {
            this.tradeOfferButtons[l] = this.addButton(new OrangeMerchantGui.TradeButton(i + 5, k, l, (p_214132_1_) -> {
                if (p_214132_1_ instanceof OrangeMerchantGui.TradeButton) {
                    this.shopItem = ((OrangeMerchantGui.TradeButton)p_214132_1_).getIndex() + this.scrollOff;
                    this.postButtonClick();
                }

            }));
            k += 20;
        }

    }

    protected void renderLabels(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {


        ITextComponent tradeOffer = new TranslationTextComponent("gui.supplementaries.orange_trader.trade")
                .withStyle(TextFormatting.WHITE)
                .withStyle(TextFormatting.BOLD);
        this.font.draw(p_230451_1_, tradeOffer, (float)(49 + this.imageWidth / 2 - this.font.width(tradeOffer) / 2), 10.0F, 4210752);

        ITextComponent iReceive = new TranslationTextComponent("gui.supplementaries.orange_trader.get")
                .withStyle(TextFormatting.WHITE);
        this.font.draw(p_230451_1_, iReceive, (float)(49 -29 + this.imageWidth / 2 - this.font.width(iReceive) / 2), 24.0F, 4210752);

        ITextComponent uReceive = new TranslationTextComponent("gui.supplementaries.orange_trader.receive")
                .withStyle(TextFormatting.WHITE);
        this.font.draw(p_230451_1_, uReceive, (float)(49 + 42 + this.imageWidth / 2 - this.font.width(uReceive) / 2), 24.0F, 4210752);


        this.font.draw(p_230451_1_, this.inventory.getDisplayName(), (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
        int l = this.font.width(TRADES_LABEL);
        this.font.draw(p_230451_1_, TRADES_LABEL, (float)(5 - l / 2 + 48), 6.0F, 4210752);
    }

    protected void renderBg(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        blit(p_230450_1_, i, j, this.getBlitOffset(), 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 512);
        MerchantOffers merchantoffers = this.menu.getOffers();
        if (!merchantoffers.isEmpty()) {
            int k = this.shopItem;
            if (k < 0 || k >= merchantoffers.size()) {
                return;
            }

            MerchantOffer merchantoffer = merchantoffers.get(k);
            if (merchantoffer.isOutOfStock()) {
                this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                blit(p_230450_1_, this.leftPos + 83 + 99, this.topPos + 35, this.getBlitOffset(), 311.0F, 0.0F, 28, 21, 256, 512);
            }
        }

    }

    private void renderProgressBar(MatrixStack p_238839_1_, int p_238839_2_, int p_238839_3_, MerchantOffer p_238839_4_) {
        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
        int i = this.menu.getTraderLevel();
        int j = this.menu.getTraderXp();
        if (i < 5) {
            blit(p_238839_1_, p_238839_2_ + 136, p_238839_3_ + 16, this.getBlitOffset(), 0.0F, 186.0F, 102, 5, 256, 512);
            int k = VillagerData.getMinXpPerLevel(i);
            if (j >= k && VillagerData.canLevelUp(i)) {
                int l = 100;
                float f = 100.0F / (float)(VillagerData.getMaxXpPerLevel(i) - k);
                int i1 = Math.min(MathHelper.floor(f * (float)(j - k)), 100);
                blit(p_238839_1_, p_238839_2_ + 136, p_238839_3_ + 16, this.getBlitOffset(), 0.0F, 191.0F, i1 + 1, 5, 256, 512);
                int j1 = this.menu.getFutureTraderXp();
                if (j1 > 0) {
                    int k1 = Math.min(MathHelper.floor((float)j1 * f), 100 - i1);
                    blit(p_238839_1_, p_238839_2_ + 136 + i1 + 1, p_238839_3_ + 16 + 1, this.getBlitOffset(), 2.0F, 182.0F, k1, 3, 256, 512);
                }

            }
        }
    }

    private void renderScroller(MatrixStack p_238840_1_, int p_238840_2_, int p_238840_3_, MerchantOffers p_238840_4_) {
        int i = p_238840_4_.size() + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int l = 113;
            int i1 = Math.min(113, this.scrollOff * k);
            if (this.scrollOff == i - 1) {
                i1 = 113;
            }

            blit(p_238840_1_, p_238840_2_ + 94, p_238840_3_ + 18 + i1, this.getBlitOffset(), 0.0F, 199.0F, 6, 27, 256, 512);
        } else {
            blit(p_238840_1_, p_238840_2_ + 94, p_238840_3_ + 18, this.getBlitOffset(), 6.0F, 199.0F, 6, 27, 256, 512);
        }

    }

    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        this.renderBackground(p_230430_1_);
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        MerchantOffers merchantoffers = this.menu.getOffers();
        if (!merchantoffers.isEmpty()) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            int k = j + 16 + 1;
            int l = i + 5 + 5;
            RenderSystem.pushMatrix();
            RenderSystem.enableRescaleNormal();
            this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
            this.renderScroller(p_230430_1_, i, j, merchantoffers);
            int i1 = 0;

            for(MerchantOffer merchantoffer : merchantoffers) {
                if (this.canScroll(merchantoffers.size()) && (i1 < this.scrollOff || i1 >= 7 + this.scrollOff)) {
                    ++i1;
                } else {
                    ItemStack itemstack = merchantoffer.getBaseCostA();
                    ItemStack itemstack1 = merchantoffer.getCostA();
                    ItemStack itemstack2 = merchantoffer.getCostB();
                    ItemStack itemstack3 = merchantoffer.getResult();
                    this.itemRenderer.blitOffset = 100.0F;
                    int j1 = k + 2;
                    this.renderAndDecorateCostA(p_230430_1_, itemstack1, itemstack, l, j1);
                    if (!itemstack2.isEmpty()) {
                        this.itemRenderer.renderAndDecorateFakeItem(itemstack2, i + 5 + 35, j1);
                        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack2, i + 5 + 35, j1);
                    }

                    this.renderButtonArrows(p_230430_1_, merchantoffer, i, j1);
                    this.itemRenderer.renderAndDecorateFakeItem(itemstack3, i + 5 + 68, j1);
                    this.itemRenderer.renderGuiItemDecorations(this.font, itemstack3, i + 5 + 68, j1);
                    this.itemRenderer.blitOffset = 0.0F;
                    k += 20;
                    ++i1;
                }
            }

            int k1 = this.shopItem;
            MerchantOffer merchantoffer1 = merchantoffers.get(k1);
            if (this.menu.showProgressBar()) {
                this.renderProgressBar(p_230430_1_, i, j, merchantoffer1);
            }

            if (merchantoffer1.isOutOfStock() && this.isHovering(186, 35, 22, 21, (double)p_230430_2_, (double)p_230430_3_) && this.menu.canRestock()) {
                this.renderTooltip(p_230430_1_, DEPRECATED_TOOLTIP, p_230430_2_, p_230430_3_);
            }

            for(OrangeMerchantGui.TradeButton merchantscreen$tradebutton : this.tradeOfferButtons) {
                if (merchantscreen$tradebutton.isHovered()) {
                    merchantscreen$tradebutton.renderToolTip(p_230430_1_, p_230430_2_, p_230430_3_);
                }

                merchantscreen$tradebutton.visible = merchantscreen$tradebutton.index < this.menu.getOffers().size();
            }

            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
        }

        this.renderTooltip(p_230430_1_, p_230430_2_, p_230430_3_);
    }

    private void renderButtonArrows(MatrixStack p_238842_1_, MerchantOffer p_238842_2_, int p_238842_3_, int p_238842_4_) {
        RenderSystem.enableBlend();
        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
        if (p_238842_2_.isOutOfStock()) {
            blit(p_238842_1_, p_238842_3_ + 5 + 35 + 20, p_238842_4_ + 3, this.getBlitOffset(), 25.0F, 171.0F, 10, 9, 256, 512);
        } else {
            blit(p_238842_1_, p_238842_3_ + 5 + 35 + 20, p_238842_4_ + 3, this.getBlitOffset(), 15.0F, 171.0F, 10, 9, 256, 512);
        }

    }

    private void renderAndDecorateCostA(MatrixStack p_238841_1_, ItemStack p_238841_2_, ItemStack p_238841_3_, int p_238841_4_, int p_238841_5_) {
        this.itemRenderer.renderAndDecorateFakeItem(p_238841_2_, p_238841_4_, p_238841_5_);
        if (p_238841_3_.getCount() == p_238841_2_.getCount()) {
            this.itemRenderer.renderGuiItemDecorations(this.font, p_238841_2_, p_238841_4_, p_238841_5_);
        } else {
            this.itemRenderer.renderGuiItemDecorations(this.font, p_238841_3_, p_238841_4_, p_238841_5_, p_238841_3_.getCount() == 1 ? "1" : null);
            this.itemRenderer.renderGuiItemDecorations(this.font, p_238841_2_, p_238841_4_ + 14, p_238841_5_, p_238841_2_.getCount() == 1 ? "1" : null);
            this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
            this.setBlitOffset(this.getBlitOffset() + 300);
            blit(p_238841_1_, p_238841_4_ + 7, p_238841_5_ + 12, this.getBlitOffset(), 0.0F, 176.0F, 9, 2, 256, 512);
            this.setBlitOffset(this.getBlitOffset() - 300);
        }

    }

    private boolean canScroll(int p_214135_1_) {
        return p_214135_1_ > 7;
    }

    public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double p_231043_5_) {
        int i = this.menu.getOffers().size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.scrollOff = (int)((double)this.scrollOff - p_231043_5_);
            this.scrollOff = MathHelper.clamp(this.scrollOff, 0, j);
        }

        return true;
    }

    public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) {
        int i = this.menu.getOffers().size();
        if (this.isDragging) {
            int j = this.topPos + 18;
            int k = j + 139;
            int l = i - 7;
            float f = ((float)p_231045_3_ - (float)j - 13.5F) / ((float)(k - j) - 27.0F);
            f = f * (float)l + 0.5F;
            this.scrollOff = MathHelper.clamp((int)f, 0, l);
            return true;
        } else {
            return super.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        }
    }

    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        this.isDragging = false;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (this.canScroll(this.menu.getOffers().size()) && p_231044_1_ > (double)(i + 94) && p_231044_1_ < (double)(i + 94 + 6) && p_231044_3_ > (double)(j + 18) && p_231044_3_ <= (double)(j + 18 + 139 + 1)) {
            this.isDragging = true;
        }

        return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }

    @OnlyIn(Dist.CLIENT)
    class TradeButton extends Button {
        final int index;

        public TradeButton(int p_i50601_2_, int p_i50601_3_, int p_i50601_4_, Button.IPressable p_i50601_5_) {
            super(p_i50601_2_, p_i50601_3_, 89, 20, StringTextComponent.EMPTY, p_i50601_5_);
            this.index = p_i50601_4_;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(MatrixStack p_230443_1_, int p_230443_2_, int p_230443_3_) {
            if (this.isHovered && OrangeMerchantGui.this.menu.getOffers().size() > this.index + OrangeMerchantGui.this.scrollOff) {
                if (p_230443_2_ < this.x + 20) {
                    ItemStack itemstack = OrangeMerchantGui.this.menu.getOffers().get(this.index + OrangeMerchantGui.this.scrollOff).getCostA();
                    OrangeMerchantGui.this.renderTooltip(p_230443_1_, itemstack, p_230443_2_, p_230443_3_);
                } else if (p_230443_2_ < this.x + 50 && p_230443_2_ > this.x + 30) {
                    ItemStack itemstack2 = OrangeMerchantGui.this.menu.getOffers().get(this.index + OrangeMerchantGui.this.scrollOff).getCostB();
                    if (!itemstack2.isEmpty()) {
                        OrangeMerchantGui.this.renderTooltip(p_230443_1_, itemstack2, p_230443_2_, p_230443_3_);
                    }
                } else if (p_230443_2_ > this.x + 65) {
                    ItemStack itemstack1 = OrangeMerchantGui.this.menu.getOffers().get(this.index + OrangeMerchantGui.this.scrollOff).getResult();
                    OrangeMerchantGui.this.renderTooltip(p_230443_1_, itemstack1, p_230443_2_, p_230443_3_);
                }
            }

        }
    }
}

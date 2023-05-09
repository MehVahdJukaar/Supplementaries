package net.mehvahdjukaar.supplementaries.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;

public class RedMerchantScreen extends MerchantScreen {
    private static final ResourceLocation TEXTURE = ModTextures.RED_MERCHANT_GUI_TEXTURE;
    private static final MutableComponent TRADE_OFFER = Component.translatable("gui.supplementaries.orange_trader.trade");
    private static final MutableComponent I_RECEIVE = Component.translatable("gui.supplementaries.orange_trader.get");
    private static final MutableComponent YOU_RECEIVE = Component.translatable("gui.supplementaries.orange_trader.receive");

    //TODO: use render screen event
    public RedMerchantScreen(MerchantMenu merchantMenu, Inventory inventory, Component component) {
        super(merchantMenu, inventory, component);
    }


    @Override
    protected void renderLabels(PoseStack pPoseStack, int pX, int pY) {

        super.renderLabels(pPoseStack, pX, pY);

        //TODO: blit trade offer thing

        MutableComponent tradeOffer = TRADE_OFFER
                .withStyle(ChatFormatting.WHITE)
                .withStyle(ChatFormatting.BOLD);
        this.font.draw(pPoseStack, tradeOffer, (49 + this.imageWidth / 2f - this.font.width(tradeOffer) / 2f), 10.0F, 4210752);

        Component iReceive = I_RECEIVE
                .withStyle(ChatFormatting.WHITE);
        this.font.draw(pPoseStack, iReceive, (49 - 29 + this.imageWidth / 2f - this.font.width(iReceive) / 2f), 24.0F, 4210752);

        Component uReceive = YOU_RECEIVE
                .withStyle(ChatFormatting.WHITE);
        this.font.draw(pPoseStack, uReceive, (49 + 42 + this.imageWidth / 2f - this.font.width(uReceive) / 2f), 24.0F, 4210752);
    }
}

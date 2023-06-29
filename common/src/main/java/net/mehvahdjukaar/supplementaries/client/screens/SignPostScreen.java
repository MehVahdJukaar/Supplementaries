package net.mehvahdjukaar.supplementaries.client.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FramedBlocksCompat;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class SignPostScreen extends TextHolderEditScreen<SignPostBlockTile> {

    private ModelPart signModel;

    private SignPostScreen(SignPostBlockTile tile) {
        super(tile, Component.translatable("sign.edit"));
        this.textHolderIndex = !this.tile.getSignUp().active() ? 1 : 0;
    }

    public static void open(SignPostBlockTile teSign) {
        Minecraft.getInstance().setScreen(new SignPostScreen(teSign));
    }

    @Override
    protected boolean canScroll() {
        return this.tile.getSignUp().active() && this.tile.getSignDown().active();

    }


    @Override
    protected void init() {
        super.init();
        this.signModel = this.minecraft.getEntityModels().bakeLayer(ClientRegistry.SIGN_POST_MODEL);
    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Lighting.setupForFlatItems();
        this.renderBackground(graphics);

        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);

        PoseStack poseStack = graphics.pose();
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        poseStack.pushPose();
        poseStack.translate(this.width / 2d, 0.0D, 50.0D);
        poseStack.scale(93.75F, -93.75F, 93.75F);
        poseStack.translate(0.0D, -1.3125D, 0.0D);
        // renders sign
        poseStack.pushPose();

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var signUp = tile.getSignUp();
        var signDown = tile.getSignDown();
        boolean leftUp = signUp.left();
        boolean leftDown = signDown.left();

        int[] o = new int[2];
        o[0] = leftUp ? 1 : -1;
        o[1] = leftDown ? 1 : -1;

        //render signs

        boolean blink = this.updateCounter / 6 % 2 == 0;

        poseStack.pushPose();
        renderSign(poseStack, bufferSource, signUp, leftUp);

        poseStack.translate(0, -0.5, 0);
        renderSign(poseStack, bufferSource, signDown, leftDown);

        poseStack.popPose();

        //render fence
        poseStack.translate(-0.5, -0.5, -0.5);
        BlockState fence = this.tile.getHeldBlock();
        if (CompatHandler.FRAMEDBLOCKS && tile.isFramed()) fence = FramedBlocksCompat.getFramedFence();
        if (fence != null) {
            blockRenderer.renderSingleBlock(fence, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();

        //renders text

        if (signUp.active() || signDown.active()) {
            poseStack.translate(-3 * 0.010416667F * o[0], 0.21875, 0.1875 + 0.005);
            poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);

            int cursorPos = this.textInputUtil.getCursorPos();
            int selectionPos = this.textInputUtil.getSelectionPos();

            if (signUp.active()) {
                var properties = tile.getTextHolder(0).getGUIRenderTextProperties();
                TextUtil.renderGuiLine(properties, this.messages[0][0], font, graphics, bufferSource,
                        cursorPos, selectionPos, this.textHolderIndex == 0, blink, -10);
            }
            if (signDown.active()) {
                poseStack.translate(-3 * o[1], 0, 0);
                var properties = tile.getTextHolder(1).getGUIRenderTextProperties();
                TextUtil.renderGuiLine(properties, this.messages[1][0], font, graphics, bufferSource,
                        cursorPos, selectionPos, this.textHolderIndex == 1, blink, 48 - 10);
            }
        }

        poseStack.popPose();
        Lighting.setupFor3DItems();
    }

    private void renderSign(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, SignPostBlockTile.Sign active, boolean leftDown) {
        if (active.active()) {

            poseStack.pushPose();
            if (!leftDown) {
                poseStack.mulPose(RotHlpr.YN180);
                poseStack.translate(0, 0, -0.3125);
            }
            poseStack.scale(1, -1, -1);
            Material material = ModMaterials.SIGN_POSTS_MATERIALS.get().get(active.woodType());
            VertexConsumer builder = material.buffer(bufferSource, RenderType::entitySolid);

            this.signModel.render(poseStack, builder, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}
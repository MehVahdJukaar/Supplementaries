package net.mehvahdjukaar.supplementaries.client.screens;


import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.DoormatBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.DoormatBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.DoormatBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class DoormatScreen extends TextHolderEditScreen<DoormatBlockTile> {

    private final BlockState state;

    private DoormatScreen(DoormatBlockTile tile) {
        super(tile, Component.translatable("gui.supplementaries.doormat.edit"));
        this.state = this.tile.getBlockState().getBlock().defaultBlockState().setValue(DoormatBlock.FACING, Direction.EAST);
    }

    public static void open(DoormatBlockTile tile) {
        Minecraft.getInstance().setScreen(new DoormatScreen(tile));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

        super.render(graphics, mouseX, mouseY, partialTicks);

        Lighting.setupForFlatItems();
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);


        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        poseStack.translate((this.width / 2d), 0.0D, 50.0D);
        poseStack.scale(93.75F, -93.75F, 93.75F);
        poseStack.translate(0.0D, -1.25D, 0.0D);

        // renders sign
        poseStack.pushPose();

        poseStack.mulPose(RotHlpr.Y90);
        poseStack.translate(0, -0.5, -0.5);
        poseStack.mulPose(RotHlpr.Z90);

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        blockRenderer.renderSingleBlock(state, graphics.pose(), bufferSource, 15728880, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();


        //renders text
        boolean blink = this.updateCounter / 6 % 2 == 0;

        poseStack.translate(0, 0.0625 - 2 * 0.010416667F, 0.0625 + 0.005);
        poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);

        TextUtil.renderGuiText(this.tile.textHolder.getGUIRenderTextProperties(),
                this.messages[textHolderIndex], this.font, graphics,
                this.textInputUtil.getCursorPos(), this.textInputUtil.getSelectionPos(),
                this.lineIndex, blink, DoormatBlockTileRenderer.LINE_SEPARATION);

        poseStack.popPose();
        Lighting.setupFor3DItems();
    }
}


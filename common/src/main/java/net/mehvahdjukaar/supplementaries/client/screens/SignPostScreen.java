package net.mehvahdjukaar.supplementaries.client.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetTextHolderPacket;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FramedBlocksCompat;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import java.util.stream.IntStream;

public class SignPostScreen extends Screen {
    private TextFieldHelper textInputUtil;
    /**
     * The index of the line that is being edited.
     */
    private int editLine;
    //for ticking cursor
    private int updateCounter;
    private final SignPostBlockTile tile;
    private static final int MAXLINES = 2;
    private final String[] cachedLines;

    private ModelPart signModel;

    private SignPostScreen(SignPostBlockTile teSign) {
        super(Component.translatable("sign.edit"));
        this.tile = teSign;
        this.cachedLines = IntStream.range(0, MAXLINES)
                .mapToObj(l->teSign.getTextHolder().getMessage(l, Minecraft.getInstance().isTextFilteringEnabled()))
                .map(Component::getString).toArray(String[]::new);

        editLine = !this.tile.getSignUp().active() ? 1 : 0;
    }

    public static void open(SignPostBlockTile teSign) {
        Minecraft.getInstance().setScreen(new SignPostScreen(teSign));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        this.textInputUtil.charTyped(codePoint);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (hasBothSignsActive()) {
            this.scrollText((int) delta);
            return true;
        }
        return false;
    }

    private boolean hasBothSignsActive() {
        return this.tile.getSignUp().active() && this.tile.getSignDown().active();
    }

    public void scrollText(int amount) {
        this.editLine = Math.floorMod(this.editLine - amount, MAXLINES);
        this.textInputUtil.setCursorToEnd();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (hasBothSignsActive()) {
            // up arrow
            if (keyCode == 265) {
                this.scrollText(1);
                return true;
            }
            // down arrow, enter
            else if (keyCode == 264 || keyCode == 257 || keyCode == 335) {
                this.scrollText(-1);
                return true;
            }
        }
        return this.textInputUtil.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        ++this.updateCounter;
        if (!this.tile.getType().isValid(this.tile.getBlockState())) {
            this.close();
        }
    }

    @Override
    public void onClose() {
        this.close();
    }

    @Override
    public void removed() {
        // send new text to the server
        NetworkHandler.CHANNEL.sendToServer(new ServerBoundSetTextHolderPacket(this.tile.getBlockPos(), this.tile.getTextHolder()));
    }

    private void close() {
        this.tile.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.close())
                .bounds(this.width / 2 - 100, this.height / 4 + 120, 200, 20).build());

        this.textInputUtil = new TextFieldHelper(() -> this.cachedLines[this.editLine], (s) -> {
            this.cachedLines[this.editLine] = s;
            this.tile.getTextHolder().setMessage(this.editLine, Component.literal(s));
        }, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), (s) -> this.minecraft.font.width(s) <= 90);

        this.signModel = this.minecraft.getEntityModels().bakeLayer(ClientRegistry.SIGN_POST_MODEL);
    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Lighting.setupForFlatItems(); //TODO why this here ??
        this.renderBackground(graphics);

        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics. drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);

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
            var properties = tile.getTextHolder().getGUIRenderTextProperties();

            int cursorPos = this.textInputUtil.getCursorPos();
            int selectionPos = this.textInputUtil.getSelectionPos();

            if (signUp.active()) {
                TextUtil.renderGuiLine(properties, this.cachedLines[0], font, graphics, bufferSource,
                        cursorPos, selectionPos, this.editLine == 0, blink, -10);
            }
            if (signDown.active()) {
                poseStack.translate(-3 * o[1], 0, 0);
                TextUtil.renderGuiLine(properties, this.cachedLines[1], font, graphics, bufferSource,
                        cursorPos, selectionPos, this.editLine == 1, blink, 48 - 10);
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
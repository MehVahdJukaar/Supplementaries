package net.mehvahdjukaar.supplementaries.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.client.TextUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetTextHolderPacket;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.framedblocks.FramedSignPost;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.client.Minecraft;
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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.stream.IntStream;

public class SignPostGui extends Screen {
    private TextFieldHelper textInputUtil;
    /**
     * The index of the line that is being edited.
     */
    private int editLine;
    //for ticking cursor
    private int updateCounter;
    private final SignPostBlockTile tileSign;
    private static final int MAXLINES = 2;
    private final String[] cachedLines;

    private ModelPart signModel;

    private SignPostGui(SignPostBlockTile teSign) {
        super(new TranslatableComponent("sign.edit"));
        this.tileSign = teSign;
        this.cachedLines = IntStream.range(0, MAXLINES).mapToObj(teSign.textHolder::getLine).map(Component::getString).toArray(String[]::new);

        editLine = !this.tileSign.up ? 1 : 0;
    }

    public static void open(SignPostBlockTile teSign){
        Minecraft.getInstance().setScreen(new SignPostGui(teSign));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        this.textInputUtil.charTyped(codePoint);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.tileSign.up && this.tileSign.down) {
            this.scrollText((int) delta);
            return true;
        }
        return false;
    }

    public void scrollText(int amount) {
        this.editLine = Math.floorMod(this.editLine - amount, MAXLINES);
        this.textInputUtil.setCursorToEnd();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (this.tileSign.up && this.tileSign.down) {
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
        if (!this.tileSign.getType().isValid(this.tileSign.getBlockState())) {
            this.close();
        }
    }

    @Override
    public void onClose() {
        this.close();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        // send new text to the server
        NetworkHandler.INSTANCE.sendToServer(new ServerBoundSetTextHolderPacket(this.tileSign.getBlockPos(), this.tileSign.getTextHolder()));
        //this.tileSign.textHolder.setEditable(true);
    }

    private void close() {
        this.tileSign.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, (p_238847_1_) -> this.close()));
        //this.tileSign.textHolder.setEditable(false);
        this.textInputUtil = new TextFieldHelper(() -> this.cachedLines[this.editLine], (s) -> {
            this.cachedLines[this.editLine] = s;
            this.tileSign.textHolder.setLine(this.editLine, new TextComponent(s));
        }, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), (p_238848_1_) -> this.minecraft.font.width(p_238848_1_) <= 90);

        this.signModel = this.minecraft.getEntityModels().bakeLayer(ClientRegistry.SIGN_POST_MODEL);
    }


    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Lighting.setupForFlatItems();
        this.renderBackground(poseStack);

        super.render(poseStack, mouseX, mouseY, partialTicks);

        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 40, 16777215);

        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        poseStack.pushPose();
        poseStack.translate(this.width / 2d, 0.0D, 50.0D);
        poseStack.scale(93.75F, -93.75F, 93.75F);
        poseStack.translate(0.0D, -1.3125D, 0.0D);
        // renders sign
        poseStack.pushPose();

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        boolean leftUp = tileSign.leftUp;
        boolean leftDown = tileSign.leftDown;

        int[] o = new int[2];
        o[0] = leftUp ? 1 : -1;
        o[1] = leftDown ? 1 : -1;

        //render signs

        boolean blink = this.updateCounter / 6 % 2 == 0;

        if (this.tileSign.up) {

            poseStack.pushPose();
            if (!leftUp) {
                poseStack.mulPose(RotHlpr.YN180);
                poseStack.translate(0, 0, -0.3125);
            }
            poseStack.scale(1, -1, -1);
            Material material = ClientRegistry.SIGN_POSTS_MATERIALS.get(this.tileSign.woodTypeUp);
            VertexConsumer builder = material.buffer(bufferSource, RenderType::entitySolid);

            this.signModel.render(poseStack, builder, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
        }
        if (this.tileSign.down) {

            poseStack.pushPose();
            if (!leftDown) {
                poseStack.mulPose(RotHlpr.YN180);
                poseStack.translate(0, 0, -0.3125);
            }
            poseStack.translate(0, -0.5, 0);
            poseStack.scale(1, -1, -1);
            Material material = ClientRegistry.SIGN_POSTS_MATERIALS.get(this.tileSign.woodTypeDown);
            VertexConsumer builder = material.buffer(bufferSource, RenderType::entitySolid);

            this.signModel.render(poseStack, builder, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        //render fence
        poseStack.translate(-0.5, -0.5, -0.5);
        BlockState fence = this.tileSign.mimic;
        if (CompatHandler.framedblocks && tileSign.framed) fence = FramedSignPost.framedFence;
        if (fence != null) {
            blockRenderer.renderSingleBlock(fence, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        }
        poseStack.popPose();

        //renders text

        if (this.tileSign.up || this.tileSign.down) {
            poseStack.translate(-3 * 0.010416667F * o[0], 0.21875, 0.1875 + 0.005);
            poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
            TextUtil.RenderTextProperties properties = new TextUtil.RenderTextProperties(tileSign.textHolder, LightTexture.FULL_BRIGHT, () -> true);

            int cursorPos = this.textInputUtil.getCursorPos();
            int selectionPos = this.textInputUtil.getSelectionPos();

            if (this.tileSign.up) {
                TextUtil.renderGuiLine(properties, this.cachedLines[0], font, poseStack, bufferSource,
                        cursorPos, selectionPos, this.editLine == 0, blink, -10);
            }
            if (this.tileSign.down) {
                poseStack.translate(-3 * o[1], 0, 0);
                TextUtil.renderGuiLine(properties, this.cachedLines[1], font, poseStack, bufferSource,
                        cursorPos, selectionPos, this.editLine == 1, blink, 48 - 10);
            }
        }

        poseStack.popPose();
        Lighting.setupFor3DItems();
    }
}
package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.IdentityHashMap;
import java.util.Map;


public class SignPostBlockTileRenderer implements BlockEntityRenderer<SignPostBlockTile> {
    public static final Map<WoodType, BakedModel> MODELS = new IdentityHashMap<>();
    private static ModelBlockRenderer renderer;

    private final Camera camera;
    private final Font font;

    public SignPostBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        this.font = context.getFont();
        ModelManager manager = Minecraft.getInstance().getModelManager();
        MODELS.clear();
        for (var e : ClientRegistry.SIGN_POST_MODELS.get().entrySet()) {
            MODELS.put(e.getKey(), ClientHelper.getModel(manager, e.getValue()));
        }
        renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();

    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public void render(SignPostBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        renderSignsText(tile, poseStack, bufferIn, combinedLightIn, combinedOverlayIn);

    }

    private void renderSignsText(SignPostBlockTile tile, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        BlockPos pos = tile.getBlockPos();
        Vec3 cameraPos = camera.getPosition();

        //don't render signs from far away
        LOD lod = new LOD(cameraPos, pos);

        if (!lod.isNear()) return;

        var signUp = tile.getSignUp();
        var signDown = tile.getSignDown();

        boolean up = signUp.active();
        boolean down = signDown.active();
        //render signs
        if (up || down) {

            float relAngle = LOD.getRelativeAngle(cameraPos, pos);

            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);

            if (up) {
                if (LOD.isOutOfFocus(relAngle, signUp.yaw() + 90, 2)) {
                    var v = new Vector3f(1,0,0);
                    v.rotateY(signUp.yaw() * Mth.DEG_TO_RAD);
                    var textProperties = tile.getTextHolder(0).computeRenderProperties(combinedLightIn, v, lod::isVeryNear);

                    renderSignText(tile, poseStack, bufferIn, signUp, textProperties, 0);
                }
            }

            if (down) {
                if (LOD.isOutOfFocus(relAngle, signDown.yaw() + 90, 2)) {

                    Vector3f normalVector = new Vector3f(1,0,0);
                    normalVector.rotateY(signUp.yaw() * Mth.DEG_TO_RAD);
                    var textProperties = tile.getTextHolder(1).computeRenderProperties(combinedLightIn, normalVector, lod::isVeryNear);

                    poseStack.translate(0, -0.5, 0);
                    renderSignText(tile, poseStack, bufferIn, signDown, textProperties, 1);
                }
            }
            poseStack.popPose();
        }
    }

    private void renderSignText(SignPostBlockTile tile,
                                PoseStack matrixStackIn, MultiBufferSource bufferIn,
                                SignPostBlockTile.Sign sign,
                                TextUtil.RenderProperties textProperties, int line) {

        matrixStackIn.pushPose();
        boolean left = sign.left();
        int o = left ? 1 : -1;

        matrixStackIn.mulPose(Axis.YP.rotationDegrees(sign.yaw() - 90));

        if (tile.isSlim()) matrixStackIn.translate(0, 0, -1 / 16f);


        matrixStackIn.translate(-0.03125 * o, 0.28125, 0.1875 + 0.005);
        matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);

        TextUtil.renderLine(tile.getTextHolder(line).getRenderMessages(0, font), font,
                -4, matrixStackIn, bufferIn, textProperties);
        matrixStackIn.popPose();

    }


    public static void renderSigns(PoseStack poseStack, VertexConsumer builder, int combinedLightIn, int combinedOverlayIn,
                                   SignPostBlockTile.Sign signUp, SignPostBlockTile.Sign signDown, boolean slim) {

        boolean up = signUp.active();
        boolean down = signDown.active();
        //render signs
        if (up || down) {
            poseStack.pushPose();

            if (down) {
                renderSign(poseStack, builder, combinedLightIn, combinedOverlayIn, signDown, slim);
            }

            if (up) {
                poseStack.translate(0, 0.5, 0);
                renderSign(poseStack, builder, combinedLightIn, combinedOverlayIn, signUp, slim);
            }

            poseStack.popPose();
        }
    }

    public static void renderSign(
            PoseStack posestack, VertexConsumer builder,
            int light, int overlay,
            SignPostBlockTile.Sign sign, boolean slim) {
        posestack.pushPose();

        boolean left = sign.left();
        posestack.translate(0.5, 0.5, 0.5);
        posestack.mulPose(Axis.YP.rotationDegrees(sign.yaw() - 90));

        if (slim) posestack.translate(0, 0, -1 / 16f);

        //sign block
        if (!left) {
            posestack.mulPose(RotHlpr.YN180);
            posestack.translate(0, 0, -0.3125);
        }
        posestack.translate(-0.5, -0.5, -0.25);
        renderer.renderModel(posestack.last(),
                builder,
                null,
                MODELS.get(sign.woodType()),
                1.0F, 1.0F, 1.0F,
                light, overlay);

        posestack.popPose();
    }
}

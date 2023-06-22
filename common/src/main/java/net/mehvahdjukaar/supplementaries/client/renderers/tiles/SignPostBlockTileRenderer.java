package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;


public class SignPostBlockTileRenderer implements BlockEntityRenderer<SignPostBlockTile> {
    private final Camera camera;
    private final Font font;
    public final ModelPart signModel;

    public static LayerDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("sign", CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-12.0F, -5.0F, -3.0F, 2.0F, 1.0F, 1.0F)
                        .texOffs(0, 0)
                        .addBox(-8.0F, -7.0F, -3.0F, 16.0F, 5.0F, 1.0F)
                        .texOffs(0, 6)
                        .addBox(-10.0F, -6.0F, -3.0F, 2.0F, 3.0F, 1.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 16);
    }

    public SignPostBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(ClientRegistry.SIGN_POST_MODEL);
        this.signModel = model.getChild("sign");
        this.camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        this.font = context.getFont();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(SignPostBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        BlockPos pos = tile.getBlockPos();
        Vec3 cameraPos = camera.getPosition();

        //don't render signs from far away
        LOD lod = new LOD(cameraPos, pos);

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
                var v = new Vector3f();
                v.rotateY(signUp.yaw() * Mth.DEG_TO_RAD);
                var textProperties = tile.getTextHolder(0)
                        .computeRenderProperties(combinedLightIn, v, lod::isVeryNear);

                poseStack.pushPose();
                renderSign(tile, poseStack, bufferIn, combinedLightIn, combinedOverlayIn,
                        lod, signUp, relAngle, textProperties, 0);
                poseStack.popPose();
            }

            if (down) {
                var v = new Vector3f();
                v.rotateY(signUp.yaw()* Mth.DEG_TO_RAD);
                var textProperties = tile.getTextHolder(1)
                        .computeRenderProperties(combinedLightIn,
                                v, lod::isVeryNear);

                poseStack.pushPose();
                poseStack.translate(0, -0.5, 0);
                renderSign(tile, poseStack, bufferIn, combinedLightIn, combinedOverlayIn,
                        lod, signDown, relAngle, textProperties, 1);
                poseStack.popPose();
            }
            poseStack.popPose();
        }

    }

    private void renderSign(SignPostBlockTile tile,
                            PoseStack matrixStackIn, MultiBufferSource bufferIn,
                            int combinedLightIn, int combinedOverlayIn, LOD lod,
                            SignPostBlockTile.Sign sign, float relAngle,
                            TextUtil.RenderProperties textProperties, int line) {

        boolean left = sign.left();
        int o = left ? 1 : -1;

        matrixStackIn.mulPose(Axis.YP.rotationDegrees(sign.yaw() - 90));

        if (tile.isSlim()) matrixStackIn.translate(0, 0, -1 / 16f);

        //sign block
        matrixStackIn.pushPose();

        if (!left) {
            matrixStackIn.mulPose(RotHlpr.YN180);
            matrixStackIn.translate(0, 0, -0.3125);
        }

        matrixStackIn.scale(1, -1, -1);
        Material material = ModMaterials.SIGN_POSTS_MATERIALS.get().get(sign.woodType());
        VertexConsumer builder = material.buffer(bufferIn, RenderType::entitySolid);
        signModel.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.popPose();

        if (lod.isNear() && LOD.isOutOfFocus(relAngle, sign.yaw() + 90, 2)) {

            matrixStackIn.translate(-0.03125 * o, 0.28125, 0.1875 + 0.005);
            matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);

            TextUtil.renderLine(tile.getTextHolder(line).getRenderMessages(0, font), font,
                    -4, matrixStackIn, bufferIn, textProperties);

        }
    }
}

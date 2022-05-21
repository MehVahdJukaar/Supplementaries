package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.client.TextUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.LOD;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
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
import net.minecraft.world.phys.Vec3;


public class SignPostBlockTileRenderer implements BlockEntityRenderer<SignPostBlockTile> {
    private static final int LINE_MAX_WIDTH = 90;
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
    public void render(SignPostBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        BlockPos pos = tile.getBlockPos();
        Vec3 cameraPos = camera.getPosition();

        //don't render signs from far away
        LOD lod = new LOD(cameraPos, pos);

        boolean up = tile.up;
        boolean down = tile.down;
        //render signs
        if (up || down) {

            float relAngle = LOD.getRelativeAngle(cameraPos, pos);

            TextUtil.RenderTextProperties textProperties = new TextUtil.RenderTextProperties(tile.textHolder, combinedLightIn, lod::isVeryNear);

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);

            if (up) {
                matrixStackIn.pushPose();

                boolean left = tile.leftUp;
                int o = left ? 1 : -1;

                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(tile.yawUp - 90));
                //matrixStackIn.rotate(Const.YN90);

                if(tile.isSlim) matrixStackIn.translate(0,0,-1/16f);

                //sign block
                matrixStackIn.pushPose();

                if (!left) {
                    matrixStackIn.mulPose(RotHlpr.YN180);
                    matrixStackIn.translate(0, 0, -0.3125);
                }

                matrixStackIn.scale(1, -1, -1);
                Material material = ClientRegistry.SIGN_POSTS_MATERIALS.get(tile.woodTypeUp);
                //sanity check. can happen when log detection fails across versions
                if(material != null) {
                    VertexConsumer builder = material.buffer(bufferIn, RenderType::entitySolid);
                    signModel.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
                }

                matrixStackIn.popPose();

                //culling
                if (lod.isNear() && LOD.isOutOfFocus(relAngle, tile.yawUp + 90, 2)) {

                    //text up
                    matrixStackIn.translate(-0.03125 * o, 0.28125, 0.1875 + 0.005);
                    matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);

                    TextUtil.renderLine(tile.textHolder, 0, font, LINE_MAX_WIDTH, -4, matrixStackIn, bufferIn, textProperties);
                }

                matrixStackIn.popPose();
            }

            if (down) {
                matrixStackIn.pushPose();

                boolean left = tile.leftDown;
                int o = left ? 1 : -1;

                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(tile.yawDown - 90));
                matrixStackIn.translate(0, -0.5, 0);

                if(tile.isSlim) matrixStackIn.translate(0,0,-1/16f);

                //sign block
                matrixStackIn.pushPose();

                if (!left) {
                    matrixStackIn.mulPose(RotHlpr.YN180);
                    matrixStackIn.translate(0, 0, -0.3125);
                }

                matrixStackIn.scale(1, -1, -1);
                Material material = ClientRegistry.SIGN_POSTS_MATERIALS.get(tile.woodTypeDown);
                VertexConsumer builder = material.buffer(bufferIn, RenderType::entitySolid);
                signModel.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

                matrixStackIn.popPose();

                //inverted huh
                if (lod.isNear() && LOD.isOutOfFocus(relAngle, tile.yawDown + 90, 2)) {

                    //text down
                    matrixStackIn.translate(-0.03125 * o, 0.28125, 0.1875 + 0.005);
                    matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);

                    TextUtil.renderLine(tile.textHolder, 1, font, LINE_MAX_WIDTH, -4, matrixStackIn, bufferIn, textProperties);
                }

                matrixStackIn.popPose();
            }
            matrixStackIn.popPose();
        }

    }
}

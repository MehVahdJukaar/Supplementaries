package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.GlobeTextureManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;


public class GlobeBlockTileRenderer implements BlockEntityRenderer<GlobeBlockTile> {

    private final Map<GlobeBlockTile.GlobeModel, ModelPart> models = new HashMap<>();

    public static LayerDefinition createBaseMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("globe", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -28.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0, 24, 0));

        return LayerDefinition.create(mesh, 32, 16);
    }

    public static LayerDefinition createSpecialMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("flat", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -28.0F, -4.0F, 8.0F, 4.0F, 8.0F)
                        .texOffs(0, 13)
                        .addBox(-4.0F, -24.0F, -4.0F, 8.0F, 2.0F, 8.0F)
                        .texOffs(4, 23)
                        .addBox(-3.0F, -22.0F, -3.0F, 6.0F, 1.0F, 6.0F)
                        .texOffs(8, 24)
                        .addBox(-2.0F, -21.0F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(0, 24, 0));

        root.addOrReplaceChild("sheared", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -28.0F, -4.0F, 8.0F, 8.0F, 4.0F)
                        .texOffs(0, 12)
                        .addBox(0.0F, -28.0F, 0.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(0, 24, 0));

        PartDefinition snow = root.addOrReplaceChild("snow", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -28.0F, -4.0F, 8.0F, 5.0F, 8.0F)
                        .texOffs(0, 14)
                        .addBox(-4.0F, -23.0F, -4.0F, 8.0F, 1.0F, 8.0F)
                        .texOffs(4, 16)
                        .addBox(-3.0F, -22.0F, -3.0F, 6.0F, 1.0F, 6.0F)
                        .texOffs(0, 17)
                        .addBox(-2.0F, -24.0F, -2.0F, 4.0F, 1.0F, 4.0F)
                        .texOffs(0, 28)
                        .addBox(-1.0F, -25.975F, -1.0F, 2.0F, 2.0F, 2.0F)
                        .texOffs(12, 20)
                        .addBox(-1.0F, -21.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0, 24, 0));
        snow.addOrReplaceChild("roof_l", CubeListBuilder.create()
                        .texOffs(11, 27)
                        .addBox(0.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0, -25.9F, 0, 0, 0, 0.7854F));
        snow.addOrReplaceChild("roof_r", CubeListBuilder.create()
                        .texOffs(0, 27)
                        .addBox(-2.0F, -1.0F, -1.0F, 3.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0, -25.9F, 0, 0, 0, -0.7854F));


        return LayerDefinition.create(mesh, 32, 32);
    }

    public GlobeBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(ClientRegistry.GLOBE_BASE_MODEL);
        models.put(GlobeBlockTile.GlobeModel.GLOBE, model.getChild("globe"));
        ModelPart special = context.bakeLayer(ClientRegistry.GLOBE_SPECIAL_MODEL);
        models.put(GlobeBlockTile.GlobeModel.FLAT, special.getChild("flat"));
        models.put(GlobeBlockTile.GlobeModel.SNOW, special.getChild("snow"));
        models.put(GlobeBlockTile.GlobeModel.SHEARED, special.getChild("sheared"));
        ClientRegistry.GLOBE_RENDERER_INSTANCE = this;
    }

    @Override
    public void render(GlobeBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(RotHlpr.rot(tile.getDirection()));
        matrixStackIn.mulPose(RotHlpr.XN90);
        matrixStackIn.translate(0, +0.0625, 0);
        matrixStackIn.mulPose(RotHlpr.XN22);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, tile.prevYaw + tile.face, tile.yaw + tile.face)));


        this.renderGlobe(tile.renderData, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, tile.isSepia(), tile.getLevel());

        matrixStackIn.popPose();
    }

    public void renderGlobe(Pair<GlobeBlockTile.GlobeModel, ResourceLocation> data, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, boolean isSepia, Level level) {
        if (data == null) return;
        poseStack.pushPose();
        poseStack.mulPose(RotHlpr.X180);
        ResourceLocation texture = ClientConfigs.block.GLOBE_RANDOM.get() ? data.getSecond() : GlobeBlockTile.GlobeType.EARTH.texture;

        ModelPart model = this.models.get(data.getFirst());

        VertexConsumer builder;
        if (texture == null) {
            builder = buffer.getBuffer(GlobeTextureManager.getRenderType(level, isSepia));
        } else {
            builder = buffer.getBuffer(RenderType.entityCutout(texture));
        }

        model.render(poseStack, builder, light, overlay, 1, 1, 1, 1);
        poseStack.popPose();
    }

}
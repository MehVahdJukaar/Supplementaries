package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.renderers.NoiseRenderType;
import net.mehvahdjukaar.supplementaries.client.renderers.SphereRenderType;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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

import java.util.EnumMap;
import java.util.Map;


public class GlobeBlockTileRenderer implements BlockEntityRenderer<GlobeBlockTile> {

    private final Map<GlobeManager.Model, ModelPart> models = new EnumMap<>(GlobeManager.Model.class);
    private final boolean noise;

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

    public static GlobeBlockTileRenderer INSTANCE = null;

    public GlobeBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(ClientRegistry.GLOBE_BASE_MODEL);
        models.put(GlobeManager.Model.GLOBE, model.getChild("globe"));
        ModelPart special = context.bakeLayer(ClientRegistry.GLOBE_SPECIAL_MODEL);
        models.put(GlobeManager.Model.FLAT, special.getChild("flat"));
        models.put(GlobeManager.Model.SNOW, special.getChild("snow"));
        models.put(GlobeManager.Model.SHEARED, special.getChild("sheared"));
        INSTANCE = this;
        this.noise = MiscUtils.FESTIVITY.isAprilsFool();

    }

    @Override
    public void render(GlobeBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(RotHlpr.rot(tile.getDirection()));
        matrixStackIn.translate(0, 0.0625, 0);
        matrixStackIn.mulPose(RotHlpr.X22);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(-tile.getRotation(partialTicks)));


        this.renderGlobe(tile.getRenderData(), matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn,
                tile.isSepia(), tile.getLevel());

        matrixStackIn.popPose();
    }

    public void renderGlobe(Pair<GlobeManager.Model, ResourceLocation> data, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, boolean isSepia, Level level) {
        if (data == null) return;
        poseStack.pushPose();
        poseStack.mulPose(RotHlpr.X180);
        ResourceLocation texture = ClientConfigs.Blocks.GLOBE_RANDOM.get() ? data.getSecond() :
                GlobeManager.Type.EARTH.getTexture();

        ModelPart model = this.models.get(data.getFirst());

        VertexConsumer builder;
        if (texture == null) {
            if(true){
                builder = buffer.getBuffer(SphereRenderType.STATIC_NOISE.apply(ModTextures.GLOBE_TEXTURE));
                int lu = VertexUtil.lightU(light);
                int lv = VertexUtil.lightV(light);
                poseStack.last().pose().setRotationYXZ(0,0,0);
                Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
                poseStack.mulPose(cam.rotation());
             //   Uniform intensity = ClientRegistry.SPHERE_SHADER.get().getUniform("CameraPos");
            //    intensity.set(cam.getPosition().toVector3f());
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
float radius = 0.25f;
                VertexUtil.addQuad(builder, poseStack, -radius, -radius, radius, radius, lu, lv);
                poseStack.popPose();
                return;
            }
            if (noise) {
                double si = Math.sin(System.currentTimeMillis() / 8000.0) * 30;
                float v = (float) Mth.clamp(si, -0.5, 0.5);
                float c = (float) Mth.clamp(si, -2, 2);
                Uniform intensity = ClientRegistry.NOISE_SHADER.get().getUniform("Intensity");
                if (intensity != null) intensity.set(Mth.cos(Mth.PI * c / 4f));
                poseStack.scale(v + 0.5f + 0.01f, 1, 1);
                builder = buffer.getBuffer(NoiseRenderType.STATIC_NOISE.apply(ModTextures.GLOBE_TEXTURE));
            } else {
                RenderType renderType = GlobeManager.getRenderType(level, isSepia);
                builder = buffer.getBuffer(renderType);
            }
        } else {
            builder = buffer.getBuffer(RenderType.entityTranslucentCull(texture));
        }

        model.render(poseStack, builder, light, overlay, -1);
        poseStack.popPose();
    }

}
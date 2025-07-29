package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.GlobeRenderData;
import net.mehvahdjukaar.supplementaries.client.renderers.NoiseRenderType;
import net.mehvahdjukaar.supplementaries.client.renderers.SphereRenderType;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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
    public void render(GlobeBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(RotHlpr.rot(tile.getDirection()));
        poseStack.translate(0, 0.0625, 0);
        poseStack.mulPose(RotHlpr.X22);
        poseStack.mulPose(Axis.YP.rotationDegrees(90 - tile.getRotation(partialTicks)));

        PoseStack test = new PoseStack();
        test.mulPose(RotHlpr.X22);

        this.renderGlobe(tile.getRenderData(), poseStack, bufferIn, combinedLightIn, combinedOverlayIn,
                tile.isSepia(), tile.getLevel());

        poseStack.popPose();
    }

    public void renderGlobe(GlobeRenderData data, PoseStack poseStack, MultiBufferSource buffer,
                            int light, int overlay, boolean isSepia, Level level) {
        poseStack.pushPose();
        poseStack.mulPose(RotHlpr.X180);

        GlobeManager.Model globeModel = data.getModel(isSepia);

        VertexConsumer builder;

        if (globeModel == GlobeManager.Model.ROUND || (noise && isSepia)) {
            poseStack.mulPose(RotHlpr.Z180);
            builder = buffer.getBuffer(SphereRenderType.RENDER_TYPE.apply(data.getTexture(isSepia)));
            try {
                var mc = Minecraft.getInstance();
                ClientRegistry.SPHERE_SHADER.get().getUniform("ScreenSize")
                        .set(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            } catch (Exception ignored) {
            }
            addSphereQuad(poseStack, builder, 0.8f, light);
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
            builder = buffer.getBuffer(NoiseRenderType.RENDER_TYPE.apply(data.getTexture(isSepia)));
        } else if (globeModel == GlobeManager.Model.FLAT) {
            poseStack.scale(0.01f, 1, 1);
            globeModel = GlobeManager.DEFAULT_DATA.getModel(isSepia);
            RenderType renderType = RenderUtil.getEntityCutoutMipmapRenderType(GlobeManager.DEFAULT_DATA.getTexture(isSepia));
            builder = buffer.getBuffer(renderType);
        } else {
            RenderType renderType = RenderUtil.getEntityCutoutMipmapRenderType(data.getTexture(isSepia));
            builder = buffer.getBuffer(renderType);
        }
        ModelPart model = this.models.get(globeModel);

        model.render(poseStack, builder, light, overlay, -1);
        poseStack.popPose();
    }

    private static void addSphereQuad(PoseStack stack, VertexConsumer consumer, float radius, int light) {
        Matrix4f matrix = stack.last().pose();

        Vector3f sphereRot = matrix.getEulerAnglesXYZ(new Vector3f());
        var spherePos = matrix.transformPosition(new Vector3f(0, 0, 0));

        matrix.setRotationYXZ(0, 0, 0);

        Minecraft mc = Minecraft.getInstance();
        Camera cam = mc.gameRenderer.getMainCamera();

        matrix.rotate(cam.rotation());
        matrix.translate(0, 0, 0.08f);

        Vector3f v1 = matrix.transformPosition(new Vector3f(radius, radius, 0));
        Vector3f centerRel1 = spherePos.sub(v1, new Vector3f());
        consumer.addVertex(v1.x, v1.y, v1.z)
                .setNormal(centerRel1.x, centerRel1.y, centerRel1.z) //sphere center
                .setColor(-1)
                .setLight(light);
        addExtraVec3f(consumer, sphereRot);

        Vector3f v2 = matrix.transformPosition(new Vector3f(-radius, radius, 0));
        Vector3f centerRel2 = spherePos.sub(v2, new Vector3f());
        consumer.addVertex(v2.x, v2.y, v2.z)
                .setNormal(centerRel2.x, centerRel2.y, centerRel2.z) //sphere center
                .setColor(-1)
                .setLight(light);
        addExtraVec3f(consumer, sphereRot);

        Vector3f v3 = matrix.transformPosition(new Vector3f(-radius, -radius, 0));
        Vector3f centerRel3 = spherePos.sub(v3, new Vector3f());
        consumer.addVertex(v3.x, v3.y, v3.z)
                .setNormal(centerRel3.x, centerRel3.y, centerRel3.z) //sphere center
                .setColor(-1)
                .setLight(light);
        addExtraVec3f(consumer, sphereRot);

        Vector3f v4 = matrix.transformPosition(new Vector3f(radius, -radius, 0));
        Vector3f centerRel4 = spherePos.sub(v4, new Vector3f());
        consumer.addVertex(v4.x, v4.y, v4.z)
                .setNormal(centerRel4.x, centerRel4.y, centerRel4.z) //sphere center
                .setColor(-1)
                .setLight(light);
        addExtraVec3f(consumer, sphereRot);

    }

    private static void addExtraVec3f(VertexConsumer consumer, Vector3f vec) {
        consumer.setUv(vec.x, vec.y);
        var shorts = floatToTwoShorts(vec.z);
        consumer.setUv1(shorts[0], shorts[1]);
    }

    public static short[] floatToTwoShorts(float value) {
        int bits = Float.floatToIntBits(value); // Step 1: convert float to raw int bits
        short high = (short) ((bits >>> 16) & 0xFFFF); // Step 2: high 16 bits
        short low = (short) (bits & 0xFFFF);          // Step 3: low 16 bits
        return new short[]{high, low};
    }

}
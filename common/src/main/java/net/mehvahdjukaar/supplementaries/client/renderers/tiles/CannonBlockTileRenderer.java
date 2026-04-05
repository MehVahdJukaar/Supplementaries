package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.math.EntityAngles;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonTrajectoryRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CannonBlockTileRenderer implements BlockEntityRenderer<CannonBlockTile> {

    private final ModelPart head;
    private final ModelPart legs;
    private final ModelPart pivot;
    private final ModelPart model;

    public CannonBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(ClientRegistry.CANNON_MODEL);
        this.legs = model.getChild("legs");
        this.pivot = legs.getChild("head_pivot");
        this.head = pivot.getChild("head");
        this.model = model;
    }

    @ForgeOverride
    public AABB getRenderBoundingBox(BlockEntity tile) {
        return new AABB(tile.getBlockPos()).inflate(0.2);
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public boolean shouldRenderOffScreen(CannonBlockTile blockEntity) {
        return true;
    }

    @Override
    public void render(CannonBlockTile tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {


        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        CannonTrajectoryRenderer.render(tile, poseStack, bufferSource, packedLight, packedOverlay, partialTick);

        renderCannonModel(this, tile, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();

    }

    public static void renderCannonModel(CannonBlockTileRenderer renderer,
                                         CannonBlockTile tile, float partialTick, PoseStack poseStack,
                                         MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        poseStack.pushPose();

        // Base rotation (block orientation)
        Quaternionf cannonBaseRot =
                tile.getBlockState()
                        .getValue(CannonBlock.FACING)
                        .getOpposite()
                        .getRotation();

        // Head local rot
        Quaternionf cannonHeadRot = tile.getLocalOrientation(partialTick);

        // Debug (world space)
        if (PlatHelper.isDev()) {
            poseStack.pushPose();
            poseStack.translate(0, 0.02, 0);
            renderDebug(poseStack, bufferSource, cannonHeadRot, 0xff00ffff);
            poseStack.popPose();
        }

        // Move into base space
        poseStack.mulPose(cannonBaseRot);

        // Compute head rotation relative to base
        Quaternionf localRot = new Quaternionf(cannonBaseRot)
                .invert()
                .mul(cannonHeadRot);

        // Debug (should overlap with world debug)
        if (PlatHelper.isDev()) {
            renderDebug(poseStack, bufferSource, localRot, 0xffff00ff);
        }

        // Canonical forward (IMPORTANT: must match your model!)
        Vector3f forward = new Vector3f(0, 0, -1);

        // Rotate into base-local space
        localRot.transform(forward);

        EntityAngles angles = EntityAngles.fromQuaternion(localRot);

        float yaw = angles.yawRad();
        float pitch = angles.pitchRad();

        renderer.legs.yRot = -yaw;

        // negative depends on your model convention (likely correct)
        renderer.pivot.xRot = pitch;

        // roll is physically impossible → force zero
        renderer.pivot.zRot = 0;

        // fixed adjustment if your model needs it
        renderer.head.yRot = Mth.PI;
        // animation
        float cooldownCounter = tile.getCooldownAnimation(partialTick);
        float fireCounter = tile.getFiringAnimation(partialTick);

        //write equation of sawtooth wave with same period as that sine wave
        float squish = MthUtils.asymmetricTriangleWave(1 - cooldownCounter, 0.01f, 0.15f) * 0.2f;

        float wobble = Mth.sin(fireCounter * 20f * (float) Math.PI) * 0.005f;
        float scale = wobble + 1f + squish * 0.7f;
        renderer.head.xScale = scale;
        renderer.head.yScale = scale;

        renderer.head.zScale = 1 - squish;
        renderer.head.z = 1 - squish * 5.675f;

        VertexConsumer builder = ModMaterials.CANNON_MATERIAL.buffer(bufferSource, RenderType::entityCutout);
        renderer.model.render(poseStack, builder, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderDebug(PoseStack poseStack, MultiBufferSource bufferSource, Quaternionf quat, int color) {
        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());

        Vector3f forward = new Vector3f(0, 0, 2);
        forward.rotate(quat);
        vc.addVertex(pose, 0, 0, 0)
                .setColor(color)
                .setNormal(pose, 0, 1, 0);
        vc.addVertex(pose, forward.x, forward.y, forward.z)
                .setColor(color)
                .setNormal(pose, 0, 1, 0);

        poseStack.popPose();
    }


    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition legs = partdefinition.addOrReplaceChild("legs", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(6.0F, -4.0F, -3.0F, 2.0F, 10.0F, 6.0F)
                        .texOffs(48, 0).addBox(-8.0F, -4.0F, -3.0F, 2.0F, 10.0F, 6.0F),
                PartPose.ZERO);

        PartDefinition head = legs.addOrReplaceChild("head_pivot", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

        head.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 46).addBox(-6.0F, -6.0F, -6.5F, 12.0F, 12.0F, 6.0F)
                        .texOffs(0, 18).addBox(-6.0F, -6.0F, -6.5F, 12.0F, 12.0F, 13.0F, new CubeDeformation(-0.3125F)),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("base", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, 6.0F, -8.0F, 16.0F, 2.0F, 16.0F),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}

package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
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
        Quaternionf rotation = tile.getBlockState().getValue(CannonBlock.FACING).getOpposite().getRotation();

        poseStack.mulPose(rotation);

        VertexConsumer builder = ModMaterials.CANNON_MATERIAL.buffer(bufferSource, RenderType::entityCutout);

        float pitchRad = tile.getPitch(partialTick) * Mth.DEG_TO_RAD;
        float yawRad = tile.getYaw(partialTick) * Mth.DEG_TO_RAD;

        Vector3f forward = new Vector3f(0f, 0, 1);

        forward.rotateX(Mth.PI - pitchRad);

        forward.rotateY(Mth.PI - yawRad);
        forward.rotate(rotation.invert());

        yawRad = (float) Mth.atan2(forward.x, forward.z);

        pitchRad = (float) Mth.atan2(-forward.y, Mth.sqrt(forward.x * forward.x + forward.z * forward.z));
        //float rollRad = (float) Math.atan2(forward.y, forward.z);

        renderer.legs.yRot = yawRad;
        renderer.pivot.xRot = pitchRad;
        renderer.pivot.zRot = 0;


        // animation
        float cooldownCounter = tile.getCooldownAnimation(partialTick);
        float fireCounter = tile.getFiringAnimation(partialTick);

        //write equation of sawtooth wave with same period as that sine wave
        float squish = triangle(1 - cooldownCounter, 0.01f, 0.15f) * 0.2f;

        float wobble = Mth.sin(fireCounter * 20f * (float) Math.PI) * 0.005f;
        float scale = wobble + 1f + squish * 0.7f;
        renderer.head.xScale = scale;
        renderer.head.yScale = scale;

        renderer.head.zScale = 1 - squish;
        renderer.head.z = squish * 5.675f;

        renderer.model.render(poseStack, builder, packedLight, packedOverlay);
        poseStack.popPose();
    }

    public static float triangle(float cooldownCounter, float mid, float end) {
        if (cooldownCounter <= mid) {
            // Calculate the slope for the rising part
            float slope = 1 / mid;
            // Calculate the y-coordinate based on the slope
            return slope * cooldownCounter;
        }
        // Check if cooldownCounter is within the range of mid to end
        else if (cooldownCounter <= end) {
            // Calculate the slope for the falling part
            float slope = -1 / (end - mid);
            // Calculate the y-coordinate based on the slope and offset by 1 to start from 1
            return slope * (cooldownCounter - mid) + 1;
        }
        // If cooldownCounter is greater than f, return 0
        else {
            return 0;
        }
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

        PartDefinition bone = head.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 46).addBox(-6.0F, -6.0F, -6.5F, 12.0F, 12.0F, 6.0F)
                        .texOffs(0, 18).addBox(-6.0F, -6.0F, -6.5F, 12.0F, 12.0F, 13.0F, new CubeDeformation(-0.3125F)),
                PartPose.ZERO);

        PartDefinition base = partdefinition.addOrReplaceChild("vehicle", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, 6.0F, -8.0F, 16.0F, 2.0F, 16.0F),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}

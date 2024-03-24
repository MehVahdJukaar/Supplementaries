package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

    @Override
    public boolean shouldRenderOffScreen(CannonBlockTile blockEntity) {
        return true;
    }

    @Override
    public void render(CannonBlockTile tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {


        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        Quaternionf rotation = tile.getBlockState().getValue(CannonBlock.FACING).getRotation();
        poseStack.mulPose(rotation);

        VertexConsumer builder = ModMaterials.CANNON_MATERIAL.buffer(bufferSource, RenderType::entityCutout);


        long t = System.currentTimeMillis();

        //write equation of sawtooth wave with same period as that sine wave
        float wave = (t % 1000) / 1000f;

        float yawRad = tile.getYaw(partialTick) * Mth.DEG_TO_RAD;
        float absoluteYaw = yawRad;
        float pitchRad = tile.getPitch(partialTick) * Mth.DEG_TO_RAD;

        Vector3f forward = new Vector3f(0f, 0, 1);

        forward.rotateX(Mth.PI - pitchRad);

        forward.rotateY(Mth.PI - yawRad);
        forward.rotate(rotation.invert());

        yawRad = (float) Mth.atan2(forward.x, forward.z);

        pitchRad = (float) Mth.atan2(-forward.y, Mth.sqrt(forward.x * forward.x + forward.z * forward.z));
        float rollRad = (float) Math.atan2(forward.y, forward.z);

        this.legs.yRot = yawRad;
        this.pivot.xRot = pitchRad;
        this.pivot.zRot = 0;

        float scale = Mth.sin((t % 200) / 200f * (float) Math.PI) * 0.01f + 1f + wave * 0.06f;
        head.xScale = scale;
        head.yScale = scale;
        float c = (wave) * 0.1f;
        head.zScale = 1 - c;
        head.z = c * 5.675f;

        model.render(poseStack, builder, packedLight, packedOverlay);


        poseStack.popPose();

        CannonTrajectoryRenderer.render(tile, poseStack, bufferSource, packedLight, packedOverlay, partialTick, absoluteYaw);
    }


    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition legs = partdefinition.addOrReplaceChild("legs", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(6.0F, 4.0F, -3.0F, 2.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 0).addBox(-8.0F, 4.0F, -3.0F, 2.0F, 10.0F, 6.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));

        PartDefinition head = legs.addOrReplaceChild("head_pivot", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition bone = head.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 46).addBox(-6.0F, -6.0F, -6.5F, 12.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 18).addBox(-6.0F, -6.0F, -6.5F, 12.0F, 12.0F, 13.0F, new CubeDeformation(-0.3125F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -2.0F, 0.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.0F, 8, -8.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}

package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Vector3f;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.tiles.ClockBlockTile;
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


public class ClockBlockTileRenderer implements BlockEntityRenderer<ClockBlockTile> {

    public final ModelPart hourHand;
    public final ModelPart minuteHand;

    public static LayerDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("hour", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-0.5F, 0.0F, 0.0F, 1.0F, 5.0F, 0.0F),
                PartPose.offset(0, 24, 0));

        root.addOrReplaceChild("minute", CubeListBuilder.create()
                        .texOffs(2, 0)
                        .addBox(-0.5F, 0.0F, 0.0F, 1.0F, 6.0F, 0.0F),
                PartPose.offset(0, 24, 0));

        return LayerDefinition.create(mesh, 16, 16);
    }

    public ClockBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(ClientRegistry.CLOCK_HANDS_MODEL);
        this.minuteHand = model.getChild("minute");
        this.hourHand = model.getChild("hour");
    }

    @Override
    public void render(ClockBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        VertexConsumer builder = ModMaterials.CLOCK_HAND.buffer(bufferIn, RenderType::entityCutoutNoCull);

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5d, 0.5d, 0.5d);
        matrixStackIn.mulPose(RotHlpr.rot(tile.getDirection()));

        //hours
        matrixStackIn.pushPose();

        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(tile.getRoll(partialTicks)));
        matrixStackIn.translate(0, -1.5, -0.5 + 0.02083333);

        this.hourHand.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 1, 1, 1, 1);

        matrixStackIn.popPose();

        //minutes
        matrixStackIn.pushPose();

        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(tile.getRollS(partialTicks)));
        matrixStackIn.translate(0, -1.5, -0.5 + 0.04166667);

        this.minuteHand.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, 1, 1, 1, 1);

        matrixStackIn.popPose();

        matrixStackIn.popPose();
    }

}
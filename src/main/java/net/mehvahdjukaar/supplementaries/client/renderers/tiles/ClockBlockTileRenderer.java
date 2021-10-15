package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.block.tiles.ClockBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;


public class ClockBlockTileRenderer extends BlockEntityRenderer<ClockBlockTile> {
    public final Material HAND_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, Textures.CLOCK_HAND_TEXTURE);
    public final ModelPart hourHand = new ModelPart(16, 16, 0, 0);
    public final ModelPart minuteHand = new ModelPart(16, 16, 2, 0);


    public ClockBlockTileRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);

        this.hourHand.addBox(-0.5F, 0.0F, 0.0F, 1.0F, 5.0F, 0.0F, 0.0F, false);
        this.hourHand.setPos(0.0F, 24.0F, 0.0F);
        this.minuteHand.addBox(-0.5F, 0.0F, 0.0F, 1.0F, 6.0F, 0.0F, 0.0F, false);
        this.minuteHand.setPos(0.0F, 24.0F, 0.0F);

    }

    @Override

    public void render(ClockBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        VertexConsumer builder = HAND_TEXTURE.buffer(bufferIn, RenderType::entityCutoutNoCull);

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5d, 0.5d, 0.5d);
        matrixStackIn.mulPose(Const.rot(tile.getDirection()));

        matrixStackIn.mulPose(Const.X90);

        //hours
        matrixStackIn.pushPose();

        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(Mth.rotLerp(partialTicks, tile.prevRoll, tile.roll)));
        matrixStackIn.translate(0,-1.5, -0.5+0.02083333);

        this.hourHand.render(matrixStackIn, builder, combinedLightIn,combinedOverlayIn,1,1,1,1);

        matrixStackIn.popPose();

        //minutes
        matrixStackIn.pushPose();

        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(Mth.rotLerp(partialTicks, tile.sPrevRoll, tile.sRoll)));
        matrixStackIn.translate(0,-1.5, -0.5+0.04166667);

        this.minuteHand.render(matrixStackIn, builder, combinedLightIn,combinedOverlayIn,1,1,1,1);

        matrixStackIn.popPose();

        matrixStackIn.popPose();
    }


}
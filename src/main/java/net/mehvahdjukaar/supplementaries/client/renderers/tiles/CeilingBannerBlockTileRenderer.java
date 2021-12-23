package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CeilingBannerBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CeilingBannerBlockTile;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CeilingBannerBlockTileRenderer implements BlockEntityRenderer<CeilingBannerBlockTile> {
    private final ModelPart flag;
    private final ModelPart bar;

    public CeilingBannerBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelpart = context.bakeLayer(ModelLayers.BANNER);
        this.flag = modelpart.getChild("flag");
        this.bar = modelpart.getChild("bar");
    }

    public void render(CeilingBannerBlockTile tile, float p_225616_2_, PoseStack matrixStack, MultiBufferSource p_225616_4_, int p_225616_5_, int p_225616_6_) {
        List<Pair<BannerPattern, DyeColor>> list = tile.getPatterns();
        if (list != null) {

            matrixStack.pushPose();
            long i;

            i = tile.getLevel().getGameTime();
            BlockState blockstate = tile.getBlockState();

            if (blockstate.getValue(CeilingBannerBlock.ATTACHED)) {
                matrixStack.translate(0, 0.625, 0);
            }
            matrixStack.translate(0.5D, -0.3125 - 0.0208333333333, 0.5D); //1/32 * 2/3

            float f3 = -blockstate.getValue(CeilingBannerBlock.FACING).toYRot();
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(f3));

            matrixStack.pushPose();
            matrixStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
            VertexConsumer buffer = ModelBakery.BANNER_BASE.buffer(p_225616_4_, RenderType::entitySolid);

            this.bar.render(matrixStack, buffer, p_225616_5_, p_225616_6_);
            BlockPos blockpos = tile.getBlockPos();
            float f2 = ((float) Math.floorMod((long) (blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + i, 100L) + p_225616_2_) / 100.0F;
            this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(((float) Math.PI * 2F) * f2)) * (float) Math.PI;
            this.flag.y = -32.0F;
            BannerRenderer.renderPatterns(matrixStack, p_225616_4_, p_225616_5_, p_225616_6_, this.flag, ModelBakery.BANNER_BASE, true, list);
            matrixStack.popPose();
            matrixStack.popPose();
        }
    }

}


package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.EndermanSkullModel;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EndermanSkullBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.client.renderer.blockentity.SkullBlockRenderer.renderSkull;

public class EndermanSkullBlockTileRenderer implements BlockEntityRenderer<EndermanSkullBlockTile> {

    @Nullable
    public static EndermanSkullModel MODEL = null;

    public EndermanSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        MODEL = new EndermanSkullModel(context.getModelSet().bakeLayer(ClientRegistry.ENDERMAN_HEAD_MODEL));
    }

    @Override
    public void render(EndermanSkullBlockTile blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        var m = MODEL;
        if (m == null) return;
        float f = blockEntity.getMouthAnimation(partialTick);

        BlockState blockState = blockEntity.getBlockState();
        boolean bl = blockState.getBlock() instanceof WallSkullBlock;
        Direction direction = bl ? blockState.getValue(WallSkullBlock.FACING) : null;
        float g = 22.5F * (bl ? (2 + direction.get2DDataValue()) * 4 : blockState.getValue(SkullBlock.ROTATION));
        RenderType renderType = RenderType.entityCutout(ModTextures.ENDERMAN_HEAD);
        poseStack.pushPose();
        if (direction != null) {
            var v = direction.step();
            v.mul(0.001f);
            poseStack.translate(v.x(), v.y(), v.z());
        }
        renderSkull(direction, g, f, poseStack, bufferSource, packedLight, m, renderType);

        renderType = RenderType.eyes(ModTextures.ENDERMAN_HEAD_EYES);
        renderSkull(direction, g, f, poseStack, bufferSource, 15728640, m, renderType);

        poseStack.popPose();
    }

}

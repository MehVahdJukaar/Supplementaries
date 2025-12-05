package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.EndermanSkullModel;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EndermanSkullBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.renderer.LightTexture;
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

    private final EndermanSkullModel model;

    public EndermanSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new EndermanSkullModel(context.getModelSet().bakeLayer(ClientRegistry.ENDERMAN_HEAD_MODEL));
    }

    @Override
    public void render(EndermanSkullBlockTile blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {


        BlockState blockState = blockEntity.getBlockState();
        boolean wall = blockState.getBlock() instanceof WallSkullBlock;
        Direction direction = wall ? blockState.getValue(WallSkullBlock.FACING) : null;
        float rotation = 22.5F * (wall ? (2 + direction.get2DDataValue()) * 4 : blockState.getValue(SkullBlock.ROTATION));
        RenderType renderType = RenderType.entityCutoutNoCull(ModTextures.ENDERMAN_HEAD);
        poseStack.pushPose();
        if (direction != null) {
            var v = direction.step();
            v.mul(0.001f);
            poseStack.translate(v.x(), v.y(), v.z());
        }
        float oldJawAnim = blockEntity.getAnimation(partialTick);
        float jawAim = blockEntity.getMouthAnimation(partialTick);

        model.setupJawAnimation(jawAim);
        renderSkull(direction, rotation, oldJawAnim, poseStack, bufferSource, packedLight, model, renderType);

        poseStack.popPose();
    }

}

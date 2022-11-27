package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.EndermanSkullModel;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EndermanSkullBlockTile;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.client.renderer.blockentity.SkullBlockRenderer.renderSkull;

public class EndermanSkullBlockTileRenderer implements BlockEntityRenderer<EndermanSkullBlockTile> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/enderman/enderman.png");
    private static final ResourceLocation EYES = new ResourceLocation("textures/entity/enderman/enderman_eyes.png");

    private final EndermanSkullModel model;

    public EndermanSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new EndermanSkullModel(context.getModelSet().bakeLayer(ModelLayers.ENDERMAN));
    }

    @Override
    public void render(EndermanSkullBlockTile blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float f = blockEntity.getMouthAnimation(partialTick);

        BlockState blockState = blockEntity.getBlockState();
        boolean bl = blockState.getBlock() instanceof WallSkullBlock;
        Direction direction = bl ? blockState.getValue(WallSkullBlock.FACING) : null;
        float g = 22.5F * (bl ? (2 + direction.get2DDataValue()) * 4 : blockState.getValue(SkullBlock.ROTATION));
        RenderType renderType = RenderType.entityCutoutNoCull(TEXTURE);
        renderSkull(direction, g, f, poseStack, bufferSource, packedLight, model, renderType);

        renderType = RenderType.eyes(EYES);
        renderSkull(direction, g, f, poseStack, bufferSource, 15728640, model, renderType);
    }

}

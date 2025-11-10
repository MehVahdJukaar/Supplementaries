package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.EndermanSkullModel;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.client.renderer.blockentity.SkullBlockRenderer.renderSkull;

public class SpiderSkullBlockTileRenderer implements BlockEntityRenderer<SkullBlockEntity> {

    private final SkullModel model;

    public SpiderSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new SkullModel(context.getModelSet().bakeLayer(ClientRegistry.SPIDER_HEAD_MODEL));
    }

    @Override
    public void render(SkullBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        float f = 0;
        BlockState blockState = blockEntity.getBlockState();
        boolean wall = blockState.getBlock() instanceof WallSkullBlock;
        Direction direction = wall ? blockState.getValue(WallSkullBlock.FACING) : null;
        float rotation = 22.5F * (wall ? (2 + direction.get2DDataValue()) * 4 : blockState.getValue(SkullBlock.ROTATION));
        RenderType renderType = RenderType.entityCutoutNoCull(ModTextures.SPIDER_HEAD);
        poseStack.pushPose();
        if (direction != null) {
            var v = direction.step();
            v.mul(0.001f);
            poseStack.translate(v.x(), v.y(), v.z());
        }

        renderSkull(direction, rotation, f, poseStack, bufferSource, packedLight, model, renderType);

        renderType = RenderType.eyes(ModTextures.SPIDER_HEAD_EYES);
        renderSkull(direction, rotation, f, poseStack, bufferSource, LightTexture.FULL_SKY, model, renderType);

        poseStack.popPose();
    }

    //same as spider head. We do this because texture pack like to decapitate the spider model. Looking at you, Fresh
    public static LayerDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartPose partPose = PartPose.offset(0.0F, -13.0F, 0.0F);
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(28, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), partPose);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }
}

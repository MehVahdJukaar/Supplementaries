package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadsTransformer;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlowerBoxBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlowerBoxBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class FlowerBoxBakedModel implements CustomBakedModel {
    private final BakedModel box;
    private final BlockModelShaper blockModelShaper;
    private final ModelState rotation;

    public FlowerBoxBakedModel(BakedModel box, ModelState rotation) {
        this.box = box;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        this.rotation = rotation;
    }

    private final ThreadLocal<BlockPos> posHack = ThreadLocal.withInitial(() -> BlockPos.ZERO);

    @Override
    public ExtraModelData getModelData(@NotNull ExtraModelData tileData, BlockPos pos, BlockState state, BlockAndTintGetter level) {
        posHack.set(pos);
        return tileData;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
        BlockPos pos = posHack.get();
        List<BakedQuad> quads = new ArrayList<>();

        //box
        try {
            quads.addAll(box.getQuads(state, side, rand));
        } catch (Exception ignored) {
        }

        //mimic
        try {

            if (state != null) {
                BlockState[] flowers = new BlockState[]{
                        data.get(FlowerBoxBlockTile.FLOWER_0),
                        data.get(FlowerBoxBlockTile.FLOWER_1),
                        data.get(FlowerBoxBlockTile.FLOWER_2)
                };

                PoseStack poseStack = new PoseStack();

                Matrix4f rot = rotation.getRotation().getMatrix();
                poseStack.mulPoseMatrix(rot);
                //no idea what these do anymore
                poseStack.translate(-0.3125, 0, 0);

                if (state.getValue(FlowerBoxBlock.ATTACHMENT) != AttachFace.WALL) {
                    poseStack.translate(0, 0, -0.3125);
                }

                float scale = 0.625f;
                poseStack.scale(scale, scale, scale);

                poseStack.translate(0.5, 0.5, 1);

                float offset = ((pos.getX() + pos.getZ()) % 2 == 0) ? 0.001f : -0.001f;
                if (state.getValue(FlowerBoxBlock.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    offset = -offset;
                }
                poseStack.translate(offset, offset, offset);

                for (int i = 0; i < 3; i++) {
                    BlockState flower = flowers[i];
                    if (flower != null && !flower.isAir()) {
                        poseStack.pushPose();
                        poseStack.translate(0.5 * i, 0, 0);

                        if (flower.hasProperty(BlockStateProperties.FLOWER_AMOUNT)) {
                            poseStack.translate(scale / 4f, 0, scale / 4f);
                        }
                        this.addBlockToModel(i, quads, flower, pos, poseStack, side, rand);
                        if (flower.hasProperty(DoublePlantBlock.HALF)) {
                            poseStack.translate(0, scale, 0);
                            this.addBlockToModel(i, quads, flower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), pos, poseStack, side, rand);
                        }
                        poseStack.popPose();
                    }

                }
            }

        } catch (Exception ignored) {
        }
        return quads;
    }

    private void addBlockToModel(int index, final List<BakedQuad> quads, BlockState state,
                                 BlockPos pos,
                                 PoseStack poseStack, @Nullable Direction side, @NotNull RandomSource rand) {
        BakedModel model;
        //for special flowers
        ResourceLocation res = FlowerPotHandler.getSpecialFlowerModel(state.getBlock().asItem(), true);
        if (res != null) {
            if (state.hasProperty(DoublePlantBlock.HALF) && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                //dont render double plants
                return;
            }
            model = ClientHelper.getModel(blockModelShaper.getModelManager(), res);
        } else {
            model = blockModelShaper.getBlockModel(state);
        }

        List<BakedQuad> mimicQuads = model.getQuads(state, side, rand);
        if(mimicQuads.isEmpty())return;

        poseStack.pushPose();
        if (res == null) {
            poseStack.translate(-0.5f, -0.5f, -0.5f);
            poseStack.scale(0.6249f, 0.6249f, 0.6249f);
        } else {
            poseStack.translate(-0.5f, -0.5f + 3 / 16f, -0.5f);
            // very ugly aa
            if (CommonConfigs.Building.FLOWER_BOX_SIMPLE_MODE.get()) {
                poseStack.scale(1 / 0.625f, 1 / 0.625f, 1 / 0.625f);
            }

        }
        Matrix4f matrix = poseStack.last().pose();
        poseStack.popPose();

        BakedQuadsTransformer transformer = BakedQuadsTransformer.create()
                .applyingTransform(matrix)
                .applyingShade(false)
                .applyingAmbientOcclusion(true)
                .applyingTintIndex(index);

        List<BakedQuad> collection = transformer.transformAll(mimicQuads);
        quads.addAll(collection);
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData) {
        return box.getParticleIcon();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

}

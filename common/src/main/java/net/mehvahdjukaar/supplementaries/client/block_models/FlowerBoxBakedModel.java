package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlowerBoxBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlowerBoxBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlowerBoxBakedModel implements CustomBakedModel {
    private final BakedModel box;
    private final BlockModelShaper blockModelShaper;

    public FlowerBoxBakedModel(BakedModel box) {
        this.box = box;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
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

                PoseStack matrixStack = new PoseStack();

                matrixStack.translate(0.5, 0.5, 0.5);

                float yaw = -state.getValue(FlowerBoxBlock.FACING).getOpposite().toYRot();
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
                matrixStack.translate(-0.3125, -3 / 16f, 0);

                if (state.getValue(FlowerBoxBlock.FLOOR)) {
                    matrixStack.translate(0, 0, -0.3125);
                }
                matrixStack.scale(0.625f, 0.625f, 0.625f);

                matrixStack.translate(0.5, 0.5, 0.5);

                matrixStack.translate(-0.5, 0, 0);

                for (int i = 0; i < 3; i++) {
                    BlockState flower = flowers[i];
                    if (flower != null && !flower.isAir()) {
                        this.addBlockToModel(i, quads, flower, matrixStack, side, rand);
                        if (flower.hasProperty(DoublePlantBlock.HALF)) {
                            matrixStack.translate(0, 1, 0);
                            this.addBlockToModel(i, quads, flower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), matrixStack, side, rand);
                            matrixStack.translate(0, -1, 0);
                        }
                    }

                    matrixStack.translate(0.5, 0, 0);
                }
            }

        } catch (Exception ignored) {
        }
        return quads;
    }

    private void addBlockToModel(int index, final List<BakedQuad> quads, BlockState state, PoseStack matrixStack, @Nullable Direction side, @NotNull RandomSource rand) {

        BakedModel model;
        //for special flowers
        //TODO: automatically scan and load models from blockstate flower folder
        ResourceLocation res = FlowerPotHandler.getSpecialFlowerModel(state.getBlock().asItem());
        if (res != null) {
            if (state.hasProperty(DoublePlantBlock.HALF) && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                //dont render double plants
                return;
            }
            model = ClientPlatformHelper.getModel(blockModelShaper.getModelManager(), res);
        } else {
            model = blockModelShaper.getBlockModel(state);
        }

        List<BakedQuad> mimicQuads = model.getQuads(state, side, rand);
        for (BakedQuad q : mimicQuads) {
            int[] v = Arrays.copyOf(q.getVertices(), q.getVertices().length);

            TextureAtlasSprite texture = this.getParticleIcon();
            if (res == null) {
                VertexUtil.moveVertices(v, -0.5f, -0.5f, -0.5f);
                VertexUtil.scaleVertices(v, 0.6249f);
            } else {
                VertexUtil.moveVertices(v, -0.5f, -0.5f + 3 / 16f, -0.5f);
            }

            VertexUtil.transformVertices(v, matrixStack, texture);

            quads.add(new BakedQuad(v, q.getTintIndex() >= 0 ? index : q.getTintIndex(), q.getDirection(), q.getSprite(), q.isShade()));
        }
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData) {
        return box.getParticleIcon();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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

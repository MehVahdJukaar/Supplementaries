package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlowerBoxBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlowerBoxBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSetSpeakerBlockPacket;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
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

                PoseStack poseStack = new PoseStack();

                Matrix4f rot = rotation.getRotation().getMatrix();
                poseStack.mulPoseMatrix(rot);
                //no idea what these do anymore
                poseStack.translate(-0.3125, 0, 0);

                if (state.getValue(FlowerBoxBlock.FLOOR)) {
                    poseStack.translate(0, 0, -0.3125);
                }

                float scale = 0.625f;
                poseStack.scale(scale, scale, scale);

                poseStack.translate(0.5, 0.5, 1);


                for (int i = 0; i < 3; i++) {
                    BlockState flower = flowers[i];
                    if (flower != null && !flower.isAir()) {
                        poseStack.pushPose();
                        poseStack.translate(0.5 * i, 0, 0);

                        if (flower.hasProperty(BlockStateProperties.FLOWER_AMOUNT)) {
                            poseStack.translate(scale/4f, 0, scale/4f);
                        }
                        this.addBlockToModel(i, quads, flower, poseStack, side, rand);
                        if (flower.hasProperty(DoublePlantBlock.HALF)) {
                            poseStack.translate(0, scale, 0);
                            this.addBlockToModel(i, quads, flower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), poseStack, side, rand);
                        }
                        poseStack.popPose();
                    }

                }
            }

        } catch (Exception ignored) {
        }
        return quads;
    }

    private void addBlockToModel(int index, final List<BakedQuad> quads, BlockState state, PoseStack poseStack, @Nullable Direction side, @NotNull RandomSource rand) {

        BakedModel model;
        //for special flowers
        //TODO: automatically scan and load models from blockstate flower folder
        ResourceLocation res = FlowerPotHandler.getSpecialFlowerModel(state.getBlock().asItem());
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
        for (BakedQuad q : mimicQuads) {
            poseStack.pushPose();
            int[] v = Arrays.copyOf(q.getVertices(), q.getVertices().length);

            if (res == null) {
                poseStack.translate(-0.5f, -0.5f, -0.5f);
                poseStack.scale(0.6249f, 0.6249f, 0.6249f);
            } else {
                poseStack.translate(-0.5f, -0.5f + 3 / 16f, -0.5f);
            }
            Matrix4f matrix = poseStack.last().pose();
            VertexUtil.transformVertices(v, matrix);

            poseStack.popPose();

            quads.add(new BakedQuad(v, q.getTintIndex() >= 0 ? index : q.getTintIndex(),
                    Direction.rotate(matrix, q.getDirection()), q.getSprite(), q.isShade()));
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

package net.mehvahdjukaar.supplementaries.client.models;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.blocks.FlowerBoxBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.FlowerBoxBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FlowerBoxBakedModel implements IDynamicBakedModel {
    private final IBakedModel box;
    private final BlockModelShapes blockModelShaper;

    public FlowerBoxBakedModel(IBakedModel box) {
        this.box = box;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        List<BakedQuad> quads = new ArrayList<>();

        try {
            quads.addAll(box.getQuads(state, side, rand, EmptyModelData.INSTANCE));
        } catch (Exception ignored) {}

        /*
                        Direction dir = state.getValue(WallLanternBlock.FACING);
                        if (mimic.hasProperty(BlockStateProperties.FACING)) {
                    mimic = mimic.setValue(BlockStateProperties.FACING, dir);
                } else if (mimic.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    mimic = mimic.setValue(BlockStateProperties.HORIZONTAL_FACING, dir);
                }

            if (flower1 != null && !flower1.isAir()) {
                this.addBlockToModel(quads, flower1, matrixStack, side, rand);
                if(flower1.getBlock() instanceof DoublePlantBlock){
                    BlockState flower1_up = flower1.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                    matrixStack.translate(0, 1, 0);
                    this.addBlockToModel(quads, flower1_up, matrixStack, side, rand);
                    matrixStack.translate(0, -1, 0);
                }
            }

            matrixStack.translate(0.5,0,0);

            if (flower2 != null && !flower2.isAir()) {
                this.addBlockToModel(quads, flower2, matrixStack, side, rand);
                if(flower2.getBlock() instanceof DoublePlantBlock){
                    BlockState flower2_up = flower2.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                    matrixStack.translate(0, 1, 0);
                    this.addBlockToModel(quads, flower2_up, matrixStack, side, rand);
                    matrixStack.translate(0, -1, 0);
                }
            }

            matrixStack.translate(-1,0,0);

            if (flower0 != null && !flower0.isAir()) {
                this.addBlockToModel(quads, flower0, matrixStack, side, rand);
                if(flower0.getBlock() instanceof DoublePlantBlock){
                    BlockState flower0_up = flower0.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                    matrixStack.translate(0, 1, 0);
                    this.addBlockToModel(quads, flower0_up, matrixStack, side, rand);
                    matrixStack.translate(0, -1, 0);
                }
            }
         */
        try{

            BlockState flower0 = extraData.getData(FlowerBoxBlockTile.FLOWER_0);
            BlockState flower1 = extraData.getData(FlowerBoxBlockTile.FLOWER_1);
            BlockState flower2 = extraData.getData(FlowerBoxBlockTile.FLOWER_2);


            MatrixStack matrixStack = new MatrixStack();

            matrixStack.translate(0.5, 0.5, 0.5);

            float yaw = -state.getValue(FlowerBoxBlock.FACING).getOpposite().toYRot();
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
            matrixStack.translate(-0.3125,-3/16f,0);

            if(state.getValue(FlowerBoxBlock.FLOOR)) {
                matrixStack.translate(0, 0, -0.3125);
            }
            matrixStack.scale(0.625f, 0.625f, 0.625f);

            matrixStack.translate(0.5, 0.5, 0.5);

            if (flower1 != null && !flower1.isAir()) {
                this.addBlockToModel(quads, flower1, matrixStack, side, rand);
                if(flower1.getBlock() instanceof DoublePlantBlock){
                    BlockState flower1_up = flower1.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                    matrixStack.translate(0, 1, 0);
                    this.addBlockToModel(quads, flower1_up, matrixStack, side, rand);
                    matrixStack.translate(0, -1, 0);
                }
            }

            matrixStack.translate(0.5,0,0);

            if (flower2 != null && !flower2.isAir()) {
                this.addBlockToModel(quads, flower2, matrixStack, side, rand);
                if(flower2.getBlock() instanceof DoublePlantBlock){
                    BlockState flower2_up = flower2.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                    matrixStack.translate(0, 1, 0);
                    this.addBlockToModel(quads, flower2_up, matrixStack, side, rand);
                    matrixStack.translate(0, -1, 0);
                }
            }

            matrixStack.translate(-1,0,0);

            if (flower0 != null && !flower0.isAir()) {
                this.addBlockToModel(quads, flower0, matrixStack, side, rand);
                if(flower0.getBlock() instanceof DoublePlantBlock){
                    BlockState flower0_up = flower0.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                    matrixStack.translate(0, 1, 0);
                    this.addBlockToModel(quads, flower0_up, matrixStack, side, rand);
                    matrixStack.translate(0, -1, 0);
                }
            }



        } catch (Exception ignored) {}


        return quads;
    }

    private void addBlockToModel(final List<BakedQuad> quads, BlockState state, MatrixStack matrixStack, @Nullable Direction side, @Nonnull Random rand){
        IBakedModel model = blockModelShaper.getBlockModel(state);

        List<BakedQuad> mimicQuads = model.getQuads(state, side, rand, EmptyModelData.INSTANCE);
        for (BakedQuad q : mimicQuads) {

            int[] v = Arrays.copyOf(q.getVertices(), q.getVertices().length);

            v = RendererUtil.moveVertices(v, -0.5f, -0.5f, -0.5f);
            v = RendererUtil.scaleVertices(v, 0.625f);

            v = RendererUtil.transformVertices(v, matrixStack);

            quads.add(new BakedQuad(v, q.getTintIndex(), q.getDirection(), q.getSprite(), q.isShade()));
        }
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
    public TextureAtlasSprite getParticleIcon() {
        return box.getParticleIcon();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public ItemCameraTransforms getTransforms() {
        return ItemCameraTransforms.NO_TRANSFORMS;
    }




}

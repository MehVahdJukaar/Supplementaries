package net.mehvahdjukaar.supplementaries.client.models;

import net.mehvahdjukaar.supplementaries.block.blocks.MimicBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.RopeKnotBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.FrameBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.MimicBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.mehvahdjukaar.supplementaries.block.blocks.RopeKnotBlock.*;

public class RopeKnotBlockBakedModel implements IDynamicBakedModel {
    private final IBakedModel knot;
    private final BlockModelShapes blockModelShaper;

    public RopeKnotBlockBakedModel(IBakedModel knot) {
        this.knot = knot;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        //always on cutout layer
        BlockState mimic = extraData.getData(FrameBlockTile.MIMIC);

        List<BakedQuad> quads = new ArrayList<>();

        try{
            if (mimic != null && !(mimic.getBlock() instanceof MimicBlock) && !mimic.isAir()) {
                IBakedModel model = blockModelShaper.getBlockModel(mimic);

                quads.addAll(model.getQuads(mimic, side, rand, EmptyModelData.INSTANCE));
            }
        } catch (Exception ignored) {}

        try {
            quads.addAll(knot.getQuads(mimic, side, rand, EmptyModelData.INSTANCE));
        } catch (Exception ignored) {}

        try{
            if(state != null && state.getBlock() instanceof  RopeKnotBlock){
                BlockState rope = ModRegistry.ROPE.get().defaultBlockState()
                    .setValue(UP,state.getValue(UP))
                    .setValue(DOWN,state.getValue(DOWN))
                    .setValue(NORTH, state.getValue(NORTH))
                    .setValue(SOUTH, state.getValue(SOUTH))
                    .setValue(EAST, state.getValue(EAST))
                    .setValue(WEST, state.getValue(WEST));

                IBakedModel model = blockModelShaper.getBlockModel(rope);

                quads.addAll(model.getQuads(rope, side, rand, EmptyModelData.INSTANCE));
            }
        } catch (Exception ignored) {}


        return quads;
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
    public TextureAtlasSprite getParticleIcon() {
        return knot.getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@NotNull IModelData data) {
        BlockState mimic = data.getData(MimicBlockTile.MIMIC);
        if (mimic != null && !mimic.isAir()) {

            IBakedModel model = blockModelShaper.getBlockModel(mimic);
            try {
                return model.getParticleIcon();
            } catch (Exception ignored) {}

        }
        return getParticleIcon();
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
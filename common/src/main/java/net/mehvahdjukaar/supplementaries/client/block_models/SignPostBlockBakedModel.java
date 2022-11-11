package net.mehvahdjukaar.supplementaries.client.block_models;

import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.supplementaries.common.block.IColored;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FramedBlocksCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

public class SignPostBlockBakedModel implements CustomBakedModel {
    private final BlockModelShaper blockModelShaper;

    public SignPostBlockBakedModel() {
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
        BlockState mimic = data.get(ModBlockProperties.MIMIC);
        Boolean isFramed = data.get(ModBlockProperties.FRAMED);

        boolean framed = CompatHandler.FRAMEDBLOCKS && (isFramed != null && isFramed);

        //            if (mimic != null && !mimic.isAir() && (layer == null || (framed || RenderTypeLookup.canRenderInLayer(mimic, layer)))) {
        //always solid.

        if (mimic != null && !mimic.isAir()) {

            ExtraModelData data2;
            if (framed) {
                //TODO: fix
                data2 = FramedBlocksCompat.getModelData(mimic);
                mimic = FramedBlocksCompat.getFramedFence();
            } else {
                data2 = ExtraModelData.EMPTY;
            }
            BakedModel model = blockModelShaper.getBlockModel(mimic);

            return model.getQuads(mimic, side, rand);
        }

        return Collections.emptyList();
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
    public TextureAtlasSprite getBlockParticle(ExtraModelData data) {
        BlockState mimic = data.get(SignPostBlockTile.MIMIC);
        if (mimic != null && !mimic.isAir()) {

            BakedModel model = blockModelShaper.getBlockModel(mimic);
            try {
                return model.getParticleIcon();
            } catch (Exception ignored) {
            }
        }
        return blockModelShaper.getBlockModel(Blocks.OAK_PLANKS.defaultBlockState()).getParticleIcon();
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

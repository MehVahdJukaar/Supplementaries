package net.mehvahdjukaar.supplementaries.client.block_models.forge;

import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.framedblocks.FramedSignPost;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.Collections;
import java.util.List;

public class SignPostBlockBakedModel implements CustomBakedModel {
    private final BlockModelShaper blockModelShaper;

    public SignPostBlockBakedModel() {
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
        try {
            BlockState mimic = data.get(ModBlockProperties.MIMIC);
            Boolean isFramed = data.get(ModBlockProperties.FRAMED);

            boolean framed = CompatHandler.framedblocks && (isFramed != null && isFramed);

            //            if (mimic != null && !mimic.isAir() && (layer == null || (framed || RenderTypeLookup.canRenderInLayer(mimic, layer)))) {
            //always solid.

            if (mimic != null && !mimic.isAir()) {

                ModelData data2;
                if (framed) {
                    data2 = FramedSignPost.getModelData(mimic);
                    mimic = FramedSignPost.framedFence;
                } else {
                    data2 = ModelData.EMPTY;
                }
                BakedModel model = blockModelShaper.getBlockModel(mimic);

                return model.getQuads(mimic, side, rand, data2, renderType);
            }
        } catch (Exception ignored) {
            int a = 1;
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
        return getParticleIcon();
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

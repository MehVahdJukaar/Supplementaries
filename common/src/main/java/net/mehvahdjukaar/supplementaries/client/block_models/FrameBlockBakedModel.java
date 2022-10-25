package net.mehvahdjukaar.supplementaries.client.block_models;

import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.MimicBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class FrameBlockBakedModel implements CustomBakedModel {
    private final BakedModel overlay;
    private final BlockModelShaper blockModelShaper;

    public FrameBlockBakedModel(BakedModel overlay) {
        this.overlay = overlay;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
        //always on cutout layer

        List<BakedQuad> quads = new ArrayList<>();

        if (state != null) {
            try {
                BlockState mimic = data.get(ModBlockProperties.MIMIC);
                if (mimic == null || (mimic.isAir())) {
                    BakedModel model = blockModelShaper.getBlockModel(state.setValue(FrameBlock.HAS_BLOCK, false));
                    quads.addAll(model.getQuads(mimic, side, rand));
                    return quads;
                }

                if (!(mimic.getBlock() instanceof MimicBlock)) {
                    BakedModel model = blockModelShaper.getBlockModel(mimic);

                    quads.addAll(model.getQuads(mimic, side, rand));
                }
            } catch (Exception ignored) {
            }

            try {
                quads.addAll(overlay.getQuads(state, side, rand));
            } catch (Exception ignored) {
            }
        }
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
    public TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ModTextures.TIMBER_CROSS_BRACE_TEXTURE);
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

package net.mehvahdjukaar.supplementaries.client.block_models;

import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.framedblocks.FramedSignPost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SignPostBlockBakedModel implements IDynamicBakedModel {
    private final BlockModelShaper blockModelShaper;

    public SignPostBlockBakedModel() {
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        try {
            BlockState mimic = extraData.getData(BlockProperties.MIMIC);
            Boolean isFramed = extraData.getData(BlockProperties.FRAMED);

            boolean framed = CompatHandler.framedblocks && (isFramed!=null && isFramed);

            //            if (mimic != null && !mimic.isAir() && (layer == null || (framed || RenderTypeLookup.canRenderInLayer(mimic, layer)))) {
            //always solid.

            if (mimic != null && !mimic.isAir()) {

                IModelData data;
                if (framed) {
                    data = FramedSignPost.getModelData(mimic);
                    mimic = FramedSignPost.framedFence;
                } else {
                    data = EmptyModelData.INSTANCE;
                }
                BakedModel model = blockModelShaper.getBlockModel(mimic);

                return model.getQuads(mimic, side, rand, data);


            }
        }
        catch (Exception ignored){
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
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(Textures.TIMBER_CROSS_BRACE_TEXTURE);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull IModelData data) {
        BlockState mimic = data.getData(SignPostBlockTile.MIMIC);
        if (mimic != null && !mimic.isAir()) {

            BakedModel model = blockModelShaper.getBlockModel(mimic);
            try {
                return model.getParticleIcon();
            } catch (Exception ignored) {}

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

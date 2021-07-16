package net.mehvahdjukaar.supplementaries.client.models;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.framedblocks.FramedSignPost;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.MinecraftForgeClient;
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
    private final BlockModelShapes blockModelShaper;

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

            RenderType layer = MinecraftForgeClient.getRenderLayer();

            //RenderType layer = MinecraftForgeClient.getRenderLayer();
            // if (layer == null || RenderTypeLookup.canRenderInLayer(mimic, layer)) {
            //always solid.
            if (mimic != null && !mimic.isAir() && (layer == null || (framed || RenderTypeLookup.canRenderInLayer(mimic, layer)))) {

                IModelData data;
                if (framed) {
                    data = FramedSignPost.getModelData(mimic);
                    mimic = FramedSignPost.framedFence;
                } else {
                    data = EmptyModelData.INSTANCE;
                }
                IBakedModel model = blockModelShaper.getBlockModel(mimic);

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
        return Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(Textures.TIMBER_CROSS_BRACE_TEXTURE);

    }


    @Override
    public TextureAtlasSprite getParticleTexture(@NotNull IModelData data) {
        BlockState mimic = data.getData(SignPostBlockTile.MIMIC);
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

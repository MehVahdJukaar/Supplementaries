package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.DummySprite;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.SignPostBlockTileRenderer;
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

import java.util.ArrayList;
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
        SignPostBlockTile.Sign up = data.get(ModBlockProperties.SIGN_UP);
        SignPostBlockTile.Sign down = data.get(ModBlockProperties.SIGN_DOWN);
        Float zOffset = data.get(ModBlockProperties.RENDER_OFFSET);

        boolean framed = CompatHandler.FRAMEDBLOCKS && (isFramed != null && isFramed);

        //            if (mimic != null && !mimic.isAir() && (layer == null || (framed || RenderTypeLookup.canRenderInLayer(mimic, layer)))) {
        //always solid.

        List<BakedQuad> quads = new ArrayList<>();
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

            quads.addAll(model.getQuads(mimic, side, rand));
        }

        if (up != null && down != null) {
            try (BakedQuadBuilder builder = BakedQuadBuilder.create(DummySprite.INSTANCE, quads::add)) {
                builder.setAutoDirection();
                builder.setAmbientOcclusion(false); //looks bad as they go beyond 1 block
                SignPostBlockTileRenderer.renderSigns(new PoseStack(),
                        builder, 0, 0, up, down, zOffset);
            } catch (Exception e) {
                Supplementaries.error();
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
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData data) {
        BlockState mimic = data.get(SignPostBlockTile.MIMIC_KEY);
        if (mimic != null && !mimic.isAir()) {

            BakedModel model = blockModelShaper.getBlockModel(mimic);
            try {
                return model.getParticleIcon();
            } catch (Exception ignored) {
            }
        }
        var sign = data.get(SignPostBlockTile.SIGN_UP_KEY);
        if (sign == null || !sign.active()) {
            sign = data.get(SignPostBlockTile.SIGN_DOWN_KEY);
        }
        if (sign != null && sign.active()) {
            BlockState planks = sign.woodType().planks.defaultBlockState();
            BakedModel model = blockModelShaper.getBlockModel(planks);
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

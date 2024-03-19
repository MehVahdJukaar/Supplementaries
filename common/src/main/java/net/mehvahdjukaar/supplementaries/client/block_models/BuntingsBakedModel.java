package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.supplementaries.client.DummySprite;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.BuntingBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BuntingBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BuntingsBakedModel implements CustomBakedModel {
    private final BlockModelShaper blockModelShaper;
    private final ModelState rotation;

    public BuntingsBakedModel(ModelState transform, Function<Material, TextureAtlasSprite> spriteGetter) {
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        this.rotation = transform;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
        List<BakedQuad> quads = new ArrayList<>();
        try {
            if (!data.get(BuntingBlockTile.IS_FANCY)) {
                DyeColor north = data.get(BuntingBlockTile.NORTH_BUNTING);
                DyeColor south = data.get(BuntingBlockTile.SOUTH_BUNTING);
                DyeColor east = data.get(BuntingBlockTile.EAST_BUNTING);
                DyeColor west = data.get(BuntingBlockTile.WEST_BUNTING);
                PoseStack poseStack = new PoseStack();
                poseStack.translate(0.5, 0.5, 0.5);
                BakedQuadBuilder builder = BakedQuadBuilder.create(DummySprite.INSTANCE);
                builder.setAutoDirection();
                builder.setAmbientOcclusion(false);
                builder.setAutoBuild(quads::add);
                if (north != null) {
                    BuntingBlockTileRenderer.renderBunting(north, Direction.NORTH,
                            0, poseStack, builder, null,
                            0, OverlayTexture.NO_OVERLAY, BlockPos.ZERO, 0);
                }
                if (south != null) {
                    BuntingBlockTileRenderer.renderBunting(south, Direction.SOUTH,
                            0, poseStack, builder, null,
                            0, OverlayTexture.NO_OVERLAY, BlockPos.ZERO, 0);
                }
                if (east != null) {
                    BuntingBlockTileRenderer.renderBunting(east, Direction.EAST,
                            0, poseStack, builder, null,
                            0, OverlayTexture.NO_OVERLAY, BlockPos.ZERO, 0);
                }
                if (west != null) {
                    BuntingBlockTileRenderer.renderBunting(west, Direction.WEST,
                            0, poseStack, builder, null,
                            0, OverlayTexture.NO_OVERLAY, BlockPos.ZERO, 0);
                }
            }
        } catch (Exception ignored) {
        }

        return quads;
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData) {
        return null;
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

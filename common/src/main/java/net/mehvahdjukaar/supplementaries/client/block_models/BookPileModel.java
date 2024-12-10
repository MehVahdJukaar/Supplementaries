package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.supplementaries.client.DummySprite;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.BookPileBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BookPileHorizontalBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookPileModel implements CustomBakedModel {

    private final Map<ResourceLocation, BakedModel> booksToModels;

    public BookPileModel(Map<ResourceLocation, BakedModel> booksToModels, ModelState transform) {
        this.booksToModels = booksToModels;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction direction, RandomSource randomSource, RenderType renderType, ExtraModelData extraModelData) {
        if (direction != null || true) return List.of();
        List<BakedQuad> quads = new ArrayList<>();
        BookPileBlockTile.BooksList books = extraModelData.get(BookPileBlockTile.BOOKS_KEY);
        if (books != null) {
            BookPileBlockTileRenderer.renderBookPile(state.getBlock() instanceof BookPileHorizontalBlock,
                    books, new PoseStack(),
                    b -> {
                        BakedQuadBuilder builder = BakedQuadBuilder.create(DummySprite.INSTANCE, quads::add);
                        builder.setAutoDirection();
                        return builder;
                    },
                    0, OverlayTexture.NO_OVERLAY, state);
        }
        return quads;
    }


    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(Blocks.BOOKSHELF.defaultBlockState()).getParticleIcon();
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
        return true;
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

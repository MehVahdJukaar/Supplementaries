package net.mehvahdjukaar.supplementaries.client.block_models;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager.BlackboardKey;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class BlackboardBakedModel implements IDynamicBakedModel {
    //model cache used when switching frequently between models
    // data needed to rebake

    private final IModelConfiguration owner;
    private final ModelBakery bakery;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final ModelState modelTransform;
    private final ItemOverrides overrides;

    private final BakedModel back;

    public BlackboardBakedModel(BakedModel unbaked, IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides) {
        this.back = unbaked;
        this.owner = owner;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        this.overrides = overrides;
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
        return spriteGetter.apply(owner.resolveTexture("particle"));
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }


    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random, IModelData data) {
        List<BakedQuad> quads = new ArrayList<>(back.getQuads(state, direction, random, data));
        if (data != EmptyModelData.INSTANCE) {
            BlackboardKey key = data.getData(BlackboardBlockTile.BLACKBOARD);
            if (key != null) {
                quads.addAll(BlackboardTextureManager.INSTANCE.getBlackboardInstance(key).getOrCreateQuads(this::generateQuads));
            }
        }

        return quads;
        /*
        ResourceLocation texture;
        if (data == EmptyModelData.INSTANCE) {
            texture = MissingTextureSprite.getLocation();
        }
        else {
            // get texture name, if missing use missing
            // also use missing if no retextured, that just makes the cache smaller for empty cauldron

            Black = data.getData(BlackboardBlockTile.TEXTURE);
            if (texture == null) {
                texture = MissingTextureSprite.getLocation();
            } else {
                // serverside uses texture "name" rather than path, use the sprite getter to translate
                texture = Textures.HONEY_TEXTURE;
            }
        }

        // fetch liquid offset amount

        // determine model variant
        IBakedModel baked = null;//warmBakery.apply(texture);
        // return quads



        return baked.getQuads(state, direction, random, data);
        */

    }

    private List<BakedQuad> generateQuads(byte[][] pixels) {
        TextureAtlasSprite black = spriteGetter.apply(owner.resolveTexture("black"));
        TextureAtlasSprite white = spriteGetter.apply(owner.resolveTexture("white"));

        List<BakedQuad> newQuads = new ArrayList<>();
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                byte b = pixels[pixels.length-x][pixels[x].length-y];
                int tint = BlackboardBlock.colorFromByte(b);
                TextureAtlasSprite sprite = b == 0 ? black : white;
                newQuads.add(createPixelQuad(x / 16f, y / 16f, 0.3125f, 1 / 16f, 1 / 16f, sprite, tint));

            }
        }
        return newQuads;
    }

    //MCjty code

    private static BakedQuad createPixelQuad(float x, float y, float z, float width, float height, TextureAtlasSprite sprite, int color) {
        Vec3 normal = new Vec3(0, 0, 1);
        float tw = sprite.getWidth() * height;
        float th = sprite.getHeight() * width;
        float u0 = x * 16;
        float v0 = y * 16;

        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getNearest(normal.x, normal.y, normal.z));
        putVertex(builder, normal, x, y, z,
                u0, v0, sprite, color);
        putVertex(builder, normal, x, y + height, z,
                u0, v0 + th, sprite, color);
        putVertex(builder, normal, x + width, y + height, z,
                u0 + tw, v0 + th, sprite, color);
        putVertex(builder, normal, x + width, y, z,
                u0 + tw, v0, sprite, color);
        return builder.build();
    }

    private static void putVertex(BakedQuadBuilder builder, Vec3 normal,
                                  float x, float y, float z, float u, float v, TextureAtlasSprite sprite, int color) {

        float r = (color >> 16 & 255) / 255f;
        float g = (color >> 8 & 255) / 255f;
        float b = (color >> 0 & 255) / 255f;
        ImmutableList<VertexFormatElement> elements = builder.getVertexFormat().getElements().asList();
        for (int j = 0; j < elements.size(); j++) {
            VertexFormatElement e = elements.get(j);
            switch (e.getUsage()) {
                case POSITION:
                    builder.put(j, x, y, z, 1.0f);
                    break;
                case COLOR:
                    builder.put(j, r, g, b, 1.0f);
                    break;
                case UV:
                    switch (e.getIndex()) {
                        case 0 -> {
                            float iu = sprite.getU(u);
                            float iv = sprite.getV(v);
                            builder.put(j, iu, iv);
                        }
                        case 2 -> builder.put(j, (short) 0, (short) 0);
                        default -> builder.put(j);
                    }
                    break;
                case NORMAL:
                    builder.put(j, (float) normal.x, (float) normal.y, (float) normal.z);
                    break;
                default:
                    builder.put(j);
                    break;
            }
        }
    }

    private static class Square {
        public final byte color;
        public final int x;
        public final int y;
        public int width;
        public int height;

        private Square(byte color, int x, int y) {
            this.color = color;
            this.x = x;
            this.y = y;
        }

        public int area() {
            return width * height;
        }

        public boolean isCovered(int x, int y) {
            return x >= this.x && y >= this.y && x < this.x + width && y < this.y + height;
        }

        // public Square[] expandW(byte[][] image) {
        //}
    }

    // public void stuff(byte[][] image, int width, int height){
    //     Square s = new Square(image[0][0],0,0);
//
    //      Square[] west = s.expandW(image);
    //  }


}



package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager.BlackboardKey;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BlackboardBakedModel implements CustomBakedModel {

    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final ModelState modelTransform;

    private final BakedModel back;
    private final BlockModel owner;

    public BlackboardBakedModel(BlockModel owner, BakedModel baked, Function<Material, TextureAtlasSprite> spriteGetter,
                                ModelState modelTransform) {
        this.back = baked;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        this.owner = owner;
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
        return back.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
        List<BakedQuad> quads = new ArrayList<>(back.getQuads(state, side, rand));
        if (data != ExtraModelData.EMPTY && state != null && side == null) {
            Direction dir = state.getValue(BlackboardBlock.FACING);
            BlackboardKey key = data.get(BlackboardBlockTile.BLACKBOARD);
            if (key != null) {
                var blackboard = BlackboardTextureManager.getBlackboardInstance(key);
                quads.addAll(blackboard.getOrCreateModel(dir, b -> generateQuads(b, this.modelTransform)));
            }
        }

        return quads;
    }

    private List<BakedQuad> generateQuads(byte[][] pixels, ModelState modelTransform) {
        List<BakedQuad> quads;
        try (TextureAtlasSprite black = spriteGetter.apply(owner.getMaterial("black"));
             TextureAtlasSprite white = spriteGetter.apply(owner.getMaterial("white"))) {
            quads = new ArrayList<>();
            var rotation = modelTransform.getRotation();

            for (int x = 0; x < pixels.length; x++) {
                int length = 0;
                int startY = 0;
                byte prevColor = pixels[0][x];
                for (int y = 0; y <= pixels[x].length; y++) {
                    Byte current = null;
                    if (y < pixels[x].length) {
                        byte b = pixels[x][y];
                        if (prevColor == b) {
                            length++;
                            continue;
                        }
                        current = b;
                    }
                    //block uv is incorrectly flipped on both axis... too bad
                    //draws prev quad
                    int tint =  255 << 24 | BlackboardBlock.colorFromByte(prevColor);
                    TextureAtlasSprite sprite = prevColor == 0 ? black : white;
                    quads.add(createPixelQuad((15 - x) / 16f, (16 - length - startY) / 16f, 1 - 0.3125f,
                            1 / 16f, length / 16f, sprite, tint, rotation));
                    startY = y;
                    if (current != null) {
                        prevColor = current;
                    }
                    length = 1;
                }
            }
        }
        return quads;
    }

    public static BakedQuad createPixelQuad(float x, float y, float z, float width, float height, TextureAtlasSprite sprite, int color, Transformation transform) {
        Vector3f normal = new Vector3f(0, 0, -1);

        BakedQuadBuilder builder = BakedQuadBuilder.create();

        BakedQuadBuilder.applyModelRotation(0,0,-1, transform.getMatrix());
        float tu = sprite.getWidth() * width;
        float tv = sprite.getHeight() * height;
        float u0 = x * 16;
        float v0 = y * 16;
        color = -1;
        builder.setDirection(Direction.getNearest(normal.x(), normal.y(), normal.z()));
        builder.setSprite(sprite);

        putVertex(builder, normal, x + width, y + height, z,
                u0 + tu, v0 + tv, sprite, color, transform);
        putVertex(builder, normal, x + width, y, z,
                u0 + tu, v0, sprite, color, transform);
        putVertex(builder, normal, x, y, z,
                u0, v0, sprite, color, transform);
        putVertex(builder, normal, x, y + height, z,
                u0, v0 + tv, sprite, color, transform);

        return builder.build();
    }

    private static void putVertex(BakedQuadBuilder builder, Vector3f normal,
                                  float x, float y, float z, float u, float v,
                                  TextureAtlasSprite sprite, int color, Transformation transformation) {

        Vector3f posV = BakedQuadBuilder.applyModelRotation(x, y, z, transformation.getMatrix());

        builder.pos(posV);

        builder.color(color);

        builder.uv(sprite.getU(u), sprite.getV(v));

        builder.normal(normal.x(), normal.y(), normal.z());

        builder.endVertex();
    }


}



package net.mehvahdjukaar.supplementaries.client.block_models.forge;

import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager.BlackboardKey;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BlackboardBakedModel implements CustomBakedModel {
    //model cache used when switching frequently between models
    // data needed to rebake

    private final IGeometryBakingContext owner;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final ModelState modelTransform;
    private final ItemOverrides overrides;

    private final BakedModel back;

    public BlackboardBakedModel(BakedModel unbaked, IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides) {
        this.back = unbaked;
        this.owner = owner;
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
    public TextureAtlasSprite getBlockParticle(ExtraModelData data) {
        return spriteGetter.apply(owner.getMaterial("particle"));
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
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
        List<BakedQuad> quads = new ArrayList<>(back.getQuads(state, side, rand));
        if (data != ExtraModelData.EMPTY && state != null && side == null) {
            Direction dir = state.getValue(BlackboardBlock.FACING);
            BlackboardKey key = data.get(BlackboardBlockTile.BLACKBOARD);
            if (key != null) {
                quads.addAll(BlackboardTextureManager.getBlackboardInstance(key)
                        .getOrCreateModel(dir, b -> generateQuads(b, this.modelTransform)));
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
                    //block uv is oncorrectly flipped on both axis... too bai
                    //draws prev quad
                    int tint = BlackboardBlock.colorFromByte(prevColor);
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

    //MCjty code

    private static BakedQuad createPixelQuad(float x, float y, float z, float width, float height,
                                             TextureAtlasSprite sprite, int color, Transformation transform) {
        Vector3f normal = new Vector3f(0, 0, -1);
        applyModelRotation(normal, transform);
        float tu = sprite.getWidth() * width;
        float tv = sprite.getHeight() * height;
        float u0 = x * 16;
        float v0 = y * 16;

        AtomicReference<BakedQuad> output = new AtomicReference<>();
        QuadBakingVertexConsumer builder = new QuadBakingVertexConsumer(output::set);
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

        return output.get();
    }

    private static void putVertex(QuadBakingVertexConsumer builder, Vector3f normal,
                                  float x, float y, float z, float u, float v,
                                  TextureAtlasSprite sprite, int color, Transformation transformation) {

        Vector3f posV = new Vector3f(x, y, z);
        applyModelRotation(posV, transformation);
        builder.vertex(posV.x(), posV.y(), posV.z());

        builder.color(color);

        builder.uv(sprite.getU(u), sprite.getV(v));

        builder.normal(normal.x(), normal.y(), normal.z());
    }


    public static void applyModelRotation(Vector3f pPos, Transformation pTransform) {
        if (pTransform != Transformation.identity()) {
            rotateVertexBy(pPos, new Vector3f(0.5F, 0.5F, 0.5F), pTransform.getMatrix());
        }
    }

    private static void rotateVertexBy(Vector3f pPos, Vector3f pOrigin, Matrix4f pTransform) {
        Vector4f vector4f = new Vector4f(pPos.x() - pOrigin.x(), pPos.y() - pOrigin.y(), pPos.z() - pOrigin.z(), 1.0F);
        vector4f.transform(pTransform);
        pPos.set(vector4f.x() + pOrigin.x(), vector4f.y() + pOrigin.y(), vector4f.z() + pOrigin.z());
    }


}



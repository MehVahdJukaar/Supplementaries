package net.mehvahdjukaar.supplementaries.client.block_models;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadsTransformer;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RandomRotationModel implements CustomBakedModel {

    private final Map<Direction, List<BakedQuad>[]> quadCache = new Object2ObjectArrayMap<>();
    private final BakedModel wrapped;

    public RandomRotationModel(BakedModel back, ModelState modelTransform) {
        this.wrapped = back;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return wrapped.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return wrapped.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData data) {
        return wrapped.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return wrapped.getOverrides();
    }

    @Override
    public ItemTransforms getTransforms() {
        return wrapped.getTransforms();
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType,
                                         ExtraModelData data) {
        int randIndex = rand.nextInt(4);
        float angle = randIndex * 90f;
        List<BakedQuad>[] array = quadCache.computeIfAbsent(side, d -> new List[4]);

        List<BakedQuad> arr = array[randIndex];
        if (arr == null) {
            List<BakedQuad> newQuads = new ArrayList<>();
            for (BakedQuad q : wrapped.getQuads(state, side, rand)) {
                //rotate texture of each quad. very naiive. works for us bc we only have one quad that takes up the whole face
                Direction normal = q.getDirection();
                //apply random rotation over the normal axis
                Quaternionf randomRot = new Quaternionf()
                        .rotateAxis(angle * Mth.DEG_TO_RAD, normal.step());
                Matrix4f mat = new Matrix4f();
                mat.rotate(randomRot);
                BakedQuadsTransformer transformer = BakedQuadsTransformer.create()
                        .applyingTransform(mat);
                newQuads.add(transformer.transform(q));
            }
            array[randIndex] = newQuads;
            return newQuads;
        }
        return array[randIndex];
    }


}



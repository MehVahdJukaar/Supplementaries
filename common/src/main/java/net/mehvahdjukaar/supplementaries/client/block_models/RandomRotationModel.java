package net.mehvahdjukaar.supplementaries.client.block_models;

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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RandomRotationModel implements CustomBakedModel {

    //precomputed, sodium calls this from multiple threads...
    private final Map<Direction, List<BakedQuad>[]> quadCache = new EnumMap<>(Direction.class);
    private final BakedModel wrapped;

    public RandomRotationModel(BakedModel back, ModelState modelTransform) {
        this.wrapped = back;

        RandomSource rand = RandomSource.create(42);
        for (Direction side : Direction.values()) {
            @SuppressWarnings("unchecked")
            List<BakedQuad>[] rotations = new List[4];
            for (int i = 0; i < 4; i++) {
                rotations[i] = buildRotatedQuads(side, rand, i * 90f);
            }
            quadCache.put(side, rotations);
        }
    }

    private List<BakedQuad> buildRotatedQuads(Direction side, RandomSource rand, float angle) {
        List<BakedQuad> list = new ArrayList<>();
        for (BakedQuad q : wrapped.getQuads(null, side, rand)) {
            Quaternionf rotation = new Quaternionf().rotateAxis(angle * Mth.DEG_TO_RAD, q.getDirection().step());
            var transformer = BakedQuadsTransformer.create().applyingTransform(new Matrix4f().rotate(rotation));
            list.add(transformer.transform(q));
        }
        return List.copyOf(list);
    }


    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
        if (side == null) return wrapped.getQuads(state, null, rand);
        return quadCache.get(side)[rand.nextInt(4)];
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
}
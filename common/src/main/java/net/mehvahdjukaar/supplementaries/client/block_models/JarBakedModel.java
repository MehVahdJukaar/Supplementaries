package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class JarBakedModel implements CustomBakedModel {

    private static final boolean SINGLE_PASS = true;//  PlatHelper.getPlatform().isFabric();

    //hacky
    private static final Vector3f LAST_KNOWN_DIMENSIONS = new Vector3f(8/16f, 12/16f, 1/16f);

    private final BakedModel jar;
    private final float width;
    private final float height;
    private final float yOffset;

    public JarBakedModel(BakedModel goblet, float width, float height, float yOffset, ModelState rotation) {
        this.jar = goblet;
        this.width = width;
        this.height = height;
        this.yOffset = yOffset;
        LAST_KNOWN_DIMENSIONS.set(width, height, yOffset);
    }

    public static Vector3f getJarLiquidDimensions() {
        return LAST_KNOWN_DIMENSIONS;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
        List<BakedQuad> quads = new ArrayList<>();
        if (!SINGLE_PASS && renderType == RenderType.translucent()) {
            var fluid = data.get(ModBlockProperties.FLUID);
            if (fluid != null && !fluid.isEmptyFluid()) {
                float amount = data.get(ModBlockProperties.FILL_LEVEL);

                TextureAtlasSprite sprite = ModMaterials.get(fluid.getStillTexture()).sprite();
                BakedQuadBuilder builder = BakedQuadBuilder.create(sprite);
                builder.setAutoDirection();
                builder.lightEmission(fluid.getLuminosity());
                builder.setTint(1);
                var poseStack = new PoseStack();
                poseStack.translate(0.5, yOffset, 0.5);
                try {
                    builder.setAutoBuild(quads::add);
                } catch (Exception ignored) {
                }
                VertexUtil.addCube(builder, poseStack, 0.5f - width / 2f, 0, width,
                        height * amount,
                        0, -1);

                //VertexUtils.addQuad(builder, poseStack, 0,0,1,1,1,1);
            }
            if (!SINGLE_PASS) return quads;
        }
        if(renderType == RenderType.cutout()) {
            quads.addAll(jar.getQuads(state, side, rand));
        }
        return quads;
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData) {
        return jar.getParticleIcon();
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
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }


}

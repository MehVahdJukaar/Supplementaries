package net.mehvahdjukaar.supplementaries.client.block_models.forge;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.MimicBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeKnotBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.mehvahdjukaar.supplementaries.common.block.blocks.RopeKnotBlock.*;

public class RopeKnotBlockBakedModel implements IDynamicBakedModel {
    private final BakedModel knot;
    private final BlockModelShaper blockModelShaper;

    public RopeKnotBlockBakedModel(BakedModel knot) {
        this.knot = knot;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, RenderType renderType) {

        List<BakedQuad> quads = new ArrayList<>();

        //mimic
        try {

            BlockState mimic = extraData.get(BlockProperties.MIMIC);
            if (mimic != null && !(mimic.getBlock() instanceof MimicBlock) && !mimic.isAir()) {
                BakedModel model = blockModelShaper.getBlockModel(mimic);

                quads.addAll(model.getQuads(mimic, side, rand, ModelData.EMPTY));
            }


        } catch (Exception ignored) {
        }

        //knot & rope
        try {
            if (state != null && state.getBlock() instanceof RopeKnotBlock) {
                BlockState rope = ModRegistry.ROPE.get().defaultBlockState()
                        .setValue(UP, state.getValue(UP))
                        .setValue(DOWN, state.getValue(DOWN))
                        .setValue(NORTH, state.getValue(NORTH))
                        .setValue(SOUTH, state.getValue(SOUTH))
                        .setValue(EAST, state.getValue(EAST))
                        .setValue(WEST, state.getValue(WEST));

                BakedModel model = blockModelShaper.getBlockModel(rope);
                //rope
                quads.addAll(model.getQuads(rope, side, rand, ModelData.EMPTY, renderType));

                //knot
                quads.addAll(knot.getQuads(state, side, rand, ModelData.EMPTY, renderType));
            }
        } catch (Exception ignored) {
        }


        return quads;
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
        return knot.getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        BlockState mimic = data.get(BlockProperties.MIMIC);
        if (mimic != null && !mimic.isAir()) {

            BakedModel model = blockModelShaper.getBlockModel(mimic);
            try {
                return model.getParticleIcon();
            } catch (Exception ignored) {
            }

        }
        return getParticleIcon();
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
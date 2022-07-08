package net.mehvahdjukaar.supplementaries.client.block_models.forge;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class HangingPotBakedModel implements IDynamicBakedModel {
    private final BakedModel rope;
    private final BlockModelShaper blockModelShaper;

    public HangingPotBakedModel(BakedModel rope) {
        this.rope = rope;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData) {

        //always on cutout layer
        List<BakedQuad> quads = new ArrayList<>();

        if (state != null) {
            try {
                BlockState mimic = extraData.getData(BlockProperties.MIMIC);

                if (mimic != null) {
                    BakedModel model = blockModelShaper.getBlockModel(mimic);
                    quads.addAll(model.getQuads(mimic, side, rand, ModelData.EMPTY));
                }
            } catch (Exception ignored) {
            }

            try {
                quads.addAll(rope.getQuads(state, side, rand, ModelData.EMPTY));
            } catch (Exception ignored) {
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
        return rope.getParticleIcon();
   }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        BlockState mimic = data.getData(BlockProperties.MIMIC);
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

package net.mehvahdjukaar.supplementaries.client.block_models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class HangingSignBakedModel implements IDynamicBakedModel {
    private final EnumMap<BlockProperties.SignAttachment, ImmutableList<BakedModel>> attachmentMap;
    private final BakedModel particle;

    private final BlockModelShaper blockModelShaper;

    public HangingSignBakedModel(BakedModel stick,
                                 BakedModel leftPost, BakedModel leftPalisade, BakedModel leftWall, BakedModel leftBeam,
                                 BakedModel rightPost, BakedModel rightPalisade, BakedModel rightWall, BakedModel rightBeam) {
        Map<BlockProperties.SignAttachment, ImmutableList<BakedModel>> temp = new HashMap<>();
        for (BlockProperties.SignAttachment a : BlockProperties.SignAttachment.values()) {
            ImmutableList.Builder<BakedModel> b = ImmutableList.builder();
            if (a != BlockProperties.SignAttachment.CEILING) {
                b.add(stick);
                switch (a.left) {
                    case POST -> b.add(leftPost);
                    case PALISADE -> b.add(leftPalisade);
                    case WALL -> b.add(leftWall);
                    case BEAM -> b.add(leftBeam);
                }
                switch (a.right) {
                    case POST -> b.add(rightPost);
                    case PALISADE -> b.add(rightPalisade);
                    case WALL -> b.add(rightWall);
                    case BEAM -> b.add(rightBeam);
                }
            }
            temp.put(a, b.build());
        }

        this.attachmentMap = Maps.newEnumMap(temp);
        this.particle = stick;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        List<BakedQuad> quads = new ArrayList<>();

        if (state != null && state.getBlock() instanceof HangingSignBlock hs) {
            //support & connections
            try {
                var a = state.getValue(HangingSignBlock.ATTACHMENT);
                attachmentMap.get(a).forEach(b -> quads.addAll(b.getQuads(state, side, rand, EmptyModelData.INSTANCE)));

            } catch (Exception ignored) {
            }
            //connections

            //static sign
            try {
                boolean fancy = Boolean.TRUE.equals(extraData.getData(BlockProperties.FANCY));
                if (!fancy) {

                    BakedModel model = blockModelShaper.getModelManager().getModel(Materials.HANGING_SIGNS_BLOCK_MODELS.get(hs.woodType));
                    if (model.getParticleIcon() instanceof MissingTextureAtlasSprite) return quads;
                    var signQuads = model.getQuads(state, side, rand, EmptyModelData.INSTANCE);
                    boolean flipped = state.getValue(HangingSignBlock.AXIS) == Direction.Axis.X;
                    boolean ceiling = state.getValue(HangingSignBlock.ATTACHMENT) == BlockProperties.SignAttachment.CEILING;
                    if (flipped || ceiling) {
                        //TODO: move to renderUtils
                        for (BakedQuad q : signQuads) {
                            int[] v = Arrays.copyOf(q.getVertices(), q.getVertices().length);
                            Direction dir = q.getDirection();
                            if (flipped) {
                                RendererUtil.rotateVerticesY(v, q.getSprite(), Rotation.CLOCKWISE_90);
                                if (dir.getAxis() != Direction.Axis.Y) dir = dir.getClockWise();
                            }
                            if (ceiling) {
                                RendererUtil.moveVertices(v, 0, 0.125f, 0, q.getSprite());
                            }
                            quads.add(new BakedQuad(v, q.getTintIndex(), dir, q.getSprite(), q.isShade()));
                        }
                    } else quads.addAll(signQuads);
                }
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
        return particle.getParticleIcon();
        //return Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(Textures.TIMBER_CROSS_BRACE_TEXTURE);
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

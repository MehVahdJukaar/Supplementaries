package net.mehvahdjukaar.supplementaries.client.block_models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class HangingSignBakedModel implements CustomBakedModel {
    private final EnumMap<ModBlockProperties.SignAttachment, ImmutableList<BakedModel>> attachmentMap;
    private final BakedModel particle;

    private final BlockModelShaper blockModelShaper;

    public HangingSignBakedModel(BakedModel stick,
                                 BakedModel leftPost, BakedModel leftPalisade, BakedModel leftWall, BakedModel leftBeam, BakedModel leftStick,
                                 BakedModel rightPost, BakedModel rightPalisade, BakedModel rightWall, BakedModel rightBeam, BakedModel rightStick) {
        Map<ModBlockProperties.SignAttachment, ImmutableList<BakedModel>> temp = new EnumMap<>(ModBlockProperties.SignAttachment.class);
        for (ModBlockProperties.SignAttachment a : ModBlockProperties.SignAttachment.values()) {
            ImmutableList.Builder<BakedModel> b = ImmutableList.builder();
            if (a != ModBlockProperties.SignAttachment.CEILING) {
                b.add(stick);
                switch (a.left) {
                    case POST -> b.add(leftPost);
                    case PALISADE -> b.add(leftPalisade);
                    case WALL -> b.add(leftWall);
                    case BEAM -> b.add(leftBeam);
                    case STICK -> b.add(leftStick);
                }
                switch (a.right) {
                    case POST -> b.add(rightPost);
                    case PALISADE -> b.add(rightPalisade);
                    case WALL -> b.add(rightWall);
                    case BEAM -> b.add(rightBeam);
                    case STICK -> b.add(rightStick);
                }
            }
            temp.put(a, b.build());
        }

        this.attachmentMap = Maps.newEnumMap(temp);
        this.particle = stick;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {
        List<BakedQuad> quads = new ArrayList<>();

        if (state != null && state.getBlock() instanceof HangingSignBlock hs) {
            //support & connections
            try {
                var a = state.getValue(HangingSignBlock.ATTACHMENT);
                attachmentMap.get(a).forEach(b -> quads.addAll(b.getQuads(state, side, rand)));

            } catch (Exception ignored) {
            }
            //connections

            //static sign
            try {
                boolean fancy = Boolean.TRUE.equals(data.get(ModBlockProperties.FANCY));
                if (!fancy) {

                    BakedModel model = ClientHelper.getModel(blockModelShaper.getModelManager(), ClientRegistry.HANGING_SIGNS_BLOCK_MODELS.get(hs.woodType));
                    if (model.getParticleIcon() instanceof MissingTextureAtlasSprite) return quads;
                    var signQuads = model.getQuads(state, side, rand);
                    boolean flipped = state.getValue(HangingSignBlock.AXIS) == Direction.Axis.X;
                    boolean ceiling = state.getValue(HangingSignBlock.ATTACHMENT) == ModBlockProperties.SignAttachment.CEILING;
                    if (flipped || ceiling) {
                        //TODO: move to renderUtils
                        for (BakedQuad q : signQuads) {
                            int[] v = Arrays.copyOf(q.getVertices(), q.getVertices().length);
                            Direction dir = q.getDirection();
                            if (flipped) {
                                VertexUtil.rotateVerticesY(v, q.getSprite(), Rotation.CLOCKWISE_90);
                                if (dir.getAxis() != Direction.Axis.Y) dir = dir.getClockWise();
                            }
                            if (ceiling) {
                                VertexUtil.moveVertices(v, 0, 0.125f, 0);
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
    public TextureAtlasSprite getBlockParticle(ExtraModelData data) {
        return particle.getParticleIcon();
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

package net.mehvahdjukaar.supplementaries.client.block_models;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.block.MimicBlock;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.client.ClientSpecialModelsManager;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WallLanternBakedModel implements CustomBakedModel {
    private final BakedModel support;
    private final BlockModelShaper blockModelShaper;
    private final ModelState rotation;

    public WallLanternBakedModel(BakedModel support, ModelState state) {
        this.support = support;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        this.rotation = state;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData data) {

        List<BakedQuad> quads = new ArrayList<>();

        BlockState mimic = null;
        try {
            mimic = data.get(ModBlockProperties.MIMIC);
        } catch (Exception ignored) {
        }

        //support
        try {

            var supportQuads = support.getQuads(state, side, rand);
            if (!supportQuads.isEmpty()) {
                if (mimic != null) {
                    var sprite = ClientSpecialModelsManager.getTextureForLantern(mimic.getBlock());
                    if (sprite != null) {
                        supportQuads = VertexUtil.swapSprite(supportQuads, sprite);
                    }
                }
                quads.addAll(supportQuads);
            }

        } catch (Exception ignored) {
        }

        //mimic
        try {
            boolean fancy = Boolean.TRUE.equals(data.get(ModBlockProperties.FANCY));

            if (!fancy) {
                if (mimic != null && !(mimic.getBlock() instanceof MimicBlock) && !mimic.isAir() && state != null) {
                    Direction dir = state.getValue(WallLanternBlock.FACING);
                    if (mimic.hasProperty(BlockStateProperties.FACING)) {
                        mimic = mimic.setValue(BlockStateProperties.FACING, dir);
                    } else if (mimic.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                        mimic = mimic.setValue(BlockStateProperties.HORIZONTAL_FACING, dir);
                    }

                    BakedModel model = ClientSpecialModelsManager.getWallLanternModel(blockModelShaper, mimic);

                    List<BakedQuad> mimicQuads = model.getQuads(mimic, side, rand);

                    for (BakedQuad q : mimicQuads) {
                        int[] v = Arrays.copyOf(q.getVertices(), q.getVertices().length);
                        Matrix4f mat = new Matrix4f();
                        mat.translate(0,2/16f,0);
                        mat.translate(dir.step().mul(-2/16f));

                        VertexUtil.transformVertices(v, mat);

                        quads.add(new BakedQuad(v, q.getTintIndex(),
                               Direction.rotate(mat, q.getDirection()), q.getSprite(), q.isShade()));
                    }
                }
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
    public TextureAtlasSprite getBlockParticle(ExtraModelData data) {
        BlockState mimic = data.get(ModBlockProperties.MIMIC);
        if (mimic != null && !mimic.isAir()) {

            BakedModel model = blockModelShaper.getBlockModel(mimic);
            try {
                return model.getParticleIcon();
            } catch (Exception ignored) {
            }

        }
        return support.getParticleIcon();
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

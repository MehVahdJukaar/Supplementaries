package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SugarBlock extends FallingBlock {

    public SugarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlock) {
        if (shouldDissolve(level, pos, blockState2)) {


            //level.addDestroyBlockEffect(blockPos, blockState);

            level.destroyBlock(pos, false);
        }
    }

    public void dissolve(ParticleEngine engine, ClientLevel level, BlockState state, BlockPos pos){


        VoxelShape voxelShape = state.getShape(level, pos);
        double d2 = 0.25;
        voxelShape.forAllBoxes((d, e, f, g, h, i) -> {
            double j = Math.min(1.0, g - d);
            double k = Math.min(1.0, h - e);
            double l = Math.min(1.0, i - f);
            int m = Math.max(2, Mth.ceil(j / 0.25));
            int n = Math.max(2, Mth.ceil(k / 0.25));
            int o = Math.max(2, Mth.ceil(l / 0.25));
            for (int p = 0; p < m; ++p) {
                for (int q = 0; q < n; ++q) {
                    for (int r = 0; r < o; ++r) {
                        double s = ((double)p + 0.5) / (double)m;
                        double t = ((double)q + 0.5) / (double)n;
                        double u = ((double)r + 0.5) / (double)o;
                        double v = s * j + d;
                        double w = t * k + e;
                        double x = u * l + f;
                        engine.add(new TerrainParticle(level, (double)pos.getX() + v, (double)pos.getY() + w, (double)pos.getZ() + x,
                                s - 0.5, t - 0.5, u - 0.5, state, pos));
                    }
                }
            }
        });

    }

    private static boolean shouldDissolve(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getFluidState().is(FluidTags.WATER);
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getMapColor(level, pos).col;
    }
}

package net.mehvahdjukaar.supplementaries.block.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class CrimsonLanternBlock extends EnhancedLanternBlock {

    public CrimsonLanternBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        worldIn.addParticle(ParticleTypes.FLAME, 0.5, 0.5, 0.5, 0.0D, 0.0D, 0.0D);
    }
}

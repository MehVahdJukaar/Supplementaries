package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.function.Supplier;

public class FloorCandleSkullBlock extends AbstractCandleSkullBlock {

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    public FloorCandleSkullBlock(Properties properties) {
        this(properties, () -> ParticleTypes.SMALL_FLAME);
    }

    public FloorCandleSkullBlock(Properties properties, Supplier<ParticleType<? extends ParticleOptions>> particle) {
        super(properties, particle);
        this.registerDefaultState(this.defaultBlockState().setValue(ROTATION, 0).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(ROTATION);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }


}

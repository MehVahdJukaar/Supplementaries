package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public class RedstoneIlluminatorBlock extends Block {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public RedstoneIlluminatorBlock(Properties properties) {
        super(properties.lightLevel((state) -> 15 - state.getValue(POWER)));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return state.getValue(POWER) != 15;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return 15 - state.getValue(POWER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, moving);
        if (!level.isClientSide) {
            int pow = level.getBestNeighborSignal(pos);
            level.setBlock(pos, state.setValue(POWER, Mth.clamp(pow, 0, 15)), 2);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(POWER,
                context.getLevel().getBestNeighborSignal(context.getClickedPos()));
    }
}
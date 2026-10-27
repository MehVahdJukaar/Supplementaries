package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

//yeah not really an origina idea
public class RedstoneLanternBlock extends LanternBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RedstoneLanternBlock(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> state.getValue(POWERED) ? 0 : 8));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HANGING, false).setValue(WATERLOGGED, false)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        return state.setValue(POWERED, hasNeighborSignal(context.getLevel(), context.getClickedPos(), state));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving) {
            for (Direction direction : Direction.values()) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }
        }
    }

    protected boolean hasNeighborSignal(Level level, BlockPos pos, BlockState state) {
        Direction toSupport = LanternBlock.getConnectedDirection(state).getOpposite();
        return level.hasSignal(pos.relative(toSupport), toSupport);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (state.getValue(POWERED) != this.hasNeighborSignal(level, pos, state) && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean powered = this.hasNeighborSignal(level, pos, state);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
        }
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return !state.getValue(POWERED) && side.getAxis().isHorizontal() ? 15 : 0;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) return;

        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        double side = (random.nextDouble() - 0.5) * 0.25;
        double x = pos.getX() + 0.5 + dir.getStepX() * 0.21 + dir.getStepZ() * side;
        double z = pos.getZ() + 0.5 + dir.getStepZ() * 0.21 + dir.getStepX() * side;
        double y = pos.getY() + (state.getValue(HANGING) ? 2 : 1) / 16d + random.nextDouble() * 5 / 16d;
        level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0, 0, 0);
    }
}

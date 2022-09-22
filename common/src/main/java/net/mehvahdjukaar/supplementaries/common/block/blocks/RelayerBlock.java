package net.mehvahdjukaar.supplementaries.common.block.blocks;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.supplementaries.client.particles.SugarParticle;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.QuiverLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public class RelayerBlock extends DirectionalBlock {

    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RelayerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(POWER, 0).setValue(POWERED, false));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER, POWERED, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var dir = context.getNearestLookingDirection();
        var state = this.defaultBlockState().setValue(FACING, dir);
        int p = getSignalInFront(context.getLevel(), context.getClickedPos(), dir);
        state = state.setValue(POWER, p).setValue(POWERED, p != 0);
        return state;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!worldIn.isClientSide) this.updatePower(state, worldIn, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        if (moving || pos.relative(state.getValue(FACING)).equals(fromPos)) this.updatePower(state, world, pos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide() && isMoving) {
            this.updatePower(state, level, pos);
        }
    }

    private void updatePower(BlockState state, Level level, BlockPos pos) {
        var dir = state.getValue(FACING);
        int pow = getSignalInFront(level, pos, dir);

        if (pow != state.getValue(POWER) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    private int getSignalInFront(Level level, BlockPos pos, Direction dir) {
        var behind = pos.relative(dir);
        int pow = level.getSignal(behind, dir);
        BlockState b = level.getBlockState(behind);
        if (b.getBlock() instanceof RedStoneWireBlock) {
            pow = Math.max(b.getValue(RedStoneWireBlock.POWER), pow);
        }else if(b.getBlock() instanceof DiodeBlock repeaterBlock){
            pow = Math.max(repeaterBlock.getOutputSignal(level, behind, b), pow);
        }else if(b.is(this)){
            pow = Math.max(b.getValue(POWER), pow);
        }
        return pow;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        Direction front = state.getValue(FACING);
        Direction back = front.getOpposite();

        int pow = getSignalInFront(level, pos, front);

        level.setBlock(pos, state.setValue(POWERED, pow != 0).setValue(POWER, Mth.clamp(pow, 0, 15)), 1 | 2 | 4);

        BlockPos blockPos = pos.relative(back);
        level.neighborChanged(blockPos, this, pos);
        level.updateNeighborsAtExceptFromFacing(blockPos, this, front);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getSignal(level, pos, direction);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.getValue(POWERED) && state.getValue(FACING) == direction) {
            return state.getValue(POWER);
        }
        return 0;
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        if (direction == null) return false;
        return direction == state.getValue(ObserverBlock.FACING);
    }

}
package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrystalDisplayBlock extends WaterBlock {

    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    protected static final VoxelShape SHAPE_EAST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);
    protected static final VoxelShape SHAPE_WEST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);

    public CrystalDisplayBlock(Properties properties) {
        super(properties.lightLevel((state) -> state.getValue(POWER) != 0 ? 6 : 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(ATTACHED, false)
                .setValue(FACING, Direction.NORTH).setValue(POWER, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, flag);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER, FACING, WATERLOGGED, ATTACHED);

    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, worldIn, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, level, pos);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    private void updatePower(BlockState state, Level world, BlockPos pos) {
        if (!world.isClientSide) {
            int power = Mth.clamp(world.getBestNeighborSignal(pos), 0, 15);
            Direction dir = state.getValue(FACING);

            state = state.setValue(ATTACHED, false);
            if (CommonConfigs.Redstone.CRYSTAL_DISPLAY_CHAINED.get()) {
                if (power > 10) {
                    // if its master
                    BlockPos slavePos = pos.relative(dir.getClockWise());
                    BlockState slaveState = world.getBlockState(slavePos);
                    if (slaveState.is(this) && slaveState.getValue(FACING) == dir
                            && !world.hasSignal(slavePos.relative(dir.getOpposite()), dir)) {
                        power %= 10;
                    }
                } else {
                    //if its slave
                    BlockPos masterPos = pos.relative(dir.getCounterClockWise());
                    if (world.getBlockState(masterPos).is(this) && world.getBlockState(masterPos).getValue(FACING) == dir &&
                            !world.hasSignal(pos.relative(dir.getOpposite()), dir)) {
                        int masterPower = world.getBestNeighborSignal(masterPos);
                        if (masterPower >= 10) {
                            power = Math.max(power, masterPower / 10);
                            state = state.setValue(ATTACHED, true);
                        }
                    }
                }
            }
            world.setBlock(pos, state.setValue(POWER, power), 3);
        }
    }

}
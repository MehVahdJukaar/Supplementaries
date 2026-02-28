package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.ClockBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundDisplayClockTimePacket;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClockBlock extends WaterBlock implements EntityBlock {

    protected static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 1, 16, 16, 16);
    protected static final VoxelShape SHAPE_SOUTH = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    protected static final VoxelShape SHAPE_EAST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);
    protected static final VoxelShape SHAPE_WEST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);

    protected static final VoxelShape SHAPE_NORTH_2 = Block.box(0, 0, 1, 16, 16, 15);
    protected static final VoxelShape SHAPE_SOUTH_2 = MthUtils.rotateVoxelShape(SHAPE_NORTH_2, Direction.SOUTH);
    protected static final VoxelShape SHAPE_EAST_2 = MthUtils.rotateVoxelShape(SHAPE_NORTH_2, Direction.EAST);
    protected static final VoxelShape SHAPE_WEST_2 = MthUtils.rotateVoxelShape(SHAPE_NORTH_2, Direction.WEST);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty HOUR = ModBlockProperties.HOUR;
    public static final BooleanProperty TWO_FACED = ModBlockProperties.TWO_FACED;

    public ClockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TWO_FACED, false)
                .setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
    }

    public static void displayCurrentHour(Level world, Player player) {
        if (player instanceof ServerPlayer sp)
            NetworkHelper.sendToClientPlayer(sp,
                    new ClientBoundDisplayClockTimePacket(
                            world.getDayTime(), !canReadTime(world)));
    }

    public static boolean canReadTime(Level level) {
        boolean naturalDim = (level.dimensionType().natural() || CommonConfigs.Tweaks.COMPASS_WORKS_IN_UNNATURAL_DIMENSIONS.get());
        return naturalDim ^ MiscUtils.FESTIVITY.isAprilsFool();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(this.asItem()) && !player.isSecondaryUseActive() && hitResult.getDirection() == state.getValue(FACING).getOpposite() && !state.getValue(TWO_FACED)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            displayCurrentHour(level, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockState oldState = level.getBlockState(context.getClickedPos());
        if (oldState.is(this)) {
            return oldState.setValue(TWO_FACED, true);
        } else {
            return super.getStateForPlacement(context)
                    .setValue(HOUR, ClockBlockTile.calculateHour((int) (level.getDayTime() % 24000)))
                    .setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(TWO_FACED)) {
            return switch (state.getValue(FACING)) {
                case SOUTH -> SHAPE_SOUTH_2;
                case EAST -> SHAPE_EAST_2;
                case WEST -> SHAPE_WEST_2;
                default -> SHAPE_NORTH_2;
            };
        } else {
            return switch (state.getValue(FACING)) {
                case SOUTH -> SHAPE_SOUTH;
                case EAST -> SHAPE_EAST;
                case WEST -> SHAPE_WEST;
                default -> SHAPE_NORTH;
            };
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ClockBlockTile(pPos, pState);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof ClockBlockTile) {
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HOUR, FACING, TWO_FACED);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        if (world.dimensionType().natural()) {
            if (world.getBlockEntity(pos) instanceof ClockBlockTile tile) {
                return tile.getPower();
            }
        }
        return 0;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide && level.getBlockEntity(pos) instanceof ClockBlockTile tile) {
            tile.updateInitialTime();
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return Utils.getTicker(pBlockEntityType, ModRegistry.CLOCK_BLOCK_TILE.get(), ClockBlockTile::tick);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return !useContext.isSecondaryUseActive() && useContext.getClickedFace() == state.getValue(FACING).getOpposite() &&
                useContext.getItemInHand().is(this.asItem()) && !state.getValue(TWO_FACED);
    }
}

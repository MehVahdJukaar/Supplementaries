package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.ClockBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
        int time = ((int) (world.getDayTime() + 6000) % 24000);
        int m = (int) (((time % 1000f) / 1000f) * 60);
        int h = time / 1000;
        String a = "";

        String ob = "";
        String br = "";
        if (!world.dimensionType().natural()) {
            time = world.random.nextInt(24000);
            ob += ChatFormatting.OBFUSCATED;
            br += ChatFormatting.RESET;
        }

        if (!ClientConfigs.Blocks.CLOCK_24H.get()) {
            a = time < 12000 ? " AM" : " PM";
            h = h % 12;
            if (h == 0) h = 12;
        }
        String text = ob + h + br + ":" + ob + ((m < 10) ? "0" : "") + m + br + a;

        player.displayClientMessage(Component.literal(text), true);

    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (player.getItemInHand(handIn).is(this.asItem()) && !player.isSecondaryUseActive() &&
                hit.getDirection() == state.getValue(FACING).getOpposite() && !state.getValue(TWO_FACED)) {
            return super.use(state, worldIn, pos, player, handIn, hit);
        }
        if (worldIn.isClientSide()) {
            displayCurrentHour(worldIn, player);
        }
        return InteractionResult.sidedSuccess(worldIn.isClientSide);
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
            tile.updateInitialTime(level, state, pos);
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

package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StickBlock extends WaterBlock implements IRotatable { // IRotationLockable,
    protected static final VoxelShape Y_AXIS_AABB = Block.box(7D, 0.0D, 7D, 9D, 16.0D, 9D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(7D, 7D, 0.0D, 9D, 9D, 16.0D);
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 7D, 7D, 16.0D, 9D, 9D);
    protected static final VoxelShape Y_Z_AXIS_AABB = Shapes.or(Y_AXIS_AABB, Z_AXIS_AABB);
    protected static final VoxelShape Y_X_AXIS_AABB = Shapes.or(Y_AXIS_AABB, X_AXIS_AABB);
    protected static final VoxelShape X_Z_AXIS_AABB = Shapes.or(X_AXIS_AABB, Z_AXIS_AABB);
    protected static final VoxelShape X_Y_Z_AXIS_AABB = Shapes.or(X_AXIS_AABB, Y_AXIS_AABB, Z_AXIS_AABB);

    public static final BooleanProperty AXIS_X = ModBlockProperties.AXIS_X;
    public static final BooleanProperty AXIS_Y = ModBlockProperties.AXIS_Y;
    public static final BooleanProperty AXIS_Z = ModBlockProperties.AXIS_Z;

    public static final Map<Direction.Axis, BooleanProperty> AXIS2PROPERTY =
            Map.of(Direction.Axis.X, AXIS_X, Direction.Axis.Y, AXIS_Y, Direction.Axis.Z, AXIS_Z);

    public StickBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE).setValue(AXIS_Y, true).setValue(AXIS_X, false).setValue(AXIS_Z, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS_X, AXIS_Y, AXIS_Z);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        boolean x = state.getValue(AXIS_X);
        boolean y = state.getValue(AXIS_Y);
        boolean z = state.getValue(AXIS_Z);
        return getStickShape(x, y, z);
    }

    public static VoxelShape getStickShape(boolean x, boolean y, boolean z) {
        if (x) {
            if (y) {
                if (z) return X_Y_Z_AXIS_AABB;
                return Y_X_AXIS_AABB;
            } else if (z) return X_Z_AXIS_AABB;
            return X_AXIS_AABB;
        }
        if (z) {
            if (y) return Y_Z_AXIS_AABB;
            return Z_AXIS_AABB;
        }
        return Y_AXIS_AABB;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        BooleanProperty axis = AXIS2PROPERTY.get(context.getClickedFace().getAxis());
        if (blockstate.is(this) || (CompatHandler.FARMERS_DELIGHT && FarmersDelightCompat.canAddStickToTomato(blockstate, axis))) {
            return blockstate.setValue(axis, true);
        } else {
            return super.getStateForPlacement(context).setValue(AXIS_Y, false).setValue(axis, true);
        }
    }


    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem())) {
            BooleanProperty axis = AXIS2PROPERTY.get(context.getClickedFace().getAxis());
            if (!state.getValue(axis)) return true;
        }
        return super.canBeReplaced(state, context);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return super.isPathfindable(state, pathComputationType);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            if (CommonConfigs.Building.FLAG_POLE.get()) {
                if (this != ModRegistry.STICK_BLOCK.get()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                if (level.isClientSide) return ItemInteractionResult.SUCCESS;
                else {
                    Direction moveDir = player.isShiftKeyDown() ? Direction.DOWN : Direction.UP;
                    findConnectedFlag(level, pos, Direction.UP, moveDir, 0);
                    findConnectedFlag(level, pos, Direction.DOWN, moveDir, 0);
                }
                return ItemInteractionResult.CONSUME;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static boolean isVertical(BlockState state) {
        return state.getValue(AXIS_Y) && !state.getValue(AXIS_X) && !state.getValue(AXIS_Z);
    }

    public static boolean findConnectedFlag(Level level, BlockPos pos, Direction searchDir, Direction moveDir, int it) {
        if (it > CommonConfigs.Building.FLAG_POLE_LENGTH.get()) return false;
        BlockState state = level.getBlockState(pos);
        Block b = state.getBlock();
        if (b == ModRegistry.STICK_BLOCK.get() && isVertical(state)) {
            return findConnectedFlag(level, pos.relative(searchDir), searchDir, moveDir, it + 1);
        } else if (b instanceof FlagBlock && it != 0) {
            BlockPos toPos = pos.relative(moveDir);
            BlockState stick = level.getBlockState(toPos);

            if (level.getBlockEntity(pos) instanceof FlagBlockTile tile && stick.getBlock() == ModRegistry.STICK_BLOCK.get() && isVertical(stick)) {

                level.setBlockAndUpdate(pos, stick);
                level.setBlockAndUpdate(toPos, state);

                CompoundTag tag = tile.saveWithoutMetadata(level.registryAccess());
                BlockEntity te = level.getBlockEntity(toPos);
                if (te != null) {
                    te.loadWithComponents(tag, level.registryAccess());
                }
                level.playSound(null, toPos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1F, 1.4F);
                return true;
            }
        }
        return false;
    }

    //quark
    //TODO: improve for multiple sticks
    //@Override
    public BlockState applyRotationLock(Level level, BlockPos blockPos, BlockState state, Direction dir, int half) {
        int i = 0;
        if (state.getValue(AXIS_X)) i++;
        if (state.getValue(AXIS_Y)) i++;
        if (state.getValue(AXIS_Z)) i++;
        if (i == 1) state.setValue(AXIS_Z, false).setValue(AXIS_X, false)
                .setValue(AXIS_Y, false).setValue(AXIS2PROPERTY.get(dir.getAxis()), true);
        return state;
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, LevelAccessor level, BlockPos pos, Rotation rotation, Direction axis, @org.jetbrains.annotations.Nullable Vec3 hit) {
        if(rotation == Rotation.CLOCKWISE_180)return Optional.empty();
        boolean x = state.getValue(AXIS_X);
        boolean y = state.getValue(AXIS_Y);
        boolean z = state.getValue(AXIS_Z);
        BlockState newState = switch (axis.getAxis()) {
            case Y -> state.setValue(AXIS_X, z).setValue(AXIS_Z, x);
            case X -> state.setValue(AXIS_Y, z).setValue(AXIS_Z, y);
            case Z -> state.setValue(AXIS_X, y).setValue(AXIS_Y, x);
        };
        if(newState != state)return Optional.of(newState);
        return Optional.empty();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return getRotatedState(state, null, null, rotation, Direction.UP, null).orElse(state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder pBuilder) {
        int i = 0;
        if (state.getValue(AXIS_X)) i++;
        if (state.getValue(AXIS_Y)) i++;
        if (state.getValue(AXIS_Z)) i++;
        return List.of(new ItemStack(this.asItem(), i));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor levelIn, BlockPos currentPos, BlockPos facingPos) {
        if (this == ModRegistry.STICK_BLOCK.get()) {
          //  if (facing == Direction.DOWN && !levelIn.isClientSide() && CompatHandler.FARMERS_DELIGHT) {
          //     FarmersDelightCompat.tryTomatoLogging(facingState, levelIn, facingPos,false);
          //  }
        }

        return super.updateShape(stateIn, facing, facingState, levelIn, currentPos, facingPos);
    }

}

package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.IAnalogRotatable;
import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class PulleyBlock extends RotatedPillarBlock implements EntityBlock, IRotatable, IAnalogRotatable {
    public static final EnumProperty<Winding> WINDING = ModBlockProperties.WINDING;
    public static final BooleanProperty FLIPPED = ModBlockProperties.FLIPPED;

    public PulleyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(WINDING, Winding.NONE).setValue(FLIPPED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WINDING, FLIPPED);
    }

    /**
     * simplified rotate method that only rotates pulley on its axis
     * if direction is null assumes default orientation
     *
     * @return true if rotation was successful
     */
    public boolean windPulley(BlockState state, BlockPos pos, LevelAccessor world, Rotation rot, @Nullable Direction dir) {
        Direction.Axis axis = state.getValue(AXIS);
        if (axis == Direction.Axis.Y) return false;
        if (dir == null) dir = axis == Direction.Axis.Z ? Direction.NORTH : Direction.WEST;
        return this.rotateOverAxis(state, world, pos, rot, dir, null).isPresent();
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation, Direction axis, Vec3 hit) {
        Direction.Axis myAxis = state.getValue(RotatedPillarBlock.AXIS);
        Direction.Axis targetAxis = axis.getAxis();
        if (myAxis == targetAxis) return Optional.of(state.cycle(FLIPPED));
        if (myAxis == Direction.Axis.X) {
            return Optional.of(state.setValue(AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.Z : Direction.Axis.Y));
        } else if (myAxis == Direction.Axis.Z) {
            return Optional.of(state.setValue(AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.X : Direction.Axis.Y));
        } else if (myAxis == Direction.Axis.Y) {
            return Optional.of(state.setValue(AXIS, targetAxis == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z));
        }
        return Optional.of(state);
    }

    //actually unwinds ropes & rotate connected
    @Override
    public void onRotated(BlockState newState, BlockState oldState, LevelAccessor world, BlockPos pos, Rotation originalRot, Direction axis, @Nullable Vec3 hit) {
        if (axis.getAxis().isHorizontal() && axis.getAxis() == oldState.getValue(AXIS)) {

            Rotation rot = originalRot;
            if (world.getBlockEntity(pos) instanceof PulleyBlockTile pulley) {
                if (axis.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
                    rot = rot.getRotated(Rotation.CLOCKWISE_180);
                pulley.windByRotation(rot);
            }
            //try turning connected
            BlockPos connectedPos = pos.relative(axis);
            BlockState connected = world.getBlockState(connectedPos);
            if (connected.is(this) && newState.getValue(AXIS) == connected.getValue(AXIS)) {
                this.windPulley(connected, connectedPos, world, originalRot, axis);
            }
        }
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof PulleyBlockTile tile) {
            if (player instanceof ServerPlayer sp) {
                if (!(player.isShiftKeyDown() && this.windPulley(state, pos, level, Rotation.COUNTERCLOCKWISE_90, null))) {
                    PlatHelper.openCustomMenu(sp, tile);
                    PiglinAi.angerNearbyPiglins(player, true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider mp ? mp : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PulleyBlockTile(pPos, pState);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }


    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity tile = level.getBlockEntity(pos);
        return tile != null && tile.triggerEvent(id, param);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @Override
    public boolean canRotateAnalog(BlockState state, Level level, BlockPos pos, Direction fromDir) {
        return CommonConfigs.Redstone.PULLEY_CONTINUOUS.get()
                && fromDir.getAxis().isHorizontal()
                && state.getValue(AXIS) == fromDir.getAxis();
    }

    @Override
    public void rotateAnalog(BlockState state, Level level, BlockPos pos, Direction fromDir, boolean ccw, float speed) {
        if (!canRotateAnalog(state, level, pos, fromDir)) return;
        boolean extending = ccw;
        forEachDaisyChained(state, level, pos, fromDir, tile -> tile.windByAnalogRotation(extending, speed));
    }

    public void windByCrank(BlockState state, Level level, BlockPos pos, Direction fromDir, boolean extending) {
        forEachDaisyChained(state, level, pos, fromDir, tile -> tile.windByCrank(extending));
    }

    private void forEachDaisyChained(BlockState state, Level level, BlockPos pos, Direction fromDir, Consumer<PulleyBlockTile> action) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        BlockState current = state;
        while (current.is(this) && current.getValue(AXIS) == fromDir.getAxis()) {
            if (level.getBlockEntity(cursor) instanceof PulleyBlockTile tile) {
                action.accept(tile);
            }
            cursor.move(fromDir);
            current = level.getBlockState(cursor);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof PulleyBlockTile tile) {
            tile.keepWindingWhileCranked();
        }
    }

}
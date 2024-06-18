package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PulleyBlock extends RotatedPillarBlock implements EntityBlock, IRotatable {
    public static final EnumProperty<Winding> TYPE = ModBlockProperties.WINDING;
    public static final BooleanProperty FLIPPED = ModBlockProperties.FLIPPED;

    public PulleyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(TYPE, Winding.NONE).setValue(FLIPPED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE, FLIPPED);
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
                if (axis.getAxisDirection() == Direction.AxisDirection.NEGATIVE) rot = rot.getRotated(Rotation.CLOCKWISE_180);
                pulley.rotateDirectly(rot);
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
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof PulleyBlockTile tile && tile.isAccessibleBy(player)) {
            if (player instanceof ServerPlayer sp) {
                if (!(player.isShiftKeyDown() && this.windPulley(state, pos, worldIn, Rotation.COUNTERCLOCKWISE_90, null)))
                    PlatHelper.openCustomMenu(sp, tile, pos);
            }
            return InteractionResult.sidedSuccess(worldIn.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level blockEntity, BlockPos pos) {
        BlockEntity tileEntity = blockEntity.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider mp ? mp : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PulleyBlockTile(pPos, pState);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof Container tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
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
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockUtil.addOptionalOwnership(placer, worldIn, pos);
    }
}
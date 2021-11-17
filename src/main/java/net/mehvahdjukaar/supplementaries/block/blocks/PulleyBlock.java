package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.api.IRotatable;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.BlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
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

import javax.annotation.Nullable;

public class PulleyBlock extends RotatedPillarBlock implements EntityBlock, IRotatable {
    public static final EnumProperty<Winding> TYPE = BlockProperties.WINDING;
    public static final BooleanProperty FLIPPED = BlockProperties.FLIPPED;

    public PulleyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(TYPE, Winding.NONE).setValue(FLIPPED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE, FLIPPED);
    }

    //rotates itself and pull up/down. unchecked
    //all methods here are called server side
    public boolean windPulley(BlockState state, BlockPos pos, Level world, Rotation rot, @Nullable Direction dir) {
        BlockState newState = state.cycle(FLIPPED);
        world.setBlockAndUpdate(pos, newState);
        if(dir == null) return false;
        return this.onRotated(newState, state, dir, rot, world, pos);
    }

    @Override
    public boolean onRotated(BlockState newState, BlockState oldState, Direction axis, Rotation rot, Level world, BlockPos pos) {
        boolean success = false;
        if(axis.getAxis().isHorizontal()) {

            if (world.getBlockEntity(pos) instanceof PulleyBlockTile pulley) {
                success = pulley.handleRotation(rot);

            }
            //try turning connected
            BlockPos connectedPos = pos.relative(axis);
            BlockState connected = world.getBlockState(connectedPos);
            if (connected.is(this) && newState.getValue(AXIS) == connected.getValue(AXIS)) {
                return this.windPulley(connected, connectedPos, world, rot, axis);
            }
        }
        return success;
    }

    @Override
    public BlockState rotateState(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation, Direction axis) {
        if(state.getValue(RotatedPillarBlock.AXIS) == axis.getAxis()) return state.cycle(FLIPPED);
        return state;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof PulleyBlockTile tile && tile.isAccessibleBy(player)) {
            if (player instanceof ServerPlayer) {
                if (!(player.isShiftKeyDown() && this.windPulley(state, pos, worldIn, Rotation.COUNTERCLOCKWISE_90, null)))
                    player.openMenu(tile);
            }
            return InteractionResult.sidedSuccess(worldIn.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider ? (MenuProvider) tileEntity : null;
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
        BlockUtils.addOptionalOwnership(placer, worldIn, pos);
    }
}
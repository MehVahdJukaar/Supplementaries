package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

public class GoldDoorBlock extends DoorBlock {

    public GoldDoorBlock(Properties builder) {
        super(builder, BlockSetType.GOLD);
    }

    public boolean canBeOpened(BlockState state) {
        return !state.getValue(POWERED);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (this.canBeOpened(state)) {
            tryOpenDoubleDoor(level, state, pos);

            state = state.cycle(OPEN);
            level.setBlock(pos, state, 10);
            this.playSound(player, level, pos, state.getValue(OPEN));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean hasPower = worldIn.hasNeighborSignal(pos) || worldIn.hasNeighborSignal(pos.relative(state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
        if (blockIn != this && hasPower != state.getValue(POWERED)) {
            worldIn.setBlock(pos, state.setValue(POWERED, hasPower), 2);
        }

    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) state.setValue(OPEN, false);
        return state;
    }

    //double door stuff

    @SuppressWarnings("ConstantConditions")
    public static void tryOpenDoubleDoor(Level world, BlockState state, BlockPos pos) {
        if ((CompatHandler.QUARK && QuarkCompat.isDoubleDoorEnabled() || CompatHandler.DOUBLEDOORS)) {
            Direction direction = state.getValue(DoorBlock.FACING);
            boolean isOpen = state.getValue(DoorBlock.OPEN);
            DoorHingeSide isMirrored = state.getValue(DoorBlock.HINGE);
            BlockPos mirrorPos = pos.relative(isMirrored == DoorHingeSide.RIGHT ? direction.getCounterClockWise() : direction.getClockWise());
            BlockPos doorPos = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? mirrorPos : mirrorPos.below();
            BlockState other = world.getBlockState(doorPos);
            if (other.getBlock() == state.getBlock() && other.getValue(DoorBlock.FACING) == direction && !other.getValue(DoorBlock.POWERED) &&
                    other.getValue(DoorBlock.OPEN) == isOpen && other.getValue(DoorBlock.HINGE) != isMirrored) {
                BlockState newState = other.cycle(DoorBlock.OPEN);
                world.setBlock(doorPos, newState, 10);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void tryOpenDoubleDoorKey(Level world, BlockState state, BlockPos pos, Player player, InteractionHand hand) {
        if ((CompatHandler.QUARK && QuarkCompat.isDoubleDoorEnabled() || CompatHandler.DOUBLEDOORS)) {
            Direction direction = state.getValue(DoorBlock.FACING);
            boolean isOpen = state.getValue(DoorBlock.OPEN);
            DoorHingeSide isMirrored = state.getValue(DoorBlock.HINGE);
            BlockPos mirrorPos = pos.relative(isMirrored == DoorHingeSide.RIGHT ? direction.getCounterClockWise() : direction.getClockWise());
            BlockPos doorPos = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? mirrorPos : mirrorPos.below();
            BlockState other = world.getBlockState(doorPos);
            if (other.getBlock() == state.getBlock() && other.getValue(DoorBlock.FACING) == direction && other.getValue(DoorBlock.OPEN) == isOpen && other.getValue(DoorBlock.HINGE) != isMirrored) {
                if (world.getBlockEntity(doorPos) instanceof KeyLockableTile keyLockableTile && (keyLockableTile.handleAction(player, hand, "door"))) {
                    BlockState newState = other.cycle(DoorBlock.OPEN);
                    world.setBlock(doorPos, newState, 10);
                }
            }
        }
    }
}

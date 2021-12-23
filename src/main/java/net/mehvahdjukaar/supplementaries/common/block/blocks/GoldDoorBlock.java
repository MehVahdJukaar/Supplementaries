package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkPlugin;
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
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

public class GoldDoorBlock extends DoorBlock {

    public GoldDoorBlock(Properties builder) {
        super(builder);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (state.getValue(POWERED)) return InteractionResult.PASS;

        tryOpenDoubleDoor(worldIn, state, pos);

        state = state.cycle(OPEN);
        worldIn.setBlock(pos, state, 10);
        worldIn.levelEvent(player, state.getValue(OPEN) ? this.getOpenSound() : this.getCloseSound(), pos, 0);
        return InteractionResult.sidedSuccess(worldIn.isClientSide);
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
        if (state == null) return state;
        return state.setValue(OPEN, false);
    }

    private int getCloseSound() {
        return 1011;
    }

    private int getOpenSound() {
        return 1005;
    }


    //double door stuff

    public static void tryOpenDoubleDoor(Level world, BlockState state, BlockPos pos) {
        if ((CompatHandler.quark && QuarkPlugin.isDoubleDoorEnabled() || CompatHandler.doubledoors)) {
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

    public static void tryOpenDoubleDoorKey(Level world, BlockState state, BlockPos pos, Player player, InteractionHand hand) {
        if ((CompatHandler.quark && QuarkPlugin.isDoubleDoorEnabled() || CompatHandler.doubledoors)) {
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

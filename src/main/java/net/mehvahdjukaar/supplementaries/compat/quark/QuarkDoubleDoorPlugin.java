package net.mehvahdjukaar.supplementaries.compat.quark;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class QuarkDoubleDoorPlugin {
    public static void openDoor(Level world, BlockState state, BlockPos pos) {

//        if (ModuleLoader.INSTANCE.isModuleEnabled(DoubleDoorOpeningModule.class)) {
//            Direction direction = state.getValue(DoorBlock.FACING);
//            boolean isOpen = state.getValue(DoorBlock.OPEN);
//            DoorHingeSide isMirrored = state.getValue(DoorBlock.HINGE);
//            BlockPos mirrorPos = pos.relative(isMirrored == DoorHingeSide.RIGHT ? direction.getCounterClockWise() : direction.getClockWise());
//            BlockPos doorPos = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? mirrorPos : mirrorPos.below();
//            BlockState other = world.getBlockState(doorPos);
//            if (other.getBlock() == state.getBlock() && other.getValue(DoorBlock.FACING) == direction && !other.getValue(DoorBlock.POWERED) &&
//                    other.getValue(DoorBlock.OPEN) == isOpen && other.getValue(DoorBlock.HINGE) != isMirrored) {
//                BlockState newState = other.cycle(DoorBlock.OPEN);
//                world.setBlock(doorPos, newState, 10);
//            }
//        }
    }

    public static void openDoorKey(Level world, BlockState state, BlockPos pos, Player player, InteractionHand hand) {

//        if (ModuleLoader.INSTANCE.isModuleEnabled(DoubleDoorOpeningModule.class)) {
//            Direction direction = state.getValue(DoorBlock.FACING);
//            boolean isOpen = state.getValue(DoorBlock.OPEN);
//            DoorHingeSide isMirrored = state.getValue(DoorBlock.HINGE);
//            BlockPos mirrorPos = pos.relative(isMirrored == DoorHingeSide.RIGHT ? direction.getCounterClockWise() : direction.getClockWise());
//            BlockPos doorPos = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? mirrorPos : mirrorPos.below();
//            BlockState other = world.getBlockState(doorPos);
//            if (other.getBlock() == state.getBlock() && other.getValue(DoorBlock.FACING) == direction && other.getValue(DoorBlock.OPEN) == isOpen && other.getValue(DoorBlock.HINGE) != isMirrored) {
//                TileEntity te = world.getBlockEntity(doorPos);
//                if (te instanceof KeyLockableTile &&
//                        (((KeyLockableTile) te).handleAction(player, hand, "door"))) {
//                    BlockState newState = other.cycle(DoorBlock.OPEN);
//                    world.setBlock(doorPos, newState, 10);
//                }
//            }
//        }
    }

}

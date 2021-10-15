package net.mehvahdjukaar.supplementaries.compat.quark;

import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.tweaks.module.DoubleDoorOpeningModule;

public class QuarkDoubleDoorPlugin {
    public static void openDoor(Level world, BlockState state, BlockPos pos) {

        if (ModuleLoader.INSTANCE.isModuleEnabled(DoubleDoorOpeningModule.class)) {
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

    public static void openDoorKey(World world, BlockState state, BlockPos pos, PlayerEntity player, Hand hand) {

        if (ModuleLoader.INSTANCE.isModuleEnabled(DoubleDoorOpeningModule.class)) {
            Direction direction = state.getValue(DoorBlock.FACING);
            boolean isOpen = state.getValue(DoorBlock.OPEN);
            DoorHingeSide isMirrored = state.getValue(DoorBlock.HINGE);
            BlockPos mirrorPos = pos.relative(isMirrored == DoorHingeSide.RIGHT ? direction.getCounterClockWise() : direction.getClockWise());
            BlockPos doorPos = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? mirrorPos : mirrorPos.below();
            BlockState other = world.getBlockState(doorPos);
            if (other.getBlock() == state.getBlock() && other.getValue(DoorBlock.FACING) == direction && other.getValue(DoorBlock.OPEN) == isOpen && other.getValue(DoorBlock.HINGE) != isMirrored) {
                TileEntity te = world.getBlockEntity(doorPos);
                if (te instanceof KeyLockableTile &&
                        (((KeyLockableTile) te).handleAction(player, hand, "door"))) {
                    BlockState newState = other.cycle(DoorBlock.OPEN);
                    world.setBlock(doorPos, newState, 10);
                }
            }
        }
    }

}

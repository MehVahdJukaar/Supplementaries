package net.mehvahdjukaar.supplementaries.plugins.quark;

import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.tweaks.module.DoubleDoorOpeningModule;

public class QuarkDoubleDoorPlugin {
    public static void openDoor(World world,BlockState state, BlockPos pos) {

        if (ModuleLoader.INSTANCE.isModuleEnabled(DoubleDoorOpeningModule.class)) {
            Direction direction = state.get(DoorBlock.FACING);
            boolean isOpen = state.get(DoorBlock.OPEN);
            DoorHingeSide isMirrored = state.get(DoorBlock.HINGE);
            BlockPos mirrorPos = pos.offset(isMirrored == DoorHingeSide.RIGHT ? direction.rotateYCCW() : direction.rotateY());
            BlockPos doorPos = state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? mirrorPos : mirrorPos.down();
            BlockState other = world.getBlockState(doorPos);
            if (other.getBlock() == state.getBlock() && other.get(DoorBlock.FACING) == direction && other.get(DoorBlock.OPEN) == isOpen && other.get(DoorBlock.HINGE) != isMirrored) {
                BlockState newState = other.func_235896_a_(DoorBlock.OPEN);
                world.setBlockState(doorPos, newState,10);
            }
        }
    }
    public static void openDoorKey(World world, BlockState state, BlockPos pos, PlayerEntity player, Hand hand) {

        if (ModuleLoader.INSTANCE.isModuleEnabled(DoubleDoorOpeningModule.class)) {
            Direction direction = state.get(DoorBlock.FACING);
            boolean isOpen = state.get(DoorBlock.OPEN);
            DoorHingeSide isMirrored = state.get(DoorBlock.HINGE);
            BlockPos mirrorPos = pos.offset(isMirrored == DoorHingeSide.RIGHT ? direction.rotateYCCW() : direction.rotateY());
            BlockPos doorPos = state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? mirrorPos : mirrorPos.down();
            BlockState other = world.getBlockState(doorPos);
            if (other.getBlock() == state.getBlock() && other.get(DoorBlock.FACING) == direction && other.get(DoorBlock.OPEN) == isOpen && other.get(DoorBlock.HINGE) != isMirrored) {
                TileEntity te = world.getTileEntity(doorPos);
                if (te instanceof KeyLockableTile &&
                        (((KeyLockableTile) te).handleAction(player, hand, "door"))) {
                    BlockState newState = other.func_235896_a_(DoorBlock.OPEN);
                    world.setBlockState(doorPos, newState,10);
                }
            }
        }
    }


}

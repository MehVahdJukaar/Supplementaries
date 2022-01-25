package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.api.IRotatable;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.VectorUtils;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.properties.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class BlockUtils {

    public static <T extends Comparable<T>, A extends Property<T>> BlockState replaceProperty(BlockState from, BlockState to, A property) {
        if (from.hasProperty(property)) {
            return to.setValue(property, from.getValue(property));
        }
        return to;
    }

    public static void addOptionalOwnership(LivingEntity placer, TileEntity tileEntity){
        if(ServerConfigs.cached.SERVER_PROTECTION && placer instanceof PlayerEntity) {
            ((IOwnerProtected) tileEntity).setOwner(placer.getUUID());
        }
    }

    public static void addOptionalOwnership(LivingEntity placer, World world, BlockPos pos){
        if(ServerConfigs.cached.SERVER_PROTECTION && placer instanceof PlayerEntity) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IOwnerProtected) {
                ((IOwnerProtected) tile).setOwner(placer.getUUID());
            }
        }
    }

    //rotation stuff
    //returns rotation direction axis which might be different that the clicked face
    public static Optional<Direction> tryRotatingBlockAndConnected(Direction face, boolean ccw, BlockPos targetPos, World level, Vector3d hit) {
        BlockState state = level.getBlockState(targetPos);
        if (state.getBlock() instanceof IRotatable) {
            return ((IRotatable)state.getBlock()).rotateOverAxis(state, level, targetPos, ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90, face, hit);
        }
        Optional<Direction> special = tryRotatingSpecial(face, ccw, targetPos, level, state, hit);
        if (special.isPresent()) return special;
        return tryRotatingBlock(face, ccw, targetPos, level, state, hit);
    }

    public static Optional<Direction> tryRotatingBlock(Direction face, boolean ccw, BlockPos targetPos, World level, Vector3d hit) {
        return tryRotatingBlock(face, ccw, targetPos, level, level.getBlockState(targetPos), hit);
    }

    // can be called on both sides
    // returns the direction onto which the block was actually rotated
    public static Optional<Direction> tryRotatingBlock(Direction dir, boolean ccw, BlockPos targetPos, World world, BlockState state, Vector3d hit) {

        //interface stuff
        if (state.getBlock() instanceof IRotatable) {
            return ((IRotatable)state.getBlock()).rotateOverAxis(state, world, targetPos, ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90, dir, hit);
        }

        Optional<BlockState> optional = getRotatedState(dir, ccw, targetPos, world, state);
        if (optional.isPresent()) {
            BlockState rotated = optional.get();

            if (rotated.canSurvive(world, targetPos)) {
                rotated = Block.updateFromNeighbourShapes(rotated, world, targetPos);

                if (rotated != state) {
                    if (world instanceof ServerWorld) {
                        world.setBlock(targetPos, rotated, 11);
                        //level.updateNeighborsAtExceptFromFacing(pos, newState.getBlock(), mydir.getOpposite());
                    }
                    return Optional.of(dir);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<BlockState> getRotatedState(Direction dir, boolean ccw, BlockPos targetPos, World world, BlockState state) {

        // is block blacklisted?
        if (isBlacklisted(state)) return Optional.empty();

        Rotation rot = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        Block block = state.getBlock();

        if (state.hasProperty(BlockProperties.FLIPPED)) {
            return Optional.of(state.cycle(BlockProperties.FLIPPED));
        }
        //horizontal facing blocks -easy
        if (dir.getAxis() == Direction.Axis.Y) {

            if (block == Blocks.CAKE) {
                int bites = state.getValue(CakeBlock.BITES);
                if (bites != 0) return Optional.of(ModRegistry.DIRECTIONAL_CAKE.get().defaultBlockState()
                        .setValue(CakeBlock.BITES, bites).rotate(world, targetPos, rot));
            }

            return Optional.of(state.rotate(world, targetPos, rot));
        }
        // 6 dir blocks blocks
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return Optional.of(rotateBlockStateOnAxis(state, dir, ccw));
        }
        // axis blocks
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis targetAxis = state.getValue(BlockStateProperties.AXIS);
            Direction.Axis myAxis = dir.getAxis();
            if (myAxis == Direction.Axis.X) {
                return Optional.of(state.setValue(BlockStateProperties.AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.Z : Direction.Axis.Y));
            } else if (myAxis == Direction.Axis.Z) {
                return Optional.of(state.setValue(BlockStateProperties.AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.X : Direction.Axis.Y));
            }
        }
        if (block instanceof StairsBlock) {
            Direction facing = state.getValue(StairsBlock.FACING);
            if (facing.getAxis() == dir.getAxis()) return Optional.empty();

            boolean flipped = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ^ ccw;
            Half half = state.getValue(StairsBlock.HALF);
            boolean top = half == Half.TOP;
            boolean positive = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE;

            if ((top ^ positive) ^ flipped) {
                half = top ? Half.BOTTOM : Half.TOP;
            } else {
                facing = facing.getOpposite();
            }

            return Optional.of(state.setValue(StairsBlock.HALF, half).setValue(StairsBlock.FACING, facing));
        }
        if (state.hasProperty(SlabBlock.TYPE)) {
            SlabType type = state.getValue(SlabBlock.TYPE);
            if (type == SlabType.DOUBLE) return Optional.empty();
            return Optional.of(state.setValue(SlabBlock.TYPE, type == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM));
        }
        if (state.hasProperty(TrapDoorBlock.HALF)) {
            return Optional.of(state.cycle(TrapDoorBlock.HALF));
        }
        return Optional.empty();
    }

    //check if it has facing property
    private static BlockState rotateBlockStateOnAxis(BlockState state, Direction axis, boolean ccw) {
        Vector3d targetNormal = VectorUtils.ItoD(state.getValue(BlockStateProperties.FACING).getNormal());
        Vector3d myNormal = VectorUtils.ItoD(axis.getNormal());
        if (!ccw) targetNormal = targetNormal.scale(-1);

        Vector3d rotated = myNormal.cross(targetNormal);
        // not on same axis, can rotate
        if (rotated != Vector3d.ZERO) {
            Direction newDir = Direction.getNearest(rotated.x(), rotated.y(), rotated.z());
            return state.setValue(BlockStateProperties.FACING, newDir);
        }
        return state;
    }

    private static boolean isBlacklisted(BlockState state) {
        // double blocks
        if (state.getBlock() instanceof BedBlock) return true;
        if (state.hasProperty(BlockStateProperties.CHEST_TYPE)) {
            if (state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE) return true;
        }
        // no piston bases
        if (state.hasProperty(BlockStateProperties.EXTENDED)) {
            if (state.getValue(BlockStateProperties.EXTENDED)) return true;
        }
        // nor piston arms
        if (state.hasProperty(BlockStateProperties.SHORT)) return true;

        return state.getBlock() == Blocks.END_PORTAL_FRAME;
    }


    private static Optional<Direction> tryRotatingSpecial(Direction face, boolean ccw, BlockPos pos, World level, BlockState state, Vector3d hit) {
        Block b = state.getBlock();
        Rotation rot = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        if (state.hasProperty(BlockStateProperties.ROTATION_16)) {
            int r = state.getValue(BlockStateProperties.ROTATION_16);
            r += (ccw ? -1 : 1);
            if (r < 0) r += 16;
            r = r % 16;
            level.setBlock(pos, state.setValue(BlockStateProperties.ROTATION_16, r), 2);
            return Optional.of(Direction.UP);
        }

        if (state.hasProperty(BlockStateProperties.EXTENDED) && state.getValue(BlockStateProperties.EXTENDED)) {
            if (state.hasProperty(PistonHeadBlock.FACING)) {
                BlockState newBase = rotateBlockStateOnAxis(state, face, ccw);
                BlockPos headPos = pos.relative(state.getValue(PistonHeadBlock.FACING));
                if (level.getBlockState(headPos).hasProperty(PistonHeadBlock.SHORT)) {
                    BlockPos newHeadPos = pos.relative(newBase.getValue(PistonHeadBlock.FACING));
                    if (level.getBlockState(newHeadPos).getMaterial().isReplaceable()) {

                        level.setBlock(newHeadPos, rotateBlockStateOnAxis(level.getBlockState(headPos), face, ccw), 2);
                        level.setBlock(pos, newBase, 2);
                        level.removeBlock(headPos, false);
                        return Optional.of(face);
                    }
                }
                return Optional.empty();
            }
        }
        if (state.hasProperty(BlockStateProperties.SHORT)) {
            if (state.hasProperty(PistonHeadBlock.FACING)) {
                BlockState newBase = rotateBlockStateOnAxis(state, face, ccw);
                BlockPos headPos = pos.relative(state.getValue(PistonHeadBlock.FACING).getOpposite());
                if (level.getBlockState(headPos).hasProperty(BlockStateProperties.EXTENDED)) {
                    BlockPos newHeadPos = pos.relative(newBase.getValue(PistonHeadBlock.FACING).getOpposite());
                    if (level.getBlockState(newHeadPos).getMaterial().isReplaceable()) {

                        level.setBlock(newHeadPos, rotateBlockStateOnAxis(level.getBlockState(headPos), face, ccw), 2);
                        level.setBlock(pos, newBase, 2);
                        level.removeBlock(headPos, false);
                        return Optional.of(face);
                    }
                }
                return Optional.empty();
            }
        }
        if (b instanceof BedBlock && face.getAxis() == Direction.Axis.Y) {
            BlockState newBed = state.rotate(level, pos, rot);
            BlockPos oldPos = pos.relative(getConnectedBedDirection(state));
            BlockPos targetPos = pos.relative(getConnectedBedDirection(newBed));
            if (level.getBlockState(targetPos).getMaterial().isReplaceable()) {
                level.setBlock(targetPos, level.getBlockState(oldPos).rotate(level, oldPos, rot), 2);
                level.setBlock(pos, newBed, 2);
                level.removeBlock(oldPos, false);
                return Optional.of(face);
            }
            return Optional.empty();
        }
        if (b instanceof ChestBlock && face.getAxis() == Direction.Axis.Y) {
            if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockState newChest = state.rotate(level, pos, rot);
                BlockPos oldPos = pos.relative(ChestBlock.getConnectedDirection(state));
                BlockPos targetPos = pos.relative(ChestBlock.getConnectedDirection(newChest));
                if (level.getBlockState(targetPos).getMaterial().isReplaceable()) {
                    BlockState connectedNewState = level.getBlockState(oldPos).rotate(level, oldPos, rot);
                    level.setBlock(targetPos, connectedNewState, 2);
                    level.setBlock(pos, newChest, 2);

                    TileEntity tile = level.getBlockEntity(oldPos);
                    if (tile != null) {
                        CompoundNBT tag = tile.save(new CompoundNBT());

                        TileEntity target = TileEntity.loadStatic(level.getBlockState(targetPos), tag);
                        if (target != null) {
                            level.setBlockEntity(targetPos, target);
                            target.clearCache();
                        }

                        tile.setRemoved();
                    }

                    level.setBlockAndUpdate(oldPos, Blocks.AIR.defaultBlockState());
                    return Optional.of(face);
                }
            }
            return Optional.empty();
        }
        if (DoorBlock.isWoodenDoor(state)) {
            //TODO: add
            //level.setBlockAndUpdate(state.rotate(level, pos, rot));

        }
        return Optional.empty();
    }

    private static Direction getConnectedBedDirection(BlockState bedState) {
        BedPart part = bedState.getValue(BedBlock.PART);
        Direction dir = bedState.getValue(BedBlock.FACING);
        return part == BedPart.FOOT ? dir : dir.getOpposite();
    }

    //TODO: add rotation vertical slabs & doors
}

package net.mehvahdjukaar.supplementaries.common.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BlockUtil {

    public static <T extends Comparable<T>, A extends Property<T>> BlockState replaceProperty(BlockState from, BlockState to, A property) {
        if (from.hasProperty(property)) {
            return to.setValue(property, from.getValue(property));
        }
        return to;
    }

    public static <T extends BlockEntity & IOwnerProtected> void addOptionalOwnership(LivingEntity placer, T tileEntity) {
        if (CommonConfigs.General.SERVER_PROTECTION.get() && placer instanceof Player) {
            tileEntity.setOwner(placer.getUUID());
        }
    }

    public static void addOptionalOwnership(LivingEntity placer, Level world, BlockPos pos) {
        if (CommonConfigs.General.SERVER_PROTECTION.get() && placer instanceof Player) {
            if (world.getBlockEntity(pos) instanceof IOwnerProtected tile) {
                tile.setOwner(placer.getUUID());
            }
        }
    }

    //rotation stuff
    //returns rotation direction axis which might be different that the clicked face

    /**
     * A more powerful rotate method. Not only rotates the block itself but tries to rotate its connected ones aswell like chests
     * If it fails it will also try to rotate using the Y axis. Used by wrench
     *
     * @return Optional face on which it was rotated
     */
    public static Optional<Direction> tryRotatingBlockAndConnected(Direction face, boolean ccw, BlockPos targetPos, Level level, Vec3 hit) {
        BlockState state = level.getBlockState(targetPos);
        if (state.getBlock() instanceof IRotatable rotatable) {
            return rotatable.rotateOverAxis(state, level, targetPos, ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90, face, hit);
        }
        Optional<Direction> special = tryRotatingSpecial(face, ccw, targetPos, level, state, hit);
        if (special.isPresent()) return special;

        var ret = tryRotatingBlock(face, ccw, targetPos, level, state, hit);

        //try again using up direction if previously failed. Doing this cause many people dont even realize you have to click on the axis you want to rotate
        if (ret.isEmpty()) {
            ret = tryRotatingBlock(Direction.UP, ccw, targetPos, level, level.getBlockState(targetPos), hit);
        }
        return ret;
    }

    public static Optional<Direction> tryRotatingBlock(Direction face, boolean ccw, BlockPos targetPos, Level level, Vec3 hit) {
        return tryRotatingBlock(face, ccw, targetPos, level, level.getBlockState(targetPos), hit);
    }

    // can be called on both sides
    // returns the direction onto which the block was actually rotated
    public static Optional<Direction> tryRotatingBlock(Direction dir, boolean ccw, BlockPos targetPos, Level level, BlockState state, Vec3 hit) {

        // container shuffle stuff
        if (!level.isClientSide && CommonConfigs.Redstone.TURN_TABLE_SHUFFLE.get() &&
                dir.getAxis() != Direction.Axis.Y && state.hasProperty(BarrelBlock.FACING)) {
            if (!state.is(ModTags.TURN_TABLE_CANT_SHUFFLE) && level.getBlockEntity(targetPos) instanceof Container c) {
                shuffleContainerContent(c, level);
                //continue normally below
            }
        }

        //interface stuff
        if (state.getBlock() instanceof IRotatable rotatable) {
            return rotatable.rotateOverAxis(state, level, targetPos, ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90, dir, hit);
        }
        Optional<BlockState> optional = getRotatedState(dir, ccw, targetPos, level, state);
        if (optional.isPresent()) {
            BlockState rotated = optional.get();

            if (rotated.canSurvive(level, targetPos)) {
                rotated = Block.updateFromNeighbourShapes(rotated, level, targetPos);

                if (rotated != state) {
                    if (level instanceof ServerLevel) {
                        level.setBlock(targetPos, rotated, 11);
                        //also needs to call neighbor changed
                        //copied from rail. calls neighbor updated. we need both this and updatefromneighbor
                        level.neighborChanged(rotated, targetPos, rotated.getBlock(), targetPos, false);
                    }
                    return Optional.of(dir);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<BlockState> getRotatedState(Direction dir, boolean ccw, BlockPos targetPos, Level world, BlockState state) {

        // is block blacklisted?
        if (isBlacklisted(state)) return Optional.empty();

        Rotation rot = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        Block block = state.getBlock();

        if (state.hasProperty(ModBlockProperties.FLIPPED)) {
            return Optional.of(state.cycle(ModBlockProperties.FLIPPED));
        }
        //horizontal facing blocks -easy
        if (dir.getAxis() == Direction.Axis.Y) {

            if (block == Blocks.CAKE) {
                var dc = CompatObjects.DIRECTIONAL_CAKE.get();
                if (dc != null) {
                    int bites = state.getValue(CakeBlock.BITES);
                    if (bites != 0) return Optional.of(ForgeHelper.rotateBlock(
                            dc.defaultBlockState().setValue(CakeBlock.BITES, bites), world, targetPos, rot));
                }
            }

            BlockState rotated = ForgeHelper.rotateBlock(state, world, targetPos, rot);
            //also hardcoding vanilla rotation methods cause some mods just dont implement rotate methods for their blocks
            //this could cause problems for mods that do and dont want it to be rotated but those should really be added to the blacklist
            if (rotated == state) {
                rotated = rotateVerticalStandard(state, rotated, rot);
            }
            return Optional.of(rotated);
        } else if (state.hasProperty(BlockStateProperties.ATTACH_FACE) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            var res = getRotatedHorizontalFaceBlock(state, dir, ccw);
            if (res.isPresent()) return res;
        }
        // 6 dir blocks blocks
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return getRotatedDirectionalBlock(state, dir, ccw);
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
        if (block instanceof StairBlock) {
            return getRotatedStairs(state, dir, ccw);
        }
        if (state.hasProperty(SlabBlock.TYPE)) {
            SlabType type = state.getValue(SlabBlock.TYPE);
            if (type == SlabType.DOUBLE) return Optional.empty();
            return Optional.of(state.setValue(SlabBlock.TYPE, type == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM));
        }
        if (state.hasProperty(TrapDoorBlock.HALF)) {
            return Optional.of(state.cycle(TrapDoorBlock.HALF));
        }
        if (CompatHandler.QUARK) {
            WoodType type = WoodTypeRegistry.INSTANCE.getBlockTypeOf(block);
            if (type != null && type.planks == block) {
                var verticalPlanks = type.getBlockOfThis("quark:vertical_planks");
                if (verticalPlanks != null) return Optional.of(verticalPlanks.defaultBlockState());
            }
        }
        return Optional.empty();
    }

    private static BlockState rotateVerticalStandard(BlockState state, BlockState rotated, Rotation rot) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            rotated = state.setValue(BlockStateProperties.FACING,
                    rot.rotate(state.getValue(BlockStateProperties.FACING)));
        } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            rotated = state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                    rot.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
        } else if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            rotated = RotatedPillarBlock.rotatePillar(state, rot);
        } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            rotated = state.cycle(BlockStateProperties.HORIZONTAL_AXIS);
        }
        return rotated;
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

        return state.is(ModTags.ROTATION_BLACKLIST);
    }


    private static Optional<Direction> tryRotatingSpecial(Direction face, boolean ccw, BlockPos pos, Level level, BlockState state, Vec3 hit) {
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

        if (state.hasProperty(BlockStateProperties.SHORT) || state.hasProperty(PistonBaseBlock.EXTENDED) && state.hasProperty(PistonBaseBlock.FACING)) {
            var opt = rotatePistonHead(state, pos, level, face, ccw);
            if (opt.isPresent()) return opt;
        }
        if (b instanceof BedBlock) {
            return rotateBedBlock(face, pos, level, state, rot);
        }
        if (b instanceof ChestBlock) {
            return rotateDoubleChest(face, pos, level, state, rot);
        }
        if (DoorBlock.isWoodenDoor(state)) {
            //TODO: add
            //level.setBlockAndUpdate(state.rotate(level, pos, rot));
        }
        if (CompatHandler.QUARK && QuarkCompat.tryRotateStool(level, state, pos)) {
            return Optional.of(face);
        }

        return Optional.empty();
    }

    private static void shuffleContainerContent(Container c, Level level) {
        ObjectArrayList<ItemStack> content = ObjectArrayList.of();
        for (int i = 0; i < c.getContainerSize(); i++) {
            content.add(c.removeItemNoUpdate(i));
        }
        Util.shuffle(content, level.random);

        for (int i = 0; i < content.size(); i++) {
            c.setItem(i, content.get(i));
        }
        c.setChanged();
    }


    //TODO: add rotation vertical slabs & doors


    //rotations per block

    public static Optional<BlockState> getRotatedStairs(BlockState state, Direction axis, boolean ccw) {
        Direction facing = state.getValue(StairBlock.FACING);
        if (facing.getAxis() == axis.getAxis()) return Optional.empty();

        boolean flipped = axis.getAxisDirection() == Direction.AxisDirection.POSITIVE ^ ccw;
        Half half = state.getValue(StairBlock.HALF);
        boolean top = half == Half.TOP;
        boolean positive = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE;

        if ((top ^ positive) ^ flipped) {
            half = top ? Half.BOTTOM : Half.TOP;
        } else {
            facing = facing.getOpposite();
        }

        return Optional.of(state.setValue(StairBlock.HALF, half).setValue(StairBlock.FACING, facing));
    }

    //check if it has facing property
    public static Optional<BlockState> getRotatedDirectionalBlock(BlockState state, Direction axis, boolean ccw) {
        Vec3 targetNormal = MthUtils.V3itoV3(state.getValue(BlockStateProperties.FACING).getNormal());
        Vec3 myNormal = MthUtils.V3itoV3(axis.getNormal());
        if (!ccw) targetNormal = targetNormal.scale(-1);

        Vec3 rotated = myNormal.cross(targetNormal);
        // not on same axis, can rotate
        if (!rotated.equals(Vec3.ZERO)) {
            Direction newDir = Direction.getNearest(rotated.x(), rotated.y(), rotated.z());
            return Optional.of(state.setValue(BlockStateProperties.FACING, newDir));
        }
        return Optional.empty();
    }

    public static Optional<BlockState> getRotatedHorizontalFaceBlock(BlockState original, Direction axis, boolean ccw) {

        Direction facingDir = original.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (facingDir.getAxis() == axis.getAxis()) return Optional.empty();

        var face = original.getValue(BlockStateProperties.ATTACH_FACE);
        return Optional.of(switch (face) {
            case FLOOR -> original.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, ccw ? axis.getClockWise() : axis.getCounterClockWise());
            case CEILING -> original.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, !ccw ? axis.getClockWise() : axis.getCounterClockWise());
            case WALL -> {
                ccw = ccw ^ (axis.getAxisDirection() != Direction.AxisDirection.POSITIVE);
                yield original.setValue(BlockStateProperties.ATTACH_FACE,
                        (facingDir.getAxisDirection() == Direction.AxisDirection.POSITIVE) ^ ccw ? AttachFace.CEILING : AttachFace.FLOOR);
            }
        });
    }

    private static Optional<Direction> rotateDoubleChest(Direction face, BlockPos pos, Level level, BlockState state, Rotation rot) {
        if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockState newChest = ForgeHelper.rotateBlock(state, level, pos, rot);
            BlockPos oldPos = pos.relative(ChestBlock.getConnectedDirection(state));
            BlockPos targetPos = pos.relative(ChestBlock.getConnectedDirection(newChest));
            if (level.getBlockState(targetPos).canBeReplaced()) {
                BlockState connectedNewState = ForgeHelper.rotateBlock(level.getBlockState(oldPos), level, oldPos, rot);
                level.setBlock(targetPos, connectedNewState, 2);
                level.setBlock(pos, newChest, 2);

                BlockEntity tile = level.getBlockEntity(oldPos);
                if (tile != null) {
                    CompoundTag tag = tile.saveWithoutMetadata();
                    if (level.getBlockEntity(targetPos) instanceof ChestBlockEntity newChestTile) {
                        newChestTile.load(tag);
                    }
                    tile.setRemoved();
                }

                level.setBlockAndUpdate(oldPos, Blocks.AIR.defaultBlockState());
                return Optional.of(face);
            }
        }
        return Optional.empty();
    }


    public static Optional<Direction> rotatePistonHead(BlockState state, BlockPos pos, Level level, Direction face, boolean ccw) {
        Optional<BlockState> newBase = getRotatedDirectionalBlock(state, face, ccw);
        if (newBase.isEmpty()) return Optional.empty();
        BlockState newBaseState = newBase.get();
        BlockPos oldHeadPos;
        BlockState oldHead;
        BlockPos newHeadPos;
        if (state.hasProperty(PistonHeadBlock.SHORT)) {
            oldHeadPos = pos.relative(state.getValue(PistonHeadBlock.FACING).getOpposite());
            oldHead = level.getBlockState(oldHeadPos);
            if (!oldHead.hasProperty(PistonBaseBlock.EXTENDED)) return Optional.empty();
            newHeadPos = pos.relative(newBaseState.getValue(PistonHeadBlock.FACING).getOpposite());
        } else if (state.hasProperty(PistonBaseBlock.EXTENDED)) {
            oldHeadPos = pos.relative(state.getValue(PistonHeadBlock.FACING));
            oldHead = level.getBlockState(oldHeadPos);
            if (!oldHead.hasProperty(PistonHeadBlock.SHORT)) return Optional.empty();
            newHeadPos = pos.relative(newBaseState.getValue(PistonBaseBlock.FACING));
        } else return Optional.empty();

        if (level.getBlockState(newHeadPos).canBeReplaced()) {
            Optional<BlockState> rotatedHead = getRotatedDirectionalBlock(oldHead, face, ccw);
            if (rotatedHead.isPresent()) {
                level.setBlock(newHeadPos, rotatedHead.get(), 2);
                level.setBlock(pos, newBaseState, 2);
                level.removeBlock(oldHeadPos, false);
                return Optional.of(face);
            }
        }

        return Optional.empty();
    }


    public static @NotNull Optional<Direction> rotateBedBlock(Direction face, BlockPos pos, Level level, BlockState state, Rotation rot) {
        BlockState newBed = ForgeHelper.rotateBlock(state, level, pos, rot);
        BlockPos oldPos = pos.relative(getConnectedBedDirection(state));
        BlockPos targetPos = pos.relative(getConnectedBedDirection(newBed));
        if (level.getBlockState(targetPos).canBeReplaced()) {
            level.setBlock(targetPos, ForgeHelper.rotateBlock(level.getBlockState(oldPos), level, oldPos, rot), 2);
            level.setBlock(pos, newBed, 2);
            level.removeBlock(oldPos, false);
            return Optional.of(face);
        }
        return Optional.empty();
    }

    public static Direction getConnectedBedDirection(BlockState bedState) {
        BedPart part = bedState.getValue(BedBlock.PART);
        Direction dir = bedState.getValue(BedBlock.FACING);
        return part == BedPart.FOOT ? dir : dir.getOpposite();
    }
}

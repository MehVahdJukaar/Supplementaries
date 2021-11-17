package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.api.IRotatable;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.common.VectorUtils;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockUtils {

    public static <T extends BlockEntity & IOwnerProtected> void addOptionalOwnership(LivingEntity placer, T tileEntity) {
        if (ServerConfigs.cached.SERVER_PROTECTION && placer instanceof Player) {
            tileEntity.setOwner(placer.getUUID());
        }
    }

    public static void addOptionalOwnership(LivingEntity placer, Level world, BlockPos pos) {
        if (ServerConfigs.cached.SERVER_PROTECTION && placer instanceof Player) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IOwnerProtected) {
                ((IOwnerProtected) tile).setOwner(placer.getUUID());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(BlockEntityType<A> type, BlockEntityType<E> targetType, BlockEntityTicker<? super E> ticker) {
        return targetType == type ? (BlockEntityTicker<A>)ticker : null;
    }

    public static class PlayerLessContext extends BlockPlaceContext {
        public PlayerLessContext(Level worldIn, @Nullable Player playerIn, InteractionHand handIn, ItemStack stackIn, BlockHitResult rayTraceResultIn) {
            super(worldIn, playerIn, handIn, stackIn, rayTraceResultIn);
        }
    }

    //rotation stuff

    public static boolean tryRotatingBlock(Direction face, boolean ccw, BlockPos targetPos, Level level) {
        return tryRotatingBlock(face,ccw,targetPos,level, level.getBlockState(targetPos));
    }

    // can be called on both sides
    public static boolean tryRotatingBlock(Direction dir, boolean ccw, BlockPos targetPos, Level world, BlockState state){
        Optional<BlockState> optional = getRotatedState(dir, ccw, targetPos, world, state);
        if(optional.isPresent()){
            BlockState rotated = optional.get();
            Block b = state.getBlock();
            if(rotated != state || (b instanceof IRotatable r && r.alwaysRotateOverAxis(state, dir))){

                if (rotated.canSurvive(world, targetPos)) {
                    if(world instanceof ServerLevel serverLevel) {
                        rotated = Block.updateFromNeighbourShapes(rotated, world, targetPos);
                        world.setBlock(targetPos, rotated, 11);
                        //level.updateNeighborsAtExceptFromFacing(pos, newState.getBlock(), mydir.getOpposite());
                    }

                    if(b instanceof IRotatable r)r.onRotated(rotated,state, dir,
                            ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90, world, targetPos);

                    return true;
                }
            }
        }
        return false;
    }

    public static Optional<BlockState> getRotatedState(Direction dir, boolean ccw, BlockPos targetPos, Level world, BlockState state){

        // is block blacklisted?
        if (isBlacklisted(state)) return Optional.empty();

        Rotation rot = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;

        Block block = state.getBlock();

        if(block instanceof IRotatable rotatable){
            return Optional.of(rotatable.rotateState(state, world, targetPos, rot, dir));
        }
        //horizontal facing blocks -easy
        else if (dir.getAxis() == Direction.Axis.Y) {
            return Optional.of(state.rotate(world, targetPos, rot));
        }
        // 6 dir blocks blocks
        else if (state.hasProperty(BlockStateProperties.FACING)) {
            Vec3 targetNormal = VectorUtils.ItoD(state.getValue(BlockStateProperties.FACING).getNormal());
            Vec3 myNormal = VectorUtils.ItoD(dir.getNormal());
            if (!ccw) targetNormal = targetNormal.scale(-1);

            Vec3 rotated = myNormal.cross(targetNormal);
            if (rotated == Vec3.ZERO) {
                // same axis, can't rotate
                return Optional.empty();
            }
            Direction newDir = Direction.getNearest(rotated.x(), rotated.y(), rotated.z());
            return Optional.of(state.setValue(BlockStateProperties.FACING, newDir));
        }
        // axis blocks
        else if (state.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis targetAxis = state.getValue(BlockStateProperties.AXIS);
            Direction.Axis myAxis = dir.getAxis();
            if (myAxis == targetAxis) {
                // same axis, can't rotate
                return Optional.empty();
            } else if (myAxis == Direction.Axis.X) {
                return Optional.of(state.setValue(BlockStateProperties.AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.Z : Direction.Axis.Y));
            } else if (myAxis == Direction.Axis.Z) {
                return Optional.of(state.setValue(BlockStateProperties.AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.X : Direction.Axis.Y));
            }
        }
        return Optional.empty();
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
}

package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface IRotatable {
    /**
     * returns the block state rotated over an axis. Do not use this to actually rotate the block. use rotateOverAxis or onRotated instead
     * @param state original block state
     * @param world world
     * @param pos position
     * @param rotation rotation
     * @param axis rotation axis
     * @return rotated state. Optional.empty() if block hasn't been rotated. you can return the same blockstate for tile entities that have other meaning of rotation
     */
    Optional<BlockState> getRotatedState(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation,
                               Direction axis, @Nullable Vec3 hit);
    /**
     * actually rotates the block. Overrides if you need more control over what rotating actually means
     * @param state original block state
     * @param world world
     * @param pos position
     * @param rotation rotation
     * @param axis rotation axis
     * @return the direction onto which the block was actually rotated. Optional.empty() if it wasn't rotated at all
     */
    default Optional<Direction> rotateOverAxis(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation,
                                   Direction axis, @Nullable Vec3 hit){

        Optional<BlockState> optional = this.getRotatedState(state, world, pos,rotation,axis, hit);
        if (optional.isPresent()) {
            BlockState rotated = optional.get();

            if (rotated.canSurvive(world, pos)) {
                rotated = Block.updateFromNeighbourShapes(rotated, world, pos);

                if (world instanceof ServerLevel) {
                    world.setBlock(pos, rotated, 11);
                    //level.updateNeighborsAtExceptFromFacing(pos, newState.getBlock(), mydir.getOpposite());
                }
                this.onRotated(rotated, state, world, pos, rotation, axis, hit);
                return Optional.of(axis);
            }
        }
        return Optional.empty();
    }

    /**
     * performs extra actions after it has been rotated
     * @param newState blockstate
     * @param oldState old blockstate
     * @param axis axis of rotation
     */
    default void onRotated(BlockState newState, BlockState oldState, LevelAccessor world, BlockPos pos, Rotation rotation,
                                         Direction axis, @Nullable Vec3 hit){
    }




}

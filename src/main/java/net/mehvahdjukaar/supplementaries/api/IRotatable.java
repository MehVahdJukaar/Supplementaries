package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public interface IRotatable {
    /**
     * returns the block state rotated over an axis
     * @param state original block state
     * @param world world
     * @param pos position
     * @param rotation rotation
     * @param axis rotation axis
     * @return rotated state
     */
    BlockState rotateState(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation, Direction axis);

    /**
     * rotation call back. should be called after a rotated block is placed
     * @param newState new rotated state
     * @param oldState old state
     * @param axis axis of rotation
     * @param world level
     * @param pos position
     * @return if the action performed was successful
     */
    boolean onRotated(BlockState newState, BlockState oldState, Direction axis, Rotation rotation, Level world, BlockPos pos);

    /**
     * if this should always be considered "rotated" onto an axis even when no blockstate change is detected
     * @param state blockstate
     * @param axis axis of rotation
     * @return always considered rotated
     */
    default boolean alwaysRotateOverAxis(BlockState state, Direction axis){
        return false;
    };




}

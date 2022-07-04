package net.mehvahdjukaar.supplementaries.common.block.util;

import net.minecraft.world.level.block.state.BlockState;

public interface IBlockHolder {

    BlockState getHeldBlock(int index);

    boolean setHeldBlock(BlockState state, int index);

    default BlockState getHeldBlock() {
        return getHeldBlock(0);
    }

    default boolean setHeldBlock(BlockState state) {
        return setHeldBlock(state, 0);
    }
}

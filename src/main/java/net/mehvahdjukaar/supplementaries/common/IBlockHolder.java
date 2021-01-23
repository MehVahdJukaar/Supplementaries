package net.mehvahdjukaar.supplementaries.common;

import net.minecraft.block.BlockState;

public interface IBlockHolder {
    BlockState getHeldBlock();
    boolean setHeldBlock(BlockState state);
}

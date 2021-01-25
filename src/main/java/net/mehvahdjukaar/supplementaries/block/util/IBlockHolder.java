package net.mehvahdjukaar.supplementaries.block.util;

import net.minecraft.block.BlockState;

public interface IBlockHolder {
    BlockState getHeldBlock();
    boolean setHeldBlock(BlockState state);
}

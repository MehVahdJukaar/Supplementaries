package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public interface IFrameBlock {

    //gets the filled block from the input one if available. Mainly for daub
    @Nullable
    Block getFilledBlock(Block inserted);
}

package net.mehvahdjukaar.supplementaries.common.block.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IInstanceTick {

    void instanceTick(Level level, BlockPos pos);
}

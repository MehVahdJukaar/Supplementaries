package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.supplementaries.common.block.tiles.HangingSignTileExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated(forRemoval = true)
public interface IExtendedHangingSign {

    HangingSignTileExtension getExtension();

}

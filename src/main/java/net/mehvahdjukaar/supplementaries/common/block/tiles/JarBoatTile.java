package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class JarBoatTile extends BlockEntity{
    public JarBoatTile(BlockPos pos, BlockState state) {
        super(ModRegistry.JAR_BOAT_TILE.get(), pos, state);
    }
}

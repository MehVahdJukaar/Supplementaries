package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleCakeBlockTile extends MimicBlockTile {
    protected DoubleCakeBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.DOUBLE_CAKE, pos, state);
    }
}

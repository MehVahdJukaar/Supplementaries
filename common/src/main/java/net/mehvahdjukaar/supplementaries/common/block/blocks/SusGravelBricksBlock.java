package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.SusGravelBricksTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SusGravelBricksBlock extends GravelBricksBlock implements EntityBlock {

    public SusGravelBricksBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SusGravelBricksTile(pos, state);
    }

}
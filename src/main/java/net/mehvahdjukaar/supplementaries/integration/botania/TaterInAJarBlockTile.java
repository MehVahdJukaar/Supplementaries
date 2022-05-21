package net.mehvahdjukaar.supplementaries.integration.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.common.block.tile.TileTinyPotato;

import javax.annotation.Nonnull;

public class TaterInAJarBlockTile extends TileTinyPotato {


    public TaterInAJarBlockTile(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Nonnull
    public BlockEntityType<TaterInAJarBlockTile> getType() {
        return BotaniaCompatRegistry.TATER_IN_A_JAR_TILE.get();
    }

}

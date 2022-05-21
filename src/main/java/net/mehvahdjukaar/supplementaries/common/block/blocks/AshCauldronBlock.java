package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Predicate;

public class AshCauldronBlock extends LayeredCauldronBlock {
    public AshCauldronBlock(BlockBehaviour.Properties properties,
                            Predicate<Biome.Precipitation> predicate,
                            Map<Item, CauldronInteraction> interactionMap) {
        super(BlockBehaviour.Properties.copy(Blocks.CAULDRON), predicate, interactionMap);


    }

    //lower level with water
    public void handlePrecipitation(BlockState pState, Level pLevel, BlockPos pPos, Biome.Precipitation pPrecipitation) {
        pState.getValue(LEVEL);

            pLevel.setBlockAndUpdate(pPos, pState.setValue(LEVEL, pState.getValue(LEVEL)-1));

    }

}

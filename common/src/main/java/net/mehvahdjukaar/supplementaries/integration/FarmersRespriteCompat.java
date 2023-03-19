package net.mehvahdjukaar.supplementaries.integration;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import umpaz.farmersrespite.FarmersRespite;
import umpaz.farmersrespite.common.block.KettleBlock;

public class FarmersRespriteCompat {

    public static IntegerProperty getWaterLevel(){
        return KettleBlock.WATER_LEVEL;
    }

    public static boolean isKettle(BlockState block){
        return block.getBlock() instanceof KettleBlock;
    }
}

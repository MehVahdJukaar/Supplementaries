package net.mehvahdjukaar.supplementaries.common.block.util.MobContainer;


import net.mehvahdjukaar.supplementaries.common.block.util.MobContainer.MobContainer;
import net.minecraft.core.Direction;

public interface IMobContainerProvider {
    MobContainer getMobContainer();

    default Direction getDirection(){
        return Direction.NORTH;
    }
}

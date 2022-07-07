package net.mehvahdjukaar.supplementaries.common.capabilities.mobholder;


import net.minecraft.core.Direction;

public interface IMobContainerProvider {
    MobContainer getMobContainer();

    default Direction getDirection(){
        return Direction.NORTH;
    }
}

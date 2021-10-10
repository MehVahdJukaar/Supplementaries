package net.mehvahdjukaar.supplementaries.common.mobholder;


import net.minecraft.util.Direction;

public interface IMobContainerProvider {
    MobContainer getMobContainer();

    default Direction getDirection(){
        return Direction.NORTH;
    }
}

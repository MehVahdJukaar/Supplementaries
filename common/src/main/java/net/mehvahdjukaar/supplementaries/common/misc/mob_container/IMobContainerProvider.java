package net.mehvahdjukaar.supplementaries.common.misc.mob_container;


import net.minecraft.core.Direction;

/**
 * Implement in a block that can hold mobs
 */
public interface IMobContainerProvider {
    MobContainer getMobContainer();

    default Direction getDirection(){
        return Direction.NORTH;
    }
}

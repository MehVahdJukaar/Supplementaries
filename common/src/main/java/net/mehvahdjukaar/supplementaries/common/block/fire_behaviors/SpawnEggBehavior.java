package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.world.item.SpawnEggItem;

public class SpawnEggBehavior extends SpawnMobBehavior {

    public SpawnEggBehavior() {
        super(s -> ((SpawnEggItem) s.getItem()).getType(s));
    }
}

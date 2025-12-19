package net.mehvahdjukaar.supplementaries.api;

import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehaviorRegistry;
import net.minecraft.core.RegistryAccess;

/**
 * Implemented by classes that provide one or more {@link IFireItemBehavior}s
 */
public interface IFireItemBehaviorProvider {
    void register(RegistryAccess registry, IFireItemBehaviorRegistry event);
}

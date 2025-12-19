package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.world.level.ItemLike;

public interface IFireItemBehaviorRegistry {
    /**
     * Register a custom {@link IFireItemBehavior} for cannons
     */
    void registerCannonBehavior(ItemLike item, IFireItemBehavior behavior);

    /**
     * Register a custom {@link IFireItemBehavior} for trapped presents
     */
    void registerPresentBehavior(ItemLike item, IFireItemBehavior behavior);
}

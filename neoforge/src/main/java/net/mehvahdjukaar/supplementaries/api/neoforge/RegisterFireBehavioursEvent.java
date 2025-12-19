package net.mehvahdjukaar.supplementaries.api.neoforge;

import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehaviorRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Neoforge event dispatched when we want to register {@link IFireItemBehavior}s
 */
public class RegisterFireBehavioursEvent extends Event implements IFireItemBehaviorRegistry {
    private final RegistryAccess registry;
    private final IFireItemBehaviorRegistry delegate;

    @ApiStatus.Internal
    public RegisterFireBehavioursEvent(RegistryAccess registry, IFireItemBehaviorRegistry delegate) {
        this.registry = registry;
        this.delegate = delegate;
    }

    public RegistryAccess getRegistryAccess() {
        return registry;
    }

    @Override
    public void registerCannonBehavior(ItemLike item, IFireItemBehavior behavior) {
        delegate.registerCannonBehavior(item, behavior);
    }

    @Override
    public void registerPresentBehavior(ItemLike item, IFireItemBehavior behavior) {
        delegate.registerPresentBehavior(item, behavior);
    }
}

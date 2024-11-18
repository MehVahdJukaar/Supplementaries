package net.mehvahdjukaar.supplementaries.common.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.jetbrains.annotations.NotNull;

import java.util.WeakHashMap;
import java.util.function.Function;

// so hear me out, datapack registry entries are one per logical side
// this means we cant serialize them properly if we just keep 1 instance as we might want to serialize them both ways
// so we need to keep one instance per logical side
// how to do that tho? we need a way we can then retrieve with a RegistryAccess or Level
// Weak HashMap using HolderLookup.Provider as key? nope those can be subclasses and are very often, leading to more undeded instances
// so we use a dummy object from one of the registries datapack registires...
public class SidedInstance<T> {

    private final WeakHashMap<Holder.Reference<BannerPattern>, T> instances = new WeakHashMap<>();
    private final Function<HolderLookup.Provider, T> factory;

    private SidedInstance(Function<HolderLookup.Provider, T> factory) {
        this.factory = factory;
    }

    public static <T> SidedInstance<T> of(Function<HolderLookup.Provider, T> factory) {
        return new SidedInstance<>(factory);
    }

    public T get(HolderLookup.Provider ra) {
        return instances.computeIfAbsent(getDummyKey(ra),
                k -> this.factory.apply(ra));
    }

    public void set(HolderLookup.Provider ra, T instance) {
        instances.put(getDummyKey(ra), instance);
    }

    private static Holder.@NotNull Reference<BannerPattern> getDummyKey(HolderLookup.Provider ra) {
        return ra.lookupOrThrow(Registries.BANNER_PATTERN).getOrThrow(BannerPatterns.BASE);
    }
}

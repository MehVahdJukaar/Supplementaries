package net.mehvahdjukaar.supplementaries.common.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

// so hear me out, datapack registry entries are one per logical side
// this means we cant serialize them properly if we just keep 1 instance as we might want to serialize them both ways
// so we need to keep one instance per logical side
// how to do that tho? we need a way we can then retrieve with a RegistryAccess or Level
// Weak HashMap using HolderLookup.Provider as key? nope those can be subclasses and are very often, leading to more undeded instances
// so we use a dummy object from one of the registries datapack registires...
public class SidedInstance<T> {

    //hack so we can have essentially an identity map
    private final Cache<ChatType, T> instances = CacheBuilder.newBuilder()
            .weakKeys()
            .build();

    private final Function<HolderLookup.Provider, T> factory;

    private SidedInstance(Function<HolderLookup.Provider, T> factory) {
        this.factory = factory;
    }

    public static <T> SidedInstance<T> of(Function<HolderLookup.Provider, T> factory) {
        return new SidedInstance<>(factory);
    }

    public T get(HolderLookup.Provider ra) {
        try {
            return instances.get(getDummyKey(ra),
                    () -> this.factory.apply(ra));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(HolderLookup.Provider ra, T instance) {
        instances.put(getDummyKey(ra), instance);
    }

    private ChatType getDummyKey(HolderLookup.Provider ra) {
        return ra.lookupOrThrow(Registries.CHAT_TYPE)
                .getOrThrow(ChatType.CHAT).value();
    }
}

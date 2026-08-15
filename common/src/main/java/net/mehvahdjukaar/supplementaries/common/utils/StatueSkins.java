package net.mehvahdjukaar.supplementaries.common.utils;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.minecraft.world.level.block.entity.SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR;

public class StatueSkins {

    private static final Map<String, ResolvableProfile> CACHE = new HashMap<>();
    private static int creditsGeneration = -1;

    @Nullable
    public static ResolvableProfile get(String statueName) {
        // credits are fetched in the background and land whenever they land
        if (creditsGeneration != Credits.generation()) {
            creditsGeneration = Credits.generation();
            CACHE.clear();
        }
        String name = statueName.toLowerCase(Locale.ROOT);
        if (CACHE.containsKey(name)) return CACHE.get(name);

        Pair<UUID, String> contributor = Credits.INSTANCE.statues().get(name);
        if (contributor == null) {
            CACHE.put(name, null);
            return null;
        }
        UUID id = contributor.getFirst();
        ResolvableProfile profile = id != null ?
                new ResolvableProfile(Optional.empty(), Optional.of(id), new PropertyMap()) :
                new ResolvableProfile(Optional.of(contributor.getSecond()), Optional.empty(), new PropertyMap());

        CACHE.put(name, profile);
        profile.resolve().thenAcceptAsync(resolved -> CACHE.put(name, resolved), CHECKED_MAIN_THREAD_EXECUTOR);
        return CACHE.get(name);
    }
}

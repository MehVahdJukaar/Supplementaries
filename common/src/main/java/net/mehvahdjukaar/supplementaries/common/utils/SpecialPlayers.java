package net.mehvahdjukaar.supplementaries.common.utils;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SpecialPlayers {

    //for pickle
    public static final List<UUID> DEVS = new ArrayList<>();
    //statues valid players
    public static final Map<String, Pair<UUID, String>> STATUES = new HashMap<>();
    //custom globes
    public static final Map<String, ResourceLocation> GLOBES = new HashMap<>();

    //TODO: fix pickle not working on me offline
    static {
        addSpecialPlayer("Dev", true, false, false, "380df991-f603-344c-a090-369bad2a924a");
        addSpecialPlayer("Dev", true, false, true, "5084e6f3-8f54-43f1-8df5-1dca109e430f");
        addSpecialPlayer("MehVahdJukaar", true, false, true, "898b3a39-e486-405c-a873-d6b472dc3ba2", "TheEvilGolem");
        addSpecialPlayer("Capobianco", true, true, true, "90ceb598-9983-4da3-9cae-436d5afb9d81");
        addSpecialPlayer("Plantkillable", true, true, true, "720f165c-b066-4113-9622-63fc63c65696");
        addSpecialPlayer("Agrona", true, true, false, (UUID) null, "Pancake", "Pancakes");

        Credits.INSTANCE.getSupporters().forEach((n, s) -> addSpecialPlayer(n, false, s.hasGlobe(), s.hasStatue(), s.getUuid()));
    }

    private static void addSpecialPlayer(String name, boolean isDev, boolean hasGlobe, boolean hasStatue, String id, String... alias) {
        UUID onlineId;
        if (id == null) {
            onlineId = null;
        } else {
            onlineId = UUID.fromString(id);
        }
        addSpecialPlayer(name, isDev, hasGlobe, hasStatue, onlineId);

    }

    private static void addSpecialPlayer(String name, boolean isDev, boolean hasGlobe, boolean hasStatue, @Nullable UUID onlineId, String... alias) {
        name = name.toLowerCase(Locale.ROOT);

        if (isDev) {
            if (onlineId != null) DEVS.add(onlineId);
            DEVS.add(UUIDUtil.createOfflinePlayerUUID(name));

        }
        if (hasGlobe) {
            ResourceLocation texture = new ResourceLocation(Supplementaries.MOD_ID, "textures/entity/globes/globe_" + name + ".png");
            GLOBES.put(name, texture);
            for (String n : alias) {
                GLOBES.put(n.toLowerCase(Locale.ROOT), texture);
            }
        }
        Pair<UUID, String> p = Pair.of(onlineId, name);
        if (hasStatue) {
            STATUES.put(name, p);
            for (String n : alias) {
                STATUES.put(n.toLowerCase(Locale.ROOT), p);
            }
        }
    }

}

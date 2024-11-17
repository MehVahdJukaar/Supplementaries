package net.mehvahdjukaar.supplementaries.common.misc.mob_container;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncCapturedMobsPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CapturedMobHandler extends SimpleJsonResourceReloadListener {

    public static CapturedMobHandler INSTANCE;

    private final Set<String> commandMobs = new HashSet<>();
    private final Map<EntityType<?>, DataDefinedCatchableMob> customMobProperties = new IdentityHashMap<>();
    private static DataDefinedCatchableMob moddedFishProperty;
    private final HolderLookup.Provider registryAccess;

    public CapturedMobHandler(HolderLookup.Provider ra) {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
                "catchable_mobs_properties");
        this.registryAccess = ra;

        INSTANCE = this;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        customMobProperties.clear();

        var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        var list = new ArrayList<DataDefinedCatchableMob>();
        jsons.forEach((key, json) -> {
            var data = DataDefinedCatchableMob.CODEC.parse(ops, json).getOrThrow();
            if (key.getPath().equals("generic_fish")) {
                moddedFishProperty = data;
            } else {
                list.add(data);
            }
        });
        for (var c : list) {
            for (var o : c.getOwners()) {
                BuiltInRegistries.ENTITY_TYPE.getOptional(o).ifPresent(e -> customMobProperties.put(e, c));
            }
        }
        //somebody reported a weird bug with this
        if (moddedFishProperty == null) {
            Supplementaries.LOGGER.error("Failed to find json for 'generic_fish'. How? Found jsons were : {}", jsons.keySet());
        }
    }

    public void sendDataToClient(ServerPlayer player) {
        Set<DataDefinedCatchableMob> set = new HashSet<>(customMobProperties.values());
        NetworkHelper.sendToClientPlayer(player,
                new ClientBoundSyncCapturedMobsPacket(set, moddedFishProperty));
    }

    public void acceptClientData(Set<DataDefinedCatchableMob> list, @Nullable DataDefinedCatchableMob defaultFish) {
        if (defaultFish != null) {
            moddedFishProperty = defaultFish;
        }
        customMobProperties.clear();
        for (var c : list) {
            for (var o : c.getOwners()) {
                BuiltInRegistries.ENTITY_TYPE.getOptional(o).ifPresent(e -> customMobProperties.put(e, c));
            }
        }
    }

    public ICatchableMob getDataCap(EntityType<?> type, boolean isFish) {
        var c = customMobProperties.get(type);
        if (c == null && isFish) return moddedFishProperty;
        return c;
    }

    public ICatchableMob getCatchableMobCapOrDefault(Entity entity) {
        if (entity instanceof ICatchableMob cap) return cap;
        var forgeCap = SuppPlatformStuff.getForgeCap(entity, ICatchableMob.class);
        if (forgeCap != null) return forgeCap;
        var prop = getDataCap(entity.getType(), BucketHelper.isModdedFish(entity));
        if (prop != null) return prop;
        return ICatchableMob.DEFAULT;
    }

    public boolean isCommandMob(String entity) {
        return commandMobs.contains(entity);
    }

    public void addCommandMob(String name) {
        commandMobs.add(name);
    }
}

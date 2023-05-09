package net.mehvahdjukaar.supplementaries.common.misc.mob_container;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncCapturedMobsPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CapturedMobHandler extends SimpleJsonResourceReloadListener {

    private static final Set<String> COMMAND_MOBS = new HashSet<>();

    private static final Map<EntityType<?>, DataDefinedCatchableMob> CUSTOM_MOB_PROPERTIES = new IdentityHashMap<>();
    private static DataDefinedCatchableMob moddedFishProperty;

    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final CapturedMobHandler RELOAD_INSTANCE = new CapturedMobHandler();

    private CapturedMobHandler() {
        super(GSON, "catchable_mobs_properties");
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        CUSTOM_MOB_PROPERTIES.clear();
        var list = new ArrayList<DataDefinedCatchableMob>();
        jsons.forEach((key, json) -> {
            var v = DataDefinedCatchableMob.CODEC.parse(JsonOps.INSTANCE, json);
            var data = v.getOrThrow(false, e -> Supplementaries.LOGGER.error("failed to parse captured mob properties: {}", e));
            if (key.getPath().equals("generic_fish")) {
                moddedFishProperty = data;
            } else {
                list.add(data);
            }
        });
        for (var c : list) {
            for (var o : c.getOwners()) {
                BuiltInRegistries.ENTITY_TYPE.getOptional(o).ifPresent(e -> CUSTOM_MOB_PROPERTIES.put(e, c));
            }
        }
        //somebody reported a weird bug with this
        if (moddedFishProperty == null) {
            Supplementaries.LOGGER.error("Failed to find json for 'generic_fish'. How? Found jsons were : {}", jsons.keySet());
        }
    }


    public static void sendDataToClient(ServerPlayer player) {
        Set<DataDefinedCatchableMob> set = new HashSet<>(CUSTOM_MOB_PROPERTIES.values());
        NetworkHandler.CHANNEL.sendToClientPlayer(player,
                new ClientBoundSyncCapturedMobsPacket(set, moddedFishProperty));
    }

    public static void acceptClientData(Set<DataDefinedCatchableMob> list, @Nullable DataDefinedCatchableMob defaultFish) {
        if (defaultFish != null) {
            moddedFishProperty = defaultFish;
        }
        CUSTOM_MOB_PROPERTIES.clear();
        for (var c : list) {
            for (var o : c.getOwners()) {
                BuiltInRegistries.ENTITY_TYPE.getOptional(o).ifPresent(e -> CUSTOM_MOB_PROPERTIES.put(e, c));
            }
        }
    }

    public static ICatchableMob getDataCap(EntityType<?> type, boolean isFish) {
        var c = CUSTOM_MOB_PROPERTIES.get(type);
        if (c == null && isFish) return moddedFishProperty;
        return c;
    }

    public static ICatchableMob getCatchableMobCapOrDefault(Entity entity) {
        if (entity instanceof ICatchableMob cap) return cap;
        var forgeCap = SuppPlatformStuff.getForgeCap(entity, ICatchableMob.class);
        if (forgeCap != null) return forgeCap;
        var prop = getDataCap(entity.getType(), BucketHelper.isModdedFish(entity));
        if (prop != null) return prop;
        return ICatchableMob.DEFAULT;
    }

    //debug
    public static void saveFile(DataDefinedCatchableMob data) {
        File folder = PlatHelper.getGamePath().resolve("test_cap").toFile();

        if (!folder.exists()) {
            folder.mkdir();
        }
        try {

            File exportPath = new File(folder, data.getOwners().get(0).toString().replace(":", "_") + ".json");
            try (FileWriter writer = new FileWriter(exportPath)) {
                var j = DataDefinedCatchableMob.CODEC.encodeStart(JsonOps.INSTANCE, data);

                CapturedMobHandler.GSON.toJson(sortJson(j.result().get().getAsJsonObject()), writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonObject sortJson(JsonObject jsonObject) {
        try {
            Map<String, JsonElement> joToMap = new TreeMap<>();
            jsonObject.entrySet().forEach(e -> {
                var j = e.getValue();
                if (j instanceof JsonObject jo) j = sortJson(jo);
                joToMap.put(e.getKey(), j);
            });
            JsonObject sortedJSON = new JsonObject();
            joToMap.forEach(sortedJSON::add);
            return sortedJSON;
        } catch (Exception ignored) {
        }
        return jsonObject;
    }

    public static boolean isCommandMob(String entity) {
        return COMMAND_MOBS.contains(entity);
    }

    public static void addCommandMob(String name) {
        COMMAND_MOBS.add(name);
    }
}

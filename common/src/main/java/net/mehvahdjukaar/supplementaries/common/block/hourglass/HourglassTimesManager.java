package net.mehvahdjukaar.supplementaries.common.block.hourglass;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncHourglassPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;

public class HourglassTimesManager extends RegistryAccessJsonReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final HourglassTimesManager RELOAD_INSTANCE = new HourglassTimesManager();

    private static final Map<Item, HourglassTimeData> DUSTS_MAP = new Object2ObjectOpenHashMap<>();
    private static final Set<HourglassTimeData> DUSTS = new HashSet<>();

    public HourglassTimesManager() {
        super(GSON, "hourglass_dusts");
    }

    @Override
    public void parse(Map<ResourceLocation, JsonElement> jsonMap, RegistryAccess access) {
        List<HourglassTimeData> list = new ArrayList<>();
        jsonMap.forEach((key, json) -> {
            try {
                var result = HourglassTimeData.REGISTRY_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, access), json);
                HourglassTimeData data = result.getOrThrow(false, e -> Supplementaries.LOGGER.error("Failed to parse hourglass data: {}", e));
                list.add(data);
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for hourglass data " + key);
            }
        });
        list.sort(Comparator.comparing(HourglassTimeData::getOrdering));
        list.forEach(HourglassTimesManager::addData);
    }

    public static void addData(HourglassTimeData data) {
        DUSTS.add(data);
        data.getItems().forEach(i -> {
            if(i.value() == Items.AIR){
                int aa = 1;
            }
            else DUSTS_MAP.put(i.value(), data);
        });
    }

    public static HourglassTimeData getData(Item item) {
        return DUSTS_MAP.getOrDefault(item, HourglassTimeData.EMPTY);
    }

    public static void acceptClientData(List<HourglassTimeData> hourglass) {
        hourglass.forEach(HourglassTimesManager::addData);
    }

    public static void sendDataToClient(ServerPlayer player) {
        NetworkHandler.CHANNEL.sendToClientPlayer(player,new ClientBoundSyncHourglassPacket(DUSTS));
    }

}
package net.mehvahdjukaar.supplementaries.common.block.hourglass;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncHourglassPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;

public class HourglassTimesManager extends RegistryAccessJsonReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final HourglassTimesManager INSTANCE = new HourglassTimesManager();

    private final Map<Item, HourglassTimeData> dustsMap = new Object2ObjectOpenHashMap<>();
    private final Set<HourglassTimeData> dusts = new HashSet<>();

    public HourglassTimesManager() {
        super(GSON, "hourglass_dusts");
    }

    @Override
    public void parse(Map<ResourceLocation, JsonElement> jsonMap, RegistryAccess access) {
        dusts.clear();
        dustsMap.clear();
        List<HourglassTimeData> list = new ArrayList<>();
        jsonMap.forEach((key, json) -> {
            try {
                var result = HourglassTimeData.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, access), json);
                HourglassTimeData data = result.getOrThrow();
                list.add(data);
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for hourglass data {}", key);
            }
        });
        this.setData(list);
    }

    public void setData(List<HourglassTimeData> list) {
        this.dusts.clear();
        this.dustsMap.clear();

        list.sort(Comparator.comparing(HourglassTimeData::ordering));
        for(var data : Lists.reverse(list)) {
            this.dusts.add(data);
            data.getItems().forEach(i -> {
                if (i.value() == Items.AIR) {
                    Supplementaries.error();
                } else this.dustsMap.put(i.value(), data);
            });
        }
    }

    public static HourglassTimeData getData(Item item) {
        return INSTANCE.dustsMap.getOrDefault(item, HourglassTimeData.EMPTY);
    }

    public static void sendDataToClient(ServerPlayer player) {
        NetworkHelper.sendToClientPlayer(player, new ClientBoundSyncHourglassPacket(INSTANCE.dusts));
    }

}
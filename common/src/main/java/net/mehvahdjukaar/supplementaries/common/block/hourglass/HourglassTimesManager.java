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
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncHourglassPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.*;

//make a data map?
public class HourglassTimesManager extends SimpleJsonResourceReloadListener {

    public static final WeakHashMap<HolderLookup.Provider, HourglassTimesManager> INSTANCES = new WeakHashMap<>();

    public static HourglassTimesManager getInstance(HolderLookup.Provider ra) {
        return INSTANCES.computeIfAbsent(ra, HourglassTimesManager::new);
    }

    public static HourglassTimesManager getInstance(Level level) {
        return getInstance(level.registryAccess());
    }

    private final Map<Item, HourglassTimeData> dustsMap = new Object2ObjectOpenHashMap<>();
    private final Set<HourglassTimeData> dusts = new HashSet<>();
    private final HolderLookup.Provider registryAccess;

    public HourglassTimesManager(HolderLookup.Provider registryAccess) {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
                "hourglass_dusts");
        this.registryAccess = registryAccess;

        INSTANCES.put(registryAccess, this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        dusts.clear();
        dustsMap.clear();
        List<HourglassTimeData> list = new ArrayList<>();
        jsonMap.forEach((key, json) -> {
            try {
                var result = HourglassTimeData.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registryAccess), json);
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

    public HourglassTimeData getData(Item item) {
        return dustsMap.getOrDefault(item, HourglassTimeData.EMPTY);
    }

    public static void sendDataToClient(ServerPlayer player) {
        HourglassTimesManager instance = getInstance(player.level());
        NetworkHelper.sendToClientPlayer(player, new ClientBoundSyncHourglassPacket(instance.dusts));
    }

}
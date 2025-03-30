package net.mehvahdjukaar.supplementaries.common.block.hourglass;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendHourglassDataPacket;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;

//make a data map?
public class HourglassTimesManager extends SimpleJsonResourceReloadListener {

    private static final SidedInstance<HourglassTimesManager> INSTANCES = SidedInstance.of(HourglassTimesManager::new);

    public static HourglassTimesManager getInstance(HolderLookup.Provider ra) {
        return INSTANCES.get(ra);
    }

    public static HourglassTimesManager getInstance(@NotNull Level level) {
        Preconditions.checkNotNull(level);
        return getInstance(level.registryAccess());
    }

    private final Map<Item, HourglassTimeData> dustsMap = new Object2ObjectOpenHashMap<>();
    private final Set<HourglassTimeData> dusts = new HashSet<>();
    private final HolderLookup.Provider registryAccess;

    public HourglassTimesManager(HolderLookup.Provider registryAccess) {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
                "hourglass_dusts");
        this.registryAccess = registryAccess;

        INSTANCES.set(registryAccess, this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        dustsMap.clear();
        List<HourglassTimeData> list = new ArrayList<>();
        RegistryOps<JsonElement> ops = ForgeHelper.conditionalOps(JsonOps.INSTANCE, registryAccess, this);
        var codec = ForgeHelper.conditionalCodec(HourglassTimeData.CODEC);
        jsonMap.forEach((key, json) -> {
            try {
                var result = codec.parse(ops, json).getOrThrow();
                result.ifPresent(list::add);
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for hourglass data {}", key, e);
            }
        });
        this.setData(list);
    }

    public void setData(List<HourglassTimeData> list) {
        this.dusts.clear();
        this.dusts.addAll(list);
    }

    public HourglassTimeData getData(Item item) {
        if (dustsMap.isEmpty()) {
            List<HourglassTimeData> list = new ArrayList<>(dusts);
            list.sort(Comparator.comparing(HourglassTimeData::ordering));
            for (var data : Lists.reverse(list)) {
                this.dusts.add(data);
                data.getItems().forEach(i -> {
                    if (i.value() == Items.AIR) {
                        Supplementaries.error();
                    } else this.dustsMap.put(i.value(), data);
                });
            }
        }

        return dustsMap.getOrDefault(item, HourglassTimeData.EMPTY);
    }

    public static void sendDataToClient(ServerPlayer player) {
        HourglassTimesManager instance = getInstance(player.level());
        NetworkHelper.sendToClientPlayer(player, new ClientBoundSendHourglassDataPacket(instance.dusts));
    }

}
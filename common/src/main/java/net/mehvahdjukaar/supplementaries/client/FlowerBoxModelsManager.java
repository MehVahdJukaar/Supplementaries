package net.mehvahdjukaar.supplementaries.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//client side
public class FlowerBoxModelsManager extends SimpleJsonResourceReloadListener {

    public static final FlowerBoxModelsManager INSTANCE = new FlowerBoxModelsManager();

    private final Map<Item, FlowerBoxPlant> flowers = new IdentityHashMap<>();

    public FlowerBoxModelsManager() {
        super(new Gson(), "flower_box_plants");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        flowers.clear();
        //no registry access since we load on boot and there's no registry access there
        //RegistryOps<JsonElement> ops = ForgeHelper.conditionalOps(JsonOps.INSTANCE, registryAccess, this);
        var codec = ForgeHelper.conditionalCodec(FlowerBoxPlant.CODEC);
        jsonMap.forEach((key, json) -> {
            try {
                FlowerBoxPlant result = codec.parse(JsonOps.INSTANCE, json).getOrThrow().get();
                for (Item item : result.seedItems) {
                    flowers.put(item, result);
                }
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for hourglass data {}", key, e);
            }
        });
    }

    private record FlowerBoxPlant(List<Item> seedItems, ModelResourceLocation tallModel,
                                  ModelResourceLocation normalModel) {
        private FlowerBoxPlant(List<Item> item, Optional<ResourceLocation> tallModel, Optional<ResourceLocation> normalModel) {
            this(item, tallModel.map(RenderUtil::getStandaloneModelLocation).orElse(null),
                    normalModel.map(RenderUtil::getStandaloneModelLocation).orElse(null));
        }

        public static final Codec<FlowerBoxPlant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MiscUtils.LENIENT_ITEM_OR_ITEM_LIST.lenientOptionalFieldOf("items", List.of()).forGetter(FlowerBoxPlant::seedItems),
                ResourceLocation.CODEC.optionalFieldOf("big_model").forGetter(f -> Optional.ofNullable(f.tallModel).map(ModelResourceLocation::id)),
                ResourceLocation.CODEC.optionalFieldOf("small_model").forGetter(f -> Optional.ofNullable(f.normalModel).map(ModelResourceLocation::id))
        ).apply(instance, FlowerBoxPlant::new));
    }

    @Nullable
    public ModelResourceLocation getSpecialFlowerModel(Item i) {
        FlowerBoxModelsManager instance = INSTANCE;
        if (instance == null) return null;
        FlowerBoxPlant flower = instance.flowers.get(i);
        if (flower == null) return null;
        ModelResourceLocation res;
        if (CommonConfigs.Building.FLOWER_BOX_SIMPLE_MODE.get()) {
            res = flower.tallModel;
            if (res != null) return res;
        }
        res = flower.normalModel;
        return res;
    }

}

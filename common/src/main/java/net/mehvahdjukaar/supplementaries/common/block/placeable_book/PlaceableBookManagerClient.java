package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener.scanDirectory;

public class PlaceableBookManagerClient {
    private static final Gson GSON =  new GsonBuilder().setPrettyPrinting().create();
    //client
    //static, just 1 instance exists
    private static final MapRegistry<List<BookModelVisuals>> bookVisuals = new MapRegistry<>("placeable_books_visuals");
    private static final BookModelVisuals missingModel = new BookModelVisuals(
            new ModelResourceLocation(Supplementaries.res("missing"), "missing"),
            -1, 0, false, DataComponentMap.EMPTY);

    public static void onEarlyPackLoad(ResourceManager resourceManager) {
        Map<ResourceLocation, JsonElement> js = new HashMap<>();
        scanDirectory(resourceManager, "placeable_books_visuals", GSON, js);

        bookVisuals.clear();
        DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
        for (var entry : js.entrySet()) {
            var m = BookModelVisuals.LIST_CODEC.parse(ops, entry.getValue()).getOrThrow();
            bookVisuals.register(entry.getKey(), m);
        }
    }


    //client stuff. Ugly
    public static List<BookModelVisuals> getValidModelsForBookItem(HolderLookup.Provider level, ItemStack stack, boolean horizontal) {
        var instance = PlaceableBookManager.getInstance(level); //client instance
        BookType type = instance.get(stack.getItem(), horizontal);
        if (type == null) {
            Supplementaries.LOGGER.warn("No book type found for item: {}", stack.getItem());
            return List.of(missingModel);
        }
        var list = bookVisuals.getValue(type.bookVisuals());
        if (list == null || list.isEmpty()) {
            Supplementaries.LOGGER.warn("No visuals found for book type: {}", type);
            return List.of(missingModel);
        }
        var set = list.stream()
                .filter(v -> v.matchesComponents(stack.getComponents()))
                .toList();
        if (set.isEmpty()) {
            Supplementaries.LOGGER.warn("No visuals matched for book item: {}", stack);
            return List.of(missingModel);
        }
        return set;
    }

    public static List<ModelResourceLocation> getExtraModels() {
        List<ModelResourceLocation> list = new ArrayList<>();
        for (var entry : bookVisuals.getEntries()) {
            for (var model : entry.getValue()) {
                list.add(model.model());
            }
        }
        return list;
    }
}

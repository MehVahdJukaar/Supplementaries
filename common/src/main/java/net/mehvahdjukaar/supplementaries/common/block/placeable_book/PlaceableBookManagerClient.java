package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class PlaceableBookManagerClient extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final PlaceableBookManagerClient INSTANCE = new PlaceableBookManagerClient();
    private static final MapRegistry<List<BookModelVisuals.VariantModelList>> bookVisuals = new MapRegistry<>("placeable_books_visuals");
    private static final ResourceLocation FALLBACK_VISUALS = Supplementaries.res("normal_book");
    private static final BookModelVisuals missingModel = new BookModelVisuals(
            RenderUtil.getStandaloneModelLocation(Supplementaries.res("special_models/block/books/book_brown")),
            -1, 0, false);

    private PlaceableBookManagerClient() {
        super(GSON, "placeable_books_visuals");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> js, ResourceManager resourceManager, ProfilerFiller profiler) {
        bookVisuals.clear();
        DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
        for (var entry : js.entrySet()) {
            var m = BookModelVisuals.VariantModelList.LIST_CODEC.parse(ops, entry.getValue()).getOrThrow();
            bookVisuals.register(entry.getKey(), m);
        }
    }

    private static List<BookModelVisuals> getFallbackModels() {
        var list = bookVisuals.getValue(FALLBACK_VISUALS);
        if (list != null && !list.isEmpty()) {
            return list.getFirst().models();
        }
        return List.of(missingModel);
    }

    //client stuff. Ugly
    public static List<BookModelVisuals> getValidModelsForBookItem(HolderLookup.Provider level, ItemStack stack, boolean horizontal) {
        BookType type = PlaceableBookManager.get(stack.getItem(), horizontal, level);
        if (type == null) {
            Supplementaries.LOGGER.warn("No book type found for item: {}", stack.getItem());
            return getFallbackModels();
        }
        var list = bookVisuals.getValue(type.bookVisuals());
        if (list == null || list.isEmpty()) {
            Supplementaries.LOGGER.warn("No visuals found for book type: {}", type);
            return getFallbackModels();
        }
        BookModelVisuals.VariantModelList modelsList = null;
        for (var m : list) {
            if (m.matchesComponents(stack.getComponents())) {
                modelsList = m;
                break;
            }
        }
        if (modelsList == null) {
            Supplementaries.LOGGER.warn("No visuals matched for book item: {}", stack);
            return getFallbackModels();
        }
        return modelsList.models();
    }
}

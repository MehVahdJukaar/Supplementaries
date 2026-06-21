package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener.scanDirectory;

public class PlaceableBookManagerClient {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    //client
    //static, just 1 instance exists
    private static final MapRegistry<List<BookModelVisuals.VariantModelList>> bookVisuals = new MapRegistry<>("placeable_books_visuals");
    // Visuals to fall back to when a book item can't be matched to a known variant (e.g. porting
    // old worlds whose stored book item data no longer matches any variant). Falling back to the
    // normal book's color list lets the renderer pick a random color instead of showing vanilla's
    // purple/black missing-texture cube.
    private static final ResourceLocation FALLBACK_VISUALS = Supplementaries.res("normal_book");
    // last resort if even the normal book visuals are missing: a plain brown book (a real, registered model)
    private static final BookModelVisuals missingModel = new BookModelVisuals(
            RenderUtil.getStandaloneModelLocation(Supplementaries.res("block/books/book_brown")),
            -1, 0, false);

    private static List<BookModelVisuals> getFallbackModels() {
        var list = bookVisuals.getValue(FALLBACK_VISUALS);
        if (list != null && !list.isEmpty()) {
            return list.get(0).models();
        }
        return List.of(missingModel);
    }

    private static void reload(ResourceManager resourceManager) {
        Map<ResourceLocation, JsonElement> js = new HashMap<>();
        scanDirectory(resourceManager, "placeable_books_visuals", GSON, js);

        bookVisuals.clear();
        DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
        for (var entry : js.entrySet()) {
            var m = BookModelVisuals.VariantModelList.LIST_CODEC.parse(ops, entry.getValue()).getOrThrow();
            bookVisuals.register(entry.getKey(), m);
        }
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

    private static List<ModelResourceLocation> getExtraModels() {
        List<ModelResourceLocation> list = new ArrayList<>();
        for (var entry : bookVisuals.getEntries()) {
            for (var model : entry.getValue()) {
                for (var visuals : model.models()) {
                    list.add(visuals.model());
                }
            }
        }
        return list;
    }

    public static void registerExtraModels(ClientHelper.SpecialModelEvent event) {
        //run reloader and register extra models
        reload(Minecraft.getInstance().getResourceManager());

        getExtraModels().forEach(event::register);
    }
}

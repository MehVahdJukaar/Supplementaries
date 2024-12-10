package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlaceableBookManager extends SimpleJsonResourceReloadListener {

    public static final SidedInstance<PlaceableBookManager> INSTANCES = SidedInstance.of(PlaceableBookManager::new);

    private final MapRegistry<BookType> books = new MapRegistry<>("placeable_books");
    private final HolderLookup.Provider registryAccess;

    public PlaceableBookManager(HolderLookup.Provider registryAccess) {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
                "placeable_books");
        this.registryAccess = registryAccess;

        INSTANCES.set(registryAccess, this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        books.clear();
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        for (var entry : object.entrySet()) {
            BookType type = BookType.CODEC.decode(ops, entry.getValue()).getOrThrow()
                    .getFirst();
            books.register(entry.getKey(), type);
        }
    }
/*
    //TODO: make data
    public void setup() {
        registerDefault(DyeColor.BROWN, 1);
        registerDefault(DyeColor.WHITE, 1);
        registerDefault(DyeColor.BLACK, 1);
        registerDefault(DyeColor.LIGHT_GRAY);
        registerDefault(DyeColor.GRAY);
        registerDefault(DyeColor.ORANGE);
        registerDefault(DyeColor.YELLOW);
        registerDefault(DyeColor.LIME);
        registerDefault("green", 0x2fc137);
        registerDefault("cyan", 0x16ecbf);
        registerDefault(DyeColor.LIGHT_BLUE);
        registerDefault(DyeColor.BLUE);
        registerDefault(DyeColor.PURPLE);
        registerDefault(DyeColor.MAGENTA);
        registerDefault(DyeColor.PINK);
        registerDefault(DyeColor.RED);
        register(new BookType("enchanted", 0, 1, true, null), Items.ENCHANTED_BOOK);
        register(new BookType("and_quill", 0, 1, false, null), Items.WRITABLE_BOOK);
        register(new BookType("written", 0, 1, false, null), Items.WRITTEN_BOOK);
        register(new BookType("tattered", 0, 1, false, null), null);
        register(new BookType("tome", 0, 1, true, null), CompatObjects.TOME.get());
        register(new BookType("gene", 0, 1, false, null), CompatObjects.GENE_BOOK.get());
    }
*/

    public BookType rand(Random r) {
        return null;
    }

    public Collection<BookType> getAll() {
        return books.getValues();
    }

    public BookType getByName(String name) {
        return null;
    }

    public ArrayList<BookType> getByItem(ItemStack stack) {
        if (AntiqueInkItem.hasAntiqueInk(stack)) {
            return new ArrayList<>(List.of(getByName("tattered")));
        }
        Item item = stack.getItem();
        if (Utils.getID(item).getNamespace().equals("inspirations")) {
            String colName = Utils.getID(item).getPath().replace("_book", "");
            return new ArrayList<>(List.of(getByName(colName)));
        }
        return null;
    }


}

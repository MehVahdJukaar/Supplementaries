package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlaceableBookManager extends SimpleJsonResourceReloadListener {

    public static final SidedInstance<PlaceableBookManager> INSTANCES = SidedInstance.of(PlaceableBookManager::new);

    private final Map<String, BookType> byName = new HashMap<>();
    private final Multimap<Item, BookType> byItem = HashMultimap.create();
    private final HolderLookup.Provider registryAccess;

    public PlaceableBookManager(HolderLookup.Provider registryAccess) {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
                "placeable_books");
        this.registryAccess = registryAccess;

        INSTANCES.set(registryAccess, this);
    }

    public void register(BookType type, @Nullable Item item) {
        if (item != null) {
            byItem.put(item, type);
            byName.put(type.texture(), type);
        }
    }
/*
    public void registerDefault(DyeColor color) {
        register(new BookType(color), Items.BOOK);
    }


    public void registerDefault(DyeColor color, int angle) {
        register(new BookType(color, angle, false), Items.BOOK);
    }

    public void registerDefault(String name, int color) {
        register(new BookType(name, color, false), Items.BOOK);
    }


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
        ArrayList<BookType> all = getAll();
        return all.get(r.nextInt(all.size()));
    }

    public ArrayList<BookType> getAll() {
        return new ArrayList<>(byItem.values());
    }

    public BookType getByName(String name) {
        var b = byName.get(name);
        if (b == null) return byName.get("brown");
        return b;
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
        return new ArrayList<>(byItem.get(item));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {

    }
}

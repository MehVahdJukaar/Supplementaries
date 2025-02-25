package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        DynamicOps<JsonElement> ops = ForgeHelper.conditionalOps(JsonOps.INSTANCE, registryAccess, this);
        Codec<Optional<BookType>> codec = ForgeHelper.conditionalCodec(BookType.CODEC);
        for (var entry : object.entrySet()) {
          //  codec.parse(ops, entry.getValue())
          //          .getOrThrow().ifPresent(type ->
          //                  books.register(entry.getKey(), type));
        }
    }

    public List<BookType> getForItem(ItemStack stack, boolean horizontal) {
        List<BookType> results = new ArrayList<>();
        for (var entry : books.getValues()) {
            if (entry.predicate().test(stack) && entry.isHorizontal() == horizontal) {
                results.add(entry);
            }
        }
        return results;
    }

    public BookType getByName(ResourceLocation id) {
        return books.getValue(id);
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

}

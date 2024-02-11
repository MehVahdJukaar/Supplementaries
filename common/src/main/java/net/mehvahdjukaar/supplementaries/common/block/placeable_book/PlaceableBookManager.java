package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlaceableBookManager {

    private static final Map<String, BookType> BY_NAME = new HashMap<>();
    private static final Multimap<Item, BookType> BY_ITEM = HashMultimap.create();

    public static void register(BookType type, @Nullable Item item) {
        if (item != null) {
            BY_ITEM.put(item, type);
            BY_NAME.put(type.name(), type);
        }
    }

    public static void registerDefault(DyeColor color) {
        register(new BookType(color), Items.BOOK);
    }


    public static void registerDefault(DyeColor color, int angle) {
        register(new BookType(color, angle, false), Items.BOOK);
    }

    public static void registerDefault(String name, int color) {
        register(new BookType(name, color, false), Items.BOOK);
    }


    //TODO: make data
    public static void setup() {
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
        register(new BookType("enchanted", 0, 1, true), Items.ENCHANTED_BOOK);
        register(new BookType("and_quill", 0, 1, false), Items.WRITABLE_BOOK);
        register(new BookType("written", 0, 1, false), Items.WRITTEN_BOOK);
        register(new BookType("tattered", 0, 1, false), null);
        register(new BookType("tome", 0, 1, true), CompatObjects.TOME.get());
        register(new BookType("gene", 0, 1, false), CompatObjects.GENE_BOOK.get());
    }

    public static BookType rand(Random r) {
        ArrayList<BookType> all = getAll();
        return all.get(r.nextInt(all.size()));
    }

    public static ArrayList<BookType> getAll() {
        return new ArrayList<>(BY_ITEM.values());
    }

    public static BookType getByName(String name) {
        var b = BY_NAME.get(name);
        if (b == null) return BY_NAME.get("brown");
        return b;
    }

    public static ArrayList<BookType> getByItem(ItemStack stack) {
        if (AntiqueInkItem.hasAntiqueInk(stack)) {
            return new ArrayList<>(List.of(getByName("tattered")));
        }
        Item item = stack.getItem();
        if (Utils.getID(item).getNamespace().equals("inspirations")) {
            String colName = Utils.getID(item).getPath().replace("_book", "");
            return new ArrayList<>(List.of(getByName(colName)));
        }
        return new ArrayList<>(BY_ITEM.get(item));
    }
}

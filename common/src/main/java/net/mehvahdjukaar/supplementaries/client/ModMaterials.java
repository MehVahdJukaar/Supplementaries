package net.mehvahdjukaar.supplementaries.client;

import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;

public class ModMaterials {
    public static final ResourceLocation SIGN_SHEET = new ResourceLocation("textures/atlas/signs.png");
    public static final ResourceLocation BANNER_SHEET = new ResourceLocation("textures/atlas/banner_patterns.png");

    //materials
    public static final Material CLOCK_HAND = new Material(TextureAtlas.LOCATION_BLOCKS, ModTextures.CLOCK_HAND_TEXTURE);
    public static final Material BLACKBOARD_OUTLINE = new Material(LOCATION_BLOCKS, ModTextures.BLACKBOARD_GRID);
    public static final Material BELLOWS_MATERIAL = new Material(LOCATION_BLOCKS, ModTextures.BELLOWS_TEXTURE);
    public static final Material BUBBLE_BLOCK_MATERIAL = new Material(LOCATION_BLOCKS, ModTextures.BUBBLE_BLOCK_TEXTURE);
    public static final Material BLACKBOARD_WHITE = new Material(LOCATION_BLOCKS, ModTextures.BLACKBOARD_WHITE_TEXTURE);
    public static final Material BLACKBOARD_BLACK = new Material(LOCATION_BLOCKS, ModTextures.BLACKBOARD_BLACK_TEXTURE);
    public static final Material SAND_MATERIAL = new Material(LOCATION_BLOCKS, ModTextures.SAND_TEXTURE);
    public static final Material BOOK_GLINT_MATERIAL = new Material(LOCATION_BLOCKS, Supplementaries.res( "block/books/book_enchanted"));

    public static final Supplier<Map<WoodType, Material>> SIGN_POSTS_MATERIALS = Suppliers.memoize(() -> {
        var map = new IdentityHashMap<WoodType, Material>();
        ModRegistry.SIGN_POST_ITEMS.forEach((wood, item) -> map
                .put(wood, new Material(LOCATION_BLOCKS, Supplementaries.res("block/sign_posts/" +
                        Utils.getID(item).getPath()))));
        return map;
    });

    public static final Supplier<Map<BannerPattern, Material>> FLAG_MATERIALS = Suppliers.memoize(() -> {
        var map = new IdentityHashMap<BannerPattern, Material>();
        for (var v : ModTextures.FLAG_TEXTURES.entrySet()) {
            map.put(v.getKey(), new Material(BANNER_SHEET, v.getValue()));
        }
        return map;
    });

    public static final Material CANVAS_SIGH_MATERIAL = new Material(SIGN_SHEET, Supplementaries.res("entity/signs/hanging/farmersdelight/extension_canvas"));
    public static final Supplier<Map<net.minecraft.world.level.block.state.properties.WoodType, Material>> HANGING_SIGN_EXTENSIONS =
            Suppliers.memoize(() -> net.minecraft.world.level.block.state.properties.WoodType.values().collect(Collectors.toMap(
                    Function.identity(),
                    w -> {
                        String str = w.name();
                        if (str.contains(":")) {
                            str = str.replace(":", "/extension_");
                        } else str = "extension_" + str;
                        return new Material(SIGN_SHEET, Supplementaries.res("entity/signs/hanging/" + str));
                    },
                    (v1, v2) -> v1,
                    IdentityHashMap::new)));

    @Nullable
    public static Material getFlagMaterialForPatternItem(BannerPatternItem item) {
        var p = ITEM_TO_PATTERNS.get(item);
        if (p == null) {
            for (var j : BuiltInRegistries.BANNER_PATTERN.getTag(item.getBannerPattern()).get()) {
                ITEM_TO_PATTERNS.put(item, j.value());
                return FLAG_MATERIALS.get().get(j.value());
            }
            return null;
        } else return FLAG_MATERIALS.get().get(p);
    }

    private static final Map<BannerPatternItem, BannerPattern> ITEM_TO_PATTERNS = new IdentityHashMap<>();


    private static final Cache<ResourceLocation, Material> CACHED_MATERIALS = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();


}

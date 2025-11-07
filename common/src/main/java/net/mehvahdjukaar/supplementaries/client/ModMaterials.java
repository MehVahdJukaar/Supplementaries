package net.mehvahdjukaar.supplementaries.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;

public class ModMaterials {
    public static final ResourceLocation SIGN_SHEET = ResourceLocation.withDefaultNamespace("textures/atlas/signs.png");
    public static final ResourceLocation BANNER_SHEET = ResourceLocation.withDefaultNamespace("textures/atlas/banner_patterns.png");

    //materials
    public static final Material CLOCK_HAND = new Material(TextureAtlas.LOCATION_BLOCKS, ModTextures.CLOCK_HAND_TEXTURE);
    public static final Material FISHIES = new Material(TextureAtlas.LOCATION_BLOCKS, ModTextures.FISHIES_TEXTURE);
    public static final Material BLACKBOARD_OUTLINE = new Material(LOCATION_BLOCKS, ModTextures.BLACKBOARD_GRID);
    public static final Material BELLOWS_MATERIAL = new Material(LOCATION_BLOCKS, ModTextures.BELLOWS_TEXTURE);
    public static final Material BUBBLE_BLOCK_MATERIAL = new Material(LOCATION_BLOCKS, ModTextures.BUBBLE_BLOCK_TEXTURE);
    public static final Material BLACKBOARD_WHITE = new Material(LOCATION_BLOCKS, ModTextures.BLACKBOARD_WHITE_TEXTURE);
    public static final Material BLACKBOARD_BLACK = new Material(LOCATION_BLOCKS, ModTextures.BLACKBOARD_BLACK_TEXTURE);
    public static final Material SAND_MATERIAL = new Material(LOCATION_BLOCKS, ModTextures.SAND_TEXTURE);
    public static final Material CANNON_MATERIAL = new Material(LOCATION_BLOCKS, ModTextures.CANNON_TEXTURE);
    public static final Material BOOK_GLINT_MATERIAL = new Material(LOCATION_BLOCKS, Supplementaries.res("block/books/book_enchanted"));
    public static final Material CANNON_TARGET_MATERIAL = new Material(LOCATION_BLOCKS, Supplementaries.res("block/cannon_target"));
    public static final Material CANNON_TARGET_RED_MATERIAL = new Material(LOCATION_BLOCKS, Supplementaries.res("block/cannon_target_charging"));
    public static final Material WIND_VANE_MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, Supplementaries.res("block/wind_vane"));
    public static final Map<DyeColor, Material> BUNTING_MATERIAL =
            Arrays.stream(DyeColor.values()).collect(Collectors.toMap(Function.identity(),
                    c -> new Material(TextureAtlas.LOCATION_BLOCKS,
                            Supplementaries.res("block/buntings/bunting_" + c.getName()))));

    public static final Function<BannerPattern, Material> FLAG_MATERIALS = Util.memoize(pattern ->
            new Material(BANNER_SHEET, Supplementaries.res("entity/banner/flags/" + pattern.assetId()
                    .toShortLanguageKey().replace(":", "/").replace(".", "/"))
            ));
    public static final Material FLAG_BASE_MATERIAL = new Material(BANNER_SHEET, Supplementaries.res("entity/banner/flags/vehicle"));


    @Nullable
    public static Material getFlagMaterialForPatternItem(Level level, BannerPatternItem item) {
        var p = ITEM_TO_PATTERNS.get(item);
        if (p == null) {
            Registry<BannerPattern> registry = level.registryAccess().registryOrThrow(Registries.BANNER_PATTERN);
            for (var j : registry.getTag(item.getBannerPattern()).get()) {
                ITEM_TO_PATTERNS.put(item, j.value());
                return FLAG_MATERIALS.apply(j.value());
            }
            return null;
        } else return FLAG_MATERIALS.apply(p);
    }

    private static final Map<BannerPatternItem, BannerPattern> ITEM_TO_PATTERNS = new IdentityHashMap<>();


    private static final Cache<ResourceLocation, Material> CACHED_MATERIALS = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    //cached materials
    public static Material get(ResourceLocation bockTexture) {
        try {
            return CACHED_MATERIALS.get(bockTexture, () -> new Material(TextureAtlas.LOCATION_BLOCKS, bockTexture));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Function<ResourceLocation, ModelResourceLocation> TO_STANDALONE_MODEL = Util.memoize(
            RenderUtil::getStandaloneModelLocation
    );

}

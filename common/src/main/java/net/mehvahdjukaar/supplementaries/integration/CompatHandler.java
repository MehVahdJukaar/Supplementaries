package net.mehvahdjukaar.supplementaries.integration;


import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.common.items.BlockPlacerItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CompatHandler {

    public static final boolean QUARK = isLoaded("quark");
    public static final boolean DECO_BLOCKS = isLoaded("decorative_blocks");
    public static final boolean CONFIGURED = isLoaded("configured");
    public static final boolean OREGANIZED = isLoaded("oreganized");
    public static final boolean CREATE = isLoaded("create");
    public static final boolean TORCHSLAB = isLoaded("torchslabmod");
    public static final boolean CURIOS = isLoaded("curios");
    public static final boolean FARMERS_DELIGHT = isLoaded("farmersdelight");
    public static final boolean INFERNALEXP = isLoaded("infernalexp");
    public static final boolean INSPIRATIONS = isLoaded("inspirations");
    public static final boolean FRAMEDBLOCKS = isLoaded("framedblocks");
    public static final boolean RGBLIB = isLoaded("rgblib");
    public static final boolean ENDERGETIC = isLoaded("endergetic");
    public static final boolean DECO_BLOCKS_ABNORMALS = isLoaded("decorative_blocks_abnormals");
    public static final boolean MUCH_MORE_MOD_COMPAT = isLoaded("muchmoremodcompat");
    public static final boolean AUTUMNITY = isLoaded("autumnity");
    public static final boolean BUZZIER_BEES = isLoaded("buzzier_bees");
    public static final boolean FLYWHEEL = isLoaded("flywheel");
    public static final boolean REPURPOSED_STRUCTURES = isLoaded("repurposed_structures");
    public static final boolean TETRA = isLoaded("tetra");
    public static final boolean POKECUBE_LEGENDS = isLoaded("pokecube_legends");
    public static final boolean POKECUBE = isLoaded("pokecube");
    public static final boolean DYNAMICTREES = isLoaded("dynamictrees");
    public static final boolean MOREMINECARTS = isLoaded("moreminecarts");
    public static final boolean HABITAT = isLoaded("habitat");
    public static final boolean SIMPLEFARMING = isLoaded("simplefarming");
    public static final boolean ATMOSPHERIC = isLoaded("atmospheric");
    public static final boolean ENCHANTEDBOOKREDESIGN = isLoaded("enchantedbookredesign");
    public static final boolean CUSTOMVILLAGERTRADES = isLoaded("customvillagertrades");
    public static final boolean COMPUTERCRAFT = isLoaded("computercraft");
    public static final boolean NETHERSDELIGHT = isLoaded("nethers_delight");
    public static final boolean DOUBLEDOORS = isLoaded("doubledoors");
    public static final boolean MALUM = isLoaded("malum");
    public static final boolean BOTANIA = isLoaded("botania");
    public static final boolean MAPATLAS = isLoaded("map_atlases");
    public static final boolean WAYSTONES = isLoaded("waystones");
    public static final boolean OVERWEIGHT_FARMING = isLoaded("overweight_farming");
    public static final boolean SNOWYSPIRIT = isLoaded("snowyspirit");
    public static final boolean CLOTH_CONFIG = isLoaded("cloth_config");
    public static final boolean FLAN = isLoaded("flan");
    public static final boolean BREEZY = isLoaded("breezy");
    public static final boolean SHIMMER = isLoaded("shimmer");
    public static final boolean BUMBLEZONE = isLoaded("the_bumblezone");
    public static final boolean CAVE_ENHANCEMENTS = isLoaded("cave_enhancements");
    public static final boolean CUSTOM_PLAYER_MODELS = isLoaded("cpm");


    private static boolean isLoaded(String name) {
        return PlatformHelper.isModLoaded(name);
    }

    public static void setup() {
        if (CREATE) CreateCompat.setup();
        if (COMPUTERCRAFT) CCCompat.setup();

        var i = Registry.ITEM.getOptional(new ResourceLocation("quark:ancient_tome"));

        i.ifPresent(b -> BlockPlacerItem.registerPlaceableItem(ModRegistry.BOOK_PILE.get(), () -> b, CommonConfigs.Tweaks.PLACEABLE_BOOKS));
    }

    public static void initOptionalRegistries() {
        if (FARMERS_DELIGHT) FarmersDelightCompat.init();
        if (DECO_BLOCKS) DecoBlocksCompat.init();
        if (QUARK) QuarkCompat.init();
        if (BUZZIER_BEES) BuzzierBeesCompat.init();
        if (CAVE_ENHANCEMENTS) CaveEnhancementsCompat.init();
        //if (inspirations) CauldronRecipes.registerStuff();
    }


    public static Block DynTreesGetOptionalDynamicSapling(Item item, Level level, BlockPos worldPosition) {
        return null;
    }


    public static boolean isVerticalSlabEnabled() {
        return QUARK && QuarkCompat.isVerticalSlabEnabled();
    }
}

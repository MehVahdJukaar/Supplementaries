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

    public static final boolean QUARK;
    public static final boolean DECO_BLOCKS;
    public static final boolean CONFIGURED;
    public static final boolean CREATE;
    public static final boolean TORCHSLAB;
    public static final boolean CURIOS;
    public static final boolean FARMERS_DELIGHT;
    public static final boolean INFERNALEXP;
    public static final boolean INSPIRATIONS;
    public static final boolean FRAMEDBLOCKS;
    public static final boolean RGBLIB;
    public static final boolean ENDERGETIC;
    public static final boolean BUZZIER_BEES;
    public static final boolean AUTUMNITY;
    public static final boolean DECO_BLOCKS_ABNORMALS;
    public static final boolean MUCH_MORE_MOD_COMPAT;
    public static final boolean FLYWHEEL;
    public static final boolean REPURPOSED_STRUCTURES;
    public static final boolean TETRA;
    public static final boolean POKECUBE_LEGENDS;
    public static final boolean POKECUBE;
    public static final boolean DYNAMICTREES;
    public static final boolean MOREMINECARTS;
    public static final boolean HABITAT;
    public static final boolean SIMPLEFARMING;
    public static final boolean ATMOSPHERIC;
    public static final boolean ENCHANTEDBOOKREDESIGN;
    public static final boolean COMPUTERCRAFT;
    public static final boolean CUSTOMVILLAGERTRADES;
    public static final boolean NETHERSDELIGHT;
    public static final boolean DOUBLEDOORS;
    public static final boolean MALUM;
    public static final boolean BOTANIA;
    public static final boolean MAPATLAS;
    public static final boolean WAYSTONES;
    public static final boolean OVERWEIGHT_FARMING;
    public static final boolean SNOWYSPIRIT;
    public static final boolean OREGANIZED;
    public static final boolean CLOTH_CONFIG;
    public static final boolean FLAN;

    static {
        QUARK = isLoaded("quark");
        DECO_BLOCKS = isLoaded("decorative_blocks");
        CONFIGURED = isLoaded("configured");
        OREGANIZED = isLoaded("oreganized");
        CREATE = isLoaded("create");
        TORCHSLAB = isLoaded("torchslabmod");
        CURIOS = isLoaded("curios");
        FARMERS_DELIGHT = isLoaded("farmersdelight");
        INFERNALEXP = isLoaded("infernalexp");
        INSPIRATIONS = isLoaded("inspirations");
        FRAMEDBLOCKS = isLoaded("framedblocks");
        RGBLIB = isLoaded("rgblib");
        ENDERGETIC = isLoaded("endergetic");
        DECO_BLOCKS_ABNORMALS = isLoaded("decorative_blocks_abnormals");
        MUCH_MORE_MOD_COMPAT = isLoaded("muchmoremodcompat");
        AUTUMNITY = isLoaded("autumnity");
        BUZZIER_BEES = isLoaded("buzzier_bees");
        FLYWHEEL = isLoaded("flywheel");
        REPURPOSED_STRUCTURES = isLoaded("repurposed_structures");
        TETRA = isLoaded("tetra");
        POKECUBE_LEGENDS = isLoaded("pokecube_legends");
        POKECUBE = isLoaded("pokecube");
        DYNAMICTREES = isLoaded("dynamictrees");
        MOREMINECARTS = isLoaded("moreminecarts");
        HABITAT = isLoaded("habitat");
        SIMPLEFARMING = isLoaded("simplefarming");
        ATMOSPHERIC = isLoaded("atmospheric");
        ENCHANTEDBOOKREDESIGN = isLoaded("enchantedbookredesign");
        CUSTOMVILLAGERTRADES = isLoaded("customvillagertrades");
        COMPUTERCRAFT = isLoaded("computercraft");
        NETHERSDELIGHT = isLoaded("nethers_delight");
        DOUBLEDOORS = isLoaded("doubledoors");
        MALUM = isLoaded("malum");
        BOTANIA = isLoaded("botania");
        MAPATLAS = isLoaded("map_atlases");
        WAYSTONES = isLoaded("waystones");
        OVERWEIGHT_FARMING = isLoaded("overweight_farming");
        SNOWYSPIRIT = isLoaded("snowyspirit");
        CLOTH_CONFIG = isLoaded("cloth_config");
        FLAN = isLoaded("flan");
    }

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

        //if (inspirations) CauldronRecipes.registerStuff();
    }


    public static Block DynTreesGetOptionalDynamicSapling(Item item, Level level, BlockPos worldPosition) {
        return null;
    }


    public static boolean isVerticalSlabEnabled() {
        return QUARK && QuarkCompat.isVerticalSlabEnabled();
    }
}

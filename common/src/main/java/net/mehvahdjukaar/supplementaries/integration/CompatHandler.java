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

    public static final boolean quark;
    public static final boolean deco_blocks;
    public static final boolean configured;
    public static final boolean create;
    public static final boolean torchslab;
    public static final boolean curios;
    public static final boolean farmers_delight;
    public static final boolean infernalexp;
    public static final boolean inspirations;
    public static final boolean framedblocks;
    public static final boolean rgblib;
    public static final boolean endergetic;
    public static final boolean buzzier_bees;
    public static final boolean autumnity;
    public static final boolean deco_blocks_abnormals;
    public static final boolean much_more_mod_compat;
    public static final boolean flywheel;
    public static final boolean repurposed_structures;
    public static final boolean tetra;
    public static final boolean pokecube_legends;
    public static final boolean pokecube;
    public static final boolean dynamictrees;
    public static final boolean moreminecarts;
    public static final boolean habitat;
    public static final boolean simplefarming;
    public static final boolean atmospheric;
    public static final boolean enchantedbookredesign;
    public static final boolean computercraft;
    public static final boolean customvillagertrades;
    public static final boolean nethersdelight;
    public static final boolean doubledoors;
    public static final boolean malum;
    public static final boolean botania;
    public static final boolean mapatlas;
    public static final boolean waystones;
    public static final boolean overweight_farming;
    public static final boolean snowyspirit;
    public static final boolean oreganized;
    public static final boolean cloth_config;
    public static final boolean flan;

    static {
        quark = isLoaded("quark");
        deco_blocks = isLoaded("decorative_blocks");
        configured = isLoaded("configured");
        oreganized = isLoaded("oreganized");
        create = isLoaded("create");
        torchslab = isLoaded("torchslabmod");
        curios = isLoaded("curios");
        farmers_delight = isLoaded("farmersdelight");
        infernalexp = isLoaded("infernalexp");
        inspirations = isLoaded("inspirations");
        framedblocks = isLoaded("framedblocks");
        rgblib = isLoaded("rgblib");
        endergetic = isLoaded("endergetic");
        deco_blocks_abnormals = isLoaded("decorative_blocks_abnormals");
        much_more_mod_compat = isLoaded("muchmoremodcompat");
        autumnity = isLoaded("autumnity");
        buzzier_bees = isLoaded("buzzier_bees");
        flywheel = isLoaded("flywheel");
        repurposed_structures = isLoaded("repurposed_structures");
        tetra = isLoaded("tetra");
        pokecube_legends = isLoaded("pokecube_legends");
        pokecube = isLoaded("pokecube");
        dynamictrees = isLoaded("dynamictrees");
        moreminecarts = isLoaded("moreminecarts");
        habitat = isLoaded("habitat");
        simplefarming = isLoaded("simplefarming");
        atmospheric = isLoaded("atmospheric");
        enchantedbookredesign = isLoaded("enchantedbookredesign");
        customvillagertrades = isLoaded("customvillagertrades");
        computercraft = isLoaded("computercraft");
        nethersdelight = isLoaded("nethers_delight");
        doubledoors = isLoaded("doubledoors");
        malum = isLoaded("malum");
        botania = isLoaded("botania");
        mapatlas = isLoaded("map_atlases");
        waystones = isLoaded("waystones");
        overweight_farming = isLoaded("overweight_farming");
        snowyspirit = isLoaded("snowyspirit");
        cloth_config = isLoaded("cloth_config");
        flan = isLoaded("flan");
    }

    private static boolean isLoaded(String name) {
        return PlatformHelper.isModLoaded(name);
    }

    public static void setup() {
        // if (create) CreatePlugin.initialize();
        if (computercraft) CCCompat.initialize();

        var i = Registry.ITEM.getOptional(new ResourceLocation("quark:ancient_tome"));

        i.ifPresent(b -> BlockPlacerItem.registerPlaceableItem(ModRegistry.BOOK_PILE.get(), () -> b, CommonConfigs.Tweaks.PLACEABLE_BOOKS));
    }

    public static void initOptionalRegistries() {
        if (farmers_delight) FarmersDelightCompat.init();
        if (deco_blocks) DecoBlocksCompat.init();
        if (quark) QuarkCompat.init();

        //if (inspirations) CauldronRecipes.registerStuff();
    }


    public static Block DynTreesGetOptionalDynamicSapling(Item item, Level level, BlockPos worldPosition) {
        return null;
    }


    public static boolean isVerticalSlabEnabled() {
        return quark && QuarkCompat.isVerticalSlabEnabled();
    }
}

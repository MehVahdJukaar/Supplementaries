package net.mehvahdjukaar.supplementaries.compat;


import net.mehvahdjukaar.supplementaries.compat.create.SupplementariesCreatePlugin;
import net.mehvahdjukaar.supplementaries.compat.decorativeblocks.DecoBlocksCompatRegistry;
import net.mehvahdjukaar.supplementaries.compat.farmersdelight.FDCompatRegistry;
import net.mehvahdjukaar.supplementaries.compat.inspirations.CauldronRecipes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;

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

    static {
        ModList ml = ModList.get();
        quark = ml.isLoaded("quark");
        deco_blocks = ml.isLoaded("decorative_blocks");
        configured = ml.isLoaded("configured");
        create = ml.isLoaded("create");
        torchslab = ml.isLoaded("torchslabmod");
        curios = ml.isLoaded("curios");
        farmers_delight = ml.isLoaded("farmersdelight");
        infernalexp = ml.isLoaded("infernalexp");
        inspirations = ml.isLoaded("inspirations");
        framedblocks = ml.isLoaded("framedblocks");
        rgblib = ml.isLoaded("rgblib");
        endergetic = ml.isLoaded("endergetic");
        deco_blocks_abnormals = ml.isLoaded("decorative_blocks_abnormals");
        much_more_mod_compat = ml.isLoaded("muchmoremodcompat");
        autumnity = ml.isLoaded("autumnity");
        buzzier_bees = ml.isLoaded("buzzier_bees");
        flywheel = ml.isLoaded("flywheel");
        repurposed_structures = ml.isLoaded("repurposed_structures");
        tetra = ml.isLoaded("tetra");
        pokecube_legends = ml.isLoaded("pokecube_legends");
        pokecube = ml.isLoaded("pokecube");
        dynamictrees = ml.isLoaded("dynamictrees");
        moreminecarts = ml.isLoaded("moreminecarts");
        habitat = ml.isLoaded("habitat");
        simplefarming = ml.isLoaded("simplefarming");
        atmospheric = ml.isLoaded("atmospheric");
        enchantedbookredesign = ml.isLoaded("enchantedbookredesign");
    }

    public static void init(){
        if (create) SupplementariesCreatePlugin.initialize();
    }

    public static void registerOptionalBlocks(final RegistryEvent.Register<Block> event){
        //if (deco_blocks) DecoBlocksCompatRegistry.registerBlocks(event);
        if (farmers_delight) FDCompatRegistry.registerBlocks(event);
    }

    public static void registerOptionalItems(final RegistryEvent.Register<Item> event){
        if (farmers_delight) FDCompatRegistry.registerItems(event);

    }

    public static void registerOptionalRecipes(final RegistryEvent.Register<RecipeSerializer<?>> event){
        if (inspirations) CauldronRecipes.registerRecipes(event);
    }

}

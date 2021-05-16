package net.mehvahdjukaar.supplementaries.compat;

import net.mehvahdjukaar.supplementaries.compat.create.SupplementariesCreatePlugin;
import net.mehvahdjukaar.supplementaries.compat.decorativeblocks.DecoBlocksCompatRegistry;
import net.mehvahdjukaar.supplementaries.compat.farmersdelight.FDCompatRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;
import vectorwing.farmersdelight.FarmersDelight;

public class CompatHandler {
    public static final boolean quark;
    public static final boolean deco_blocks;
    public static final boolean configured;
    public static final boolean create;
    public static final boolean torchslab;
    public static final boolean curios;
    public static final boolean farmers_delight;

    static {
        ModList ml = ModList.get();
        quark = ml.isLoaded("quark");
        deco_blocks = ml.isLoaded("decorative_blocks");
        configured = ml.isLoaded("configured");
        create = ml.isLoaded("create");
        torchslab = ml.isLoaded("torchslabmod");
        curios = ml.isLoaded("curios");
        farmers_delight = ml.isLoaded("farmersdelight");
    }

    public static void init(){
        if (create) SupplementariesCreatePlugin.initialize();
    }

    public static void registerOptionalBlocks(final RegistryEvent.Register<Block> event){
        if (deco_blocks) DecoBlocksCompatRegistry.registerBlocks(event);
        if (farmers_delight) FDCompatRegistry.registerBlocks(event);
    }

    public static void registerOptionalItems(final RegistryEvent.Register<Item> event){
        if (farmers_delight) FDCompatRegistry.registerItems(event);
    }
}

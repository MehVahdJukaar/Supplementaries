package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.blocks.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Supplementaries.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Supplementaries.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Supplementaries.MOD_ID);


    public static void init(){
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    //planter
    public static final RegistryObject<Block> PLANTER = BLOCKS.register("planter", () -> new PlanterBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA)
                    .hardnessAndResistance(2f, 6f)
                    .harvestLevel(1)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
    ));
    public static final RegistryObject<Item> PLANTER_ITEM = ITEMS.register("planter", () -> new BlockItem(PLANTER.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS)));

    //clock
    public static final RegistryObject<Block> CLOCK_BLOCK = BLOCKS.register("clock_block", () -> new ClockBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(2f, 6f)
                    .harvestLevel(0)
                    .setRequiresTool()
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<ClockBlockTile>> CLOCK_BLOCK_TILE = TILES.register("clock_block",
            () -> TileEntityType.Builder.create(ClockBlockTile::new, CLOCK_BLOCK.get()).build(null));

    public static final RegistryObject<Item> CLOCK_BLOCK_ITEM = ITEMS.register("clock_block", () -> new BlockItem(CLOCK_BLOCK.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));

    //pedestal
    public static final RegistryObject<Block> PEDESTAL = BLOCKS.register("pedestal", () -> new PedestalBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(2f, 6f)
                    .harvestLevel(1)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<PedestalBlockTile>> PEDESTAL_TILE = TILES.register("pedestal",
            () -> TileEntityType.Builder.create(PedestalBlockTile::new, PEDESTAL.get()).build(null));

    public static final RegistryObject<Item> PEDESTAL_ITEM = ITEMS.register("pedestal", () -> new BlockItem(PEDESTAL.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS)));

    //wind vane
    public static final RegistryObject<Block> WIND_VANE = BLOCKS.register("wind_vane", () -> new WindVaneBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(5f, 6f)
                    .harvestLevel(2)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<WindVaneBlockTile>> WIND_VANE_TILE = TILES.register("wind_vame",
            () -> TileEntityType.Builder.create(WindVaneBlockTile::new, WIND_VANE.get()).build(null));

    public static final RegistryObject<Item> WIND_VANE_ITEM = ITEMS.register("wind_vane", () -> new BlockItem(WIND_VANE.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));
}

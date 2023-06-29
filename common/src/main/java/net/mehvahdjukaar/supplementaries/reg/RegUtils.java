package net.mehvahdjukaar.supplementaries.reg;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CeilingBannerBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.DoubleCakeBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.common.items.BlockPlacerItem;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.common.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.common.misc.CakeRegistry;
import net.mehvahdjukaar.supplementaries.common.misc.CakeRegistry.CakeType;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.BuzzierBeesCompat;
import net.mehvahdjukaar.supplementaries.integration.CaveEnhancementsCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
//this is just stuff that could have been in registry class. split to make classes smaller
public class RegUtils {

    public static void initDynamicRegistry() {
        BlockSetAPI.registerBlockSetDefinition(CakeRegistry.INSTANCE);
        BlockSetAPI.addDynamicBlockRegistration(RegUtils::registerDoubleCakes, CakeType.class);
        BlockSetAPI.addDynamicItemRegistration(RegUtils::registerSignPostItems, WoodType.class);
        BlockSetAPI.addDynamicBlockRegistration(RegUtils::dummy, WoodType.class);
    }

    //TODO: 1.20 remove
    private static void dummy(Registrator<Block> blockRegistrator, Collection<WoodType> ts) {
        int aa = 1;
    }

    /**
     * Registers a placeable item for a modded item with the given string
     */
    public static Supplier<Block> regPlaceableItem(
            String name, Supplier<? extends Block> sup, String itemLocation, Supplier<Boolean> config) {
        Supplier<Item> itemSupp = () -> BuiltInRegistries.ITEM.get(new ResourceLocation(itemLocation));
        return regPlaceableItem(name, sup, itemSupp, config);
    }

    public static Supplier<Block> regPlaceableItem(String name, Supplier<? extends Block> sup,
                                                   Supplier<? extends Item> itemSupplier,
                                                   Supplier<Boolean> config) {
        Supplier<Block> newSupp = () -> {
            Block b = sup.get();
            BlockPlacerItem.registerPlaceableItem(b, itemSupplier, config);
            return b;
        };
        return regBlock(name, newSupp);
    }

    public static <T extends Item> Supplier<T> regItem(String name, Supplier<T> sup) {
        return RegHelper.registerItem(Supplementaries.res(name), sup);
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> regTile(String name, Supplier<T> sup) {
        return RegHelper.registerBlockEntityType(Supplementaries.res(name), sup);
    }

    public static <T extends Block> RegSupplier<T> regBlock(String name, Supplier<T> sup) {
        return RegHelper.registerBlock(Supplementaries.res(name), sup);
    }


    public static <T extends Block> RegSupplier<T> regWithItem(String name, Supplier<T> blockFactory) {
        return regWithItem(name, blockFactory, 0);
    }

    public static <T extends Block> RegSupplier<T> regWithItem(String name, Supplier<T> blockFactory, int burnTime) {
        return regWithItem(name, blockFactory, new Item.Properties(), burnTime);
    }

    public static <T extends Block> RegSupplier<T> regWithItem(String name, Supplier<T> blockFactory, Item.Properties properties, int burnTime) {
        RegSupplier<T> block = regBlock(name, blockFactory);
        regBlockItem(name, block, properties, burnTime);
        return block;
    }

    public static RegSupplier<BlockItem> regBlockItem(String name, Supplier<? extends Block> blockSup, Item.Properties properties, int burnTime) {
        return RegHelper.registerItem(Supplementaries.res(name), () -> burnTime == 0 ? new BlockItem(blockSup.get(), properties) :
                new WoodBasedBlockItem(blockSup.get(), properties, burnTime));
    }

    //candle holders
    public static Map<DyeColor, Supplier<Block>> registerCandleHolders(ResourceLocation baseName) {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();

        BlockBehaviour.Properties prop = BlockBehaviour.Properties.of()
                .noCollission()
                .pushReaction(PushReaction.DESTROY)
                .noOcclusion()
                .instabreak()
                .sound(SoundType.LANTERN);

        Supplier<Block> block = RegHelper.registerBlockWithItem(baseName,
                () -> new CandleHolderBlock(null, prop));
        map.put(null, block);

        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            String name = baseName.getPath() + "_" + color.getName();
            Supplier<Block> coloredBlock = RegHelper.registerBlockWithItem(new ResourceLocation(baseName.getNamespace(), name),
                    () -> new CandleHolderBlock(color, prop)
            );
            map.put(color, coloredBlock);
        }
        if (CompatHandler.BUZZIER_BEES) {
            BuzzierBeesCompat.registerCandle(baseName);
        }
        if (CompatHandler.CAVE_ENHANCEMENTS) {
            CaveEnhancementsCompat.registerCandle(baseName);
        }
        return map;
    }

    //flags
    public static Map<DyeColor, Supplier<Block>> registerFlags(String baseName) {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();

        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            String name = baseName + "_" + color.getName();
            Supplier<Block> block = regBlock(name, () -> new FlagBlock(color,
                    BlockBehaviour.Properties.of()
                            .ignitedByLava()
                            .mapColor(color.getMapColor())
                            .strength(1.0F)
                            .noOcclusion()
                            .sound(SoundType.WOOD))
            );
            map.put(color, block);

            regItem(name, () -> new FlagItem(block.get(), new Item.Properties()
                    .stacksTo(16)
            ));
        }
        return map;
    }

    //ceiling banners
    public static Map<DyeColor, Supplier<Block>> registerCeilingBanners(String baseName) {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();
        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            String name = baseName + "_" + color.getName();
            map.put(color, regPlaceableItem(name, () -> new CeilingBannerBlock(color,
                            BlockBehaviour.Properties.of()
                                    .ignitedByLava()
                                    .forceSolidOn()
                                    .mapColor(color.getMapColor())
                                    .strength(1.0F)
                                    .noCollission()
                                    .sound(SoundType.WOOD)
                    ), color.getName() + "_banner", CommonConfigs.Tweaks.CEILING_BANNERS
            ));
        }
        return map;
    }

    //presents
    public static Map<DyeColor, Supplier<Block>> registerPresents(String baseName, BiFunction<DyeColor, BlockBehaviour.Properties, Block> presentFactory) {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();

        Supplier<Block> block = regBlock(baseName, () -> presentFactory.apply(null,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WOOD)
                        .pushReaction(PushReaction.DESTROY)
                        .strength(1.0F)
                        .sound(ModSounds.PRESENT)));
        map.put(null, block);
        regItem(baseName, () -> new PresentItem(block.get(), new Item.Properties()));


        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            String name = baseName + "_" + color.getName();
            Supplier<Block> bb = regBlock(name, () -> presentFactory.apply(color,
                    BlockBehaviour.Properties.of()
                            .mapColor(color.getMapColor())
                            .strength(1.0F)
                            .sound(ModSounds.PRESENT))
            );
            map.put(color, bb);
            //item

            regItem(name, () -> new PresentItem(bb.get(), (new Item.Properties())));
        }
        return map;
    }

    //sign posts
    private static void registerSignPostItems(Registrator<Item> event, Collection<WoodType> woodTypes) {
        for (WoodType wood : woodTypes) {
            String name = wood.getVariantId(ModConstants.SIGN_POST_NAME);
            SignPostItem item = new SignPostItem(new Item.Properties().stacksTo(16), wood);
            wood.addChild("supplementaries:sign_post", item);
            event.register(Supplementaries.res(name), item);
            ModRegistry.SIGN_POST_ITEMS.put(wood, item);
        }
    }


    private static void registerDoubleCakes(Registrator<Block> event, Collection<CakeType> cakeTypes) {
        for (CakeType type : cakeTypes) {

            ResourceLocation id = Supplementaries.res(type.getVariantId("double"));
            DoubleCakeBlock block = new DoubleCakeBlock(type);
            type.addChild("double_cake", block);
            event.register(id, block);
            ModRegistry.DOUBLE_CAKES.put(type, block);
        }
    }

}
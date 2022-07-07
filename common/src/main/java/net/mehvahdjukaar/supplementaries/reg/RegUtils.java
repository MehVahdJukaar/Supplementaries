package net.mehvahdjukaar.supplementaries.reg;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.moonlight.api.set.BlockSetManager;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.moonlight.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.registry.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CeilingBannerBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.common.items.BlockPlacerItem;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.common.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
//this is just stuff that could have been in registry class. split to make classes smaller
public class RegUtils {

    public static void initDynamicRegistry() {
        BlockSetManager.addDynamicBlockRegistration(RegUtils::registerHangingSignBlocks, WoodType.class);
        BlockSetManager.addDynamicItemRegistration(RegUtils::registerHangingSignItems, WoodType.class);
        BlockSetManager.addDynamicItemRegistration(RegUtils::registerSignPostItems, WoodType.class);
    }

    //gets the tab given or null if the item is disabled
    public static CreativeModeTab getTab(CreativeModeTab g, String regName) {
        if (RegistryConfigs.Reg.isEnabled(regName)) {
            return ModRegistry.MOD_TAB == null ? g : ModRegistry.MOD_TAB;
        }
        return null;
    }

    public static CreativeModeTab getTab(String modId, CreativeModeTab g, String regName) {
        return PlatformHelper.isModLoaded(modId) ? getTab(g, regName) : null;
    }


    /**
     * Registers a placeable item for a modded item with the given string
     */
    public static Supplier<Block> regPlaceableItem(
            String name, Supplier<? extends Block> sup, String itemLocation, Supplier<Boolean> config) {
        Supplier<Item> itemSupp = () -> Registry.ITEM.get(new ResourceLocation(itemLocation));
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

    public static <T extends Block> Supplier<T> regBlock(String name, Supplier<T> sup) {
        return RegHelper.registerBlock(Supplementaries.res(name), sup);
    }


    public static <T extends Block> Supplier<T> regWithItem(String name, Supplier<T> blockFactory, CreativeModeTab tab) {
        return regWithItem(name, blockFactory, new Item.Properties().tab(getTab(tab, name)), 0);
    }

    public static <T extends Block> Supplier<T> regWithItem(String name, Supplier<T> blockFactory, CreativeModeTab tab, int burnTime) {
        return regWithItem(name, blockFactory, new Item.Properties().tab(getTab(tab, name)), burnTime);
    }

    public static <T extends Block> Supplier<T> regWithItem(String name, Supplier<T> blockFactory, Item.Properties properties, int burnTime) {
        Supplier<T> block = regBlock(name, blockFactory);
        regBlockItem(name, block, properties, burnTime);
        return block;
    }

    public static <T extends Block> Supplier<T> regWithItem(String name, Supplier<T> block, CreativeModeTab tab, String requiredMod) {
        CreativeModeTab t = PlatformHelper.isModLoaded(requiredMod) ? tab : null;
        return regWithItem(name, block, t);
    }

    public static Supplier<BlockItem> regBlockItem(String name, Supplier<? extends Block> blockSup, Item.Properties properties, int burnTime) {
        return RegHelper.registerItem(Supplementaries.res(name), () -> burnTime == 0 ? new BlockItem(blockSup.get(), properties) :
                new WoodBasedBlockItem(blockSup.get(), properties, burnTime));
    }

    public static Supplier<BlockItem> regBlockItem(String name, Supplier<? extends Block> blockSup, Item.Properties properties) {
        return regBlockItem(name, blockSup, properties, 0);
    }


    public static <T extends Entity> Supplier<EntityType<T>> regEntity(String name, EntityType.Builder<T> builder) {
        return ModRegistry.ENTITIES.register(name, () -> builder.build(name));
    }

    //flags
    public static Map<DyeColor, Supplier<Block>> registerFlags(String baseName) {
        ImmutableMap.Builder<DyeColor, Supplier<Block>> builder = new ImmutableMap.Builder<>();

        for (DyeColor color : DyeColor.values()) {
            String name = baseName + "_" + color.getName();
            Supplier<Block> block = regBlock(name, () -> new FlagBlock(color,
                    BlockBehaviour.Properties.of(Material.WOOD, color.getMaterialColor())
                            .strength(1.0F)
                            .noOcclusion()
                            .sound(SoundType.WOOD))
            );
            builder.put(color, block);

            regItem(name, () -> new FlagItem(block.get(), new Item.Properties()
                    .stacksTo(16)
                    .tab(getTab(CreativeModeTab.TAB_DECORATIONS, baseName))
            ));
        }
        return builder.build();
    }

    //ceiling banners
    public static Map<DyeColor, Supplier<Block>> registerCeilingBanners(String baseName) {
        Map<DyeColor, Supplier<Block>> map = new LinkedHashMap<>();
        //TODO: fix this not working
        for (DyeColor color : DyeColor.values()) {
            String name = baseName + "_" + color.getName();
            map.put(color, regPlaceableItem(name, () -> new CeilingBannerBlock(color,
                            BlockBehaviour.Properties.of(Material.WOOD, color.getMaterialColor())
                                    .strength(1.0F)
                                    .noCollission()
                                    .sound(SoundType.WOOD)
                                    .lootFrom(() -> BannerBlock.byColor(color))
                    ), color.getName() + "_banner", ServerConfigs.Tweaks.CEILING_BANNERS
            ));
        }
        return map;
    }

    //presents
    public static Map<DyeColor, Supplier<Block>> registerPresents(String baseName, BiFunction<DyeColor, BlockBehaviour.Properties, Block> presentFactory) {
        Map<DyeColor, Supplier<Block>> map = new LinkedHashMap<>();

        for (DyeColor color : DyeColor.values()) {
            String name = baseName + "_" + color.getName();
            Supplier<Block> block = regBlock(name, () -> presentFactory.apply(color,
                    BlockBehaviour.Properties.of(Material.WOOL, color.getMaterialColor())
                            .strength(1.0F)
                            .sound(ModSounds.PRESENT))
            );
            map.put(color, block);
            //item

            regItem(name, () ->
                    new PresentItem(block.get(), (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, name)), map));
        }
        Supplier<Block> block = regBlock(baseName, () -> presentFactory.apply(null,
                BlockBehaviour.Properties.of(Material.WOOL, MaterialColor.WOOD)
                        .strength(1.0F)
                        .sound(ModSounds.PRESENT)));
        map.put(null, block);
        regItem(baseName, () ->
                new PresentItem(block.get(), (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, baseName)), map));

        return map;
    }


    //hanging signs
    private static void registerHangingSignBlocks(Registrator<Block> event, Collection<WoodType> woodTypes) {
        for (WoodType wood : woodTypes) {
            String name = wood.getVariantId(RegistryConstants.HANGING_SIGN_NAME);
            HangingSignBlock block = new HangingSignBlock(
                    BlockBehaviour.Properties.of(wood.material, wood.material.getColor())
                            .strength(2f, 3f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .noCollission(),
                    wood
            );
            event.register(Supplementaries.res(name), block);
            ModRegistry.HANGING_SIGNS.put(wood, block);
        }
    }

    public static void registerHangingSignItems(Registrator<Item> event, Collection<WoodType> woodTypes) {
        for (var entry : ModRegistry.HANGING_SIGNS.entrySet()) {
            WoodType wood = entry.getKey();
            //should be there already since this is fired after block reg
            Block block = entry.getValue();
            Item item = new WoodBasedBlockItem(block,
                    new Item.Properties().stacksTo(16).tab(
                            getTab(CreativeModeTab.TAB_DECORATIONS, RegistryConstants.HANGING_SIGN_NAME)),
                    wood, 200
            );
            event.register(Utils.getID(block), item);
            ModRegistry.HANGING_SIGNS_ITEMS.put(wood, item);
        }

    }

    //sign posts
    public static void registerSignPostItems(Registrator<Item> event, Collection<WoodType> woodTypes) {
        for (WoodType wood : woodTypes) {
            String name = wood.getVariantId(RegistryConstants.SIGN_POST_NAME);
            SignPostItem item = new SignPostItem(
                    new Item.Properties().stacksTo(16).tab(
                            getTab(CreativeModeTab.TAB_DECORATIONS, RegistryConstants.SIGN_POST_NAME)),
                    wood
            );
            event.register(Supplementaries.res(name), item);
            ModRegistry.SIGN_POST_ITEMS.put(wood, item);
        }
    }


}
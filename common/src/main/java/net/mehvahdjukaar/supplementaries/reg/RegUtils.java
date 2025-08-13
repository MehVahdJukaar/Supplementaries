package net.mehvahdjukaar.supplementaries.reg;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.*;
import net.mehvahdjukaar.supplementaries.common.events.overrides.SuppAdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.items.*;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.BuzzierBeesCompat;
import net.mehvahdjukaar.supplementaries.integration.CaveEnhancementsCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
//this is just stuff that could have been in registry class. split to make classes smaller
public class RegUtils {

    protected static final BlockBehaviour.StatePredicate NEVER = (state, reader, pos) -> false;

    public static void initDynamicRegistry() {
        BlockSetAPI.addDynamicItemRegistration(RegUtils::registerSignPostItems, WoodType.class);
        BlockSetAPI.addDynamicItemRegistration(RegUtils::registerCannonBoatItems, WoodType.class);
    }

    public static void registerAdditionalPlacements() {
        //register placeable items. just static ones

        AdditionalItemPlacementsAPI.registerSimplePlacement(ModRegistry.PANCAKE_ITEM.get(), ModRegistry.PANCAKE.get());

        if (CommonConfigs.Tweaks.PLACEABLE_STICKS.get()) {
            AdditionalItemPlacementsAPI.registerPlacement(Items.STICK, new SuppAdditionalPlacement(ModRegistry.STICK_BLOCK.get()));
        }
        if (CommonConfigs.Tweaks.PLACEABLE_RODS.get()) {
            AdditionalItemPlacementsAPI.registerPlacement(Items.BLAZE_ROD, new SuppAdditionalPlacement(ModRegistry.BLAZE_ROD_BLOCK.get()));
        }
        if (CommonConfigs.Tweaks.PLACEABLE_GUNPOWDER.get()) {
            AdditionalItemPlacementsAPI.registerPlacement(Items.GUNPOWDER, new SuppAdditionalPlacement(ModRegistry.GUNPOWDER_BLOCK.get()));
        }

        if (CommonConfigs.Tools.LUNCH_BOX_PLACEABLE.get()) {
            AdditionalItemPlacementsAPI.registerPlacement(ModRegistry.LUNCH_BASKET_ITEM.get(), new AdditionalItemPlacement(ModRegistry.LUNCH_BASKET.get()) {
                @Override
                public InteractionResult overrideUseOn(UseOnContext pContext, FoodProperties foodProperties) {
                    if (!pContext.getPlayer().isSecondaryUseActive()) return InteractionResult.PASS;
                    ItemStack basket = pContext.getItemInHand();
                    var content = basket.get(ModComponents.LUNCH_BASKET_CONTENT.get());
                    if (content != null && content.canEatFrom()) return InteractionResult.PASS;
                    return super.overrideUseOn(pContext, foodProperties);
                }
            });
        }
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
        return regWithItem(name, blockFactory, new Item.Properties());
    }

    public static <T extends Block> RegSupplier<T> regWithItem(String name, Supplier<T> blockFactory, Item.Properties properties) {
        RegSupplier<T> block = regBlock(name, blockFactory);
        regBlockItem(name, block, properties);
        return block;
    }

    public static RegSupplier<BlockItem> regBlockItem(String name, Supplier<? extends Block> blockSup, Item.Properties properties) {
        return RegHelper.registerItem(Supplementaries.res(name), () -> new BlockItem(blockSup.get(), properties));
    }

    //candle holders
    public static Map<DyeColor, Supplier<Block>> registerCandleHolders(ResourceLocation baseName) {
        return registerCandleHolders(baseName, CandleHolderBlock::getParticleOffsets);
    }

    public static Map<DyeColor, Supplier<Block>> registerCandleHolders(ResourceLocation baseName,
                                                                       Function<BlockState, List<Vec3>> particleOffsets) {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();

        BlockBehaviour.Properties prop = BlockBehaviour.Properties.of()
                .noCollission()
                .pushReaction(PushReaction.DESTROY)
                .noOcclusion()
                .instabreak()
                .sound(SoundType.LANTERN);

        Supplier<Block> block = RegHelper.registerBlockWithItem(baseName,
                () -> new CandleHolderBlock(null, prop, particleOffsets));
        map.put(null, block);

        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            String name = baseName.getPath() + "_" + color.getName();
            Supplier<Block> coloredBlock = RegHelper.registerBlockWithItem(baseName.withPath(name),
                    () -> new CandleHolderBlock(color, prop, particleOffsets)
            );
            map.put(color, coloredBlock);
        }
        ModRegistry.ALL_CANDLE_HOLDERS.addAll(map.values());

        if (CompatHandler.BUZZIER_BEES) {
            BuzzierBeesCompat.registerCandle(baseName, particleOffsets);
        }
        if (CompatHandler.CAVE_ENHANCEMENTS) {
            CaveEnhancementsCompat.registerCandle(baseName, particleOffsets);
        }
        return map;
    }


    //just ceiling buntings will be returned here
    public static Map<DyeColor, Supplier<Block>> registerBuntings(String baseName) {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();
        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            BlockBehaviour.Properties prop = BlockBehaviour.Properties.of()
                    .ignitedByLava()
                    .mapColor(color.getMapColor())
                    .instabreak()
                    .noOcclusion()
                    .sound(SoundType.WOOL);
            Supplier<Block> wallBlock = regBlock(baseName + "_wall_" + color.getName(),
                    () -> new BuntingWallBlock(color, prop));

            Supplier<Block> ceilingBlock = regBlock(baseName + "_" + color.getName(),
                    () -> new BuntingCeilingBlock(color, prop));
            map.put(color, ceilingBlock);
            ModRegistry.BUNTING_WALL_BLOCKS.put(color, wallBlock);

            regItem(baseName + "_" + color.getName(), () -> new BuntingItem(color, ceilingBlock.get(), wallBlock.get(),
                    new Item.Properties(),
                    Direction.UP));
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
                    .component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
                    .stacksTo(16)
            ));
        }
        return map;
    }

    //presents
    public static Map<DyeColor, Supplier<Block>> registerPresents(
            String baseName, BiFunction<DyeColor, BlockBehaviour.Properties, Block> presentFactory,
            BiFunction<Block, Item.Properties, PresentItem> itemFactory) {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();

        Supplier<Block> block = regBlock(baseName, () -> presentFactory.apply(null,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WOOD)
                        .pushReaction(PushReaction.DESTROY)
                        .strength(0.5F)
                        .sound(ModSounds.PRESENT)));
        map.put(null, block);
        regItem(baseName, () -> itemFactory.apply(block.get(), new Item.Properties()));


        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            String name = baseName + "_" + color.getName();
            Supplier<Block> bb = regBlock(name, () -> presentFactory.apply(color,
                    BlockBehaviour.Properties.of()
                            .mapColor(color.getMapColor())
                            .pushReaction(PushReaction.DESTROY)
                            .strength(0.5F)
                            .sound(ModSounds.PRESENT))
            );
            map.put(color, bb);
            //item

            regItem(name, () -> itemFactory.apply(bb.get(), (new Item.Properties())));
        }
        return map;
    }

    //sign posts
    private static void registerSignPostItems(Registrator<Item> event, Collection<WoodType> woodTypes) {
        for (WoodType wood : woodTypes) {
            String name = wood.getVariantId(ModConstants.WAY_SIGN_NAME);
            SignPostItem item = new SignPostItem(ModRegistry.WAY_SIGN_WALL.get(),
                    new Item.Properties().stacksTo(16), wood);
            wood.addChild("supplementaries:way_sign", item);
            event.register(Supplementaries.res(name), item);
            ModRegistry.WAY_SIGN_ITEMS.put(wood, item);
        }
    }

    private static void registerCannonBoatItems(Registrator<Item> event, Collection<WoodType> woodTypes) {
        for (WoodType wood : woodTypes) {
            if (wood.getChild("boat") != null) {
                String name = wood.getVariantId(ModConstants.CANNON_BOAT_NAME);
                CannonBoatItem item = new CannonBoatItem(new Item.Properties().stacksTo(1), wood);
                wood.addChild("supplementaries:cannon_boat", item);
                event.register(Supplementaries.res(name), item);
                ModRegistry.CANNON_BOAT_ITEMS.put(wood, item);
            }
        }
    }

    public static Map<DyeColor, Supplier<Block>> registerAwnings(String baseName) {
        Map<DyeColor, Supplier<Block>> map = new Object2ObjectLinkedOpenHashMap<>();
        Supplier<Block> defAwning = regBlock(baseName, () -> new AwningBlock(null,
                BlockBehaviour.Properties.of()
                        .ignitedByLava()
                        .mapColor(MapColor.SAND)
                        .strength(1.0F)
                        .forceSolidOff()
                        .noOcclusion()
                        .sound(SoundType.WOOL))
        );
        map.put(null, defAwning);
        regItem(baseName, () -> new BlockItem(defAwning.get(), new Item.Properties()));
        for (DyeColor color : BlocksColorAPI.SORTED_COLORS) {
            String name = baseName + "_" + color.getName();
            Supplier<Block> block = regBlock(name, () -> new AwningBlock(color,
                    BlockBehaviour.Properties.of()
                            .ignitedByLava()
                            .mapColor(color.getMapColor())
                            .strength(1.0F)
                            .forceSolidOff()
                            .noOcclusion()
                            .sound(SoundType.WOOL))
            );
            map.put(color, block);

            regItem(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }
        return map;
    }
}
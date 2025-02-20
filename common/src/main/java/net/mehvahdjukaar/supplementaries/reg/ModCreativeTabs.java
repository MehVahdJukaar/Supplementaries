package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.common.items.BuntingItem;
import net.mehvahdjukaar.supplementaries.common.items.components.SoftFluidTankView;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModCreativeTabs {

    private static final Set<Item> HIDDEN_ITEMS = new HashSet<>();
    private static final List<ItemStack> NON_HIDDEN_ITEMS = new ArrayList<>();


    //my dude you are doing conditional registration here
    public static final RegSupplier<CreativeModeTab> MOD_TAB = !CommonConfigs.General.CREATIVE_TAB.get() ? null :
            RegHelper.registerCreativeModeTab(Supplementaries.res("supplementaries"),
                    (c) -> c.title(Component.translatable("tab.supplementaries.supplementaries"))
                            .icon(() -> ModRegistry.GLOBE_ITEM.get().getDefaultInstance()));

    public static final RegSupplier<CreativeModeTab> JAR_TAB = !CommonConfigs.General.JAR_TAB.get() ? null :
            RegHelper.registerCreativeModeTab(Supplementaries.res("jars"),
                    (c) -> SuppPlatformStuff.searchBar(c)
                            .title(Component.translatable("tab.supplementaries.jars"))
                            .icon(() -> ModRegistry.JAR_ITEM.get().getDefaultInstance()));

    public static void init() {
        RegHelper.addItemsToTabsRegistration(ModCreativeTabs::registerItemsToTabs);
    }

    private static boolean isRunningSetup = false;

    public static void setup() {
        isRunningSetup = true;
        List<Item> all = new ArrayList<>(BuiltInRegistries.ITEM.entrySet().stream().filter(e -> e.getKey().location().getNamespace()
                .equals(Supplementaries.MOD_ID)).map(Map.Entry::getValue).toList());
        Map<ResourceKey<CreativeModeTab>, List<ItemStack>> map = new HashMap<>();
        CreativeModeTabs.tabs().forEach(t -> map.putIfAbsent(BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(t).get(), new ArrayList<>()));
        var dummy = new RegHelper.ItemToTabEvent() {

            @Override
            public void addItems(ResourceKey<CreativeModeTab> resourceKey, @Nullable Predicate<ItemStack> predicate,
                                 boolean reverse, List<ItemStack> list) {
                var l = map.computeIfAbsent(resourceKey, t -> new ArrayList<>());
                if (reverse) {
                    var v = new ArrayList<>(list);
                    Collections.reverse(v);
                    l.addAll(v);
                } else l.addAll(list);
            }
        };
        registerItemsToTabs(dummy);
        for (var e : map.values()) {
            NON_HIDDEN_ITEMS.addAll(e);
        }

        for (var v : NON_HIDDEN_ITEMS) {
            all.remove(v.getItem());
        }
        HIDDEN_ITEMS.addAll(all);
        isRunningSetup = false;
    }

    public static boolean isHidden(Item item) {
        return HIDDEN_ITEMS.contains(item);
    }

    public static void registerItemsToTabs(RegHelper.ItemToTabEvent e) {

        if (JAR_TAB != null && !isRunningSetup) {
            if (CommonConfigs.Functional.JAR_ENABLED.get()) {
                e.addAfter(JAR_TAB.getHolder().unwrapKey().get(), null, getJars());
            }
        }
        if (MOD_TAB != null && !isRunningSetup) {
            List<ItemStack> toAdd = new ArrayList<>();
            for (var i : NON_HIDDEN_ITEMS) {
                if (toAdd.stream().noneMatch(a -> ItemStack.isSameItemSameComponents(a, i))) {
                    toAdd.add(i);
                }
            }

            e.add(MOD_TAB.getHolder().unwrapKey().get(), toAdd.toArray(ItemStack[]::new));
            return;
        }

        List<Supplier<? extends ItemLike>> sconces = new ArrayList<>(ModRegistry.SCONCES);
        sconces.add(ModRegistry.SCONCE_LEVER);

        before(e, Items.LANTERN, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SCONCE_NAME,
                sconces.toArray(Supplier[]::new));

        before(e, Items.CAMPFIRE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FIRE_PIT_NAME,
                ModRegistry.FIRE_PIT);

        before(e, Items.CHAIN, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.ROPE_NAME,
                ModRegistry.ROPE);

        after(e, Items.PEARLESCENT_FROGLIGHT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.END_STONE_LAMP_NAME,
                ModRegistry.END_STONE_LAMP);

        after(e, Items.PEARLESCENT_FROGLIGHT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.DEEPSLATE_LAMP_NAME,
                ModRegistry.DEEPSLATE_LAMP);

        after(e, Items.PEARLESCENT_FROGLIGHT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.BLACKSTONE_LAMP_NAME,
                ModRegistry.BLACKSTONE_LAMP);

        after(e, Items.PEARLESCENT_FROGLIGHT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.STONE_LAMP_NAME,
                ModRegistry.STONE_LAMP);

        after(e, Items.DAMAGED_ANVIL, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.PEDESTAL_NAME,
                ModRegistry.PEDESTAL);

        before(e, Items.COMPOSTER, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.BLACKBOARD_NAME,
                ModRegistry.BLACKBOARD);

        before(e, Items.COMPOSTER, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.NOTICE_BOARD_NAME,
                ModRegistry.NOTICE_BOARD);

        before(e, Items.COMPOSTER, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.CLOCK_BLOCK_NAME,
                ModRegistry.CLOCK_BLOCK);


        before(e, Items.COMPOSTER, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.PULLEY_BLOCK_NAME,
                ModRegistry.PULLEY_BLOCK);


        after(e, Items.JUKEBOX, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SPEAKER_BLOCK_NAME,
                ModRegistry.SPEAKER_BLOCK);

        after(e, Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.CAGE_NAME,
                ModRegistry.CAGE);

        after(e, Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.GLOBE_SEPIA_NAME,
                ModRegistry.GLOBE_SEPIA);

        after(e, Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.GLOBE_NAME,
                ModRegistry.GLOBE);

        after(e, Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.HOURGLASS_NAME,
                ModRegistry.HOURGLASS);

        after(e, Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.JAR_NAME,
                ModRegistry.JAR);

        after(e, Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.GOBLET_NAME,
                ModRegistry.GOBLET);

        after(e, Items.ARMOR_STAND, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.STATUE_NAME,
                ModRegistry.STATUE);

        after(e, Items.FLOWER_POT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.URN_NAME,
                ModRegistry.URN);

        after(e, Items.FLOWER_POT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.PLANTER_NAME,
                ModRegistry.PLANTER);

        after(e, Items.FLOWER_POT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FLOWER_BOX_NAME,
                ModRegistry.FLOWER_BOX);

        before(e, Items.DRAGON_HEAD, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.ENDERMAN_HEAD_NAME,
                ModRegistry.ENDERMAN_SKULL_ITEM);

        before(e, Items.BOOKSHELF, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.TIMBER_FRAME_NAME,
                ModRegistry.TIMBER_FRAME, ModRegistry.TIMBER_BRACE, ModRegistry.TIMBER_CROSS_BRACE);

        if (CommonConfigs.Building.WAY_SIGN_ENABLED.get() && !PlatHelper.isDev()) {
            for (var v : ModRegistry.WAY_SIGN_ITEMS.entrySet()) {
                var w = v.getKey();
                e.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, i -> {
                    if (i.is(ItemTags.HANGING_SIGNS)) {
                        var b = w.getBlockOfThis("hanging_sign");
                        return b != null && i.is(b.asItem());
                    }
                    return false;
                }, v.getValue());
            }
        }

        before(e, Items.CHEST, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.DOORMAT_NAME,
                ModRegistry.DOORMAT);

        before(e, Items.CHEST, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.ITEM_SHELF_NAME,
                ModRegistry.ITEM_SHELF);

        after(e, ItemTags.CANDLES, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.CANDLE_HOLDER_NAME,
                ModRegistry.ALL_CANDLE_HOLDERS.toArray(Supplier[]::new));

        after(e, ItemTags.CANDLES, CreativeModeTabs.COLORED_BLOCKS,
                ModConstants.CANDLE_HOLDER_NAME,
                ModRegistry.CANDLE_HOLDERS.values().toArray(Supplier[]::new));

        after(e, Items.ENDER_CHEST, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SAFE_NAME,
                ModRegistry.SAFE);

        before(e, Items.SHULKER_BOX, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SACK_NAME,
                ModRegistry.SACK);

        after(e, Items.PINK_SHULKER_BOX, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.TRAPPED_PRESENT_NAME,
                ModRegistry.TRAPPED_PRESENTS.values().toArray(Supplier[]::new));

        after(e, Items.PINK_SHULKER_BOX, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.PRESENT_NAME,
                ModRegistry.PRESENTS.values().toArray(Supplier[]::new));


        after(e, Items.PINK_SHULKER_BOX, CreativeModeTabs.COLORED_BLOCKS,
                ModConstants.TRAPPED_PRESENT_NAME,
                ModRegistry.TRAPPED_PRESENTS.values().toArray(Supplier[]::new));

        after(e, Items.PINK_SHULKER_BOX, CreativeModeTabs.COLORED_BLOCKS,
                ModConstants.PRESENT_NAME,
                ModRegistry.PRESENTS.values().toArray(Supplier[]::new));

        e.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, i -> i.is(Items.INFESTED_DEEPSLATE), getSpikeItems());

        after(e, Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FODDER_NAME,
                ModRegistry.FODDER);

        after(e, Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SUGAR_CUBE_NAME,
                ModRegistry.SUGAR_CUBE);

        after(e, Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FEATHER_BLOCK_NAME,
                ModRegistry.FEATHER_BLOCK);

        after(e, Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FLINT_BLOCK_NAME,
                ModRegistry.FLINT_BLOCK);

        after(e, Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.GRAVEL_BRICKS_NAME,
                ModRegistry.SUS_GRAVEL_BRICKS);

        after(e, Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SLIDY_BLOCK_NAME,
                ModRegistry.SLIDY_BLOCK);


        after(e, ItemTags.BANNERS, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.AWNING_NAME,
                ModRegistry.AWNINGS.values().toArray(Supplier[]::new));

        after(e, ItemTags.BANNERS, CreativeModeTabs.COLORED_BLOCKS,
                ModConstants.AWNING_NAME,
                ModRegistry.AWNINGS.values().toArray(Supplier[]::new));


        if (CommonConfigs.isEnabled(ModConstants.BUNTING_NAME)) {
            e.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, i -> i.is(ItemTags.BANNERS),
                    BuntingItem.getColored(DyeColor.WHITE));

            e.addAfter(CreativeModeTabs.COLORED_BLOCKS, i -> i.is(ItemTags.BANNERS),
                    BlocksColorAPI.SORTED_COLORS.stream().map(BuntingItem::getColored)
                            .toArray(ItemStack[]::new));
        }

        after(e, ItemTags.BANNERS, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FLAG_NAME,
                ModRegistry.FLAGS.values().toArray(Supplier[]::new));

        after(e, ItemTags.BANNERS, CreativeModeTabs.COLORED_BLOCKS,
                ModConstants.FLAG_NAME,
                ModRegistry.FLAGS.values().toArray(Supplier[]::new));


        before(e, Items.DISPENSER, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.CANNON_NAME,
                ModRegistry.CANNON);

        after(e, i -> i.getItem().components().get(DataComponents.JUKEBOX_PLAYABLE) != null, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.PIRATE_DISC_NAME,
                ModRegistry.PIRATE_DISC);

        after(e, Items.LAVA_BUCKET, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.LUMISENE_NAME,
                ModFluids.LUMISENE_BUCKET);

        after(e, Items.HONEY_BOTTLE, CreativeModeTabs.FOOD_AND_DRINKS,
                ModConstants.LUMISENE_BOTTLE_NAME,
                ModFluids.LUMISENE_BOTTLE);

        after(e, Items.FIREWORK_ROCKET, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.CONFETTI_POPPER_NAME,
                ModRegistry.CONFETTI_POPPER);

        after(e, Items.TNT_MINECART, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.DISPENSER_MINECART_NAME,
                ModRegistry.DISPENSER_MINECART_ITEM);

        after(e, Items.REDSTONE_TORCH, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.SCONCE_LEVER_NAME,
                ModRegistry.SCONCE_LEVER);

        before(e, Items.LEVER, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.CRANK_NAME,
                ModRegistry.CRANK);

        before(e, Items.PISTON, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.TURN_TABLE_NAME,
                ModRegistry.TURN_TABLE);

        before(e, Items.PISTON, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.SPRING_LAUNCHER_NAME,
                ModRegistry.SPRING_LAUNCHER);

        after(e, Items.NOTE_BLOCK, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.SPEAKER_BLOCK_NAME,
                ModRegistry.SPEAKER_BLOCK);

        after(e, Items.HOPPER, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.FAUCET_NAME,
                ModRegistry.FAUCET);

        before(e, Items.TARGET, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.COG_BLOCK_NAME,
                ModRegistry.COG_BLOCK);

        before(e, Items.NOTE_BLOCK, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.BELLOWS_NAME,
                ModRegistry.BELLOWS);

        after(e, Items.OBSERVER, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.CRYSTAL_DISPLAY_NAME,
                ModRegistry.CRYSTAL_DISPLAY);

        after(e, Items.OBSERVER, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.RELAYER_NAME,
                ModRegistry.RELAYER);

        after(e, Items.LIGHTNING_ROD, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.WIND_VANE_NAME,
                ModRegistry.WIND_VANE);

        after(e, Items.IRON_DOOR, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.NETHERITE_DOOR_NAME,
                ModRegistry.NETHERITE_DOOR);

        after(e, Items.IRON_DOOR, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.GOLD_DOOR_NAME,
                ModRegistry.GOLD_DOOR);

        after(e, Items.IRON_TRAPDOOR, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.NETHERITE_TRAPDOOR_NAME,
                ModRegistry.NETHERITE_TRAPDOOR);

        after(e, Items.IRON_TRAPDOOR, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.GOLD_TRAPDOOR_NAME,
                ModRegistry.GOLD_TRAPDOOR);

        before(e, Items.OAK_DOOR, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.LOCK_BLOCK_NAME,
                ModRegistry.LOCK_BLOCK);

        before(e, Items.REDSTONE_LAMP, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.REDSTONE_ILLUMINATOR_NAME,
                ModRegistry.REDSTONE_ILLUMINATOR);

        after(e, Items.END_CRYSTAL, CreativeModeTabs.COMBAT,
                ModConstants.CANNONBALL_NAME,
                ModRegistry.CANNONBALL_ITEM);

        after(e, Items.END_CRYSTAL, CreativeModeTabs.COMBAT,
                ModConstants.BOMB_NAME,
                ModRegistry.BOMB_ITEM, ModRegistry.BOMB_BLUE_ITEM);

        afterML(e, ModRegistry.BOMB_BLUE_ITEM.get(), CreativeModeTabs.COMBAT,
                "oreganized",
                ModConstants.BOMB_NAME,
                ModRegistry.BOMB_SPIKY_ITEM);

        before(e, Items.BOW, CreativeModeTabs.COMBAT,
                ModConstants.QUIVER_NAME,
                ModRegistry.QUIVER_ITEM);

        after(e, Items.CLOCK, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.DEPTH_METER_NAME,
                ModRegistry.ALTIMETER_ITEM);

        after(e, Items.MAP, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.SLICE_MAP_NAME,
                ModRegistry.SLICE_MAP);

        before(e, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.GOLD_DOOR_NAME,
                ModRegistry.GOLD_DOOR);

        before(e, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.GOLD_TRAPDOOR_NAME,
                ModRegistry.GOLD_TRAPDOOR);

        after(e, Items.NETHERITE_BLOCK, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.NETHERITE_TRAPDOOR_NAME,
                ModRegistry.NETHERITE_TRAPDOOR);

        after(e, Items.NETHERITE_BLOCK, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.NETHERITE_DOOR_NAME,
                ModRegistry.NETHERITE_DOOR);

        after(e, Items.SMALL_DRIPLEAF, CreativeModeTabs.NATURAL_BLOCKS,
                ModConstants.FLAX_NAME,
                ModRegistry.FLAX_WILD);

        after(e, Items.BEETROOT_SEEDS, CreativeModeTabs.NATURAL_BLOCKS,
                ModConstants.FLAX_NAME,
                ModRegistry.FLAX_SEEDS_ITEM);

        after(e, Items.HAY_BLOCK, CreativeModeTabs.NATURAL_BLOCKS,
                ModConstants.FLAX_NAME,
                ModRegistry.FLAX_BLOCK);

        after(e, Items.GRAVEL, CreativeModeTabs.NATURAL_BLOCKS,
                ModConstants.RAKED_GRAVEL_NAME,
                ModRegistry.RAKED_GRAVEL);


        after(e, Items.PUMPKIN_PIE, CreativeModeTabs.FOOD_AND_DRINKS,
                ModConstants.CANDY_NAME,
                ModRegistry.CANDY_ITEM);

        after(e, Items.PUMPKIN_PIE, CreativeModeTabs.FOOD_AND_DRINKS,
                ModConstants.PANCAKE_NAME,
                ModRegistry.PANCAKE_ITEM);

        after(e, Items.NETHER_BRICK, CreativeModeTabs.INGREDIENTS,
                ModConstants.ASH_BRICKS_NAME,
                ModRegistry.ASH_BRICK_ITEM);

        after(e, Items.GLOW_INK_SAC, CreativeModeTabs.INGREDIENTS,
                ModConstants.ANTIQUE_INK_NAME,
                ModRegistry.ANTIQUE_INK);

        after(e, Items.WHEAT, CreativeModeTabs.INGREDIENTS,
                ModConstants.FLAX_NAME,
                ModRegistry.FLAX_ITEM);

        before(e, Items.PAPER, CreativeModeTabs.INGREDIENTS,
                ModConstants.ASH_NAME,
                ModRegistry.ASH_BLOCK);

        // add(e, CreativeModeTabs.SPAWN_EGGS,
        //        ModConstants.RED_MERCHANT_NAME,
        //        ModRegistry.RED_MERCHANT_SPAWN_EGG_ITEM);

        before(e, Items.BRICKS, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.ASH_BRICKS_NAME,
                ModRegistry.ASH_BRICKS_BLOCKS.values().toArray(Supplier[]::new));


        after(e, Items.REINFORCED_DEEPSLATE, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.GRAVEL_BRICKS_NAME,
                ModRegistry.SUS_GRAVEL_BRICKS);

        after(e, Items.REINFORCED_DEEPSLATE, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.GRAVEL_BRICKS_NAME,
                ModRegistry.GRAVEL_BRICKS);

        after(e, Items.REINFORCED_DEEPSLATE, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.SLIDY_BLOCK_NAME,
                ModRegistry.SLIDY_BLOCK);

        before(e, Items.FISHING_ROD, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.LUNCH_BASKET_NAME,
                ModRegistry.LUNCH_BASKET_ITEM);

        after(e, Items.GUSTER_BANNER_PATTERN, CreativeModeTabs.INGREDIENTS,
                ModConstants.DRAGON_PATTERN_NAME,
                ModRegistry.DRAGON_PATTERN);

        before(e, Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.WRENCH_NAME,
                ModRegistry.WRENCH);

        before(e, Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.KEY_NAME,
                ModRegistry.KEY_ITEM);

        before(e, Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.SLINGSHOT_NAME,
                ModRegistry.SLINGSHOT_ITEM);


        before(e, Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.ROPE_ARROW_NAME,
                ModRegistry.ROPE_ARROW_ITEM);

        before(e, Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.SOAP_NAME,
                ModRegistry.SOAP);

        before(e, Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.BUBBLE_BLOWER_NAME,
                () -> {
                    var item = ModRegistry.BUBBLE_BLOWER.get().getDefaultInstance();
                    item.set(ModComponents.CHARGES.get(), item.get(ModComponents.MAX_CHARGES.get()));
                    return item;
                });

        after(e, i -> i.getItem() instanceof DyeItem, CreativeModeTabs.INGREDIENTS,
                ModConstants.SOAP_NAME,
                ModRegistry.SOAP);


        after(e, Items.SPECTRAL_ARROW, CreativeModeTabs.COMBAT,
                ModConstants.ROPE_ARROW_NAME,
                ModRegistry.ROPE_ARROW_ITEM);

        after(e, Items.LEAD, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.FLUTE_NAME,
                ModRegistry.FLUTE_ITEM);


        after(e, Items.MOSSY_STONE_BRICK_WALL, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.STONE_TILE_NAME,
                ModRegistry.STONE_TILE_BLOCKS.values().toArray(Supplier[]::new));

        after(e, Items.POLISHED_BLACKSTONE_BRICK_WALL, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.BLACKSTONE_TILE_NAME,
                ModRegistry.BLACKSTONE_TILE_BLOCKS.values().toArray(Supplier[]::new));

        add(e, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.LAPIS_BRICKS_NAME,
                ModRegistry.LAPIS_BRICKS_BLOCKS.values().toArray(Supplier[]::new));

        add(e, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.CHECKER_BLOCK_NAME,
                ModRegistry.CHECKER_BLOCK, ModRegistry.CHECKER_SLAB);

        add(e, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.FINE_WOOD_NAME,
                ModRegistry.FINE_WOOD, ModRegistry.FINE_WOOD_STAIRS, ModRegistry.FINE_WOOD_SLAB);

        add(e, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.DAUB_NAME,
                ModRegistry.DAUB);

        add(e, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.WATTLE_AND_DAUB,
                ModRegistry.DAUB_FRAME, ModRegistry.DAUB_BRACE, ModRegistry.DAUB_CROSS_BRACE);

        add(e, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.WICKER_FENCE_NAME,
                ModRegistry.WICKER_FENCE);

        after(e, Items.IRON_BARS, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.IRON_GATE_NAME,
                ModRegistry.IRON_GATE);

        afterML(e, "quark:gold_bars", CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.IRON_GATE_NAME,
                ModRegistry.GOLD_GATE);

        before(e, Items.COAL_BLOCK, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.SOAP_NAME,
                ModRegistry.SOAP_BLOCK);

        before(e, Items.OAK_FENCE_GATE, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.IRON_GATE_NAME,
                ModRegistry.IRON_GATE);

        after(e, Items.ARMOR_STAND, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.HAT_STAND_NAME,
                ModRegistry.HAT_STAND);

        CompatHandler.addItemsToTabs(e);

        SYNCED_ADD_TO_TABS.forEach(o -> o.accept(e));
    }

    //for supp2. ugly i know but fabric has no load order
    public static final List<Consumer<RegHelper.ItemToTabEvent>> SYNCED_ADD_TO_TABS = new ArrayList<>();

    private static void after(RegHelper.ItemToTabEvent event, TagKey<Item> target,
                              ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
        after(event, i -> i.is(target), tab, key, items);
    }

    private static void after(RegHelper.ItemToTabEvent event, Item target,
                              ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
        after(event, i -> i.is(target), tab, key, items);
    }

    private static void after(RegHelper.ItemToTabEvent event, Predicate<ItemStack> targetPred,
                              ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
        if (CommonConfigs.isEnabled(key)) {
            if (items[0].get() instanceof ItemStack) {
                ItemStack[] entries = Arrays.stream(items).map(s -> (ItemStack) s.get()).toArray(ItemStack[]::new);
                event.addAfter(tab, targetPred, entries);
            } else {
                ItemLike[] entries = Arrays.stream(items).map((s -> (ItemLike) (s.get()))).toArray(ItemLike[]::new);
                event.addAfter(tab, targetPred, entries);
            }
        }
    }

    private static void before(RegHelper.ItemToTabEvent event, Item target,
                               ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
        before(event, i -> i.is(target), tab, key, items);
    }

    private static void before(RegHelper.ItemToTabEvent event, Predicate<ItemStack> targetPred,
                               ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
        if (CommonConfigs.isEnabled(key)) {
            if (items[0].get() instanceof ItemStack) {
                ItemStack[] entries = Arrays.stream(items).map(s -> (ItemStack) s.get()).toArray(ItemStack[]::new);
                event.addBefore(tab, targetPred, entries);
            } else {
                ItemLike[] entries = Arrays.stream(items).map(s -> (ItemLike) s.get()).toArray(ItemLike[]::new);
                event.addBefore(tab, targetPred, entries);
            }
        }
    }

    private static void add(RegHelper.ItemToTabEvent event,
                            ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
        if (CommonConfigs.isEnabled(key)) {
            ItemLike[] entries = Arrays.stream(items).map((s -> (ItemLike) (s.get()))).toArray(ItemLike[]::new);
            event.add(tab, entries);
        }
    }

    private static void afterML(RegHelper.ItemToTabEvent event, Item target,
                                ResourceKey<CreativeModeTab> tab, String key, String modLoaded,
                                Supplier<?>... items) {
        if (PlatHelper.isModLoaded(modLoaded)) {
            after(event, target, tab, key, items);
        }
    }

    private static void afterML(RegHelper.ItemToTabEvent event, String modTarget,
                                ResourceKey<CreativeModeTab> tab, String key,
                                Supplier<?>... items) {
        ResourceLocation id = ResourceLocation.tryParse(modTarget);
        BuiltInRegistries.ITEM.getOptional(id).ifPresent(target -> after(event, target, tab, key, items));
    }

    private static void afterTL(RegHelper.ItemToTabEvent event, Item target,
                                ResourceKey<CreativeModeTab> tab, String key,
                                List<String> tags,
                                Supplier<?>... items) {
        if (isTagOn(tags.toArray(String[]::new))) {
            after(event, target, tab, key, items);
        }
    }

    private static void beforeML(RegHelper.ItemToTabEvent event, Item target,
                                 ResourceKey<CreativeModeTab> tab,
                                 String key, String modLoaded,
                                 Supplier<?>... items) {
        if (PlatHelper.isModLoaded(modLoaded)) {
            before(event, target, tab, key, items);
        }
    }

    private static void beforeTL(RegHelper.ItemToTabEvent event, Item target,
                                 ResourceKey<CreativeModeTab> tab, String key,
                                 List<String> tags,
                                 Supplier<?>... items) {
        if (isTagOn(tags.toArray(String[]::new))) {
            after(event, target, tab, key, items);
        }
    }

    private static boolean isTagOn(String... tags) {
        for (var t : tags)
            if (BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, ResourceLocation.parse(t))).isPresent()) {
                return true;
            }
        return false;
    }

    public static ItemStack[] getSpikeItems() {
        var items = new ArrayList<ItemStack>();
        if (CommonConfigs.Functional.BAMBOO_SPIKES_ENABLED.get()) {
            items.add(ModRegistry.BAMBOO_SPIKES_ITEM.get().getDefaultInstance());
            if (CommonConfigs.Functional.TIPPED_SPIKES_ENABLED.get() && CommonConfigs.Functional.TIPPED_SPIKES_TAB.get()) {
                items.add(makeSpikeItem(Potions.POISON));
                items.add(makeSpikeItem(Potions.LONG_POISON));
                items.add(makeSpikeItem(Potions.STRONG_POISON));
                for (var potion : BuiltInRegistries.POTION.holders().toList()) {
                    if (potion == Potions.POISON || potion == Potions.LONG_POISON || potion == Potions.STRONG_POISON)
                        continue;
                    if (BambooSpikesTippedItem.isPotionValid(new PotionContents(potion))) {
                        items.add(makeSpikeItem(potion));
                    }
                }
            }
        }
        return items.toArray(ItemStack[]::new);
    }

    public static ItemStack makeSpikeItem(Holder<Potion> pot) {
        ItemStack stack = ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get().getDefaultInstance();
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(pot));
        return stack;
    }

    private static ItemStack[] getJars() {
        List<ItemStack> items = new ArrayList<>();
        items.add(ModRegistry.JAR_ITEM.get().getDefaultInstance());
        JarBlockTile tempTile = new JarBlockTile(BlockPos.ZERO, ModRegistry.JAR.get().defaultBlockState());
        SoftFluidTank fluidHolder = SoftFluidTank.create(tempTile.getMaxStackSize());
        if (CommonConfigs.Functional.JAR_LIQUIDS.get()) {
            for (var h : SoftFluidRegistry.getHolders()) {
                var s = h.value();
                if (!s.isEnabled()) continue;
                if (MLBuiltinSoftFluids.POTION.is(h) || MLBuiltinSoftFluids.EMPTY.is(h)) continue;
                fluidHolder.clear();
                fluidHolder.setFluid(SoftFluidStack.of(h, 100));
                fluidHolder.capCapacity();
                tryAddJar(items, fluidHolder);
            }

            for (var potion : BuiltInRegistries.POTION.holders().toList()) {
                SoftFluidStack fluidStack = SoftFluidStack.of(MLBuiltinSoftFluids.POTION.getHolderUnsafe(), 100);
                fluidStack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                fluidHolder.setFluid(fluidStack);
                fluidHolder.capCapacity();
                tryAddJar(items, fluidHolder);
            }
        }
        return items.toArray(ItemStack[]::new);
    }


    private static void tryAddJar(List<ItemStack> items, SoftFluidTank tank) {
        if (!tank.isEmpty()) {
            ItemStack returnStack = new ItemStack(ModRegistry.JAR_ITEM.get());
            returnStack.set(ModComponents.SOFT_FLUID_CONTENT.get(), SoftFluidTankView.of(tank));
            for (ItemStack i : items) {
                if (i.equals(returnStack)) return;
            }
            items.add(returnStack);
        }
    }

}

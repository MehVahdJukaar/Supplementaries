package net.mehvahdjukaar.supplementaries.reg;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModCreativeTabs {

    //my dude you are doing conditional registration here
    public static final RegSupplier<CreativeModeTab> MOD_TAB = !CommonConfigs.General.CREATIVE_TAB.get() ? null :
            RegHelper.registerCreativeModeTab(Supplementaries.res("supplementaries"),
                    (c) -> c.title(Component.translatable("tab.supplementaries.supplementaries"))
                            .icon(() -> ModRegistry.GLOBE_ITEM.get().getDefaultInstance()));

    public static void init() {
        RegHelper.addItemsToTabsRegistration(ModCreativeTabs::registerItemsToTabs);
    }

    public static void registerItemsToTabs(RegHelper.ItemToTabEvent event) {

        TabAdder adder = new TabAdder(event);

        List<Supplier<? extends ItemLike>> sconces = new ArrayList<>(ModRegistry.SCONCES);
        sconces.add(ModRegistry.SCONCE_LEVER);

        adder.before(Items.LANTERN, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SCONCE_NAME,
                sconces.toArray(Supplier[]::new));

        adder.before(Items.CAMPFIRE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FIRE_PIT_NAME,
                ModRegistry.FIRE_PIT);

        adder.before(Items.CHAIN, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.ROPE_NAME,
                ModRegistry.ROPE);

        adder.after(Items.PEARLESCENT_FROGLIGHT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.END_STONE_LAMP_NAME,
                ModRegistry.END_STONE_LAMP);

        adder.after(Items.PEARLESCENT_FROGLIGHT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.DEEPSLATE_LAMP_NAME,
                ModRegistry.DEEPSLATE_LAMP);

        adder.after(Items.PEARLESCENT_FROGLIGHT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.BLACKSTONE_LAMP_NAME,
                ModRegistry.BLACKSTONE_LAMP);

        adder.after(Items.PEARLESCENT_FROGLIGHT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.STONE_LAMP_NAME,
                ModRegistry.STONE_LAMP);

        adder.after(Items.DAMAGED_ANVIL, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.PEDESTAL_NAME,
                ModRegistry.PEDESTAL);

        adder.before(Items.COMPOSTER, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.BLACKBOARD_NAME,
                ModRegistry.BLACKBOARD);

        adder.before(Items.COMPOSTER, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.NOTICE_BOARD_NAME,
                ModRegistry.NOTICE_BOARD);

        adder.before(Items.COMPOSTER, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.CLOCK_BLOCK_NAME,
                ModRegistry.CLOCK_BLOCK);


        adder.before(Items.COMPOSTER, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.PULLEY_BLOCK_NAME,
                ModRegistry.PULLEY_BLOCK);


        adder.after(Items.JUKEBOX, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SPEAKER_BLOCK_NAME,
                ModRegistry.SPEAKER_BLOCK);

        adder.after(Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.CAGE_NAME,
                ModRegistry.CAGE);

        adder.after(Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.GLOBE_SEPIA_NAME,
                ModRegistry.GLOBE_SEPIA);

        adder.after(Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.GLOBE_NAME,
                ModRegistry.GLOBE);

        adder.after(Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.HOURGLASS_NAME,
                ModRegistry.HOURGLASS);

        adder.after(Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.JAR_NAME,
                ModRegistry.JAR);

        adder.after(Items.CAULDRON, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.GOBLET_NAME,
                ModRegistry.GOBLET);

        adder.after(Items.ARMOR_STAND, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.STATUE_NAME,
                ModRegistry.STATUE);

        adder.after(Items.FLOWER_POT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.URN_NAME,
                ModRegistry.URN);

        adder.after(Items.FLOWER_POT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.PLANTER_NAME,
                ModRegistry.PLANTER);

        adder.after(Items.FLOWER_POT, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FLOWER_BOX_NAME,
                ModRegistry.FLOWER_BOX);

        adder.after(ItemTags.TRIM_TEMPLATES, CreativeModeTabs.INGREDIENTS,
                ModConstants.GALLEON_NAME,
                ModRegistry.BLAST_TRIM_TEMPLATE);

        adder.before(Items.DRAGON_HEAD, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.ENDERMAN_HEAD_NAME,
                ModRegistry.ENDERMAN_SKULL_ITEM);

        adder.after(Items.CREEPER_HEAD, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SPIDER_HEAD_NAME,
                ModRegistry.SPIDER_SKULL_ITEM);

        adder.before(Items.BOOKSHELF, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.TIMBER_FRAME_NAME,
                ModRegistry.TIMBER_FRAME, ModRegistry.TIMBER_BRACE, ModRegistry.TIMBER_CROSS_BRACE);

        if (CommonConfigs.Building.WAY_SIGN_ENABLED.get()) {
            for (var v : ModRegistry.WAY_SIGN_ITEMS.entrySet()) {
                var w = v.getKey();
                event.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, i -> {
                    if (i.is(ItemTags.HANGING_SIGNS)) {
                        var b = w.getBlockOfThis("hanging_sign");
                        return b != null && i.is(b.asItem());
                    }
                    return false;
                }, v.getValue());
            }
        }

        if (CommonConfigs.Functional.CANNON_BOAT_ENABLED.get()) {
            for (var v : ModRegistry.CANNON_BOAT_ITEMS.entrySet()) {
                var w = v.getKey();
                event.addAfter(CreativeModeTabs.TOOLS_AND_UTILITIES, i -> {
                    if (i.is(ItemTags.CHEST_BOATS)) {
                        var b = w.getItemOfThis("chest_boat");
                        return b != null && i.is(b);
                    }
                    return false;
                }, v.getValue());
            }
        }

        adder.before(Items.CHEST, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.DOORMAT_NAME,
                ModRegistry.DOORMAT);

        adder.before(Items.CHEST, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.ITEM_SHELF_NAME,
                ModRegistry.ITEM_SHELF);

        adder.after(ItemTags.CANDLES, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.CANDLE_HOLDER_NAME,
                ModRegistry.ALL_CANDLE_HOLDERS.toArray(Supplier[]::new));

        adder.after(ItemTags.CANDLES, CreativeModeTabs.COLORED_BLOCKS,
                ModConstants.CANDLE_HOLDER_NAME,
                ModRegistry.CANDLE_HOLDERS.values().toArray(Supplier[]::new));

        adder.after(Items.ENDER_CHEST, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SAFE_NAME,
                ModRegistry.SAFE);

        adder.before(Items.SHULKER_BOX, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SACK_NAME,
                ModRegistry.SACK);

        adder.after(Items.PINK_SHULKER_BOX, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.TRAPPED_PRESENT_NAME,
                ModRegistry.TRAPPED_PRESENTS.values().toArray(Supplier[]::new));

        adder.after(Items.PINK_SHULKER_BOX, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.PRESENT_NAME,
                ModRegistry.PRESENTS.values().toArray(Supplier[]::new));


        adder.after(Items.PINK_SHULKER_BOX, CreativeModeTabs.COLORED_BLOCKS,
                ModConstants.TRAPPED_PRESENT_NAME,
                ModRegistry.TRAPPED_PRESENTS.values().toArray(Supplier[]::new));

        adder.after(Items.PINK_SHULKER_BOX, CreativeModeTabs.COLORED_BLOCKS,
                ModConstants.PRESENT_NAME,
                ModRegistry.PRESENTS.values().toArray(Supplier[]::new));

        event.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, i -> i.is(Items.INFESTED_DEEPSLATE), makeSpikeItems());

        adder.after(Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FODDER_NAME,
                ModRegistry.FODDER);

        adder.after(Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SUGAR_CUBE_NAME,
                ModRegistry.SUGAR_CUBE);

        adder.after(Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FEATHER_BLOCK_NAME,
                ModRegistry.FEATHER_BLOCK);

        adder.after(Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FLINT_BLOCK_NAME,
                ModRegistry.FLINT_BLOCK);

        adder.after(Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.GRAVEL_BRICKS_NAME,
                ModRegistry.SUS_GRAVEL_BRICKS);

        adder.after(Items.INFESTED_DEEPSLATE, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SLIDY_BLOCK_NAME,
                ModRegistry.SLIDY_BLOCK);


        adder.after(ItemTags.BANNERS, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.AWNING_NAME,
                ModRegistry.AWNINGS.values().toArray(Supplier[]::new));

        adder.after(ItemTags.BANNERS, CreativeModeTabs.COLORED_BLOCKS,
                ModConstants.AWNING_NAME,
                ModRegistry.AWNINGS.values().toArray(Supplier[]::new));


        if (CommonConfigs.isEnabled(ModConstants.BUNTING_NAME)) {
            event.addAfter(CreativeModeTabs.FUNCTIONAL_BLOCKS, i -> i.is(ItemTags.BANNERS),
                    ModRegistry.BUNTING_BLOCKS.get(DyeColor.WHITE).get());

            event.addAfter(CreativeModeTabs.COLORED_BLOCKS, i -> i.is(ItemTags.BANNERS),
                    ModRegistry.BUNTING_BLOCKS.values().stream().map(Supplier::get)
                            .toArray(Block[]::new));
        }

        adder.after(ItemTags.BANNERS, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.FLAG_NAME,
                ModRegistry.FLAGS.values().toArray(Supplier[]::new));

        adder.after(ItemTags.BANNERS, CreativeModeTabs.COLORED_BLOCKS,
                ModConstants.FLAG_NAME,
                ModRegistry.FLAGS.values().toArray(Supplier[]::new));


        adder.before(Items.DISPENSER, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.CANNON_NAME,
                ModRegistry.CANNON);

        adder.after(i -> i.getItem().components().get(DataComponents.JUKEBOX_PLAYABLE) != null, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.PIRATE_DISC_NAME,
                ModRegistry.PIRATE_DISC);

        adder.after(Items.LAVA_BUCKET, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.LUMISENE_NAME,
                ModFluids.LUMISENE_BUCKET);

        adder.after(Items.HONEY_BOTTLE, CreativeModeTabs.FOOD_AND_DRINKS,
                ModConstants.LUMISENE_BOTTLE_NAME,
                ModFluids.LUMISENE_BOTTLE);

        adder.after(Items.FIREWORK_ROCKET, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.CONFETTI_POPPER_NAME,
                ModRegistry.CONFETTI_POPPER);

        adder.after(Items.TNT_MINECART, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.DISPENSER_MINECART_NAME,
                ModRegistry.DISPENSER_MINECART_ITEM);

        adder.after(Items.REDSTONE_TORCH, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.SCONCE_LEVER_NAME,
                ModRegistry.SCONCE_LEVER);

        adder.before(Items.LEVER, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.CRANK_NAME,
                ModRegistry.CRANK);

        adder.before(Items.PISTON, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.TURN_TABLE_NAME,
                ModRegistry.TURN_TABLE);

        adder.before(Items.PISTON, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.SPRING_LAUNCHER_NAME,
                ModRegistry.SPRING_LAUNCHER);

        adder.after(Items.NOTE_BLOCK, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.SPEAKER_BLOCK_NAME,
                ModRegistry.SPEAKER_BLOCK);

        adder.after(Items.HOPPER, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.FAUCET_NAME,
                ModRegistry.FAUCET);

        adder.before(Items.TARGET, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.COG_BLOCK_NAME,
                ModRegistry.COG_BLOCK);

        adder.before(Items.NOTE_BLOCK, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.BELLOWS_NAME,
                ModRegistry.BELLOWS);

        adder.after(Items.OBSERVER, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.CRYSTAL_DISPLAY_NAME,
                ModRegistry.CRYSTAL_DISPLAY);

        adder.after(Items.OBSERVER, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.RELAYER_NAME,
                ModRegistry.RELAYER);

        adder.after(Items.LIGHTNING_ROD, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.WIND_VANE_NAME,
                ModRegistry.WIND_VANE);

        adder.after(Items.IRON_DOOR, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.NETHERITE_DOOR_NAME,
                ModRegistry.NETHERITE_DOOR);

        adder.after(Items.IRON_DOOR, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.GOLD_DOOR_NAME,
                ModRegistry.GOLD_DOOR);

        adder.after(Items.IRON_TRAPDOOR, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.NETHERITE_TRAPDOOR_NAME,
                ModRegistry.NETHERITE_TRAPDOOR);

        adder.after(Items.IRON_TRAPDOOR, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.GOLD_TRAPDOOR_NAME,
                ModRegistry.GOLD_TRAPDOOR);

        adder.before(Items.OAK_DOOR, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.LOCK_BLOCK_NAME,
                ModRegistry.LOCK_BLOCK);

        adder.before(Items.REDSTONE_LAMP, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.REDSTONE_ILLUMINATOR_NAME,
                ModRegistry.REDSTONE_ILLUMINATOR);

        adder.after(Items.END_CRYSTAL, CreativeModeTabs.COMBAT,
                ModConstants.CANNONBALL_NAME,
                ModRegistry.CANNONBALL_ITEM);

        adder.after(Items.END_CRYSTAL, CreativeModeTabs.COMBAT,
                ModConstants.BOMB_NAME,
                ModRegistry.BOMB_ITEM, ModRegistry.BOMB_BLUE_ITEM);

        adder.afterML(ModRegistry.BOMB_BLUE_ITEM.get(), CreativeModeTabs.COMBAT,
                "oreganized",
                ModConstants.BOMB_NAME,
                ModRegistry.BOMB_SPIKY_ITEM);

        adder.before(Items.BOW, CreativeModeTabs.COMBAT,
                ModConstants.QUIVER_NAME,
                ModRegistry.QUIVER_ITEM);

        adder.after(Items.CLOCK, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.DEPTH_METER_NAME,
                ModRegistry.ALTIMETER_ITEM);

        adder.after(Items.MAP, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.SLICE_MAP_NAME,
                ModRegistry.SLICE_MAP);

        adder.before(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.GOLD_DOOR_NAME,
                ModRegistry.GOLD_DOOR);

        adder.before(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.GOLD_TRAPDOOR_NAME,
                ModRegistry.GOLD_TRAPDOOR);

        adder.after(Items.NETHERITE_BLOCK, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.NETHERITE_TRAPDOOR_NAME,
                ModRegistry.NETHERITE_TRAPDOOR);

        adder.after(Items.NETHERITE_BLOCK, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.NETHERITE_DOOR_NAME,
                ModRegistry.NETHERITE_DOOR);

        adder.after(Items.SMALL_DRIPLEAF, CreativeModeTabs.NATURAL_BLOCKS,
                ModConstants.FLAX_NAME,
                ModRegistry.FLAX_WILD);

        adder.after(Items.BEETROOT_SEEDS, CreativeModeTabs.NATURAL_BLOCKS,
                ModConstants.FLAX_NAME,
                ModRegistry.FLAX_SEEDS_ITEM);

        adder.after(Items.HAY_BLOCK, CreativeModeTabs.NATURAL_BLOCKS,
                ModConstants.FLAX_NAME,
                ModRegistry.FLAX_BLOCK);

        adder.after(Items.GRAVEL, CreativeModeTabs.NATURAL_BLOCKS,
                ModConstants.RAKED_GRAVEL_NAME,
                ModRegistry.RAKED_GRAVEL);


        adder.after(Items.PUMPKIN_PIE, CreativeModeTabs.FOOD_AND_DRINKS,
                ModConstants.CANDY_NAME,
                ModRegistry.CANDY_ITEM);

        adder.after(Items.PUMPKIN_PIE, CreativeModeTabs.FOOD_AND_DRINKS,
                ModConstants.PANCAKE_NAME,
                ModRegistry.PANCAKE_ITEM);

        adder.after(Items.NETHER_BRICK, CreativeModeTabs.INGREDIENTS,
                ModConstants.ASH_BRICKS_NAME,
                ModRegistry.ASH_BRICK_ITEM);

        adder.after(Items.GLOW_INK_SAC, CreativeModeTabs.INGREDIENTS,
                ModConstants.ANTIQUE_INK_NAME,
                ModRegistry.ANTIQUE_INK);

        adder.after(Items.WHEAT, CreativeModeTabs.INGREDIENTS,
                ModConstants.FLAX_NAME,
                ModRegistry.FLAX_ITEM);

        adder.before(Items.PAPER, CreativeModeTabs.INGREDIENTS,
                ModConstants.ASH_NAME,
                ModRegistry.ASH_BLOCK);

        // add(e, CreativeModeTabs.SPAWN_EGGS,
        //        ModConstants.RED_MERCHANT_NAME,
        //        ModRegistry.RED_MERCHANT_SPAWN_EGG_ITEM);

        adder.add(CreativeModeTabs.SPAWN_EGGS,
                ModConstants.PLUNDERER_NAME,
                ModRegistry.PLUNDERER_SPAWN_EGG_ITEM);

        adder.before(Items.BRICKS, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.ASH_BRICKS_NAME,
                ModRegistry.ASH_BRICKS_BLOCKS.values().toArray(Supplier[]::new));


        adder.after(Items.REINFORCED_DEEPSLATE, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.GRAVEL_BRICKS_NAME,
                ModRegistry.SUS_GRAVEL_BRICKS);

        adder.after(Items.REINFORCED_DEEPSLATE, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.GRAVEL_BRICKS_NAME,
                ModRegistry.GRAVEL_BRICKS);

        adder.after(Items.REINFORCED_DEEPSLATE, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.SLIDY_BLOCK_NAME,
                ModRegistry.SLIDY_BLOCK);

        adder.before(Items.FISHING_ROD, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.LUNCH_BASKET_NAME,
                ModRegistry.LUNCH_BASKET_ITEM);

        adder.after(Items.GUSTER_BANNER_PATTERN, CreativeModeTabs.INGREDIENTS,
                ModConstants.DRAGON_PATTERN_NAME,
                ModRegistry.DRAGON_PATTERN);

        adder.before(Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.WRENCH_NAME,
                ModRegistry.WRENCH);

        adder.before(Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.KEY_NAME,
                ModRegistry.KEY_ITEM);

        adder.before(Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.SLINGSHOT_NAME,
                ModRegistry.SLINGSHOT_ITEM);


        adder.before(Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.ROPE_ARROW_NAME,
                ModRegistry.ROPE_ARROW_ITEM);

        adder.before(Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.SOAP_NAME,
                ModRegistry.SOAP);

        adder.before(Items.FLINT_AND_STEEL, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.BUBBLE_BLOWER_NAME,
                () -> {
                    var item = ModRegistry.BUBBLE_BLOWER.get().getDefaultInstance();
                    item.set(ModComponents.CHARGES.get(), item.get(ModComponents.MAX_CHARGES.get()));
                    return item;
                });

        adder.after(i -> i.getItem() instanceof DyeItem, CreativeModeTabs.INGREDIENTS,
                ModConstants.SOAP_NAME,
                ModRegistry.SOAP);


        adder.after(Items.SPECTRAL_ARROW, CreativeModeTabs.COMBAT,
                ModConstants.ROPE_ARROW_NAME,
                ModRegistry.ROPE_ARROW_ITEM);

        adder.after(Items.LEAD, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModConstants.FLUTE_NAME,
                ModRegistry.FLUTE_ITEM);


        adder.after(Items.MOSSY_STONE_BRICK_WALL, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.STONE_TILE_NAME,
                ModRegistry.STONE_TILE_BLOCKS.values().toArray(Supplier[]::new));

        adder.after(Items.POLISHED_BLACKSTONE_BRICK_WALL, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.BLACKSTONE_TILE_NAME,
                ModRegistry.BLACKSTONE_TILE_BLOCKS.values().toArray(Supplier[]::new));

        adder.add(CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.LAPIS_BRICKS_NAME,
                ModRegistry.LAPIS_BRICKS_BLOCKS.values().toArray(Supplier[]::new));

        adder.add(CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.CHECKER_BLOCK_NAME,
                ModRegistry.CHECKER_BLOCK, ModRegistry.CHECKER_SLAB);

        adder.add(CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.FINE_WOOD_NAME,
                ModRegistry.FINE_WOOD, ModRegistry.FINE_WOOD_STAIRS, ModRegistry.FINE_WOOD_SLAB);

        adder.add(CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.DAUB_NAME,
                ModRegistry.DAUB);

        adder.add(CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.WATTLE_AND_DAUB,
                ModRegistry.DAUB_FRAME, ModRegistry.DAUB_BRACE, ModRegistry.DAUB_CROSS_BRACE);

        adder.add(CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.WICKER_FENCE_NAME,
                ModRegistry.WICKER_FENCE);

        adder.after(Items.IRON_BARS, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.GOLD_BARS_NAME,
                ModRegistry.GOLD_BARS);

        adder.after(Items.IRON_BARS, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.IRON_GATE_NAME,
                ModRegistry.IRON_GATE);

        if (CompatHandler.QUARK && QuarkCompat.isGoldBarsOn()) {
            adder.afterML("quark:gold_bars", CreativeModeTabs.BUILDING_BLOCKS,
                    ModConstants.IRON_GATE_NAME,
                    ModRegistry.GOLD_GATE);
        } else if (CommonConfigs.Building.GOLD_BARS_ENABLED.get()) {
            adder.after(ModRegistry.GOLD_BARS.get().asItem(), CreativeModeTabs.BUILDING_BLOCKS,
                    ModConstants.IRON_GATE_NAME,
                    ModRegistry.GOLD_GATE);
        }

        adder.before(Items.COAL_BLOCK, CreativeModeTabs.BUILDING_BLOCKS,
                ModConstants.SOAP_NAME,
                ModRegistry.SOAP_BLOCK);

        adder.before(Items.OAK_FENCE_GATE, CreativeModeTabs.REDSTONE_BLOCKS,
                ModConstants.IRON_GATE_NAME,
                ModRegistry.IRON_GATE);

        adder.after(Items.ARMOR_STAND, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.HAT_STAND_NAME,
                ModRegistry.HAT_STAND);

        CompatHandler.addItemsToTabs(event);

        SYNCED_ADD_TO_TABS.forEach(o -> o.accept(event));
    }


    public static final class TabAdder {
        private final RegHelper.ItemToTabEvent event;

        private final List<ItemStack> uniqueStacksAdded = new ArrayList<>();

        public TabAdder(RegHelper.ItemToTabEvent event) {
            this.event = event;
        }

        private void before(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemStack... items) {
            ResourceKey<CreativeModeTab> tabKey = getTabKey(tab);
            for (ItemStack stack : items) {
                if (isUnique(stack)) {
                    event.addBefore(tabKey, target, stack);
                }
            }
        }

        private void before(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemLike... items) {
            before(tab, target, Arrays.stream(items)
                    .map(i -> i.asItem().getDefaultInstance()).toArray(ItemStack[]::new));
        }

        private void after(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemStack... items) {
            ResourceKey<CreativeModeTab> tabKey = getTabKey(tab);
            for (ItemStack stack : items) {
                if (isUnique(stack)) {
                    event.addAfter(tabKey, target, stack);
                }
            }
        }

        private void after(ResourceKey<CreativeModeTab> tab, Predicate<ItemStack> target, ItemLike... items) {
            after(tab, target, Arrays.stream(items)
                    .map(i -> i.asItem().getDefaultInstance()).toArray(ItemStack[]::new));
        }

        private void add(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            ResourceKey<CreativeModeTab> tabKey = getTabKey(tab);
            for (ItemLike item : items) {
                ItemStack stack = item.asItem().getDefaultInstance();
                if (isUnique(stack)) {
                    event.add(tabKey, stack);
                }
            }
        }


        private ResourceKey<CreativeModeTab> getTabKey(ResourceKey<CreativeModeTab> tab) {
            return MOD_TAB == null ? tab : (ResourceKey<CreativeModeTab>) MOD_TAB.getKey();
        }

        private boolean isUnique(ItemStack stack) {
            Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(stack.getItem());
            if (MOD_TAB == null) return true;
            for (var s : uniqueStacksAdded) {
                if (s.getItem() == stack.getItem()) {
                    if (ItemStack.isSameItemSameComponents(s, stack)) {
                        return false;
                    }
                }
            }
            uniqueStacksAdded.add(stack);
            return true;
        }

        private void after(TagKey<Item> target,
                           ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
            after(i -> i.is(target), tab, key, items);
        }

        private void after(ItemLike target,
                           ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
            after(i -> i.is(target.asItem()), tab, key, items);
        }

        private void after(Predicate<ItemStack> targetPred,
                           ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
            if (CommonConfigs.isEnabled(key)) {
                var first = items[0].get();
                if (first instanceof ItemStack) {
                    ItemStack[] entries = Arrays.stream(items).map(s -> (ItemStack) s.get()).toArray(ItemStack[]::new);
                    after(tab, targetPred, entries);
                } else if (first instanceof Collection<?>) {
                    for (Object i : items) {
                        if (!(i instanceof Collection<?> c)) continue;
                        ItemLike[] entries = c.stream().map(s -> (ItemLike) s).toArray(ItemLike[]::new);
                        after(tab, targetPred, entries);
                    }
                } else {
                    ItemLike[] entries = Arrays.stream(items).map((s -> (ItemLike) (s.get()))).toArray(ItemLike[]::new);
                    after(tab, targetPred, entries);
                }
            }
        }

        private void before(ItemLike target,
                            ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
            before(i -> i.is(target.asItem()), tab, key, items);
        }

        private void before(Predicate<ItemStack> targetPred,
                            ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
            if (CommonConfigs.isEnabled(key)) {
                if (items[0].get() instanceof ItemStack) {
                    ItemStack[] entries = Arrays.stream(items).map(s -> (ItemStack) s.get()).toArray(ItemStack[]::new);
                    before(tab, targetPred, entries);
                } else {
                    ItemLike[] entries = Arrays.stream(items).map(s -> (ItemLike) s.get()).toArray(ItemLike[]::new);
                    before(tab, targetPred, entries);
                }
            }
        }

        private void add(ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
            if (CommonConfigs.isEnabled(key)) {
                ItemLike[] entries = Arrays.stream(items).map((s -> (ItemLike) (s.get()))).toArray(ItemLike[]::new);
                add(tab, entries);
            }
        }

        private void afterML(Item target,
                             ResourceKey<CreativeModeTab> tab, String key, String modLoaded,
                             Supplier<?>... items) {
            if (PlatHelper.isModLoaded(modLoaded)) {
                after(target, tab, key, items);
            }
        }

        private void afterML(String modTarget,
                             ResourceKey<CreativeModeTab> tab, String key,
                             Supplier<?>... items) {
            ResourceLocation id = ResourceLocation.tryParse(modTarget);
            BuiltInRegistries.ITEM.getOptional(id).ifPresent(target -> after(target, tab, key, items));
        }

        private void afterTL(Item target,
                             ResourceKey<CreativeModeTab> tab, String key,
                             List<String> tags,
                             Supplier<?>... items) {
            if (isTagOn(tags.toArray(String[]::new))) {
                after(target, tab, key, items);
            }
        }

        private void beforeML(Item target,
                              ResourceKey<CreativeModeTab> tab,
                              String key, String modLoaded,
                              Supplier<?>... items) {
            if (PlatHelper.isModLoaded(modLoaded)) {
                before(target, tab, key, items);
            }
        }

        private void beforeTL(Item target,
                              ResourceKey<CreativeModeTab> tab, String key,
                              List<String> tags,
                              Supplier<?>... items) {
            if (isTagOn(tags.toArray(String[]::new))) {
                after(target, tab, key, items);
            }
        }

        private static boolean isTagOn(String... tags) {
            for (var t : tags)
                if (BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, ResourceLocation.parse(t))).isPresent()) {
                    return true;
                }
            return false;
        }
    }


    //for supp2. ugly i know but fabric has no load order
    public static final List<Consumer<RegHelper.ItemToTabEvent>> SYNCED_ADD_TO_TABS = new ArrayList<>();


    private static ItemStack[] makeSpikeItems() {
        var items = new ArrayList<ItemStack>();
        if (CommonConfigs.Functional.BAMBOO_SPIKES_ENABLED.get()) {
            items.add(ModRegistry.BAMBOO_SPIKES_ITEM.get().getDefaultInstance());
            if (CommonConfigs.Functional.TIPPED_SPIKES_ENABLED.get() && CommonConfigs.Functional.TIPPED_SPIKES_TAB.get()) {
                items.add(BambooSpikesTippedItem.createItemStack(Potions.POISON));
                items.add(BambooSpikesTippedItem.createItemStack(Potions.LONG_POISON));
                items.add(BambooSpikesTippedItem.createItemStack(Potions.STRONG_POISON));
                for (var potion : BuiltInRegistries.POTION.holders().toList()) {
                    if (potion == Potions.POISON || potion == Potions.LONG_POISON || potion == Potions.STRONG_POISON)
                        continue;
                    if (BambooSpikesTippedItem.isPotionValid(new PotionContents(potion))) {
                        items.add(BambooSpikesTippedItem.createItemStack(potion));
                    }
                }
            }
        }
        return items.toArray(ItemStack[]::new);
    }


}

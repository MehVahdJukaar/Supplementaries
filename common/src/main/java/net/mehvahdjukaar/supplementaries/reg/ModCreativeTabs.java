package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModCreativeTabs {

    private static final Set<Item> HIDDEN_ITEMS = new HashSet<>();
    private static final Set<Item> ALL_ITEMS = new LinkedHashSet<>();

    public static final Supplier<CreativeModeTab> MOD_TAB = !CommonConfigs.General.CREATIVE_TAB.get() ? null :
            RegHelper.registerCreativeModeTab(Supplementaries.res("supplementaries"),
                    (c) -> c.icon(() -> ModRegistry.GLOBE_ITEM.get().getDefaultInstance()));

    public static final Supplier<CreativeModeTab> JAR_TAB = !CommonConfigs.General.JAR_TAB.get() ? null :
            RegHelper.registerCreativeModeTab(Supplementaries.res("jars"),
                    (c) -> SuppPlatformStuff.searchBar(c).icon(() -> ModRegistry.JAR_ITEM.get().getDefaultInstance()));

    public static void init() {
        RegHelper.addItemsToTabsRegistration(ModCreativeTabs::registerItemsToTabs);
    }

    public static void setup() {
        List<Item> hidden = new ArrayList<>(BuiltInRegistries.ITEM.entrySet().stream().filter(e -> e.getKey().location().getNamespace()
                .equals(Supplementaries.MOD_ID)).map(Map.Entry::getValue).toList());
        var dummy = new RegHelper.ItemToTabEvent((creativeModeTab, itemStackPredicate, aBoolean, itemStacks) -> {
            itemStacks.forEach(i -> hidden.remove(i.getItem()));
        });
        registerItemsToTabs(dummy);
        HIDDEN_ITEMS.addAll(hidden);
    }

    public static boolean isHidden(Item item) {
        return HIDDEN_ITEMS.contains(item);
    }

    public static void registerItemsToTabs(RegHelper.ItemToTabEvent e) {

        if(MOD_TAB != null){

            return;
        }

        before(e, Items.LANTERN, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SCONCE_NAME,
                ModRegistry.SCONCE, ModRegistry.SCONCE_SOUL, ModRegistry.SCONCE_LEVER);
        //TODO: modded ones

        before(e, Items.CHAIN, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.COPPER_LANTERN_NAME,
                ModRegistry.COPPER_LANTERN);

        before(e, Items.CHAIN, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.CRIMSON_LANTERN_NAME,
                ModRegistry.CRIMSON_LANTERN);

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
                ModConstants.TURN_TABLE_NAME,
                ModRegistry.TURN_TABLE);

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


        after(e, ItemTags.SIGNS, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.HANGING_SIGN_NAME,
                ModRegistry.HANGING_SIGNS.values().stream().map(i -> (Supplier<Item>) i::asItem).toArray(Supplier[]::new));

        after(e, ItemTags.SIGNS, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.SIGN_POST_NAME,
                ModRegistry.SIGN_POST_ITEMS.values().stream().map(i -> (Supplier<Item>) () -> i).toArray(Supplier[]::new));


        before(e, Items.CHEST, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModConstants.ITEM_SHELF_NAME,
                ModRegistry.ITEM_SHELF);

        after(e, ItemTags.CANDLES, CreativeModeTabs.FUNCTIONAL_BLOCKS,
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
    }

    private static void after(RegHelper.ItemToTabEvent event, TagKey<Item> target, CreativeModeTab tab, String key, Supplier<?>... items) {
        after(event, i -> i.is(target), tab, key, items);
    }

    private static void after(RegHelper.ItemToTabEvent event, Item target, CreativeModeTab tab, String key, Supplier<?>... items) {
        after(event, i -> i.is(target), tab, key, items);
    }

    private static void after(RegHelper.ItemToTabEvent event, Predicate<ItemStack> targetPred, CreativeModeTab tab, String key, Supplier<?>... items) {
        if (CommonConfigs.isEnabled(key)) {
            ItemLike[] entries = Arrays.stream(items).map((s -> (ItemLike) (s.get()))).toArray(ItemLike[]::new);
            for(var v : entries){
                if(new ItemStack(v.asItem()).isEmpty()){
                    int aa = 1;
                    return;
                }
            }
            event.addAfter(tab, targetPred, entries);
        }
    }

    private static void before(RegHelper.ItemToTabEvent event, Item target, CreativeModeTab tab, String key, Supplier<?>... items) {
        if (CommonConfigs.isEnabled(key)) {
            ItemLike[] entries = Arrays.stream(items).map(s -> (ItemLike) s.get()).toArray(ItemLike[]::new);
            for(var v : entries){
                if(new ItemStack(v.asItem()).isEmpty()){
                    int aa = 1;
                    return;
                }
            }
            event.addBefore(tab, i -> i.is(target), entries);
        }
    }

    private static void afterML(RegHelper.ItemToTabEvent event, Item target, CreativeModeTab tab, String key, String modLoaded,
                                Supplier<?>... items) {
        if (PlatHelper.isModLoaded(modLoaded)) {
            after(event, target, tab, key, items);
        }
    }

    private static void afterTL(RegHelper.ItemToTabEvent event, Item target, CreativeModeTab tab, String key,
                                List<String> tags,
                                Supplier<?>... items) {
        if (isTagOn(tags.toArray(String[]::new))) {
            after(event, target, tab, key, items);
        }
    }

    private static boolean isTagOn(String... tags) {
        for (var t : tags)
            if (BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, new ResourceLocation(t))).isPresent()) {
                return true;
            }
        return false;
    }
/*
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        //freaking bookshelf mod is calling this method before configs are loaded...
        if(!ClientConfigs.SPEC.isLoaded() ||  (ClientConfigs.Blocks.TIPPED_BAMBOO_SPIKES_TAB.get() && CommonConfigs.Functional.TIPPED_SPIKES_ENABLED.get())) {
            if (this.allowedIn(group)) {
                items.add(BambooSpikesTippedItem. makeSpikeItem(Potions.POISON));
                items.add(BambooSpikesTippedItem.makeSpikeItem(Potions.LONG_POISON));
                items.add(BambooSpikesTippedItem.makeSpikeItem(Potions.STRONG_POISON));
                for (Potion potion : BuiltInRegistries.POTION) {
                    if (potion == Potions.POISON || potion == Potions.LONG_POISON || potion == Potions.STRONG_POISON)
                        continue;
                    if (!potion.getEffects().isEmpty() && potion != Potions.EMPTY && BambooSpikesTippedItem.isPotionValid(potion)) {
                        items.add(BambooSpikesTippedItem.makeSpikeItem(potion));
                    }
                }
            }
        }
    }*/

    private static void populateJarTab(List<ItemStack> items, CreativeModeTab tab) {
        items.add(ModRegistry.JAR_ITEM.get().getDefaultInstance());
        JarBlockTile tempTile = new JarBlockTile(BlockPos.ZERO, ModRegistry.JAR.get().defaultBlockState());
        SoftFluidTank fluidHolder = SoftFluidTank.create(tempTile.getMaxStackSize());

        if (CommonConfigs.Functional.JAR_COOKIES.get()) {
            for (var i : BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.COOKIES)) {
                ItemStack regItem = new ItemStack(i);
                CompoundTag com = new CompoundTag();
                if (tempTile.canPlaceItem(0, regItem)) {
                    regItem.setCount(tempTile.getMaxStackSize());
                    ContainerHelper.saveAllItems(com, NonNullList.withSize(1, regItem));
                    tryAddJar(items, com);
                }
            }
        }
        if (CommonConfigs.Functional.JAR_LIQUIDS.get()) {
            for (SoftFluid s : SoftFluidRegistry.getValues()) {
                if (s == BuiltInSoftFluids.POTION.get() || s.isEmpty()) continue;
                CompoundTag com = new CompoundTag();
                fluidHolder.clear();
                fluidHolder.fill(s);
                fluidHolder.save(com);
                tryAddJar(items, com);
            }

            for (ResourceLocation potion : BuiltInRegistries.POTION.keySet()) {
                CompoundTag com = new CompoundTag();
                com.putString("Potion", potion.toString());
                fluidHolder.fill(BuiltInSoftFluids.POTION.get(), com);
                CompoundTag com2 = new CompoundTag();
                fluidHolder.save(com2);
                tryAddJar(items, com2);
            }
        }
    }


    private static void tryAddJar(List<ItemStack> items, CompoundTag com) {
        if (!com.isEmpty()) {
            ItemStack returnStack = new ItemStack(ModRegistry.JAR_ITEM.get());
            returnStack.addTagElement("BlockEntityTag", com);
            for (ItemStack i : items) {
                if (i.equals(returnStack)) return;
            }
            items.add(returnStack);
        }
    }

}

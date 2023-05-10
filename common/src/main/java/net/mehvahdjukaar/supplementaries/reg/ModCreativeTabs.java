package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.*;

public class ModCreativeTabs {

    private static final Set<Item> HIDDEN_ITEMS = new HashSet<>();

    public static final CreativeModeTab MOD_TAB = !CommonConfigs.General.CREATIVE_TAB.get() ? null :
            PlatHelper.createModTab(Supplementaries.res("supplementaries"),
                    () -> ModRegistry.GLOBE_ITEM.get().getDefaultInstance(), false);

    public static final CreativeModeTab JAR_TAB = !CommonConfigs.General.JAR_TAB.get() ? null :
            PlatHelper.createModTab(Supplementaries.res("jars"),
                    () -> new ItemStack(ModRegistry.JAR_ITEM.get()), true, ModCreativeTabs::populateJarTab);


    private static void tryAdd(List<ItemStack> items, CompoundTag com) {
        if (!com.isEmpty()) {
            ItemStack returnStack = new ItemStack(ModRegistry.JAR_ITEM.get());
            returnStack.addTagElement("BlockEntityTag", com);
            for (ItemStack i : items) {
                if (i.equals(returnStack)) return;
            }
            items.add(returnStack);
        }
    }

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
                    tryAdd(items, com);
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
                tryAdd(items, com);
            }

            for (ResourceLocation potion : BuiltInRegistries.POTION.keySet()) {
                CompoundTag com = new CompoundTag();
                com.putString("Potion", potion.toString());
                fluidHolder.fill(BuiltInSoftFluids.POTION.get(), com);
                CompoundTag com2 = new CompoundTag();
                fluidHolder.save(com2);
                tryAdd(items, com2);
            }
        }
    }

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

    public static boolean isHidden(Item item){
        return HIDDEN_ITEMS.contains(item);
    }

    public static void registerItemsToTabs(RegHelper.ItemToTabEvent event) {
        if (CommonConfigs.isEnabled(ModConstants.RELAYER_NAME)) {
            event.addAfter(CreativeModeTabs.REDSTONE_BLOCKS, i -> i.is(Items.REDSTONE),
                    ModRegistry.RELAYER.get());
        }

    }

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
    }


}

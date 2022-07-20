package net.mehvahdjukaar.supplementaries.reg;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.fluids.ISoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.VanillaSoftFluids;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.BucketHelper;
import net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.MobContainer;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ModCreativeTabs {

    public static final CreativeModeTab MOD_TAB =  !RegistryConfigs.CREATIVE_TAB.get() ? null :
            PlatformHelper.createModTab(Supplementaries.res("supplementaries"),
                    ()-> ModRegistry.GLOBE_ITEM.get().getDefaultInstance(), false);

    public static final CreativeModeTab JAR_TAB = !RegistryConfigs.JAR_TAB.get() ? null :
            PlatformHelper.createModTab(Supplementaries.res("jars"),
                    ModCreativeTabs::makeIcon, true, ModCreativeTabs::populateTab);


    private static ItemStack makeIcon() {
        ItemStack icon = new ItemStack(ModRegistry.JAR_ITEM.get());
        ISoftFluidTank fluidHolder = ISoftFluidTank.create(12);
        fluidHolder.fill(VanillaSoftFluids.HONEY.get());
        CompoundTag com = new CompoundTag();
        fluidHolder.save(com);
        icon.addTagElement("BlockEntityTag", com);
        return icon;
    }

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

    private static void populateTab(List<ItemStack> items, CreativeModeTab tab) {
        items.add(ModRegistry.JAR_ITEM.get().getDefaultInstance());
        JarBlockTile tempTile = new JarBlockTile(BlockPos.ZERO, ModRegistry.JAR.get().defaultBlockState());
        ISoftFluidTank fluidHolder = ISoftFluidTank.create(tempTile.getMaxStackSize());


        if (ServerConfigs.Blocks.JAR_CAPTURE.get()) {
            for (Item i : BucketHelper.getValidBuckets()) {
                CompoundTag com = new CompoundTag();
                MobContainer.MobData data = new MobContainer.MobData(new ItemStack(i));
                data.saveToTag(com);
                tryAdd(items, com);
            }
        }
        if (ServerConfigs.Blocks.JAR_COOKIES.get()) {
            //TODO: use this elsewhere
            for (var i : Registry.ITEM.getTagOrEmpty(ModTags.COOKIES)) {
                ItemStack regItem = new ItemStack(i);
                CompoundTag com = new CompoundTag();
                if (tempTile.canPlaceItem(0, regItem)) {
                    regItem.setCount(tempTile.getMaxStackSize());
                    ContainerHelper.saveAllItems(com, NonNullList.withSize(1, regItem));
                    tryAdd(items, com);
                }
            }
        }
        if (ServerConfigs.Blocks.JAR_LIQUIDS.get()) {
            for (SoftFluid s : SoftFluidRegistry.getValues()) {
                if (s == VanillaSoftFluids.POTION.get() || s.isEmpty()) continue;
                CompoundTag com = new CompoundTag();
                fluidHolder.clear();
                fluidHolder.fill(s);
                fluidHolder.save(com);
                tryAdd(items, com);
            }

            for (ResourceLocation potion : net.minecraft.core.Registry.POTION.keySet()) {
                CompoundTag com = new CompoundTag();
                com.putString("Potion", potion.toString());
                fluidHolder.fill(VanillaSoftFluids.POTION.get(), com);
                CompoundTag com2 = new CompoundTag();
                fluidHolder.save(com2);
                tryAdd(items, com2);
            }
        }
    }

}

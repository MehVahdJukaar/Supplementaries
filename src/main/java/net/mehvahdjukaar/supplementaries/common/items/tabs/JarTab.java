package net.mehvahdjukaar.supplementaries.common.items.tabs;

import net.mehvahdjukaar.moonlight.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.fluids.SoftFluidHolder;
import net.mehvahdjukaar.moonlight.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.BucketHelper;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.MobContainer;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class JarTab extends CreativeModeTab {

    public JarTab(String label) {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        ItemStack icon = new ItemStack(ModRegistry.JAR_ITEM.get());
        SoftFluidHolder fluidHolder = new SoftFluidHolder(12);
        fluidHolder.fill(SoftFluidRegistry.HONEY.get());
        CompoundTag com = new CompoundTag();
        fluidHolder.save(com);
        icon.addTagElement("BlockEntityTag", com);
        return icon;
    }

    @Override
    public boolean hasSearchBar() {
        return true;
    }

    public static void tryAdd(NonNullList<ItemStack> items, CompoundTag com) {
        if (!com.isEmpty()) {
            ItemStack returnStack = new ItemStack(ModRegistry.JAR_ITEM.get());
            returnStack.addTagElement("BlockEntityTag", com);
            for (ItemStack i : items) {
                if (i.equals(returnStack)) return;
            }
            items.add(returnStack);
        }
    }

    public static void populateTab(NonNullList<ItemStack> items) {
        items.add(ModRegistry.JAR_ITEM.get().getDefaultInstance());
        JarBlockTile tempTile = new JarBlockTile(BlockPos.ZERO, ModRegistry.JAR.get().defaultBlockState());
        SoftFluidHolder fluidHolder = new SoftFluidHolder(tempTile.getMaxStackSize());


        if(ServerConfigs.cached.JAR_CAPTURE) {
            for (Item i : BucketHelper.getValidBuckets()) {
                CompoundTag com = new CompoundTag();
                MobContainer.MobData data = new MobContainer.MobData(new ItemStack(i));
                data.saveToTag(com);
                tryAdd(items, com);
            }
        }
        if(ServerConfigs.cached.JAR_COOKIES) {
            for (Item i : ForgeRegistries.ITEMS) {
                ItemStack regItem = new ItemStack(i);
                CompoundTag com = new CompoundTag();
                if (tempTile.canPlaceItem(0, regItem)) {
                    regItem.setCount(tempTile.getMaxStackSize());
                    ContainerHelper.saveAllItems(com, NonNullList.withSize(1, regItem));
                    tryAdd(items, com);
                }
            }
        }
        if(ServerConfigs.cached.JAR_LIQUIDS) {
            for (SoftFluid s : SoftFluidRegistry.getRegisteredFluids()) {
                if (s == SoftFluidRegistry.POTION.get() || s.isEmpty()) continue;
                CompoundTag com = new CompoundTag();
                fluidHolder.clear();
                fluidHolder.fill(s);
                fluidHolder.save(com);
                tryAdd(items, com);
            }

            for (ResourceLocation potion : net.minecraft.core.Registry.POTION.keySet()) {
                CompoundTag com = new CompoundTag();
                com.putString("Potion", potion.toString());
                fluidHolder.fill(SoftFluidRegistry.POTION.get(), com);
                CompoundTag com2 = new CompoundTag();
                fluidHolder.save(com2);
                tryAdd(items, com2);
            }
        }


    }


}
